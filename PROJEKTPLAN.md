# Discounter-Angebots-System – Projektplan

## Projektübersicht

Ein System zur Anzeige aktueller wöchentlicher Prospekt-URLs deutscher Discounter und Supermärkte in Deutschland.

Das Projekt besteht aus drei Modulen:

- Java-Backend
- Java-CLI-Tool
- Vue.js-Frontend

Der MVP zeigt offizielle Prospekt- und Angebotslinks. Eine spätere Version kann Produkte, Preise und Kategorien direkt in der Anwendung darstellen.

---

## Ziele

### Phase 1 – URL-Ausgabe (aktueller Scope)

- Ausgabe offizieller Prospekt- und Angebotsseiten definierter Händler
- Vor Abruf **aller** Prospekte muss der Nutzer eine PLZ und/oder Region angeben
- Unterstützung PLZ-basierter Differenzierung, insbesondere bei REWE, EDEKA und Netto Marken-Discount
- Unterstützung regionaler Varianten bei Aldi Nord und Aldi Süd über PLZ-Mapping
- Filterbare Händler-/Filialauswahl: Wenn ausschließlich Händler ohne Standortpflicht gewählt werden, darf der Abruf ohne PLZ/Region funktionieren
- Dynamische Ermittlung aktueller Wochen-URLs, falls ein Händler wöchentlich wechselnde Prospektpfade nutzt
- Bereitstellung über CLI und Webanwendung
- Keine Speicherung fremder Prospekt-PDFs oder Produktbilder
- Keine Produktdaten-Extraktion im MVP

### Phase 2 – Filialgenaue Prospekt-Auflösung

- Händler-spezifische Resolver für PLZ → Filiale → Prospektseite
- Health-Check für offizielle URLs
- Optionaler Scheduler für regelmäßige Prüfungen
- Caching der letzten gültigen Links

### Phase 3 – Produktansicht

- Parsing von HTML-Angebotsseiten, sofern rechtlich und technisch vertretbar
- Optionales PDF-/Prospekt-Parsing nur nach separater Prüfung
- Produktansicht direkt in der Webanwendung
- Suche, Kategorien, Gültigkeitszeitraum und Händlerfilter
- Optionale Datenbankanbindung mit PostgreSQL

---

## Unterstützte Händler (Phase 1)

| Händler | Besonderheit | Phase-1-Strategie |
|---|---|---|
| REWE | PLZ-/marktbasierte Angebote und Prospekte | Offizielle Angebotsseite verlinken; später Markt-Resolver |
| EDEKA | PLZ-/marktbasierte Angebote und Prospekte | Offizielle Angebotsseite + Marktsuche verlinken; später Markt-ID-Resolver |
| Aldi Nord | Region via PLZ-Mapping | Offizielle Prospektseite verlinken |
| Aldi Süd | Region via PLZ-Mapping | Offizielle Prospektseite verlinken |
| Lidl | Filiale wählbar; Prospektseite vorhanden | Offizielle Prospektseite verlinken |
| Penny | Angebote und Prospekt der Woche | Offizielle Angebots-/Prospektseite verlinken |
| Netto Marken-Discount | Filiale/PLZ relevant | Offizielle Online-Prospektseite verlinken; später Filial-Resolver |
| Kaufland | Filialangebote und Prospekte | Offizielle Prospektseite verlinken |

> Hinweis Netto: Unterstützt wird zunächst nur **Netto Marken-Discount**.

---

## Standort-, Region- und Filiallogik

### Grundregel

Die Anwendung unterscheidet zwischen zwei Abrufarten:

1. **Alle Prospekte abrufen**
   - Der Nutzer muss vorher eine **PLZ und/oder Region** eingeben.
   - Dadurch können Aldi Nord/Süd korrekt aufgelöst und PLZ-/filialabhängige Händler wie REWE, EDEKA und Netto korrekt behandelt werden.
   - Ohne Standortkontext darf der Abruf „alle Prospekte“ nicht stillschweigend unvollständige Ergebnisse liefern.

2. **Gefilterte Händler abrufen**
   - Der Nutzer kann gezielt Händler auswählen.
   - Wenn alle ausgewählten Händler keinen Standort benötigen, ist keine PLZ/Region erforderlich.
   - Sobald mindestens ein ausgewählter Händler PLZ, Region oder Filialauswahl benötigt, fordert Backend/CLI/Webapp Standortkontext an.

### Verhalten bei fehlender PLZ/Region

