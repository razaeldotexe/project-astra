# Project Astra

A Discord Bot built with Java using the [JDA (Java Discord API)](https://github.com/discord-jda/JDA) library.

## Prerequisites

- Java 25 or higher
- Maven 3.x

## Structure

- `src/main/java/` : Contains the source code of the bot.
- `pom.xml` : Maven configuration containing the project dependencies.

## Setup and Run

1. Clone this repository.
2. Build the project using Maven:
   ```sh
   mvn clean compile
   ```
3. Run the bot:
   ```sh
   mvn exec:java
   ```

Make sure you configure your bot token (e.g., via environment variables or a configuration file) before running.

## Dependencies

- JDA 5.0.0-beta.20
- slf4j-simple 2.0.12
