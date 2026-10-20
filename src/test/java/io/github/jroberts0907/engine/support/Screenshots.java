package io.github.jroberts0907.engine.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/** Saves failure screenshots to target/screenshots. */
public final class Screenshots {

    private static final Path DIR = Path.of("target", "screenshots");

    private Screenshots() {
    }

    public static void save(WebDriver driver, String name) {
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        String fileName = name.replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("^_|_$", "") + ".png";
        try {
            Files.createDirectories(DIR);
            Files.write(DIR.resolve(fileName), camera.getScreenshotAs(OutputType.BYTES));
        } catch (WebDriverException e) {
            // e.g. a JavaScript alert is open; skip the screenshot rather than hide the real failure
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
