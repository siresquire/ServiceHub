package com.amalitech.qa.tests.requests;

import com.amalitech.qa.base.BaseApiTest;
import com.amalitech.qa.config.TestConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API tests for Service Request endpoints
 */
@Epic("ServiceHub API Testing")
@Feature("Service Requests")
@TestMethodOrder(OrderAnnotation.class)
public class ServiceRequestApiTest extends BaseApiTest {
    
    private static Long createdRequestId;
    private static Long testDepartmentId;
    private static Long testAgentId;
    private static String testUserToken;
    private static String testAgentToken;
    private static final String REQUESTS_ENDPOINT = "/api/requests";
    
    @BeforeAll
    public static void setupClass() {
        Assumptions.assumeTrue(TestConfig.hasAdminToken(), 
            "Admin token required for Service Request tests");
        
        // Setup test data: department, agent, and user
        setupTestData();
    }
    
    private static void setupTestData() {
        // Create test department
        Map<String, Object> departmentData = new HashMap<>();
        departmentData.put("name", "Test Service Department");
        departmentData.put("category", "IT_SUPPORT");
        departmentData.put("contactEmail", "testservice@example.com");
        
        Response deptResponse = given()
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "Bearer " + TestConfig.getAdminToken())
            .body(departmentData)
            .when()
                .post("/api/admin/departments")
            .then()
                .statusCode(201)
                .extract().response();
        
        testDepartmentId = deptResponse.jsonPath().getLong("id");
        
