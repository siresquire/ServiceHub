package com.amalitech.qa.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for all API tests providing common configuration and utilities
 */
public abstract class BaseTest {

    protected static final String BASE_URI = "http://localhost:8080";
    protected static final String CONTENT_TYPE_JSON = "application/json";

    @BeforeAll
    public static void globalSetup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Enable detailed logging for debugging
        RestAssured.filters(new io.restassured.filter.log.RequestLoggingFilter(),
                           new io.restassured.filter.log.ResponseLoggingFilter());
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
}