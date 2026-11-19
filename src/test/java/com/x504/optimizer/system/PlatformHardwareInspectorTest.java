package com.x504.optimizer.system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlatformHardwareInspectorTest {
    @Test
    void returnsReadOnlyHardwareSectionsOnCurrentPlatform() {
        PlatformHardwareInspector.Report report = new PlatformHardwareInspector().inspect();
        assertNotNull(report);
        assertNotNull(report.graphics());
        assertNotNull(report.storage());
    }

    @Test
    void reportListsAreImmutable() {
        PlatformHardwareInspector.Report report =
                new PlatformHardwareInspector.Report(java.util.List.of("GPU"), java.util.List.of("Disk"));
        assertThrows(UnsupportedOperationException.class, () -> report.graphics().add("change"));
        assertThrows(UnsupportedOperationException.class, () -> report.storage().add("change"));
    }
}