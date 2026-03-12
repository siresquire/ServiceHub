package com.amalitech.qa.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Test data for service request API tests
 */
public class ServiceRequestTestData {
    
    // Request titles and descriptions
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
    
    // Categories
    public enum Category {
        HARDWARE,
        SOFTWARE,
        NETWORK,
        ACCESS,
        OTHER
    }
    
    // Priorities
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    // Request statuses
    public enum Status {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }
    
    /**
     * Create a valid service request
     */
    public static Map<String, Object> createValidServiceRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", REQUEST_TITLES[0]);
        request.put("description", REQUEST_DESCRIPTIONS[0]);
        request.put("category", Category.HARDWARE.name());
        request.put("priority", Priority.HIGH.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    /**
     * Create a service request with missing required fields
     */
    public static Map<String, Object> createIncompleteServiceRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("description", "Missing title and other required fields");
        // Missing title, category, priority
        return request;
    }
    
    /**
     * Create a service request with invalid department
     */
    public static Map<String, Object> createInvalidDepartmentServiceRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Request Invalid Dept");
        request.put("description", "Testing invalid department");
        request.put("category", Category.SOFTWARE.name());
        request.put("priority", Priority.MEDIUM.name());
        request.put("departmentId", TestData.INVALID_ID);
        return request;
    }
    
    /**
     * Create a service request for testing get by ID
     */
    public static Map<String, Object> createTestGetByIdServiceRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Get By ID");
        request.put("description", "Testing get by ID functionality");
        request.put("category", Category.NETWORK.name());
        request.put("priority", Priority.LOW.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a service request for assignment testing
     */
    public static Map<String, Object> createAssignmentTestServiceRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Assign to Me");
        request.put("description", "Testing assignment to current agent");
        request.put("category", Category.SOFTWARE.name());
        request.put("priority", Priority.MEDIUM.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a service request for specific agent assignment
     */
    public static Map<String, Object> createSpecificAgentAssignmentRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Assign to Specific Agent");
        request.put("description", "Testing assignment to specific agent");
        request.put("category", Category.ACCESS.name());
        request.put("priority", Priority.HIGH.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    /**
     * Create a service request for status update testing
     */
    public static Map<String, Object> createStatusUpdateTestRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Status Update");
        request.put("description", "Testing status update functionality");
        request.put("category", Category.OTHER.name());
        request.put("priority", Priority.MEDIUM.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a service request for resolution testing
     */
    public static Map<String, Object> createResolveTestRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Resolve Request");
        request.put("description", "Testing resolve functionality");
        request.put("category", Category.HARDWARE.name());
        request.put("priority", Priority.LOW.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a service request for invalid transition testing
     */
    public static Map<String, Object> createInvalidTransitionTestRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test Invalid Transition");
        request.put("description", "Testing invalid status transition");
        request.put("category", Category.SOFTWARE.name());
        request.put("priority", Priority.HIGH.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a high priority request for SLA testing
     */
    public static Map<String, Object> createSlaTestRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Test SLA Fields");
        request.put("description", "Testing SLA deadline fields");
        request.put("category", Category.HARDWARE.name());
        request.put("priority", Priority.CRITICAL.name());
        request.put("departmentId", TestData.IT_DEPARTMENT_ID);
        return request;
    }
    
    /**
     * Create a status update request
     */
    public static Map<String, Object> createStatusUpdateRequest(Status newStatus, String comment) {
        Map<String, Object> request = new HashMap<>();
        request.put("newStatus", newStatus.name());
        request.put("comment", comment);
        return request;
    }
}