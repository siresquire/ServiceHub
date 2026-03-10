import os
import random
import pandas as pd
from sqlalchemy import create_engine, text
from config import DATABASE_URL

engine = create_engine(DATABASE_URL)

# CSV value → DB enum mappings
CATEGORY_MAP = {'IT_SUPPORT': 'IT', 'FACILITIES': 'FACILITIES', 'HR_REQUEST': 'HR'}
STATUS_MAP   = {'OPEN': 'SUBMITTED', 'ASSIGNED': 'ASSIGNED', 'IN_PROGRESS': 'IN_PROGRESS',
                'RESOLVED': 'RESOLVED', 'CLOSED': 'CLOSED'}

def get_ids(conn, query, params=None):
    result = conn.execute(text(query), params or {})
    return [row[0] for row in result]

def load_sample_to_source():
    csv_path = os.path.join(os.path.dirname(__file__), "data", "sample_data.csv")
    if not os.path.exists(csv_path):
        print(f"Error: {csv_path} not found.")
        return

    print(f"Reading sample data from {csv_path}...")
    try:
        df = pd.read_csv(csv_path, encoding="utf-8")
    except UnicodeDecodeError:
        print("UTF-8 decode failed, trying Windows-1252...")
        df = pd.read_csv(csv_path, encoding="cp1252")

    print(f"Loaded {len(df)} rows from CSV.")

    with engine.connect() as conn:
        agent_ids = get_ids(conn, "SELECT id FROM users WHERE role = :r", {"r": "AGENT"})
        emp_ids   = get_ids(conn, "SELECT id FROM users WHERE role = :r", {"r": "EMPLOYEE"})
        dept_ids  = get_ids(conn, "SELECT id FROM departments")

        if not agent_ids or not emp_ids or not dept_ids:
            print("Error: seed users/departments missing. Start the backend first.")
            return

        db_df = pd.DataFrame()
        db_df['title']          = df['title']
        db_df['description']    = df['description']
        db_df['category']       = df['category'].map(CATEGORY_MAP)
        db_df['priority']       = df['priority']
        db_df['status']         = df['status'].map(STATUS_MAP).fillna(df['status'])
        db_df['requester_id']   = [random.choice(emp_ids) for _ in range(len(df))]
        db_df['assigned_to_id'] = [
            random.choice(agent_ids) if s not in ('OPEN', 'SUBMITTED') else None
            for s in df['status']
        ]
        db_df['department_id']  = [random.choice(dept_ids) for _ in range(len(df))]
        db_df['created_at']     = pd.to_datetime(df['created_at'])
        db_df['updated_at']     = pd.to_datetime(df['updated_at'])
        db_df['resolved_at']    = pd.to_datetime(df['resolved_at'])

        # Compute sla_deadline = created_at + SLA resolution hours per priority
        sla_hours = {'LOW': 48, 'MEDIUM': 24, 'HIGH': 8, 'CRITICAL': 4}
        db_df['sla_deadline'] = db_df['created_at'] + df['priority'].map(sla_hours).apply(
            lambda h: pd.Timedelta(hours=h)
        )

        print(f"Inserting {len(db_df)} rows into 'service_requests'...")
        try:
            db_df.to_sql("service_requests", engine, if_exists="append", index=False)
            print(f"Successfully loaded {len(db_df)} service requests!")
        except Exception as e:
            print(f"Error loading to DB: {e}")

if __name__ == "__main__":
    load_sample_to_source()
