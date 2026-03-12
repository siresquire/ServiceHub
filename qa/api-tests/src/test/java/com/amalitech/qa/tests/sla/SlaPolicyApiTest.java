package com.amalitech.qa.tests.sla;

import com.amalitech.qa.base.BaseTest;
import com.amalitech.qa.config.ApiConfig;
import com.amalitech.qa.data.SlaPolicyTestData;
import com.amalitech.qa.data.TestData;
import com.amalitech.qa.utils.TokenManager;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SLA Policy API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SlaPolicyApiTest extends BaseTest {

    private static Long createdPolicyId;

    // CREATE SLA POLICY TESTS

    @Test
    @DisplayName("Should successfully create a new SLA policy")
    public void testCreateSlaPolicy() {
        Map<String, Object> policyData = SlaPolicyTestData.createValidSlaPolicyRequest();

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("created"))
                .body("data.id", notNullValue())
                .body("data.category", equalTo(SlaPolicyTestData.RequestCategory.IT_SUPPORT.name()))
                .body("data.priority", equalTo(SlaPolicyTestData.Priority.HIGH.name()))
                .body("data.responseTimeHours", equalTo((float) SlaPolicyTestData.SLA_RESPONSE_TIME_HOURS))
                .body("data.resolutionTimeHours", equalTo((float) SlaPolicyTestData.SLA_RESOLUTION_TIME_HOURS))
                .extract()
                .response();

        createdPolicyId = response.jsonPath().getLong("data.id");
        assertNotNull(createdPolicyId, "Created policy ID should not be null");

        System.out.println("Create SLA Policy Response: " + response.asString());
        System.out.println("Created Policy ID: " + createdPolicyId);
    }

    @Test
    @DisplayName("Should fail to create duplicate SLA policy")
    public void testCreateDuplicateSlaPolicy() {
        // First create a policy
        Map<String, Object> policyData = SlaPolicyTestData.createDuplicateSlaPolicyRequest();

        givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200);

        // Try to create the same policy again (duplicate)
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(400)
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Duplicate SLA Policy Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to create SLA policy with invalid data")
    public void testCreateSlaPolicyInvalidData() {
        Map<String, Object> policyData = SlaPolicyTestData.createInvalidSlaPolicyRequest();

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(400)
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Invalid SLA Policy Data Response: " + response.asString());
    }

    // GET SLA POLICIES TESTS

    @Test
    @DisplayName("Should get all SLA policies paginated")
    public void testGetAllSlaPoliciesPaginated() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("retrieved"))
                .body("data.content", notNullValue())
                .body("data.totalElements", greaterThanOrEqualTo(0))
                .body("data.size", equalTo(10))
                .body("data.number", equalTo(0))
                .extract()
                .response();

        System.out.println("Get All SLA Policies Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get SLA policy by ID")
    public void testGetSlaPolicyById() {
        // First create a policy to retrieve
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("category", TestData.REQUEST_CATEGORY_IT_SUPPORT);
        policyData.put("priority", TestData.PRIORITY_CRITICAL);
        policyData.put("responseTimeHours", 1.0);
        policyData.put("resolutionTimeHours", 4.0);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long policyId = createResponse.jsonPath().getLong("data.id");

        // Now get it by ID
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/" + policyId)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("retrieved"))
                .body("data.id", equalTo(policyId.intValue()))
                .body("data.category", equalTo(TestData.REQUEST_CATEGORY_IT_SUPPORT))
                .body("data.priority", equalTo(TestData.PRIORITY_CRITICAL))
                .body("data.responseTimeHours", equalTo(1.0f))
                .body("data.resolutionTimeHours", equalTo(4.0f))
                .extract()
                .response();

        System.out.println("Get SLA Policy By ID Response: " + response.asString());
    }

    @Test
    @DisplayName("Should return 404 for non-existent SLA policy")
    public void testGetNonExistentSlaPolicy() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Non-existent SLA Policy Response: " + response.asString());
    }

    // GET SLA POLICIES BY CATEGORY TESTS

    @Test
    @DisplayName("Should get SLA policies by IT_SUPPORT category")
    public void testGetSlaPoliciesByItSupportCategory() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/categories/" + TestData.REQUEST_CATEGORY_IT_SUPPORT)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("retrieved"))
                .body("data", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get SLA Policies by IT_SUPPORT Category Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get SLA policies by FACILITIES category")
    public void testGetSlaPoliciesByFacilitiesCategory() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/categories/" + TestData.REQUEST_CATEGORY_FACILITIES)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("retrieved"))
                .body("data", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get SLA Policies by FACILITIES Category Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get SLA policies by HR_REQUEST category")
    public void testGetSlaPoliciesByHrRequestCategory() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/categories/" + TestData.REQUEST_CATEGORY_HR_REQUEST)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("retrieved"))
                .body("data", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get SLA Policies by HR_REQUEST Category Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with invalid category")
    public void testGetSlaPoliciesByInvalidCategory() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/categories/INVALID_CATEGORY")
                .then()
                .statusCode(400)
                .extract()
                .response();

        System.out.println("Invalid Category Response: " + response.asString());
    }

    // UPDATE SLA POLICY TESTS

    @Test
    @DisplayName("Should successfully update SLA policy")
    public void testUpdateSlaPolicy() {
        // First create a policy to update
        Map<String, Object> createData = new HashMap<>();
        createData.put("category", TestData.REQUEST_CATEGORY_IT_SUPPORT);
        createData.put("priority", TestData.PRIORITY_LOW);
        createData.put("responseTimeHours", 12.0);
        createData.put("resolutionTimeHours", 72.0);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(createData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long policyId = createResponse.jsonPath().getLong("data.id");

        // Now update it
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("responseTimeHours", 6.0);
        updateData.put("resolutionTimeHours", 36.0);

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(updateData)
                .when()
                .patch(ApiConfig.SLA_POLICIES + "/" + policyId)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("updated"))
                .body("data.id", equalTo(policyId.intValue()))
                .body("data.responseTimeHours", equalTo(6.0f))
                .body("data.resolutionTimeHours", equalTo(36.0f))
                .extract()
                .response();

        System.out.println("Update SLA Policy Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to update SLA policy with invalid data")
    public void testUpdateSlaPolicyInvalidData() {
        // Use existing policy ID if available, otherwise use a known ID
        Long policyId = createdPolicyId != null ? createdPolicyId : 1L;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("responseTimeHours", -2.0); // Invalid negative value
        updateData.put("resolutionTimeHours", -10.0); // Invalid negative value

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(updateData)
                .when()
                .patch(ApiConfig.SLA_POLICIES + "/" + policyId)
                .then()
                .statusCode(400)
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Update SLA Policy Invalid Data Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to update non-existent SLA policy")
    public void testUpdateNonExistentSlaPolicy() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("responseTimeHours", 4.0);
        updateData.put("resolutionTimeHours", 24.0);

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(updateData)
                .when()
                .patch(ApiConfig.SLA_POLICIES + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Update Non-existent SLA Policy Response: " + response.asString());
    }

    // DELETE SLA POLICY TESTS

    @Test
    @DisplayName("Should successfully delete SLA policy")
    public void testDeleteSlaPolicy() {
        // First create a policy to delete
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("category", TestData.REQUEST_CATEGORY_FACILITIES);
        policyData.put("priority", TestData.PRIORITY_HIGH);
        policyData.put("responseTimeHours", 2.0);
        policyData.put("resolutionTimeHours", 12.0);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long policyId = createResponse.jsonPath().getLong("data.id");

        // Now delete it
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .delete(ApiConfig.SLA_POLICIES + "/" + policyId)
                .then()
                .statusCode(200)
                .body("message", containsStringIgnoringCase("deleted"))
                .extract()
                .response();

        System.out.println("Delete SLA Policy Response: " + response.asString());

        // Verify it's deleted
        givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SLA_POLICIES + "/" + policyId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should fail to delete non-existent SLA policy")
    public void testDeleteNonExistentSlaPolicy() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .delete(ApiConfig.SLA_POLICIES + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Delete Non-existent SLA Policy Response: " + response.asString());
    }

    // AUTHORIZATION TESTS

    @Test
    @DisplayName("Should fail with unauthorized access (no token)")
    public void testUnauthorizedAccess() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Unauthorized Access Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with forbidden access (non-admin user)")
    public void testForbiddenAccess() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to create SLA policy with non-admin user")
    public void testForbiddenCreateAccess() {
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("category", TestData.REQUEST_CATEGORY_IT_SUPPORT);
        policyData.put("priority", TestData.PRIORITY_MEDIUM);
        policyData.put("responseTimeHours", 4.0);
        policyData.put("resolutionTimeHours", 24.0);

        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(policyData)
                .when()
                .post(ApiConfig.SLA_POLICIES)
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Create Access Response: " + response.asString());
    }
}