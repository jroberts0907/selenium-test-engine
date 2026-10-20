package io.github.jroberts0907.engine.keyword;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Optional pacing for watching or recording a run (-DstepDelay=800 -Dhighlight=true): outlines the
 * element each step is about to use, pauses before acting, and types one character at a time.
 *
 * Off by default. These are the only sleeps in the project, and they exist so a person can follow
 * along; tests never depend on them to pass.
 */
final class DemoMode {

    private static final String ACTION_COLOR = "#7c3aed";
    private static final String ASSERT_COLOR = "#16a34a";
    private static final String FAILED_COLOR = "#dc2626";
    private static final Duration TYPING_DELAY = Duration.ofMillis(60);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration stepDelay;
    private final boolean highlight;
    private List<WebElement> marked = List.of();

    DemoMode(WebDriver driver, WebDriverWait wait, Duration stepDelay, boolean highlight) {
        this.driver = driver;
        this.wait = wait;
        this.stepDelay = stepDelay;
        this.highlight = highlight;
    }

    void beforeStep(Keyword keyword, String target) {
        if (highlight && keyword.needsTarget() && keyword != Keyword.OPEN && keyword != Keyword.ASSERT_GONE) {
            marked = findAll(target);
            mark(keyword.jsonName().startsWith("assert") ? ASSERT_COLOR : ACTION_COLOR);
        }
        pause(stepDelay);
    }

    void afterStep() {
        mark(null);
        marked = List.of();
    }

    /** Turns the step's elements red and holds, so a recording shows where the test failed. */
    void onFailure() {
        if (!marked.isEmpty()) {
            mark(FAILED_COLOR);
            pause(stepDelay.multipliedBy(3));
        }
    }

    void type(WebElement field, String text) {
        if (stepDelay.isZero()) {
            field.sendKeys(text);
            return;
        }
        for (char c : text.toCharArray()) {
            field.sendKeys(String.valueOf(c));
            pause(TYPING_DELAY);
        }
    }

    private List<WebElement> findAll(String target) {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(Locators.parse(target)));
        } catch (TimeoutException e) {
            return List.of(); // the step itself will wait again and report the failure properly
        }
    }

    /** Outlines the marked elements in a color, or clears the outline when color is null. */
    private void mark(String color) {
        for (int i = 0; i < marked.size(); i++) {
            try {
                ((JavascriptExecutor) driver).executeScript("""
                        const el = arguments[0], color = arguments[1];
                        el.style.outline = color ? '3px solid ' + color : '';
                        el.style.outlineOffset = color ? '3px' : '';
                        el.style.boxShadow = color ? '0 0 0 8px ' + color + '33' : '';
                        if (color && arguments[2]) el.scrollIntoView({block: 'center'});
                        """, marked.get(i), color, i == 0);
            } catch (WebDriverException e) {
                // The element left the page (e.g. a click navigated away); nothing to clear
            }
        }
    }

    private static void pause(Duration duration) {
        if (duration.isZero()) {
            return;
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
