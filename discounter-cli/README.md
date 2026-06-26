# discounter-cli

## Zweck

Java-CLI für Prospektabfragen gegen das lokale KaufeSchlau-Backend.

## Voraussetzungen

- Java 21
- Maven 3.9+
- laufendes Backend unter `http://localhost:8080` oder via `BACKEND_URL`

## Bauen

```bash
mvn -pl discounter-cli package
```

## Tests

```bash
mvn -pl discounter-cli test
```

## Nutzung

Hilfe anzeigen:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar --help
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --help
```

Prospekte abrufen:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --plz 65185
```

Alternative Backend-URL:

```bash
BACKEND_URL=http://localhost:8081 java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --id lidl
```
