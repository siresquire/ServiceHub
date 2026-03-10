# ServiceHub Test Automation

Comprehensive test automation suite for the ServiceHub application.

## Modules

### 1. API Tests (`api-tests/`)
REST API test automation using REST Assured and JUnit 5.

**Key Features:**
- REST Assured for API testing
- JUnit 5 for test execution
- Base test classes for common setup
- Configuration management
- JSON processing with Jackson

**Run API tests:**
```bash
cd api-tests
mvn clean test
```

### 2. UI Tests (`ui-tests/`)
Selenium-based UI test automation using WebDriver and JUnit 5.

**Key Features:**
- Selenium WebDriver 4
- WebDriverManager for automatic driver management
- Page Object Model pattern
- Support for Chrome and Firefox
- Headless execution support

**Run UI tests:**
```bash
cd ui-tests
mvn clean test
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- ServiceHub application running locally

## Quick Start

### Build all test modules
```bash
cd qa
mvn clean install -DskipTests
```

### Run all tests
```bash
# API tests
cd api-tests && mvn test

# UI tests
cd ui-tests && mvn test
```

## Test Development Guidelines

1. **API Tests**: Extend `BaseApiTest` and use REST Assured
2. **UI Tests**: Extend `BaseUITest` and follow Page Object Model
3. **Naming**: Test classes must end with `Test` or `Tests`
4. **Configuration**: Use properties files for environment-specific settings
5. **Assertions**: Use AssertJ for fluent assertions

## Future Enhancements

- Integration test suite
- Performance testing module
- Test reporting with Allure
- CI/CD pipeline integration
- Database validation utilities
