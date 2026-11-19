package com.x504.optimizer.install;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationToolsInstallerTest {
    private final OptimizationToolsInstaller installer = new OptimizationToolsInstaller();

    @Test void fullBundleContainsDiagnosticsAndGamingTools() {
        var packages = installer.packages(true);
        assertTrue(packages.containsAll(java.util.List.of("inxi", "smartmontools", "vulkan-tools", "gamemode", "mangohud", "gamescope", "power-profiles-daemon", "zram-generator")));
        assertEquals(packages.size(), packages.stream().distinct().count());
    }

    @Test void commandUsesOnlyFixedPacmanArgumentsAndPackages() {
        var command = installer.command(true);
        assertEquals(java.util.List.of("sudo", "pacman", "-S", "--needed"), command.subList(0, 4));
        assertFalse(command.contains("-Rns"));
        assertFalse(command.contains("--noconfirm"));
    }

    @Test void diagnosticsBundleDoesNotIncludeGamingExtras() {
        assertFalse(installer.packages(false).contains("gamescope"));
        assertTrue(installer.packages(false).contains("vulkan-tools"));
    }
}