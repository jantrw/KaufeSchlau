# Architektur

## Überblick

Dieser Branch ergänzt das Phase-1-Backend um ein Vue-Frontend. Das Frontend fragt das Backend per HTTP ab und bildet die Standortregeln direkt in der Oberfläche ab.

## Module

- `discounter-backend`
  - liefert Prospektdaten und Validierungsfehler unter `http://localhost:8080`
- `discounter-frontend`
  - `HomeView` koordiniert Eingaben, Laden und Fehlerzustände
  - `RetailerFilter`, `RegionInput`, `DiscounterList` und `DiscounterCard` bilden die UI-Segmente
  - `services/api.ts` kapselt den Backend-Aufruf und normalisiert die Response

## Integrationsfluss

1. Nutzer wählen Händler und optional PLZ oder Region im Frontend.
2. `HomeView` leitet die Auswahl an `fetchProspects` weiter.
3. Das Frontend ruft `GET /api/v1/prospects` auf dem Backend auf.
4. Die Response wird in UI-geeignete `ProspectLink`-Objekte normalisiert.
5. Komponenten zeigen Links, Hinweise und Validierungsfehler an.

## Technische Entscheidungen

- Frontend mit Vue 3, Vite und TypeScript
- Backend-Aufrufe über Axios
- UI-Validierung für optionale und verpflichtende Standortangaben direkt im Frontend
- Response-Normalisierung unterstützt sowohl reine Arrays als auch `{ items: [...] }`
