# discounter-backend

## Zweck

Spring-Boot-Backend für Phase-1-Prospektlinks und Validierungsfehler.

## Voraussetzungen

- Java 21
- Maven 3.9+

## Start

```bash
mvn -f discounter-backend/pom.xml spring-boot:run
```

Standard-URL:

```text
http://localhost:8080
```

## Tests

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
