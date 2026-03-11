from Scripts.Extract import extract_requests, extract_sla_policies
from Scripts.Transform import (
    transform_sla_metrics,
    transform_daily_volume,
    transform_agent_performance,
    transform_sla_breaches,
    transform_department_workload
)
from Scripts.Load import load_table
from Scripts.Utils import logger


def run_pipeline():

    logger.info("Starting ServiceHub ETL pipeline")

    try:
        requests_df = extract_requests()
        sla_df = extract_sla_policies()

        # Transformations
        sla_metrics = transform_sla_metrics(requests_df)
        daily_volume = transform_daily_volume(requests_df)
        agent_performance = transform_agent_performance(requests_df)
        sla_breaches = transform_sla_breaches(requests_df, sla_df)
        department_workload = transform_department_workload(requests_df)
    
        # Loads
        load_table(sla_metrics, "analytics_sla_metrics")
        load_table(daily_volume, "analytics_daily_volume")
        load_table(agent_performance, "analytics_agent_performance")
        load_table(sla_breaches, "analytics_sla_breaches")
        load_table(department_workload, "analytics_department_workload")

        logger.info("ETL pipeline completed successfully")

    except Exception:
        logger.exception("Pipeline failed")


if __name__ == "__main__":
    run_pipeline()