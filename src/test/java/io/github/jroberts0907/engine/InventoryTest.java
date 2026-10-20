package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jroberts0907.engine.pages.InventoryPage;
import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;
import io.github.jroberts0907.engine.support.Config;
import io.github.jroberts0907.engine.support.TestUsers;

class InventoryTest extends BaseTest {

    private static final int PRODUCT_COUNT = 6;

    private InventoryPage inventory;

    // Runs after BaseTest's @BeforeEach, so the browser is already open
    @BeforeEach
    void signIn() {
        inventory = new LoginPage(driver).open().loginAs(Config.testUser(), TestUsers.PASSWORD);
    }

    @Test
    void sortsByPriceLowToHigh() {
        List<Double> prices = inventory.sortBy("lohi").productPrices();

        assertEquals(PRODUCT_COUNT, prices.size());
        // Comparing to a sorted copy handles the two $15.99 items, unlike a hard-coded list
        assertEquals(prices.stream().sorted().toList(), prices);
    }

    @Test
    void sortsByNameZToA() {
        List<String> names = inventory.sortBy("za").productNames();

        assertEquals(PRODUCT_COUNT, names.size());
        assertEquals(names.stream().sorted(Comparator.reverseOrder()).toList(), names);
    }

    @Test
    void cartBadgeCountsAddedItems() {
        assertEquals(0, inventory.cartCount());

        inventory.addToCart("Sauce Labs Backpack").addToCart("Sauce Labs Bike Light");

        assertEquals(2, inventory.cartCount());
    }
}
