package dev.letsgoaway.geyserextras.core.preferences;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Writes preference files off the network threads, one at a time and in order, and atomically:
 * a file is either the old or the new version, never truncated. Saving used to start a new thread
 * per save that truncated the file before writing it, so a shutdown in between left empty files
 * and players lost their settings and packs (upstream #61).
 */
public final class PreferencesIO {
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "GeyserExtras preferences writer");
        thread.setDaemon(true);
        return thread;
    });

    private PreferencesIO() {
    }

    /**
     * Queues a write of {@code content} to {@code file}, or its deletion when {@code content} is null.
     */
    public static Future<?> submit(Path file, @Nullable String content, Consumer<IOException> onError) {
        return WRITER.submit(() -> {
            try {
                write(file, content);
            } catch (IOException e) {
                onError.accept(e);
            }
        });
    }

    /**
     * Waits for queued writes, for shutdown.
     */
    public static void flush(long timeout, TimeUnit unit) {
        try {
            WRITER.submit(() -> {
            }).get(timeout, unit);
        } catch (Exception ignored) {
            // Best effort: never block a shutdown on a stuck disk
        }
    }

    public static void write(Path file, @Nullable String content) throws IOException {
        if (content == null) {
            Files.deleteIfExists(file);
            return;
        }
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temp = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
