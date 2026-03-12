# ServiceHub API Tests

This project contains automated API tests for the ServiceHub application using REST Assured, JUnit 5, and Allure reporting.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- ServiceHub backend application running on `http://localhost:8080`

## Project Structure

```
qa/api-tests/
├── src/test/java/com/amalitech/qa/
│   ├── base/           # Base test classes and utilities
│   ├── config/         # Test configuration classes
│   └── tests/          # Test classes organized by feature
│       ├── admin/      # Admin management tests
│       ├── auth/       # Authentication tests
│       ├── requests/   # Service request tests
│       └── sla/        # SLA policy tests
├── src/test/resources/ # Test resources and configuration
└── target/             # Build output and test results
```

## Running Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthApiTest
```

### Run Tests with Specific Profile
```bash
mvn test -Dspring.profiles.active=test
```

## Allure Reporting

This project is configured with Allure reporting to provide detailed test execution reports with request/response details.

### Generate and View Allure Reports

1. **Run tests** (this generates test results):
   ```bash
   mvn clean test
   ```

2. **Generate and serve Allure report**:
   ```bash
   mvn allure:serve
   ```
   This command will:
   - Generate the HTML report from test results
   - Start a local web server
   - Automatically open the report in your default browser

### Alternative Allure Commands

- **Generate report only** (without serving):
  ```bash
  mvn allure:report
  ```
  Report will be generated in `target/site/allure-maven-plugin/`

- **View existing report**:
  ```bash
  mvn allure:serve
  ```

### Test Results Location

- **Raw test results**: `target/allure-results/`
- **Generated HTML report**: `target/site/allure-maven-plugin/`

## Test Configuration

### Environment Variables

Set the following environment variables or system properties for authenticated tests:

- `ADMIN_TOKEN` - JWT token for admin user
- `AGENT_TOKEN` - JWT token for agent user
- `BASE_URL` - API base URL (default: http://localhost:8080)

### Example Configuration

```bash
# Set environment variables
export ADMIN_TOKEN="your-admin-jwt-token"
export AGENT_TOKEN="your-agent-jwt-token"

# Or pass as system properties
mvn test -Dadmin.token="your-admin-jwt-token" -Dagent.token="your-agent-jwt-token"
```

## Test Features

### Allure Annotations

Tests are organized using Allure annotations:

- **@Epic**: ServiceHub API Testing
- **@Feature**: Authentication API, Service Requests, SLA Policy Management, Admin Management
- **@Story**: Specific user stories (e.g., User Registration, Create Service Request)
- **@Severity**: Test importance (BLOCKER, CRITICAL, NORMAL, MINOR)
- **@Description**: Detailed test descriptions

### Automatic Request/Response Logging

All API requests and responses are automatically captured in Allure reports, including:
- Request URLs, methods, headers, and bodies
- Response status codes, headers, and bodies
- Request timing and performance metrics

### Test Categories

1. **Authentication Tests** (`AuthApiTest`)
   - User registration, login, logout
   - Token validation and security

2. **Service Request Tests** (`ServiceRequestApiTest`)
   - Request creation, assignment, status updates
   - Role-based access control

3. **SLA Policy Tests** (`SlaPolicyApiTest`)
   - Policy creation, updates, deletion
   - Category-based filtering

4. **Admin Tests** (`AdminApiTest`)
   - User management and role assignments
   - Department management

## Troubleshooting

### Common Issues

1. **Tests fail with 401 Unauthorized**
   - Ensure admin/agent tokens are properly configured
   - Verify tokens are not expired

2. **Connection refused errors**
   - Ensure ServiceHub backend is running on http://localhost:8080
   - Check if the base URL is correctly configured

3. **Allure report not generating**
   - Ensure tests have been run at least once: `mvn clean test`
   - Check that `target/allure-results/` directory exists and contains files

### Viewing Test Results

- **Console output**: Test results are displayed in the console during execution
- **Surefire reports**: Available in `target/surefire-reports/`
- **Allure reports**: Rich HTML reports with detailed test information

## Contributing

When adding new tests:

1. Extend the appropriate base test class (`BaseApiTest`)
2. Add proper Allure annotations (@Epic, @Feature, @Story, @Severity, @Description)
3. Follow the existing naming conventions
4. Include both positive and negative test scenarios
5. Ensure proper cleanup of test data

## Support

For questions or issues with the test suite, please refer to the project documentation or contact the QA team.