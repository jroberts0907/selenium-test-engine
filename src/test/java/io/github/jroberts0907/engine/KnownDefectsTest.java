package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.jroberts0907.engine.pages.CheckoutInfoPage;
import io.github.jroberts0907.engine.pages.InventoryPage;
import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;
import io.github.jroberts0907.engine.support.TestUsers;

/**
 * Bugs built into Sauce Demo's test accounts. Each test asserts the correct behavior,
 * so it fails while the bug exists. Tagged known-defect and left out of the default run;
 * run them with {@code ./mvnw test -Pknown-defects}. See "Known defects" in the README.
 */
@Tag("known-defect")
class KnownDefectsTest {

    /** Prices for the standard account, which match the product catalog. */
    private static final Map<String, Double> CATALOG_PRICES = Map.of(
            "Sauce Labs Backpack", 29.99,
            "Sauce Labs Bike Light", 9.99,
            "Sauce Labs Bolt T-Shirt", 15.99,
            "Sauce Labs Fleece Jacket", 49.99,
            "Sauce Labs Onesie", 7.99,
            "Test.allTheThings() T-Shirt (Red)", 15.99);

    private static InventoryPage signIn(BaseTest test, String username) {
        return new LoginPage(test.driver()).open().loginAs(username, TestUsers.PASSWORD);
    }

    private static CheckoutInfoPage startCheckout(BaseTest test, String username) {
        return signIn(test, username).addToCart("Sauce Labs Backpack").openCart().checkout();
    }

    @Nested
    @DisplayName("problem_user")
    class ProblemUser extends BaseTest {

        @Test
        @DisplayName("sorting by price reorders the products")
        void sortingWorks() {
            List<Double> prices = signIn(this, TestUsers.PROBLEM).sortBy("lohi").productPrices();

            assertEquals(prices.stream().sorted().toList(), prices);
        }

        @Test
        @DisplayName("every product can be added to the cart")
        void everyProductCanBeAdded() {
            InventoryPage inventory = signIn(this, TestUsers.PROBLEM);
            List<String> names = inventory.productNames();
            names.forEach(inventory::addToCart);

            assertEquals(names.size(), inventory.cartCount());
        }

        @Test
        @DisplayName("a product can be removed from the product list")
        void productCanBeRemoved() {
            // The backpack is one of the products problem_user *can* add, so only the remove is under test
            InventoryPage inventory = signIn(this, TestUsers.PROBLEM)
                    .addToCart("Sauce Labs Backpack")
                    .removeFromCart("Sauce Labs Backpack");

            assertEquals(0, inventory.cartCount());
        }

        @Test
        @DisplayName("each product shows its own image")
        void productImagesAreDistinct() {
            List<String> images = signIn(this, TestUsers.PROBLEM).productImageSources();

            assertEquals(images.size(), new HashSet<>(images).size(), "images: " + images);
        }

        @Test
        @DisplayName("checkout accepts a valid name and postal code")
        void checkoutAcceptsValidDetails() {
            CheckoutInfoPage page = startCheckout(this, TestUsers.PROBLEM);

            // continueWith waits for the overview page, so it throws if the form is rejected
            assertDoesNotThrow(() -> page.continueWith("Test", "User", "29501"));
        }
    }

    @Nested
    @DisplayName("error_user")
    class ErrorUser extends BaseTest {

        @Test
        @DisplayName("sorting by price works without an error alert")
        void sortingWorks() {
            List<Double> prices = signIn(this, TestUsers.ERROR).sortBy("lohi").productPrices();

            assertEquals(prices.stream().sorted().toList(), prices);
        }

        @Test
        @DisplayName("every product can be added to the cart")
        void everyProductCanBeAdded() {
            InventoryPage inventory = signIn(this, TestUsers.ERROR);
            List<String> names = inventory.productNames();
            names.forEach(inventory::addToCart);

            assertEquals(names.size(), inventory.cartCount());
        }

        @Test
        @DisplayName("checkout rejects a missing last name")
        void missingLastNameIsRejected() {
            CheckoutInfoPage page = startCheckout(this, TestUsers.ERROR).attemptContinue("Test", "", "29501");

            assertEquals("Error: Last Name is required", page.errorMessage());
        }

        @Test
        @DisplayName("Finish completes the order")
        void finishCompletesOrder() {
            String header = startCheckout(this, TestUsers.ERROR)
                    .continueWith("Test", "User", "29501")
                    .finish()
                    .header();

            assertEquals("Thank you for your order!", header);
        }
    }

    @Nested
    @DisplayName("visual_user")
    class VisualUser extends BaseTest {

        @Test
        @DisplayName("product prices match the catalog")
        void pricesMatchCatalog() {
            InventoryPage inventory = signIn(this, TestUsers.VISUAL);
            List<String> names = inventory.productNames();
            List<Double> prices = inventory.productPrices();

            for (int i = 0; i < names.size(); i++) {
                assertEquals(CATALOG_PRICES.get(names.get(i)), prices.get(i), names.get(i));
            }
        }
    }

    @Nested
    @DisplayName("performance_glitch_user")
    class PerformanceGlitchUser extends BaseTest {

        private static final Duration LOGIN_BUDGET = Duration.ofSeconds(3);

        @Test
        @DisplayName("login reaches the product list within 3 seconds")
        void loginIsFast() {
            long start = System.nanoTime();
            signIn(this, TestUsers.PERFORMANCE_GLITCH);
            Duration took = Duration.ofNanos(System.nanoTime() - start);

            assertTrue(took.compareTo(LOGIN_BUDGET) <= 0, "login took " + took.toMillis() + " ms");
        }
    }
}
