# KaufeSchlau

## Codex Autopilot

Das Skript `scripts/codex-autopilot.sh` verarbeitet offene GitHub-Issues in einem festen Loop.

`.codex-loop/` ist bewusst versioniert. Der Ordner zeigt den letzten lokalen Loop-Stand, blockiert den Loop-Start nicht und wird zusammen mit echten Issue-Änderungen in Branch und PR mitgeführt.

Beispiel:

```bash
WORKER_MODEL=gpt-5.5 WORKER_REASONING_EFFORT=medium REVIEWER_MODEL=gpt-5.4 REVIEWER_REASONING_EFFORT=medium DOC_WORKER_MODEL=gpt-5.4-mini DOC_WORKER_REASONING_EFFORT=low PUSH=true ./scripts/codex-autopilot.sh
```
```bash
git checkout main
git pull --ff-only origin main

$env:PUSH="true"
$env:WORKER_MODEL="gpt-5.5"
$env:WORKER_REASONING_EFFORT="medium"
$env:REVIEWER_MODEL="gpt-5.4"
$env:REVIEWER_REASONING_EFFORT="medium"
$env:DOC_WORKER_MODEL="gpt-5.4-mini"
$env:DOC_WORKER_REASONING_EFFORT="low"

& "C:\Program Files\Git\bin\bash.exe" "A:\Development\KaufeSchlau\scripts\codex-autopilot.sh"
```

## Voraussetzungen

- `git`
- `gh`
- `codex` o. Ä.
- sauberer Working Tree vor Start
- Zum automatischen Schließen muss `PUSH=true` gesetzt sein, weil das Issue erst nach PR-Erstellung geschlossen wird.
