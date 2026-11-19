package com.x504.optimizer.system;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CommandExecutorTest {
    private final CommandExecutor executor = new CommandExecutor();

    @Test
    void runsAllowlistedReadOnlyCommand() throws Exception {
        var result = executor.executeReadOnly(List.of("uname", "-s"), Duration.ofSeconds(3));
        assertFalse(result.timedOut());
        assertTrue(result.successful());
        assertFalse(result.standardOutput().isBlank());
    }

    @Test
    void rejectsCommandsOutsideAllowlist() {
        assertThrows(SecurityException.class,
                () -> executor.executeReadOnly(List.of("sh", "-c", "echo unsafe"), Duration.ofSeconds(1)));
    }

    @Test
    void rejectsEmptyCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeReadOnly(List.of(), Duration.ofSeconds(1)));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeReadOnly(java.util.Arrays.asList("uname", null), Duration.ofSeconds(1)));
    }

    @Test
    void rejectsNullByteArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeReadOnly(List.of("uname", "bad\u0000argument"), Duration.ofSeconds(1)));
    }

    @Test
    void rejectsInvalidTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeReadOnly(List.of("uname"), Duration.ZERO));
    }
}
