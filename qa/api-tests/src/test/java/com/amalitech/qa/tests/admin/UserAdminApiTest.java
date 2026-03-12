package com.amalitech.qa.tests.admin;

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

@DisplayName("User Admin API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAdminApiTest extends BaseTest {

    private static Long testUserId;

    // GET USERS TESTS

    @Test
    @DisplayName("Should get all users")
    public void testGetAllUsers() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", notNullValue())
                .body("[0].fullName", notNullValue())
                .body("[0].email", notNullValue())
                .body("[0].role", notNullValue())
                .extract()
                .response();

        System.out.println("Get All Users Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get user by ID")
    public void testGetUserById() {
        // First get all users to find a valid user ID
        Response allUsersResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long userId = allUsersResponse.jsonPath().getLong("[0].id");
        testUserId = userId; // Store for later tests

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS + "/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.intValue()))
                .body("fullName", notNullValue())
                .body("email", notNullValue())
                .body("role", notNullValue())
                .extract()
                .response();

        System.out.println("Get User By ID Response: " + response.asString());
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    public void testGetNonExistentUser() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Non-existent User Response: " + response.asString());
    }

    // GET USERS BY ROLE TESTS

    @Test
    @DisplayName("Should get users by ADMIN role")
    public void testGetUsersByAdminRole() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "ADMIN")
                .when()
                .get(ApiConfig.ADMIN_USERS + "/by-role")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].role", equalTo("ADMIN"))
                .extract()
                .response();

        System.out.println("Get Users By ADMIN Role Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get users by AGENT role")
    public void testGetUsersByAgentRole() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "AGENT")
                .when()
                .get(ApiConfig.ADMIN_USERS + "/by-role")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get Users By AGENT Role Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get users by USER role")
    public void testGetUsersByUserRole() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "USER")
                .when()
                .get(ApiConfig.ADMIN_USERS + "/by-role")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(0)))
                .extract()
                .response();

        System.out.println("Get Users By USER Role Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail with invalid role parameter")
    public void testGetUsersByInvalidRole() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "INVALID_ROLE")
                .when()
                .get(ApiConfig.ADMIN_USERS + "/by-role")
                .then()
                .statusCode(400)
                .extract()
                .response();

        System.out.println("Invalid Role Response: " + response.asString());
    }

    // UPDATE USER ROLE TESTS

    @Test
    @DisplayName("Should successfully update user role from USER to AGENT")
    public void testUpdateUserRoleToAgent() {
        // First create a test user by registering
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", "Test Role User");
        registerRequest.put("email", "roletest" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", TestData.TEST_USER_PASSWORD);
        registerRequest.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response registerResponse = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Find the created user
        Response allUsersResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String testEmail = registerRequest.get("email").toString();
        Long userId = null;
        
        // Find user by email
        for (int i = 0; i < allUsersResponse.jsonPath().getList("$").size(); i++) {
            if (testEmail.equals(allUsersResponse.jsonPath().getString("[" + i + "].email"))) {
                userId = allUsersResponse.jsonPath().getLong("[" + i + "].id");
                break;
            }
        }

        assertNotNull(userId, "Test user should be found");

        // Update role to AGENT
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "AGENT")
                .queryParam("departmentId", TestData.IT_DEPARTMENT_ID)
                .when()
                .put(ApiConfig.ADMIN_USERS + "/" + userId + "/role")
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.intValue()))
                .body("role", equalTo("AGENT"))
                .body("department", notNullValue())
                .extract()
                .response();

        System.out.println("Update User Role to AGENT Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail to update role to AGENT without departmentId")
    public void testUpdateUserRoleToAgentWithoutDepartment() {
        // Use existing test user ID if available, otherwise use a known ID
        Long userId = testUserId != null ? testUserId : 1L;

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .queryParam("role", "AGENT")
                // Missing departmentId parameter
                .when()
                .put(ApiConfig.ADMIN_USERS + "/" + userId + "/role")
                .then()
                .statusCode(400)
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Update Role Without Department Response: " + response.asString());
    }

    // DELETE USER TESTS

    @Test
    @DisplayName("Should successfully delete a user")
    public void testDeleteUser() {
        // First create a test user to delete
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", "Test Delete User");
        registerRequest.put("email", "deletetest" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", TestData.TEST_USER_PASSWORD);
        registerRequest.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response registerResponse = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Find the created user
        Response allUsersResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String testEmail = registerRequest.get("email").toString();
        Long userId = null;
        
        // Find user by email
        for (int i = 0; i < allUsersResponse.jsonPath().getList("$").size(); i++) {
            if (testEmail.equals(allUsersResponse.jsonPath().getString("[" + i + "].email"))) {
                userId = allUsersResponse.jsonPath().getLong("[" + i + "].id");
                break;
            }
        }

        assertNotNull(userId, "Test user should be found");

        // Delete the user
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .delete(ApiConfig.ADMIN_USERS + "/" + userId)
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("deleted"))
                .extract()
                .response();

        System.out.println("Delete User Response: " + response.asString());

        // Verify user is deleted
        givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS + "/" + userId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should fail to delete ADMIN user")
    public void testDeleteAdminUser() {
        // Find an admin user
        Response allUsersResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Long adminUserId = null;
        
        // Find admin user
        for (int i = 0; i < allUsersResponse.jsonPath().getList("$").size(); i++) {
            if ("ADMIN".equals(allUsersResponse.jsonPath().getString("[" + i + "].role"))) {
                adminUserId = allUsersResponse.jsonPath().getLong("[" + i + "].id");
                break;
            }
        }

        assertNotNull(adminUserId, "Admin user should be found");

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .delete(ApiConfig.ADMIN_USERS + "/" + adminUserId)
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Delete Admin User Response: " + response.asString());
    }

    // AUTHORIZATION TESTS

    @Test
    @DisplayName("Should fail with unauthorized access (no token)")
    public void testUnauthorizedAccess() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.ADMIN_USERS)
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
                .get(ApiConfig.ADMIN_USERS)
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Response: " + response.asString());
    }
}