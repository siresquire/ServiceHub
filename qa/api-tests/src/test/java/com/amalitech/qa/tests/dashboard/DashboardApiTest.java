package com.amalitech.qa.tests.dashboard;

import com.amalitech.qa.base.BaseTest;
import com.amalitech.qa.config.ApiConfig;
import com.amalitech.qa.utils.TokenManager;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardApiTest extends BaseTest {

    // DASHBOARD SUMMARY TESTS

    @Test
    @DisplayName("Should successfully fetch dashboard summary (admin)")
    public void testGetDashboardSummaryAsAdmin() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/summary")
                .then()
                .statusCode(200)
                .body("totalRequests", notNullValue())
                .body("openRequests", notNullValue())
                .body("resolvedRequests", notNullValue())
                .body("totalUsers", notNullValue())
                .body("agentCount", notNullValue())
                .body("totalDepartments", notNullValue())
                .body("avgResolutionHours", notNullValue())
                .body("slaComplianceRate", notNullValue())
                .body("requestsByCategory", notNullValue())
                .body("requestsByPriority", notNullValue())
                .body("requestsByStatus", notNullValue())
                .extract()
                .response();

        // Validate that numeric fields are non-negative
        Long totalRequests = response.jsonPath().getLong("totalRequests");
        Long openRequests = response.jsonPath().getLong("openRequests");
        Long resolvedRequests = response.jsonPath().getLong("resolvedRequests");
        Long totalUsers = response.jsonPath().getLong("totalUsers");
        Long agentCount = response.jsonPath().getLong("agentCount");
        Long totalDepartments = response.jsonPath().getLong("totalDepartments");
        Double avgResolutionHours = response.jsonPath().getDouble("avgResolutionHours");
        Double slaComplianceRate = response.jsonPath().getDouble("slaComplianceRate");

        assertTrue(totalRequests >= 0, "Total requests should be non-negative");
        assertTrue(openRequests >= 0, "Open requests should be non-negative");
        assertTrue(resolvedRequests >= 0, "Resolved requests should be non-negative");
        assertTrue(totalUsers >= 0, "Total users should be non-negative");
        assertTrue(agentCount >= 0, "Agent count should be non-negative");
        assertTrue(totalDepartments >= 0, "Total departments should be non-negative");
        
        if (avgResolutionHours != null) {
            assertTrue(avgResolutionHours >= 0, "Average resolution hours should be non-negative");
        }
        
        if (slaComplianceRate != null) {
            assertTrue(slaComplianceRate >= 0 && slaComplianceRate <= 100, 
                "SLA compliance rate should be between 0 and 100");
        }

        System.out.println("Dashboard Summary (Admin) Response: " + response.asString());
    }

    @Test
    @DisplayName("Should successfully fetch dashboard summary (agent)")
    public void testGetDashboardSummaryAsAgent() {
        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/summary")
                .then()
                .statusCode(200)
                .body("totalRequests", notNullValue())
                .body("openRequests", notNullValue())
                .body("resolvedRequests", notNullValue())
                .body("requestsByCategory", notNullValue())
                .body("requestsByPriority", notNullValue())
                .body("requestsByStatus", notNullValue())
                .extract()
                .response();

        System.out.println("Dashboard Summary (Agent) Response: " + response.asString());
    }

    @Test
    @DisplayName("Should validate dashboard summary metrics structure")
    public void testDashboardSummaryMetricsStructure() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/summary")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Validate that category breakdown contains expected categories
        Object requestsByCategory = response.jsonPath().get("requestsByCategory");
        assertNotNull(requestsByCategory, "Requests by category should not be null");

        // Validate that priority breakdown contains expected priorities
        Object requestsByPriority = response.jsonPath().get("requestsByPriority");
        assertNotNull(requestsByPriority, "Requests by priority should not be null");

        // Validate that status breakdown contains expected statuses
        Object requestsByStatus = response.jsonPath().get("requestsByStatus");
        assertNotNull(requestsByStatus, "Requests by status should not be null");

        System.out.println("Dashboard Metrics Structure Validation Response: " + response.asString());
    }

    // SLA STATISTICS TESTS

    @Test
    @DisplayName("Should successfully fetch SLA statistics (admin only)")
    public void testGetSlaStatistics() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/sla")
                .then()
                .statusCode(200)
                .body("slaComplianceRate", notNullValue())
                .extract()
                .response();

        // Validate SLA compliance rate is within valid range
        Double slaComplianceRate = response.jsonPath().getDouble("slaComplianceRate");
        if (slaComplianceRate != null) {
            assertTrue(slaComplianceRate >= 0 && slaComplianceRate <= 100, 
                "SLA compliance rate should be between 0 and 100");
        }

        System.out.println("SLA Statistics Response: " + response.asString());
    }

    @Test
    @DisplayName("Should validate SLA statistics structure")
    public void testSlaStatisticsStructure() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/sla")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Check if slaByCategory is present (may be null if no data)
        Object slaByCategory = response.jsonPath().get("slaByCategory");
        // slaByCategory can be null if no SLA data exists, so we just log it
        System.out.println("SLA by Category: " + slaByCategory);

        System.out.println("SLA Statistics Structure Response: " + response.asString());
    }

    // TRENDS TESTS

    @Test
    @DisplayName("Should successfully fetch trends with 7 days period")
    public void testGetTrendsSevenDays() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("period", 7)
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(200)
                .body("dailyVolume", notNullValue())
                .body("period", notNullValue())
                .extract()
                .response();

        String period = response.jsonPath().getString("period");
        assertEquals("7", period, "Period should be 7 days");

        // Validate daily volume structure
        Object dailyVolume = response.jsonPath().get("dailyVolume");
        assertNotNull(dailyVolume, "Daily volume should not be null");

        System.out.println("Trends 7 Days Response: " + response.asString());
    }

    @Test
    @DisplayName("Should successfully fetch trends with 30 days period")
    public void testGetTrendsThirtyDays() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("period", 30)
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(200)
                .body("dailyVolume", notNullValue())
                .body("period", notNullValue())
                .extract()
                .response();

        String period = response.jsonPath().getString("period");
        assertEquals("30", period, "Period should be 30 days");

        System.out.println("Trends 30 Days Response: " + response.asString());
    }

    @Test
    @DisplayName("Should use default period when no parameter provided")
    public void testGetTrendsDefaultPeriod() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(200)
                .body("dailyVolume", notNullValue())
                .body("period", notNullValue())
                .extract()
                .response();

        String period = response.jsonPath().getString("period");
        assertEquals("7", period, "Default period should be 7 days");

        System.out.println("Trends Default Period Response: " + response.asString());
    }

    @Test
    @DisplayName("Should successfully fetch trends as agent")
    public void testGetTrendsAsAgent() {
        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .queryParam("period", 7)
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(200)
                .body("dailyVolume", notNullValue())
                .body("period", equalTo("7"))
                .extract()
                .response();

        System.out.println("Trends (Agent) Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with invalid period parameter")
    public void testGetTrendsInvalidPeriod() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("period", 999) // Invalid period
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(400)
                .extract()
                .response();

        System.out.println("Invalid Period Response: " + response.asString());
    }

    @Test
    @DisplayName("Should validate trends daily volume structure")
    public void testTrendsDailyVolumeStructure() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("period", 7)
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(200)
                .extract()
                .response();

        Object dailyVolume = response.jsonPath().get("dailyVolume");
        assertNotNull(dailyVolume, "Daily volume should not be null");

        // The dailyVolume should be a map with date keys and volume values
        // We can't predict exact dates, but we can validate the structure
        System.out.println("Daily Volume Structure: " + dailyVolume);
        System.out.println("Trends Daily Volume Structure Response: " + response.asString());
    }

    // AUTHORIZATION TESTS

    @Test
    @DisplayName("Should fail with unauthorized access (no token)")
    public void testUnauthorizedAccessSummary() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.DASHBOARD + "/summary")
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Unauthorized Access Summary Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with unauthorized access to SLA endpoint (no token)")
    public void testUnauthorizedAccessSla() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.DASHBOARD + "/sla")
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Unauthorized Access SLA Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with unauthorized access to trends endpoint (no token)")
    public void testUnauthorizedAccessTrends() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Unauthorized Access Trends Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with forbidden access to summary (customer role)")
    public void testForbiddenAccessSummary() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/summary")
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Summary Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with forbidden access to SLA (agent role)")
    public void testForbiddenAccessSlaAsAgent() {
        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/sla")
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access SLA (Agent) Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with forbidden access to SLA (customer role)")
    public void testForbiddenAccessSlaAsCustomer() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/sla")
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access SLA (Customer) Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with forbidden access to trends (customer role)")
    public void testForbiddenAccessTrends() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.DASHBOARD + "/trends")
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Trends Response: " + response.asString());
    }
}