# Dokumentation

## Nutzung des Autopilot-Skripts

Start:

```bash
./scripts/codex-autopilot.sh
```

Beispiel mit expliziten Modellen:

```bash
WORKER_MODEL=gpt-5.5 WORKER_REASONING_EFFORT=medium REVIEWER_MODEL=gpt-5.4 REVIEWER_REASONING_EFFORT=medium DOC_WORKER_MODEL=gpt-5.4-mini DOC_WORKER_REASONING_EFFORT=low PUSH=true ./scripts/codex-autopilot.sh
```

### Verhalten

- Wenn mehrere passende Issues offen sind, wird immer die kleinste Issue-Nummer zuerst genommen.
- Issues mit `codex-blocked`, `codex-in-progress` oder `codex-reviewed` werden übersprungen.
- Existiert der erwartete Issue-Branch schon lokal, setzt der Loop dort fort.
- Existiert er nur auf `origin`, legt der Loop einen lokalen Tracking-Branch darauf an.
- Nur wenn noch kein Issue-Branch existiert, erstellt der Loop ihn neu von `main`.
- `.codex-loop/issue.md` enthält den Issue-Body und zusätzlich die vorhandenen GitHub-Kommentare.
- Ein Issue bleibt im Review-Loop, bis der Reviewer `APPROVED` liefert oder mehr als `MAX_ROUNDS` Änderungsschleifen nötig wären.
- Der Doku-Worker läuft erst nach erfolgreichem Code-Review.
- Derselbe Reviewer prüft danach mögliche Doku-Änderungen.
- Bei neuen Features oder neuen lokalen Run-/Testwegen in Backend, CLI oder Frontend ist Doku Pflicht.
- `README.md` deckt nur Setup, Start und Testbefehle ab.
- `docs/architecture.md` deckt nur Modulgrenzen, Verantwortlichkeiten und Integrationsfluss ab.
- `docs/documentation.md` deckt nur Verhalten, Nutzung, Validierung und Beispiele ab.
- Doppelte Aussagen zwischen diesen drei Dateien sollen vermieden statt umformuliert werden.
- `DOCS_UNCHANGED` ist nur für reine Tests, nicht-funktionale Refactors oder interne Korrekturen ohne Doku-Auswirkung zulässig.
- Bei `PUSH=true` werden Branch und Pull Request erstellt und das Issue danach geschlossen.
- Wenn in einem Resume-Lauf kein neuer Commit entsteht, der Branch aber bereits einen echten Diff gegen `main` hat, erstellt der Loop den fehlenden PR trotzdem.
- Modell und Reasoning-Level werden getrennt gesetzt; `gpt-5.5-medium` ist kein einzelner Modell-Slug.
- `.codex-loop/` ist versioniert, wird beim Clean-Check ignoriert und zusammen mit echten Issue-Änderungen in Branch und PR mitgeführt.
- Ein reiner `.codex-loop`-Diff reicht nicht für Commit, PR oder Issue-Abschluss.

### Abbruchfälle

- Dirty Working Tree vor Start
- fehlende CLI-Tools
- Worker oder Doku-Worker meldet `BLOCKED`
- Reviewer-Ausgabe entspricht nicht dem erwarteten Format
- maximale Review-Runden erreicht
