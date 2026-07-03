# PlacePro

PlacePro is a desktop placement management system for college campuses. The project uses Java Swing for the user interface, a service layer for business logic, raw JDBC for data access, and MySQL 8+ for persistence.

## Tech Stack

- Java 11+
- Maven
- Java Swing
- MySQL 8+
- JDBC
- jBCrypt
- Gson
- JUnit 5

## Setup

1. Install JDK 11 or newer and verify `java -version`.
2. Install MySQL 8+ and create a database such as `placepro`.
3. Copy `src/main/resources/config.properties.example` to `src/main/resources/config.properties`.
4. Update the database settings in `config.properties` with your local values.

## Build

```bash
mvn clean install
```

## Run

```bash
java -jar target/placepro-1.0.0-SNAPSHOT.jar
```
