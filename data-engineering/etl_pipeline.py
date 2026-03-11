import os
import signal
import sys

from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.triggers.cron import CronTrigger

from Scripts.Extract import extract_requests, extract_sla_policies
from Scripts.Transform import (
    transform_sla_metrics,
    transform_daily_volume,
    transform_agent_performance,
    transform_sla_breaches,
    transform_department_workload,
    transform_sla_compliance,
)
from Scripts.Load import load_table
from Scripts.Utils import logger
from Scripts.test_db import task_test_db_connection
from Scripts.validate_sla_policies import task_validate_sla_policies

OUTPUT_DIR = "output"

# ---------------------------------------------------------------------------
# Schedule — override via environment variables if needed
# ---------------------------------------------------------------------------
# Defaults to every day at 06:00. Set SCHEDULE_HOUR / SCHEDULE_MINUTE in
# your .env or docker-compose environment block to change.
SCHEDULE_HOUR   = int(os.getenv("SCHEDULE_HOUR",   "6"))
SCHEDULE_MINUTE = int(os.getenv("SCHEDULE_MINUTE", "0"))


def run_pipeline():
    """Extract, transform, and load all ServiceHub analytics tables."""

    logger.info("Starting ServiceHub ETL pipeline")

    try:
        os.makedirs(OUTPUT_DIR, exist_ok=True)

        # --- Pre-flight checks ---
        task_test_db_connection()
        task_validate_sla_policies()

        # --- Extract ---
        requests_df = extract_requests()
        sla_df      = extract_sla_policies()

        # --- Transform ---
        sla_metrics        = transform_sla_metrics(requests_df)
        daily_volume       = transform_daily_volume(requests_df)
        agent_performance  = transform_agent_performance(requests_df)
        sla_breaches       = transform_sla_breaches(requests_df, sla_df)
        department_workload = transform_department_workload(requests_df)
        sla_compliance     = transform_sla_compliance(requests_df, sla_df)

        # --- Load ---
        load_table(sla_metrics,         "analytics_sla_metrics")
        load_table(daily_volume,         "analytics_daily_volume")
        load_table(agent_performance,    "analytics_agent_performance")
        load_table(sla_breaches,         "analytics_sla_breaches")
        load_table(department_workload,  "analytics_department_workload")
        load_table(sla_compliance,       "analytics_sla_compliance")

        # --- Export ---
        csv_path = os.path.join(OUTPUT_DIR, "sla_compliance.csv")
        sla_compliance.to_csv(csv_path, index=False)
        logger.info(f"SLA compliance CSV exported to {csv_path}")

        logger.info("ETL pipeline completed successfully")

    except Exception:
        logger.exception("Pipeline failed")


def start_scheduler():
    """Start the blocking scheduler and register a graceful shutdown handler."""

    scheduler = BlockingScheduler(timezone="UTC")

    scheduler.add_job(
        run_pipeline,
        trigger=CronTrigger(hour=SCHEDULE_HOUR, minute=SCHEDULE_MINUTE),
        id="servicehub_etl",
        name="ServiceHub ETL Pipeline",
        max_instances=1,          # prevent overlapping runs
        misfire_grace_time=300,   # allow up to 5 min late start before skipping
    )

    # Graceful shutdown on SIGTERM / SIGINT (Docker stop sends SIGTERM)
    def shutdown(signum, frame):
        logger.info("Shutdown signal received — stopping scheduler")
        scheduler.shutdown(wait=False)
        sys.exit(0)

    signal.signal(signal.SIGTERM, shutdown)
    signal.signal(signal.SIGINT,  shutdown)

    logger.info(
        f"Scheduler started — pipeline runs daily at "
        f"{SCHEDULE_HOUR:02d}:{SCHEDULE_MINUTE:02d} UTC"
    )

    # Run once immediately on startup so you don't wait until the next
    # scheduled window to verify everything works.
    logger.info("Running pipeline immediately on startup...")
    run_pipeline()

    scheduler.start()


if __name__ == "__main__":
    start_scheduler()