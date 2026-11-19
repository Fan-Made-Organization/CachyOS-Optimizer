package com.x504.optimizer.monitor;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Métricas portables de CPU y memoria mediante Java MXBeans.
 * Temperatura y uso GPU se consultan en Linux cuando sysfs los expone.
 */
public final class LiveMetrics {
    private CpuTicks previousCpu;
    private static final OperatingSystemMXBean OS =
            ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean bean ? bean : null;

    public Sample sample() {
        double cpu = readPortableCpu();
        double memory = readPortableMemory();
        double temperature = isLinux() ? readMaxTemperature() : -1;
        double gpu = isLinux() ? readGpuUsage() : -1;
        if (isLinux()) {
            CpuTicks now = readLinuxCpu();
            if (now != null) {
                if (previousCpu != null) cpu = now.percentSince(previousCpu);
                previousCpu = now;
            }
        }
        return new Sample(LocalTime.now().withNano(0), clamp(cpu), clamp(memory),
                temperature, gpu, Runtime.getRuntime().availableProcessors());
    }

    private static double readPortableCpu() {
        if (OS != null) {
            double load = OS.getCpuLoad();
            if (Double.isFinite(load) && load >= 0) return clamp(load * 100.0);
        }
        return 0;
    }

    private static double readPortableMemory() {
        if (OS != null && OS.getTotalMemorySize() > 0) {
            return clamp((OS.getTotalMemorySize() - OS.getFreeMemorySize()) * 100.0 / OS.getTotalMemorySize());
        }
        long max = Runtime.getRuntime().maxMemory();
        return max <= 0 ? 0 : clamp((max - Runtime.getRuntime().freeMemory()) * 100.0 / max);
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("linux");
    }

    private static CpuTicks readLinuxCpu() {
        try {
            String[] p = Files.readAllLines(Path.of("/proc/stat")).getFirst().trim().split("\\s+");
            long idle = Long.parseLong(p[4]) + (p.length > 5 ? Long.parseLong(p[5]) : 0);
            long total = 0;
            for (int i=1;i<p.length;i++) total += Long.parseLong(p[i]);
            return new CpuTicks(total, idle);
        } catch (Exception ex) { return null; }
    }

    private static double readMaxTemperature() {
        List<Path> inputs = new ArrayList<>();
        Path thermal = Path.of("/sys/class/thermal");
        try (var dirs = Files.list(thermal)) {
            inputs.addAll(dirs.filter(p -> p.getFileName().toString().matches("thermal_zone[0-9]+"))
                    .map(p -> p.resolve("temp")).filter(Files::isReadable).toList());
        } catch (IOException ignored) { }
        Path hwmon = Path.of("/sys/class/hwmon");
        try (var dirs = Files.list(hwmon)) {
            for (Path dir : dirs.toList()) {
                try (var files = Files.list(dir)) {
                    inputs.addAll(files.filter(p -> p.getFileName().toString().matches("temp[0-9]+_input"))
                            .filter(Files::isReadable).toList());
                } catch (IOException ignored) { }
            }
        } catch (IOException ignored) { }
        double max = -1;
        for (Path path : inputs) {
            try {
                double raw = Double.parseDouble(Files.readString(path).trim());
                double celsius = raw > 1000 ? raw / 1000.0 : raw;
                if (celsius >= -20 && celsius <= 150) max = Math.max(max, celsius);
            } catch (Exception ignored) { }
        }
        return max;
    }

    private static double readGpuUsage() {
        Path drm = Path.of("/sys/class/drm");
        try (var cards = Files.list(drm)) {
            for (Path card : cards.filter(p -> p.getFileName().toString().matches("card[0-9]+")).toList()) {
                Path device = card.resolve("device");
                Path amd = device.resolve("gpu_busy_percent");
                if (Files.isReadable(amd)) {
                    try { return clamp(Double.parseDouble(Files.readString(amd).trim())); }
                    catch (Exception ignored) { }
                }
                Path intel = device.resolve("gt/gt0/");
                for (String file : List.of("busy", "busy_percent")) {
                    Path metric = intel.resolve(file);
                    if (Files.isReadable(metric)) {
                        try {
                            String raw = Files.readString(metric).trim().replace("%", "");
                            return clamp(Double.parseDouble(raw));
                        } catch (Exception ignored) { }
                    }
                }
            }
        } catch (IOException ignored) { }
        return -1;
    }

    private static double clamp(double value) {
        if (!Double.isFinite(value)) return 0;
        return Math.max(0, Math.min(100, value));
    }
    private record CpuTicks(long total, long idle) {
        double percentSince(CpuTicks old) {
            long dt=total-old.total, di=idle-old.idle;
            return dt<=0 ? 0 : clamp((dt-di)*100.0/dt);
        }
    }
    public record Sample(LocalTime time, double cpuPercent, double memoryPercent,
                         double temperatureCelsius, double gpuPercent, int logicalCpus) {}
}
