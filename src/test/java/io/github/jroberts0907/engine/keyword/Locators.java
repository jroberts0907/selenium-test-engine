package io.github.jroberts0907.engine.keyword;

import org.openqa.selenium.By;

/**
 * Turns a script's target text into a Selenium locator.
 *
 * <ul>
 *   <li>{@code testid=username} → {@code [data-test='username']}</li>
 *   <li>{@code id=user-name}</li>
 *   <li>{@code xpath=(//button)[1]}</li>
 *   <li>{@code css=.title}, or any text without a prefix, is a CSS selector</li>
 * </ul>
 */
public final class Locators {

    private Locators() {
    }

    public static By parse(String target) {
        if (target.startsWith("testid=")) {
            return By.cssSelector("[data-test='" + target.substring("testid=".length()) + "']");
        }
        if (target.startsWith("id=")) {
            return By.id(target.substring("id=".length()));
        }
        if (target.startsWith("xpath=")) {
            return By.xpath(target.substring("xpath=".length()));
        }
        if (target.startsWith("css=")) {
            return By.cssSelector(target.substring("css=".length()));
        }
        return By.cssSelector(target);
    }
}
