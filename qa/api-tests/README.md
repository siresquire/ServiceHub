# ServiceHub API Tests

Comprehensive API test suite for the ServiceHub application using RestAssured + JUnit 5 with Allure reporting.

## Test Structure

Tests are organized by feature areas:
- **Authentication** - User registration, login, logout
- **Departments** - Department CRUD operations
- **Users** - User management and role updates
- **Service Requests** - Request creation, assignment, status updates
- **SLA Policies** - SLA policy management
- **Dashboard** - Analytics and reporting endpoints

## Running Tests

### Prerequisites
- Java 17+
- Maven 3.6+
- ServiceHub backend running on `http://localhost:8080`

### Execute Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthApiTest

# Run specific test method
mvn test -Dtest=AuthApiTest#testSuccessfulLogin
```

## Allure Reporting

### Generate Allure Report
```bash
# Run tests and generate Allure results
mvn clean test

# Generate and serve Allure report (requires Allure CLI)
mvn allure:serve

# Or generate static report
mvn allure:report
```

### Install Allure CLI (Optional)
```bash
# Using npm
npm install -g allure-commandline

# Using Homebrew (macOS)
brew install allure

# Manual download from https://github.com/allure-framework/allure2/releases
```

### View Reports
- **Live server**: `mvn allure:serve` opens report in browser
- **Static files**: Generated in `target/site/allure-maven-plugin/`
- **Results**: Raw JSON files in `target/allure-results/`

## Test Features

### Allure Annotations
- `@Epic` - High-level feature grouping
- `@Feature` - Specific functionality area
- `@Story` - User story or test scenario
- `@Severity` - Test importance level
- `@Description` - Detailed test description

### Attachments
- Request payloads (JSON)
- Response bodies (JSON)
- HTTP status codes
- Authentication tokens
- Error messages

### Test Data
- Centralized test data in `data/` package
- Reusable request builders
- Environment-specific configuration

## Configuration

### Base URL
Default: `http://localhost:8080`
Configure in `ApiConfig.java`

### Authentication
- Admin token management via `TokenManager`
- Automatic token refresh
- Role-based test execution

### Allure Properties
Configure in `src/test/resources/allure.properties`:
```properties
allure.results.directory=target/allure-results
allure.link.issue.pattern=https://github.com/ServiceHub/issues/{}
allure.link.tms.pattern=https://github.com/ServiceHub/testcases/{}
```

## Test Categories

### Critical Tests
- User authentication flows
- Service request creation
- Dashboard summary retrieval

### Normal Tests
- Data validation scenarios
- Error handling
- Edge cases

### Authorization Tests
- Role-based access control
- Token validation
- Forbidden access scenarios
