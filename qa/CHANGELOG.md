# QA Test Automation Changelog

## [1.0.0] - 2026-03-09

### Added
- Initial test automation framework setup
- API tests module with REST Assured and JUnit 5
- UI tests module with Selenium WebDriver and JUnit 5
- Maven build configuration for both modules
- Base test classes for common setup and utilities
- Configuration management with properties files
- Page Object Model base classes for UI tests
- Sample test implementations
- Comprehensive documentation (README, SETUP guide)
- Verification scripts for Windows and Unix systems
- .gitignore for test artifacts

### Dependencies
**API Tests:**
- REST Assured 5.4.0
- JUnit 5.10.1
- Jackson 2.16.1
- AssertJ 3.25.1
- SLF4J 2.0.9

**UI Tests:**
- Selenium WebDriver 4.16.1
- WebDriverManager 5.6.3
- JUnit 5.10.1
- AssertJ 3.25.1
- SLF4J 2.0.9

### Project Structure
```
qa/
├── api-tests/          # REST API test automation
├── ui-tests/           # Selenium UI test automation
├── README.md           # Overview and quick start
├── SETUP.md            # Detailed setup guide
├── CHANGELOG.md        # This file
├── verify-setup.sh     # Unix verification script
├── verify-setup.bat    # Windows verification script
└── .gitignore          # Git ignore rules
```

### Configuration
- Java 17 as target version
- Maven Surefire 3.2.3 for test execution
- Parallel test execution support
- Configurable test properties for environments

### Notes
- TestNG removed in favor of JUnit 5 exclusively
- WebDriverManager handles automatic driver downloads
- Tests require ServiceHub backend running on localhost:8080
- Both modules build successfully with `mvn clean compile test-compile`
