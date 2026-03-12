package com.amalitech.qa.base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for all API tests providing common configuration and utilities
 */
public abstract class BaseApiTest {

    protected static final String BASE_URI = "http://localhost:8080";
    protected static final String CONTENT_TYPE_JSON = "application/json";

    @BeforeAll
    public static void globalSetup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Configure Allure RestAssured filter to capture all API requests and responses
        RestAssured.filters(new AllureRestAssured());
    }

    @BeforeEach
    public void setUp() {
        // Reset any per-test configurations if needed
    }

    /**
     * Get a basic request specification with JSON headers
     */
    protected RequestSpecification givenJsonRequest() {
        return RestAssured.given()
                .contentType(CONTENT_TYPE_JSON)
                .accept(CONTENT_TYPE_JSON);
    }

    /**
     * Get a request specification with JWT token authentication
     */
    protected RequestSpecification givenAuthenticatedRequest(String token) {
        return givenJsonRequest()
                .header("Authorization", "Bearer " + token);
    }

    /**
     * Get a request specification with admin authentication
     */
    protected RequestSpecification givenAdminRequest() {
        return givenAuthenticatedRequest(getAdminToken());
    }

    /**
     * Get a request specification with agent authentication
     */
    protected RequestSpecification givenAgentRequest() {
        return givenAuthenticatedRequest(getAgentToken());
    }

    /**
     * Override this method in subclasses to provide admin token
     */
    protected String getAdminToken() {
        return System.getProperty("admin.token", "");
    }

    /**
     * Override this method in subclasses to provide agent token
     */
    protected String getAgentToken() {
        return System.getProperty("agent.token", "");
    }
}
