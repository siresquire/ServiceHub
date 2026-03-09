# ServiceHub UI Tests

Selenium-based UI test automation for the ServiceHub application using WebDriver and JUnit 5.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Chrome or Firefox browser
- ServiceHub application running on http://localhost:8080

## Project Structure

```
ui-tests/
├── src/test/java/com/amalitech/qa/
│   ├── base/              # Base test classes
│   ├── pages/             # Page Object Model classes
│   └── DashboardPageTest.java
├── src/test/resources/
│   └── test.properties    # Test configuration properties
└── pom.xml
```

## Running Tests

### Run all tests (headless mode)
```bash
mvn clean test
```

### Run with visible browser
```bash
mvn clean test -Dheadless=false
```

### Run with Firefox
```bash
mvn clean test -Dbrowser=firefox
```

### Run specific test class
```bash
mvn test -Dtest=DashboardPageTest
```

## Configuration

Edit `src/test/resources/test.properties` to configure:
- Base URL
- Browser type
- Headless mode
- Wait timeouts

## Adding New Tests

1. Extend `BaseUITest` class
2. Create Page Objects extending `BasePage`
3. Follow Page Object Model pattern
4. Use JUnit 5 annotations (@Test, @BeforeEach, @AfterEach)
5. Follow naming convention: `*Test.java`
