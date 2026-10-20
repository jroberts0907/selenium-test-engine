package io.github.jroberts0907.engine.keyword;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Replaces ${name} placeholders in script text, so one script can run with different data. */
public final class Variables {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{(\\w+)}");

    private Variables() {
    }

    /** Returns text with every ${name} replaced; null stays null. Unknown names are an error. */
    public static String resolve(String text, Map<String, String> values) {
        if (text == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = values.get(name);
            if (value == null) {
                throw new IllegalArgumentException(
                        "Unknown variable ${" + name + "}. Known variables: " + values.keySet());
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
