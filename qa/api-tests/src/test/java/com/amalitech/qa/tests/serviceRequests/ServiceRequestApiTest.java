package com.amalitech.qa.tests.serviceRequests;

import com.amalitech.qa.base.BaseTest;
import com.amalitech.qa.config.ApiConfig;
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

@DisplayName("Service Request API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceRequestApiTest extends BaseTest {

    private static Long createdRequestId;

    // CREATE REQUEST TESTS

    @Test
    @DisplayName("Should successfully create a new service request")
    public void testCreateServiceRequest() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", TestData.REQUEST_TITLES[0]);
        requestData.put("description", TestData.REQUEST_DESCRIPTIONS[0]);
        requestData.put("category", TestData.CATEGORY_HARDWARE);
        requestData.put("priority", TestData.PRIORITY_HIGH);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo(TestData.REQUEST_TITLES[0]))
                .body("description", equalTo(TestData.REQUEST_DESCRIPTIONS[0]))
                .body("category", equalTo(TestData.CATEGORY_HARDWARE))
                .body("priority", equalTo(TestData.PRIORITY_HIGH))
                .body("status", equalTo(TestData.STATUS_OPEN))
                .body("requesterName", notNullValue())
                .body("departmentName", notNullValue())
                .body("createdAt", notNullValue())
                .extract()
                .response();

        createdRequestId = response.jsonPath().getLong("id");
        assertNotNull(createdRequestId, "Created request ID should not be null");

        System.out.println("Create Service Request Response: " + response.asString());
        System.out.println("Created Request ID: " + createdRequestId);
    }

    @Test
    @DisplayName("Should fail to create request with missing required fields")
    public void testCreateRequestMissingFields() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("description", "Missing title and other required fields");
        // Missing title, category, priority

        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(400)
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Missing Fields Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to create request with invalid department")
    public void testCreateRequestInvalidDepartment() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Request Invalid Dept");
        requestData.put("description", "Testing invalid department");
        requestData.put("category", TestData.CATEGORY_SOFTWARE);
        requestData.put("priority", TestData.PRIORITY_MEDIUM);
        requestData.put("departmentId", TestData.INVALID_ID);

        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(anyOf(is(400), is(404)))
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Invalid Department Response: " + response.asString());
    }

    // FETCH REQUESTS TESTS

    @Test
    @DisplayName("Should get all requests (role-based)")
    public void testGetAllRequests() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get All Requests Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get all requests paginated (admin only)")
    public void testGetAllRequestsPaginated() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get(ApiConfig.SERVICE_REQUESTS + "/all")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("size", equalTo(5))
                .body("number", equalTo(0))
                .extract()
                .response();

        System.out.println("Get Paginated Requests Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get my requests")
    public void testGetMyRequests() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.SERVICE_REQUESTS + "/my-requests")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get My Requests Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get request by ID")
    public void testGetRequestById() {
        // First create a request to get
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Get By ID");
        requestData.put("description", "Testing get by ID functionality");
        requestData.put("category", TestData.CATEGORY_NETWORK);
        requestData.put("priority", TestData.PRIORITY_LOW);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Now get it by ID
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.SERVICE_REQUESTS + "/" + requestId)
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("title", equalTo("Test Get By ID"))
                .body("status", notNullValue())
                .body("createdAt", notNullValue())
                .extract()
                .response();

        System.out.println("Get Request By ID Response: " + response.asString());
    }

    @Test
    @DisplayName("Should return 404 for non-existent request")
    public void testGetNonExistentRequest() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.SERVICE_REQUESTS + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Non-existent Request Response: " + response.asString());
    }

    // ASSIGNMENT TESTS

    @Test
    @DisplayName("Should assign request to current agent")
    public void testAssignRequestToCurrentAgent() {
        // First create a request
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Assign to Me");
        requestData.put("description", "Testing assignment to current agent");
        requestData.put("category", TestData.CATEGORY_SOFTWARE);
        requestData.put("priority", TestData.PRIORITY_MEDIUM);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Assign to current agent
        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + requestId + "/assign")
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("status", equalTo(TestData.STATUS_IN_PROGRESS))
                .body("assignedToName", notNullValue())
                .extract()
                .response();

        System.out.println("Assign to Current Agent Response: " + response.asString());
    }

    @Test
    @DisplayName("Should assign request to specific agent (admin only)")
    public void testAssignRequestToSpecificAgent() {
        // First create a request
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Assign to Specific Agent");
        requestData.put("description", "Testing assignment to specific agent");
        requestData.put("category", TestData.CATEGORY_ACCESS);
        requestData.put("priority", TestData.PRIORITY_HIGH);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Get an agent ID (assuming agent exists with ID 2)
        Long agentId = 2L;

        // Assign to specific agent (admin only)
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + requestId + "/assign/" + agentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("status", equalTo(TestData.STATUS_IN_PROGRESS))
                .extract()
                .response();

        System.out.println("Assign to Specific Agent Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to assign non-existing request")
    public void testAssignNonExistingRequest() {
        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + TestData.INVALID_ID + "/assign")
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Assign Non-existing Request Response: " + response.asString());
    }

    // STATUS UPDATE TESTS

    @Test
    @DisplayName("Should update request status with valid transition")
    public void testUpdateRequestStatusValidTransition() {
        // First create a request
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Status Update");
        requestData.put("description", "Testing status update functionality");
        requestData.put("category", TestData.CATEGORY_OTHER);
        requestData.put("priority", TestData.PRIORITY_MEDIUM);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Update status from OPEN to IN_PROGRESS
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", TestData.STATUS_IN_PROGRESS);
        statusUpdate.put("comment", "Starting work on this request");

        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .body(statusUpdate)
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + requestId + "/status")
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("status", equalTo(TestData.STATUS_IN_PROGRESS))
                .body("updatedAt", notNullValue())
                .extract()
                .response();

        System.out.println("Update Status Response: " + response.asString());
    }

    @Test
    @DisplayName("Should update request status to RESOLVED")
    public void testUpdateRequestStatusToResolved() {
        // First create a request
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Resolve Request");
        requestData.put("description", "Testing resolve functionality");
        requestData.put("category", TestData.CATEGORY_HARDWARE);
        requestData.put("priority", TestData.PRIORITY_LOW);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Update status to RESOLVED
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", TestData.STATUS_RESOLVED);
        statusUpdate.put("comment", "Issue has been resolved");

        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .body(statusUpdate)
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + requestId + "/status")
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("status", equalTo(TestData.STATUS_RESOLVED))
                .body("resolved", equalTo(true))
                .body("resolvedAt", notNullValue())
                .extract()
                .response();

        System.out.println("Resolve Request Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to update status with invalid transition")
    public void testUpdateRequestStatusInvalidTransition() {
        // First create a request
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Invalid Transition");
        requestData.put("description", "Testing invalid status transition");
        requestData.put("category", TestData.CATEGORY_SOFTWARE);
        requestData.put("priority", TestData.PRIORITY_HIGH);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response createResponse = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long requestId = createResponse.jsonPath().getLong("id");

        // Try invalid transition from OPEN to CLOSED (should go through RESOLVED first)
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", TestData.STATUS_CLOSED);
        statusUpdate.put("comment", "Trying invalid transition");

        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .body(statusUpdate)
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + requestId + "/status")
                .then()
                .statusCode(anyOf(is(400), is(422)))
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Invalid Transition Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to update status for non-existing request")
    public void testUpdateStatusNonExistingRequest() {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", TestData.STATUS_IN_PROGRESS);
        statusUpdate.put("comment", "Trying to update non-existing request");

        Response response = givenAuthenticatedRequest(TokenManager.getAgentToken())
                .body(statusUpdate)
                .when()
                .put(ApiConfig.SERVICE_REQUESTS + "/" + TestData.INVALID_ID + "/status")
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Update Non-existing Request Response: " + response.asString());
    }

    // SLA AND ADDITIONAL FIELD VALIDATION TESTS

    @Test
    @DisplayName("Should validate SLA related fields in response")
    public void testSlaFieldsValidation() {
        // Create a high priority request to test SLA fields
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test SLA Fields");
        requestData.put("description", "Testing SLA deadline fields");
        requestData.put("category", TestData.CATEGORY_HARDWARE);
        requestData.put("priority", TestData.PRIORITY_CRITICAL);
        requestData.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .body(requestData)
                .when()
                .post(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("priority", equalTo(TestData.PRIORITY_CRITICAL))
                .body("slaBreached", notNullValue())
                .body("responseSlaDeadline", notNullValue())
                .body("resolutionSlaDeadline", notNullValue())
                .extract()
                .response();

        System.out.println("SLA Fields Validation Response: " + response.asString());
    }

    // AUTHORIZATION TESTS

    @Test
    @DisplayName("Should fail with unauthorized access (no token)")
    public void testUnauthorizedAccess() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.SERVICE_REQUESTS)
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Unauthorized Access Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail admin-only endpoint with non-admin user")
    public void testForbiddenAccessPaginatedRequests() {
        Response response = givenAuthenticatedRequest(TokenManager.getCustomerToken())
                .when()
                .get(ApiConfig.SERVICE_REQUESTS + "/all")
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Response: " + response.asString());
    }
}