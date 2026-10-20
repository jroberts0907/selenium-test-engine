package io.github.jroberts0907.engine.support;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Creates the WebDriver for a test. Locally, Selenium Manager finds or downloads
 * the browser driver; in CI, SELENIUM_REMOTE_URL points at a Selenium container.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        Capabilities options = optionsFor(Config.browser(), Config.headless());

        String remoteUrl = Config.remoteUrl();
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            try {
                return new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Bad SELENIUM_REMOTE_URL: " + remoteUrl, e);
            }
        }

        return switch (options) {
            case ChromeOptions chrome -> new ChromeDriver(chrome);
            case EdgeOptions edge -> new EdgeDriver(edge);
            case FirefoxOptions firefox -> new FirefoxDriver(firefox);
            default -> throw new IllegalStateException("No driver for " + options.getBrowserName());
        };
    }

    private static Capabilities optionsFor(String browser, boolean headless) {
        String[] size = Config.windowSize().split(",");
        return switch (browser) {
            case "chrome" -> {
                ChromeOptions options = withoutPasswordPrompts(new ChromeOptions())
                        .addArguments("--window-size=" + Config.windowSize());
                yield headless ? options.addArguments("--headless=new") : options;
            }
            case "edge" -> {
                EdgeOptions options = withoutPasswordPrompts(new EdgeOptions())
                        .addArguments("--window-size=" + Config.windowSize());
                yield headless ? options.addArguments("--headless=new") : options;
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions().addArguments("--width=" + size[0], "--height=" + size[1]);
                yield headless ? options.addArguments("-headless") : options;
            }
            default -> throw new IllegalArgumentException(
                    "Unknown browser '" + browser + "'. Use chrome, edge or firefox.");
        };
    }

    /**
     * Turns off the password manager. Sauce Demo's shared password is on public breach lists, so Chromium
     * shows a "Change your password" dialog after login. The dialog is browser UI, so it never appears in
     * screenshots, but it swallows the next clicks and the test times out.
     */
    private static <T extends ChromiumOptions<T>> T withoutPasswordPrompts(T options) {
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return options;
    }
}
