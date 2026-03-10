import pandas as pd
from Scripts.Utils import logger

def transform_sla_metrics(requests_df):
    logger.info("Transforming SLA metrics...")

    if requests_df.empty:
        return pd.DataFrame()

    df = requests_df.copy()

    df["created_at"] = pd.to_datetime(df["created_at"])
    df["resolved_at"] = pd.to_datetime(df["resolved_at"])

    resolved = df[df["resolved_at"].notna()].copy()

    if resolved.empty:
        return pd.DataFrame()

    resolved["resolution_hours"] = (
        resolved["resolved_at"] - resolved["created_at"]
    ).dt.total_seconds() / 3600

    summary = resolved.groupby(["category", "priority"]).agg(
        total_resolved=("id", "count"),
        avg_resolution_hours=("resolution_hours", "mean"),
        max_resolution_hours=("resolution_hours", "max")
    ).reset_index()

    return summary

def transform_daily_volume(requests_df):
    logger.info("Transforming daily volume...")

    if requests_df.empty:
        return pd.DataFrame()

    df = requests_df.copy()
    df["date"] = pd.to_datetime(df["created_at"]).dt.date

    return df.groupby(["date", "category"]).size().reset_index(name="request_count")

def transform_agent_performance(requests_df):
    logger.info("Transforming agent performance...")

    if requests_df.empty:
        return pd.DataFrame()

    df = requests_df.copy()

    df["created_at"] = pd.to_datetime(df["created_at"])
    df["resolved_at"] = pd.to_datetime(df["resolved_at"])

    resolved = df[df["resolved_at"].notna()].copy()

    if resolved.empty:
        return pd.DataFrame()

    resolved["resolution_hours"] = (
        resolved["resolved_at"] - resolved["created_at"]
    ).dt.total_seconds() / 3600

    performance = resolved.groupby("assignee_id").agg(
        total_resolved=("id", "count"),
        avg_resolution_hours=("resolution_hours", "mean"),
        min_resolution_hours=("resolution_hours", "min"),
        max_resolution_hours=("resolution_hours", "max"),
    ).reset_index()

    return performance

def transform_sla_breaches(requests_df, sla_df):
    """Detect requests that exceeded SLA resolution time."""

    if requests_df.empty or sla_df.empty:
        return pd.DataFrame()

    requests_df["created_at"] = pd.to_datetime(requests_df["created_at"])
    requests_df["resolved_at"] = pd.to_datetime(requests_df["resolved_at"])

    merged = requests_df.merge(sla_df, on=["category", "priority"], how="left")

    merged["resolution_hours"] = (
        merged["resolved_at"] - merged["created_at"]
    ).dt.total_seconds() / 3600

    breaches = merged[merged["resolution_hours"] > merged["resolution_time_hours"]]

    result = breaches.groupby(["category", "priority"]).agg(
        total_breaches=("id", "count"),
        avg_breach_hours=("resolution_hours", "mean"),
        max_breach_hours=("resolution_hours", "max"),
    ).reset_index()

    return result