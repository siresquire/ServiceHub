import os
from dotenv import load_dotenv

# Resolve .env from the project root (one level above data-engineering/)
_env_path = os.path.join(os.path.dirname(__file__), "..", ".env.example")
load_dotenv(dotenv_path=_env_path)

DB_CONFIG = {
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASSWORD"),
    "host": os.getenv("DB_HOST"),
    "port": os.getenv("DB_PORT"),
    "database": os.getenv("DB_NAME"),
}

DATABASE_URL = (
    f"postgresql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
    f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
)