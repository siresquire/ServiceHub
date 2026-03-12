package com.amalitech.qa.utils;

import com.amalitech.qa.config.ApiConfig;
import com.amalitech.qa.data.TestData;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing JWT authentication tokens
 */
public class TokenManager {
    
    private static String adminToken;
    private static String agentToken;
    private static String customerToken;
    
    /**
     * Get admin token, login if not cached
     */
    public static String getAdminToken() {
        if (adminToken == null || isTokenExpired(adminToken)) {
            adminToken = performLogin(TestData.ADMIN_EMAIL, TestData.ADMIN_PASSWORD);
        }
        return adminToken;
    }
    
    /**
     * Get agent token, login if not cached
     */
    public static String getAgentToken() {
        if (agentToken == null || isTokenExpired(agentToken)) {
            agentToken = performLogin(TestData.AGENT_EMAIL, TestData.AGENT_PASSWORD);
        }
        return agentToken;
    }
    
    /**
     * Get customer token, login if not cached
     */
    public static String getCustomerToken() {
        if (customerToken == null || isTokenExpired(customerToken)) {
            customerToken = performLogin(TestData.CUSTOMER_EMAIL, TestData.CUSTOMER_PASSWORD);
        }
        return customerToken;
    }
    
    /**
     * Perform login request and extract token
     */
    private static String performLogin(String email, String password) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(loginRequest)
                .when()
                .post(ApiConfig.AUTH_LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .response();
        
        return response.jsonPath().getString("token");
    }
    
    /**
     * Check if token is expired (simplified check)
     * In a real implementation, you would decode the JWT and check expiration
     */
    private static boolean isTokenExpired(String token) {
        // For now, assume tokens don't expire during test execution
        // In production, implement proper JWT expiration checking
        return false;
    }
    
    /**
     * Clear all cached tokens (useful for cleanup)
     */
    public static void clearTokens() {
        adminToken = null;
        agentToken = null;
        customerToken = null;
    }
}