# Architektur

## Überblick

Dieser Stand enthält das Phase-1-Backend, eine eigenständige Java-CLI und ein Vue-Frontend. CLI und Frontend rufen dasselbe REST-Backend auf und bereiten die Antworten jeweils für Terminal oder Oberfläche auf.

## Module

- `discounter-backend`
  - `ProspectController` bedient `GET /api/v1/prospects` und `GET /api/v1/prospects/{id}`.
  - `ApiExceptionHandler` übersetzt Fach- und Validierungsfehler in HTTP-Responses.
  - `LocationRequirementService` entscheidet, ob für die gewählte Händlerauswahl Standortkontext Pflicht ist.
  - `AldiRegionResolverService` validiert PLZ und Regionsangaben und leitet Aldi Nord oder Aldi Süd her.
  - `discounters.yml` beschreibt die unterstützten Händler.
  - `plz-bundesland.json` liefert die PLZ- und Bundeslandzuordnung.
- `discounter-cli`
  - `DiscounterCli` registriert die Picocli-Kommandos.
  - `ListCommand` baut Query-Parameter, ruft das Backend auf und formatiert die Ausgabe.
  - `OutputFormat` schaltet zwischen Text- und JSON-Ausgabe um.
- `discounter-frontend`
  - `HomeView` koordiniert Eingaben, Laden und Fehlerzustände.
  - `RetailerFilter`, `RegionInput`, `DiscounterList` und `DiscounterCard` bilden die UI-Segmente.
  - `services/api.ts` kapselt den Backend-Aufruf und normalisiert die Response.

## Integrationsfluss

1. Die CLI liest Parameter wie `--plz`, `--region`, `--id`, `--ids` und `--format`.
2. Sie baut daraus einen Request auf `/api/v1/prospects`.
3. Das Backend validiert Händlerauswahl, PLZ und Region.
4. Für Aldi wird bei Bedarf die Region aus PLZ oder Bundesland abgeleitet.
5. Das Backend liefert offizielle Phase-1-Einstiegspunkte zurück.
6. Die CLI gibt entweder das Original-JSON oder formatierte Textzeilen mit optionalen Hinweisen aus.
7. Das Frontend ruft denselben Endpunkt auf, normalisiert Array- und `items`-Responses und zeigt Links, Hinweise und Validierungsfehler an.

## Technische Entscheidungen

- Backend als Spring Boot REST-Anwendung ohne Datenbank oder Caching
- CLI-Kommunikation über `java.net.http.HttpClient`
- Terminal-Parsing über Picocli
- Fat-JAR-Bau der CLI über `maven-shade-plugin`
- Standard-Backend-URL der CLI per Umgebungsvariable `BACKEND_URL` überschreibbar
- Frontend mit Vue 3, Vite und TypeScript
- Frontend-Aufrufe über Axios
- UI-Validierung für optionale und verpflichtende Standortangaben direkt im Frontend
- Response-Normalisierung im Frontend unterstützt sowohl reine Arrays als auch `{ items: [...] }`

## Lokale Integration

`docker-compose.yml` startet die lokale Phase-1-Integration ohne eigene Dockerfiles:

- `backend` nutzt das Maven-Image und startet `discounter-backend` per Spring Boot.
- `frontend` nutzt das Node-Image und startet den Vite-Dev-Server auf Port `5173`.
- `cli` ist ein optionales Compose-Profil und ruft das Backend über `http://backend:8080` auf.

Das Backend erlaubt CORS nur für `http://localhost:5173`, damit das lokale Frontend direkt gegen Port `8080` arbeiten kann.

Das ist eine bewusste Phase-1-Entscheidung für lokale Reproduzierbarkeit.
Produktionsnahe Dockerfiles für Backend und Frontend sind in Issue #18 geplant.
