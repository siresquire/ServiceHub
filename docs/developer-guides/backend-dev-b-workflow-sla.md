# Developer Guide — Backend Dev B: Workflow & SLA

> **Assignee**: Fiifi Akyer Yawson (fiifi.yawson@amalitech.com)
> **Branch**: `feature/workflow-sla`

## Your Responsibilities

You own the status workflow engine and SLA tracking — what makes ServiceHub a real ticketing system.

| Deliverable | Status |
|-------------|--------|
| Status Workflow (OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED) | TODO |
| SLA Engine (define & track SLA per category + priority) | TODO |
| Response Time Tracking (creation → first response) | TODO |
| Resolution Time Tracking (creation → resolution) | TODO |
| SLA Breach Detection (flag requests exceeding thresholds) | TODO |

## Files You Own

```
backend/src/main/java/com/servicehub/
├── service/WorkflowService.java       ← NEW: Status transition logic
├── service/SlaService.java            ← NEW: SLA tracking & breach detection
├── model/SlaPolicy.java               ← Existing entity (response_hours, resolution_hours)
├── model/enums/RequestStatus.java     ← OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
├── repository/SlaPolicyRepository.java ← Data access for SLA policies
└── dto/StatusUpdateRequest.java       ← DTO for status changes
```

## Getting Started

```bash
git checkout -b feature/workflow-sla develop
docker-compose up --build

# Review the existing SLA policy seed data
cat backend/src/main/resources/data.sql
```

## Key Implementation Notes

### Status Transition Rules
Only these transitions are valid:
```
OPEN       → ASSIGNED       (auto or manual)
ASSIGNED   → IN_PROGRESS    (agent picks up the ticket)
IN_PROGRESS → RESOLVED      (agent resolves the issue)
RESOLVED   → CLOSED         (requester confirms resolution)
```
Invalid transitions (e.g., OPEN → RESOLVED) should throw an `IllegalStateException`.

### SLA Policy Data (from data.sql)

| Category | Priority | Response Time | Resolution Time |
|----------|----------|--------------|-----------------|
| IT_SUPPORT | HIGH | 1 hour | 4 hours |
| IT_SUPPORT | MEDIUM | 4 hours | 24 hours |
| IT_SUPPORT | LOW | 8 hours | 48 hours |
| HR_REQUEST | HIGH | 2 hours | 8 hours |
| HR_REQUEST | MEDIUM | 8 hours | 48 hours |
| FACILITIES | HIGH | 1 hour | 8 hours |
| FACILITIES | MEDIUM | 4 hours | 24 hours |

### SLA Tracking Fields
Add these fields to the `ServiceRequest` entity (coordinate with Dev A):
```java
private LocalDateTime firstResponseAt;  // When status first moved from OPEN
private LocalDateTime resolvedAt;       // When status moved to RESOLVED
private boolean slaBreached;            // True if resolution exceeds SLA
```

### SLA Breach Detection Logic
```
response_time = firstResponseAt - createdAt
resolution_time = resolvedAt - createdAt

SLA is breached if:
  response_time > slaPolicy.responseHours  OR
  resolution_time > slaPolicy.resolutionHours
```

## Coordination Points

- **With Dev A (Ange)**: The `ServiceRequest` entity needs `firstResponseAt`, `resolvedAt`, and `slaBreached` fields. Coordinate the entity changes.
- **With Dev C (Alphonse)**: Dashboard APIs will need methods to query SLA compliance rates. Expose these via your `SlaService`.
- **With Data (Richard)**: The ETL pipeline reads `resolved_at` and `created_at` from the database. Ensure your timestamps are stored correctly.
