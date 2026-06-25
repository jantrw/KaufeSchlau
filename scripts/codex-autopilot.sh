#!/usr/bin/env bash
set -euo pipefail

BASE_BRANCH="${BASE_BRANCH:-main}"
PHASE_LABEL="${PHASE_LABEL:-phase-1}"
BLOCKED_LABEL="${BLOCKED_LABEL:-codex-blocked}"
IN_PROGRESS_LABEL="${IN_PROGRESS_LABEL:-codex-in-progress}"
REVIEWED_LABEL="${REVIEWED_LABEL:-codex-reviewed}"
MAX_ISSUES="${MAX_ISSUES:-10}"
MAX_ROUNDS="${MAX_ROUNDS:-5}"
PUSH="${PUSH:-false}"

WORKER_MODEL="${WORKER_MODEL:-gpt-5.5}"
WORKER_REASONING_EFFORT="${WORKER_REASONING_EFFORT:-medium}"
REVIEWER_MODEL="${REVIEWER_MODEL:-gpt-5.4}"
REVIEWER_REASONING_EFFORT="${REVIEWER_REASONING_EFFORT:-medium}"
DOC_WORKER_MODEL="${DOC_WORKER_MODEL:-gpt-5.4-mini}"
DOC_WORKER_REASONING_EFFORT="${DOC_WORKER_REASONING_EFFORT:-low}"
LOOP_DIR="${LOOP_DIR:-.codex-loop}"

mkdir -p "$LOOP_DIR"

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing command: $1"
    exit 1
  }
}

need git
need gh
need codex

ensure_clean_tree() {
  local status
  status="$(git status --porcelain=v1 --untracked-files=all | grep -v " $LOOP_DIR/" || true)"

  if [[ -n "$status" ]]; then
    echo "Working tree is dirty. Commit or stash first."
    exit 1
  fi
}

slugify() {
  echo "$1" \
    | sed -e 's/Ä/Ae/g' -e 's/Ö/Oe/g' -e 's/Ü/Ue/g' -e 's/ä/ae/g' -e 's/ö/oe/g' -e 's/ü/ue/g' -e 's/ß/ss/g' \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//' \
    | cut -c1-60
}

codex_exec() {
  local sandbox="$1"
  local output_file="$2"
  local model="$3"
  local reasoning_effort="$4"
  local prompt="$5"
  local -a args

  args=(
    -a
    never
    exec
    --sandbox "$sandbox"
    --output-last-message "$output_file"
  )

  if [[ -n "$model" ]]; then
    args+=(--model "$model")
  fi

  if [[ -n "$reasoning_effort" ]]; then
    args+=(-c "model_reasoning_effort=\"$reasoning_effort\"")
  fi

  codex "${args[@]}" "$prompt"
}

next_issue_number() {
  gh issue list \
    --state open \
    --search "label:${PHASE_LABEL} -label:${BLOCKED_LABEL} -label:${REVIEWED_LABEL} -label:${IN_PROGRESS_LABEL}" \
    --limit 100 \
    --json number \
    --jq 'sort_by(.number) | .[0].number // empty'
}

issue_field() {
  local number="$1"
  local field="$2"

  gh issue view "$number" --json "$field" --jq ".${field}"
}

