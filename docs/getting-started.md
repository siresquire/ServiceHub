# Getting Started with ServiceHub

Welcome to the ServiceHub project! This guide will get you from zero to a running application in under 5 minutes.

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Latest | Runs all services in containers |
| [Git](https://git-scm.com/) | Latest | Version control |
| [JDK 17](https://adoptium.net/) | 17 | Local IDE development (optional — Docker handles this) |
| [Python 3.11+](https://www.python.org/) | 3.11+ | Pre-commit hooks (optional) |

## Step 1: Clone the Repository

```bash
git clone <repo-url>
cd 4-ServiceHub
```

## Step 2: Create Your Environment File

```bash
# Copy the template — edit .env if you need custom values
cp .env.example .env
```

## Step 3: Start All Services

```bash
docker-compose up --build
```

This starts 3 containers:
- **servicehub-db** — PostgreSQL database (port 5432)
- **servicehub-backend** — Spring Boot application (port 8080)
- **servicehub-etl** — Python data pipeline (runs once, then exits)

## Step 4: Verify Everything Works

| Check | URL | Expected |
|-------|-----|----------|
| Application UI | http://localhost:8080 | Login page |
| Swagger API Docs | http://localhost:8080/swagger-ui.html | API documentation |
| Health Check | http://localhost:8080/actuator/health | `{"status":"UP"}` |

## Step 5: Log In

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@amalitech.com | password123 |
| Agent | agent@amalitech.com | password123 |
| User | user@amalitech.com | password123 |

## Step 6: Set Up Pre-commit Hooks (Recommended)

```bash
pip install pre-commit
pre-commit install
pre-commit install --hook-type commit-msg
```

This ensures your commits follow the team's conventions before they leave your machine.

## Step 7: Create Your Feature Branch

```bash
# Replace with your assigned branch name
git checkout -b feature/your-feature-name develop
```

See [branching-strategy.md](branching-strategy.md) for the full Git workflow.

## Common Commands

```bash
docker-compose up --build -d    # Start in background
docker-compose logs -f backend  # Watch backend logs
docker-compose down             # Stop all services
docker-compose down -v          # Stop + delete database data
docker exec -it servicehub-db psql -U servicehub -d servicehub  # DB shell
```

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Port 5432 already in use | Stop local PostgreSQL: `sudo service postgresql stop` |
| Port 8080 already in use | Change `SERVER_PORT` in `.env` |
| Backend can't connect to DB | Ensure postgres container is healthy: `docker-compose ps` |
| Maven build fails in Docker | Clear Docker cache: `docker-compose build --no-cache` |
| Tests fail locally | Ensure `SPRING_PROFILES_ACTIVE=test` is set |

## What's Next?

1. Read your [role-specific guide](developer-guides/) for your assigned deliverables
2. Review the [architecture overview](architecture.md)
3. Check the [branching strategy](branching-strategy.md)
4. Start working on your feature branch!
