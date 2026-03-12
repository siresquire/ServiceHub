package com.amalitech.qa.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for API testing
 */
public class ApiConfig {
    
    public static final String BASE_URL = "http://localhost:8080";
    public static final String API_VERSION = "/api/v1";
    
    // Endpoints
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String SERVICE_REQUESTS = "/api/requests";
    public static final String DEPARTMENTS = "/departments";
    public static final String SLA_POLICIES = "/sla-policies";
    public static final String DASHBOARD = "/dashboard";
    public static final String ADMIN = "/admin";
    
    // Admin endpoints
    public static final String ADMIN_USERS = "/api/admin/users";
    public static final String ADMIN_DEPARTMENTS = "/api/admin/departments";
    
    // Default headers
    public static Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }
    
    // Authentication headers
    public static Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = getDefaultHeaders();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }
    
    // Timeouts
    public static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
    public static final int LONG_TIMEOUT = 60000; // 60 seconds
}