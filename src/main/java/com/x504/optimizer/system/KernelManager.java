package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Planificador conservador de kernels. Nunca elimina kernels ni modifica el bootloader. */
public final class KernelManager {
    public record KernelOption(String id, String label, List<String> packages, String note) {
        public KernelOption { packages = List.copyOf(packages); }
    }
    public record BootInventory(List<String> installedModuleTrees, List<String> bootFiles, String activeKernel) {
        public BootInventory { installedModuleTrees = List.copyOf(installedModuleTrees); bootFiles = List.copyOf(bootFiles); }
        public boolean hasPossibleFallback() {
            long otherKernelTrees = installedModuleTrees.stream().filter(v -> !v.equals(activeKernel)).count();
            long bootKernelImages = bootFiles.stream().filter(v -> v.startsWith("vmlinuz") || v.startsWith("vmlinux")).count();
            return otherKernelTrees > 0 || bootKernelImages > 1;
        }
    }

    public List<KernelOption> options(LinuxPlatformSupport.Platform platform) {
        return switch (platform.family()) {
            case ARCH -> platform.id().equals("cachyos")
                    ? List.of(
                            new KernelOption("default", "Kernel estándar CachyOS", List.of("linux-cachyos", "linux-cachyos-headers"), "Paquetes del repositorio CachyOS; verifica que el repositorio correspondiente esté habilitado."),
                            new KernelOption("lts", "Kernel LTS", List.of("linux-lts", "linux-lts-headers"), "Rama de soporte prolongado; conserva el kernel actual.")
                    )
                    : List.of(
                            new KernelOption("default", "Kernel estándar de la distribución", List.of("linux", "linux-headers"), "Paquete estándar Arch; en derivadas los repositorios pueden variar."),
                            new KernelOption("lts", "Kernel LTS", List.of("linux-lts", "linux-lts-headers"), "Rama de soporte prolongado; conserva el kernel actual."),
                            new KernelOption("zen", "Kernel Zen", List.of("linux-zen", "linux-zen-headers"), "Kernel orientado a escritorio; no garantiza más FPS ni menor latencia.")
                    );
            case DEBIAN -> {
                boolean ubuntuBased = platform.id().equals("ubuntu") || platform.idLike().contains("ubuntu");
                boolean arm64 = System.getProperty("os.arch", "").equalsIgnoreCase("aarch64");
                List<String> packages = ubuntuBased
                        ? List.of("linux-generic", "linux-headers-generic")
                        : arm64 ? List.of("linux-image-arm64", "linux-headers-arm64")
                        : List.of("linux-image-amd64", "linux-headers-amd64");
                String label = ubuntuBased ? "Kernel genérico Ubuntu y headers" : "Kernel oficial Debian y headers";
                yield List.of(new KernelOption("default", label, packages,
                        "Metapaquetes de la familia detectada; derivadas y repositorios personalizados pueden variar."));
            }
            case FEDORA -> List.of(new KernelOption("default", "Kernel oficial Fedora",
                    List.of("kernel", "kernel-devel"), "Instala el kernel mantenido por Fedora y los paquetes de desarrollo; kernel-devel puede no coincidir hasta completar actualizaciones."));
            case SUSE -> List.of(new KernelOption("default", "Kernel estándar openSUSE",
                    List.of("kernel-default", "kernel-devel"), "Usa los paquetes oficiales de la distribución; revisa patrones/repositorios habilitados."));
            case ALPINE, GENTOO, VOID, NIXOS, UNKNOWN -> List.of();
        };
    }

    public List<String> installCommand(LinuxPlatformSupport support, LinuxPlatformSupport.Platform platform, KernelOption option) {
        if (!options(platform).contains(option)) throw new IllegalArgumentException("Opción de kernel no válida para esta distribución.");
        return support.installCommand(platform, option.packages());
    }

    /** Comando de consulta sin privilegios para comprobar paquetes instalados. */
    public List<String> packageQueryCommand(LinuxPlatformSupport.Platform platform, String packageName) {
        if (packageName == null || !packageName.matches("[A-Za-z0-9@._+-]+"))
            throw new IllegalArgumentException("Nombre de paquete no válido.");
        return switch (platform.family()) {
            case ARCH -> List.of("pacman", "-Q", packageName);
            case DEBIAN -> List.of("dpkg-query", "-W", "-f=${Status}", "--", packageName);
            case FEDORA -> List.of("rpm", "-q", packageName);
            case SUSE -> List.of("zypper", "search", "--installed-only", "--match-exact", "--type", "package", packageName);
            case ALPINE, GENTOO, VOID, NIXOS, UNKNOWN -> throw new IllegalStateException("No hay consulta de paquetes para esta distribución.");
        };
    }

    /** Devuelve true/false si la consulta terminó; null si no pudo determinarse. */
    public Boolean isPackageInstalled(LinuxPlatformSupport.Platform platform, String packageName) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(packageQueryCommand(platform, packageName)).redirectErrorStream(true).start();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return null;
        }
        return process.exitValue() == 0;
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