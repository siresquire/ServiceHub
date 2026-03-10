import os
import sys
import random
import pandas as pd
from sqlalchemy import create_engine, text

# Add parent directory to path so config.py can be imported
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from config import DATABASE_URL

engine = create_engine(DATABASE_URL)

# SLA resolution hours per (category, priority) – matches sla_policies seed
SLA_RESOLUTION = {
    ('IT_SUPPORT', 'HIGH'): 4,   ('IT_SUPPORT', 'MEDIUM'): 24,
    ('IT_SUPPORT', 'LOW'): 48,   ('IT_SUPPORT', 'CRITICAL'): 4,
    ('HR_REQUEST', 'HIGH'): 8,   ('HR_REQUEST', 'MEDIUM'): 48,
    ('HR_REQUEST', 'LOW'): 96,   ('HR_REQUEST', 'CRITICAL'): 24,
    ('FACILITIES', 'HIGH'): 8,   ('FACILITIES', 'MEDIUM'): 24,
    ('FACILITIES', 'LOW'): 72,   ('FACILITIES', 'CRITICAL'): 12,
}


def get_ids(conn, query, params=None):
    result = conn.execute(text(query), params or {})
    return [row[0] for row in result]


def load_sample_to_source():
    csv_path = os.path.join(os.path.dirname(__file__), "sample_data.csv")
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
        agent_ids = get_ids(conn, "SELECT id FROM users WHERE role = 'AGENT'::user_role")
        user_ids  = get_ids(conn, "SELECT id FROM users WHERE role = 'USER'::user_role")
        dept_ids  = get_ids(conn, "SELECT id FROM departments")

        if not agent_ids or not user_ids or not dept_ids:
            print("Error: seed users/departments missing. Run _reset_db.py first.")
            return

        db_df = pd.DataFrame()
        db_df['title']       = df['title']
        db_df['description'] = df['description']
        db_df['category']    = df['category']       # matches enum directly
        db_df['priority']    = df['priority']
        db_df['status']      = df['status']          # matches enum directly
        db_df['requester_id'] = [random.choice(user_ids) for _ in range(len(df))]
        db_df['assignee_id']  = [
            random.choice(agent_ids) if s not in ('OPEN',) else None
            for s in df['status']
        ]
        db_df['department_id'] = [random.choice(dept_ids) for _ in range(len(df))]
        db_df['created_at']    = pd.to_datetime(df['created_at'])
        db_df['updated_at']    = pd.to_datetime(df['updated_at'])
        db_df['resolved_at']   = pd.to_datetime(df['resolved_at'])

        # Compute first_response_at (a few hours after created_at for non-OPEN)
        db_df['first_response_at'] = db_df.apply(
            lambda r: r['created_at'] + pd.Timedelta(hours=random.uniform(0.5, 4))
            if r['status'] != 'OPEN' else pd.NaT, axis=1
        )

        # SLA hours from policy
        db_df['sla_hours'] = [
            SLA_RESOLUTION.get((cat, pri), 24)
            for cat, pri in zip(df['category'], df['priority'])
        ]

        # Compute performance metrics
        db_df['response_time_hours'] = (
            (db_df['first_response_at'] - db_df['created_at'])
            .dt.total_seconds() / 3600
        ).round(2)
        db_df['resolution_time_hours'] = (
            (db_df['resolved_at'] - db_df['created_at'])
            .dt.total_seconds() / 3600
        ).round(2)

        # SLA breached if resolution_time_hours > sla_hours
        db_df['sla_breached'] = db_df['resolution_time_hours'] > db_df['sla_hours']
        db_df.loc[db_df['resolved_at'].isna(), 'sla_breached'] = False

        db_df['reopened_count'] = 0
        db_df['is_archived']   = False

        print(f"Inserting {len(db_df)} rows into 'service_requests'...")
        try:
            db_df.to_sql("service_requests", engine, if_exists="append", index=False)
            print(f"Successfully loaded {len(db_df)} service requests!")
        except Exception as e:
            print(f"Error loading to DB: {e}")

if __name__ == "__main__":
    load_sample_to_source()
