# Architektur

## Überblick

Dieser Branch erweitert das Backend um eine eigenständige Java-CLI. Die CLI ruft das REST-Backend per HTTP auf und formatiert die Antworten für Terminal-Nutzung.

## Module

- `pom.xml`
  - Maven-Aggregator für Backend und CLI
- `discounter-backend`
  - bleibt der HTTP-Anbieter für Prospektdaten
- `discounter-cli`
  - `DiscounterCli` registriert die Picocli-Kommandos
  - `ListCommand` baut Query-Parameter, ruft das Backend auf und formatiert die Ausgabe
  - `OutputFormat` schaltet zwischen Text- und JSON-Ausgabe um

## Integrationsfluss

1. Die CLI liest Parameter wie `--plz`, `--region`, `--id`, `--ids` und `--format`.
2. Sie baut daraus einen Request auf `/api/v1/prospects`.
3. Das Backend liefert JSON zurück.
4. Die CLI gibt entweder das Original-JSON oder formatierte Textzeilen mit optionalen Hinweisen aus.

## Technische Entscheidungen

- HTTP-Kommunikation über `java.net.http.HttpClient`
- Terminal-Parsing über Picocli
- Fat-JAR-Bau über `maven-shade-plugin`
- Standard-Backend-URL per Umgebungsvariable `BACKEND_URL` überschreibbar
