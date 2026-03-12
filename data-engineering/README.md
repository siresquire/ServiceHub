## ServiceHub Data Engineering

This package contains the data‑engineering layer for ServiceHub: an automated ETL pipeline that pulls data from the transactional ServiceHub database, builds a star‑schema analytics model, and exports SLA compliance metrics for dashboards and ad‑hoc analysis.

The pipeline is designed to be:

- **Idempotent**: safe to re‑run as part of daily or intra‑day schedules.
- **Observable**: file + console logs, CSV exports, and clear failure logging.
- **Schema‑driven**: loads a well‑documented dimensional model for helpdesk analytics.

For a deep dive into the analytics model, see `docs/analytics-schema.md`.

---

## Architecture Overview

- **Source**: transactional ServiceHub Postgres database  
  - Core tables: `service_requests`, `sla_policies`, `users`, `departments`, etc.
- **Transform / Load**:
  - `Scripts/Extract.py` – queries transactional tables into pandas DataFrames.
  - `Scripts/Load.py` – builds and loads:
    - `dim_date`, `dim_category`, `dim_priority`, `dim_agent`, `dim_department`
    - `fact_ticket_events` with SLA and resolution metrics.
  - `etl_pipeline.py` – end‑to‑end orchestration:
    - Pre‑flight checks (DB connectivity, SLA policy validation).
    - Extract, transform, and load analytics tables.
    - Export SLA compliance CSV to `exports/sla_compliance.csv`.
- **Scheduling**:
  - Uses `apscheduler` with a daily cron trigger.
  - Runs immediately on startup, then at the configured time each day.

---

## Repository Layout (data-engineering)

- `etl_pipeline.py` – main entry point; schedules and runs the pipeline.
- `config.py` – builds the `DATABASE_URL` from environment variables.
- `Scripts/`
  - `Extract.py` – extract requests and SLA policies from Postgres.
  - `Transform.py` – SLA compliance and other transformations.
  - `Load.py` – dimension + fact loaders and pipeline orchestrator.
  - `Utils.py` – logging, retry decorator, shared utilities.
  - `test_db.py` – task to validate DB connectivity.
  - `validate_sla_policies.py` – task to validate SLA reference data.
- `schema/`
  - `source_schema.sql` – transactional schema reference.
  - `analytics_schema.sql` – star‑schema analytics model.
  - `sample_data.sql` – seed data for local experimentation.
- `docs/analytics-schema.md` – detailed documentation of the analytics model.
- `Dockerfile` – container image for running the ETL pipeline.
- `requirements.txt` – Python dependencies for this package.

---

## Prerequisites

- **Python**: 3.11+ (matches the Docker image)
- **Postgres**: running instance with the ServiceHub schema applied
- **Dependencies**:

```bash
cd ServiceHub/data-engineering
pip install -r requirements.txt
```

---

## Configuration

Database connectivity is configured via environment variables. In the project root there is an example file `.env.example` that documents the expected variables.

The data‑engineering package currently loads this file via `config.py`. For a real deployment you should:

- **Create a `.env` file at the project root** (next to `.env.example`).
- Ensure it defines at least:

```bash
DB_USER=servicehub
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=5432
DB_NAME=servicehub
```

Scheduling is controlled with optional environment variables (default: `06:00` UTC):

```bash
SCHEDULE_HOUR=6      # 0–23
SCHEDULE_MINUTE=0    # 0–59
```

These are read in `etl_pipeline.py` and used by the APScheduler cron trigger.

---

## Running the Pipeline (Local)

From the `ServiceHub/data-engineering` directory:

1. **Ensure DB and schema exist**  
   - Postgres is running and accessible with the credentials in your `.env`.  
   - The ServiceHub transactional schema and analytics schema (`schema/*.sql`) have been applied.

2. **Run the ETL once (foreground)**:

```bash
python etl_pipeline.py
```

This will:

- Run pre‑flight checks (`Scripts.test_db`, `Scripts.validate_sla_policies`).
- Extract requests and SLA policies.
- Load dimension and fact tables into the analytics schema.
- Export a CSV of SLA compliance metrics into the local `exports/` directory.

Logs are written to:

- `logs/etl_pipeline.log`
- Standard output (your terminal)

---

## Running with Docker

You can also run the ETL pipeline in a container (useful for CI/CD or scheduled jobs).

From `ServiceHub/data-engineering`:

```bash
docker build -t servicehub-etl .
```

Then run, passing your environment file from the project root:

```bash
docker run --rm \
  --env-file ../.env \
  -v "%cd%/exports:/app/exports" \
  -v "%cd%/logs:/app/logs" \
  servicehub-etl
```

The container:

- Installs dependencies from `requirements.txt`.
- Starts `etl_pipeline.py`, which runs the pipeline immediately and then according to the configured schedule.

On Linux/macOS, adapt the volume mounts accordingly:

```bash
docker run --rm \
  --env-file ../.env \
  -v "$(pwd)/exports:/app/exports" \
  -v "$(pwd)/logs:/app/logs" \
  servicehub-etl
```

---

## Troubleshooting

- **DB connection errors**
  - Verify `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, and `DB_NAME`.
  - You can run the DB test task directly:

    ```bash
    python -m Scripts.test_db
    ```

- **SLA policy validation failures**
  - Ensure `sla_policies` in the source DB matches expected categories and priorities.
  - See `Scripts/validate_sla_policies.py` and `docs/analytics-schema.md` for details.

- **Empty or partial data in analytics tables**
  - Confirm that the transactional tables contain data for the date range.
  - Check `logs/etl_pipeline.log` for warnings about skipped rows or missing foreign keys.

---

## Extending the Pipeline

- **New dimensions**: add loaders in `Scripts/Load.py` and update `analytics_schema.sql`.
- **New facts or measures**: extend `fact_ticket_events` or create new fact tables and loaders.
- **Additional exports**: compute extra aggregates in a Transform step and write them to CSV or database tables.

Keep the contract of the analytics schema (`schema/analytics_schema.sql` and `docs/analytics-schema.md`) stable where possible, so dashboards and BI tools remain compatible as the pipeline evolves.

