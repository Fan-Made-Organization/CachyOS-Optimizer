package com.x504.optimizer.system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedDiagnosticsTest {
    @Test void reportContainsAllDiagnosticAreas() {
        var report = new AdvancedDiagnostics().inspect();
        assertNotNull(report.cpu());
        assertNotNull(report.memory());
        assertNotNull(report.storage());
        assertNotNull(report.network());
        assertNotNull(report.graphics());
        assertNotNull(report.services());
        assertNotNull(report.kernel());
        assertNotNull(report.gaming());
        assertFalse(report.cpu().isEmpty());
        assertFalse(report.memory().isEmpty());
        assertFalse(report.network().isEmpty());
        assertFalse(report.graphics().isEmpty());
    }

    @Test void reportExplainsPressureMetricsAndGraphicsLimitations() {
        var diagnostics = new AdvancedDiagnostics();
        assertTrue(diagnostics.inspectMemory().stream().anyMatch(line -> line.contains("PSI") || line.contains("memoria")));
        assertTrue(diagnostics.inspectGraphics().stream().anyMatch(line -> line.contains("aceleración") || line.contains("DRM")));
    }
}