# Developer Guide — Backend Dev A: Request Management

> **Assignee**: Ange Buhendwa (ange.buhendwa@amalitech.com)
> **Branch**: `feature/request-management`

## Your Responsibilities

You own the core service request functionality — the heart of ServiceHub.

| Deliverable | Status |
|-------------|--------|
| Request CRUD API (create, read, update, delete) | TODO |
| Category System (IT_SUPPORT, FACILITIES, HR_REQUEST with auto-routing) | TODO |
| Priority Management (LOW, MEDIUM, HIGH, CRITICAL) | TODO |
| Request Validation (input validation, business rules) | TODO |
| Thymeleaf Views (request submission form, request list page) | TODO |

## Files You Own

```
backend/src/main/java/com/servicehub/
├── controller/ServiceRequestController.java   ← REST + Thymeleaf endpoints
├── service/ServiceRequestService.java         ← Business logic
├── dto/ServiceRequestDto.java                 ← Create/update request DTO
├── dto/ServiceRequestResponse.java            ← Response DTO
├── model/ServiceRequest.java                  ← JPA entity
├── model/enums/RequestCategory.java           ← IT_SUPPORT, FACILITIES, HR_REQUEST
├── model/enums/Priority.java                  ← LOW, MEDIUM, HIGH, CRITICAL
├── model/enums/RequestStatus.java             ← OPEN, ASSIGNED, etc.
└── repository/ServiceRequestRepository.java   ← Data access
```

## Getting Started

```bash
# 1. Set up your dev environment (see docs/getting-started.md)
git checkout -b feature/request-management develop

# 2. Start the application
docker-compose up --build

# 3. Look for TODO comments in your files
grep -r "TODO" backend/src/main/java/com/servicehub/controller/ServiceRequestController.java
grep -r "TODO" backend/src/main/java/com/servicehub/service/ServiceRequestService.java
```

## Key Implementation Notes

### Auto-Routing Logic
When a request is created, automatically assign it to the correct department:
- `IT_SUPPORT` → IT Support department (id=1)
- `HR_REQUEST` → HR department (id=2)
- `FACILITIES` → Facilities department (id=3)

### Category Enum
Already defined in `model/enums/RequestCategory.java`:
```java
public enum RequestCategory {
    IT_SUPPORT, FACILITIES, HR_REQUEST
}
```

### Validation Rules
- Title: required, 5-200 characters
- Description: required, 10-2000 characters
- Category: required, must be a valid enum value
- Priority: required, must be a valid enum value

### API Endpoints to Implement
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/requests` | Create a new service request |
| GET | `/api/requests` | List all requests (with pagination) |
| GET | `/api/requests/{id}` | Get a single request |
| PUT | `/api/requests/{id}` | Update a request |
| DELETE | `/api/requests/{id}` | Delete a request (admin only) |

### Thymeleaf Views to Create
- `templates/requests/create.html` — Request submission form
- `templates/requests/list.html` — Request list with filtering

## Coordination Points

- **With Dev B (Fiifi)**: Your request creation should set initial status to `OPEN`. Dev B's WorkflowService handles all subsequent transitions.
- **With Dev C (Alphonse)**: The authenticated user (from JWT) should be automatically set as the requester. Dev C's AuthService provides the current user.
- **With QA (Zakaria)**: Share your API contracts so Zakaria can write REST Assured tests against your endpoints.
