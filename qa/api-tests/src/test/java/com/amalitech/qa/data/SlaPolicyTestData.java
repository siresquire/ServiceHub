package com.amalitech.qa.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Test data for SLA policy API tests
 */
public class SlaPolicyTestData {
    
    // SLA policy test data
    public static final String SLA_POLICY_NAME = "Standard IT Support";
    public static final String SLA_POLICY_DESCRIPTION = "Standard SLA for IT support requests";
    public static final int SLA_RESPONSE_TIME_HOURS = 4;
    public static final int SLA_RESOLUTION_TIME_HOURS = 24;
    
    // Request categories for SLA policies
    public enum RequestCategory {
        IT_SUPPORT,
        FACILITIES,
        HR_REQUEST
    }
    
    // Priorities for SLA policies
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Create a valid SLA policy request
     */
    public static Map<String, Object> createValidSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("priority", Priority.HIGH.name());
        request.put("responseTimeHours", (double) SLA_RESPONSE_TIME_HOURS);
        request.put("resolutionTimeHours", (double) SLA_RESOLUTION_TIME_HOURS);
        return request;
    }
    
    /**
     * Create a duplicate SLA policy request
     */
    public static Map<String, Object> createDuplicateSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.FACILITIES.name());
        request.put("priority", Priority.MEDIUM.name());
        request.put("responseTimeHours", 8.0);
        request.put("resolutionTimeHours", 48.0);
        return request;
    }
    
    /**
     * Create an SLA policy request with invalid data
     */
    public static Map<String, Object> createInvalidSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.HR_REQUEST.name());
        request.put("priority", Priority.LOW.name());
        request.put("responseTimeHours", -1.0); // Invalid negative value
        request.put("resolutionTimeHours", -5.0); // Invalid negative value
        return request;
    }
    /**
     * Create an SLA policy for testing get by ID
     */
    public static Map<String, Object> createTestGetByIdSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("priority", Priority.CRITICAL.name());
        request.put("responseTimeHours", 1.0);
        request.put("resolutionTimeHours", 4.0);
        return request;
    }
    
    /**
     * Create an SLA policy for update testing
     */
    public static Map<String, Object> createTestUpdateSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.IT_SUPPORT.name());
        request.put("priority", Priority.LOW.name());
        request.put("responseTimeHours", 12.0);
        request.put("resolutionTimeHours", 72.0);
        return request;
    }
    
    /**
     * Create an SLA policy update request
     */
    public static Map<String, Object> createSlaPolicyUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("responseTimeHours", 6.0);
        request.put("resolutionTimeHours", 36.0);
        return request;
    }
    
    /**
     * Create an invalid SLA policy update request
     */
    public static Map<String, Object> createInvalidSlaPolicyUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("responseTimeHours", -2.0); // Invalid negative value
        request.put("resolutionTimeHours", -10.0); // Invalid negative value
        return request;
    }
    
    /**
     * Create an SLA policy for deletion testing
     */
    public static Map<String, Object> createTestDeleteSlaPolicyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("category", RequestCategory.FACILITIES.name());
        request.put("priority", Priority.HIGH.name());
        request.put("responseTimeHours", 2.0);
        request.put("resolutionTimeHours", 12.0);
        return request;
    }
    
    /**
     * Create a valid update request for existing policy
     */
    public static Map<String, Object> createValidUpdateRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("responseTimeHours", 4.0);
        request.put("resolutionTimeHours", 24.0);
        return request;
    }
}