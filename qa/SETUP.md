# QA Test Automation Setup Guide

This guide walks through the complete setup and verification of the ServiceHub test automation framework.

## What's Been Set Up

### 1. API Tests Module (`api-tests/`)
- **Framework**: REST Assured + JUnit 5
- **Dependencies**:
  - REST Assured 5.4.0 for API testing
  - JUnit 5.10.1 for test execution
  - Jackson 2.16.1 for JSON processing
  - AssertJ 3.25.1 for fluent assertions
- **Structure**:
  - `base/BaseApiTest.java` - Base class with common setup
  - `config/TestConfig.java` - Configuration management
  - `ServiceRequestApiTest.java` - Sample API test
  - `test.properties` - Test configuration

### 2. UI Tests Module (`ui-tests/`)
- **Framework**: Selenium WebDriver + JUnit 5
- **Dependencies**:
  - Selenium 4.16.1 for browser automation
  - WebDriverManager 5.6.3 for automatic driver management
  - JUnit 5.10.1 for test execution
  - AssertJ 3.25.1 for fluent assertions
- **Structure**:
  - `base/BaseUITest.java` - Base class with WebDriver setup
  - `pages/BasePage.java` - Base Page Object class
  - `DashboardPageTest.java` - Sample UI test
  - `test.properties` - Test configuration

### 3. Build Configuration
- Maven 3.12.1 compiler plugin (Java 17)
- Maven Surefire 3.2.3 for test execution
- Proper test resource handling
- Parallel test execution support

## Prerequisites

Before running tests, ensure you have:

1. **Java 17 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **Chrome or Firefox browser** (for UI tests)

## Verification Steps

### Step 1: Verify Build Setup

```bash
cd qa

# Verify API tests build
cd api-tests
mvn clean compile test-compile

# Verify UI tests build
cd ../ui-tests
mvn clean compile test-compile
```

Both should show `BUILD SUCCESS`.

### Step 2: Run Verification Script (Optional)

```bash
cd qa
chmod +x verify-setup.sh
./verify-setup.sh
```

### Step 3: Start ServiceHub Backend

The tests require the ServiceHub backend to be running:

```bash
cd backend
mvn spring-boot:run
```

Wait until you see: `Started ServiceHubApplication`

### Step 4: Run Tests

**API Tests:**
```bash
cd qa/api-tests
mvn test
```

**UI Tests:**
```bash
cd qa/ui-tests
mvn test
```

## Configuration

### API Tests Configuration
Edit `qa/api-tests/src/test/resources/test.properties`:
```properties
base.uri=http://localhost:8080
api.timeout=30000
admin.email=admin@amalitech.com
admin.password=password123
```

### UI Tests Configuration
Edit `qa/ui-tests/src/test/resources/test.properties`:
```properties
base.url=http://localhost:8080
browser=chrome
headless=true
implicit.wait=10
explicit.wait=10
```

## Running Tests with Custom Properties

**Change base URL:**
```bash
mvn test -Dbase.uri=http://localhost:9090
```

**Run UI tests with visible browser:**
```bash
mvn test -Dheadless=false
```

**Run UI tests with Firefox:**
```bash
mvn test -Dbrowser=firefox
```

**Run specific test class:**
```bash
mvn test -Dtest=ServiceRequestApiTest
```

## Project Structure

```
qa/
├── api-tests/
│   ├── src/test/java/com/amalitech/qa/
│   │   ├── base/
│   │   │   └── BaseApiTest.java
│   │   ├── config/
│   │   │   └── TestConfig.java
│   │   └── ServiceRequestApiTest.java
│   ├── src/test/resources/
│   │   └── test.properties
│   ├── pom.xml
│   └── README.md
├── ui-tests/
│   ├── src/test/java/com/amalitech/qa/
│   │   ├── base/
│   │   │   └── BaseUITest.java
│   │   ├── pages/
│   │   │   └── BasePage.java
│   │   └── DashboardPageTest.java
│   ├── src/test/resources/
│   │   └── test.properties
│   ├── pom.xml
│   └── README.md
├── .gitignore
├── README.md
├── SETUP.md
└── verify-setup.sh
```

## Adding New Tests

### API Test Example

```java
package com.amalitech.qa;

import com.amalitech.qa.base.BaseApiTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class MyApiTest extends BaseApiTest {

    @Test
    public void testEndpoint() {
        given()
            .spec(requestSpec)
        .when()
            .get("/api/endpoint")
        .then()
            .statusCode(200);
    }
}
```

### UI Test Example

```java
package com.amalitech.qa;

import com.amalitech.qa.base.BaseUITest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyUITest extends BaseUITest {

    @Test
    public void testPage() {
        driver.get(baseUrl + "/page");
        assertTrue(driver.getTitle().contains("Expected"));
    }
}
```

## Troubleshooting

### Tests fail to connect to backend
- Ensure ServiceHub backend is running on http://localhost:8080
- Check `test.properties` for correct base URI/URL

### WebDriver issues
- WebDriverManager automatically downloads drivers
- Ensure you have Chrome or Firefox installed
- Try running with `-Dheadless=false` to see browser

### Build failures
- Verify Java 17 is installed: `java -version`
- Verify Maven is installed: `mvn -version`
- Clean and rebuild: `mvn clean install`

## Next Steps

1. **Expand test coverage** as features are developed
2. **Add Page Objects** for UI tests following POM pattern
3. **Create test data builders** for complex API payloads
4. **Integrate with CI/CD** pipeline
5. **Add test reporting** (Allure, Surefire reports)
6. **Implement test utilities** for common operations

## Support

For issues or questions:
- Check module README files in `api-tests/` and `ui-tests/`
- Review test examples in existing test classes
- Consult framework documentation (REST Assured, Selenium, JUnit 5)
