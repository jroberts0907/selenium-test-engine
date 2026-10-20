package io.github.jroberts0907.engine.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/** The product list shown after signing in. */
public class InventoryPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By SORT = By.cssSelector("[data-test='product-sort-container']");
    private static final By PRODUCT_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By PRODUCT_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By PRODUCT_IMAGES = By.cssSelector(".inventory_item_img img");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public InventoryPage waitUntilLoaded() {
        visible(TITLE);
        return this;
    }

    public String title() {
        return textOf(TITLE);
    }

    /** Picks a sort option by its value: az, za, lohi or hilo. */
    public InventoryPage sortBy(String option) {
        new Select(visible(SORT)).selectByValue(option);
        return this;
    }

    /** Product names in the order they're shown. */
    public List<String> productNames() {
        return textsOf(PRODUCT_NAMES);
    }

    /** Product prices in the order they're shown, as numbers ("$29.99" becomes 29.99). */
    public List<Double> productPrices() {
        return textsOf(PRODUCT_PRICES).stream()
                .map(price -> Double.parseDouble(price.replace("$", "")))
                .toList();
    }

    /** Image URL of each product, in the order they're shown. */
    public List<String> productImageSources() {
        visible(PRODUCT_IMAGES);
        return driver.findElements(PRODUCT_IMAGES).stream().map(img -> img.getDomProperty("src")).toList();
    }

    /** Clicks a product's Add to cart button. */
    public InventoryPage addToCart(String productName) {
        click(By.cssSelector("[data-test='add-to-cart-" + testIdSlug(productName) + "']"));
        return this;
    }

    /** Clicks a product's Remove button on the product list. */
    public InventoryPage removeFromCart(String productName) {
        click(By.cssSelector("[data-test='remove-" + testIdSlug(productName) + "']"));
        return this;
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver).waitUntilLoaded();
    }
}
