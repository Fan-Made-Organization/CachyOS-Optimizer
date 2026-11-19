package com.x504.optimizer.monitor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LiveMetricsTest {
    @Test
    void capturesSafeLiveMetrics() {
        LiveMetrics.Sample sample = new LiveMetrics().sample();
        assertNotNull(sample);
        assertTrue(sample.cpuPercent() >= 0 && sample.cpuPercent() <= 100);
        assertTrue(sample.memoryPercent() >= 0 && sample.memoryPercent() <= 100);
        assertTrue(sample.temperatureCelsius() == -1
                || (sample.temperatureCelsius() >= -20 && sample.temperatureCelsius() <= 150));
        assertTrue(sample.gpuPercent() == -1
                || (sample.gpuPercent() >= 0 && sample.gpuPercent() <= 100));
        assertTrue(sample.logicalCpus() > 0);
    }
}