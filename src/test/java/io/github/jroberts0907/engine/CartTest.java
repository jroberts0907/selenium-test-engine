package io.github.jroberts0907.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jroberts0907.engine.pages.CartPage;
import io.github.jroberts0907.engine.pages.InventoryPage;
import io.github.jroberts0907.engine.pages.LoginPage;
import io.github.jroberts0907.engine.support.BaseTest;
import io.github.jroberts0907.engine.support.TestUsers;

class CartTest extends BaseTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    private InventoryPage inventory;

    @BeforeEach
    void signInAndAddTwoItems() {
        inventory = new LoginPage(driver).open()
                .loginAs(TestUsers.STANDARD, TestUsers.PASSWORD)
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT);
    }

    @Test
    void addedItemsAppearInCart() {
        List<String> names = inventory.openCart().itemNames();

        assertEquals(2, names.size());
        assertEquals(Set.of(BACKPACK, BIKE_LIGHT), Set.copyOf(names));
    }

    @Test
    void removingAnItemDropsItFromCart() {
        CartPage cart = inventory.openCart().remove(BACKPACK);

        assertEquals(List.of(BIKE_LIGHT), cart.itemNames());
        assertEquals(1, cart.cartCount());
    }

    @Test
    void continueShoppingKeepsTheCart() {
        InventoryPage backOnInventory = inventory.openCart().continueShopping();

        assertEquals(2, backOnInventory.cartCount());
        assertEquals(2, backOnInventory.openCart().itemNames().size());
    }
}
