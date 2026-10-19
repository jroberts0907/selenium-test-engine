package io.github.jroberts0907.engine.support;

/**
 * Run settings, read from system properties so they can be changed from the
 * command line, e.g. {@code ./mvnw test -Dbrowser=edge -Dheadless=false}.
 */
public final class Config {

    private Config() {
    }

    /** Site under test. */
    public static String baseUrl() {
        return System.getProperty("baseUrl", "https://www.saucedemo.com");
    }

    /** chrome, edge or firefox. */
    public static String browser() {
        return System.getProperty("browser", "chrome").toLowerCase();
    }

    /** Run without a visible window. Pass -Dheadless=false to watch the tests. */
    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    /** Selenium Grid / container address. When set, tests run there instead of on this machine. */
    public static String remoteUrl() {
        return System.getenv("SELENIUM_REMOTE_URL");
    }
}
