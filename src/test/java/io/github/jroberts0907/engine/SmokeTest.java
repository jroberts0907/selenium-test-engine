package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;

/** Checks that a browser starts and the site is up before the real suite runs. */
class SmokeTest extends BaseTest {

    @Test
    void loginPageLoads() {
        new LoginPage(driver).open();

        assertEquals("Swag Labs", driver.getTitle());
    }
}