issue_comments_markdown() {
  local number="$1"

  gh issue view "$number" --json comments --jq '
    [
      .comments[]
      | select((.body // "") != "")
      | "### Kommentar von @" + (.author.login // "unknown") + " am " + .createdAt + "\n\n" + .body
    ] | join("\n\n")
  '
}

has_local_branch() {
  local branch="$1"
  git show-ref --verify --quiet "refs/heads/$branch"
}

has_remote_branch() {
  local branch="$1"
  git show-ref --verify --quiet "refs/remotes/origin/$branch"
}

branch_has_diff_against_base() {
  local branch="$1"
  local merge_base

  merge_base="$(git merge-base "$BASE_BRANCH" "$branch")"
  ! git diff --quiet "$merge_base" "$branch"
}

pr_exists_for_branch() {
  local branch="$1"
  gh pr list --head "$branch" --state all --json number --jq 'length > 0'
}

branch_requires_docs() {
  local branch="$1"
  local merge_base

  merge_base="$(git merge-base "$BASE_BRANCH" "$branch")"
  git diff --name-only "$merge_base" "$branch" | grep -Eq \
    '^(discounter-backend/src/main/|discounter-backend/src/main/resources/|discounter-cli/src/main/|discounter-frontend/src/|docker-compose\.yml)$'
}

checkout_issue_branch() {
  local branch="$1"

  if has_local_branch "$branch"; then
    echo "Reusing local branch $branch"
    git checkout "$branch"
    return
  fi

  if has_remote_branch "$branch"; then
    echo "Reusing remote branch $branch"
    git checkout -b "$branch" --track "origin/$branch"
    return
  fi

  echo "Creating new branch $branch from $BASE_BRANCH"
  git checkout "$BASE_BRANCH"
  git pull --ff-only origin "$BASE_BRANCH" || true
  git checkout -b "$branch"
}

init_loop_dir() {
  mkdir -p "$LOOP_DIR"

  for file in issue.md review.md worker.md doc-worker.md; do
    if [[ ! -f "$LOOP_DIR/$file" ]]; then
      : > "$LOOP_DIR/$file"
    fi
  done
}

run_worker_initial() {
  codex_exec \
    workspace-write \
    .codex-loop/worker.md \
    "$WORKER_MODEL" \
    "$WORKER_REASONING_EFFORT" \
    "$(cat <<'EOF'
$issue-worker

Bearbeite das Issue aus .codex-loop/issue.md.

Regeln:
- Bearbeite genau dieses eine Issue.
- Mache die kleinste korrekte Änderung.
- Keine unrelated Refactors.
- Aktualisiere Doku-Dateien nicht selbst, außer das Issue verlangt explizit Doku als Kernumfang.
- Führe relevante Tests, Linter oder Typechecks aus.
- Prüfe selbst `git diff`.
- Wenn fertig: schreibe am Ende exakt `READY_FOR_REVIEW`.
- Wenn blockiert: schreibe am Ende exakt `BLOCKED`.
EOF
)"
}

run_worker_fix() {
  codex_exec \
    workspace-write \
    .codex-loop/worker.md \
    "$WORKER_MODEL" \
    "$WORKER_REASONING_EFFORT" \
    "$(cat <<'EOF'
$issue-worker

Lies .codex-loop/review.md.

Wenn dort CHANGES_REQUESTED steht:
- Setze alle Review-Punkte um.
- Ändere nur, was nötig ist.
- Aktualisiere Doku-Dateien nicht selbst, außer das Issue verlangt explizit Doku als Kernumfang.
- Führe relevante Tests, Linter oder Typechecks aus.
- Prüfe selbst `git diff`.
- Schreibe am Ende exakt `READY_FOR_REVIEW`.

Wenn du blockiert bist:
- Erkläre kurz warum.
- Schreibe am Ende exakt `BLOCKED`.
EOF
)"
}

run_reviewer() {
  codex_exec \
    read-only \
    .codex-loop/review.md \
    "$REVIEWER_MODEL" \
    "$REVIEWER_REASONING_EFFORT" \
    "$(cat <<EOF
\$code-reviewer

Reviewe den aktuellen Diff gegen $BASE_BRANCH.

Kontext:
- Issue steht in .codex-loop/issue.md.
- Worker-Zusammenfassung steht in .codex-loop/worker.md.

Prüfe:
- Erfüllt der Code das Issue?
- Gibt es Bugs, Edge Cases oder Regressionen?
- Sind Tests ausreichend?
- Gibt es Security-, Datenverlust- oder Concurrency-Probleme?
- Ist die Änderung unnötig groß?

Wichtig:
- Nicht schreiben.
- Nicht fixen.
- Nur reviewen.

Antworte exakt mit einem dieser Formate:

APPROVED

oder

CHANGES_REQUESTED:
- konkreter Punkt
- konkreter Punkt
EOF
)"
}

