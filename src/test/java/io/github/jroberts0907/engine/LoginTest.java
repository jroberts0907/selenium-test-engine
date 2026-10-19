package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.jroberts0907.engine.pages.InventoryPage;
import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;
import io.github.jroberts0907.engine.support.TestUsers;

class LoginTest extends BaseTest {

    @Test
    void standardUserReachesProducts() {
        InventoryPage inventory = new LoginPage(driver).open()
                .loginAs(TestUsers.STANDARD, TestUsers.PASSWORD);

        assertEquals("Products", inventory.title());
        assertTrue(driver.getCurrentUrl().endsWith("/inventory.html"));
    }

    // In @CsvSource, '' is an empty string and single quotes wrap text that contains commas
    @ParameterizedTest(name = "user=''{0}'' password=''{1}''")
    @CsvSource({
            "locked_out_user, secret_sauce,   'Epic sadface: Sorry, this user has been locked out.'",
            "standard_user,   wrong_password, Epic sadface: Username and password do not match any user in this service",
            "'',              secret_sauce,   Epic sadface: Username is required",
            "standard_user,   '',             Epic sadface: Password is required",
    })
    void rejectedLoginShowsError(String username, String password, String expectedError) {
        LoginPage login = new LoginPage(driver).open()
                .attemptLogin(username, password);

        assertEquals(expectedError, login.errorMessage());
        assertFalse(driver.getCurrentUrl().contains("inventory"), "should stay on the login page");
    }
}
