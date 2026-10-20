package io.github.jroberts0907.engine.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.jroberts0907.engine.support.Config;

/**
 * Shared helpers for page objects. Every helper waits for its element first,
 * so pages never need Thread.sleep.
 */
public abstract class BasePage {


    // The header (and its cart badge) is on every page after login
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.timeout());
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    protected String textOf(By locator) {
        return visible(locator).getText();
    }

    /** Text of every match, waiting for at least one to appear. */
    protected List<String> textsOf(By locator) {
        visible(locator);
        return driver.findElements(locator).stream().map(WebElement::getText).toList();
    }

    protected boolean isShown(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    /** Number on the header's cart badge, or 0 when the badge isn't shown. */
    public int cartCount() {
        return isShown(CART_BADGE) ? Integer.parseInt(textOf(CART_BADGE)) : 0;
    }

    /** The site builds button ids from product names: "Sauce Labs Backpack" -> sauce-labs-backpack. */
    protected static String testIdSlug(String productName) {
        return productName.replaceAll("\\s+", "-").toLowerCase();
    }
}
