package io.github.jroberts0907.engine.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.Augmenter;

/**
 * Records the page as video frames using Chromium's DevTools screencast (Chrome and Edge only).
 * Only the page is captured, never the desktop, and it works headless.
 *
 * Frames are saved as frames/00001.jpg, ... with frames.csv listing each frame's capture time
 * (epoch ms), which lines up with the step times in steps.csv. Chromium sends a frame only when
 * the page repaints, so a still page produces no frames.
 */
public final class BrowserRecorder implements AutoCloseable {

    private final DevTools devTools;
    private final Path framesDir;
    private final Path index;
    private final AtomicInteger count = new AtomicInteger();
    // Each frame must be acknowledged before Chromium sends the next. Acks go through their own thread
    // because sending a command from inside an event listener would wait on the listener's own thread.
    private final ExecutorService acks = Executors.newSingleThreadExecutor();

    private BrowserRecorder(DevTools devTools, Path dir) throws IOException {
        this.devTools = devTools;
        this.framesDir = Files.createDirectories(dir.resolve("frames"));
        this.index = dir.resolve("frames.csv");
        Files.writeString(index, "frame,captured_ms\n");
    }

    public static BrowserRecorder start(WebDriver driver, Path dir, int maxWidth) {
        WebDriver withDevTools = driver instanceof HasDevTools ? driver : new Augmenter().augment(driver);
        if (!(withDevTools instanceof HasDevTools hasDevTools)) {
            throw new IllegalStateException("Video recording needs Chrome or Edge (DevTools); got " + driver);
        }
        DevTools devTools = hasDevTools.getDevTools();
        devTools.createSession();
        try {
            BrowserRecorder recorder = new BrowserRecorder(devTools, dir);
            devTools.addListener(new Event<Map<String, Object>>("Page.screencastFrame",
                    input -> input.read(Json.MAP_TYPE)), recorder::saveFrame);
            devTools.send(new Command<Void>("Page.startScreencast",
                    Map.of("format", "jpeg", "quality", 90, "maxWidth", maxWidth, "everyNthFrame", 1)));
            return recorder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void saveFrame(Map<String, Object> frame) {
        Object sessionId = frame.get("sessionId");
        acks.submit(() -> devTools.send(new Command<Void>("Page.screencastFrameAck", Map.of("sessionId", sessionId))));

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) frame.get("metadata");
        long capturedMs = Math.round(((Number) metadata.get("timestamp")).doubleValue() * 1000);
        int n = count.incrementAndGet();
        try {
            Files.write(framesDir.resolve("%05d.jpg".formatted(n)), Base64.getDecoder().decode((String) frame.get("data")));
            Files.writeString(index, n + "," + capturedMs + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            devTools.send(new Command<Void>("Page.stopScreencast", Map.of()));
        } finally {
            acks.shutdown();
            try {
                acks.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            devTools.close();
        }
    }
}
