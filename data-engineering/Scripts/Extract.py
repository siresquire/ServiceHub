import pandas as pd
from sqlalchemy import create_engine, text
from config import DATABASE_URL
from Scripts.Utils import logger,retry

engine = create_engine(DATABASE_URL)


@retry()
def extract_requests():
    """Extract service requests from database."""
    logger.info("Extracting service requests...")

    query = text("""
        SELECT sr.id, sr.title, sr.category, sr.priority, sr.status,
               sr.created_at, sr.updated_at, sr.resolved_at,
               sr.assignee_id, sr.reopened_count,
               u.name AS requester_name, d.name AS department_name
        FROM service_requests sr
        JOIN users u ON sr.requester_id = u.id
        LEFT JOIN departments d ON sr.department_id = d.id
    """)

    try:
        with engine.connect() as conn:
            df = pd.read_sql(query, conn)

        logger.info(f"Extracted {len(df)} service requests")
        return df

    except Exception as e:
        logger.exception("Error extracting service requests")
        raise


@retry()
def extract_sla_policies():
    """Extract SLA policies."""
    logger.info("Extracting SLA policies...")

    query = text("SELECT * FROM sla_policies")

    try:
        with engine.connect() as conn:
            df = pd.read_sql(query, conn)

        logger.info(f"Extracted {len(df)} SLA policies")
        return df

    except Exception:
        logger.exception("Error extracting SLA policies")
        raise