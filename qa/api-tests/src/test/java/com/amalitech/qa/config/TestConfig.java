package com.amalitech.qa.config;

/**
 * Configuration class for test environment variables and settings
 */
public class TestConfig {

    // Environment variable keys
    private static final String ADMIN_TOKEN_KEY = "ADMIN_TOKEN";
    private static final String AGENT_TOKEN_KEY = "AGENT_TOKEN";
    private static final String BASE_URL_KEY = "BASE_URL";

    // Default values
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    /**
     * Get admin authentication token from environment
     */
    public static String getAdminToken() {
        String token = System.getenv(ADMIN_TOKEN_KEY);
        if (token == null || token.trim().isEmpty()) {
            token = System.getProperty("admin.token");
        }
        return token != null ? token.trim() : "";
    }

    /**
     * Get agent authentication token from environment
     */
    public static String getAgentToken() {
        String token = System.getenv(AGENT_TOKEN_KEY);
        if (token == null || token.trim().isEmpty()) {
            token = System.getProperty("agent.token");
        }
        return token != null ? token.trim() : "";
    }

    /**
     * Get base URL for API tests
     */
    public static String getBaseUrl() {
        String baseUrl = System.getenv(BASE_URL_KEY);
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = System.getProperty("base.url", DEFAULT_BASE_URL);
        }
        return baseUrl.trim();
    }

    /**
     * Check if admin token is available
     */
    public static boolean hasAdminToken() {
        return !getAdminToken().isEmpty();
    }

    /**
     * Check if agent token is available
     */
    public static boolean hasAgentToken() {
        return !getAgentToken().isEmpty();
    }

    /**
     * Get environment name for test reporting
     */
    public static String getEnvironment() {
        return System.getProperty("test.environment", "local");
    }
}
