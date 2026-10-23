package dev.letsgoaway.geyserextras.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunSafelyTest {
    @Test
    void runsTheStep() {
        AtomicBoolean ran = new AtomicBoolean();
        GeyserExtras.runSafely("test", () -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void swallowsFailuresEvenWithoutAServer() {
        // SERVER is null here, like after a start that failed before the core was created
        assertDoesNotThrow(() -> GeyserExtras.runSafely("test", () -> {
            throw new NullPointerException("CORE is null");
        }));
        assertDoesNotThrow(() -> GeyserExtras.runSafely("test", () -> {
            throw new NoSuchMethodError("missing API");
        }));
    }
}
