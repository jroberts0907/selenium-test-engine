package io.github.jroberts0907.engine.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests for the engine itself: parsing, checking, locators and variables. No browser needed. */
class KeywordEngineTest {

    private static String scriptWithStep(String stepJson) {
        return """
                { "name": "example", "steps": [ %s ] }
                """.formatted(stepJson);
    }

    private static IllegalArgumentException rejected(String json) {
        return assertThrows(IllegalArgumentException.class, () -> ScriptLoader.parse(json, "example.json"));
    }

    @Test
    void parsesAValidScript() {
        Script script = ScriptLoader.parse("""
                {
                  "name": "sign in",
                  "description": "optional notes",
                  "steps": [
                    { "action": "open", "target": "/" },
                    { "action": "type", "target": "testid=username", "value": "standard_user" },
                    { "action": "assertUrlContains", "value": "/inventory.html" }
                  ]
                }
                """, "example.json");

        assertEquals("sign in", script.name());
        assertEquals(3, script.steps().size());
        assertEquals(Keyword.TYPE, script.steps().get(1).keyword());
        assertNull(script.steps().get(0).value());
    }

    @Test
    void unknownActionNamesTheStepAndListsKnownActions() {
        IllegalArgumentException e = rejected(scriptWithStep("""
                { "action": "open", "target": "/" }, { "action": "clik", "target": "#go" }
                """));

        assertTrue(e.getMessage().startsWith("example.json: step 2: unknown action \"clik\""), e.getMessage());
        assertTrue(e.getMessage().contains("click"), e.getMessage());
    }

    @Test
    void misspelledFieldIsRejected() {
        IllegalArgumentException e = rejected(scriptWithStep("""
                { "action": "type", "target": "#name", "valu": "x" }
                """));

        assertTrue(e.getMessage().contains("valu"), e.getMessage());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            missing target           | { "action": "click" }                                      | "click" needs a "target"
            missing value            | { "action": "type", "target": "#name" }                    | "type" needs a "value"
            count that isn't numeric | { "action": "assertCount", "target": ".row", "value": "a" } | value must be a whole number
            """)
    void incompleteStepIsRejected(String why, String stepJson, String expectedMessage) {
        IllegalArgumentException e = rejected(scriptWithStep(stepJson));

        assertTrue(e.getMessage().contains(expectedMessage), e.getMessage());
    }

    @Test
    void emptyValueIsAllowed() {
        ScriptLoader.parse(scriptWithStep("""
                { "action": "type", "target": "#name", "value": "" }
                """), "example.json");
    }

    @Test
    void scriptNeedsANameAndSteps() {
        assertTrue(rejected("{ \"steps\": [ { \"action\": \"open\", \"target\": \"/\" } ] }")
                .getMessage().contains("\"name\" is required"));
        assertTrue(rejected("{ \"name\": \"empty\", \"steps\": [] }")
                .getMessage().contains("at least one step"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            testid=username   | By.cssSelector: [data-test='username']
            id=user-name      | By.id: user-name
            xpath=(//button)[1] | By.xpath: (//button)[1]
            css=.title        | By.cssSelector: .title
            .title            | By.cssSelector: .title
            """)
    void locatorPrefixes(String target, String expected) {
        assertEquals(expected, Locators.parse(target).toString());
    }

    @Test
    void variablesAreReplaced() {
        Map<String, String> values = Map.of("testUser", "standard_user", "password", "secret_sauce");

        assertEquals("standard_user", Variables.resolve("${testUser}", values));
        assertEquals("user standard_user / secret_sauce",
                Variables.resolve("user ${testUser} / ${password}", values));
        assertEquals("no placeholders", Variables.resolve("no placeholders", values));
        assertNull(Variables.resolve(null, values));
    }

    @Test
    void unknownVariableIsAnError() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Variables.resolve("${tesUser}", Map.of("testUser", "x")));

        assertTrue(e.getMessage().contains("${tesUser}"), e.getMessage());
    }
}
