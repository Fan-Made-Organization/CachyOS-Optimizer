package com.x504.optimizer.benchmark;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkSuiteTest {
    private final BenchmarkSuite suite = new BenchmarkSuite();
    @Test void runsBoundedMemoryBenchmark() {
        var result = suite.benchmarkMemory(8);
        assertEquals(8, result.mebibytes());
        assertTrue(result.readMiBs() > 0);
        assertTrue(result.writeMiBs() > 0);
    }
    @Test void rejectsOversizedMemoryBenchmark() {
        assertThrows(IllegalArgumentException.class, () -> suite.benchmarkMemory(128));
    }
    @Test void rejectsOversizedStorageBenchmark() {
        assertThrows(IllegalArgumentException.class, () -> suite.benchmarkStorage(128));
    }
}