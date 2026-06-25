# KaufeSchlau

## Überblick

Dieser Stand enthält das Phase-1-Backend und eine Java-CLI für Prospektabfragen über das Backend.

## Voraussetzungen

- Java 21
- Maven 3.9+

## Tests ausführen

Alle Maven-Tests:

```bash
mvn test
```

Nur CLI-Tests:

```bash
mvn -pl discounter-cli test
```

## Backend lokal starten

```bash
mvn -pl discounter-backend spring-boot:run
```

Backend-URL:

```text
http://localhost:8080
```

## CLI bauen und ausführen

CLI-JAR bauen:

```bash
mvn -pl discounter-cli package
```

CLI gegen lokales Backend:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --plz 65185
```

Alternative Backend-URL:

```bash
BACKEND_URL=http://localhost:8081 java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --id lidl
```