run_doc_worker_initial() {
  codex_exec \
    workspace-write \
    .codex-loop/doc-worker.md \
    "$DOC_WORKER_MODEL" \
    "$DOC_WORKER_REASONING_EFFORT" \
    "$(cat <<'EOF'
$issue-worker

Lies .codex-loop/issue.md, .codex-loop/worker.md und den aktuellen Git-Diff.

Deine Aufgabe:
- Bearbeite nur `discounter-backend/README.md`, `discounter-cli/README.md`, `discounter-frontend/README.md`, `docs/architecture.md` und `docs/documentation.md`.
- Die Root-`README.md` bleibt tabu.
- Bei neuen Features oder neuen Lauf-/Testwegen in Backend, CLI oder Frontend ist Doku Pflicht.
- Modul-READMEs: nur modulnahes Setup, lokale Startbefehle, Testbefehle und schneller Einstieg.
- `docs/architecture.md`: nur Modulgrenzen, Verantwortlichkeiten, Integrationsfluss und technische Entscheidungen.
- `docs/documentation.md`: nur API-, CLI- und UI-Verhalten, Validierung, Hinweise und Nutzungsbeispiele.
- Schreibe nichts doppelt. Wenn ein Punkt in eine Datei gehört, erwähne ihn in den anderen Dateien höchstens kurz oder gar nicht.
- Dokumentiere nur reale Verhaltens-, Architektur- oder Nutzungsänderungen.
- Reine Dependency-, Build- oder Lockfile-Änderungen ohne neue Bedien- oder Laufauswirkung brauchen keine Doku.
- Keine allgemeinen Umschreibungen, kein Doku-Refactor, kein Fülltext.
- Führe nach Doku-Änderungen einen kurzen Self-Check auf Konsistenz mit dem Diff durch.

Wenn Doku angepasst wurde:
- schreibe am Ende exakt `READY_FOR_REVIEW`.

Wenn wirklich keine Doku-Änderung nötig ist:
- schreibe am Ende exakt `DOCS_UNCHANGED`.

Wenn blockiert:
- Erkläre kurz warum.
- schreibe am Ende exakt `BLOCKED`.
EOF
)"
}

run_doc_worker_fix() {
  codex_exec \
    workspace-write \
    .codex-loop/doc-worker.md \
    "$DOC_WORKER_MODEL" \
    "$DOC_WORKER_REASONING_EFFORT" \
    "$(cat <<'EOF'
$issue-worker

Lies .codex-loop/issue.md, .codex-loop/worker.md und .codex-loop/review.md.

Wenn dort CHANGES_REQUESTED steht:
- Setze alle Doku-Review-Punkte um.
- Bearbeite nur `discounter-backend/README.md`, `discounter-cli/README.md`, `discounter-frontend/README.md`, `docs/architecture.md` und `docs/documentation.md`.
- Halte die Doku knapp und deckungsgleich mit dem Code-Diff.
- Entferne doppelte Aussagen zwischen README, Architektur und Nutzungsdoku statt sie umzuschreiben.
- Führe einen kurzen Self-Check auf Konsistenz mit dem Diff durch.
- Schreibe am Ende exakt `READY_FOR_REVIEW`.

Wenn blockiert:
- Erkläre kurz warum.
- schreibe am Ende exakt `BLOCKED`.
EOF
)"
}

