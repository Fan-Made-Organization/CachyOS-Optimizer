package com.x504.optimizer.system;

import com.x504.optimizer.compatibility.*;
import com.x504.optimizer.hardware.HardwareSnapshot;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SystemAnalysisDashboardTest {
    @Test
    void rendersDetectedValuesAndDoesNotInventCompatibility() {
        var system = new SystemInfo("Linux", "CachyOS", "rolling", "6.12.1", "amd64", "KDE", true, true);
        var hardware = new HardwareSnapshot("CPU de prueba", 8, 16L * 1024 * 1024,
                List.of("PCI 0x1002:0x1234"));
        var compatibility = new CompatibilityReport(List.of(
                new CompatibilityItem("Linux", CompatibilityStatus.COMPATIBLE, "Detectado"),
                new CompatibilityItem("Distribución", CompatibilityStatus.COMPATIBLE, "Detectada"),
                new CompatibilityItem("Arquitectura", CompatibilityStatus.COMPATIBLE, "Detectada"),
                new CompatibilityItem("Escritorio", CompatibilityStatus.UNKNOWN, "No detectado")
        ), 75, 3, 1);

        String dashboard = new SystemAnalysisDashboard().render(system, hardware, compatibility);
        assertTrue(dashboard.contains("X504-CACHYOS-OPTIMIZER"));
        assertTrue(dashboard.contains("Sistema:       CachyOS"));
        assertTrue(dashboard.contains("Base:          Arch Linux"));
        assertTrue(dashboard.contains("Kernel:        Linux 6.12.1"));
        assertTrue(dashboard.contains("Escritorio:    KDE Plasma"));
        assertTrue(dashboard.contains("Arquitectura:  x86_64"));
        assertTrue(dashboard.contains("Compatibilidad: 75%"));
        assertTrue(dashboard.contains("PARCIAL / DATOS INCOMPLETOS"));
        assertFalse(dashboard.contains("EXCELENTE"));
    }
}
