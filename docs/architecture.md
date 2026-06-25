# Architektur

## Codex-Autopilot-Loop

Das Repository enthält mit `scripts/codex-autopilot.sh` einen einfachen Agent-Loop mit drei Rollen:

- Worker: setzt genau ein GitHub-Issue um
- Reviewer: prüft zuerst den Code-Diff und danach die Doku-Änderungen
- Doku-Worker: pflegt nur `README.md`, `docs/architecture.md` und `docs/documentation.md`

Die Issue-Auswahl erfolgt nach aufsteigender Issue-Nummer innerhalb der offenen passenden `phase-1`-Issues.
`.codex-loop/` ist Teil des Repos und dient als sichtbarer lokaler Laufzustand. Der Ordner wird zusammen mit echten Issue-Änderungen im Branch mitgeführt, aber ein reiner `.codex-loop`-Diff gilt nicht als umgesetztes Issue.
Existiert zu einem Issue bereits ein lokaler oder Remote-Branch, verwendet der Loop diesen weiter statt ihn neu von `main` aufzusetzen.
`issue.md` enthält neben Titel, URL und Body auch die vorhandenen GitHub-Issue-Kommentare als zusätzlichen Arbeitskontext.
`MAX_ROUNDS` begrenzt die Zahl der Review-Änderungsschleifen; ein letzter Fix erhält noch eine abschließende Reviewer-Prüfung.
Ein Resume-Lauf kann einen fehlenden PR nachholen, wenn auf dem Issue-Branch bereits ein fachlicher Diff gegen `main` existiert.
Bei neuen Features in Backend, CLI oder Frontend darf der Doku-Worker nicht `DOCS_UNCHANGED` liefern; dann sind README-, Architektur- oder Nutzungsupdates Pflicht.
