package com.x504.optimizer.modules;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KernelModuleTest {
    @Test void reportsKernelAndBootInformationWithoutChangingSystem() {
        var report = new KernelModule().inspect();
        assertEquals("kernel", report.moduleId());
        assertTrue(report.findings().stream().anyMatch(line -> line.startsWith("Kernel activo:")));
        assertTrue(report.warnings().stream().anyMatch(line -> line.contains("no instala")));
    }
}