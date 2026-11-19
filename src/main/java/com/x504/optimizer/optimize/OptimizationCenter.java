package com.x504.optimizer.optimize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Acciones reversibles y conservadoras para Arch/CachyOS. */
public final class OptimizationCenter {
    private static final List<String> POWER_PROFILES = List.of("balanced", "performance", "power-saver");
    private final Path stateFile;
    private final Path governorStateFile;

    public OptimizationCenter() {
        this(Path.of(System.getProperty("user.home"), ".local", "state", "cachyos-optimizer", "previous-power-profile"));
    }

    OptimizationCenter(Path stateFile) {
        this.stateFile = stateFile;
        this.governorStateFile = stateFile.resolveSibling("previous-cpu-governor");
    }

    public static List<String> powerProfiles() { return POWER_PROFILES; }
    public static boolean isValidPowerProfile(String profile) {
        return profile != null && POWER_PROFILES.contains(profile.strip().toLowerCase(Locale.ROOT));
    }

    public String currentPowerProfile() throws IOException, InterruptedException {
        CommandResult result = run(List.of("powerprofilesctl", "get"), 10);
        if (result.exitCode() != 0) throw new IOException("No se pudo consultar power-profiles-daemon: " + result.output());
        return result.output().strip();
    }

    public void applyPowerProfile(String profile) throws IOException, InterruptedException {
        if (!isValidPowerProfile(profile)) throw new IllegalArgumentException("Perfil de energía no permitido.");
        String current = currentPowerProfile();
        CommandResult available = run(List.of("powerprofilesctl", "list"), 10);
        if (available.exitCode() != 0 || !available.output().toLowerCase(Locale.ROOT).contains(profile))
            throw new IOException("El perfil '" + profile + "' no aparece entre los perfiles disponibles.");
        Path parent = stateFile.getParent();
        Files.createDirectories(parent);
        if (Files.isSymbolicLink(parent) || Files.isSymbolicLink(stateFile))
            throw new IOException("Se rechazó una ruta de estado que contiene un enlace simbólico.");
        if (!canRestorePowerProfile()) Files.writeString(stateFile, current + System.lineSeparator());
        CommandResult applied = run(List.of("powerprofilesctl", "set", profile), 20);
        if (applied.exitCode() != 0) {
            Files.deleteIfExists(stateFile);
            throw new IOException("No se pudo aplicar el perfil: " + applied.output());
        }
    }

