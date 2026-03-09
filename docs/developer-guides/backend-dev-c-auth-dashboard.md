# Developer Guide — Backend Dev C: Auth & Dashboard

> **Assignee**: Shema Alphonse (alphonse.shema@amalitech.com)
> **Branch**: `feature/auth-dashboard`

## Your Responsibilities

You own authentication, authorization, and the dashboard — the control center of ServiceHub.

| Deliverable | Status |
|-------------|--------|
| Auth System (JWT login/register with 3 roles: ADMIN, AGENT, USER) | Partial |
| Dashboard APIs (request volumes, SLA compliance, resolution metrics) | TODO |
| Reporting Endpoints (aggregate stats per category, priority, agent) | TODO |
| Thymeleaf Dashboard (charts and metrics views) | TODO |
| Role-based Access (different views per role) | TODO |

## Files You Own

```
backend/src/main/java/com/servicehub/
├── config/SecurityConfig.java         ← Spring Security configuration
├── config/JwtAuthFilter.java          ← JWT token validation filter
├── config/JwtService.java             ← JWT token generation/validation
├── config/CorsConfig.java             ← CORS configuration
├── controller/AuthController.java     ← Login/register endpoints
├── service/AuthService.java           ← Authentication business logic
├── service/DashboardService.java      ← NEW: Aggregate statistics
├── controller/DashboardController.java ← NEW: Dashboard endpoint
├── dto/AuthRequest.java               ← Login DTO
├── dto/AuthResponse.java              ← JWT token response DTO
├── dto/RegisterRequest.java           ← Registration DTO
├── dto/DashboardStatsResponse.java    ← Dashboard DTO
└── model/User.java                    ← User JPA entity
```

## Getting Started

```bash
git checkout -b feature/auth-dashboard develop
docker-compose up --build

# Test the existing auth endpoints
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@amalitech.com","password":"password123"}'
```

## Key Implementation Notes

### Role-Based Access Matrix
| Endpoint | ADMIN | AGENT | USER |
|----------|-------|-------|------|
| View all requests | ✅ | ✅ | ❌ (own only) |
| Update any request status | ✅ | ✅ | ❌ |
| Delete requests | ✅ | ❌ | ❌ |
| View dashboard | ✅ | ✅ | ❌ |
| Manage users | ✅ | ❌ | ❌ |

### Dashboard API Endpoints to Implement
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard/stats` | Overall statistics (total, open, resolved, etc.) |
| GET | `/api/dashboard/sla-compliance` | SLA compliance rates by category |
| GET | `/api/dashboard/volume-by-category` | Request count per category |
| GET | `/api/dashboard/resolution-times` | Average resolution times |

### Dashboard Metrics to Calculate
```java
// Example DashboardStatsResponse fields:
long totalRequests;
long openRequests;
long resolvedRequests;
long slaBreachedRequests;
double overallSlaComplianceRate;
Map<RequestCategory, Long> requestsByCategory;
Map<Priority, Long> requestsByPriority;
double avgResolutionTimeHours;
```

### Default Users (from data.sql)
| Email | Password | Role |
|-------|----------|------|
| admin@amalitech.com | password123 | ADMIN |
| agent@amalitech.com | password123 | AGENT |
| user@amalitech.com | password123 | USER |

## Coordination Points

- **With Dev A (Ange)**: Your SecurityConfig must allow access to request endpoints. Coordinate which paths need which roles.
- **With Dev B (Fiifi)**: Dashboard SLA metrics come from Fiifi's `SlaService`. Call his methods for compliance rates.
- **With QA (Zakaria)**: Share the JWT token format so Zakaria can authenticate in API tests.
