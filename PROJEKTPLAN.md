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
- Keine hart codierten wöchentlich wechselnden Prospektpfade; dynamische Auflösung folgt über Händler-Resolver, sobald ein Händler sie wirklich braucht
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

| Händler | Besonderheit | Phase-1-Strategie | URL |
|---|---|---|---|
| REWE | PLZ-/marktbasierte Angebote | Offizielle Angebotsseite verlinken; Markt-Resolver in Phase 2 | `https://www.rewe.de/angebote/nationale-angebote/` |
| EDEKA | PLZ-/marktbasierte Angebote | Offizielle Angebotsseite + Marktsuche verlinken; Markt-ID-Resolver in Phase 2 | `https://www.edeka.de/angebote/` |
| Aldi Nord | Region via PLZ-Mapping | Offizielle Prospektseite verlinken | `https://www.aldi-nord.de/prospekte.html` |
| Aldi Süd | Region via PLZ-Mapping | Offizielle Prospektseite verlinken | `https://www.aldi-sued.de/prospekte` |
| Lidl | Filiale wählbar | Offizielle Prospektseite verlinken | `https://www.lidl.de/c/online-prospekte/s10005610` |
| Penny | Angebote und Prospekt der Woche | Offizielle Angebots-/Prospektseite verlinken | `https://www.penny.de/angebote` |
| Netto Marken-Discount | Filiale/PLZ relevant | Offizielle Online-Prospektseite verlinken; Filial-Resolver in Phase 2 | `https://www.netto-online.de/ueber-netto/Online-Prospekte.chtm` |
| Kaufland | Filialangebote und Prospekte | Offizielle Prospektseite verlinken | `https://filiale.kaufland.de/prospekte.html` |

> Hinweis Netto: Unterstützt wird zunächst nur **Netto Marken-Discount**.

---

## Standort-, Region- und Filiallogik

### Grundregel

Die Anwendung unterscheidet zwischen zwei Abrufarten:

1. **Alle Prospekte abrufen** – Der Nutzer muss vorher eine **PLZ und/oder Region** eingeben. Ohne Standortkontext darf der Abruf nicht stillschweigend unvollständige Ergebnisse liefern.
2. **Gefilterte Händler abrufen** – Wenn alle ausgewählten Händler keinen Standort benötigen, ist keine PLZ/Region erforderlich. Sobald mindestens ein Händler PLZ, Region oder Filialauswahl benötigt, fordert das System Standortkontext an.

### Verhalten bei fehlender PLZ/Region

| Abruf | Enthält standortpflichtige Händler? | Verhalten |
|---|---|---|
| Alle Händler | Ja | Fehler/Hinweis: PLZ oder Region erforderlich |
| Nur Aldi Nord/Aldi Süd direkt gewählt | Nein (explizit gewählt) | Link darf angezeigt werden |
| Aldi automatisch bestimmen | Ja | PLZ/Region erforderlich |
| Nur Lidl/Penny/Kaufland ohne Filialauflösung | Nein | Links dürfen angezeigt werden |
| REWE/EDEKA/Netto gewählt | Ja | PLZ/Region oder Filiale erforderlich |

### Filialfilter

- Nutzer kann Händler aktivieren/deaktivieren.
- Später kann pro Händler eine konkrete Filiale gewählt werden.
- Bei Händlern ohne Standortpflicht wird kein Standort abgefragt.
- Bei Händlern mit Standortpflicht wird eine PLZ/Region abgefragt oder eine gespeicherte Filiale verwendet.

### Persistenz von Standortdaten

Für den MVP wird keine Benutzerverwaltung benötigt. Optional kann später lokal gespeichert werden: Standard-PLZ, bevorzugte Region, bevorzugte Händler und bevorzugte Filialen pro Händler.

---

## URL-Beschaffungsstrategie

### Phase 1: Konfiguration + dynamische Auflösungsregeln

Für den MVP wird eine YAML-Konfiguration verwendet. Sie enthält stabile offizielle Einstiegspunkte, Informationen zu Standortpflicht sowie Resolver-Hinweise pro Händler. **Im Code werden keine wöchentlich wechselnden Prospektpfade hart codiert.** Wenn ein Händler jede Woche neue Pfade erzeugt, muss die aktuelle URL zur Laufzeit über einen Resolver oder einen validierten Einstiegspunkt ermittelt werden.

Für Phase 1 sind zwei URL-Modi umgesetzt:

| URL-Modus | Bedeutung | Beispiel |
|---|---|---|
| `STATIC_ENTRYPOINT` | Stabiler offizieller Einstiegspunkt, der selbst auf den aktuellen Prospekt führt | Aldi Nord, Aldi Süd, Lidl |
| `LOCATION_RESOLVED` | URL hängt von PLZ, Region oder Filiale ab | REWE, EDEKA, Netto |

