package com.amalitech.qa.data;

/**
 * Centralized test data for API testing
 */
public class TestData {
    
    // User credentials
    public static final String ADMIN_EMAIL = "admin@servicehub.com";
    public static final String ADMIN_PASSWORD = "admin123"; // pragma: allowlist secret
    
    public static final String AGENT_EMAIL = "agent@servicehub.com";
    public static final String AGENT_PASSWORD = "agent123"; // pragma: allowlist secret
    
    public static final String CUSTOMER_EMAIL = "customer@servicehub.com";
    public static final String CUSTOMER_PASSWORD = "customer123"; // pragma: allowlist secret
    
    // Test user data for registration
    public static final String TEST_USER_EMAIL = "testuser@example.com";
    public static final String TEST_USER_PASSWORD = "testpass123"; // pragma: allowlist secret
    public static final String TEST_USER_FIRST_NAME = "Test";
    public static final String TEST_USER_LAST_NAME = "User";
    
    // Department IDs
    public static final Long IT_DEPARTMENT_ID = 1L;
    public static final Long HR_DEPARTMENT_ID = 2L;
    public static final Long FINANCE_DEPARTMENT_ID = 3L;
    
    // Department names
    public static final String IT_DEPARTMENT_NAME = "Information Technology";
    public static final String HR_DEPARTMENT_NAME = "Human Resources";
    public static final String FINANCE_DEPARTMENT_NAME = "Finance";
    
    // Service request test data
    public static final String[] REQUEST_TITLES = {
        "Password Reset Request",
        "Software Installation",
        "Hardware Replacement",
        "Network Access Issue",
        "Email Configuration"
    };
    
    public static final String[] REQUEST_DESCRIPTIONS = {
        "Need to reset my password for the system",
        "Please install Microsoft Office on my workstation",
        "My laptop keyboard is not working properly",
        "Cannot access the shared network drive",
        "Need help configuring email on mobile device"
    };
    
    // Priorities
    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_CRITICAL = "CRITICAL";
    
    // Categories
    public static final String CATEGORY_HARDWARE = "HARDWARE";
    public static final String CATEGORY_SOFTWARE = "SOFTWARE";
    public static final String CATEGORY_NETWORK = "NETWORK";
    public static final String CATEGORY_ACCESS = "ACCESS";
    public static final String CATEGORY_OTHER = "OTHER";
    
    // Request statuses
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CLOSED = "CLOSED";
    
    // SLA Policy test data
    public static final String SLA_POLICY_NAME = "Standard IT Support";
    public static final String SLA_POLICY_DESCRIPTION = "Standard SLA for IT support requests";
    public static final int SLA_RESPONSE_TIME_HOURS = 4;
    public static final int SLA_RESOLUTION_TIME_HOURS = 24;
    
    // Common test values
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    // Invalid data for negative testing
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String EMPTY_STRING = "";
    public static final String NULL_STRING = null;
    public static final Long INVALID_ID = 99999L;
}