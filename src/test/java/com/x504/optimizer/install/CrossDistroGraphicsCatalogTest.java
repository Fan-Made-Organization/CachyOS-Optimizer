package com.x504.optimizer.install;

import com.x504.optimizer.system.LinuxPlatformSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CrossDistroGraphicsCatalogTest {
    private final CrossDistroGraphicsCatalog catalog=new CrossDistroGraphicsCatalog();
    @Test void intelAndAmdUseDistributionSpecificMesaVulkanBundles() {
        assertTrue(catalog.packages(CrossDistroGraphicsCatalog.Vendor.INTEL,LinuxPlatformSupport.Family.DEBIAN).contains("mesa-vulkan-drivers"));
        assertTrue(catalog.packages(CrossDistroGraphicsCatalog.Vendor.AMD,LinuxPlatformSupport.Family.FEDORA).contains("mesa-dri-drivers"));
    }
    @Test void proprietaryNvidiaIsNotGuessedOnFedoraSuseOrAlpine() {
        for(var f: java.util.List.of(LinuxPlatformSupport.Family.FEDORA,LinuxPlatformSupport.Family.SUSE,LinuxPlatformSupport.Family.ALPINE))
            assertTrue(catalog.packages(CrossDistroGraphicsCatalog.Vendor.NVIDIA,f).isEmpty());
    }
    @Test void unsupportedFamiliesStayReadOnly() {
        assertTrue(catalog.packages(CrossDistroGraphicsCatalog.Vendor.INTEL,LinuxPlatformSupport.Family.NIXOS).isEmpty());
    }
    @Test void directxPackagesAreNotPresentedAsNativeDrivers() {
        assertTrue(catalog.compatibilityPackages(LinuxPlatformSupport.Family.ARCH).containsAll(java.util.List.of("dxvk","vkd3d")));
    }
}
