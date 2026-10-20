package io.github.jroberts0907.engine;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.openqa.selenium.WebDriver;

import io.github.jroberts0907.engine.keyword.KeywordRunner;
import io.github.jroberts0907.engine.keyword.Script;
import io.github.jroberts0907.engine.keyword.ScriptLoader;
import io.github.jroberts0907.engine.support.BrowserRecorder;
import io.github.jroberts0907.engine.support.Config;
import io.github.jroberts0907.engine.support.DriverFactory;
import io.github.jroberts0907.engine.support.Screenshots;
import io.github.jroberts0907.engine.support.TestUsers;

/**
 * Runs every JSON script in src/test/resources/scripts as its own JUnit test.
 * Adding a test case means adding a JSON file; no Java changes.
 */
class KeywordScriptsTest {

    private static final Map<String, String> VARIABLES = Map.of(
            "testUser", Config.testUser(),
            "password", TestUsers.PASSWORD);

    @TestFactory
    Stream<DynamicTest> scripts() {
        return ScriptLoader.loadFolder("scripts").stream()
                .filter(loaded -> loaded.file().getFileName().toString().contains(Config.scriptFilter()))
                .map(loaded -> DynamicTest.dynamicTest(
                        loaded.script().name(), loaded.file().toUri(), () -> runInFreshBrowser(loaded)));
    }

    // @BeforeEach/@AfterEach run once per @TestFactory, not per dynamic test, so each script manages its own browser
    private static void runInFreshBrowser(ScriptLoader.Loaded loaded) {
        Script script = loaded.script();
        // With -DrecordSteps or -DrecordVideo, evidence goes to target/evidence/<script file>/
        Path evidenceDir = Config.recordSteps() || Config.recordVideo()
                ? Path.of("target", "evidence", loaded.file().getFileName().toString().replace(".json", ""))
                : null;
        WebDriver driver = DriverFactory.create();
        BrowserRecorder recorder = Config.recordVideo()
                ? BrowserRecorder.start(driver, evidenceDir, Integer.parseInt(Config.windowSize().split(",")[0]))
                : null;
        try {
            new KeywordRunner(driver, VARIABLES, evidenceDir).run(script);
        } catch (AssertionError e) {
            Screenshots.save(driver, "script_" + script.name());
            throw e;
        } finally {
            if (recorder != null) {
                recorder.close();
            }
            driver.quit();
        }
    }
}
