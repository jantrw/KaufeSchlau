# Architektur

## Überblick

Dieser Stand enthält das Phase-1-Backend und eine eigenständige Java-CLI. Die CLI ruft das REST-Backend per HTTP auf und formatiert die Antworten für Terminal-Nutzung.

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

## Integrationsfluss

1. Die CLI liest Parameter wie `--plz`, `--region`, `--id`, `--ids` und `--format`.
2. Sie baut daraus einen Request auf `/api/v1/prospects`.
3. Das Backend validiert Händlerauswahl, PLZ und Region.
4. Für Aldi wird bei Bedarf die Region aus PLZ oder Bundesland abgeleitet.
5. Das Backend liefert offizielle Phase-1-Einstiegspunkte zurück.
6. Die CLI gibt entweder das Original-JSON oder formatierte Textzeilen mit optionalen Hinweisen aus.

## Technische Entscheidungen

- Backend als Spring Boot REST-Anwendung ohne Datenbank oder Caching
- CLI-Kommunikation über `java.net.http.HttpClient`
- Terminal-Parsing über Picocli
- Fat-JAR-Bau der CLI über `maven-shade-plugin`
- Standard-Backend-URL der CLI per Umgebungsvariable `BACKEND_URL` überschreibbar