Dynamische Wochen-URLs bekommen erst dann einen eigenen Resolver oder Modus, wenn ein unterstützter Händler ihn konkret benötigt.

### Phase 2: Händler-spezifische Resolver

| Händler | Geplante Resolver-Logik |
|---|---|
| REWE | PLZ → Marktsuche → marktbezogene Angebotsseite wie `/angebote/{ort}/{marketId}/{marketSlug}/` |
| EDEKA | PLZ → Marktsuche → Markt-ID → Prospektseite wie `/markt-id/{marketId}/prospekt.jsp` |
| Netto Marken-Discount | PLZ → Filialfinder → regionale Online-Prospekte |
| Kaufland | PLZ/Filiale → Filialseite/Prospekte |
| Lidl | Filiale optional; Prospektseite bleibt als Einstieg nutzbar |
| Penny | Marktseite optional; `/angebote` bleibt als Einstieg nutzbar |

Resolver-Ergebnisse können gecacht werden (inkl. Gültigkeitszeitraum und Abrufzeitpunkt). Bei Resolver-Fehler fällt das System auf den offiziellen Einstiegspunkt zurück.

### Phase 3: Produktdaten

- HTML-Angebotsseiten bevorzugen
- PDF-/Bild-OCR nur optional
- Fremde Produktbilder nicht dauerhaft speichern
- Caching-Regeln und Nutzungsbedingungen prüfen
- Datenquelle je Händler dokumentieren

---

## Getroffene Entscheidungen

| Thema | Entscheidung |
|---|---|
| Aldi-Region-Erkennung | Automatisch via PLZ-Mapping (Präfix oder Bundesland). Grenzfälle werden in Phase 2 verbessert. |
| Dynamische Wochen-URLs | Keine hart codierten Wochenpfade. Phase 1 nutzt offizielle Einstiegspunkte; konkrete dynamische Resolver folgen erst bei Bedarf. |
| PLZ-Pflicht bei „alle Prospekte" | Abruf ohne PLZ/Region wird blockiert, damit Ergebnisse nicht unvollständig regionalisiert sind. |
| Phase-1-Verhalten bei PLZ-Händlern | Für MVP nur zur offiziellen Seite verlinken; Resolver erst in Phase 2. |
| Filialauswahl bei mehreren Treffern | Liste anzeigen, sobald Resolver existiert. |
| REWE-/EDEKA-Resolver | Erst in Phase 2. |
| Netto Marken-Discount | Als PLZ-basiert behandeln. |

---

## Offene Punkte

| Priorität | Thema | Frage | Empfehlung |
|---|---|---|---|
| Hoch | Händlerfilter ohne PLZ | Welche Händler dürfen ohne Standort angezeigt werden? | Nur Händler mit `requiresLocationContext=false` oder explizit manuell gewählte Aldi-Variante |
| Hoch | Kaufland/Lidl/Penny Filialauswahl | Soll Filialauswahl auch dort direkt unterstützt werden? | Optional nach MVP |
| Hoch | Rechtliches | Wird die Anwendung öffentlich gehostet? | Vor Scraping klären |
| Mittel | URL-Health-Check | Soll ein wöchentlicher Check defekter URLs eingebaut werden? | Phase 2 |
| Mittel | Produktdaten Phase 3 | Soll Phase 3 auf offiziellen HTML-Seiten oder inoffiziellen Datenquellen basieren? | Offizielle HTML-Seiten bevorzugen |
| Niedrig | Native CLI | Soll die CLI später als native Binary gebaut werden? | Optional mit GraalVM |
| Niedrig | Benutzerprofile | Sollen Favoriten/Standard-PLZ gespeichert werden? | Nicht im MVP |

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
| UI-Bibliothek | Eigenes CSS |
| HTTP-Client | Browser Fetch API |
| Containerisierung | Docker + Nginx |

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

Wenn der Nutzer „alle Händler" auswählt, muss die PLZ-/Region-Eingabe sichtbar und verpflichtend sein.

---

## Docker Compose Setup

Das lokale Compose-Setup steht in `docker-compose.yml`.

CLI starten:

```bash
docker compose --profile cli run --rm cli
```

---

## Nicht-Ziele für den MVP

- keine Produktdaten-Extraktion
- keine OCR-Verarbeitung
- keine Speicherung von Prospekt-PDFs
- keine Speicherung fremder Produktbilder
- keine Nutzerkonten
- keine Preisverlaufshistorie
- keine Push-Benachrichtigungen
