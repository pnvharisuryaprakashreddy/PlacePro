# PlacePro

PlacePro is a desktop placement management system for college campuses. It supports the full placement lifecycle — student registration, drive publishing, applications, interview scheduling, outcomes, reporting, and optional operational monitoring — through role-based Java Swing consoles backed by a shared MySQL database.

## Tech Stack

| Layer | Technology |
| --- | --- |
| UI | Java Swing (desktop client) |
| Business logic | Java service layer |
| Data access | JDBC (raw, no ORM) |
| Database | MySQL 8+ |
| Security | BCrypt password hashing, session-based auth, role authorization |
| Reporting | Apache PDFBox (PDF export), JFreeChart (analytics charts) |
| Logging | SLF4J + Logback → `logs/placepro.log` |
| Monitoring (optional) | Prometheus Java client + embedded `/metrics` endpoint; Grafana via Docker Compose |
| Build | Maven 11, JUnit 5 |

## Prerequisites

- JDK 11 or newer
- Maven 3.8+
- MySQL 8.0+
- (Optional) Docker — for the Prometheus/Grafana monitoring stack

## Setup

### 1. Clone and configure

```bash
git clone <repository-url>
cd placepro
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Edit `config.properties` with your local MySQL credentials, resume directory, and metrics port:

```properties
db.url=jdbc:mysql://localhost:3306/placepro
db.user=your_username
db.password=your_password
resumes.directory=resumes
metrics.port=9400
```

> `config.properties` is git-ignored. Never commit real credentials.

### 2. Run database migrations

See [docs/DB_SETUP.md](docs/DB_SETUP.md) for full details.

```bash
mysql -u your_username -p < db/migrations/V1__init_schema.sql
mysql -u your_username -p < db/migrations/V2__seed_data.sql
mysql -u your_username -p < db/migrations/V2__search_indexes.sql
```

### 3. Build

```bash
mvn clean package
```

This produces a standalone fat JAR at **`target/placepro.jar`** containing all dependencies (MySQL connector, BCrypt, Gson, PDFBox, JFreeChart, Prometheus client, Logback, etc.).

### 4. Run

```bash
java -jar target/placepro.jar
```

The login screen appears. Select a role (Student, Placement Officer, Admin, or Recruiter) to sign in. Seed data credentials are documented in [docs/DB_SETUP.md](docs/DB_SETUP.md).

## Project Structure

```
src/main/java/com/placepro/
  ui/           Swing panels and consoles per role
  service/      Business logic and authorization
  dao/          JDBC data access
  model/        Domain entities
  monitoring/   Prometheus metrics registry
  util/         DB connection, logging, transactions
db/migrations/  SQL schema, seed data, indexes
docs/           Setup, monitoring, and backup guides
monitoring/     Docker Compose for Prometheus + Grafana
```

## Testing

```bash
mvn test
```

Unit tests cover authentication (registration, login, lockout), eligibility checks, application submission (duplicate prevention, transactional rollback), and drive lifecycle transitions.

## Documentation

| Guide | Description |
| --- | --- |
| [docs/DB_SETUP.md](docs/DB_SETUP.md) | MySQL schema migration and seed data |
| [docs/MONITORING.md](docs/MONITORING.md) | Optional Prometheus/Grafana operational monitoring |
| [docs/BACKUP.md](docs/BACKUP.md) | Pre-season database and resume backup procedures |

## Optional Monitoring

Each PlacePro desktop client exposes a Prometheus `/metrics` endpoint (default port 9400). This is **optional operational tooling** for the placement office IT team during peak season — it does not change the core desktop architecture and all data stays on the campus LAN.

To spin up a local Prometheus + Grafana stack:

```bash
cd monitoring
docker compose up -d
```

See [docs/MONITORING.md](docs/MONITORING.md) for scrape configuration, starter Grafana dashboards, and fleet deployment across lab machines.

## Backup

Before each placement season, back up both the MySQL database and the `resumes/` upload directory. See [docs/BACKUP.md](docs/BACKUP.md).

## License

Academic / demonstration project.
