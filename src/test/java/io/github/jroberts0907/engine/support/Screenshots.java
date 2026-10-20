package io.github.jroberts0907.engine.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/** Saves screenshots of the browser, by default to target/screenshots. */
public final class Screenshots {

    private static final Path DIR = Path.of("target", "screenshots");

    private Screenshots() {
    }

    /** Saves to target/screenshots/<name>.png, with the name made safe for a file name. */
    public static void save(WebDriver driver, String name) {
        saveTo(driver, DIR.resolve(safeFileName(name) + ".png"));
    }

    public static void saveTo(WebDriver driver, Path file) {
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, camera.getScreenshotAs(OutputType.BYTES));
        } catch (WebDriverException e) {
            // e.g. a JavaScript alert is open; skip the screenshot rather than hide the real failure
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String safeFileName(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("^_|_$", "");
    }
}