run_reviewer_docs() {
  codex_exec \
    read-only \
    .codex-loop/review.md \
    "$REVIEWER_MODEL" \
    "$REVIEWER_REASONING_EFFORT" \
    "$(cat <<EOF
\$code-reviewer

Reviewe nur die Dokumentationsänderungen im aktuellen Diff gegen $BASE_BRANCH.

Kontext:
- Issue steht in .codex-loop/issue.md.
- Worker-Zusammenfassung steht in .codex-loop/worker.md.
- Doku-Worker-Zusammenfassung steht in .codex-loop/doc-worker.md.

Prüfe:
- Sind die geänderten Modul-READMEs sowie `docs/architecture.md` und `docs/documentation.md` korrekt zum tatsächlichen Code-Diff?
- Fehlt bei neuen Features oder neuen Run-/Testwegen die passende Modul-README oder eine der beiden Doku-Dateien?
- Bleibt die Root-`README.md` unberührt?
- Ist jede Modul-README auf Setup/Run/Test fokussiert statt Architektur oder Feature-Details zu wiederholen?
- Ist `docs/architecture.md` nur Architektur und nicht Nutzungsdoku in anderem Wortlaut?
- Ist `docs/documentation.md` nur Verhalten/Nutzung und nicht Architektur oder README-Duplikat?
- Fehlt eine wichtige Nutzungs- oder Architekturinfo?
- Enthält die Doku Behauptungen, die der Code nicht erfüllt?
- Ist die Änderung knapp statt aufgebläht oder doppelt?

Wichtig:
- Nicht schreiben.
- Nicht fixen.
- Nur reviewen.

Antworte exakt mit einem dieser Formate:

APPROVED

oder

CHANGES_REQUESTED:
- konkreter Punkt
- konkreter Punkt
EOF
)"
}

mark_issue_blocked() {
  local number="$1"
  local file="$2"

  gh issue comment "$number" --body-file "$file" || true
  gh issue edit "$number" --add-label "$BLOCKED_LABEL" >/dev/null 2>&1 || true
  gh issue edit "$number" --remove-label "$IN_PROGRESS_LABEL" >/dev/null 2>&1 || true
}

