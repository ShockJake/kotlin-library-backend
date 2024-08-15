# Kotlin Library/Book Shop back-end

Simple Library/Book Shop back-end written in Kotlin.

- Has integration with Discord via Bot.
- Fetches book data from <https://openlibrary.org>

## Technologies

- Java 21
- Kotlin 1.9
- Gradle 8.7

## Running

- Clone repository:

```bash
git clone https://github.com/ShockJake/ebiznes-uj.git; cd Kotlin-library-bot
```

- Set your discord token in tokens.json
- Build and run the jar

```bash
gradle buildFatJar; java -jar .\build\libs\kotlin-library-bot.jar
```

## Linter in Pre-Commit hooks

**Ktlint** can be added to pre-commit hooks via copying contents of `pre-commit`
to `./.git/hooks/pre-commit`
