package com.x504.optimizer.system;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KernelManagerTest {
    private final KernelManager manager = new KernelManager();
    private final LinuxPlatformSupport support = new LinuxPlatformSupport();

    @Test void offersArchDefaultLtsAndZenWithMatchingHeaders() {
        var arch = platform(LinuxPlatformSupport.Family.ARCH, "pacman");
        var options = manager.options(arch);
        assertEquals(3, options.size());
        assertEquals(List.of("linux-lts", "linux-lts-headers"), options.get(1).packages());
        assertEquals(List.of("linux-zen", "linux-zen-headers"), options.get(2).packages());
        assertEquals(List.of("sudo", "pacman", "-S", "--needed", "linux", "linux-headers"), manager.installCommand(support, arch, options.get(0)));
    }

    @Test void usesCachyOsAndUbuntuSpecificKernelMetapackages() {
        var cachy = new LinuxPlatformSupport.Platform("cachyos", "CachyOS", "", "arch", LinuxPlatformSupport.Family.ARCH, "pacman");
        var ubuntu = new LinuxPlatformSupport.Platform("ubuntu", "Ubuntu", "", "debian", LinuxPlatformSupport.Family.DEBIAN, "apt-get");
        assertEquals(List.of("linux-cachyos", "linux-cachyos-headers"), manager.options(cachy).get(0).packages());
        assertEquals(List.of("linux-generic", "linux-headers-generic"), manager.options(ubuntu).get(0).packages());
    }

    @Test void offersOfficialKernelPackagesForDebianFedoraAndSuse() {
        assertEquals(List.of("linux-image-amd64", "linux-headers-amd64"), manager.options(platform(LinuxPlatformSupport.Family.DEBIAN, "apt-get")).get(0).packages());
        assertEquals(List.of("kernel", "kernel-devel"), manager.options(platform(LinuxPlatformSupport.Family.FEDORA, "dnf")).get(0).packages());
        assertEquals(List.of("kernel-default", "kernel-devel"), manager.options(platform(LinuxPlatformSupport.Family.SUSE, "zypper")).get(0).packages());
    }

    @Test void unknownDistributionHasNoKernelInstallPlan() {
        assertTrue(manager.options(platform(LinuxPlatformSupport.Family.UNKNOWN, "No detectado")).isEmpty());
    }

    @Test void rejectsOptionNotBelongingToDetectedDistribution() {
        var arch = platform(LinuxPlatformSupport.Family.ARCH, "pacman");
        var debianOption = manager.options(platform(LinuxPlatformSupport.Family.DEBIAN, "apt-get")).get(0);
        assertThrows(IllegalArgumentException.class, () -> manager.installCommand(support, arch, debianOption));
    }

    @Test void recoveryGuideNeverRecommendsRemovingExistingKernel() {
        var guide = String.join(" ", manager.recoveryGuide()).toLowerCase();
        assertTrue(guide.contains("kernel anterior"));
        assertTrue(guide.contains("no desinstales"));
        assertFalse(guide.contains("pacman -r"));
    }

    private static LinuxPlatformSupport.Platform platform(LinuxPlatformSupport.Family family, String manager) {
        return new LinuxPlatformSupport.Platform("test", "Test Linux", "1", "", family, manager);
    }
}