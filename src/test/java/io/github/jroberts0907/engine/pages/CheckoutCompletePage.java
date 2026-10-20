package io.github.jroberts0907.engine.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The confirmation shown after finishing an order. */
public class CheckoutCompletePage extends BasePage {

    private static final By HEADER = By.cssSelector("[data-test='complete-header']");

    public CheckoutCompletePage(WebDriver driver) {
        super(driver);
    }

    public CheckoutCompletePage waitUntilLoaded() {
        visible(HEADER);
        return this;
    }

    public String header() {
        return textOf(HEADER);
    }
}
