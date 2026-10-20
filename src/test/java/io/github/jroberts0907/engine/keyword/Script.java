package io.github.jroberts0907.engine.keyword;

import java.util.List;

/**
 * A test case written as JSON steps, loaded by {@link ScriptLoader} and run by {@link KeywordRunner}.
 *
 * @param name        shown as the test's name in JUnit reports
 * @param description optional notes for people reading the script
 * @param steps       run in order; the script stops at the first failing step
 */
public record Script(String name, String description, List<Step> steps) {
}
