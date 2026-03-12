package com.amalitech.qa.tests;

import com.amalitech.qa.base.BaseTest;
import com.amalitech.qa.config.ApiConfig;
import com.amalitech.qa.utils.TokenManager;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ServiceRequestApiTest extends BaseTest {

    @Test
    public void testGetAllRequests() {
        givenAuthenticatedRequest(TokenManager.getAdminToken())
        .when().get(ApiConfig.SERVICE_REQUESTS)
        .then().statusCode(200);
    }

    @Test
    public void testCreateServiceRequest() {
        givenAuthenticatedRequest(TokenManager.getAdminToken())
            .body("{\"title\":\"Laptop not working\",\"description\":\"Screen is blank\",\"category\":\"HARDWARE\",\"priority\":\"HIGH\",\"departmentId\":1}")
        .when().post(ApiConfig.SERVICE_REQUESTS)
        .then().statusCode(200).body("title", equalTo("Laptop not working"));
    }

    // TODO: testUpdateRequestStatus
    // TODO: testGetDepartments
    // TODO: testUnauthorizedAccess
}
