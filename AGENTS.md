# Agentenrichtlinien für das Projekt

# Geltungsbereich: Repository-Wurzel

## Zweck

Diese Datei regelt das Verhalten von Agenten im Repository.
Projektinhalt, Geltungsbereich, Architektur, Händlerregeln, URL-Strategie, Technologiestack, Testfälle und Umsetzungsreihenfolge stehen im `PROJEKTPLAN.md`.

## Quellen

* Referenzen:
  * `todo.md` für laufende Arbeit
  * `lessons.md` für wiederverwendbare Korrekturen
  * `docs/architecture.md` für Architekturdetails
  * `docs/documentation.md` für Nutzungs- und Verhaltensdokumentation

## Sprache und Kommunikation

* Schreibe Issues, Commit-Nachrichten, PR-Texte, Reviews und projektrelevante Doku auf Deutsch.
* Halte Texte konkret, knapp und ohne Chat-Kontext verständlich.
* Code und technische Bezeichner bleiben auf Englisch.
* UI-Texte standardmäßig Deutsch, sofern der Projektplan nichts anderes verlangt.

## Arbeitsweise

* Bei unklaren oder widersprüchlichen Anforderungen zuerst gezielt klären.
* Änderungen klein halten. Keine fachfremden Refactorings.
* Grundursache beheben. Keine Workarounds als Endzustand.
* Keine unangeforderte Abstraktion einführen.
* Projektentscheidungen nicht aus `AGENTS.md` ableiten, wenn sie im `PROJEKTPLAN.md` definiert sind.

### Aufgabenmanagement
1. **Zuerst planen**: Plan in `todo.md` mit prüfbaren Punkten schreiben.
2. **Plan verifizieren**: Vor Implementierungsstart Rückmeldung einholen.
3. **Fortschritt nachhalten**: Punkte während der Arbeit abhaken.
4. **Änderungen erklären**: Bei jedem Schritt kurze verständliche Zusammenfassung geben.
5. **Ergebnisse dokumentieren**: Review-Abschnitt in `todo.md` ergänzen.
6. **Lektionen festhalten**: `lessons.md` nach Korrekturen aktualisieren.

### Selbstverbesserung
- `lessons.md` zu Sitzungsbeginn für dieses Projekt lesen.
- Nach jeder Korrektur durch den Nutzer `lessons.md` um das Muster ergänzen.
- Regeln für dich selbst formulieren, die denselben Fehler verhindern.

## Qualität und Verifikation

* Keine Aufgabe ohne Verifikation abschließen.
* Tests müssen Verhalten absichern, nicht nur Implementierungsdetails spiegeln.
* Führe die zur Änderung passenden Checks wirklich aus.
* Wenn nicht alles testbar war, dokumentiere klar was lief und was nicht.
* Projektspezifische Akzeptanzkriterien und fachliche Testfälle stehen im `PROJEKTPLAN.md`.

## Dokumentationspflege

* `AGENTS.md` nur für Agentenregeln nutzen.
* Projektinhalt bei Bedarf im `PROJEKTPLAN.md` aktualisieren.
* Nach Verhaltensänderungen `docs/documentation.md` aktualisieren, falls vorhanden oder Teil der Aufgabe.
* Nach Architekturänderungen `docs/architecture.md` aktualisieren, falls vorhanden oder Teil der Aufgabe.

## Sicherheit

* Keine Secrets, Tokens, `.env`-Dateien oder Produktionskonfigurationen committen.
* Sensible Dateien nur ändern, wenn explizit beauftragt.
* Sicherheits- oder Rechtsrisiken sofort markieren und sichere Alternative nennen.
* Keine inoffiziellen APIs, Scraping oder andere riskante Quellen stillschweigend einführen. Maßgeblich ist der `PROJEKTPLAN.md`.

## Abhängigkeiten und Tools

* Erst Standardbibliothek, dann Plattformmittel, dann vorhandene Abhängigkeiten.
* Neue Abhängigkeiten nur mit klarem Nutzen und ohne einfachere saubere Alternative.
* Keine großflächigen Formatierungsänderungen mit Fachänderungen mischen.

## Abschlussregel

* Vor Abschluss prüfen: Ist die Änderung klein, korrekt, verifiziert und mit dem `PROJEKTPLAN.md` konsistent?
