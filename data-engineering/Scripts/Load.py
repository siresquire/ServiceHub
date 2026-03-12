from datetime import date, timedelta

import pandas as pd
from sqlalchemy import create_engine, text

from config import DATABASE_URL
from Scripts.Utils import logger, retry

engine = create_engine(DATABASE_URL)


# ─── GENERIC LOADER ───────────────────────────────────────────────────────────

@retry()
def load_table(df, table_name):
    """Truncate-and-reload a table. Used for simple aggregated/reporting tables."""
    if df.empty:
        logger.warning(f"{table_name} skipped (no data)")
        return

    try:
        with engine.begin() as conn:
            conn.execute(text(f"TRUNCATE TABLE {table_name}"))

        df.to_sql(table_name, engine, if_exists="append", index=False)
        logger.info(f"Loaded {len(df)} rows into {table_name}")

    except Exception:
        logger.exception(f"Error loading table {table_name}")
        raise


# ─── DIMENSION LOADERS ────────────────────────────────────────────────────────

@retry()
def load_dim_date(start: date, end: date) -> None:
    """Populate dim_date for a date range. Safe to re-run (upsert)."""
    logger.info(f"Loading dim_date from {start} to {end}...")

    rows = []
    current = start
    while current <= end:
        rows.append({
            "date_key":        int(current.strftime("%Y%m%d")),
            "full_date":       current,
            "day_of_week":     current.isoweekday(),
            "day_name":        current.strftime("%A"),
            "week_start_date": current - timedelta(days=current.weekday()),
            "week_number":     current.isocalendar()[1],
            "month":           current.month,
            "month_name":      current.strftime("%B"),
            "quarter":         (current.month - 1) // 3 + 1,
            "year":            current.year,
            "is_weekend":      current.isoweekday() >= 6,
        })
        current += timedelta(days=1)

    df = pd.DataFrame(rows)
    try:
        with engine.begin() as conn:
            conn.execute(text("""
                INSERT INTO dim_date (
                    date_key, full_date, day_of_week, day_name,
                    week_start_date, week_number, month, month_name,
                    quarter, year, is_weekend
                )
                SELECT
                    d.date_key, d.full_date, d.day_of_week, d.day_name,
                    d.week_start_date, d.week_number, d.month, d.month_name,
                    d.quarter, d.year, d.is_weekend
                FROM (VALUES {placeholders}) AS d(
                    date_key, full_date, day_of_week, day_name,
                    week_start_date, week_number, month, month_name,
                    quarter, year, is_weekend
                )
                ON CONFLICT (date_key) DO NOTHING
            """.format(
                placeholders=", ".join(
                    [f"(:{i}_date_key, :{i}_full_date, :{i}_dow, :{i}_day_name, "
                     f":{i}_wsd, :{i}_wn, :{i}_month, :{i}_mn, "
                     f":{i}_quarter, :{i}_year, :{i}_iswknd)"
                     for i in range(len(rows))]
                )
            )))
        # Use to_sql with a temp staging approach instead for simplicity
        raise NotImplementedError  # fall through to the pandas upsert below
    except Exception:
        pass

    # Simpler approach: stage then upsert via pandas + raw SQL
    df.to_sql("_stage_dim_date", engine, if_exists="replace", index=False)
    with engine.begin() as conn:
        conn.execute(text("""
            INSERT INTO dim_date (
                date_key, full_date, day_of_week, day_name,
                week_start_date, week_number, month, month_name,
                quarter, year, is_weekend
            )
            SELECT
                date_key, full_date, day_of_week, day_name,
                week_start_date, week_number, month, month_name,
                quarter, year, is_weekend
            FROM _stage_dim_date
            ON CONFLICT (date_key) DO NOTHING
        """))
        conn.execute(text("DROP TABLE IF EXISTS _stage_dim_date"))

    logger.info(f"dim_date loaded: {len(rows)} rows.")


