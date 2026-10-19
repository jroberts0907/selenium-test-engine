package io.github.jroberts0907.engine.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Saves a screenshot to target/screenshots when a test fails.
 *
 * Uses AfterTestExecutionCallback because it runs before @AfterEach,
 * while the browser is still open.
 */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    private static final Path DIR = Path.of("target", "screenshots");

    @Override
    public void afterTestExecution(ExtensionContext context) throws IOException {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        if (!(context.getRequiredTestInstance() instanceof BaseTest test)
                || !(test.driver() instanceof TakesScreenshot camera)) {
            return;
        }

        String name = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getDisplayName().replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("^_|_$", "") + ".png";
        Files.createDirectories(DIR);
        Files.write(DIR.resolve(name), camera.getScreenshotAs(OutputType.BYTES));
    }
}
