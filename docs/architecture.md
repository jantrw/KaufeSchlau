# Architektur

## Codex-Autopilot-Loop

Das Repository enthält mit `scripts/codex-autopilot.sh` einen einfachen Agent-Loop mit drei Rollen:

- Worker: setzt genau ein GitHub-Issue um
- Reviewer: prüft zuerst den Code-Diff und danach die Doku-Änderungen
- Doku-Worker: pflegt nur `README.md`, `docs/architecture.md` und `docs/documentation.md`
