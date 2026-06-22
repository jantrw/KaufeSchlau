# Dokumentation

## Nutzung des Autopilot-Skripts

Start:

```bash
./scripts/codex-autopilot.sh
```

Beispiel mit expliziten Modellen:

```bash
WORKER_MODEL=gpt-5.5 WORKER_REASONING_EFFORT=medium REVIEWER_MODEL=gpt-5.4 REVIEWER_REASONING_EFFORT=medium DOC_WORKER_MODEL=gpt-5.4 DOC_WORKER_REASONING_EFFORT=low PUSH=true ./scripts/codex-autopilot.sh
```

### Verhalten

- Es werden nur offene Issues mit dem Label `phase-1` verarbeitet.
- Issues mit `codex-blocked`, `codex-in-progress` oder `codex-reviewed` werden übersprungen.
- Ein Issue bleibt im Review-Loop, bis der Reviewer `APPROVED` liefert oder `MAX_ROUNDS` erreicht ist.
- Der Doku-Worker läuft erst nach erfolgreichem Code-Review.
- Derselbe Reviewer prüft danach mögliche Doku-Änderungen.
- Wenn keine Doku-Änderung nötig ist, meldet der Doku-Worker `DOCS_UNCHANGED`.
- Bei `PUSH=true` werden Branch und Pull Request erstellt und das Issue danach geschlossen.
- Modell und Reasoning-Level werden getrennt gesetzt; `gpt-5.5-medium` ist kein einzelner Modell-Slug.

### Abbruchfälle

- Dirty Working Tree vor Start
- fehlende CLI-Tools
- Worker oder Doku-Worker meldet `BLOCKED`
- Reviewer-Ausgabe entspricht nicht dem erwarteten Format
- maximale Review-Runden erreicht
