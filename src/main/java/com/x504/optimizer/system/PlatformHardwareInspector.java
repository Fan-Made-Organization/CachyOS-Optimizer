package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Inventario de GPU y almacenamiento adaptado al sistema operativo.
 * Solo consulta información: nunca ejecuta pruebas ni modifica dispositivos.
 */
public final class PlatformHardwareInspector {
    public record Report(List<String> graphics, List<String> storage) {
        public Report {
            graphics = List.copyOf(graphics);
            storage = List.copyOf(storage);
        }
    }

    public Report inspect() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) return inspectLinux();
        if (os.contains("windows")) return inspectWindows();
        if (os.contains("mac")) return inspectMac();
        return new Report(List.of("Sistema operativo no reconocido; GPU no disponible."),
                List.of("Sistema operativo no reconocido; almacenamiento no disponible."));
    }

    private Report inspectLinux() {
        List<String> gpu = new ArrayList<>();
        Path drm = Path.of("/sys/class/drm");
        try (var cards = Files.list(drm)) {
            for (Path card : cards.filter(p -> p.getFileName().toString().matches("card[0-9]+")).sorted().toList()) {
                Path dev = card.resolve("device");
                String vendor = read(dev.resolve("vendor"));
                String device = read(dev.resolve("device"));
                String driver = "no expuesto";
                try {
                    Path link = dev.resolve("driver");
                    if (Files.isSymbolicLink(link)) driver = Files.readSymbolicLink(link).getFileName().toString();
                } catch (IOException ignored) { }
                gpu.add(card.getFileName() + ": vendor=" + fallback(vendor) + ", device=" + fallback(device) + ", driver=" + driver);
                addValue(gpu, "VRAM total", dev.resolve("mem_info_vram_total"), true);
            }
        } catch (IOException ex) {
            gpu.add("No se pudo consultar /sys/class/drm.");
        }
        if (gpu.isEmpty()) gpu.add("No se detectaron adaptadores DRM o el controlador no expone sus datos.");
        List<String> storage = run(List.of("lsblk", "-o", "NAME,TYPE,SIZE,ROTA,TRAN,FSTYPE,MOUNTPOINTS"), 5);
        if (storage.isEmpty()) storage = List.of("lsblk no disponible; no se pudo crear el inventario de discos.");
        return new Report(gpu, storage);
    }

    private Report inspectWindows() {
        List<String> gpu = new ArrayList<>(runPowerShell("$ErrorActionPreference='Stop'; Get-CimInstance Win32_VideoController | Select-Object Name,DriverVersion,AdapterRAM,VideoModeDescription | Format-List", 8));
        if (gpu.isEmpty()) gpu.add("No se pudo consultar Win32_VideoController.");
        List<String> storage = new ArrayList<>(runPowerShell("Get-PhysicalDisk | Select-Object FriendlyName,MediaType,BusType,Size,HealthStatus,OperationalStatus | Format-Table -AutoSize", 8));
        if (storage.isEmpty()) storage.add("Get-PhysicalDisk no disponible o no devolvió dispositivos.");
        gpu.add("Temperatura y utilización GPU dependen del fabricante; WMI no ofrece una métrica universal.");
        storage.add("Inventario de Windows de solo lectura; no se ejecutan pruebas SMART.");
        return new Report(gpu, storage);
    }

    private Report inspectMac() {
        List<String> gpu = new ArrayList<>(run(List.of("system_profiler", "SPDisplaysDataType"), 12));
        if (gpu.isEmpty()) gpu.add("system_profiler no pudo consultar los adaptadores gráficos.");
        List<String> storage = new ArrayList<>(run(List.of("diskutil", "list"), 8));
        if (storage.isEmpty()) storage.add("diskutil no pudo consultar los discos.");
        gpu.add("La utilización y temperatura GPU no se exponen mediante una interfaz universal de macOS.");
        storage.add("Inventario de solo lectura; no se ejecutan pruebas ni reparaciones.");
        return new Report(gpu, storage);
    }

    private static List<String> runPowerShell(String script, int seconds) {
        return run(List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script), seconds);
    }

    private static List<String> run(List<String> command, int seconds) {
        Path output = null;
        try {
            output = Files.createTempFile("linux-optimizer-hardware-", ".txt");
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return List.of();
            }
            String result = Files.readString(output, StandardCharsets.UTF_8).strip();
            return result.isBlank() ? List.of() : result.lines().toList();
        } catch (IOException ex) {
            return List.of();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            if (output != null) try { Files.deleteIfExists(output); } catch (IOException ignored) { }
        }
    }

    private static void addValue(List<String> out, String label, Path path, boolean bytes) {
        String raw = read(path);
        if (raw.isBlank()) return;
        try {
            long value = Long.parseLong(raw);
            out.add(label + ": " + (bytes ? String.format(Locale.ROOT, "%.0f MiB", value / 1048576.0) : value));
        } catch (NumberFormatException ex) {
            out.add(label + ": " + raw);
        }
    }

    private static String read(Path path) {
        try { return Files.isReadable(path) ? Files.readString(path).strip() : ""; }
        catch (IOException | SecurityException ex) { return ""; }
    }

    private static String fallback(String value) { return value.isBlank() ? "no expuesto" : value; }
}
