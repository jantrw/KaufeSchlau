# Dokumentation

## Oberfläche

Die Startseite enthält:

- Händlerauswahl
- PLZ- und Regionseingabe
- Laden-Button
- Ergebnisliste mit Prospektkarten

## Verhalten

- Ohne Auswahl werden alle Händler betrachtet.
- Sobald ausgewählte Händler Standortkontext brauchen, verlangt die Oberfläche PLZ oder Region.
- Eine unvollständige PLZ wie `123` wird auch dann als Fehler angezeigt, wenn Standort nur optional wäre.
- Backend-Fehler werden als verständliche Meldung in der Ergebnisliste angezeigt.

## Ergebnisdarstellung

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
