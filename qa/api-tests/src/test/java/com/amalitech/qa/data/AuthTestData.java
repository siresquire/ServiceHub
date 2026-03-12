package com.amalitech.qa.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Test data for authentication API tests
 */
public class AuthTestData {
    
    // User credentials
    public static final String ADMIN_EMAIL = "admin@servicehub.com";
    public static final String ADMIN_PASSWORD = "admin123"; // pragma: allowlist secret
    
    public static final String AGENT_EMAIL = "agent@servicehub.com";
    public static final String AGENT_PASSWORD = "agent123"; // pragma: allowlist secret
    
    public static final String CUSTOMER_EMAIL = "customer@servicehub.com";
    public static final String CUSTOMER_PASSWORD = "customer123"; // pragma: allowlist secret
    
    // Test user data
    public static final String TEST_USER_FIRST_NAME = "Test";
    public static final String TEST_USER_LAST_NAME = "User";
    public static final String TEST_USER_FULL_NAME = TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME;
    public static final String TEST_USER_PASSWORD = "testpass123"; // pragma: allowlist secret
    
    /**
     * Create a valid registration request
     */
    public static Map<String, Object> createValidRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", TEST_USER_FULL_NAME);
        request.put("email", "newuser" + System.currentTimeMillis() + "@example.com");
        request.put("password", TEST_USER_PASSWORD);
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a registration request with duplicate email
     */
    public static Map<String, Object> createDuplicateEmailRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", TEST_USER_FULL_NAME);
        request.put("email", ADMIN_EMAIL); // Using existing admin email
        request.put("password", TEST_USER_PASSWORD);
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a registration request with invalid password
     */
    public static Map<String, Object> createInvalidPasswordRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", TEST_USER_FULL_NAME);
        request.put("email", "shortpass" + System.currentTimeMillis() + "@example.com");
        request.put("password", "12345"); // Less than 6 characters
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a registration request with missing required fields
     */
    public static Map<String, Object> createIncompleteRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "incomplete" + System.currentTimeMillis() + "@example.com");
        // Missing name, password, departmentId
        return request;
    }
    
    /**
     * Create a registration request with invalid department
     */
    public static Map<String, Object> createInvalidDepartmentRegistrationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", TEST_USER_FULL_NAME);
        request.put("email", "invaliddept" + System.currentTimeMillis() + "@example.com");
        request.put("password", TEST_USER_PASSWORD);
        request.put("departmentId", TestData.INVALID_ID);
        return request;
    }
    
    /**
     * Create a valid login request
     */
    public static Map<String, String> createValidLoginRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("email", ADMIN_EMAIL);
        request.put("password", ADMIN_PASSWORD);
        return request;
    }
    
    /**
     * Create a login request with wrong password
     */
    public static Map<String, String> createWrongPasswordLoginRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("email", ADMIN_EMAIL);
        request.put("password", "wrongpassword");
        return request;
    }
    
    /**
     * Create a login request with non-existing email
     */
    public static Map<String, String> createNonExistingEmailLoginRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");
        request.put("password", ADMIN_PASSWORD);
        return request;
    }
}