@retry()
def load_dim_category(requests_df: pd.DataFrame, sla_df: pd.DataFrame) -> None:
    """Upsert unique category names into dim_category."""
    logger.info("Loading dim_category...")

    categories = (
        pd.concat([requests_df["category"], sla_df["category"]])
        .dropna()
        .drop_duplicates()
        .rename("category_name")
        .to_frame()
    )

    categories.to_sql("_stage_dim_category", engine, if_exists="replace", index=False)
    with engine.begin() as conn:
        conn.execute(text("""
            INSERT INTO dim_category (category_name)
            SELECT category_name FROM _stage_dim_category
            ON CONFLICT (category_name) DO NOTHING
        """))
        conn.execute(text("DROP TABLE IF EXISTS _stage_dim_category"))

    logger.info(f"dim_category loaded: {len(categories)} categories.")


@retry()
def load_dim_priority(requests_df: pd.DataFrame, sla_df: pd.DataFrame) -> None:
    """Upsert priorities with SLA hours from the sla reference table."""
    logger.info("Loading dim_priority...")

    sla_hours = (
        sla_df.groupby("priority")["resolution_time_hours"]
        .mean()
        .reset_index()
        .rename(columns={"priority": "priority_name", "resolution_time_hours": "sla_hours"})
    )

    all_priorities = (
        pd.concat([requests_df["priority"], sla_df["priority"]])
        .dropna()
        .drop_duplicates()
        .rename("priority_name")
        .to_frame()
    )

    priorities = all_priorities.merge(sla_hours, on="priority_name", how="left")
    priorities.to_sql("_stage_dim_priority", engine, if_exists="replace", index=False)

    with engine.begin() as conn:
        conn.execute(text("""
            INSERT INTO dim_priority (priority_name, sla_hours)
            SELECT priority_name, sla_hours FROM _stage_dim_priority
            ON CONFLICT (priority_name) DO UPDATE
                SET sla_hours = EXCLUDED.sla_hours
        """))
        conn.execute(text("DROP TABLE IF EXISTS _stage_dim_priority"))

    logger.info(f"dim_priority loaded: {len(priorities)} priorities.")


@retry()
def load_dim_agent(requests_df: pd.DataFrame) -> None:
    """Upsert agents from the assignee_id column."""
    logger.info("Loading dim_agent...")

    agents = (
        requests_df["assignee_id"]
        .dropna()
        .drop_duplicates()
        .astype(int)
        .rename("user_id")
        .to_frame()
    )

    agents.to_sql("_stage_dim_agent", engine, if_exists="replace", index=False)
    with engine.begin() as conn:
        conn.execute(text("""
            INSERT INTO dim_agent (user_id)
            SELECT user_id FROM _stage_dim_agent
            ON CONFLICT (user_id) DO NOTHING
        """))
        conn.execute(text("DROP TABLE IF EXISTS _stage_dim_agent"))

    logger.info(f"dim_agent loaded: {len(agents)} agents.")


@retry()
def load_dim_department(requests_df: pd.DataFrame) -> None:
    """Upsert department names from the department_name column."""
    logger.info("Loading dim_department...")

    depts = (
        requests_df["department_name"]
        .dropna()
        .drop_duplicates()
        .rename("department_name")
        .to_frame()
    )

    depts.to_sql("_stage_dim_department", engine, if_exists="replace", index=False)
    with engine.begin() as conn:
        conn.execute(text("""
            INSERT INTO dim_department (department_name)
            SELECT department_name FROM _stage_dim_department
            ON CONFLICT (department_name) DO NOTHING
        """))
        conn.execute(text("DROP TABLE IF EXISTS _stage_dim_department"))

    logger.info(f"dim_department loaded: {len(depts)} departments.")


# ─── FACT LOADER ──────────────────────────────────────────────────────────────

def _build_lookup(table: str, key_col: str, name_col: str) -> dict:
    """Return {name: surrogate_key} for a dimension table."""
    with engine.connect() as conn:
        rows = conn.execute(text(f"SELECT {name_col}, {key_col} FROM {table}")).fetchall()
    return {row[0]: row[1] for row in rows}


