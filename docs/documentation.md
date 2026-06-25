# Dokumentation

## CLI-Befehl

Der Branch führt den Befehl `list` ein:

```text
list [--plz <plz>] [--region <region>] [--id <id>] [--ids <id1,id2>] [--format plain|json]
```

Die CLI zeigt die verfügbare Struktur auch direkt per `discounter --help` und `discounter list --help`.

## Parameter

- `--plz`
  - fünfstellige Postleitzahl
- `--region`
  - Region oder Bundesland
- `--id`
  - einzelner Händler
- `--ids`
  - mehrere Händler als CSV
- `--format`
  - `plain` oder `json`

## Beispiele

Plain-Text-Ausgabe:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --plz 65185
```

JSON-Ausgabe:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --ids lidl,rewe --plz 65185 --format json
```

Einzelner Händler:

```bash
java -jar discounter-cli/target/discounter-cli-0.1.0-SNAPSHOT.jar list --id lidl
```

## Verhalten

- Bei `plain` werden Name, URL und optionale Hinweise zeilenweise ausgegeben.
- Bei `json` wird der Backend-Body unverändert ausgegeben.
- `--id` und `--ids` werden intern zu einem gemeinsamen Händlerfilter zusammengeführt.
- Die CLI übernimmt Backend-Fehler als Terminal-Fehlerausgabe und endet mit Exit-Code `1`.

## Fehlerfälle

- Backend nicht erreichbar
  - Ausgabe: `Backend nicht erreichbar: <url>`
- Backend liefert Fehler-JSON
  - Ausgabe mit Fehlercode und Nachricht aus dem Response-Body
- Unterbrochener Request
  - Ausgabe: `Backend-Aufruf abgebrochen.`
