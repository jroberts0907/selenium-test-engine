package io.github.jroberts0907.engine.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/** Gives each test a fresh browser and saves a screenshot if the test fails. */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class BaseTest {

    protected WebDriver driver;

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

    public WebDriver driver() {
        return driver;
    }
}
