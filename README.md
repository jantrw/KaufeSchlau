# KaufeSchlau

KaufeSchlau zeigt Phase-1-Prospektlinks für unterstützte Discounter. Backend, Frontend und CLI laufen lokal über Docker Compose.

## Lokal starten

Voraussetzung:

- Docker mit Compose

Start:

```bash
docker compose up --build backend frontend
```

Danach:

- Frontend: <http://localhost:5173>
- Backend: <http://localhost:8080/api/v1/prospects?plz=65185>

Backend und Frontend werden aus lokalen Dockerfiles gebaut.
Das Backend-Image startet ein gebautes Spring-Boot-JAR ohne Maven.
Das Frontend-Image liefert den Vite-Build über Nginx aus.

CLI gegen das laufende Compose-Backend:

```bash
docker compose --profile cli run --rm cli
```

Eigener CLI-Aufruf:

```bash
docker compose --profile cli run --rm cli sh -c "mvn -q -pl discounter-cli -am -DskipTests package && BACKEND_URL=http://backend:8080 java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --ids lidl,penny,kaufland"
```

## Checks

```bash
mvn -pl discounter-backend test
mvn -pl discounter-cli -am test
npm --prefix discounter-frontend run test
npm --prefix discounter-frontend run build
docker compose build backend frontend
```

Manuelle Kernfälle:

```bash
curl "http://localhost:8080/api/v1/prospects?plz=65185"
curl "http://localhost:8080/api/v1/prospects"
curl "http://localhost:8080/api/v1/prospects?retailerIds=lidl,penny,kaufland"
```

## Phase-1-Einschränkungen

- Standortabhängige Händler liefern offizielle Einstiegspunkte, keine filialgenauen Prospekte.
- Echte dynamische Prospektauflösung folgt erst in Phase 2.
- Ohne PLZ, Region oder erlaubten Händlerfilter liefert das Backend `LOCATION_REQUIRED`.
