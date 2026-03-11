from sqlalchemy import create_engine, text
from config import DATABASE_URL
from Scripts.Utils import logger, retry

engine = create_engine(DATABASE_URL)


@retry()
def load_table(df, table_name):
    """Load dataframe into analytics table."""
    if df.empty:
        logger.warning(f"{table_name} skipped (no data)")
        return

    try:
        with engine.begin() as conn:
            conn.execute(text(f"TRUNCATE TABLE {table_name}"))

        df.to_sql(
            table_name,
            engine,
            if_exists="append",
            index=False
        )

        logger.info(f"Loaded {len(df)} rows into {table_name}")

    except Exception:
        logger.exception(f"Error loading table {table_name}")
        raise