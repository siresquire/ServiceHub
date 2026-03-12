package com.amalitech.qa.tests.admin;

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
 * API tests for Admin endpoints
 */
@Epic("ServiceHub API Testing")
@Feature("Admin Management")
@TestMethodOrder(OrderAnnotation.class)
public class AdminApiTest extends BaseApiTest {
    
    private static Long createdDepartmentId;
    private static Long testUserId;
    private static final String ADMIN_USERS_ENDPOINT = "/api/admin/users";
    private static final String ADMIN_DEPARTMENTS_ENDPOINT = "/api/admin/departments";
    
    @BeforeAll
    public static void setupClass() {
        Assumptions.assumeTrue(TestConfig.hasAdminToken(), 
            "Admin token required for Admin API tests");
    }
    
    // ========== USER MANAGEMENT TESTS ==========
    
    @Test
    @Order(1)
    @Story("User Management")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test retrieving all users in the system with admin privileges")
    @DisplayName("GET /api/admin/users - Get all users")
    public void testGetAllUsers() {
        givenAdminRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].id", notNullValue())
                .body("[0].fullName", notNullValue())
                .body("[0].email", notNullValue())
                .body("[0].role", notNullValue())
                .log().ifValidationFails();
    }
    
    @Test
    @Order(2)
    @DisplayName("GET /api/admin/users/by-role - Get users by role")
    public void testGetUsersByRole() {
        // Test getting ADMIN users
        givenAdminRequest()
            .queryParam("role", "ADMIN")
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/by-role")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .body("findAll { it.role == 'ADMIN' }.size()", 
                      equalTo(Integer.valueOf("$")))
                .log().ifValidationFails();
        
        // Test getting USER role
        givenAdminRequest()
            .queryParam("role", "USER")
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/by-role")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
        
        // Test getting AGENT role
        givenAdminRequest()
            .queryParam("role", "AGENT")
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/by-role")
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(3)
    @DisplayName("GET /api/admin/users/by-role - Invalid role")
    public void testGetUsersByInvalidRole() {
        givenAdminRequest()
            .queryParam("role", "INVALID_ROLE")
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/by-role")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(4)
    @DisplayName("Create test user for role management tests")
    public void testCreateTestUserForRoleTests() {
        // Create a test user via registration endpoint
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "admintest" + uniqueId + "@example.com";
        
        Map<String, Object> registerData = new HashMap<>();
        registerData.put("name", "Admin Test User");
        registerData.put("email", testEmail);
        registerData.put("password", "password123");
        
        Response response = givenJsonRequest()
            .body(registerData)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(201)
                .extract().response();
        
        // Get the created user ID by searching all users
        Response usersResponse = givenAdminRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT)
            .then()
                .statusCode(200)
                .extract().response();
        
        testUserId = usersResponse.jsonPath()
            .getLong("find { it.email == '" + testEmail + "' }.id");
        
        Assertions.assertNotNull(testUserId, "Test user should be created successfully");
    }
    
    @Test
    @Order(5)
    @DisplayName("GET /api/admin/users/{id} - Get user by ID")
    public void testGetUserById() {
        Assumptions.assumeTrue(testUserId != null, "Test user must be created first");
        
        givenAdminRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/" + testUserId)
            .then()
                .statusCode(200)
                .body("id", equalTo(testUserId.intValue()))
                .body("fullName", equalTo("Admin Test User"))
                .body("role", equalTo("USER"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(6)
    @DisplayName("GET /api/admin/users/{id} - Get non-existent user")
    public void testGetNonExistentUser() {
        givenAdminRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(7)
    @Story("User Role Management")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test promoting user to AGENT role with department assignment")
    @DisplayName("PUT /api/admin/users/{id}/role - Update user role to AGENT")
    public void testUpdateUserRoleToAgent() {
        Assumptions.assumeTrue(testUserId != null, "Test user must be created first");
        Assumptions.assumeTrue(createdDepartmentId != null, "Department must be created first");
        
        givenAdminRequest()
            .queryParam("role", "AGENT")
            .queryParam("departmentId", createdDepartmentId)
            .when()
                .put(ADMIN_USERS_ENDPOINT + "/" + testUserId + "/role")
            .then()
                .statusCode(200)
                .body("id", equalTo(testUserId.intValue()))
                .body("role", equalTo("AGENT"))
                .body("department", notNullValue())
                .log().ifValidationFails();
    }
    
    @Test
    @Order(8)
    @DisplayName("PUT /api/admin/users/{id}/role - Update role without required departmentId")
    public void testUpdateUserRoleToAgentWithoutDepartment() {
        Assumptions.assumeTrue(testUserId != null, "Test user must be created first");
        
        givenAdminRequest()
            .queryParam("role", "AGENT")
            // Missing departmentId
            .when()
                .put(ADMIN_USERS_ENDPOINT + "/" + testUserId + "/role")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(9)
    @DisplayName("PUT /api/admin/users/{id}/role - Update user role back to USER")
    public void testUpdateUserRoleBackToUser() {
        Assumptions.assumeTrue(testUserId != null, "Test user must be created first");
        
        givenAdminRequest()
            .queryParam("role", "USER")
            .when()
                .put(ADMIN_USERS_ENDPOINT + "/" + testUserId + "/role")
            .then()
                .statusCode(200)
                .body("id", equalTo(testUserId.intValue()))
                .body("role", equalTo("USER"))
                .log().ifValidationFails();
    }
    
    // ========== DEPARTMENT MANAGEMENT TESTS ==========
    
    @Test
    @Order(10)
    @DisplayName("GET /api/admin/departments - Get all departments")
    public void testGetAllDepartments() {
        givenAdminRequest()
            .when()
                .get(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(11)
    @Story("Department Management")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test creating a new department with valid category and contact information")
    @DisplayName("POST /api/admin/departments - Create new department")
    public void testCreateDepartment() {
        Map<String, Object> departmentData = createDepartmentPayload(
            "Test IT Department", "IT_SUPPORT", "testit@example.com");
        
        Response response = givenAdminRequest()
            .body(departmentData)
            .when()
                .post(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Test IT Department"))
                .body("category", equalTo("IT_SUPPORT"))
                .body("contactEmail", equalTo("testit@example.com"))
                .body("active", equalTo(true))
                .log().ifValidationFails()
                .extract().response();
        
        createdDepartmentId = response.jsonPath().getLong("id");
        Assertions.assertNotNull(createdDepartmentId, "Created department ID should not be null");
    }
    
    @Test
    @Order(12)
    @DisplayName("POST /api/admin/departments - Create department with invalid data")
    public void testCreateDepartmentWithInvalidData() {
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("name", ""); // Empty name
        invalidData.put("category", "INVALID_CATEGORY");
        invalidData.put("contactEmail", "invalid-email");
        
        givenAdminRequest()
            .body(invalidData)
            .when()
                .post(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(13)
    @DisplayName("GET /api/admin/departments/{id} - Get department by ID")
    public void testGetDepartmentById() {
        Assumptions.assumeTrue(createdDepartmentId != null, "Department must be created first");
        
        givenAdminRequest()
            .when()
                .get(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId)
            .then()
                .statusCode(200)
                .body("id", equalTo(createdDepartmentId.intValue()))
                .body("name", equalTo("Test IT Department"))
                .body("category", equalTo("IT_SUPPORT"))
                .body("contactEmail", equalTo("testit@example.com"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(14)
    @DisplayName("GET /api/admin/departments/{id} - Get non-existent department")
    public void testGetNonExistentDepartment() {
        givenAdminRequest()
            .when()
                .get(ADMIN_DEPARTMENTS_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(15)
    @DisplayName("PUT /api/admin/departments/{id} - Update department")
    public void testUpdateDepartment() {
        Assumptions.assumeTrue(createdDepartmentId != null, "Department must be created first");
        
        Map<String, Object> updateData = createDepartmentPayload(
            "Updated IT Department", "IT_SUPPORT", "updated@example.com");
        
        givenAdminRequest()
            .body(updateData)
            .when()
                .put(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId)
            .then()
                .statusCode(200)
                .body("id", equalTo(createdDepartmentId.intValue()))
                .body("name", equalTo("Updated IT Department"))
                .body("contactEmail", equalTo("updated@example.com"))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(16)
    @DisplayName("PUT /api/admin/departments/{id}/toggle - Toggle department status")
    public void testToggleDepartmentStatus() {
        Assumptions.assumeTrue(createdDepartmentId != null, "Department must be created first");
        
        // First toggle - should deactivate
        Response response1 = givenAdminRequest()
            .when()
                .put(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId + "/toggle")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdDepartmentId.intValue()))
                .log().ifValidationFails()
                .extract().response();
        
        boolean firstToggleActive = response1.jsonPath().getBoolean("active");
        
        // Second toggle - should reactivate
        Response response2 = givenAdminRequest()
            .when()
                .put(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId + "/toggle")
            .then()
                .statusCode(200)
                .body("id", equalTo(createdDepartmentId.intValue()))
                .log().ifValidationFails()
                .extract().response();
        
        boolean secondToggleActive = response2.jsonPath().getBoolean("active");
        
        // Verify toggle worked
        Assertions.assertNotEquals(firstToggleActive, secondToggleActive, 
            "Department active status should toggle");
    }
    
    @Test
    @Order(17)
    @DisplayName("Test different department categories")
    public void testCreateDepartmentsWithDifferentCategories() {
        // Test FACILITIES department
        Map<String, Object> facilitiesData = createDepartmentPayload(
            "Facilities Department", "FACILITIES", "facilities@example.com");
        
        Response facilitiesResponse = givenAdminRequest()
            .body(facilitiesData)
            .when()
                .post(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(201)
                .body("category", equalTo("FACILITIES"))
                .log().ifValidationFails()
                .extract().response();
        
        Long facilitiesDeptId = facilitiesResponse.jsonPath().getLong("id");
        
        // Test HR_REQUEST department
        Map<String, Object> hrData = createDepartmentPayload(
            "HR Department", "HR_REQUEST", "hr@example.com");
        
        Response hrResponse = givenAdminRequest()
            .body(hrData)
            .when()
                .post(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(201)
                .body("category", equalTo("HR_REQUEST"))
                .log().ifValidationFails()
                .extract().response();
        
        Long hrDeptId = hrResponse.jsonPath().getLong("id");
        
        // Clean up created departments
        if (facilitiesDeptId != null) {
            givenAdminRequest()
                .when()
                    .delete(ADMIN_DEPARTMENTS_ENDPOINT + "/" + facilitiesDeptId)
                .then()
                    .statusCode(200)
                    .log().ifValidationFails();
        }
        
        if (hrDeptId != null) {
            givenAdminRequest()
                .when()
                    .delete(ADMIN_DEPARTMENTS_ENDPOINT + "/" + hrDeptId)
                .then()
                    .statusCode(200)
                    .log().ifValidationFails();
        }
    }
    
    @Test
    @Order(18)
    @DisplayName("DELETE /api/admin/users/{id} - Delete user")
    public void testDeleteUser() {
        Assumptions.assumeTrue(testUserId != null, "Test user must be created first");
        
        givenAdminRequest()
            .when()
                .delete(ADMIN_USERS_ENDPOINT + "/" + testUserId)
            .then()
                .statusCode(200)
                .body(equalTo("User deleted successfully"))
                .log().ifValidationFails();
        
        // Verify deletion
        givenAdminRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT + "/" + testUserId)
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(19)
    @DisplayName("DELETE /api/admin/departments/{id} - Delete department")
    public void testDeleteDepartment() {
        Assumptions.assumeTrue(createdDepartmentId != null, "Department must be created first");
        
        givenAdminRequest()
            .when()
                .delete(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId)
            .then()
                .statusCode(200)
                .body(equalTo("Department deleted successfully"))
                .log().ifValidationFails();
        
        // Verify deletion
        givenAdminRequest()
            .when()
                .get(ADMIN_DEPARTMENTS_ENDPOINT + "/" + createdDepartmentId)
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(20)
    @DisplayName("DELETE /api/admin/users/{id} - Delete non-existent user")
    public void testDeleteNonExistentUser() {
        givenAdminRequest()
            .when()
                .delete(ADMIN_USERS_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(21)
    @DisplayName("DELETE /api/admin/departments/{id} - Delete non-existent department")
    public void testDeleteNonExistentDepartment() {
        givenAdminRequest()
            .when()
                .delete(ADMIN_DEPARTMENTS_ENDPOINT + "/99999")
            .then()
                .statusCode(404)
                .log().ifValidationFails();
    }
    
    @Test
    @DisplayName("Test unauthorized access to admin endpoints")
    public void testUnauthorizedAccessToAdminEndpoints() {
        // Test without token
        givenJsonRequest()
            .when()
                .get(ADMIN_USERS_ENDPOINT)
            .then()
                .statusCode(401)
                .log().ifValidationFails();
        
        givenJsonRequest()
            .when()
                .get(ADMIN_DEPARTMENTS_ENDPOINT)
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    /**
     * Helper method to create department payload
     */
    private Map<String, Object> createDepartmentPayload(String name, String category, String contactEmail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("category", category);
        payload.put("contactEmail", contactEmail);
        return payload;
    }
    
    @Override
    protected String getAdminToken() {
        return TestConfig.getAdminToken();
    }
}