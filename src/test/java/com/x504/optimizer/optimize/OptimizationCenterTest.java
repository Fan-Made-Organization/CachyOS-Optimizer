package com.x504.optimizer.optimize;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationCenterTest {
    @Test void onlyKnownPowerProfilesAreAccepted() {
        assertTrue(OptimizationCenter.isValidPowerProfile("balanced"));
        assertTrue(OptimizationCenter.isValidPowerProfile("performance"));
        assertTrue(OptimizationCenter.isValidPowerProfile("power-saver"));
        assertFalse(OptimizationCenter.isValidPowerProfile("powersave"));
        assertFalse(OptimizationCenter.isValidPowerProfile("../../etc"));
        assertFalse(OptimizationCenter.isValidPowerProfile(null));
    }

    @Test void onlyAllowlistedCpuGovernorsAreAccepted() {
        assertTrue(OptimizationCenter.isValidGovernor("performance"));
        assertTrue(OptimizationCenter.isValidGovernor("powersave"));
        assertTrue(OptimizationCenter.isValidGovernor("schedutil"));
        assertFalse(OptimizationCenter.isValidGovernor("../../etc"));
        assertFalse(OptimizationCenter.isValidGovernor(null));
    }

    @Test void exposesAllSixOptimizationAreas() {
        var center = new OptimizationCenter(java.nio.file.Path.of("target", "test-state", "previous-profile"));
        assertEquals(6, center.categoryStatus().size());
        assertTrue(center.categoryStatus().stream().anyMatch(line -> line.startsWith("Kernel:")));
        assertTrue(center.categoryStatus().stream().anyMatch(line -> line.startsWith("RAM:")));
    }
}