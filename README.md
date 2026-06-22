# KaufeSchlau

## Codex Autopilot

Das Skript `scripts/codex-autopilot.sh` verarbeitet offene GitHub-Issues in einem festen Loop:

1. Issue mit Label `phase-1` wählen, solange es nicht `codex-blocked`, `codex-in-progress` oder `codex-reviewed` trägt.
2. Worker bearbeitet genau dieses Issue.
3. Reviewer prüft den Diff gegen `main`.
4. Bei `CHANGES_REQUESTED` arbeitet derselbe Worker weiter am selben Issue.
5. Erst nach Code-Freigabe läuft der Doku-Worker für `README.md`, `docs/architecture.md` und `docs/documentation.md`.
6. Derselbe Reviewer prüft danach die Doku-Änderungen.
7. Erst danach wird committed und optional ein PR erstellt.

## Konfiguration

Wichtige Umgebungsvariablen:

- `BASE_BRANCH`: Zielbranch, Standard `main`
- `PHASE_LABEL`: Phasenfilter für bearbeitbare Issues, Standard `phase-1`
- `BLOCKED_LABEL`: Blockerlabel, Standard `codex-blocked`
- `IN_PROGRESS_LABEL`: Label während Bearbeitung, Standard `codex-in-progress`
- `REVIEWED_LABEL`: Label nach erfolgreichem Loop, Standard `codex-reviewed`
- `MAX_ISSUES`: maximale Anzahl Issues pro Lauf, Standard `10`
- `MAX_ROUNDS`: maximale Review-Runden für Code und Doku, Standard `5`
- `PUSH`: bei `true` werden Branch und PR erzeugt
- `WORKER_MODEL`: Modell für den Umsetzungs-Worker, Standard `gpt-5.5`
- `WORKER_REASONING_EFFORT`: Reasoning-Level für den Worker, Standard `medium`
- `REVIEWER_MODEL`: Modell für Code- und Doku-Review, Standard `gpt-5.4`
- `REVIEWER_REASONING_EFFORT`: Reasoning-Level für den Reviewer, Standard `medium`
- `DOC_WORKER_MODEL`: Modell für den Doku-Worker, Standard `gpt-5.4`
- `DOC_WORKER_REASONING_EFFORT`: Reasoning-Level für den Doku-Worker, Standard `low`

Beispiel:

```bash
WORKER_MODEL=gpt-5.5 WORKER_REASONING_EFFORT=medium REVIEWER_MODEL=gpt-5.4 REVIEWER_REASONING_EFFORT=medium DOC_WORKER_MODEL=gpt-5.4 DOC_WORKER_REASONING_EFFORT=low PUSH=true ./scripts/codex-autopilot.sh
```

## Voraussetzungen

- `git`
- `gh`
- `codex`
- sauberer Working Tree vor Start
- Zum automatischen Schließen muss `PUSH=true` gesetzt sein, weil das Issue erst nach PR-Erstellung geschlossen wird.
