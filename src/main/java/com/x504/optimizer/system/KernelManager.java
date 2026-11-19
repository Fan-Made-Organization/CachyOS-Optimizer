package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Planificador conservador de kernels. Nunca elimina kernels ni modifica el bootloader. */
public final class KernelManager {
    public record KernelOption(String id, String label, List<String> packages, String note) {
        public KernelOption { packages = List.copyOf(packages); }
    }
    public record BootInventory(List<String> installedModuleTrees, List<String> bootFiles, String activeKernel) {
        public BootInventory { installedModuleTrees = List.copyOf(installedModuleTrees); bootFiles = List.copyOf(bootFiles); }
        public boolean hasPossibleFallback() {
            return installedModuleTrees.stream().anyMatch(v -> !v.equals(activeKernel))
                    || bootFiles.stream().anyMatch(v -> v.toLowerCase().contains("vmlinuz") || v.toLowerCase().contains("vmlinux"));
        }
    }

    public List<KernelOption> options(LinuxPlatformSupport.Platform platform) {
        return switch (platform.family()) {
            case ARCH -> List.of(
                    new KernelOption("default", "Kernel estándar de la distribución", List.of("linux", "linux-headers"), "Paquete estándar Arch; en derivadas los repositorios pueden variar."),
                    new KernelOption("lts", "Kernel LTS", List.of("linux-lts", "linux-lts-headers"), "Rama de soporte prolongado; conserva el kernel actual."),
                    new KernelOption("zen", "Kernel Zen", List.of("linux-zen", "linux-zen-headers"), "Kernel orientado a escritorio; no garantiza más FPS ni menor latencia.")
            );
            case DEBIAN -> List.of(new KernelOption("default", "Kernel oficial de la distribución",
                    List.of("linux-image-amd64", "linux-headers-amd64"), "Metapaquetes oficiales para Debian/Ubuntu x86_64; la disponibilidad depende de repositorios y arquitectura."));
            case FEDORA -> List.of(new KernelOption("default", "Kernel oficial Fedora",
                    List.of("kernel", "kernel-devel"), "Instala el kernel mantenido por Fedora y los paquetes de desarrollo; kernel-devel puede no coincidir hasta completar actualizaciones."));
            case SUSE -> List.of(new KernelOption("default", "Kernel estándar openSUSE",
                    List.of("kernel-default", "kernel-devel"), "Usa los paquetes oficiales de la distribución; revisa patrones/repositorios habilitados."));
            case UNKNOWN -> List.of();
        };
    }

    public List<String> installCommand(LinuxPlatformSupport support, LinuxPlatformSupport.Platform platform, KernelOption option) {
        if (!options(platform).contains(option)) throw new IllegalArgumentException("Opción de kernel no válida para esta distribución.");
        return support.installCommand(platform, option.packages());
    }

    public BootInventory inspectBootInventory() {
        String active = System.getProperty("os.version", "unknown");
        List<String> modules = new ArrayList<>();
        for (Path root : List.of(Path.of("/usr/lib/modules"), Path.of("/lib/modules"))) {
            if (!Files.isDirectory(root)) continue;
            try (var stream = Files.list(root)) {
                stream.filter(Files::isDirectory).map(p -> p.getFileName().toString()).forEach(modules::add);
            } catch (IOException ignored) { }
            if (!modules.isEmpty()) break;
        }
        List<String> boot = new ArrayList<>();
        for (Path root : List.of(Path.of("/boot"), Path.of("/boot/efi"), Path.of("/efi"))) {
            if (!Files.isDirectory(root)) continue;
            try (var stream = Files.list(root)) {
                stream.filter(Files::isRegularFile).map(p -> p.getFileName().toString())
                        .filter(n -> n.startsWith("vmlinuz") || n.startsWith("vmlinux") || n.startsWith("initramfs") || n.startsWith("initrd"))
                        .forEach(boot::add);
            } catch (IOException ignored) { }
        }
        return new BootInventory(modules.stream().distinct().sorted().toList(), boot.stream().distinct().sorted().toList(), active);
    }

    public List<String> recoveryGuide() {
        return List.of(
                "Antes de instalar: confirma que el kernel actual arranca y que conoces la clave de cifrado si usas disco cifrado.",
                "No desinstales el kernel actual. Mantén al menos una versión anterior y sus initramfs en /boot.",
                "Tras instalar, revisa los mensajes del gestor de paquetes y confirma que se generaron initramfs y una entrada de arranque.",
                "Reinicia solo cuando tengas acceso físico o remoto de recuperación y sepas abrir el menú del bootloader.",
                "Si el nuevo kernel falla: abre GRUB/systemd-boot, selecciona la entrada del kernel anterior y arranca con ella.",
                "Ya dentro del sistema, revisa journalctl -b -1 y los registros del gestor de paquetes; no borres el kernel que funciona.",
                "Si no aparece una entrada anterior o no puedes acceder al menú de arranque, detente y usa la documentación oficial de tu distribución o asistencia técnica."
        );
    }
}