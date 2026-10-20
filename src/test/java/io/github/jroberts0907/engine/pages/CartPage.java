package io.github.jroberts0907.engine.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** The cart, reached from the header's cart icon. */
public class CartPage extends BasePage {

    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");
    private static final By CONTINUE_SHOPPING = By.cssSelector("[data-test='continue-shopping']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage waitUntilLoaded() {
        visible(CHECKOUT_BUTTON);
        return this;
    }

    /** Names of the items in the cart. Empty when the cart is, so this doesn't wait for items to appear. */
    public List<String> itemNames() {
        return driver.findElements(ITEM_NAMES).stream().map(WebElement::getText).toList();
    }

    /** Removes an item and waits for it to leave the list, so the next read sees the updated cart. */
    public CartPage remove(String productName) {
        By removeButton = By.cssSelector("[data-test='remove-" + testIdSlug(productName) + "']");
        click(removeButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(removeButton));
        return this;
    }

    public CheckoutInfoPage checkout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutInfoPage(driver).waitUntilLoaded();
    }

    public InventoryPage continueShopping() {
        click(CONTINUE_SHOPPING);
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
