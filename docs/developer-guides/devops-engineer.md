# Developer Guide — DevOps Engineer

> **Assignee**: Prince Tetteh Ayiku (prince.ayiku@amalitech.com)
> **Branch**: `feature/devops-infra`

## Your Responsibilities

| Deliverable | Status |
|-------------|--------|
| Docker Setup (Dockerfiles for backend + database) | ✅ Done |
| Docker Compose (multi-container with proper networking) | ✅ Done |
| CI/CD Pipeline (GitHub Actions: build, test, deploy) | ✅ Done |
| Environment Config (dev, test, prod profiles) | ✅ Done |
| Deployment Docs (setup + deployment documentation) | ✅ Done |

## Files You Own

```
.github/
├── workflows/
│   ├── ci.yml                          ← CI pipeline
│   └── deploy.yml                      ← CD pipeline
├── CODEOWNERS                          ← Auto-reviewer assignment
└── pull_request_template.md            ← PR checklist

.env.example                            ← Environment variable template
.gitignore                              ← Git exclusion rules
.pre-commit-config.yaml                 ← Local code quality hooks
Makefile                                ← Developer convenience commands
docker-compose.yml                      ← Main service orchestration
docker-compose.observability.yml        ← Monitoring stack (Prometheus + Grafana)

backend/
├── Dockerfile                          ← Multi-stage Java 17 build
└── .dockerignore                       ← Docker context exclusions

backend/src/main/resources/
├── application.yml                     ← Base config (env-var driven)
├── application-dev.yml                 ← Local dev overrides
├── application-test.yml                ← CI/CD test overrides
├── application-prod.yml                ← Production config
└── logback-spring.xml                  ← Structured JSON logging

devops/
├── scripts/
│   └── deploy.sh                       ← Deployment script
├── prometheus/
│   └── prometheus.yml                  ← Prometheus scrape config
└── grafana/
    └── provisioning/
        └── datasources.yml             ← Grafana auto-provisioning

docs/
├── getting-started.md
├── architecture.md
├── branching-strategy.md
└── developer-guides/                   ← Role-specific guides (6 files)
```

## Operational Runbook

### Starting the Full Stack
```bash
cp .env.example .env
docker-compose up --build -d
```

### Checking Service Health
```bash
# All containers
docker-compose ps

# Backend health endpoint
curl http://localhost:8080/actuator/health

# Database connectivity
docker exec -it servicehub-db pg_isready -U servicehub
```

### Viewing Logs
```bash
docker-compose logs -f backend    # Backend only
docker-compose logs -f            # All services
```

### Full Reset (clean slate)
```bash
docker-compose down -v --remove-orphans
docker-compose up --build -d
```

## GitHub Repository Setup Checklist

After creating the team repository:
- [ ] Push this codebase to the repo
- [ ] Enable branch protection on `main` (require PR, 1 approval, CI pass)
- [ ] Update `CODEOWNERS` with actual GitHub usernames
- [ ] Set up GitHub Projects Kanban board
- [ ] Add repository secrets (JWT_SECRET for production)
- [ ] Verify CI pipeline runs on a test PR
