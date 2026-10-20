package io.github.jroberts0907.engine.keyword;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.jroberts0907.engine.support.Config;
import io.github.jroberts0907.engine.support.Screenshots;

/**
 * Runs a {@link Script} step by step in a browser. Every step waits for its element,
 * and a failure names the step that failed, e.g. "Step 4 of 7 failed: click testid=login-button".
 */
public class KeywordRunner {


    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Map<String, String> variables;
    private final Path evidenceDir;
    private final DemoMode demo;

    public KeywordRunner(WebDriver driver, Map<String, String> variables) {
        this(driver, variables, null);
    }

    /**
     * @param evidenceDir if not null, a screenshot is saved here after every step (01-passed.png,
     *                    02-passed.png, ..., NN-failed.png) as a record of what the test saw
     */
    public KeywordRunner(WebDriver driver, Map<String, String> variables, Path evidenceDir) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.timeout());
        this.variables = variables;
        this.evidenceDir = evidenceDir;
        this.demo = new DemoMode(driver, wait, Config.stepDelay(), Config.highlight());
    }

    public void run(Script script) {
        List<Step> steps = script.steps();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            long started = System.currentTimeMillis();
            try {
                execute(step);
                saveEvidence(i, step, started, "passed", null);
            } catch (AssertionError | RuntimeException e) {
                String message = "Step %d of %d failed: %s%n%s"
                        .formatted(i + 1, steps.size(), step, firstLine(e.getMessage()));
                demo.onFailure();
                saveEvidence(i, step, started, "failed", message);
                throw new AssertionError(message, e);
            }
        }
    }

    /** Screenshot after the step, plus a line in steps.csv with when it started and ended (epoch ms). */
    private void saveEvidence(int stepIndex, Step step, long started, String outcome, String failureMessage) {
        if (evidenceDir == null) {
            return;
        }
        long ended = System.currentTimeMillis();
        Screenshots.saveTo(driver, evidenceDir.resolve("%02d-%s.png".formatted(stepIndex + 1, outcome)));
        try {
            Path timeline = evidenceDir.resolve("steps.csv");
            if (stepIndex == 0) {
                Files.writeString(timeline, "step,action,started_ms,ended_ms,outcome\n");
            }
            Files.writeString(timeline, "%d,%s,%d,%d,%s%n".formatted(stepIndex + 1, step.action(), started, ended, outcome),
                    StandardOpenOption.APPEND);
            if (failureMessage != null) {
                Files.writeString(evidenceDir.resolve("failure.txt"), failureMessage);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void execute(Step step) {
        String target = Variables.resolve(step.target(), variables);
        String value = Variables.resolve(step.value(), variables);

        demo.beforeStep(step.keyword(), target);
        switch (step.keyword()) {
            case OPEN -> driver.get(target.startsWith("http") ? target : Config.baseUrl() + target);
            case CLICK -> wait.until(ExpectedConditions.elementToBeClickable(Locators.parse(target))).click();
            case TYPE -> {
                WebElement field = visible(target);
                field.clear();
                demo.type(field, value);
            }
            case SELECT -> new Select(visible(target)).selectByValue(value);
            case ASSERT_TEXT -> assertText(target, value);
            case ASSERT_VISIBLE -> visible(target);
            case ASSERT_GONE -> wait.until(ExpectedConditions.invisibilityOfElementLocated(Locators.parse(target)));
            case ASSERT_COUNT -> assertCount(target, Integer.parseInt(value));
            case ASSERT_URL_CONTAINS -> assertUrlContains(value);
        }
        demo.afterStep();
    }

    private WebElement visible(String target) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.parse(target)));
    }

    // The assertions wait for the expected state first, so text that renders a moment late still passes

    private void assertText(String target, String expected) {
        By locator = Locators.parse(target);
        try {
            wait.until(ExpectedConditions.textToBe(locator, expected));
        } catch (TimeoutException e) {
            throw new AssertionError("expected text <" + expected + "> but was <" + visible(target).getText() + ">");
        }
    }

    private void assertCount(String target, int expected) {
        By locator = Locators.parse(target);
        try {
            wait.until(ExpectedConditions.numberOfElementsToBe(locator, expected));
        } catch (TimeoutException e) {
            throw new AssertionError("expected " + expected + " matches but found " + driver.findElements(locator).size());
        }
    }

    private void assertUrlContains(String expected) {
        try {
            wait.until(ExpectedConditions.urlContains(expected));
        } catch (TimeoutException e) {
            throw new AssertionError("expected URL containing <" + expected + "> but was <" + driver.getCurrentUrl() + ">");
        }
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }
}