        // Create test user for requests
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testUserEmail = "requestuser" + uniqueId + "@example.com";
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "Request Test User");
        userData.put("email", testUserEmail);
        userData.put("password", "password123");
        userData.put("departmentId", testDepartmentId);
        
        Response userResponse = given()
            .contentType("application/json")
            .body(userData)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(201)
                .extract().response();
        
        testUserToken = userResponse.jsonPath().getString("token");
        
        // Create test agent
        String agentEmail = "requestagent" + uniqueId + "@example.com";
        Map<String, Object> agentData = new HashMap<>();
        agentData.put("name", "Request Test Agent");
        agentData.put("email", agentEmail);
        agentData.put("password", "password123");
        agentData.put("departmentId", testDepartmentId);
        
        Response agentRegResponse = given()
            .contentType("application/json")
            .body(agentData)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(201)
                .extract().response();
        
        testAgentToken = agentRegResponse.jsonPath().getString("token");
        
        // Get agent ID and promote to AGENT role
        Response usersResponse = given()
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "Bearer " + TestConfig.getAdminToken())
            .when()
                .get("/api/admin/users")
            .then()
                .statusCode(200)
                .extract().response();
        
        testAgentId = usersResponse.jsonPath()
            .getLong("find { it.email == '" + agentEmail + "' }.id");
        
        // Promote user to AGENT role
        given()
            .contentType("application/json")
            .accept("application/json")
            .header("Authorization", "Bearer " + TestConfig.getAdminToken())
            .queryParam("role", "AGENT")
            .queryParam("departmentId", testDepartmentId)
            .when()
                .put("/api/admin/users/" + testAgentId + "/role")
            .then()
                .statusCode(200);
        
        // Login agent again to get updated token with AGENT role
        Map<String, Object> agentLoginData = new HashMap<>();
        agentLoginData.put("email", agentEmail);
        agentLoginData.put("password", "password123");
        
        Response agentLoginResponse = given()
            .contentType("application/json")
            .body(agentLoginData)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract().response();
        
        testAgentToken = agentLoginResponse.jsonPath().getString("token");
    }
    
    @Test
    @Order(1)
    @Story("Create Service Request")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test creating a new service request with valid data")
    @DisplayName("POST /api/requests - Create new service request")
    public void testCreateServiceRequest() {
        Assumptions.assumeTrue(testUserToken != null, "Test user token must be available");
        
        Map<String, Object> requestData = createServiceRequestPayload(
            "Test Service Request", "This is a test request", "IT_SUPPORT", "HIGH", testDepartmentId);
        
        Response response = givenAuthenticatedRequest(testUserToken)
            .body(requestData)
            .when()
                .post(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo("Test Service Request"))
                .body("description", equalTo("This is a test request"))
                .body("category", equalTo("IT_SUPPORT"))
                .body("priority", equalTo("HIGH"))
                .body("status", equalTo("OPEN"))
                .body("requesterName", equalTo("Request Test User"))
                .body("departmentName", equalTo("Test Service Department"))
                .log().ifValidationFails()
                .extract().response();
        
        createdRequestId = response.jsonPath().getLong("id");
        Assertions.assertNotNull(createdRequestId, "Created request ID should not be null");
    }
    
    @Test
    @Order(2)
    @Story("Create Service Request")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test creating a service request with invalid data should return validation errors")
    @DisplayName("POST /api/requests - Create request with invalid data")
    public void testCreateServiceRequestWithInvalidData() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("title", ""); // Empty title
        invalidData.put("category", "INVALID_CATEGORY");
        invalidData.put("priority", "INVALID_PRIORITY");
        
        givenAuthenticatedRequest(testUserToken)
            .body(invalidData)
            .when()
                .post(REQUESTS_ENDPOINT)
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(3)
    @DisplayName("GET /api/requests/{id} - Get service request by ID")
    public void testGetServiceRequestById() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        
        givenAuthenticatedRequest(testUserToken)
            .when()
                .get(REQUESTS_ENDPOINT + "/" + createdRequestId)
            .then()
                .statusCode(200)
                .body("id", equalTo(createdRequestId.intValue()))
                .body("title", equalTo("Test Service Request"))
                .body("status", equalTo("OPEN"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(4)
    @DisplayName("GET /api/requests/{id} - Get non-existent request")
    public void testGetNonExistentServiceRequest() {
        givenAuthenticatedRequest(testUserToken)
            .when()
                .get(REQUESTS_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(5)
    @DisplayName("GET /api/requests - Get requests filtered by USER role")
    public void testGetRequestsAsUser() {
        givenAuthenticatedRequest(testUserToken)
            .when()
                .get(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .body("findAll { it.requesterName == 'Request Test User' }.size()", 
                      greaterThanOrEqualTo(1))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(6)
    @DisplayName("GET /api/requests/my-requests - Get user's own requests")
    public void testGetMyRequests() {
        givenAuthenticatedRequest(testUserToken)
            .when()
                .get(REQUESTS_ENDPOINT + "/my-requests")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .body("findAll { it.requesterName == 'Request Test User' }.size()", 
                      greaterThanOrEqualTo(1))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(7)
    @Story("Assign Service Request")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test agent self-assignment functionality for service requests")
    @DisplayName("PUT /api/requests/{id}/assign - Agent assigns request to themselves")
    public void testAgentAssignRequestToSelf() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        Assumptions.assumeTrue(testAgentToken != null, "Test agent token must be available");
        
        givenAuthenticatedRequest(testAgentToken)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + createdRequestId + "/assign")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdRequestId.intValue()))
                .body("status", equalTo("ASSIGNED"))
                .body("assignedToName", equalTo("Request Test Agent"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(8)
    @DisplayName("PUT /api/requests/{id}/assign/{agentId} - Admin assigns request to specific agent")
    public void testAdminAssignRequestToSpecificAgent() {
        // Create another request for this test
        Map<String, Object> requestData = createServiceRequestPayload(
            "Admin Assignment Test", "Test admin assignment", "IT_SUPPORT", "MEDIUM", testDepartmentId);
        
        Response createResponse = givenAuthenticatedRequest(testUserToken)
            .body(requestData)
            .when()
                .post(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .extract().response();
        
        Long newRequestId = createResponse.jsonPath().getLong("id");
        
        // Admin assigns to specific agent
        givenAdminRequest()
            .when()
                .put(REQUESTS_ENDPOINT + "/" + newRequestId + "/assign/" + testAgentId)
            .then()
                .statusCode(200)
                .body("id", equalTo(newRequestId.intValue()))
                .body("status", equalTo("ASSIGNED"))
                .body("assignedToName", equalTo("Request Test Agent"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(9)
    @Story("Update Service Request Status")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test updating service request status from ASSIGNED to IN_PROGRESS")
    @DisplayName("PUT /api/requests/{id}/status - Update request status to IN_PROGRESS")
    public void testUpdateRequestStatusToInProgress() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", "IN_PROGRESS");
        statusUpdate.put("comment", "Starting work on this request");
        
        givenAuthenticatedRequest(testAgentToken)
            .body(statusUpdate)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + createdRequestId + "/status")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdRequestId.intValue()))
                .body("status", equalTo("IN_PROGRESS"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(10)
    @DisplayName("PUT /api/requests/{id}/status - Update request status to RESOLVED")
    public void testUpdateRequestStatusToResolved() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", "RESOLVED");
        statusUpdate.put("comment", "Issue has been resolved");
        
        givenAuthenticatedRequest(testAgentToken)
            .body(statusUpdate)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + createdRequestId + "/status")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdRequestId.intValue()))
                .body("status", equalTo("RESOLVED"))
                .body("resolved", equalTo(true))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(11)
    @DisplayName("PUT /api/requests/{id}/status - Update request status to CLOSED")
    public void testUpdateRequestStatusToClosed() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("newStatus", "CLOSED");
        statusUpdate.put("comment", "Request closed by user");
        
        givenAuthenticatedRequest(testUserToken)
            .body(statusUpdate)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + createdRequestId + "/status")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdRequestId.intValue()))
                .body("status", equalTo("CLOSED"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(12)
    @DisplayName("PUT /api/requests/{id}/status - Invalid status transition")
    public void testInvalidStatusTransition() {
        // Create a new request for this test
        Map<String, Object> requestData = createServiceRequestPayload(
            "Status Test Request", "Test status transitions", "IT_SUPPORT", "LOW", testDepartmentId);
        
        Response createResponse = givenAuthenticatedRequest(testUserToken)
            .body(requestData)
            .when()
                .post(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .extract().response();
        
        Long newRequestId = createResponse.jsonPath().getLong("id");
        
        // Try invalid transition (OPEN -> CLOSED without going through RESOLVED)
        Map<String, Object> invalidStatusUpdate = new HashMap<>();
        invalidStatusUpdate.put("newStatus", "CLOSED");
        
        givenAuthenticatedRequest(testUserToken)
            .body(invalidStatusUpdate)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + newRequestId + "/status")
            .then()
                .statusCode(anyOf(is(400), is(422))) // Bad request or unprocessable entity
                .log().ifValidationFails();
    }
    
    @Test
    @Order(13)
    @DisplayName("GET /api/requests - Get requests filtered by AGENT role")
    public void testGetRequestsAsAgent() {
        givenAuthenticatedRequest(testAgentToken)
            .when()
                .get(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(14)
    @DisplayName("GET /api/requests - Get requests filtered by ADMIN role")
    public void testGetRequestsAsAdmin() {
        givenAdminRequest()
            .when()
                .get(REQUESTS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(15)
    @DisplayName("GET /api/requests/all - Admin get all requests paginated")
    public void testGetAllRequestsPaginated() {
        givenAdminRequest()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
                .get(REQUESTS_ENDPOINT + "/all")
            .then()
                .statusCode(200)
                .body("content", isA(java.util.List.class))
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("size", equalTo(10))
                .body("number", equalTo(0))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(16)
    @DisplayName("Test different priority levels")
    public void testCreateRequestsWithDifferentPriorities() {
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        
        for (String priority : priorities) {
            Map<String, Object> requestData = createServiceRequestPayload(
                "Priority Test - " + priority, 
                "Testing " + priority + " priority", 
                "IT_SUPPORT", 
                priority, 
                testDepartmentId);
            
            givenAuthenticatedRequest(testUserToken)
                .body(requestData)
                .when()
                    .post(REQUESTS_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body("priority", equalTo(priority))
                    .log().ifValidationFails();
        }
    }
    
    @Test
    @Order(17)
    @DisplayName("Test different categories")
    public void testCreateRequestsWithDifferentCategories() {
        String[] categories = {"IT_SUPPORT", "FACILITIES", "HR_REQUEST"};
        
        for (String category : categories) {
            Map<String, Object> requestData = createServiceRequestPayload(
                "Category Test - " + category, 
                "Testing " + category + " category", 
                category, 
                "MEDIUM", 
                testDepartmentId);
            
            givenAuthenticatedRequest(testUserToken)
                .body(requestData)
                .when()
                    .post(REQUESTS_ENDPOINT)
                .then()
                    .statusCode(200)
                    .body("category", equalTo(category))
                    .log().ifValidationFails();
        }
    }
    
    @Test
    @DisplayName("Test unauthorized access to service requests")
    public void testUnauthorizedAccess() {
        givenJsonRequest()
            .when()
                .get(REQUESTS_ENDPOINT)
            .then()
                .statusCode(401)
                .log().ifValidationFails();
        
        givenJsonRequest()
            .when()
                .post(REQUESTS_ENDPOINT)
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    @Test
    @DisplayName("Test user cannot assign requests")
    public void testUserCannotAssignRequests() {
        Assumptions.assumeTrue(createdRequestId != null, "Service request must be created first");
        
        givenAuthenticatedRequest(testUserToken)
            .when()
                .put(REQUESTS_ENDPOINT + "/" + createdRequestId + "/assign")
            .then()
                .statusCode(403) // Forbidden - user role cannot assign
                .log().ifValidationFails();
    }
    
    @AfterAll
    public static void cleanup() {
        // Clean up test data
        if (testDepartmentId != null) {
            try {
                given()
                    .contentType("application/json")
                    .accept("application/json")
                    .header("Authorization", "Bearer " + TestConfig.getAdminToken())
                    .when()
                        .delete("/api/admin/departments/" + testDepartmentId)
                    .then()
                        .statusCode(anyOf(is(200), is(404)));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Helper method to create service request payload
     */
    private Map<String, Object> createServiceRequestPayload(String title, String description, 
                                                           String category, String priority, Long departmentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("category", category);
        payload.put("priority", priority);
        if (departmentId != null) {
            payload.put("departmentId", departmentId);
        }
        return payload;
    }
    
    @Override
    protected String getAdminToken() {
        return TestConfig.getAdminToken();
    }
}