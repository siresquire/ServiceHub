package com.amalitech.qa.tests;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test to verify Allure setup is working correctly
 */
@Epic("ServiceHub API Testing")
@Feature("Test Infrastructure")
public class AllureSetupTest {

    @Test
    @Story("Allure Configuration")
    @Severity(SeverityLevel.MINOR)
    @Description("Verify that Allure reporting is properly configured and working")
    @DisplayName("Allure Setup Verification Test")
    public void testAllureSetup() {
        // Simple assertion to verify test execution
        assertTrue(true, "Allure setup test should always pass");
        
        // Add some Allure steps for demonstration
        Allure.step("Step 1: Verify test environment", () -> {
            assertTrue(System.getProperty("java.version") != null, "Java version should be available");
        });
        
        Allure.step("Step 2: Verify Allure integration", () -> {
            Allure.addAttachment("Test Info", "text/plain", "This is a test attachment to verify Allure integration");
        });
    }
}