package io.github.jroberts0907.engine.keyword;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads test scripts from JSON and checks them before any browser starts, so a typo
 * fails fast with the file and step number instead of halfway through a run.
 */
public final class ScriptLoader {

    // Unknown fields (e.g. "valu" for "value") are rejected by default
    private static final ObjectMapper JSON = new ObjectMapper();

    private ScriptLoader() {
    }

    /** A script and the file it came from. */
    public record Loaded(Path file, Script script) {
    }

    /** Loads every .json file in a classpath folder, sorted by file name. */
    public static List<Loaded> loadFolder(String classpathFolder) {
        URL url = ScriptLoader.class.getClassLoader().getResource(classpathFolder);
        if (url == null) {
            throw new IllegalArgumentException("No folder on the classpath named " + classpathFolder);
        }
        try (Stream<Path> files = Files.list(Path.of(url.toURI()))) {
            List<Loaded> loaded = new ArrayList<>();
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                loaded.add(new Loaded(file, parse(Files.readString(file), file.getFileName().toString())));
            }
            return loaded;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Parses and checks one script. {@code source} names it in error messages. */
    public static Script parse(String json, String source) {
        Script script;
        try {
            script = JSON.readValue(json, Script.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(source + ": " + e.getOriginalMessage(), e);
        }
        check(script, source);
        return script;
    }

    private static void check(Script script, String source) {
        if (isBlank(script.name())) {
            throw new IllegalArgumentException(source + ": \"name\" is required");
        }
        if (script.steps() == null || script.steps().isEmpty()) {
            throw new IllegalArgumentException(source + ": needs at least one step");
        }
        for (int i = 0; i < script.steps().size(); i++) {
            checkStep(script.steps().get(i), source + ": step " + (i + 1));
        }
    }

    private static void checkStep(Step step, String where) {
        Keyword keyword = Keyword.named(step.action()).orElseThrow(() -> new IllegalArgumentException(
                where + ": unknown action \"" + step.action() + "\". Known actions: " + Keyword.allNames()));

        if (keyword.needsTarget() && isBlank(step.target())) {
            throw new IllegalArgumentException(where + ": \"" + keyword.jsonName() + "\" needs a \"target\"");
        }
        // An empty value is allowed (e.g. typing nothing into a field), a missing one is not
        if (keyword.needsValue() && step.value() == null) {
            throw new IllegalArgumentException(where + ": \"" + keyword.jsonName() + "\" needs a \"value\"");
        }
        if (keyword == Keyword.ASSERT_COUNT && !step.value().matches("\\d+")) {
            throw new IllegalArgumentException(where + ": \"assertCount\" value must be a whole number");
        }
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
