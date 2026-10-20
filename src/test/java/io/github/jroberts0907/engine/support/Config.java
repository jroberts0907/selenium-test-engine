package io.github.jroberts0907.engine.support;

import java.time.Duration;

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

    /** Account the suite signs in as. Pass -DtestUser=problem_user to run everything as a buggy account. */
    public static String testUser() {
        return System.getProperty("testUser", TestUsers.STANDARD);
    }

    /** chrome, edge or firefox. */
    public static String browser() {
        return System.getProperty("browser", "chrome").toLowerCase();
    }

    /** Run without a visible window. Pass -Dheadless=false to watch the tests. */
    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    /** Browser window size as "width,height". Smaller sizes make screenshots easier to read. */
    public static String windowSize() {
        return System.getProperty("windowSize", "1920,1080");
    }

    /** Save a screenshot after every step of a JSON script (to target/evidence), not just on failure. */
    public static boolean recordSteps() {
        return Boolean.parseBoolean(System.getProperty("recordSteps", "false"));
    }

    /** How long to wait for elements and expected states before failing. Default 10 seconds. */
    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty("timeout", "10")));
    }

    /** Run only JSON scripts whose file name contains this text, e.g. -Dscripts=checkout. Empty runs all. */
    public static String scriptFilter() {
        return System.getProperty("scripts", "");
    }

    /** Pause before each JSON script step, in milliseconds, so a person can follow along. 0 (default) is off. */
    public static Duration stepDelay() {
        return Duration.ofMillis(Long.parseLong(System.getProperty("stepDelay", "0")));
    }

    /** Outline the element each JSON script step is about to use. */
    public static boolean highlight() {
        return Boolean.parseBoolean(System.getProperty("highlight", "false"));
    }

    /** Record each JSON script's page as video frames (Chrome/Edge) to target/evidence/<script>/frames. */
    public static boolean recordVideo() {
        return Boolean.parseBoolean(System.getProperty("recordVideo", "false"));
    }

    /** Selenium Grid / container address. When set, tests run there instead of on this machine. */
    public static String remoteUrl() {
        return System.getenv("SELENIUM_REMOTE_URL");
    }
}
