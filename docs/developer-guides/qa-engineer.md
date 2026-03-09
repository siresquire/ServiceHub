# Developer Guide — QA Engineer

> **Assignee**: Zakaria Osman (zakaria.osman@amalitech.com)
> **Branch**: `feature/qa-tests`

## Your Responsibilities

| Deliverable | Status |
|-------------|--------|
| Test Plan (comprehensive, covering all MVP features) | TODO |
| Workflow Tests (all status transitions + edge cases) | TODO |
| API Tests (REST Assured for all endpoints) | TODO |
| UI Tests (Selenium for Thymeleaf pages) | TODO |
| SLA Tests (verify SLA calculation + breach detection) | TODO |

## Files You Own

```
qa/
├── api-tests/
│   ├── pom.xml                                        ← Maven config for REST Assured
│   └── src/test/java/com/amalitech/qa/
│       └── ServiceRequestApiTest.java                 ← API test suite
└── ui-tests/
    ├── pom.xml                                        ← Maven config for Selenium
    └── src/test/java/com/amalitech/qa/
        └── DashboardPageTest.java                     ← UI test suite
```

## Getting Started

```bash
git checkout -b feature/qa-tests develop
docker-compose up --build

# Verify the backend is running
curl http://localhost:8080/actuator/health

# Run existing API tests
cd qa/api-tests && mvn test
```

## Key Implementation Notes

### Authentication in API Tests
All protected endpoints require a JWT token. Get one first:
```java
// REST Assured example — Login and get JWT
String token = given()
    .contentType("application/json")
    .body("{\"email\":\"admin@amalitech.com\",\"password\":\"password123\"}")
.when()
    .post("/api/auth/login")
.then()
    .statusCode(200)
    .extract().path("token");

// Use the token in subsequent requests
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/api/requests")
.then()
    .statusCode(200);
```

### Test Categories to Cover

**1. Auth Tests**
- Login with valid/invalid credentials
- Register new user
- Access protected endpoint without token (401)
- Access admin endpoint as USER (403)

**2. Request CRUD Tests**
- Create request with valid data
- Create request with missing fields (400)
- List requests (paginated)
- Update request
- Delete request (admin only)

**3. Workflow Tests**
- Valid transitions: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
- Invalid transitions (e.g., OPEN → CLOSED) should return 400
- Only agents/admins can change status

**4. SLA Tests**
- Verify SLA policy is applied based on category + priority
- Verify breach flag is set when resolution time exceeds SLA

### Selenium UI Test Setup
```java
// Selenium example — Login page test
WebDriver driver = new ChromeDriver();
driver.get("http://localhost:8080");
driver.findElement(By.id("email")).sendKeys("admin@amalitech.com");
driver.findElement(By.id("password")).sendKeys("password123");
driver.findElement(By.id("login-btn")).click();
assertEquals("Dashboard", driver.getTitle());
```

## Coordination Points

- **With all Backend Devs**: Request their API contracts (endpoints, request/response formats) to write accurate tests.
- **With DevOps (Prince)**: Tests run in CI automatically. If tests need special setup (e.g., browser for Selenium), coordinate with Prince.
