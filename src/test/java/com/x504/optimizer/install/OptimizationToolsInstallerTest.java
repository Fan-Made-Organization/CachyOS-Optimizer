package com.x504.optimizer.install;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationToolsInstallerTest {
    private final OptimizationToolsInstaller installer = new OptimizationToolsInstaller();

    @Test void fullBundleContainsDiagnosticsAndGamingTools() {
        var packages = installer.packages(true);
        assertTrue(packages.containsAll(java.util.List.of("inxi", "lshw", "smartmontools", "vulkan-tools", "mesa-utils", "libva-utils", "gamemode", "mangohud", "gamescope", "power-profiles-daemon", "zram-generator", "thermald", "cpupower", "lib32-gamemode", "lib32-mangohud", "protontricks", "intel-gpu-tools", "radeontop", "fastfetch")));
        assertFalse(packages.contains("nvtop"));
        assertFalse(packages.contains("powertop"));
        assertEquals(packages.size(), packages.stream().distinct().count());
    }

    @Test void usesCachyOsSpecificVkBasaltPackage() {
        var cachyPackages = OptimizationToolsInstaller.packagesFor(true, true);
        assertTrue(cachyPackages.contains("vkbasalt-cli"));
        assertFalse(cachyPackages.contains("vkbasalt"));
        assertFalse(cachyPackages.contains("lib32-vkbasalt"));

        var archPackages = OptimizationToolsInstaller.packagesFor(true, false);
        assertTrue(archPackages.contains("vkbasalt"));
        assertTrue(archPackages.contains("lib32-vkbasalt"));
        assertFalse(archPackages.contains("vkbasalt-cli"));
    }

    @    @Test void commandUsesOnlyFixedPacmanArgumentsAndPackages() {
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