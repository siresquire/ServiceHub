# ServiceHub API Tests

REST API test automation for the ServiceHub application using REST Assured and JUnit 5.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- ServiceHub backend running on http://localhost:8080

## Project Structure

```
api-tests/
├── src/test/java/com/amalitech/qa/
│   ├── base/              # Base test classes
│   ├── config/            # Test configuration
│   └── ServiceRequestApiTest.java
├── src/test/resources/
│   └── test.properties    # Test configuration properties
└── pom.xml
```

## Running Tests

### Run all tests
```bash
mvn clean test
```

### Run with custom properties
```bash
mvn clean test -Dbase.uri=http://localhost:8080
```

### Run specific test class
```bash
mvn test -Dtest=ServiceRequestApiTest
```

## Configuration

Edit `src/test/resources/test.properties` to configure:
- Base URI
- Test credentials
- Timeouts

## Adding New Tests

1. Extend `BaseApiTest` class
2. Use REST Assured for API calls
3. Use JUnit 5 annotations (@Test, @BeforeAll, @AfterAll)
4. Follow naming convention: `*Test.java`
