package com.x504.optimizer.benchmark;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkEngineTest {
    @Test
    void producesOrderedMetricsForBoundedWorkload() {
        BenchmarkResult result = new BenchmarkEngine().runCpuMicrobenchmark(10_000, 3);
        assertEquals(10_000, result.iterationsPerRun());
        assertEquals(3, result.runs());
        assertTrue(result.minimumNanos() <= result.medianNanos());
        assertTrue(result.medianNanos() <= result.maximumNanos());
        assertTrue(result.operationsPerSecond() >= 0);
    }

    @Test
    void rejectsUnboundedWorkloads() {
        var engine = new BenchmarkEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.runCpuMicrobenchmark(1, 3));
        assertThrows(IllegalArgumentException.class, () -> engine.runCpuMicrobenchmark(10_000, 6));
    }
}
