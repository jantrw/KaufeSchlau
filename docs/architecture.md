# Architektur

## Überblick

Dieser Branch führt ein einzelnes Spring-Boot-Backend ein. Das Backend liest Händler- und Regionsdaten aus statischen Ressourcen und liefert daraus Phase-1-Prospektlinks per REST aus.

## Module

- `controller`
  - `ProspectController` bedient `GET /api/v1/prospects` und `GET /api/v1/prospects/{id}`.
  - `ApiExceptionHandler` übersetzt Fach- und Validierungsfehler in HTTP-Responses.
- `service`
  - `LocationRequirementService` entscheidet, ob für die gewählte Händlerauswahl Standortkontext Pflicht ist.
  - `AldiRegionResolverService` validiert PLZ und Regionsangaben und leitet Aldi Nord/Süd her.
- `config` und `resources`
  - `discounters.yml` beschreibt die unterstützten Händler.
  - `plz-bundesland.json` liefert die PLZ- und Bundeslandzuordnung.

## Request-Fluss

1. Der Controller liest Händlerfilter, PLZ und Region aus den Query-Parametern.
2. Die Services validieren, ob die Kombination zulässig ist.
3. Für Aldi wird bei Bedarf die Region aus PLZ oder Bundesland abgeleitet.
4. Das Backend liefert nur offizielle Phase-1-Einstiegspunkte zurück, keine dynamische Filialauflösung.

## Grenzen dieses Stands

- Keine Datenbank
- Kein Caching
- Keine Händler-spezifischen Resolver aus Phase 2
- Kein Frontend und keine CLI in diesem Branch
