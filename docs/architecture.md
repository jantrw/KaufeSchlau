# Architektur

## Codex-Autopilot-Loop

Das Repository enthält mit `scripts/codex-autopilot.sh` einen einfachen Agent-Loop mit drei Rollen:

- Worker: setzt genau ein GitHub-Issue um
- Reviewer: prüft zuerst den Code-Diff und danach die Doku-Änderungen
- Doku-Worker: pflegt nur `README.md`, `docs/architecture.md` und `docs/documentation.md`

Die Issue-Auswahl erfolgt nach aufsteigender Issue-Nummer innerhalb der offenen passenden `phase-1`-Issues.
`.codex-loop/` ist Teil des Repos und dient als sichtbarer lokaler Laufzustand. Der Ordner wird zusammen mit echten Issue-Änderungen im Branch mitgeführt, aber ein reiner `.codex-loop`-Diff gilt nicht als umgesetztes Issue.
