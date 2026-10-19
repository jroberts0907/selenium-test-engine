package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import io.github.jroberts0907.engine.support.Config;
import io.github.jroberts0907.engine.support.DriverFactory;

/** Checks that a browser starts and the site is up before the real suite runs. */
class SmokeTest {

    private WebDriver driver;

    @BeforeEach
    void startBrowser() {
        driver = DriverFactory.create();
    }

    @AfterEach
    void quitBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void loginPageLoads() {
        driver.get(Config.baseUrl());

        assertEquals("Swag Labs", driver.getTitle());
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed());
    }
}
