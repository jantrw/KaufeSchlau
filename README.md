# KaufeSchlau

## Überblick

Dieser Stand enthält das Phase-1-Backend für offizielle Prospektlinks.

## Voraussetzungen

- Java 21
- Maven 3.9+

## Backend lokal starten

```bash
mvn -f discounter-backend/pom.xml spring-boot:run
```

Standard-URL:

```text
http://localhost:8080
```

## Tests ausführen

Alle Backend-Tests:

```bash
mvn -f discounter-backend/pom.xml test
```

Nur Controller-Tests:

```bash
mvn -f discounter-backend/pom.xml -Dtest=ProspectControllerTest test
```

## Schnelltest

Alle Händler mit PLZ:

```bash
curl "http://localhost:8080/api/v1/prospects?plz=65185"
```

Ein Händler:

```bash
curl "http://localhost:8080/api/v1/prospects/lidl"
```
