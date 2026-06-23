# Issue #1

Title: [feat] Backend-Basis für Händlerkonfiguration und Standortregeln
URL: https://github.com/jantrw/KaufeSchlau/issues/1

## Context
Phase 1 braucht eine belastbare Backend-Grundlage, bevor API, CLI und Frontend sauber darauf aufbauen können. Dazu gehören Händlerkonfiguration, Domänenmodell und die Regeln, wann Standortkontext zwingend ist.

## Acceptance Criteria
- [ ] Spring-Boot-Backend-Modul mit Java 21 ist angelegt und startet lokal.
- [ ] `discounters.yml` enthält die 8 definierten Händler aus dem Projektplan mit URL-Modus und Standortregeln.
- [ ] Domänenmodell für Händler, URL-Modus, Aldi-Region und Standortanforderung ist implementiert.
- [ ] `plz-bundesland.json` ist eingebunden und die Aldi-Nord/Süd-Auflösung aus PLZ ist als Service implementiert.
- [ ] Eine fachliche Prüfung entscheidet für eine gegebene Händlermenge, ob PLZ oder Region erforderlich ist.
- [ ] Verhalten ist durch Unit-Tests für Aldi-Region und Standortpflicht abgesichert.

## Technical Notes
Abhängigkeit für alle weiteren Phase-1-Backend-Themen.

## Out of Scope
Keine REST-Endpunkte.
Keine CLI.
Keine Frontend-Komponenten.
