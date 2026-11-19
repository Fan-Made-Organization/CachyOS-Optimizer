package com.x504.optimizer.install;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraphicsDriverInstallerTest {
    private final GraphicsDriverInstaller installer = new GraphicsDriverInstaller();

    @Test void intelBundleContainsOpenGlVulkanAndMediaDrivers() {
        assertTrue(installer.packages(GraphicsDriverInstaller.Vendor.INTEL).containsAll(java.util.List.of("mesa", "lib32-mesa", "vulkan-intel", "lib32-vulkan-intel", "intel-media-driver")));
    }
    @Test void amdBundleContainsMesaAndRadvVulkanDrivers() {
        assertTrue(installer.packages(GraphicsDriverInstaller.Vendor.AMD).containsAll(java.util.List.of("mesa", "lib32-mesa", "vulkan-radeon", "lib32-vulkan-radeon")));
    }
    @Test void nvidiaBundleIncludesDriverAndUserspaceLibraries() {
        assertTrue(installer.packages(GraphicsDriverInstaller.Vendor.NVIDIA).containsAll(java.util.List.of("nvidia-open", "nvidia-utils", "lib32-nvidia-utils")));
    }
    @Test void directxCompatibilityUsesTranslationLayers() {
        assertTrue(installer.compatibilityPackages().containsAll(java.util.List.of("dxvk", "vkd3d", "wine-staging")));
    }
    @Test void commandIsFixedPacmanInstallAndRejectsInvalidPackageNames() {
        assertEquals(java.util.List.of("sudo", "pacman", "-S", "--needed", "mesa"), installer.command(java.util.List.of("mesa")));
        assertThrows(IllegalArgumentException.class, () -> installer.command(java.util.List.of("mesa;rm")));
    }
}