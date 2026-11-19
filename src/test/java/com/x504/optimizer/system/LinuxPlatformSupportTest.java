package com.x504.optimizer.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LinuxPlatformSupportTest {
    @TempDir Path temp;
    private final LinuxPlatformSupport support = new LinuxPlatformSupport();
    @Test void buildsSafeArchCommand() {
        var platform = new LinuxPlatformSupport.Platform("arch", "Arch Linux", "", "", LinuxPlatformSupport.Family.ARCH, "pacman");
        assertEquals(List.of("sudo", "pacman", "-S", "--needed", "inxi"), support.installCommand(platform, List.of("inxi")));
    }
    @Test void buildsDebianAndFedoraCommandsWithoutAutoConfirm() {
        var debian = new LinuxPlatformSupport.Platform("debian", "Debian", "12", "", LinuxPlatformSupport.Family.DEBIAN, "apt-get");
        var fedora = new LinuxPlatformSupport.Platform("fedora", "Fedora", "40", "", LinuxPlatformSupport.Family.FEDORA, "dnf");
        assertEquals(List.of("sudo", "apt-get", "install", "--no-install-recommends", "inxi"), support.installCommand(debian, List.of("inxi")));
        assertEquals(List.of("sudo", "dnf", "install", "inxi"), support.installCommand(fedora, List.of("inxi")));
    }
    @Test void rejectsUnknownFamilyAndMalformedPackageNames() {
        var unknown = new LinuxPlatformSupport.Platform("other", "Other", "", "", LinuxPlatformSupport.Family.UNKNOWN, "apk");
        assertThrows(IllegalStateException.class, () -> support.installCommand(unknown, List.of("tool")));
        var arch = new LinuxPlatformSupport.Platform("arch", "Arch", "", "", LinuxPlatformSupport.Family.ARCH, "pacman");
        assertThrows(IllegalArgumentException.class, () -> support.installCommand(arch, List.of("tool;rm")));
    }
    @Test void currentHostDetectionAlwaysReturnsAPlatform() {
        var platform = support.detect();
        assertNotNull(platform.family());
        assertNotNull(platform.packageManager());
        assertFalse(platform.name().isBlank());
    }
}