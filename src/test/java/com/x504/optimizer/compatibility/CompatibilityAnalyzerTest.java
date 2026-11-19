package com.x504.optimizer.compatibility;

import com.x504.optimizer.system.SystemInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompatibilityAnalyzerTest {
    @Test
    void calculatesScoreOnlyFromKnownComponents() {
        SystemInfo info = new SystemInfo("Linux", "CachyOS", "rolling",
                "6.12.1", "x86_64", "KDE", true, true);
        CompatibilityReport report = new CompatibilityAnalyzer().analyze(info);
        assertEquals(90, report.percentage());
        assertEquals(4, report.evaluatedComponents());
        assertEquals(0, report.unknownComponents());
    }

    @Test
    void doesNotAwardPointsToUnknownComponents() {
        SystemInfo info = new SystemInfo("Linux", "Linux", "Desconocida",
                "Desconocido", "mips", "No detectado", true, false);
        CompatibilityReport report = new CompatibilityAnalyzer().analyze(info);
        assertEquals(100, report.percentage());
        assertEquals(1, report.evaluatedComponents());
        assertEquals(3, report.unknownComponents());
    }
}
