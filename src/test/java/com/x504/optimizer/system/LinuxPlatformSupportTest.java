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
        Files.writeString(target, "ID=cachyos\nID_LIKE=arch\nPRETTY_NAME=\"CachyOS\"\n");
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
    @Test void recognizesAdditionalDistributionFamilies() throws Exception {
        assertFamily("ID=nobara\nPRETTY_NAME=\"Nobara Linux\"\n", LinuxPlatformSupport.Family.FEDORA);
        assertFamily("ID=zorin\nPRETTY_NAME=\"Zorin OS\"\n", LinuxPlatformSupport.Family.DEBIAN);
        assertFamily("ID=alpine\nPRETTY_NAME=\"Alpine Linux\"\n", LinuxPlatformSupport.Family.ALPINE);
        assertFamily("ID=gentoo\nPRETTY_NAME=\"Gentoo\"\n", LinuxPlatformSupport.Family.GENTOO);
        assertFamily("ID=void\nPRETTY_NAME=\"Void Linux\"\n", LinuxPlatformSupport.Family.VOID);
        assertFamily("ID=nixos\nPRETTY_NAME=\"NixOS\"\n", LinuxPlatformSupport.Family.NIXOS);
    }

    @Test void buildsSafeAlpineAndGentooCommands() {
        var alpine = new LinuxPlatformSupport.Platform("alpine", "Alpine", "3.20", "", LinuxPlatformSupport.Family.ALPINE, "apk");
        var gentoo = new LinuxPlatformSupport.Platform("gentoo", "Gentoo", "", "", LinuxPlatformSupport.Family.GENTOO, "emerge");
        assertEquals(List.of("sudo", "apk", "add", "btop"), support.installCommand(alpine, List.of("btop")));
        assertEquals(List.of("sudo", "emerge", "--ask", "btop"), support.installCommand(gentoo, List.of("btop")));
    }

    @Test void providesSeparateToolCategoriesForSupportedFamilies() {
        var arch = new LinuxPlatformSupport.Platform("cachyos", "CachyOS", "", "arch", LinuxPlatformSupport.Family.ARCH, "pacman");
        var categories = support.toolCategories(arch);
        assertTrue(categories.containsKey("Diagnóstico y hardware"));
        assertTrue(categories.containsKey("Gráficos y vídeo"));
        assertTrue(categories.containsKey("Almacenamiento"));
        assertTrue(categories.containsKey("Red"));
        assertFalse(categories.containsKey("Monitorización"));
        assertTrue(categories.containsKey("Gaming"));
        assertTrue(categories.containsKey("Audio y multimedia"));
        assertTrue(categories.containsKey("Desarrollo y depuración"));
        assertTrue(categories.containsKey("Energía y sensores"));
        assertTrue(categories.get("Gaming").contains("gamescope"));
        assertTrue(categories.get("Energía y sensores").containsAll(List.of("power-profiles-daemon", "tlp", "tuned", "thermald", "auto-cpufreq")));
        assertTrue(categories.get("Gaming").contains("vkbasalt-cli"));
        assertFalse(categories.get("Gaming").contains("vkbasalt"));

        var archLinux = new LinuxPlatformSupport.Platform("arch", "Arch Linux", "", "", LinuxPlatformSupport.Family.ARCH, "pacman");
        var archGaming = support.toolCategories(archLinux).get("Gaming");
        assertTrue(archGaming.contains("vkbasalt"));
        assertFalse(archGaming.contains("vkbasalt-cli"));
        assertEquals(support.diagnosticTools(arch).size(), support.diagnosticTools(arch).stream().distinct().count());
    }

    @Test void quotedOsReleaseValuesAreUnwrapped() throws Exception {
        Path file = temp.resolve("quoted-os-release");
        Files.writeString(file, "ID=arch\nPRETTY_NAME=\"Arch Linux\"\nVERSION_ID=\"rolling\"\n");
        var detected = new LinuxPlatformSupport(file).detect();
        assertEquals("Arch Linux", detected.name());
        assertEquals("rolling", detected.version());
    }

    @Test void blocksGenericPackageProfilesOnImmutableDistributions() {
        var bazzite = new LinuxPlatformSupport.Platform("bazzite", "Bazzite", "", "fedora", LinuxPlatformSupport.Family.FEDORA, "dnf");
        assertTrue(support.isImmutableOrImageBased(bazzite));
        assertTrue(support.toolCategories(bazzite).isEmpty());
        assertThrows(IllegalStateException.class, () -> support.installCommand(bazzite, List.of("mesa")));

        var silverblue = new LinuxPlatformSupport.Platform("silverblue", "Fedora Silverblue", "", "fedora", LinuxPlatformSupport.Family.FEDORA, "dnf");
        assertTrue(support.isImmutableOrImageBased(silverblue));

        var arch = new LinuxPlatformSupport.Platform("arch", "Arch Linux", "", "", LinuxPlatformSupport.Family.ARCH, "pacman");
        assertFalse(support.isImmutableOrImageBased(arch));
        assertFalse(support.toolCategories(arch).isEmpty());
    }

}