@retry()
def load_fact_ticket_events(requests_df: pd.DataFrame, sla_df: pd.DataFrame) -> None:
    """
    Build and insert one fact row per ticket.

    Enriches each row with:
      - resolution_hours & response_hours   (from raw timestamps)
      - is_sla_breached                     (resolution_hours vs SLA threshold)
      - is_resolved / is_first_contact_resolved flags
      - Surrogate FK lookups for all dimensions
    """
    logger.info("Loading fact_ticket_events...")

    if requests_df.empty:
        logger.warning("requests_df is empty — skipping fact load.")
        return

    df = requests_df.copy()
    df["created_at"]  = pd.to_datetime(df["created_at"])
    df["resolved_at"] = pd.to_datetime(df["resolved_at"])

    # ── Measures ──────────────────────────────────────────────────────────────
    df["is_resolved"] = df["resolved_at"].notna()

    df["resolution_hours"] = (
        (df["resolved_at"] - df["created_at"]).dt.total_seconds() / 3600
    ).where(df["is_resolved"])

    if "first_response_at" in df.columns:
        df["first_response_at"] = pd.to_datetime(df["first_response_at"])
        df["response_hours"] = (
            (df["first_response_at"] - df["created_at"]).dt.total_seconds() / 3600
        )
    else:
        df["response_hours"] = None

    if "first_contact_resolved" in df.columns:
        df["is_first_contact_resolved"] = df["first_contact_resolved"].fillna(False)
    else:
        df["is_first_contact_resolved"] = False

    # ── SLA breach flag ───────────────────────────────────────────────────────
    if not sla_df.empty:
        sla_cols = sla_df[["category", "priority", "resolution_time_hours"]].copy()
        df = df.merge(sla_cols, on=["category", "priority"], how="left")
        df["is_sla_breached"] = (
            df["is_resolved"] & (df["resolution_hours"] > df["resolution_time_hours"])
        ).fillna(False)
    else:
        df["is_sla_breached"] = False

    # ── Surrogate key lookups ─────────────────────────────────────────────────
    cat_map   = _build_lookup("dim_category",   "category_key",   "category_name")
    pri_map   = _build_lookup("dim_priority",   "priority_key",   "priority_name")
    agent_map = _build_lookup("dim_agent",      "agent_key",      "user_id")
    dept_map  = _build_lookup("dim_department", "department_key", "department_name")

    df["date_key"]       = df["created_at"].dt.strftime("%Y%m%d").astype(int)
    df["category_key"]   = df["category"].map(cat_map)
    df["priority_key"]   = df["priority"].map(pri_map)
    df["agent_key"]      = df["assignee_id"].map(agent_map)
    df["department_key"] = df["department_name"].map(dept_map)

    # ── Drop rows missing required FKs ────────────────────────────────────────
    required = ["date_key", "category_key", "priority_key"]
    before = len(df)
    df = df.dropna(subset=required)
    skipped = before - len(df)
    if skipped:
        logger.warning(f"Skipped {skipped} rows with unresolvable FK keys.")

    # ── Select only fact columns and insert ───────────────────────────────────
    fact_cols = [
        "date_key", "category_key", "priority_key", "agent_key", "department_key",
        "resolution_hours", "response_hours",
        "is_resolved", "is_sla_breached", "is_first_contact_resolved",
    ]
    fact_df = df[fact_cols].copy()
    fact_df[["category_key", "priority_key"]] = fact_df[
        ["category_key", "priority_key"]
    ].astype(int)

    fact_df.to_sql("fact_ticket_events", engine, if_exists="append", index=False)
    logger.info(f"fact_ticket_events loaded: {len(fact_df)} rows.")


# ─── PIPELINE ORCHESTRATOR ────────────────────────────────────────────────────

def run_load_pipeline(
    requests_df: pd.DataFrame,
    sla_df: pd.DataFrame,
    date_range_start: date | None = None,
    date_range_end: date | None = None,
) -> None:
    """
    Full load pipeline. Call this after your Extract + Transform steps.

    Args:
        requests_df:      Raw tickets DataFrame from your Extract step.
        sla_df:           SLA reference DataFrame (category, priority, resolution_time_hours).
        date_range_start: First date for dim_date (defaults to min created_at).
        date_range_end:   Last date for dim_date (defaults to today).
    """
    start = date_range_start or pd.to_datetime(requests_df["created_at"]).min().date()
    end   = date_range_end   or date.today()

    # Dimensions first — fact table references all of them
    load_dim_date(start, end)
    load_dim_category(requests_df, sla_df)
    load_dim_priority(requests_df, sla_df)
    load_dim_agent(requests_df)
    load_dim_department(requests_df)

    # Fact last
    load_fact_ticket_events(requests_df, sla_df)

    logger.info("✓ Load pipeline complete.")