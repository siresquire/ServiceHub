import logging
import time
from functools import wraps

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [%(name)s] %(message)s"
)

logger = logging.getLogger("servicehub_etl")


def retry(retries=3, delay=5):
    """Retry decorator for transient failures."""
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            for attempt in range(1, retries + 1):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    logger.error(f"{func.__name__} failed (attempt {attempt}/{retries})")
                    logger.error(str(e))

                    if attempt == retries:
                        raise

                    time.sleep(delay)
        return wrapper
    return decorator