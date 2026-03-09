# Developer Guide — Data Engineer

> **Assignee**: Richard Anane Sarfo (richard.sarfo@amalitech.com)
> **Branch**: `feature/data-pipeline`

## Your Responsibilities

| Deliverable | Status |
|-------------|--------|
| Analytics Pipeline (extract ticket data, compute SLA metrics) | Partial |
| Sample Data (generate realistic tickets across categories) | TODO |
| Dashboard Data (prepared datasets for visualizations) | TODO |
| Trend Analysis (weekly/monthly resolution time trends) | TODO |
| Data Documentation (schema and metrics documentation) | TODO |

## Files You Own

```
data-engineering/
├── Dockerfile              ← Python 3.11 container
├── config.py               ← Database connection config
├── etl_pipeline.py         ← Main ETL pipeline (partially implemented)
└── requirements.txt        ← Python dependencies
```

## Getting Started

```bash
git checkout -b feature/data-pipeline develop
docker-compose up --build

# Run ETL manually
docker-compose run --rm data-engineering

# Or connect locally (requires Python 3.11)
cd data-engineering
pip install -r requirements.txt
python etl_pipeline.py
```

## Key Implementation Notes

### Database Connection
Your `config.py` already reads from environment variables. In Docker Compose, these are set automatically:
- `DB_HOST=postgres` (Docker DNS name)
- `DB_PORT=5432`
- `DB_NAME=servicehub`
- `DB_USER=servicehub`
- `DB_PASSWORD=servicehub_pass`

### Existing Code (etl_pipeline.py)
The starter code has:
- ✅ `extract_requests()` — Pulls service requests with requester and department info
- ✅ `extract_sla_policies()` — Pulls SLA policy definitions
- ✅ `transform_sla_metrics()` — Calculates resolution hours by category/priority
- ✅ `transform_daily_volume()` — Daily request counts by category
- ❌ SLA breach detection (TODO)
- ❌ Agent performance metrics (TODO)
- ❌ Department workload analysis (TODO)

### Sample Data Script
Create a script to generate realistic test data:
```python
# generate_sample_data.py
import random
from datetime import datetime, timedelta

categories = ['IT_SUPPORT', 'FACILITIES', 'HR_REQUEST']
priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
statuses = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
```

### Analytics Tables to Create
| Table | Purpose |
|-------|---------|
| `analytics_sla_metrics` | Aggregated SLA compliance by category/priority |
| `analytics_daily_volume` | Daily request counts by category |
| `analytics_agent_performance` | Resolution times per agent (NEW) |
| `analytics_department_workload` | Open tickets per department (NEW) |
| `analytics_weekly_trends` | Weekly rolling averages (NEW) |

## Coordination Points

- **With Dev B (Fiifi)**: Ensure `resolved_at` and `first_response_at` timestamps are populated correctly in the database.
- **With Dev C (Alphonse)**: Dashboard APIs may query your analytics tables directly. Coordinate table schemas.
- **With DevOps (Prince)**: The ETL container runs as a one-shot job. If you need scheduled runs, coordinate with Prince.
