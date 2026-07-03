# PlacePro Backup and Restore

Run these steps **before each placement season** and again before any major schema change or server migration. Store backups on a separate machine or network share — not on the same disk as the live MySQL data directory.

## What to back up

PlacePro has two independent data stores:

| Store | Location | Contents |
| --- | --- | --- |
| **MySQL database** | Host configured in `config.properties` (`db.url`) | Students, applications, drives, companies, interviews, notifications |
| **Resume files** | `resumes/` directory (see `resumes.directory` in config) | Uploaded student CVs referenced by the `resumes` table |

Backing up only the database **does not** restore uploaded PDFs/DOCs. Always back up **both** the database and the `resumes/` folder together.

---

## 1. Database backup (mysqldump)

Replace `your_username` with the MySQL user from `config.properties`. You will be prompted for the password.

```bash
mkdir -p backups
mysqldump -u your_username -p \
  --single-transaction \
  --routines \
  --triggers \
  --databases placepro \
  > backups/placepro_$(date +%Y%m%d_%H%M%S).sql
```

Recommended flags:

- `--single-transaction` — consistent snapshot without locking tables (InnoDB).
- `--routines --triggers` — include stored routines and triggers if any are added later.

Verify the dump file is non-empty:

```bash
ls -lh backups/placepro_*.sql
head -20 backups/placepro_*.sql
```

---

## 2. Resume directory backup

Copy the upload directory alongside the SQL dump:

```bash
tar -czf backups/resumes_$(date +%Y%m%d_%H%M%S).tar.gz resumes/
```

If `resumes.directory` in `config.properties` points elsewhere, archive that path instead.

---

## 3. Database restore

**Warning:** Restore overwrites the current `placepro` database. Stop PlacePro clients first so no one is writing during restore.

```bash
mysql -u your_username -p < backups/placepro_YYYYMMDD_HHMMSS.sql
```

Or, to restore into a fresh empty database:

```bash
mysql -u your_username -p -e "CREATE DATABASE IF NOT EXISTS placepro;"
mysql -u your_username -p placepro < backups/placepro_YYYYMMDD_HHMMSS.sql
```

After restore, confirm row counts look reasonable:

```bash
mysql -u your_username -p placepro -e "
  SELECT 'students' AS tbl, COUNT(*) AS cnt FROM students
  UNION ALL SELECT 'applications', COUNT(*) FROM applications
  UNION ALL SELECT 'placement_drives', COUNT(*) FROM placement_drives;
"
```

---

## 4. Resume directory restore

Extract the archive into the project root (or the path configured in `resumes.directory`):

```bash
tar -xzf backups/resumes_YYYYMMDD_HHMMSS.tar.gz
```

Ensure the application user can read the restored files. Resume paths stored in the database are relative to the working directory PlacePro is launched from.

---

## 5. Pre-season checklist

1. Notify staff that PlacePro will be briefly unavailable.
2. Ask all users to log out and close the desktop client.
3. Run **mysqldump** (Section 1).
4. Archive **resumes/** (Section 2).
5. Copy both artifacts to off-site or campus backup storage.
6. Optionally run a test restore on a staging MySQL instance before the season opens.

---

## 6. Application logs

Operational logs are written to `logs/placepro.log` (see `docs/MONITORING.md` for metrics). Log files are useful for troubleshooting but are **not** a substitute for database backup. Include them in archival if the placement office needs an audit trail for a completed season.
