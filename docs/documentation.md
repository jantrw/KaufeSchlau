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

## Unterstützte Parameter

- `plz`
  - fünfstellige Postleitzahl
- `region`
  - Region oder Bundesland für die Phase-1-Auflösung
- `retailerIds`
  - kommaseparierte Händler-IDs

## Verhalten

- Ohne Händlerfilter liefert das Backend alle Händler zurück, soweit die Standortregeln erfüllt sind.
- Händler mit Standortpflicht verlangen `plz` oder `region`.
- Aldi Nord und Aldi Süd werden bei passender Anfrage anhand von PLZ oder Region gefiltert.
- Für standortabhängige Händler liefert Phase 1 nur den offiziellen Einstiegspunkt plus Hinweis auf spätere Auflösung.

## Fehlerfälle

- `400 LOCATION_REQUIRED`
  - Standortkontext fehlt für die gewählte Händlerauswahl
- `400 INVALID_REQUEST`
  - PLZ oder Region ist fachlich ungültig
- `404 RETAILER_NOT_FOUND`
  - angefragte Händler-ID existiert nicht

## Beispielaufrufe

Alle Händler mit PLZ:

```bash
curl "http://localhost:8080/api/v1/prospects?plz=65185"
```

Gefilterte Händler:

```bash
curl "http://localhost:8080/api/v1/prospects?retailerIds=lidl,rewe&plz=65185"
```

Einzelner Händler:

```bash
curl "http://localhost:8080/api/v1/prospects/lidl"
```
