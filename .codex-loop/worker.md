Umgesetzt:
- `spring-boot-starter-web` durch `spring-boot-starter` ersetzt.
- `jackson-databind` gezielt ergänzt, weil bestehender JSON-Code Jackson braucht.
- `DiscounterConfigTest` prüft jetzt Aldi- und PLZ-basierte Standortregeln.

Verifikation:
- `mvn test`: 9 Tests, 0 Fehler.
- `git diff` geprüft. Hinweis: `discounter-backend/` ist untracked, daher zeigt `git diff` keinen Patch; `git status` zeigt `?? discounter-backend/`.

Offene Fragen: keine.

READY_FOR_REVIEW