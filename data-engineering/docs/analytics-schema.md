## ServiceHub Analytics Schema

**Scope**: Star-schema layer for helpdesk analytics, built on top of the transactional ServiceHub database.

**Purpose**:  
Provide a stable, query-friendly model for dashboards (e.g. Grafana, Streamlit) and ad‑hoc analysis of ticket volumes, SLAs, agent performance, and department workloads.

**Grain**:  
Central fact table `fact_ticket_events` at **one row per resolved ticket event**, joined to conformed dimensions.

---

## Source System Overview

Core transactional tables (see seed in `schema/sample_data.sql`):

- **`departments`**
  - Master data for departments (e.g. `IT Support`, `HR`, `Facilities`).
- **`sla_policies`**
  - SLA expectations by (`category`, `priority`): response and resolution hours.
- **`users`**
  - All users and agents; roles: `ADMIN`, `AGENT`, `USER`.
- **`service_requests`**
  - Ticket lifecycle entity; contains category, priority, department, timestamps, SLA flags, etc.

The analytics schema (`schema/analytics_schema.sql`) is populated via ETL from these transactional tables.

---

## Dimensional Model

### 1. `dim_date`

**Purpose**: Canonical calendar dimension for all time-based slicing, rollups, and comparative analysis.

**Grain**: One row per calendar date.

**Columns**:

- **`date_key` (INT, PK)**  
  Surrogate key in `YYYYMMDD` format (e.g. `20260311`).  
  Used as the main join key from fact tables.

- **`full_date` (DATE, UNIQUE)**  
  Actual calendar date.

- **`day_of_week` (SMALLINT)**  
  1–7 (ISO), where 1 = Monday, 7 = Sunday.

- **`day_name` (VARCHAR(9))**  
  Name of the day (`Monday`…`Sunday`).

- **`week_start_date` (DATE)**  
  ISO week Monday for the given date.

- **`week_number` (SMALLINT)**  
  ISO week number (1–53).

- **`month` (SMALLINT)**  
  Month number (1–12).

- **`month_name` (VARCHAR(9))**  
  Month name (`January`…`December`).

- **`quarter` (SMALLINT)**  
  Calendar quarter (1–4).

- **`year` (SMALLINT)**  
  Calendar year (e.g. `2026`).

- **`is_weekend` (BOOLEAN)**  
  `TRUE` if Saturday or Sunday, otherwise `FALSE`.

**Typical usage**:

- Tickets resolved per day/week/month/year.
- SLA breach rate trends over time.
- Comparing performance across periods (e.g. this week vs last week).

---

### 2. `dim_category`

**Purpose**: Normalized ticket category dimension, replacing inline category enums in tickets.

**Grain**: One row per logical ticket category.

**Columns**:

- **`category_key` (SERIAL, PK)**  
  Surrogate key.

- **`category_name` (VARCHAR(100), UNIQUE)**  
  Logical category identifier; aligns with `service_requests.category`.  
  Examples: `IT_SUPPORT`, `HR_REQUEST`, `FACILITIES`.

- **`description` (TEXT, nullable)**  
  Human-readable explanation of the category.

**Typical usage**:

- Slice metrics by category:
  - Ticket volume per category.
  - Average resolution / response times per category.
  - SLA breach rates per category.

---

### 3. `dim_priority`

**Purpose**: Ticket priority dimension with expected SLA thresholds by priority.

**Grain**: One row per priority level.

**Columns**:

- **`priority_key` (SERIAL, PK)**  
  Surrogate key.