| Abruf | Enthält standortpflichtige Händler? | Verhalten |
|---|---:|---|
| Alle Händler | Ja | Fehler/Hinweis: PLZ oder Region erforderlich |
| Nur Aldi Nord/Aldi Süd direkt gewählt | Nein, wenn explizit gewählt | Link darf angezeigt werden |
| Aldi automatisch bestimmen | Ja | PLZ/Region erforderlich |
| Nur Lidl/Penny/Kaufland ohne Filialauflösung | Nein | Links dürfen angezeigt werden |
| REWE/EDEKA/Netto gewählt | Ja | PLZ/Region oder Filiale erforderlich |

### Filialfilter

Die Webapp und CLI sollen eine Händler-/Filialauswahl unterstützen:

- Nutzer kann Händler aktivieren/deaktivieren.
- Später kann pro Händler eine konkrete Filiale gewählt werden.
- Bei Händlern ohne Standortpflicht wird kein Standort abgefragt.
- Bei Händlern mit Standortpflicht wird eine PLZ/Region abgefragt oder eine gespeicherte Filiale verwendet.

### Persistenz von Standortdaten

Für den MVP wird keine Benutzerverwaltung benötigt. Optional kann später lokal gespeichert werden:

- Standard-PLZ
- bevorzugte Region
- bevorzugte Händler
- bevorzugte Filialen pro Händler

---

## Aktueller Stand der URL-Recherche

Stand: 2026-06-22

Die Projektdatei enthielt bereits einen Abschnitt **„Wie man die Prospekt-URLs bekommt“**. Dieser Abschnitt wurde präzisiert: Für Phase 1 sollen keine wöchentlich wechselnden PDF- oder Blätterkatalog-Deep-Links fest im Code stehen. Stattdessen werden stabile offizielle Einstiegspunkte, Resolver-Hinweise und Regeln zur Standortpflicht gespeichert. Wenn ein Händler wöchentlich wechselnde Prospektpfade nutzt, soll der aktuelle Pfad dynamisch ermittelt oder mindestens durch einen Health-Check erkannt werden.

### Ergebnis

Es gibt nach aktuellem Stand keine allgemein nutzbare, offizielle, kostenlose Prospekt-API der Händler. Für den MVP ist daher der robusteste Ansatz:

1. Offizielle Händlerseiten als stabile Einstiegspunkte speichern.
2. Händler mit Filial-/PLZ-Abhängigkeit entsprechend markieren.
3. Beim Abruf prüfen, ob für die gewählten Händler PLZ, Region oder Filiale nötig ist.
4. Keine wöchentlich wechselnden Zielpfade hart im Code hinterlegen.
5. Pro Händler einen Resolver vorsehen, der bei Bedarf die aktuell gültige Wochen-URL dynamisch ableitet.

---

## Offizielle Prospekt- und Angebots-URLs für Phase 1

| Händler | URL | URL-Typ | PLZ/Filiale nötig? | Bewertung |
|---|---|---|---|---|
| Aldi Nord | `https://www.aldi-nord.de/prospekte.html` | Prospektseite | Für automatische Auswahl ja; bei expliziter Auswahl nein | Für MVP geeignet |
| Aldi Süd | `https://www.aldi-sued.de/prospekte` | Prospektseite | Für automatische Auswahl ja; bei expliziter Auswahl nein | Für MVP geeignet |
| Lidl | `https://www.lidl.de/c/online-prospekte/s10005610` | Prospektseite | Optional, Filiale kann gewählt werden | Für MVP geeignet |
| Penny | `https://www.penny.de/angebote` | Angebote + Prospekt der Woche | Optional/marktbezogen | Für MVP geeignet |
| Netto Marken-Discount | `https://www.netto-online.de/ueber-netto/Online-Prospekte.chtm` | Online-Prospekte | Ja, Filialauswahl/PLZ relevant | Für MVP als Einstieg geeignet |
| Kaufland | `https://filiale.kaufland.de/prospekte.html` | Prospektseite | Optional/filialbezogen | Für MVP geeignet |
| REWE | `https://www.rewe.de/angebote/nationale-angebote/` | Angebotsseite mit Markt-/Prospektauswahl | Ja | Für MVP als Einstieg geeignet |
| EDEKA | `https://www.edeka.de/angebote/` | Angebotsseite mit Filialbezug | Ja | Für MVP als Einstieg geeignet |
| EDEKA Marktsuche | `https://www.edeka.de/marktsuche.jsp` | Marktsuche | Ja | Hilfsseite für späteren Resolver |

---

## URL-Beschaffungsstrategie

