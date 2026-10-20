package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.jroberts0907.engine.pages.CheckoutCompletePage;
import io.github.jroberts0907.engine.pages.CheckoutInfoPage;
import io.github.jroberts0907.engine.pages.CheckoutOverviewPage;
import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;
import io.github.jroberts0907.engine.support.Config;
import io.github.jroberts0907.engine.support.TestUsers;

class CheckoutTest extends BaseTest {

    private CheckoutInfoPage checkout;

    @BeforeEach
    void signInAndStartCheckout() {
        checkout = new LoginPage(driver).open()
                .loginAs(Config.testUser(), TestUsers.PASSWORD)
                .addToCart("Sauce Labs Backpack")
                .addToCart("Sauce Labs Bike Light")
                .openCart()
                .checkout();
    }

    @Test
    void completesAPurchase() {
        CheckoutCompletePage complete = checkout.continueWith("Test", "User", "29501").finish();

        assertEquals("Thank you for your order!", complete.header());
        assertEquals(0, complete.cartCount(), "cart should be empty after ordering");
    }

    @Test
    void totalsAddUp() {
        CheckoutOverviewPage overview = checkout.continueWith("Test", "User", "29501");

        BigDecimal sumOfItems = overview.itemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        // compareTo, not equals: BigDecimal.equals treats 39.98 and 39.980 as different
        assertEquals(0, sumOfItems.compareTo(overview.itemTotal()), "item total should equal the sum of prices");
        assertEquals(0, overview.itemTotal().add(overview.tax()).compareTo(overview.total()),
                "total should equal item total plus tax");
    }

    @ParameterizedTest(name = "first=''{0}'' last=''{1}'' zip=''{2}''")
    @CsvSource({
            "'',   User, 29501, Error: First Name is required",
            "Test, '',   29501, Error: Last Name is required",
            "Test, User, '',    Error: Postal Code is required",
    })
    void missingFieldShowsError(String firstName, String lastName, String postalCode, String expectedError) {
        CheckoutInfoPage page = checkout.attemptContinue(firstName, lastName, postalCode);

        assertEquals(expectedError, page.errorMessage());
    }
}
