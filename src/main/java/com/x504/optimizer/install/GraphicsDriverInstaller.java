package com.x504.optimizer.install;

import java.io.IOException;
import com.x504.optimizer.system.LinuxPlatformSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Paquetes gráficos para Arch/CachyOS. No mezcla controladores de fabricantes distintos. */
public final class GraphicsDriverInstaller {
    public enum Vendor { INTEL, AMD, NVIDIA, UNKNOWN }

    public List<String> detectGpuVendors() {
        Path drm = Path.of("/sys/class/drm");
        if (!Files.isDirectory(drm)) return List.of("No se pudo inspeccionar /sys/class/drm.");
        List<String> found = new ArrayList<>();
        try (var entries = Files.list(drm)) {
            entries.filter(p -> p.getFileName().toString().matches("card[0-9]+"))
                    .map(p -> p.resolve("device/vendor"))
                    .filter(Files::isReadable).forEach(vendorFile -> {
                        try {
                            String id = Files.readString(vendorFile).strip().toLowerCase(Locale.ROOT);
                            String label = switch (id) {
                                case "0x8086" -> "Intel";
                                case "0x1002" -> "AMD";
                                case "0x10de" -> "NVIDIA";
                                default -> "Desconocido (PCI " + id + ")";
                            };
                            if (!found.contains(label)) found.add(label);
                        } catch (IOException ignored) { }
                    });
        } catch (IOException ignored) {
            return List.of("No se pudo leer el inventario gráfico.");
        }
        return found.isEmpty() ? List.of("No se detectaron GPU PCI en /sys/class/drm.") : List.copyOf(found);
    }

    public List<String> packages(Vendor vendor) {
        return switch (vendor) {
            case INTEL -> List.of("mesa", "lib32-mesa", "vulkan-intel", "lib32-vulkan-intel",
                    "intel-media-driver", "libva-utils", "vulkan-icd-loader", "lib32-vulkan-icd-loader");
            case AMD -> List.of("mesa", "lib32-mesa", "vulkan-radeon", "lib32-vulkan-radeon",
                    "libva-mesa-driver", "mesa-vdpau", "vulkan-icd-loader", "lib32-vulkan-icd-loader");
            case NVIDIA -> List.of("nvidia-open", "nvidia-utils", "lib32-nvidia-utils",
                    "vulkan-icd-loader", "lib32-vulkan-icd-loader");
            case UNKNOWN -> List.of("mesa", "vulkan-icd-loader", "lib32-vulkan-icd-loader");
        };
    }

    /** DXVK/VKD3D son capas de traducción DirectX para Wine/Proton, no drivers DirectX nativos. */
    public List<String> compatibilityPackages() {
        return List.of("dxvk", "vkd3d", "wine-staging", "winetricks", "gamescope",
                "vulkan-icd-loader", "lib32-vulkan-icd-loader");
    }

    public List<String> command(List<String> packages) {
        if (packages == null || packages.isEmpty() || packages.stream().anyMatch(p -> !p.matches("[a-zA-Z0-9@._+-]+")))
            throw new IllegalArgumentException("Lista de paquetes inválida.");
        List<String> command = new ArrayList<>(List.of("sudo", "pacman", "-S", "--needed"));
        command.addAll(List.copyOf(packages));
        return List.copyOf(command);
    }

    public int install(List<String> packages) throws IOException, InterruptedException {
        if (!new OptimizationToolsInstaller().isArchBased())
            throw new IllegalStateException("La instalación de controladores solo está habilitada para Arch/CachyOS.");
        if (packages == null || packages.isEmpty()) throw new IllegalArgumentException("No hay paquetes seleccionados.");
        Process process = new ProcessBuilder(command(packages)).inheritIO().start();
        return process.waitFor();
    }
    /** Ejecuta el plan usando el gestor validado para la distribución detectada. */
    public int install(LinuxPlatformSupport support, LinuxPlatformSupport.Platform platform, List<String> packages) throws IOException, InterruptedException {
        return support.install(platform, packages);
    }
}