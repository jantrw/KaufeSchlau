# KaufeSchlau

## Codex Autopilot

Das Skript `scripts/codex-autopilot.sh` verarbeitet offene GitHub-Issues in einem festen Loop.

`.codex-loop/` ist bewusst versioniert. Der Ordner zeigt den letzten lokalen Loop-Stand, wird aber von den eigentlichen Issue-Commits ausgeschlossen und blockiert den Loop-Start nicht.

Beispiel:

```bash
WORKER_MODEL=gpt-5.5 WORKER_REASONING_EFFORT=medium REVIEWER_MODEL=gpt-5.4 REVIEWER_REASONING_EFFORT=medium DOC_WORKER_MODEL=gpt-5.4-mini DOC_WORKER_REASONING_EFFORT=low PUSH=true ./scripts/codex-autopilot.sh
```

## Voraussetzungen

- `git`
- `gh`
- `codex` o. Ä.
- sauberer Working Tree vor Start
- Zum automatischen Schließen muss `PUSH=true` gesetzt sein, weil das Issue erst nach PR-Erstellung geschlossen wird.
