package com.amalitech.qa;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DashboardPageTest {
    private static WebDriver driver;

    @BeforeAll
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
    }

    @Test
    public void testDashboardPageLoads() {
        driver.get("http://localhost:8080");
        assertTrue(driver.getPageSource().contains("ServiceHub"));
    }

    // TODO: testSubmitRequestFlow
    // TODO: testStatusUpdateFlow

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }
}
