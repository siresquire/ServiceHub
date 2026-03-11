import logging
from sqlalchemy import create_engine, text
from config import DATABASE_URL

log = logging.getLogger("servicehub_etl")

def task_test_db_connection(**ctx):
    """Verify the database is reachable before doing any real work."""
    
    log.info("Testing database connection…")
    engine = create_engine(DATABASE_URL)
    with engine.connect() as conn:
        conn.execute(text("SELECT 1"))
    log.info("Database connection OK.")