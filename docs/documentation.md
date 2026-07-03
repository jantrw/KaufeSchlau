# Dokumentation

## Lokale Reproduktion

Start, CLI-Aufrufe und Checks stehen im `README.md`.
Diese Datei beschreibt das Verhalten der Codebase.

## REST-Endpunkte

Alle Prospekte:

```text
GET /api/v1/prospects
```

Einzelner Händler:

```text
GET /api/v1/prospects/{id}
```

## Query-Parameter

Der Endpunkt akzeptiert Standortkontext (`plz` oder `region`) und optional `retailerIds` als kommaseparierte Händlerliste.

## Backend-Verhalten

- Ohne Händlerfilter liefert das Backend alle Händler zurück, soweit die Standortregeln erfüllt sind.
- Händler mit Standortpflicht verlangen `plz` oder `region`.
- Explizit gewählte Aldi-Varianten sind die Ausnahme: `aldi-nord` oder `aldi-sued` dürfen ohne Standort geladen werden.
- Bei automatischer Aldi-Auswahl wird Nord oder Süd anhand von PLZ oder Region gefiltert.
- Für standortabhängige Händler liefert Phase 1 nur den offiziellen Einstiegspunkt plus Hinweis auf spätere Auflösung.

## Prospect-Response

Das Backend liefert Listen in diesem Format:

```json
{
  "items": [
    {
      "id": "rewe",
      "name": "REWE",
      "prospectUrl": "https://www.rewe.de/angebote/nationale-angebote/",
      "regionType": "PLZ_BASIERT",
      "urlMode": "LOCATION_RESOLVED",
      "requiresLocationContext": true,
      "requiresStoreSelection": true,
      "notice": "Phase 1 nutzt den offiziellen Einstiegspunkt. Filialgenaue Auflösung folgt später."
    }
  ]
}
```

## Backend-Fehlerfälle

- `400 LOCATION_REQUIRED`
  - Standortkontext fehlt für die gewählte Händlerauswahl
- `400 INVALID_REQUEST`
  - PLZ oder Region ist fachlich ungültig
- `404 RETAILER_NOT_FOUND`
  - angefragte Händler-ID existiert nicht

## CLI-Hilfe

Die vollständigen CLI-Parameter stehen in der eingebauten Hilfe und in `discounter-cli/README.md`:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --help
```

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
- Jede Karte zeigt bei vorhandenen Daten einen Badge: bevorzugt die abgeleitete Aldi-Region, sonst den Regionstyp wie `PLZ-basiert` oder `Filiale optional`.
- Wenn das Backend nur einen offiziellen Einstiegspunkt liefert, zeigt die Karte zusätzlich einen Hinweis statt einer erfundenen Region.
- Händler mit Filialpflicht oder Fallback-Verhalten zeigen zusätzliche Hinweistexte.
- Wenn der Händler eine offizielle Markt- oder Filialsuche anbietet, zeigt die Karte zusätzlich einen Direktlink dorthin, aktuell z. B. für EDEKA, REWE und Netto Marken-Discount.

## Frontend-API

Das Frontend ruft auf:

```text
GET /api/v1/prospects
```

Die Response ist ein Objekt mit `items`.

## Phase-1-Einschränkungen

- Standortabhängige Händler liefern offizielle Einstiegspunkte, keine filialgenauen Prospekte.
- Echte dynamische Prospektauflösung folgt erst in Phase 2.
- Ohne PLZ, Region oder erlaubten Händlerfilter liefert das Backend `LOCATION_REQUIRED`.
