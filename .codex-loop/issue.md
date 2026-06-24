# Issue #3

Title: [feat] CLI für gefilterte und standortabhängige Prospektabfragen
URL: https://github.com/jantrw/KaufeSchlau/issues/3

## Context
Die CLI ist ein eigener Ausgabekanal für Phase 1 und muss dieselben fachlichen Regeln wie das Backend sauber transportieren.

## Acceptance Criteria
- [ ] Ein Picocli-basiertes CLI-Modul ist angelegt.
- [ ] Der Befehl `list` unterstützt `--plz`, `--region`, `--id`, `--ids` und `--format`.
- [ ] Die CLI ruft das Backend per REST auf und rendert `plain` und `json`.
- [ ] Backend-Fehler wie fehlender Standortkontext, unbekannte Händler-ID oder nicht erreichbares Backend werden verständlich ausgegeben.
- [ ] Es gibt automatisierte Tests für Argument-Parsing und mindestens einen erfolgreichen sowie einen fehlerhaften Ablauf.

## Technical Notes
Abhängig von der REST-API.
Keine direkte Händlerlogik in der CLI duplizieren.

## Out of Scope
Keine native Distribution.
Keine lokale Persistenz von Standard-PLZ oder Favoriten.