### Phase 1: Konfiguration + dynamische Auflösungsregeln

Für den MVP wird eine YAML-Konfiguration verwendet. Diese Konfiguration darf **nicht** als Sammlung hart codierter Wochenpfade verstanden werden. Sie enthält:

- stabile offizielle Einstiegspunkte
- Information, ob PLZ/Region/Filiale erforderlich ist
- Information, ob eine URL statisch oder dynamisch aufzulösen ist
- optionale Resolver-Hinweise pro Händler

Die Regel lautet:

> Im Code werden keine wöchentlich wechselnden Prospektpfade hart codiert. Wenn ein Händler jede Woche neue Pfade erzeugt, muss die aktuelle URL zur Laufzeit über einen Resolver oder einen validierten Einstiegspunkt ermittelt werden.

Für Phase 1 sind drei URL-Modi vorgesehen:

| URL-Modus | Bedeutung | Beispiel |
|---|---|---|
| `STATIC_ENTRYPOINT` | Stabiler offizieller Einstiegspunkt, der selbst auf den aktuellen Prospekt führt | Aldi Nord, Aldi Süd, Lidl |
| `DYNAMIC_WEEKLY` | Aktuelle Wochen-URL kann wechseln und muss dynamisch ermittelt oder validiert werden | Händler mit wechselnden Prospekt-/PDF-Pfaden |
| `LOCATION_RESOLVED` | URL hängt von PLZ, Region oder Filiale ab | REWE, EDEKA, Netto |

Vorteile:

- weniger Risiko durch veraltete Links
- keine hart codierten Wochenpfade im Java-Code
- bessere Erweiterbarkeit für Resolver
- saubere Trennung zwischen Händler-Konfiguration und URL-Ermittlung

Nachteile:

- Resolver sind je Händler unterschiedlich
- REWE, EDEKA und Netto können im MVP noch auf eine Einstiegsseite verweisen, solange kein Resolver existiert
- dynamische Ermittlung muss regelmäßig getestet werden

### Phase 2: Händler-spezifische Resolver

Für eine bessere UX werden später Resolver pro Händler ergänzt.

| Händler | Geplante Resolver-Logik |
|---|---|
| REWE | PLZ → Marktsuche/Marktauswahl → marktbezogene Angebotsseite wie `/angebote/{ort}/{marketId}/{marketSlug}/` |
| EDEKA | PLZ → Marktsuche → Markt-ID → Prospektseite wie `/markt-id/{marketId}/prospekt.jsp` |
| Netto Marken-Discount | PLZ → Filialfinder/Filiale → regionale Online-Prospekte |
| Kaufland | PLZ/Filiale → Filialseite/Prospekte |
| Lidl | Filiale optional setzen; Prospektseite bleibt als Einstieg nutzbar |
| Penny | Marktseite optional; `/angebote` bleibt als Einstieg nutzbar |

### Phase 3: Produktdaten

Produktdaten sollten nicht aus Phase 1 herausgezogen werden. Dafür braucht es eine eigene technische und rechtliche Prüfung:

- HTML-Angebotsseiten bevorzugen
- PDF-/Bild-OCR nur optional
- fremde Produktbilder nicht dauerhaft speichern
- Caching-Regeln und Nutzungsbedingungen prüfen
- Datenquelle je Händler dokumentieren

---

## Entscheidungen zu offenen Punkten

### 1. UI-Bibliothek Frontend

**Entscheidung: PrimeVue 4**

Begründung:

- gute TypeScript-Unterstützung
- viele passende Komponenten wie Cards, DataView, Dropdown und Button
- Vue-3-nativ
- einfache Integration in Vite

```bash
npm install primevue @primevue/themes
```

### 2. Aldi-Region-Erkennung via PLZ

**Entscheidung: Automatisch via PLZ-Mapping**

Aldi Nord und Aldi Süd werden nicht beide angezeigt, wenn eine PLZ übergeben wurde. Das Backend löst die PLZ auf eine Aldi-Region auf.

Eine exakte Grenzziehung ist komplex, weil es Grenzbereiche gibt. Für den MVP reicht ein PLZ-Präfix- oder Bundesland-Mapping. Grenzfälle werden später verbessert.

### 3. URL-Aktualisierung

**Entscheidung: Keine hart codierten Wochenpfade**

Die Konfiguration enthält stabile Einstiegspunkte und Resolver-Metadaten. Wöchentlich wechselnde Prospektpfade werden nicht fest im Code hinterlegt.

Für Phase 1 gilt:

