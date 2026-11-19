package com.x504.optimizer.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkHistoryTest {
    @TempDir Path temp;
    @Test void appendsAndComparesSnapshots() throws Exception {
        var suite = new BenchmarkSuite();
        var ram = suite.benchmarkMemory(8);
        var disk = suite.benchmarkStorage(8);
        var history = new BenchmarkHistory();
        Path before = temp.resolve("before.csv"), after = temp.resolve("after.csv");
        history.append(before, 8, 1000, ram, disk);
        history.append(after, 8, 1200, ram, disk);
        var result = history.compare(before, after);
        assertEquals(20.0, result.cpuPercent(), 0.001);
        assertNotNull(result.beforeTimestamp());
        assertNotNull(result.afterTimestamp());
    }
    @Test void rejectsUnknownHistoryFormat() {
        Path invalid = temp.resolve("invalid.csv");
        assertDoesNotThrow(() -> java.nio.file.Files.writeString(invalid, "bad"));
        assertThrows(java.io.IOException.class, () -> new BenchmarkHistory().compare(invalid, invalid));
    }
}