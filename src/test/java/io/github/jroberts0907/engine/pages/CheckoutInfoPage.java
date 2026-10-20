package io.github.jroberts0907.engine.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Checkout step one: name and postal code. */
public class CheckoutInfoPage extends BasePage {

    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE_BUTTON = By.cssSelector("[data-test='continue']");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public CheckoutInfoPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutInfoPage waitUntilLoaded() {
        visible(CONTINUE_BUTTON);
        return this;
    }

    /** Fills the form with details that should be accepted and returns the overview. */
    public CheckoutOverviewPage continueWith(String firstName, String lastName, String postalCode) {
        attemptContinue(firstName, lastName, postalCode);
        return new CheckoutOverviewPage(driver).waitUntilLoaded();
    }

    /** Submits the form without assuming it succeeds, for testing validation errors. */
    public CheckoutInfoPage attemptContinue(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        click(CONTINUE_BUTTON);
        return this;
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