- Offizielle Einstiegspunkte dürfen konfiguriert werden.
- Dynamische Wochenpfade dürfen nur als Ergebnis einer Auflösung zurückgegeben werden.
- Das Backend prüft, ob für die gewählten Händler Standortdaten nötig sind.
- Ein Health-Check kann später prüfen, ob URLs erreichbar sind.

Für Phase 2 gilt:

- Resolver pro Händler ermitteln konkrete aktuelle Prospekt-URLs.
- Ergebnisse können gecacht werden, inklusive Gültigkeitszeitraum und Abrufzeitpunkt.
- Wenn ein Resolver fehlschlägt, fällt das System auf den offiziellen Einstiegspunkt zurück.

---

## Technischer Stack

### Backend (`discounter-backend`)

| Komponente | Technologie |
|---|---|
| Sprache | Java 21 |
| Framework | Spring Boot 3 |
| Build-Tool | Maven |
| API | REST |
| HTTP-Client | Java HttpClient |
| Konfiguration | `application.yml` + `discounters.yml` |
| Containerisierung | Docker |

### CLI (`discounter-cli`)

| Komponente | Technologie |
|---|---|
| Sprache | Java 21 |
| Framework | Picocli |
| Build-Tool | Maven |
| Kommunikation | REST-Call zum Backend |
| Packaging | Executable JAR |

### Frontend (`discounter-frontend`)

| Komponente | Technologie |
|---|---|
| Sprache | TypeScript |
| Framework | Vue 3 + Vite |
| UI-Bibliothek | PrimeVue 4 |
| HTTP-Client | Axios |
| Containerisierung | Docker + Nginx |

### Infrastruktur

| Komponente | Technologie |
|---|---|
| Orchestrierung | Docker Compose |
| Deployment | Lokal / Self-hosted |

---

## Projektstruktur

```text
discounter-offers/
├── discounter-backend/
│   ├── src/main/java/de/discounter/
│   │   ├── DiscounterApplication.java
│   │   ├── controller/
│   │   │   └── ProspectController.java
│   │   ├── service/
│   │   │   ├── ProspectService.java
│   │   │   └── RegionResolverService.java
│   │   ├── model/
│   │   │   ├── Discounter.java
│   │   │   ├── AldiRegion.java
│   │   │   ├── RegionType.java
│   │   │   └── ProspectLink.java
│   │   └── config/
│   │       └── DiscounterConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── discounters.yml
│   │   └── plz-bundesland.json
│   ├── Dockerfile
│   └── pom.xml
│
├── discounter-cli/
│   ├── src/main/java/de/discounter/cli/
│   │   ├── DiscounterCLI.java
│   │   └── commands/
│   │       ├── ListCommand.java
│   │       └── SearchCommand.java
│   ├── Dockerfile
│   └── pom.xml
│
├── discounter-frontend/
│   ├── src/
│   │   ├── main.ts
│   │   ├── App.vue
│   │   ├── components/
│   │   │   ├── DiscounterList.vue
│   │   │   ├── DiscounterCard.vue
│   │   │   ├── RetailerFilter.vue
│   │   │   └── RegionInput.vue
│   │   ├── views/
│   │   │   └── HomeView.vue
│   │   ├── services/
│   │   │   └── api.ts
│   │   └── types/
│   │       └── index.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── package.json
│
├── docker-compose.yml
├── PROJECT_PLAN.md
└── README.md
```

---

## Konfigurationsdatei: `discounters.yml`