- **`priority_name` (VARCHAR(100), UNIQUE)**  
  Priority identifier; aligns with `service_requests.priority`.  
  Examples: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`.

- **`sla_hours` (NUMERIC(6,2), nullable)**  
  Target resolution SLA threshold for this priority (hours).  
  Can be combined with category‑level SLA policies as needed.

- **`description` (TEXT, nullable)**  
  Human-readable description of what this priority means.

**Typical usage**:

- SLA performance by priority:
  - Average resolution time vs `sla_hours`.
  - Number and % of SLA breaches by priority.
  - Backlog of tickets by priority.

---

### 4. `dim_agent`

**Purpose**: Support agent dimension sourced from `users` where `role = 'AGENT'`.

**Grain**: One row per support agent.

**Columns**:

- **`agent_key` (SERIAL, PK)**  
  Surrogate key.

- **`user_id` (INT, UNIQUE, FK → `users.id`)**  
  Reference to the transactional user record.  
  Cascades on delete.

- **`display_name` (VARCHAR(200), nullable)**  
  Agent display name.

- **`department` (VARCHAR(100), nullable)**  
  Agent’s primary department or assignment (e.g. `IT Support`).

- **`is_active` (BOOLEAN, default `TRUE`)**  
  Whether the agent is currently active.

**Typical usage**:

- Agent-level metrics:
  - Tickets handled per agent.
  - Average resolution / response time per agent.
  - SLA breach rate per agent.
  - First-contact resolution performance per agent.

---

### 5. `dim_department`

**Purpose**: Department dimension used for cross‑department analytics.

**Grain**: One row per department.

**Columns**:

- **`department_key` (SERIAL, PK)**  
  Surrogate key.

- **`department_name` (VARCHAR(100), UNIQUE)**  
  Department name, e.g. `IT Support`, `HR`, `Facilities`.

**Typical usage**:

- Ticket volume, SLA performance, and agent metrics **by department**.

---

## Fact Table

### `fact_ticket_events`

**Purpose**: Central fact table for ticket lifecycle analytics; optimized for SLA and operational reporting.

**Grain**: **One row per resolved ticket event**.  
Typically derived from `service_requests` and related tables during ETL.

**Keys**:

- **`event_id` (BIGSERIAL, PK)**  
  Unique identifier for each fact row.

- **`date_key` (INT, NOT NULL, FK → `dim_date.date_key`)**  
  Analytic date for this event.  
  Common conventions:
  - Resolution date if the focus is SLA/resolution analytics.
  - Created date if focusing on ticket intake volume.

- **`category_key` (INT, NOT NULL, FK → `dim_category.category_key`)**

- **`priority_key` (INT, NOT NULL, FK → `dim_priority.priority_key`)**

- **`agent_key` (INT, nullable, FK → `dim_agent.agent_key`)**  
  Null when no agent is assigned yet or when mapping is not available.

- **`department_key` (INT, nullable, FK → `dim_department.department_key`)**

**Measures**:

- **`resolution_hours` (NUMERIC(10,4), nullable)**  
  Total time from ticket creation to resolution in hours.  
  - Null for tickets not yet resolved at ETL time.  
  - Expected derivation (conceptual):  
    \[
      \text{resolution\_hours} \approx \frac{\text{resolved\_at} - \text{created\_at}}{\text{1 hour}}
    \]

- **`response_hours` (NUMERIC(10,4), nullable)**  
  Time from ticket creation to first response / assignment in hours.  
  - Common derivation:  
    \[
      \text{response\_hours} \approx \frac{\text{first\_response\_timestamp} - \text{created\_at}}{\text{1 hour}}
    \]
    where `first_response_timestamp` can be `assigned_at` or a dedicated field.

**Status / flag columns**:

- **`is_resolved` (BOOLEAN, NOT NULL, default `FALSE`)**  
  `TRUE` if the ticket is resolved at the time of this fact row.

- **`is_sla_breached` (BOOLEAN, NOT NULL, default `FALSE`)**  
  `TRUE` if the SLA resolution deadline has been exceeded.  
  Derived from:
  - Transactional `service_requests.sla_breached`, or
  - Comparison of `resolved_at` (or `NOW()` for open tickets) vs `resolution_sla_deadline`, which in turn is based on `sla_policies` and ticket attributes.

- **`is_first_contact_resolved` (BOOLEAN, NOT NULL, default `FALSE`)**  
  `TRUE` when ticket was resolved in the first interaction (no reopen).  
  A common rule is:
  - `reopened_count = 0` and `resolved = TRUE`.

**Metadata**:

- **`refreshed_at` (TIMESTAMPTZ, NOT NULL, default `NOW()`)**  
  Timestamp when the fact row was (re)computed by the ETL pipeline.  
  Useful for data freshness checks and debugging incremental loads.

---

## Metric Definitions

Below are standard metrics to be computed from `fact_ticket_events` and dimensions.  
Unless otherwise stated, filters like `is_resolved = TRUE` should be applied where appropriate.

### 1. Ticket Volume

- **Tickets resolved**
  - **Definition**: Count of tickets resolved within the analysis period.
  - **SQL sketch**:
    ```sql
    SELECT
      COUNT(*) AS tickets_resolved
    FROM fact_ticket_events
    WHERE is_resolved = TRUE;
    ```

- **Tickets in SLA breach**
  - **Definition**: Count of tickets that breached resolution SLA.
  - **SQL sketch**:
    ```sql
    SELECT
      COUNT(*) AS tickets_breached
    FROM fact_ticket_events
    WHERE is_sla_breached = TRUE;
    ```

- **SLA breach rate**
  - **Definition**: Breached tickets / resolved tickets.
  - **SQL sketch**:
    ```sql
    SELECT
      CASE
        WHEN COUNT(*) FILTER (WHERE is_resolved = TRUE) = 0 THEN 0
        ELSE
          COUNT(*) FILTER (WHERE is_sla_breached = TRUE)::DECIMAL
          / COUNT(*) FILTER (WHERE is_resolved = TRUE)
      END AS sla_breach_rate
    FROM fact_ticket_events;
    ```

### 2. Time-to-Resolution & Response

- **Average resolution time (hours)**
  - **Definition**: Mean `resolution_hours` for resolved tickets.
  - **SQL sketch**:
    ```sql
    SELECT
      AVG(resolution_hours) AS avg_resolution_hours
    FROM fact_ticket_events
    WHERE is_resolved = TRUE;
    ```

- **Average response time (hours)**
  - **Definition**: Mean `response_hours` where available.
  - **SQL sketch**:
    ```sql
    SELECT
      AVG(response_hours) AS avg_response_hours
    FROM fact_ticket_events
    WHERE response_hours IS NOT NULL;
    ```

- **Percentile resolution times (e.g. P50, P95)**
  - **Definition**: Distribution of `resolution_hours` for SLOs and outlier detection.
  - **SQL sketch**:
    ```sql
    SELECT
      percentile_cont(0.5) WITHIN GROUP (ORDER BY resolution_hours) AS p50_resolution,
      percentile_cont(0.95) WITHIN GROUP (ORDER BY resolution_hours) AS p95_resolution
    FROM fact_ticket_events
    WHERE is_resolved = TRUE;
    ```

### 3. Operational Effectiveness

- **First-contact resolution rate**
  - **Definition**: Share of tickets resolved in a single interaction (no reopen).
  - **SQL sketch**:
    ```sql
    SELECT
      CASE
        WHEN COUNT(*) FILTER (WHERE is_resolved = TRUE) = 0 THEN 0
        ELSE
          COUNT(*) FILTER (WHERE is_first_contact_resolved = TRUE)::DECIMAL
          / COUNT(*) FILTER (WHERE is_resolved = TRUE)
      END AS fcr_rate
    FROM fact_ticket_events;
    ```

- **Agent workload**
  - **Definition**: Tickets handled per agent.
  - **SQL sketch**:
    ```sql
    SELECT
      a.display_name,
      d.department_name,
      COUNT(*) AS tickets_handled
    FROM fact_ticket_events f
    LEFT JOIN dim_agent a       ON f.agent_key      = a.agent_key
    LEFT JOIN dim_department d  ON f.department_key = d.department_key
    GROUP BY a.display_name, d.department_name;
    ```

- **Agent SLA performance**
  - **Definition**: SLA breach rate and resolution time by agent.
  - **SQL sketch**:
    ```sql
    SELECT
      a.display_name,
      COUNT(*) FILTER (WHERE is_resolved = TRUE) AS tickets_resolved,
      COUNT(*) FILTER (WHERE is_sla_breached = TRUE) AS tickets_breached,
      CASE
        WHEN COUNT(*) FILTER (WHERE is_resolved = TRUE) = 0 THEN 0
        ELSE
          COUNT(*) FILTER (WHERE is_sla_breached = TRUE)::DECIMAL
          / COUNT(*) FILTER (WHERE is_resolved = TRUE)
      END AS sla_breach_rate,
      AVG(resolution_hours) FILTER (WHERE is_resolved = TRUE) AS avg_resolution_hours
    FROM fact_ticket_events f
    LEFT JOIN dim_agent a ON f.agent_key = a.agent_key
    GROUP BY a.display_name;
    ```

---

## Example Slicing & Dicing

Because the schema is a star model, all metrics above can be sliced by any combination of:

- **Time**: day, week, month, quarter, year (`dim_date`)
- **Category**: IT vs HR vs Facilities (`dim_category`)
- **Priority**: Critical / High / Medium / Low (`dim_priority`)
- **Agent**: Who handled the ticket (`dim_agent`)
- **Department**: Organisational grouping (`dim_department`)

Example: **Monthly SLA breach rate by category and priority**:

```sql
SELECT
  d.year,
  d.month,
  c.category_name,
  p.priority_name,
  COUNT(*) FILTER (WHERE f.is_resolved = TRUE) AS resolved_tickets,
  COUNT(*) FILTER (WHERE f.is_sla_breached = TRUE) AS breached_tickets,
  CASE
    WHEN COUNT(*) FILTER (WHERE f.is_resolved = TRUE) = 0 THEN 0
    ELSE
      COUNT(*) FILTER (WHERE f.is_sla_breached = TRUE)::DECIMAL
      / COUNT(*) FILTER (WHERE f.is_resolved = TRUE)
  END AS sla_breach_rate
FROM fact_ticket_events f
JOIN dim_date      d ON f.date_key      = d.date_key
JOIN dim_category  c ON f.category_key  = c.category_key
JOIN dim_priority  p ON f.priority_key  = p.priority_key
GROUP BY
  d.year, d.month, c.category_name, p.priority_name
ORDER BY
  d.year, d.month, c.category_name, p.priority_name;
```

---

## Data Freshness & ETL Notes

- `fact_ticket_events.refreshed_at` indicates the load time of each row.
- Recommended:
  - Maintain an ETL job that:
    - Populates `dim_date` for a rolling window (e.g. ±2 years).
    - Syncs categories, priorities, agents, and departments from the transactional DB.
    - Computes / updates `resolution_hours`, `response_hours`, `is_sla_breached`, and `is_first_contact_resolved` from `service_requests` and `sla_policies`.
  - Monitor ETL health by checking:
    ```sql
    SELECT max(refreshed_at) FROM fact_ticket_events;
    ```

---

