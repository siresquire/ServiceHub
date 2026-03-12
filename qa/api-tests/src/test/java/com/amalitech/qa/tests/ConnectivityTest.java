package com.amalitech.qa.tests;

import com.amalitech.qa.base.BaseApiTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Basic connectivity tests to verify API is accessible
 */
@Epic("ServiceHub API Testing")
@Feature("API Connectivity")
public class ConnectivityTest extends BaseApiTest {

    @Test
    @Story("API Health Check")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that the API server is running and accessible")
    @DisplayName("API Server Connectivity Test")
    public void testApiConnectivity() {
        // Test public departments endpoint (no auth required)
        given()
            .when()
                .get("/api/departments")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }

    @Test
    @Story("Authentication Endpoint")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that authentication endpoints are accessible")
    @DisplayName("Authentication Endpoint Accessibility")
    public void testAuthEndpointAccessibility() {
        // Test login endpoint with invalid credentials (should return 401, not connection error)
        givenJsonRequest()
            .body("{\"email\":\"test@test.com\",\"password\":\"invalid\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
}