Review-Punkt umgesetzt. `requiresLocationContext` kommt jetzt aus der tatsächlichen Request-Anforderung; explizites `aldi-sued` ohne Standort liefert `false`.

Verifiziert:
`mvn -f discounter-backend\pom.xml test` -> 26 Tests grün.

READY_FOR_REVIEW