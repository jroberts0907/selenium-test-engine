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

    /** Selenium Grid / container address. When set, tests run there instead of on this machine. */
    public static String remoteUrl() {
        return System.getenv("SELENIUM_REMOTE_URL");
    }
}
