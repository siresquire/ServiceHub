from sqlalchemy import create_engine
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
        df.to_sql(
            table_name,
            engine,
            if_exists="replace",
            index=False
        )

        logger.info(f"Loaded {len(df)} rows into {table_name}")

    except Exception:
        logger.exception(f"Error loading table {table_name}")
        raise