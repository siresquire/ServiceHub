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

@DisplayName("Department Admin API Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DepartmentApiTest extends BaseTest {

    private static Long createdDepartmentId;

    // CREATE DEPARTMENT TESTS

    @Test
    @DisplayName("Should successfully create a new department")
    public void testCreateDepartment() {
        Map<String, Object> departmentRequest = new HashMap<>();
        departmentRequest.put("name", TestData.NEW_DEPARTMENT_NAME);
        departmentRequest.put("category", TestData.REQUEST_CATEGORY_IT_SUPPORT);
        departmentRequest.put("contactEmail", TestData.NEW_DEPARTMENT_EMAIL);

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(departmentRequest)
                .when()
                .post(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(TestData.NEW_DEPARTMENT_NAME))
                .body("category", equalTo(TestData.REQUEST_CATEGORY_IT_SUPPORT))
                .extract()
                .response();

        createdDepartmentId = response.jsonPath().getLong("id");
        assertNotNull(createdDepartmentId, "Created department ID should not be null");

        System.out.println("Create Department Response: " + response.asString());
        System.out.println("Created Department ID: " + createdDepartmentId);
    }

    @Test
    @DisplayName("Should fail to create department with duplicate name")
    public void testCreateDuplicateDepartment() {
        Map<String, Object> departmentRequest = new HashMap<>();
        departmentRequest.put("name", TestData.DUPLICATE_DEPARTMENT_NAME);
        departmentRequest.put("category", TestData.REQUEST_CATEGORY_IT_SUPPORT);
        departmentRequest.put("contactEmail", "duplicate@servicehub.com");

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(departmentRequest)
                .when()
                .post(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(anyOf(is(400), is(409)))
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Duplicate Department Response: " + response.asString());
    }

    // GET DEPARTMENT TESTS

    @Test
    @DisplayName("Should get all departments")
    public void testGetAllDepartments() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].category", notNullValue())
                .extract()
                .response();

        System.out.println("Get All Departments Response: " + response.asString());
    }

    @Test
    @DisplayName("Should get department by ID")
    public void testGetDepartmentById() {
        // Use the IT department ID from test data
        Long departmentId = TestData.IT_DEPARTMENT_ID;

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_DEPARTMENTS + "/" + departmentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(departmentId.intValue()))
                .body("name", notNullValue())
                .body("category", notNullValue())
                .extract()
                .response();

        System.out.println("Get Department By ID Response: " + response.asString());
    }

    @Test
    @DisplayName("Should return 404 for non-existent department")
    public void testGetNonExistentDepartment() {
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_DEPARTMENTS + "/" + TestData.INVALID_ID)
                .then()
                .statusCode(404)
                .extract()
                .response();

        System.out.println("Non-existent Department Response: " + response.asString());
    }

    // UPDATE DEPARTMENT TESTS

    @Test
    @DisplayName("Should successfully update department")
    public void testUpdateDepartment() {
        // First create a department to update
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("name", "Test Update Department");
        createRequest.put("category", TestData.REQUEST_CATEGORY_FACILITIES);
        createRequest.put("contactEmail", "update@test.com");

        Response createResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(createRequest)
                .when()
                .post(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long departmentId = createResponse.jsonPath().getLong("id");

        // Now update it
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", TestData.UPDATED_DEPARTMENT_NAME);
        updateRequest.put("category", TestData.REQUEST_CATEGORY_HR_REQUEST);
        updateRequest.put("contactEmail", "updated@test.com");

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(updateRequest)
                .when()
                .put(ApiConfig.ADMIN_DEPARTMENTS + "/" + departmentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(departmentId.intValue()))
                .body("name", equalTo(TestData.UPDATED_DEPARTMENT_NAME))
                .body("category", equalTo(TestData.REQUEST_CATEGORY_HR_REQUEST))
                .extract()
                .response();

        System.out.println("Update Department Response: " + response.asString());
    }

    // TOGGLE DEPARTMENT TESTS

    @Test
    @DisplayName("Should successfully toggle department active status")
    public void testToggleDepartmentStatus() {
        // Use the IT department ID from test data
        Long departmentId = TestData.IT_DEPARTMENT_ID;

        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .put(ApiConfig.ADMIN_DEPARTMENTS + "/" + departmentId + "/toggle")
                .then()
                .statusCode(200)
                .body("id", equalTo(departmentId.intValue()))
                .body("name", notNullValue())
                .extract()
                .response();

        System.out.println("Toggle Department Response: " + response.asString());
    }

    // DELETE DEPARTMENT TESTS

    @Test
    @DisplayName("Should successfully delete department")
    public void testDeleteDepartment() {
        // First create a department to delete
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("name", "Test Delete Department");
        createRequest.put("category", TestData.REQUEST_CATEGORY_FACILITIES);
        createRequest.put("contactEmail", "delete@test.com");

        Response createResponse = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .body(createRequest)
                .when()
                .post(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long departmentId = createResponse.jsonPath().getLong("id");

        // Now delete it
        Response response = givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .delete(ApiConfig.ADMIN_DEPARTMENTS + "/" + departmentId)
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("deleted"))
                .extract()
                .response();

        System.out.println("Delete Department Response: " + response.asString());

        // Verify it's deleted
        givenAuthenticatedRequest(TokenManager.getAdminToken())
                .when()
                .get(ApiConfig.ADMIN_DEPARTMENTS + "/" + departmentId)
                .then()
                .statusCode(404);
    }

    // AUTHORIZATION TESTS

    @Test
    @DisplayName("Should fail with unauthorized access (no token)")
    public void testUnauthorizedAccess() {
        Response response = givenJsonRequest()
                .when()
                .get(ApiConfig.ADMIN_DEPARTMENTS)
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
                .get(ApiConfig.ADMIN_DEPARTMENTS)
                .then()
                .statusCode(403)
                .extract()
                .response();

        System.out.println("Forbidden Access Response: " + response.asString());
    }
}