package io.github.jroberts0907.engine.support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Saves a screenshot to target/screenshots when a test fails.
 *
 * Uses AfterTestExecutionCallback because it runs before @AfterEach,
 * while the browser is still open.
 */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()
                && context.getRequiredTestInstance() instanceof BaseTest test) {
            Screenshots.save(test.driver(),
                    context.getRequiredTestClass().getSimpleName() + "_" + context.getDisplayName());
        }
    }
}
