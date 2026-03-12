import logging
import pandas as pd
from sqlalchemy import create_engine, text
from config import DATABASE_URL

log = logging.getLogger("servicehub_etl")

def task_validate_sla_policies(**ctx):
    """
    Ensure the sla_policies table is populated and contains the
    required columns before extraction starts.
    """
    REQUIRED_COLUMNS = {"category", "priority", "resolution_time_hours"}

    log.info("Validating SLA policies…")
    engine = create_engine(DATABASE_URL)
    with engine.connect() as conn:
        count = conn.execute(
            text("SELECT COUNT(*) FROM sla_policies")
        ).scalar()

    if count == 0:
        raise ValueError("sla_policies table is empty — aborting pipeline.")

    with engine.connect() as conn:
        sample = pd.read_sql(text("SELECT * FROM sla_policies LIMIT 1"), conn)

    missing = REQUIRED_COLUMNS - set(sample.columns)
    if missing:
        raise ValueError(f"sla_policies missing columns: {missing}")

    log.info(f"SLA policies OK — {count} row(s) found.")