package com.x504.optimizer.install;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Instala herramientas conocidas de diagnóstico y gaming en Arch/CachyOS. */
public final class OptimizationToolsInstaller {
    private static final List<String> DIAGNOSTIC = List.of(
            "inxi", "lshw", "dmidecode", "smartmontools", "lm_sensors",
            "vulkan-tools", "mesa-utils", "libva-utils", "vdpauinfo", "nvtop",
            "power-profiles-daemon", "zram-generator", "cpupower", "thermald", "powertop");
    private static final List<String> GAMING = List.of(
            "gamemode", "mangohud", "gamescope", "goverlay", "vkbasalt", "lib32-gamemode", "lib32-mangohud");

    public List<String> packages(boolean includeGaming) {
        List<String> result = new ArrayList<>(DIAGNOSTIC);
        if (includeGaming) result.addAll(GAMING);
        return List.copyOf(result);
    }

    public List<String> command(boolean includeGaming) {
        List<String> command = new ArrayList<>(List.of("sudo", "pacman", "-S", "--needed"));
        command.addAll(packages(includeGaming));
        return List.copyOf(command);
    }

    public boolean isArchBased() {
        if (Files.exists(Path.of("/etc/arch-release")) || Files.exists(Path.of("/etc/cachyos-release"))) return true;
        try {
            String osRelease = Files.readString(Path.of("/etc/os-release")).toLowerCase(java.util.Locale.ROOT);
            return osRelease.contains("id_like=\"arch") || osRelease.contains("id_like=arch");
        } catch (IOException | RuntimeException ex) {
            return false;
        }
    }

    /** Ejecuta Pacman en primer plano para que el usuario vea y confirme la transacción. */
    public int install(boolean includeGaming) throws IOException, InterruptedException {
        if (!isArchBased()) throw new IllegalStateException("La instalación automática solo está habilitada para Arch Linux y CachyOS.");
        if (!onPath("sudo") || !onPath("pacman")) throw new IllegalStateException("Se necesitan sudo y pacman disponibles en PATH.");
        Process process = new ProcessBuilder(command(includeGaming)).inheritIO().start();
        return process.waitFor();
    }

    private static boolean onPath(String executable) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String directory : path.split(java.io.File.pathSeparator)) {
            try { if (Files.isExecutable(Path.of(directory, executable))) return true; }
            catch (RuntimeException ignored) { }
        }
        return false;
    }
}