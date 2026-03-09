# ServiceHub — Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         ServiceHub Platform                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Presentation Layer                         │   │
│  │  Thymeleaf Templates + Bootstrap (Server-Side Rendering)     │   │
│  │  - Login / Register                                          │   │
│  │  - Request submission & list views                           │   │
│  │  - Dashboard with charts & metrics                           │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────────────────────┐   │
│  │                    API Layer (REST)                           │   │
│  │  Spring Boot 3.2 Controllers                                 │   │
│  │  - AuthController       (JWT login/register)                 │   │
│  │  - ServiceRequestCtrl   (CRUD + search)                      │   │
│  │  - DepartmentController (dept management)                    │   │
│  │  - DashboardController  (metrics & stats)                    │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────────────────────┐   │
│  │                   Service Layer                              │   │
│  │  - AuthService           (user auth, JWT generation)         │   │
│  │  - ServiceRequestService (CRUD, auto-routing, validation)    │   │
│  │  - WorkflowService       (status transitions)          TODO  │   │
│  │  - SlaService            (SLA tracking & breach)       TODO  │   │
│  │  - AssignmentService     (auto-assign to department)   TODO  │   │
│  │  - DashboardService      (aggregate statistics)        TODO  │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────────────────────┐   │
│  │                 Data Access Layer (JPA)                       │   │
│  │  Repositories: User, ServiceRequest, Department, SlaPolicy   │   │
│  └──────────────────────┬───────────────────────────────────────┘   │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────────────────────┐   │
│  │                  PostgreSQL 16                                │   │
│  │  Tables: users, service_requests, departments, sla_policies  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                  Data Engineering (Python)                    │   │
│  │  ETL Pipeline: Extract → Transform → Load analytics tables   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Runtime | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Security | Spring Security + JWT (jjwt) | 0.12.3 |
| ORM | Spring Data JPA / Hibernate | Managed by Spring Boot |
| Database | PostgreSQL | 16 |
| Templates | Thymeleaf + Bootstrap | Managed by Spring Boot |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| Monitoring | Spring Boot Actuator + Micrometer | Managed by Spring Boot |
| Metrics | Micrometer Prometheus Registry | Managed by Spring Boot |
| Data Pipeline | Python + Pandas + SQLAlchemy | 3.11+ |
| Containerization | Docker + Docker Compose | Latest |
| CI/CD | GitHub Actions | N/A |

## Domain Model

```
User (id, email, name, password, role, created_at)
  └── role: ADMIN | AGENT | USER

ServiceRequest (id, title, description, category, priority, status, ...)
  ├── category: IT_SUPPORT | FACILITIES | HR_REQUEST
  ├── priority: LOW | MEDIUM | HIGH | CRITICAL
  ├── status: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
  ├── requester_id → User
  └── department_id → Department

Department (id, name, description)

SlaPolicy (id, category, priority, response_hours, resolution_hours)
```

## Request Lifecycle (Status Workflow)

```
OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
  │                                  │
  └── Auto-assigns to department     └── SLA clock stops here
      based on category                  (resolution time measured)
```

## Security Model

- **JWT-based authentication**: Stateless, token in Authorization header
- **3 roles**: ADMIN (full access), AGENT (manage tickets), USER (own tickets only)
- **BCrypt** password hashing
- **CORS** configured for allowed origins

## Code Ownership Map

| Package / Module | Owner | Responsibility |
|------------------|-------|---------------|
| `controller/ServiceRequestController` | Dev A (Ange) | Request CRUD, validation |
| `service/ServiceRequestService` | Dev A (Ange) | Business logic, auto-routing |
| `service/WorkflowService` (TODO) | Dev B (Fiifi) | Status transitions |
| `service/SlaService` (TODO) | Dev B (Fiifi) | SLA tracking, breach detection |
| `config/`, `controller/AuthController` | Dev C (Alphonse) | JWT auth, dashboard |
| `qa/` | QA (Zakaria) | API & UI tests |
| `data-engineering/` | Data (Richard) | ETL pipeline, analytics |
| `devops/`, `.github/`, Docker | DevOps (Prince) | Infrastructure, CI/CD |
