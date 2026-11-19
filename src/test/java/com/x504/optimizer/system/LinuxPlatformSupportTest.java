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
    @Test void recognizesMajorDistributionFamiliesFromOsRelease() throws Exception {
        assertFamily("ID=debian\nPRETTY_NAME=\"Debian GNU/Linux\"\n", LinuxPlatformSupport.Family.DEBIAN);
        assertFamily("ID=fedora\nPRETTY_NAME=\"Fedora Linux\"\n", LinuxPlatformSupport.Family.FEDORA);
        assertFamily("ID=opensuse-tumbleweed\nID_LIKE=opensuse suse\n", LinuxPlatformSupport.Family.SUSE);
        assertFamily("ID=cachyos\nID_LIKE=arch\n", LinuxPlatformSupport.Family.ARCH);
    }
    private void assertFamily(String release, LinuxPlatformSupport.Family expected) throws Exception {
        Path file = temp.resolve("os-release");
        Files.writeString(file, release);
        assertEquals(expected, new LinuxPlatformSupport(file).detect().family());
    }
    @Test void followsStandardOsReleaseSymlinkUsedByArchBasedSystems() throws Exception {
        Path target = temp.resolve("usr-lib-os-release");
        Path link = temp.resolve("etc-os-release");
        Files.writeString(target, "ID=cachyos\nID_LIKE=arch\nPRETTY_NAME=\\\"CachyOS\\\"\n");
        try {
            Files.createSymbolicLink(link, target.getFileName());
        } catch (UnsupportedOperationException | java.io.IOException | SecurityException ex) {
            return; // El sistema de archivos/entorno no permite crear enlaces simbólicos.
        }
        var detected = new LinuxPlatformSupport(link).detect();
        assertEquals(LinuxPlatformSupport.Family.ARCH, detected.family());
        assertEquals("cachyos", detected.id());
        assertEquals("CachyOS", detected.name());
    }
    @Test void currentHostDetectionAlwaysReturnsAPlatform() {
        var platform = support.detect();
        assertNotNull(platform.family());
        assertNotNull(platform.packageManager());
        assertFalse(platform.name().isBlank());
    }
}