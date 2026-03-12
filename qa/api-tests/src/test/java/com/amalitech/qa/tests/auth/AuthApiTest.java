package com.amalitech.qa.tests.auth;

import com.amalitech.qa.base.BaseTest;
import com.amalitech.qa.config.ApiConfig;
import com.amalitech.qa.data.TestData;
import com.amalitech.qa.utils.TokenManager;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication API Tests")
public class AuthApiTest extends BaseTest {

    // REGISTER TESTS
    
    @Test
    @DisplayName("Should successfully register a new user")
    public void testSuccessfulRegistration() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", TestData.TEST_USER_FIRST_NAME + " " + TestData.TEST_USER_LAST_NAME);
        registerRequest.put("email", "newuser" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", TestData.TEST_USER_PASSWORD);
        registerRequest.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response response = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo(registerRequest.get("email")))
                .body("role", notNullValue())
                .body("fullName", notNullValue())
                .extract()
                .response();

        System.out.println("Registration Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    public void testDuplicateEmailRegistration() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", TestData.TEST_USER_FIRST_NAME + " " + TestData.TEST_USER_LAST_NAME);
        registerRequest.put("email", TestData.ADMIN_EMAIL); // Using existing admin email
        registerRequest.put("password", TestData.TEST_USER_PASSWORD);
        registerRequest.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response response = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(anyOf(is(400), is(409))) // Accept both 400 and 409 for duplicate
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Duplicate Email Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail registration with invalid password (less than 6 chars)")
    public void testInvalidPasswordRegistration() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", TestData.TEST_USER_FIRST_NAME + " " + TestData.TEST_USER_LAST_NAME);
        registerRequest.put("email", "shortpass" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", "12345"); // Less than 6 characters
        registerRequest.put("departmentId", TestData.IT_DEPARTMENT_ID);

        Response response = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(anyOf(is(400), is(500))) // Accept both 400 and 500
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Invalid Password Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail registration with missing required fields")
    public void testMissingRequiredFieldsRegistration() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", "incomplete" + System.currentTimeMillis() + "@example.com");
        // Missing name, password, departmentId

        Response response = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(anyOf(is(400), is(500))) // Accept both 400 and 500 for now
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Missing Fields Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail registration with invalid departmentId")
    public void testInvalidDepartmentIdRegistration() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("name", TestData.TEST_USER_FIRST_NAME + " " + TestData.TEST_USER_LAST_NAME);
        registerRequest.put("email", "invaliddept" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", TestData.TEST_USER_PASSWORD);
        registerRequest.put("departmentId", TestData.INVALID_ID); // Invalid department ID

        Response response = givenJsonRequest()
                .body(registerRequest)
                .when()
                .post(ApiConfig.AUTH_REGISTER)
                .then()
                .statusCode(anyOf(is(400), is(404))) // Accept both 400 and 404 for invalid department
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Invalid Department Response: " + response.asString());
    }

    // LOGIN TESTS

    @Test
    @DisplayName("Should successfully login with valid credentials")
    public void testSuccessfulLogin() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", TestData.ADMIN_EMAIL);
        loginRequest.put("password", TestData.ADMIN_PASSWORD);

        Response response = givenJsonRequest()
                .body(loginRequest)
                .when()
                .post(ApiConfig.AUTH_LOGIN)
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo(TestData.ADMIN_EMAIL))
                .body("role", notNullValue())
                .body("fullName", notNullValue())
                .extract()
                .response();

        String token = response.jsonPath().getString("token");
        assertNotNull(token, "JWT token should not be null");
        assertTrue(token.length() > 0, "JWT token should not be empty");

        System.out.println("Login Response: " + response.asString());
        System.out.println("Extracted Token: " + token);
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    public void testLoginWithWrongPassword() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", TestData.ADMIN_EMAIL);
        loginRequest.put("password", "wrongpassword");

        Response response = givenJsonRequest()
                .body(loginRequest)
                .when()
                .post(ApiConfig.AUTH_LOGIN)
                .then()
                .statusCode(401)
                .body("message", containsStringIgnoringCase("invalid"))
                .extract()
                .response();

        System.out.println("Wrong Password Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail login with non-existing email")
    public void testLoginWithNonExistingEmail() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "nonexistent@example.com");
        loginRequest.put("password", TestData.ADMIN_PASSWORD);

        Response response = givenJsonRequest()
                .body(loginRequest)
                .when()
                .post(ApiConfig.AUTH_LOGIN)
                .then()
                .statusCode(401)
                .body("message", containsStringIgnoringCase("invalid"))
                .extract()
                .response();

        System.out.println("Non-existing Email Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail login with missing request body")
    public void testLoginWithMissingRequestBody() {
        Response response = givenJsonRequest()
                .when()
                .post(ApiConfig.AUTH_LOGIN)
                .then()
                .statusCode(anyOf(is(400), is(500))) // Accept both 400 and 500
                .body("message", notNullValue())
                .extract()
                .response();

        System.out.println("Missing Request Body Response: " + response.asString());
    }

    // LOGOUT TESTS

    @Test
    @DisplayName("Should successfully logout with valid token")
    public void testLogoutWithValidToken() {
        String token = TokenManager.getAdminToken();

        Response response = givenAuthenticatedRequest(token)
                .when()
                .post(ApiConfig.AUTH_LOGOUT)
                .then()
                .statusCode(200)
                .body(containsStringIgnoringCase("logout"))
                .extract()
                .response();

        System.out.println("Logout Response: " + response.asString());
    }

    @Test
    @DisplayName("Should fail logout without token")
    public void testLogoutWithoutToken() {
        Response response = givenJsonRequest()
                .when()
                .post(ApiConfig.AUTH_LOGOUT)
                .then()
                .statusCode(401)
                .extract()
                .response();

        System.out.println("Logout Without Token Response: " + response.asString());
    }
}