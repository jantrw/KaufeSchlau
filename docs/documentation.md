# Dokumentation

## REST-Endpunkte

Alle Prospekte:

```text
GET /api/v1/prospects
```

Einzelner Händler:

```text
GET /api/v1/prospects/{id}
```

## Unterstützte Query-Parameter

- `plz`
  - fünfstellige Postleitzahl
- `region`
  - Region oder Bundesland für die Phase-1-Auflösung
- `retailerIds`
  - kommaseparierte Händler-IDs

## Backend-Verhalten

- Ohne Händlerfilter liefert das Backend alle Händler zurück, soweit die Standortregeln erfüllt sind.
- Händler mit Standortpflicht verlangen `plz` oder `region`.
- Aldi Nord und Aldi Süd werden bei passender Anfrage anhand von PLZ oder Region gefiltert.
- Für standortabhängige Händler liefert Phase 1 nur den offiziellen Einstiegspunkt plus Hinweis auf spätere Auflösung.

## Backend-Fehlerfälle

- `400 LOCATION_REQUIRED`
  - Standortkontext fehlt für die gewählte Händlerauswahl
- `400 INVALID_REQUEST`
  - PLZ oder Region ist fachlich ungültig
- `404 RETAILER_NOT_FOUND`
  - angefragte Händler-ID existiert nicht

## CLI-Befehl

Der Branch führt den Befehl `list` ein:

```text
list [--plz <plz>] [--region <region>] [--id <id>] [--ids <id1,id2>] [--format plain|json]
```

Die CLI zeigt die verfügbare Struktur auch direkt per `discounter --help` und `discounter list --help`.

## CLI-Parameter

- `--plz`
  - fünfstellige Postleitzahl
- `--region`
  - Region oder Bundesland
- `--id`
  - einzelner Händler
- `--ids`
  - mehrere Händler als CSV
- `--format`
  - `plain` oder `json`

## CLI-Verhalten

- Bei `plain` werden Name, URL und optionale Hinweise zeilenweise ausgegeben.
- Bei `json` wird der Backend-Body unverändert ausgegeben.
- `--id` und `--ids` werden intern zu einem gemeinsamen Händlerfilter zusammengeführt.
- Die CLI übernimmt Backend-Fehler als Terminal-Fehlerausgabe und endet mit Exit-Code `1`.

## CLI-Fehlerfälle

- Backend nicht erreichbar
  - Ausgabe: `Backend nicht erreichbar: <url>`
- Backend liefert Fehler-JSON
  - Ausgabe mit Fehlercode und Nachricht aus dem Response-Body
- Unterbrochener Request
  - Ausgabe: `Backend-Aufruf abgebrochen.`

## Oberfläche

Die Startseite enthält:

- Händlerauswahl
- PLZ- und Regionseingabe
- Laden-Button
- Ergebnisliste mit Prospektkarten

## Frontend-Verhalten

- Ohne Auswahl werden alle Händler betrachtet.
- Sobald ausgewählte Händler Standortkontext brauchen, verlangt die Oberfläche PLZ oder Region.
- Eine unvollständige PLZ wie `123` wird auch dann als Fehler angezeigt, wenn Standort nur optional wäre.
- Backend-Fehler werden als verständliche Meldung in der Ergebnisliste angezeigt.

## Frontend-Ergebnisdarstellung

- Jede Karte zeigt Händlername und Prospektlink.
- Wenn eine Region sauber ableitbar ist, erscheint sie als Tag.
- Wenn das Backend nur einen offiziellen Einstiegspunkt liefert, zeigt die Karte einen Hinweis statt einer erfundenen Region.
- Händler mit Filialpflicht oder Fallback-Verhalten zeigen zusätzliche Hinweistexte.

## Frontend-API

Das Frontend ruft auf:

```text
GET /api/v1/prospects
```

Die Response darf entweder:

- ein Array von Prospekten
- oder ein Objekt mit `items`

sein. Das Frontend normalisiert beide Varianten auf dasselbe Modell.

## Frontend-Checks

- `npm --prefix discounter-frontend run test`
  - deckt Standortpflicht, Fehlerdarstellung und Ergebnisdarstellung ab
- `npm --prefix discounter-frontend run build`
  - prüft TypeScript und den Production-Build

## Beispiele

Backend mit PLZ:

```bash
curl "http://localhost:8080/api/v1/prospects?plz=65185"
```

CLI mit Plain-Text-Ausgabe:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --plz 65185
```

CLI mit JSON-Ausgabe:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --ids lidl,rewe --plz 65185 --format json
```
