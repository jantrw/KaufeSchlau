# Architektur

## Überblick

Dieser Stand enthält das Phase-1-Backend, eine eigenständige Java-CLI und ein Vue-Frontend. CLI und Frontend rufen dasselbe REST-Backend auf und bereiten die Antworten jeweils für Terminal oder Oberfläche auf.

## Module

- `discounter-backend`
  - REST-API für Prospektlisten und einzelne Händler.
  - Validiert Händlerauswahl, PLZ und Region.
  - Liest Händlerregeln aus `discounters.yml` und PLZ-Zuordnung aus `plz-bundesland.json`.
- `discounter-cli`
  - Picocli-Befehl `list`.
  - Ruft dasselbe Backend wie das Frontend auf.
  - Gibt Text oder das Backend-JSON aus.
- `discounter-frontend`
  - Vue-Oberfläche für Händlerauswahl, Standortangaben und Ergebnisliste.
  - Ruft das Backend per `fetch` auf.

## Integrationsfluss

1. Frontend oder CLI ruft `/api/v1/prospects` auf.
2. Das Backend validiert Händlerauswahl, PLZ und Region.
3. Für Aldi wird bei Bedarf Nord oder Süd abgeleitet.
4. Das Backend liefert Phase-1-Einstiegspunkte als `{ "items": [...] }`.
5. Frontend und CLI zeigen Links, Hinweise und Backend-Fehler an.

## Technische Entscheidungen

- Backend als Spring Boot REST-Anwendung ohne Datenbank oder Caching
- CLI-Kommunikation über `java.net.http.HttpClient`
- Terminal-Parsing über Picocli
- Fat-JAR-Bau der CLI über `maven-shade-plugin`
- Standard-Backend-URL der CLI per Umgebungsvariable `BACKEND_URL` überschreibbar
- Frontend mit Vue 3, Vite und TypeScript
- Frontend-Aufrufe über native `fetch`
- UI-Validierung für optionale und verpflichtende Standortangaben direkt im Frontend

## Lokale Integration

`docker-compose.yml` startet die lokale Phase-1-Integration mit produktionsnahen Images:

- `backend` wird aus `discounter-backend/Dockerfile` gebaut und startet ein Spring-Boot-JAR ohne Maven.
- `frontend` wird aus `discounter-frontend/Dockerfile` gebaut und liefert den Vite-Build über Nginx auf Port `5173` aus.
- `cli` ist weiter ein optionales Compose-Profil und ruft das Backend über `http://backend:8080` auf.

Das Backend erlaubt CORS nur für `http://localhost:5173`, damit das lokale Frontend direkt gegen Port `8080` arbeiten kann.
