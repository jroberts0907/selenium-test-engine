package io.github.jroberts0907.engine.keyword;

/**
 * One line of a test script, e.g. {"action": "type", "target": "testid=username", "value": "standard_user"}.
 *
 * @param action a {@link Keyword} name
 * @param target where to act: a locator (see {@link Locators}), or a path for "open"
 * @param value  text to type, option to select, or expected result for assertions
 */
public record Step(String action, String target, String value) {

    public Keyword keyword() {
        return Keyword.named(action).orElseThrow(() -> new IllegalStateException("Unknown action: " + action));
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(action);
        if (target != null) {
            text.append(' ').append(target);
        }
        if (value != null) {
            text.append(" \"").append(value).append('"');
        }
        return text.toString();
    }
}