    public boolean canRestorePowerProfile() {
        return Files.isRegularFile(stateFile, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(stateFile);
    }

    public void restorePowerProfile() throws IOException, InterruptedException {
        if (!canRestorePowerProfile()) throw new IOException("No hay un perfil anterior guardado para restaurar.");
        String previous = Files.readString(stateFile).strip();
        if (!isValidPowerProfile(previous)) throw new IOException("El perfil guardado no es válido; no se aplicó ningún cambio.");
        CommandResult restored = run(List.of("powerprofilesctl", "set", previous), 20);
        if (restored.exitCode() != 0) throw new IOException("No se pudo restaurar el perfil: " + restored.output());
        Files.delete(stateFile);
    }

    public String currentGovernor() throws IOException {
        Path file = Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
        if (!Files.isRegularFile(file)) throw new IOException("El kernel no expone un gobernador CPU modificable.");
        return Files.readString(file).strip();
    }

    public List<String> availableGovernors() throws IOException {
        Path file = Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
        if (!Files.isRegularFile(file)) throw new IOException("No hay lista de gobernadores disponible para este controlador CPU.");
        return List.of(Files.readString(file).strip().split("\\s+"));
    }

    public static boolean isValidGovernor(String governor) {
        return governor != null && List.of("performance", "powersave", "schedutil", "ondemand", "conservative").contains(governor);
    }

    public void applyGovernor(String governor) throws IOException, InterruptedException {
        if (!isValidGovernor(governor))
            throw new IllegalArgumentException("Gobernador no permitido.");
        if (!availableGovernors().contains(governor)) throw new IllegalArgumentException("Ese gobernador no está disponible en este kernel/controlador.");
        String current = currentGovernor();
        Path parent = governorStateFile.getParent();
        Files.createDirectories(parent);
        if (Files.isSymbolicLink(parent) || Files.isSymbolicLink(governorStateFile)) throw new IOException("Se rechazó una ruta de estado con enlace simbólico.");
        if (!canRestoreGovernor()) Files.writeString(governorStateFile, current + System.lineSeparator());
        int exit = runInherited(List.of("sudo", "cpupower", "frequency-set", "-g", governor));
        if (exit != 0) throw new IOException("cpupower no pudo aplicar el gobernador (código " + exit + ").");
    }

    public boolean canRestoreGovernor() {
        return Files.isRegularFile(governorStateFile, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(governorStateFile);
    }

    public void restoreGovernor() throws IOException, InterruptedException {
        if (!canRestoreGovernor()) throw new IOException("No hay un gobernador anterior guardado.");
        String previous = Files.readString(governorStateFile).strip();
        if (!List.of("performance", "powersave", "schedutil", "ondemand", "conservative").contains(previous) || !availableGovernors().contains(previous))
            throw new IOException("El gobernador guardado no está disponible; no se aplicó ningún cambio.");
        int exit = runInherited(List.of("sudo", "cpupower", "frequency-set", "-g", previous));
        if (exit != 0) throw new IOException("No se pudo restaurar el gobernador (código " + exit + ").");
        Files.delete(governorStateFile);
    }

    public boolean isManagedZram() {
        return Files.isRegularFile(Path.of("/var/lib/cachyos-optimizer/zram-managed"), LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isZramActive() throws IOException {
        Path zram = Path.of("/sys/block/zram0");
        if (!Files.exists(zram)) return false;
        Path swaps = Path.of("/proc/swaps");
        return Files.isRegularFile(swaps) && Files.readAllLines(swaps).stream().skip(1).anyMatch(line -> line.startsWith("/dev/zram"));
    }

    public int configureManagedZram(boolean enable) throws IOException, InterruptedException {
        String filename = enable ? "zram-enable.sh" : "zram-disable.sh";
        Path script = Path.of("scripts", filename).toAbsolutePath().normalize();
        if (!Files.isRegularFile(script)) {
            try {
                Path location = Path.of(OptimizationCenter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                Path root = Files.isDirectory(location) ? location.getParent().getParent() : location.getParent().getParent();
                script = root.resolve("scripts").resolve(filename).normalize();
            } catch (java.net.URISyntaxException ex) { throw new IOException("No se pudo localizar el proyecto.", ex); }
        }
        if (!Files.isRegularFile(script) || Files.isSymbolicLink(script))
            throw new IOException("No se encontró el script de configuración zram del proyecto.");
        return runInherited(List.of("sudo", "bash", script.toString()));
    }

    public List<String> installedKernels() throws IOException {
        Path modules = Path.of("/usr/lib/modules");
        if (!Files.isDirectory(modules)) return List.of();
        try (var dirs = Files.list(modules)) {
            return dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    public List<String> gpuSummary() throws IOException {
        Path drm = Path.of("/sys/class/drm");
        if (!Files.isDirectory(drm)) return List.of("No se encontró la interfaz DRM del kernel.");
        try (var cards = Files.list(drm)) {
            List<Path> devices = cards.filter(p -> p.getFileName().toString().matches("card[0-9]+") && Files.exists(p.resolve("device"))).toList();
            if (devices.isEmpty()) return List.of("No se detectaron tarjetas DRM.");
            List<String> result = new ArrayList<>();
            for (Path card : devices) {
                Path device = card.resolve("device");
                String driver = "desconocido";
                String vendor = "desconocido";
                try { driver = device.resolve("driver").toRealPath().getFileName().toString(); } catch (IOException ignored) { }
                try { vendor = Files.readString(device.resolve("vendor")).strip(); } catch (IOException ignored) { }
                String family = switch (vendor.toLowerCase(Locale.ROOT)) {
                    case "0x8086" -> "Intel";
                    case "0x1002" -> "AMD";
                    case "0x10de" -> "NVIDIA";
                    default -> "Fabricante no identificado";
                };
                result.add(card.getFileName() + ": " + family + " | controlador: " + driver + " | vendor PCI: " + vendor);
            }
            return List.copyOf(result);
        }
    }

    public boolean hasSolidStateDrive() throws IOException {
        Path block = Path.of("/sys/block");
        if (!Files.isDirectory(block)) return false;
        try (var devices = Files.list(block)) {
            return devices.anyMatch(device -> {
                String name = device.getFileName().toString();
                if (name.startsWith("loop") || name.startsWith("ram") || name.startsWith("zram")
                        || name.startsWith("dm-") || name.startsWith("md")) return false;
                Path rotational = device.resolve("queue/rotational");
                try { return Files.isRegularFile(rotational) && Files.readString(rotational).strip().equals("0"); }
                catch (IOException ex) { return false; }
            });
        }
    }

    public int enableTrimTimer() throws IOException, InterruptedException {
        if (!hasSolidStateDrive()) throw new IllegalStateException("No se detectó un dispositivo de almacenamiento no rotacional; no se habilitó TRIM.");
        return runInherited(List.of("sudo", "systemctl", "enable", "--now", "fstrim.timer"));
    }

    public int disableTrimTimer() throws IOException, InterruptedException {
        return runInherited(List.of("sudo", "systemctl", "disable", "--now", "fstrim.timer"));
    }

    public boolean isTrimTimerEnabled() throws IOException, InterruptedException {
        CommandResult result = run(List.of("systemctl", "is-enabled", "fstrim.timer"), 10);
        return result.exitCode() == 0 && result.output().strip().equals("enabled");
    }

    public List<String> categoryStatus() {
        List<String> lines = new ArrayList<>();
        lines.add("CPU: perfiles de energía y gobernador disponible; cambios reversibles con respaldo del valor anterior.");
        lines.add("RAM: detección y configuración zram administrada/reversible; no se purgan cachés ni se matan procesos.");
        lines.add("GPU: inventario DRM, fabricante PCI y controlador; sin cambios automáticos de drivers.");
        lines.add("Gaming: GameMode, MangoHud, Gamescope y herramientas opcionales.");
        lines.add("Almacenamiento: TRIM programado con fstrim.timer si se detecta SSD/NVMe.");
        lines.add("Kernel: inventario de versiones instaladas y kernel activo; sin eliminar ni sustituir kernels.");
        return List.copyOf(lines);
    }

    private static CommandResult run(List<String> command, int seconds) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean done = process.waitFor(seconds, TimeUnit.SECONDS);
        if (!done) { process.destroyForcibly(); process.waitFor(); throw new IOException("Tiempo de espera agotado al ejecutar " + command.getFirst()); }
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return new CommandResult(process.exitValue(), output);
    }

    private static int runInherited(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).inheritIO().start();
        return process.waitFor();
    }

    private record CommandResult(int exitCode, String output) {}
}