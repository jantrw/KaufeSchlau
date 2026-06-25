# KaufeSchlau

## Überblick

Dieser Stand enthält das Phase-1-Backend und ein Vue-Frontend für Prospektabfragen.

## Voraussetzungen

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+

## Backend lokal starten

```bash
mvn -f discounter-backend/pom.xml spring-boot:run
```

Backend-URL:

```text
http://localhost:8080
```

## Frontend lokal starten

Abhängigkeiten installieren:

```bash
npm --prefix discounter-frontend install
```

Dev-Server starten:

```bash
npm --prefix discounter-frontend run dev
```

Frontend-URL:

```text
http://localhost:5173
```

## Tests und Build

Backend-Tests:

```bash
mvn -f discounter-backend/pom.xml test
```

Frontend-Tests:

```bash
npm --prefix discounter-frontend run test
```

Frontend-Typecheck und Production-Build:

```bash
npm --prefix discounter-frontend run build
```
