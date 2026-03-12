package com.amalitech.qa.tests.auth;

import com.amalitech.qa.base.BaseApiTest;
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
 * API tests for Authentication endpoints
 */
@Epic("ServiceHub API Testing")
@Feature("Authentication API")
@TestMethodOrder(OrderAnnotation.class)
public class AuthApiTest extends BaseApiTest {
    
    private static String registeredUserToken;
    private static String testUserEmail;
    private static String testUserPassword;
    private static final String AUTH_ENDPOINT = "/api/auth";
    
    @BeforeAll
    public static void setupClass() {
        // Generate unique test user credentials
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testUserEmail = "testuser" + uniqueId + "@example.com";
        testUserPassword = "password123";
    }
    
    @Test
    @Order(1)
    @Story("User Registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test user registration with valid credentials and verify JWT token generation")
    @DisplayName("POST /api/auth/register - Register new user successfully")
    public void testRegisterNewUser() {
        Map<String, Object> registerData = createRegisterPayload(
            "Test User", testUserEmail, testUserPassword, null);
        
        Response response = givenJsonRequest()
            .body(registerData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo(testUserEmail))
                .body("role", equalTo("USER"))
                .body("fullName", equalTo("Test User"))
                .log().ifValidationFails()
                .extract().response();
        
        registeredUserToken = response.jsonPath().getString("token");
        Assertions.assertNotNull(registeredUserToken, "Registration should return a valid token");
    }
    
    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register - Register with invalid email")
    public void testRegisterWithInvalidEmail() {
        Map<String, Object> invalidData = createRegisterPayload(
            "Test User", "invalid-email", testUserPassword, null);
        
        givenJsonRequest()
            .body(invalidData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register - Register with short password")
    public void testRegisterWithShortPassword() {
        String uniqueEmail = "shortpass" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        Map<String, Object> invalidData = createRegisterPayload(
            "Test User", uniqueEmail, "123", null);
        
        givenJsonRequest()
            .body(invalidData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register - Register with duplicate email")
    public void testRegisterWithDuplicateEmail() {
        Map<String, Object> duplicateData = createRegisterPayload(
            "Another User", testUserEmail, "password456", null);
        
        givenJsonRequest()
            .body(duplicateData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(5)
    @DisplayName("POST /api/auth/register - Register with missing required fields")
    public void testRegisterWithMissingFields() {
        Map<String, Object> incompleteData = new HashMap<>();
        incompleteData.put("email", "incomplete@example.com");
        // Missing name and password
        
        givenJsonRequest()
            .body(incompleteData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(6)
    @Story("User Login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Test user login with valid credentials and verify JWT token generation")
    @DisplayName("POST /api/auth/login - Login with valid credentials")
    public void testLoginWithValidCredentials() {
        Map<String, Object> loginData = createLoginPayload(testUserEmail, testUserPassword);
        
        Response response = givenJsonRequest()
            .body(loginData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo(testUserEmail))
                .body("role", equalTo("USER"))
                .body("fullName", equalTo("Test User"))
                .log().ifValidationFails()
                .extract().response();
        
        String loginToken = response.jsonPath().getString("token");
        Assertions.assertNotNull(loginToken, "Login should return a valid token");
        
        // Update stored token with login token
        registeredUserToken = loginToken;
    }
    
    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login - Login with invalid email")
    public void testLoginWithInvalidEmail() {
        Map<String, Object> invalidData = createLoginPayload("nonexistent@example.com", testUserPassword);
        
        givenJsonRequest()
            .body(invalidData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(8)
    @DisplayName("POST /api/auth/login - Login with wrong password")
    public void testLoginWithWrongPassword() {
        Map<String, Object> invalidData = createLoginPayload(testUserEmail, "wrongpassword");
        
        givenJsonRequest()
            .body(invalidData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(9)
    @DisplayName("POST /api/auth/login - Login with malformed email")
    public void testLoginWithMalformedEmail() {
        Map<String, Object> invalidData = createLoginPayload("not-an-email", testUserPassword);
        
        givenJsonRequest()
            .body(invalidData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(10)
    @DisplayName("POST /api/auth/login - Login with empty credentials")
    public void testLoginWithEmptyCredentials() {
        Map<String, Object> emptyData = createLoginPayload("", "");
        
        givenJsonRequest()
            .body(emptyData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(400)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(11)
    @DisplayName("Verify authenticated request works with obtained token")
    public void testAuthenticatedRequestWithToken() {
        Assumptions.assumeTrue(registeredUserToken != null, 
            "User token must be available from previous tests");
        
        givenAuthenticatedRequest(registeredUserToken)
            .when()
                .get("/api/profile")
            .then()
                .statusCode(anyOf(is(200), is(404))) // 404 if profile endpoint doesn't exist
                .log().ifValidationFails();
    }
    
    @Test
    @Order(12)
    @Story("User Logout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test user logout functionality with valid JWT token")
    @DisplayName("POST /api/auth/logout - Logout with valid token")
    public void testLogoutWithValidToken() {
        Assumptions.assumeTrue(registeredUserToken != null, 
            "User token must be available from previous tests");
        
        givenAuthenticatedRequest(registeredUserToken)
            .when()
                .post(AUTH_ENDPOINT + "/logout")
            .then()
                .statusCode(anyOf(is(200), is(204)))
                .log().ifValidationFails();
    }
    
    @Test
    @Order(13)
    @DisplayName("POST /api/auth/logout - Logout without token")
    public void testLogoutWithoutToken() {
        givenJsonRequest()
            .when()
                .post(AUTH_ENDPOINT + "/logout")
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(14)
    @DisplayName("POST /api/auth/logout - Logout with invalid token")
    public void testLogoutWithInvalidToken() {
        givenAuthenticatedRequest("invalid.jwt.token")
            .when()
                .post(AUTH_ENDPOINT + "/logout")
            .then()
                .statusCode(401)
                .log().ifValidationFails();
    }
    
    @Test
    @Order(15)
    @DisplayName("Verify token is invalidated after logout")
    public void testTokenInvalidatedAfterLogout() {
        // Try to use the token after logout - should fail
        if (registeredUserToken != null) {
            givenAuthenticatedRequest(registeredUserToken)
                .when()
                    .get("/api/profile")
                .then()
                    .statusCode(anyOf(is(401), is(404))) // 401 if token blacklisted, 404 if endpoint doesn't exist
                    .log().ifValidationFails();
        }
    }
    
    @Test
    @DisplayName("Test complete authentication flow")
    public void testCompleteAuthFlow() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String flowTestEmail = "flowtest" + uniqueId + "@example.com";
        String flowTestPassword = "flowpassword123";
        
        // 1. Register
        Map<String, Object> registerData = createRegisterPayload(
            "Flow Test User", flowTestEmail, flowTestPassword, null);
        
        Response registerResponse = givenJsonRequest()
            .body(registerData)
            .when()
                .post(AUTH_ENDPOINT + "/register")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().response();
        
        String registerToken = registerResponse.jsonPath().getString("token");
        
        // 2. Logout from registration session
        givenAuthenticatedRequest(registerToken)
            .when()
                .post(AUTH_ENDPOINT + "/logout")
            .then()
                .statusCode(anyOf(is(200), is(204)));
        
        // 3. Login again
        Map<String, Object> loginData = createLoginPayload(flowTestEmail, flowTestPassword);
        
        Response loginResponse = givenJsonRequest()
            .body(loginData)
            .when()
                .post(AUTH_ENDPOINT + "/login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo(flowTestEmail))
                .extract().response();
        
        String loginToken = loginResponse.jsonPath().getString("token");
        
        // 4. Final logout
        givenAuthenticatedRequest(loginToken)
            .when()
                .post(AUTH_ENDPOINT + "/logout")
            .then()
                .statusCode(anyOf(is(200), is(204)));
    }
    
    /**
     * Helper method to create registration payload
     */
    private Map<String, Object> createRegisterPayload(String name, String email, 
                                                     String password, Long departmentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("password", password);
        if (departmentId != null) {
            payload.put("departmentId", departmentId);
        }
        return payload;
    }
    
    /**
     * Helper method to create login payload
     */
    private Map<String, Object> createLoginPayload(String email, String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return payload;
    }
    
    /**
     * Get the token from registration/login for use in other test classes
     */
    public static String getRegisteredUserToken() {
        return registeredUserToken;
    }
    
    /**
     * Get test user email for use in other test classes
     */
    public static String getTestUserEmail() {
        return testUserEmail;
    }
}