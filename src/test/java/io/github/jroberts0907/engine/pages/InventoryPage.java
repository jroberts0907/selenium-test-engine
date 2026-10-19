package io.github.jroberts0907.engine.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * The product list shown after signing in.
 *
 * TODO: add sorting, product names/prices and add-to-cart.
 */
public class InventoryPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");

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
}