```yaml
discounters:
  - id: aldi-nord
    name: "Aldi Nord"
    regionType: ALDI_REGION
    urlMode: STATIC_ENTRYPOINT
    aldiRegion: NORD
    prospectUrl: "https://www.aldi-nord.de/prospekte.html"
    officialUrl: true
    requiresLocationContext: true
    locationRequirementReason: "Nur nötig, wenn Aldi automatisch aus PLZ/Region bestimmt werden soll"
    supportsManualSelectionWithoutPlz: true
    requiresStoreSelection: false

  - id: aldi-sued
    name: "Aldi Süd"
    regionType: ALDI_REGION
    urlMode: STATIC_ENTRYPOINT
    aldiRegion: SUED
    prospectUrl: "https://www.aldi-sued.de/prospekte"
    officialUrl: true
    requiresLocationContext: true
    locationRequirementReason: "Nur nötig, wenn Aldi automatisch aus PLZ/Region bestimmt werden soll"
    supportsManualSelectionWithoutPlz: true
    requiresStoreSelection: false

  - id: lidl
    name: "Lidl"
    regionType: OPTIONAL_FILIALE
    urlMode: STATIC_ENTRYPOINT
    prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610"
    officialUrl: true
    requiresLocationContext: false
    supportsManualSelectionWithoutPlz: true
    requiresStoreSelection: false

  - id: penny
    name: "Penny"
    regionType: OPTIONAL_FILIALE
    urlMode: STATIC_ENTRYPOINT
    prospectUrl: "https://www.penny.de/angebote"
    officialUrl: true
    requiresLocationContext: false
    supportsManualSelectionWithoutPlz: true
    requiresStoreSelection: false

  - id: netto-marken-discount
    name: "Netto Marken-Discount"
    regionType: PLZ_BASIERT
    urlMode: LOCATION_RESOLVED
    prospectUrl: "https://www.netto-online.de/ueber-netto/Online-Prospekte.chtm"
    officialUrl: true
    requiresLocationContext: true
    supportsManualSelectionWithoutPlz: false
    requiresStoreSelection: true
    resolverHint: "PLZ -> Filiale -> regionaler Online-Prospekt"

  - id: kaufland
    name: "Kaufland"
    regionType: OPTIONAL_FILIALE
    urlMode: STATIC_ENTRYPOINT
    prospectUrl: "https://filiale.kaufland.de/prospekte.html"
    officialUrl: true
    requiresLocationContext: false
    supportsManualSelectionWithoutPlz: true
    requiresStoreSelection: false

  - id: rewe
    name: "REWE"
    regionType: PLZ_BASIERT
    urlMode: LOCATION_RESOLVED
    prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/"
    officialUrl: true
    requiresLocationContext: true
    supportsManualSelectionWithoutPlz: false
    requiresStoreSelection: true
    resolverHint: "PLZ -> Markt -> /angebote/{ort}/{marketId}/{marketSlug}/"

  - id: edeka
    name: "EDEKA"
    regionType: PLZ_BASIERT
    urlMode: LOCATION_RESOLVED
    prospectUrl: "https://www.edeka.de/angebote/"
    marketSearchUrl: "https://www.edeka.de/marktsuche.jsp"
    officialUrl: true
    requiresLocationContext: true
    supportsManualSelectionWithoutPlz: false
    requiresStoreSelection: true
    resolverHint: "PLZ -> Markt-ID -> /markt-id/{marketId}/prospekt.jsp"
```

Wichtig: `prospectUrl` ist der offizielle Einstiegspunkt oder Fallback. Er ist nicht zwingend die finale Wochen-URL. Finale URLs werden über `urlMode` und spätere Resolver bestimmt.

---

## PLZ → Bundesland Mapping

Für die Aldi-Region-Erkennung wird ein PLZ-Präfix-Mapping gegen Bundesländer aufgelöst. Für den MVP genügt eine statische Datei `plz-bundesland.json`.

```json
{
  "bundeslandToAldiRegion": {
    "BY": "SUED",
    "BW": "SUED",
    "HE": "SUED",
    "RP": "SUED",
    "SL": "SUED",
    "BE": "NORD",
    "BB": "NORD",
    "HB": "NORD",
    "HH": "NORD",
    "MV": "NORD",
    "NI": "NORD",
    "NW": "NORD",
    "SH": "NORD",
    "SN": "NORD",
    "ST": "NORD",
    "TH": "NORD"
  }
}
```

Die eigentliche Datei sollte zusätzlich `plzPrefixToBundesland` enthalten.

---

## REST-API Design

