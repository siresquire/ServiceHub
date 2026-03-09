package com.amalitech.qa.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for all API tests providing common setup and utilities
 */
public abstract class BaseApiTest {

    protected static RequestSpecification requestSpec;
    protected static String baseUri;

    @BeforeAll
    public static void baseSetup() {
        baseUri = System.getProperty("base.uri", "http://localhost:8080");
        RestAssured.baseURI = baseUri;

        requestSpec = new RequestSpecBuilder()
            .setBaseUri(baseUri)
            .setContentType("application/json")
            .build();
    }

    protected String getAuthToken(String email, String password) {
        return RestAssured.given()
            .contentType("application/json")
            .body(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password))
            .when()
            .post("/api/auth/login")
            .then()
            .extract()
            .path("token");
    }
}
