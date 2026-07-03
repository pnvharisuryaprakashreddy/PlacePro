# Database Setup

These scripts initialize the local MySQL schema for PlacePro and load basic sample data for development.

## Prerequisites

- MySQL 8.0 or newer
- A local MySQL user with permission to create databases and tables
- The project files present under `db/migrations`

## Migration Files

- `db/migrations/V1__init_schema.sql`: creates the `placepro` database and all tables
- `db/migrations/V2__seed_data.sql`: inserts local sample data for testing

## Run the Schema Migration

If you want to use the exact command below, `V1` will create the database automatically:

```bash
mysql -u root -p < db/migrations/V1__init_schema.sql
```

## Seed Local Test Data

After the schema is created, run:

```bash
mysql -u root -p < db/migrations/V2__seed_data.sql
```

## Verify the Setup

Open MySQL and inspect the schema:

```bash
mysql -u root -p
```

Then run:

```sql
USE placepro;
SHOW TABLES;
SELECT COUNT(*) FROM students;
SELECT COUNT(*) FROM placement_officers;
SELECT COUNT(*) FROM companies;
SELECT COUNT(*) FROM recruiters;
SELECT COUNT(*) FROM placement_drives;
```

## Re-running the Scripts

- `V1__init_schema.sql` is intended for initial setup. If tables already exist, drop the `placepro` database first or apply the script on a clean instance.
- `V2__seed_data.sql` inserts fixed sample rows and will fail on duplicate unique values if you run it multiple times without clearing data.

## Notes

- The schema uses InnoDB tables and foreign keys as defined in the PRD.
- `applications` prevents duplicate student submissions to the same drive via a unique constraint on `(student_id, drive_id)`.
- `interview_schedule` prevents duplicate interview rounds per application via a unique constraint on `(application_id, round_number)`.