### Endpunkte Phase 1

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/v1/prospects?plz={plz}` | Alle Händler mit PLZ-/Region-Auflösung |
| `GET` | `/api/v1/prospects?region={region}` | Alle Händler mit grober Regionsauflösung, falls keine PLZ genutzt wird |
| `GET` | `/api/v1/prospects?retailerIds={ids}` | Gefilterte Händler; ohne PLZ nur erlaubt, wenn keiner der Händler Standortkontext benötigt |
| `GET` | `/api/v1/prospects?plz={plz}&retailerIds={ids}` | Gefilterte Händler mit Standortkontext |
| `GET` | `/api/v1/prospects/{id}?plz={plz}` | Einzelner Händler nach ID mit optionaler PLZ-Auflösung |

### Validierungsregel

`GET /api/v1/prospects` ohne PLZ, Region oder Händlerfilter darf nicht automatisch alle Händler zurückgeben. Das Backend muss dann einen Validierungsfehler liefern, weil sonst standortabhängige Händler fehlen oder falsch aufgelöst werden könnten.

Beispiel-Fehler:

```json
{
  "error": "LOCATION_REQUIRED",
  "message": "Für den Abruf aller Prospekte ist eine PLZ oder Region erforderlich.",
  "requiredForRetailers": ["aldi-nord/aldi-sued", "rewe", "edeka", "netto-marken-discount"]
}
```

### Beispiel-Response

`GET /api/v1/prospects?plz=65185`

```json
[
  {
    "id": "aldi-sued",
    "name": "Aldi Süd",
    "regionType": "ALDI_REGION",
    "urlMode": "STATIC_ENTRYPOINT",
    "resolvedRegion": "SUED",
    "prospectUrl": "https://www.aldi-sued.de/prospekte",
    "requiresLocationContext": false,
    "requiresStoreSelection": false,
    "resolvedDynamically": false
  },
  {
    "id": "rewe",
    "name": "REWE",
    "regionType": "PLZ_BASIERT",
    "urlMode": "LOCATION_RESOLVED",
    "prospectUrl": "https://www.rewe.de/angebote/nationale-angebote/",
    "requiresLocationContext": true,
    "requiresStoreSelection": true,
    "resolvedDynamically": false,
    "fallbackUsed": true
  },
  {
    "id": "edeka",
    "name": "EDEKA",
    "regionType": "PLZ_BASIERT",
    "urlMode": "LOCATION_RESOLVED",
    "prospectUrl": "https://www.edeka.de/angebote/",
    "requiresLocationContext": true,
    "requiresStoreSelection": true,
    "resolvedDynamically": false,
    "fallbackUsed": true
  }
]
```

---

## CLI-Design

### Befehle

```bash
# Alle Prospekt-URLs: PLZ oder Region ist erforderlich
./discounter list --plz 65185
./discounter list --region hessen

# Ungültig für "alle", weil Standortkontext fehlt
./discounter list

# Gefilterte Händler ohne Standortpflicht: erlaubt ohne PLZ
./discounter list --ids lidl,penny,kaufland

# Gefilterte Händler mit Standortpflicht: PLZ/Region erforderlich
./discounter list --ids rewe,edeka --plz 65185

# Einzelner Händler
./discounter list --id lidl

# JSON-Ausgabe
./discounter list --plz 65185 --format json

# Text-Ausgabe
./discounter list --plz 65185 --format plain
```

### Beispiel-Ausgabe

```text
Aldi Süd                -> https://www.aldi-sued.de/prospekte
REWE                    -> https://www.rewe.de/angebote/nationale-angebote/       Hinweis: Markt/PLZ auf Händlerseite wählen; Resolver später
EDEKA                   -> https://www.edeka.de/angebote/                         Hinweis: Markt/PLZ auf Händlerseite wählen; Resolver später
Lidl                    -> https://www.lidl.de/c/online-prospekte/s10005610
Penny                   -> https://www.penny.de/angebote
Netto Marken-Discount   -> https://www.netto-online.de/ueber-netto/Online-Prospekte.chtm Hinweis: Filiale wählen; Resolver später
Kaufland                -> https://filiale.kaufland.de/prospekte.html
```

Beispiel-Fehler ohne Standortkontext:

```text
Fehler: Für den Abruf aller Prospekte ist eine PLZ oder Region erforderlich.
Nutze z. B.: ./discounter list --plz 65185
Oder filtere Händler ohne Standortpflicht: ./discounter list --ids lidl,penny,kaufland
```

---

## Frontend-Design

### Views und Komponenten Phase 1

- **HomeView**: Hauptseite mit PLZ-/Region-Eingabe, Händlerfilter und Händlerliste
- **RegionInput**: PLZ-Eingabefeld mit 5-stelliger Validierung und optionaler Regionsauswahl
- **RetailerFilter**: Auswahl, welche Händler angezeigt werden sollen
- **DiscounterCard**: Karte mit Name, Region-Badge, Hinweistext und Button
- **DiscounterList**: Responsive Grid aus Karten

### UI-Flow

```text
Startseite
├── Händlerfilter auswählen
│   ├── Wenn Auswahl Standort braucht: PLZ/Region anzeigen und verlangen
│   └── Wenn Auswahl keinen Standort braucht: Abruf ohne PLZ erlauben
└── Angebote anzeigen
    └── GET /api/v1/prospects?plz=65185&retailerIds=lidl,rewe,edeka
        └── Händlerliste
            └── Button: Zum Prospekt
