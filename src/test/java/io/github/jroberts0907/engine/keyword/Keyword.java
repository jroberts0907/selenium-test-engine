package io.github.jroberts0907.engine.keyword;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/** The actions a test script step can use, and which fields each one needs. */
public enum Keyword {

    OPEN("open", true, false),
    CLICK("click", true, false),
    TYPE("type", true, true),
    SELECT("select", true, true),
    ASSERT_TEXT("assertText", true, true),
    ASSERT_VISIBLE("assertVisible", true, false),
    ASSERT_GONE("assertGone", true, false),
    ASSERT_COUNT("assertCount", true, true),
    ASSERT_URL_CONTAINS("assertUrlContains", false, true);

    private final String jsonName;
    private final boolean needsTarget;
    private final boolean needsValue;

    Keyword(String jsonName, boolean needsTarget, boolean needsValue) {
        this.jsonName = jsonName;
        this.needsTarget = needsTarget;
        this.needsValue = needsValue;
    }

    public String jsonName() {
        return jsonName;
    }

    public boolean needsTarget() {
        return needsTarget;
    }

    public boolean needsValue() {
        return needsValue;
    }

    public static Optional<Keyword> named(String name) {
        return Arrays.stream(values()).filter(k -> k.jsonName.equals(name)).findFirst();
    }

    public static String allNames() {
        return Arrays.stream(values()).map(Keyword::jsonName).collect(Collectors.joining(", "));
    }
}
