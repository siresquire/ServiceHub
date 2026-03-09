package com.amalitech.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration management for API tests
 */
public class TestConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Failed to load test.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public static String getBaseUri() {
        return getProperty("base.uri", "http://localhost:8080");
    }

    public static String getAdminEmail() {
        return getProperty("admin.email", "admin@amalitech.com");
    }

    public static String getAdminPassword() {
        return getProperty("admin.password", "password123");
    }
}