```

Wenn der Nutzer „alle Händler“ auswählt, muss die PLZ-/Region-Eingabe sichtbar und verpflichtend sein.

---

## Docker Compose Setup

```yaml
version: "3.8"

services:
  backend:
    build: ./discounter-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  frontend:
    build: ./discounter-frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

  cli:
    build: ./discounter-cli
    profiles:
      - cli
    environment:
      - BACKEND_URL=http://backend:8080
```

CLI starten:

```bash
docker compose --profile cli run cli list --plz 65185
```

---

## Umsetzungsreihenfolge für Codex

### Schritt 1 – Backend Grundstruktur

- [ ] Maven-Projekt anlegen: Spring Boot 3, Java 21
- [ ] `discounters.yml` mit den 8 unterstützten Händlern anlegen
- [ ] `plz-bundesland.json` anlegen
- [ ] `DiscounterConfig.java` mit `@ConfigurationProperties` erstellen
- [ ] Modelle erstellen: `Discounter`, `ProspectLink`, `RegionType`, `AldiRegion`, `UrlMode`, `LocationRequirement`
- [ ] `RegionResolverService.java` erstellen
- [ ] `LocationRequirementService.java` erstellen: prüft, ob PLZ/Region für die gewählte Händlerauswahl nötig ist
- [ ] `ProspectResolver`-Interface erstellen, auch wenn Phase 1 zunächst Fallback-URLs nutzt
- [ ] `ProspectService.java` erstellen
- [ ] `ProspectController.java` erstellen
- [ ] Validierungsfehler `LOCATION_REQUIRED` implementieren
- [ ] CORS für das lokale Frontend konfigurieren
- [ ] Unit-Tests für PLZ → Aldi-Region schreiben
- [ ] Unit-Tests für Händlerfilter ohne PLZ schreiben
- [ ] Unit-Tests für Pflicht-PLZ bei Abruf aller Händler schreiben

### Schritt 2 – CLI

- [ ] Maven-Projekt mit Picocli anlegen
- [ ] `DiscounterCLI.java` erstellen
- [ ] `ListCommand.java` mit `--plz`, `--region`, `--id`, `--ids`, `--format` erstellen
- [ ] REST-Client mit Java HttpClient erstellen
- [ ] Ausgabe-Formatter für `plain` und `json` erstellen
- [ ] Fehlerfälle ausgeben: Backend nicht erreichbar, ungültige PLZ, unbekannte Händler-ID, fehlender Standortkontext

### Schritt 3 – Frontend

- [ ] Vue 3 + Vite + TypeScript Projekt anlegen
- [ ] PrimeVue 4 installieren und konfigurieren
- [ ] Axios installieren
- [ ] Typen in `types/index.ts` anlegen
- [ ] API-Service in `services/api.ts` anlegen
- [ ] `RegionInput.vue` erstellen
- [ ] `RetailerFilter.vue` erstellen
- [ ] `DiscounterCard.vue` erstellen
- [ ] `DiscounterList.vue` erstellen
- [ ] `HomeView.vue` erstellen
- [ ] PLZ-/Region-Eingabe nur verpflichtend machen, wenn die Auswahl Standortkontext benötigt
- [ ] Hinweis anzeigen, wenn ein Händler Filialauswahl benötigt

### Schritt 4 – Integration

- [ ] `docker-compose.yml` anlegen
- [ ] Backend, Frontend und CLI lokal starten
- [ ] Test: PLZ `65185` ergibt Aldi Süd
- [ ] Test: Ohne PLZ und ohne Händlerfilter wird `LOCATION_REQUIRED` zurückgegeben
- [ ] Test: Ohne PLZ mit `lidl,penny,kaufland` werden Links angezeigt
- [ ] Test: Ohne PLZ mit `rewe` wird `LOCATION_REQUIRED` zurückgegeben
- [ ] Test: Händlerlinks öffnen in neuem Tab
- [ ] `README.md` schreiben

### Schritt 5 – Phase-2-Vorbereitung

- [ ] Interface `ProspectResolver` entwerfen
- [ ] Resolver-Stubs für REWE, EDEKA, Netto Marken-Discount und Kaufland anlegen
- [ ] Resolver-Stubs für Händler mit potenziell wechselnden Wochenpfaden anlegen
- [ ] Health-Check-Service entwerfen
- [ ] Entscheidung zu Caching und Datenbank treffen

---

## Offene Punkte für die nächste Entscheidung

| Priorität | Thema | Frage | Empfehlung |
|---|---|---|---|
| Hoch | Standortpflicht für „alle Prospekte“ | Soll Abruf ohne PLZ/Region komplett blockiert werden? | Ja, damit Ergebnisse nicht unvollständig oder falsch regionalisiert sind |
| Hoch | Händlerfilter ohne PLZ | Welche Händler dürfen ohne Standort angezeigt werden? | Nur Händler mit `requiresLocationContext=false` oder explizit manuell gewählte Aldi-Variante |
| Hoch | Dynamische Wochen-URLs | Wie werden wöchentlich wechselnde Pfade erkannt? | Nicht hardcoden; Resolver + Health-Check + Fallback-Einstiegspunkt |
| Hoch | Phase-1-Verhalten bei PLZ-Händlern | Soll Phase 1 nur zur offiziellen Seite verlinken oder schon automatisch eine konkrete Filiale auswählen? | Für MVP nur verlinken; Resolver später |
| Hoch | Filialauswahl | Wenn mehrere Filialen zur PLZ gefunden werden: erste Filiale automatisch nehmen oder Liste anzeigen? | Liste anzeigen, sobald Resolver existiert |
| Hoch | REWE-Resolver | Soll eine marktgenaue REWE-URL automatisch erzeugt werden? | Erst in Phase 2 |
| Hoch | EDEKA-Resolver | Soll die EDEKA-Markt-ID automatisch über die Marktsuche ermittelt werden? | Erst in Phase 2 |
| Mittel | Netto Marken-Discount | Soll Netto als PLZ-basiert behandelt werden? | Ja |
| Mittel | Kaufland/Lidl/Penny | Soll Filialauswahl auch dort direkt unterstützt werden? | Optional nach MVP |
| Mittel | URL-Health-Check | Soll ein wöchentlicher Check defekter URLs eingebaut werden? | Phase 2 |
| Mittel | Speicherung | Sollen Ergebnisse gecacht werden? | Erst bei Resolvern sinnvoll |
| Mittel | Produktdaten | Soll Phase 3 auf offiziellen HTML-Seiten oder inoffiziellen Datenquellen basieren? | Offizielle HTML-Seiten bevorzugen |
| Hoch | Rechtliches | Wird die Anwendung öffentlich gehostet? | Vor Scraping klären |
| Niedrig | Native CLI | Soll die CLI später als native Binary gebaut werden? | Optional mit GraalVM |
| Niedrig | Benutzerprofile | Sollen Favoriten/Standard-PLZ gespeichert werden? | Nicht im MVP |

---

## Nicht-Ziele für den MVP

- keine Produktdaten-Extraktion
- keine OCR-Verarbeitung
- keine Speicherung von Prospekt-PDFs
- keine Speicherung fremder Produktbilder
- keine Nutzerkonten
- keine Preisverlaufshistorie
- keine Push-Benachrichtigungen

---

## Glossar

| Begriff | Bedeutung |
|---|---|
| PLZ | Deutsche Postleitzahl, fünfstellig |
| Prospekt | Digitales Angebotsblatt eines Händlers |
| AldiRegion | Enum-Wert: `NORD` oder `SUED` |
| RegionType | Art der regionalen Abhängigkeit: `BUNDESWEIT`, `OPTIONAL_FILIALE`, `PLZ_BASIERT`, `ALDI_REGION` |
| UrlMode | Art der URL-Ermittlung: `STATIC_ENTRYPOINT`, `DYNAMIC_WEEKLY`, `LOCATION_RESOLVED` |
| Standortkontext | PLZ, Region oder konkrete Filiale, die zur korrekten Prospektauflösung nötig ist |
| Resolver | Händler-spezifische Logik, die aus PLZ und Händler eine konkrete Prospektseite ermittelt |
| Health-Check | Automatische Prüfung, ob gespeicherte URLs erreichbar sind |

---

## Quellen für die URL-Recherche

- Aldi Nord: `https://www.aldi-nord.de/prospekte.html`
- Aldi Süd: `https://www.aldi-sued.de/prospekte`
- Lidl: `https://www.lidl.de/c/online-prospekte/s10005610`
- Penny: `https://www.penny.de/angebote`
- Netto Marken-Discount: `https://www.netto-online.de/ueber-netto/Online-Prospekte.chtm`
- Kaufland: `https://filiale.kaufland.de/prospekte.html`
- REWE: `https://www.rewe.de/angebote/nationale-angebote/`
- EDEKA: `https://www.edeka.de/angebote/`
- EDEKA Marktsuche: `https://www.edeka.de/marktsuche.jsp`