main() {
  init_loop_dir
  ensure_clean_tree

  git fetch origin "$BASE_BRANCH" || true

  processed=0

  while [[ "$processed" -lt "$MAX_ISSUES" ]]; do
    number="$(next_issue_number)"

    if [[ -z "$number" ]]; then
      echo "No open issues ready for processing."
      exit 0
    fi

    title="$(issue_field "$number" title)"
    body="$(issue_field "$number" body)"
    url="$(issue_field "$number" url)"
    comments="$(issue_comments_markdown "$number")"

    branch="codex/issue-${number}-$(slugify "$title")"

    echo
    echo "=== Working on issue #$number: $title ==="

    checkout_issue_branch "$branch"
    init_loop_dir

    cat > "$LOOP_DIR/issue.md" <<EOF
# Issue #$number

Title: $title
URL: $url

$body
EOF

    if [[ -n "$comments" ]]; then
      cat >> "$LOOP_DIR/issue.md" <<EOF

## Kommentare

$comments
EOF
    fi

    gh issue edit "$number" --add-label "$IN_PROGRESS_LABEL" >/dev/null 2>&1 || true

    run_worker_initial

    if grep -q "BLOCKED" "$LOOP_DIR/worker.md"; then
      echo "Worker blocked on issue #$number."
      mark_issue_blocked "$number" "$LOOP_DIR/worker.md"
      exit 1
    fi

    round=1
    code_change_rounds=0

    while true; do
      echo
      echo "--- Code review round $round for issue #$number ---"

      run_reviewer

      if grep -q "^APPROVED" "$LOOP_DIR/review.md"; then
        echo "Code reviewer approved issue #$number."
        break
      fi

      if grep -q "^CHANGES_REQUESTED" "$LOOP_DIR/review.md"; then
        if [[ "$code_change_rounds" -ge "$MAX_ROUNDS" ]]; then
          echo "Max code review rounds reached for issue #$number."
          mark_issue_blocked "$number" "$LOOP_DIR/review.md"
          exit 1
        fi

        echo "Reviewer requested code changes. Worker continues."
        run_worker_fix

        if grep -q "BLOCKED" "$LOOP_DIR/worker.md"; then
          echo "Worker blocked while fixing review feedback."
          mark_issue_blocked "$number" "$LOOP_DIR/worker.md"
          exit 1
        fi

        code_change_rounds=$((code_change_rounds + 1))
        round=$((round + 1))
        continue
      fi

      echo "Reviewer output was invalid:"
      cat "$LOOP_DIR/review.md"
      exit 1
    done

    run_doc_worker_initial

    if grep -q "BLOCKED" "$LOOP_DIR/doc-worker.md"; then
      echo "Doc worker blocked on issue #$number."
      mark_issue_blocked "$number" "$LOOP_DIR/doc-worker.md"
      exit 1
    fi

    if grep -q "DOCS_UNCHANGED" "$LOOP_DIR/doc-worker.md"; then
      if branch_requires_docs "$branch"; then
        echo "Doc worker skipped required documentation for issue #$number."
        cat > "$LOOP_DIR/review.md" <<'EOF'
CHANGES_REQUESTED:
- Für diesen Diff ist Doku Pflicht. Ergänze die passende Modul-README für Setup/Run/Test sowie die nötigen Architektur-/Nutzungsdetails in docs/architecture.md und docs/documentation.md ohne doppelte Aussagen.
EOF
        mark_issue_blocked "$number" "$LOOP_DIR/review.md"
        exit 1
      fi

      echo "Doc worker reports no documentation changes needed."
    else
      doc_round=1
      doc_change_rounds=0

      while true; do
        echo
        echo "--- Doc review round $doc_round for issue #$number ---"

        run_reviewer_docs

        if grep -q "^APPROVED" "$LOOP_DIR/review.md"; then
          echo "Reviewer approved docs for issue #$number."
          break
        fi

        if grep -q "^CHANGES_REQUESTED" "$LOOP_DIR/review.md"; then
          if [[ "$doc_change_rounds" -ge "$MAX_ROUNDS" ]]; then
            echo "Max doc review rounds reached for issue #$number."
            mark_issue_blocked "$number" "$LOOP_DIR/review.md"
            exit 1
          fi

          echo "Reviewer requested doc changes. Doc worker continues."
          run_doc_worker_fix

          if grep -q "BLOCKED" "$LOOP_DIR/doc-worker.md"; then
            echo "Doc worker blocked while fixing review feedback."
            mark_issue_blocked "$number" "$LOOP_DIR/doc-worker.md"
            exit 1
          fi

          doc_change_rounds=$((doc_change_rounds + 1))
          doc_round=$((doc_round + 1))
          continue
        fi

        echo "Reviewer output for docs was invalid:"
        cat "$LOOP_DIR/review.md"
        exit 1
      done
    fi

    git add -A

    if git diff --cached --quiet -- . ":(exclude)$LOOP_DIR"; then
      echo "No changes to commit for issue #$number."
      git reset --quiet HEAD -- "$LOOP_DIR" >/dev/null 2>&1 || true
      git checkout -- "$LOOP_DIR" >/dev/null 2>&1 || true

      if [[ "$PUSH" == "true" ]] && branch_has_diff_against_base "$branch" && [[ "$(pr_exists_for_branch "$branch")" != "true" ]]; then
        git push -u origin "$branch"
        gh pr create \
          --title "Fix #$number: $title" \
          --body "Automated Codex worker/reviewer/doc loop for #$number." \
          --base "$BASE_BRANCH" \
          --head "$branch"
        gh issue close "$number" --comment "Geschlossen nach automatischer PR-Erstellung für #$number."
      fi

      gh issue edit "$number" --remove-label "$IN_PROGRESS_LABEL" >/dev/null 2>&1 || true
      gh issue edit "$number" --add-label "$REVIEWED_LABEL" >/dev/null 2>&1 || true
      processed=$((processed + 1))
      continue
    fi

    git commit -m "Fix #$number: $title"

    if [[ "$PUSH" == "true" ]]; then
      git push -u origin "$branch"
      gh pr create \
        --title "Fix #$number: $title" \
        --body "Automated Codex worker/reviewer/doc loop for #$number." \
        --base "$BASE_BRANCH" \
        --head "$branch"
      gh issue close "$number" --comment "Geschlossen nach automatischer PR-Erstellung für #$number."
    fi

    gh issue edit "$number" --remove-label "$IN_PROGRESS_LABEL" >/dev/null 2>&1 || true
    gh issue edit "$number" --add-label "$REVIEWED_LABEL" >/dev/null 2>&1 || true

    processed=$((processed + 1))
  done

  echo
  echo "Done. Processed $processed issue(s)."
}

main "$@"
