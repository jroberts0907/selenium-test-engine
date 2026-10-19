package io.github.jroberts0907.engine.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import io.github.jroberts0907.engine.support.Config;

/** The sign-in form at the site root. */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.baseUrl());
        visible(LOGIN_BUTTON);
        return this;
    }

    /** Signs in with credentials that should work and returns the page that follows. */
    public InventoryPage loginAs(String username, String password) {
        attemptLogin(username, password);
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /** Submits the form without assuming it succeeds, for testing rejected logins. */
    public LoginPage attemptLogin(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
        return this;
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
