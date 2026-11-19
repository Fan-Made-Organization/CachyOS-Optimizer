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

    public OptimizationCenter() {
        this(Path.of(System.getProperty("user.home"), ".local", "state", "cachyos-optimizer", "previous-power-profile"));
    }

    OptimizationCenter(Path stateFile) { this.stateFile = stateFile; }

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

    public boolean hasSolidStateDrive() throws IOException {
        Path block = Path.of("/sys/block");
        if (!Files.isDirectory(block)) return false;
        try (var devices = Files.list(block)) {
            return devices.anyMatch(device -> {
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

    public List<String> categoryStatus() {
        List<String> lines = new ArrayList<>();
        lines.add("CPU: perfiles de energía mediante power-profiles-daemon; cambio reversible.");
        lines.add("RAM: inspección de zram y memoria; no se fuerza la liberación de cachés ni se mata procesos.");
        lines.add("GPU: detección y herramientas; no se cambian controladores ni parámetros gráficos a ciegas.");
        lines.add("Gaming: GameMode, MangoHud, Gamescope y herramientas opcionales.");
        lines.add("Almacenamiento: TRIM programado con fstrim.timer si se detecta SSD/NVMe.");
        lines.add("Kernel: inspección y recomendaciones; no se sustituye el kernel automáticamente.");
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