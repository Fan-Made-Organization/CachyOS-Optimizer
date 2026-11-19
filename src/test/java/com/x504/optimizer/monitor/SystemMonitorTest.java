package com.x504.optimizer.monitor;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class SystemMonitorTest {
    @Test void rejectsLongMonitoringSessions() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemMonitor().monitor(Duration.ofMinutes(6), Duration.ofSeconds(1)));
    }
    @Test void rejectsSubsecondIntervals() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemMonitor().monitor(Duration.ofSeconds(2), Duration.ofMillis(500)));
    }
}