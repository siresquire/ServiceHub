package com.amalitech.qa.tests.sla;

import com.amalitech.qa.base.BaseApiTest;
import com.amalitech.qa.config.TestConfig;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API tests for SLA Policy endpoints
 */
@TestMethodOrder(OrderAnnotation.class)
public class SlaPolicyApiTest extends BaseApiTest {
    
    private static Long createdSlaPolicyId;
    private static final String SLA_POLICIES_ENDPOINT = "/api/sla-policies";
    
    @BeforeAll
    public static void setupClass() {
        Assumptions.assumeTrue(TestConfig.hasAdminToken(), 
            "Admin token required for SLA Policy tests");
    }
    
    @Test
    @Order(1)
    @DisplayName("GET /api/sla-policies - Get all SLA policies")
    public void testGetAllSlaPolicies() {
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(2)
    @DisplayName("POST /api/sla-policies - Create new SLA policy")
    public void testCreateSlaPolicy() {
        Map<String, Object> slaPolicyData = createSlaPolicyPayload(
            "IT_SUPPORT", "HIGH", 2.0, 24.0);
        
        Response response = givenAdminRequest()
            .body(slaPolicyData)
            .when()
                .post(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("category", equalTo("IT_SUPPORT"))
                .body("priority", equalTo("HIGH"))
                .body("responseTimeHours", equalTo(2.0f))
                .body("resolutionTimeHours", equalTo(24.0f))
                .log().ifValidationFails()
                .extract().response();
        
        createdSlaPolicyId = response.jsonPath().getLong("id");
        Assertions.assertNotNull(createdSlaPolicyId, "Created SLA policy ID should not be null");
    }
    
    @Test
    @Order(3)
    @DisplayName("POST /api/sla-policies - Create SLA policy with invalid data")
    public void testCreateSlaPolicyWithInvalidData() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("category", "INVALID_CATEGORY");
        invalidData.put("priority", "HIGH");
        invalidData.put("responseTimeHours", -1.0);
        invalidData.put("resolutionTimeHours", 24.0);
        
        givenAdminRequest()
            .body(invalidData)
            .when()
                .post(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(4)
    @DisplayName("GET /api/sla-policies/{id} - Get SLA policy by ID")
    public void testGetSlaPolicyById() {
        Assumptions.assumeTrue(createdSlaPolicyId != null, 
            "SLA policy must be created first");
        
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT + "/" + createdSlaPolicyId)
            .then()
                .statusCode(200)
                .body("id", equalTo(createdSlaPolicyId.intValue()))
                .body("category", equalTo("IT_SUPPORT"))
                .body("priority", equalTo("HIGH"))
                .body("responseTimeHours", equalTo(2.0f))
                .body("resolutionTimeHours", equalTo(24.0f))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(5)
    @DisplayName("GET /api/sla-policies/{id} - Get non-existent SLA policy")
    public void testGetNonExistentSlaPolicy() {
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(6)
    @DisplayName("PATCH /api/sla-policies/{id} - Update SLA policy")
    public void testUpdateSlaPolicy() {
        Assumptions.assumeTrue(createdSlaPolicyId != null, 
            "SLA policy must be created first");
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("responseTimeHours", 4.0);
        updateData.put("resolutionTimeHours", 48.0);
        
        givenAdminRequest()
            .body(updateData)
            .when()
                .patch(SLA_POLICIES_ENDPOINT + "/" + createdSlaPolicyId)
            .then()
                .statusCode(200)
                .body("id", equalTo(createdSlaPolicyId.intValue()))
                .body("responseTimeHours", equalTo(4.0f))
                .body("resolutionTimeHours", equalTo(48.0f))
                .body("category", equalTo("IT_SUPPORT"))
                .body("priority", equalTo("HIGH"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(7)
    @DisplayName("PATCH /api/sla-policies/{id} - Update with invalid data")
    public void testUpdateSlaPolicyWithInvalidData() {
        Assumptions.assumeTrue(createdSlaPolicyId != null, 
            "SLA policy must be created first");
        
        Map<String, Object> invalidUpdateData = new HashMap<>();
        invalidUpdateData.put("responseTimeHours", -5.0);
        invalidUpdateData.put("resolutionTimeHours", -10.0);
        
        givenAdminRequest()
            .body(invalidUpdateData)
            .when()
                .patch(SLA_POLICIES_ENDPOINT + "/" + createdSlaPolicyId)
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(8)
    @DisplayName("GET /api/sla-policies/categories/{category} - Get policies by category")
    public void testGetSlaPoliciesByCategory() {
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT + "/categories/IT_SUPPORT")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .body("findAll { it.category == 'IT_SUPPORT' }.size()", 
                      greaterThanOrEqualTo(1))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(9)
    @DisplayName("GET /api/sla-policies/categories/{category} - Get policies by invalid category")
    public void testGetSlaPoliciesByInvalidCategory() {
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT + "/categories/INVALID_CATEGORY")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(10)
    @DisplayName("Test different category and priority combinations")
    public void testCreateSlaPoliciesForDifferentCombinations() {
        // Test FACILITIES + MEDIUM
        Map<String, Object> facilitiesPolicy = createSlaPolicyPayload(
            "FACILITIES", "MEDIUM", 8.0, 72.0);
        
        Response facilitiesResponse = givenAdminRequest()
            .body(facilitiesPolicy)
            .when()
                .post(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(201)
                .body("category", equalTo("FACILITIES"))
                .body("priority", equalTo("MEDIUM"))
                .log().ifValidationFails()
                .extract().response();
        
        Long facilitiesPolicyId = facilitiesResponse.jsonPath().getLong("id");
        
        // Test HR_REQUEST + CRITICAL
        Map<String, Object> hrPolicy = createSlaPolicyPayload(
            "HR_REQUEST", "CRITICAL", 1.0, 8.0);
        
        Response hrResponse = givenAdminRequest()
            .body(hrPolicy)
            .when()
                .post(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(201)
                .body("category", equalTo("HR_REQUEST"))
                .body("priority", equalTo("CRITICAL"))
                .log().ifValidationFails()
                .extract().response();
        
        Long hrPolicyId = hrResponse.jsonPath().getLong("id");
        
        // Clean up created policies
        if (facilitiesPolicyId != null) {
            givenAdminRequest()
                .when()
                    .delete(SLA_POLICIES_ENDPOINT + "/" + facilitiesPolicyId)
                .then()
                    .statusCode(anyOf(is(200), is(204)))
                    .log().ifValidationFails();
        }
        
        if (hrPolicyId != null) {
            givenAdminRequest()
                .when()
                    .delete(SLA_POLICIES_ENDPOINT + "/" + hrPolicyId)
                .then()
                    .statusCode(anyOf(is(200), is(204)))
                    .log().ifValidationFails();
        }
    }
    
    @Test
    @Order(11)
    @DisplayName("DELETE /api/sla-policies/{id} - Delete SLA policy")
    public void testDeleteSlaPolicy() {
        Assumptions.assumeTrue(createdSlaPolicyId != null, 
            "SLA policy must be created first");
        
        givenAdminRequest()
            .when()
                .delete(SLA_POLICIES_ENDPOINT + "/" + createdSlaPolicyId)
            .then()
                .statusCode(anyOf(is(200), is(204)))
                .log().ifValidationFails();
        
        // Verify deletion
        givenAdminRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT + "/" + createdSlaPolicyId)
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(12)
    @DisplayName("DELETE /api/sla-policies/{id} - Delete non-existent SLA policy")
    public void testDeleteNonExistentSlaPolicy() {
        givenAdminRequest()
            .when()
                .delete(SLA_POLICIES_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @DisplayName("Test unauthorized access to SLA policies")
    public void testUnauthorizedAccess() {
        givenJsonRequest()
            .when()
                .get(SLA_POLICIES_ENDPOINT)
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    /**
     * Helper method to create SLA policy payload
     */
    private Map<String, Object> createSlaPolicyPayload(String category, String priority, 
                                                      Double responseTime, Double resolutionTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("category", category);
        payload.put("priority", priority);
        payload.put("responseTimeHours", responseTime);
        payload.put("resolutionTimeHours", resolutionTime);
        return payload;
    }
    
    @Override
    protected String getAdminToken() {
        return TestConfig.getAdminToken();
    }
}