# Issue #2

Title: [feat] Backend-API für Prospektlinks und Validierungsfehler
URL: https://github.com/jantrw/KaufeSchlau/issues/2

## Context
Nach der Backend-Basis muss das System die offiziellen Prospekt-Einstiegspunkte per REST bereitstellen und ungültige Aufrufe fachlich korrekt ablehnen.

## Acceptance Criteria
- [ ] `GET /api/v1/prospects` und `GET /api/v1/prospects/{id}` sind gemäß Projektplan implementiert.
- [ ] Abruf ohne PLZ, Region oder erlaubten Händlerfilter liefert den Fehler `LOCATION_REQUIRED` mit verständlicher Begründung.
- [ ] Gefilterte Händler ohne Standortpflicht funktionieren ohne PLZ oder Region.
- [ ] Responses enthalten die nötigen Felder für URL, Auflösungsart, Standortpflicht und Fallback-Hinweise.
- [ ] Controller- oder Integrationstests decken die erlaubten und abgelehnten Aufrufvarianten ab.

## Technical Notes
Baut auf der Backend-Basis auf.
Für REWE, EDEKA und Netto reicht in Phase 1 die offizielle Einstiegsseite als Fallback.

## Out of Scope
Keine marktgenaue Filialauflösung.
Keine periodischen URL-Prüfungen.
