package com.amalitech.qa.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Test data for department API tests
 */
public class DepartmentTestData {
    
    // Department names
    public static final String NEW_DEPARTMENT_NAME = "Quality Assurance";
    public static final String UPDATED_DEPARTMENT_NAME = "Quality Assurance Updated";
    public static final String DUPLICATE_DEPARTMENT_NAME = "Information Technology"; // Assuming this exists
    
    // Department emails
    public static final String NEW_DEPARTMENT_EMAIL = "qa@servicehub.com";
    public static final String UPDATED_DEPARTMENT_EMAIL = "qa-updated@servicehub.com";
    
    // Request categories for departments
    public enum RequestCategory {
        IT_SUPPORT,
        FACILITIES,
        HR_REQUEST
    }
    
    /**
     * Create a valid department creation request
     */
    public static Map<String, Object> createValidDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", NEW_DEPARTMENT_NAME);
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("contactEmail", NEW_DEPARTMENT_EMAIL);
        return request;
    }
    
    /**
     * Create a department request with duplicate name
     */
    public static Map<String, Object> createDuplicateDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", DUPLICATE_DEPARTMENT_NAME);
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("contactEmail", "duplicate@servicehub.com");
        return request;
    }
    
    /**
     * Create a department request for testing updates
     */
    public static Map<String, Object> createTestUpdateDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Update Department");
        request.put("category", RequestCategory.FACILITIES.name());
        request.put("contactEmail", "update@test.com");
        return request;
    }
    
    /**
     * Create an updated department request
     */
    public static Map<String, Object> createUpdatedDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", UPDATED_DEPARTMENT_NAME);
        request.put("category", RequestCategory.HR_REQUEST.name());
        request.put("contactEmail", UPDATED_DEPARTMENT_EMAIL);
        return request;
    }
    
    /**
     * Create a department request for deletion testing
     */
    public static Map<String, Object> createTestDeleteDepartmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Delete Department");
        request.put("category", RequestCategory.FACILITIES.name());
        request.put("contactEmail", "delete@test.com");
        return request;
    }
    
    /**
     * Create a department request with IT_SUPPORT category
     */
    public static Map<String, Object> createItSupportDepartmentRequest(String name, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("contactEmail", email);
        return request;
    }
    
    /**
     * Create a department request with FACILITIES category
     */
    public static Map<String, Object> createFacilitiesDepartmentRequest(String name, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("category", RequestCategory.FACILITIES.name());
        request.put("contactEmail", email);
        return request;
    }
    
    /**
     * Create a department request with HR_REQUEST category
     */
    public static Map<String, Object> createHrRequestDepartmentRequest(String name, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("category", RequestCategory.HR_REQUEST.name());
        request.put("contactEmail", email);
        return request;
    }
}