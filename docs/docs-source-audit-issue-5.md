# Docs-Source-Audit Issue 5
## Ergebnis
Dokumentation weitgehend stimmig. Es gibt 4 relevante Abweichungen bzw. Lücken bei lokalem Start, Testausführung und Phase-1-Verhalten.

Status nach Korrektur: Alle Findings wurden im Branch behoben.

## Findings
- [Hoch] `PROJEKTPLAN.md`: Der dokumentierte CLI-Compose-Aufruf `docker compose --profile cli run cli list --plz 65185` läuft mit der aktuellen [`docker-compose.yml`](/A:/Development/KaufeSchlau/docker-compose.yml) nicht; `docker compose run` überschreibt den Service-Command und endet mit `exec: list: not found`. Erwartete Korrektur: funktionierenden Aufruf dokumentieren, z. B. `docker compose --profile cli run --rm cli` oder einen expliziten `sh -c`/`java -jar ... list ...`-Aufruf.
- [Mittel] `README.md`: Unter `Voraussetzung` steht nur Docker mit Compose, die dokumentierten Frontend-Checks nutzen aber Host-`npm` (`npm --prefix discounter-frontend run test|build`). Erwartete Korrektur: entweder Node/npm als zusätzliche Voraussetzung nennen oder für Frontend-Checks konsistent Compose-Befehle dokumentieren.
- [Mittel] `docs/documentation.md`: Die Regel `Händler mit Standortpflicht verlangen plz oder region` ist zu pauschal. Der Code erlaubt explizite Aldi-Auswahl ohne Standort, z. B. `GET /api/v1/prospects/aldi-sued` oder Filter nur mit `aldi-sued`. Erwartete Korrektur: Aldi-Ausnahme für explizite Auswahl und Einzelendpunkt ergänzen.
- [Niedrig] `docs/documentation.md`: Die Frontend-Ergebnisdarstellung beschreibt den Tag nur als abgeleitete Region. Im Frontend werden auch Typ-Badges wie `PLZ-basiert` und `Filiale optional` angezeigt; bei Fallback können Badge und Hinweis gleichzeitig erscheinen. Erwartete Korrektur: Badge-Verhalten als `resolvedRegion` oder `regionType` beschreiben, nicht nur als Regionsableitung und nicht exklusiv zum Hinweis.

## Geprüfte Bereiche
- Root-README, `PROJEKTPLAN.md`, `docs/architecture.md`, `docs/documentation.md`
- Sub-READMEs von Backend, CLI und Frontend
- `docker-compose.yml`
- Backend-Controller, Services, Konfiguration
- CLI-Befehle und Tests
- Frontend-API, View-Komponenten und Tests
- Verifikation über Compose: Backend-Tests, CLI-Tests, Frontend-Tests, Frontend-Build, dokumentierte CLI-Aufrufe
