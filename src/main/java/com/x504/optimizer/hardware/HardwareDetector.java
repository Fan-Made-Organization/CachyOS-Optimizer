package com.x504.optimizer.hardware;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Lee información del hardware desde /proc y /sys; no requiere privilegios. */
public final class HardwareDetector {
    private static final Pattern DRM_CARD = Pattern.compile("card\\d+");

    public HardwareSnapshot detect() {
        return new HardwareSnapshot(readCpuModel(), Runtime.getRuntime().availableProcessors(),
                readTotalMemory(), readGraphicsAdapters());
    }

    private String readCpuModel() {
        Path cpuInfo = Path.of("/proc/cpuinfo");
        if (!Files.isReadable(cpuInfo)) return "Desconocido";
        try {
            for (String line : Files.readAllLines(cpuInfo)) {
                if (line.startsWith("model name") || line.startsWith("Hardware")) {
                    int separator = line.indexOf(':');
                    if (separator >= 0) return line.substring(separator + 1).strip();
                }
            }
        } catch (IOException ignored) {
            // Se informa el dato como desconocido si el sistema no permite leerlo.
        }
        return "Desconocido";
    }

    private long readTotalMemory() {
        Path memInfo = Path.of("/proc/meminfo");
        if (!Files.isReadable(memInfo)) return 0;
        try {
            for (String line : Files.readAllLines(memInfo)) {
                if (line.startsWith("MemTotal:")) {
                    String[] fields = line.trim().split("\\s+");
                    return Long.parseLong(fields[1]);
                }
            }
        } catch (IOException | NumberFormatException ignored) {
            // El valor cero indica que no se pudo determinar la memoria.
        }
        return 0;
    }

    private List<String> readGraphicsAdapters() {
        Path drm = Path.of("/sys/class/drm");
        List<String> adapters = new ArrayList<>();
        if (!Files.isDirectory(drm)) return adapters;
        try (var entries = Files.list(drm)) {
            entries.filter(path -> DRM_CARD.matcher(path.getFileName().toString()).matches())
                    .forEach(path -> adapters.add(readDeviceName(path)));
        } catch (IOException ignored) {
            // La lista puede quedar vacía en sistemas sin DRM accesible.
        }
        return adapters.stream().distinct().toList();
    }

    private String readDeviceName(Path card) {
        Path vendor = card.resolve("device/vendor");
        Path device = card.resolve("device/device");
        try {
            if (Files.isReadable(vendor) && Files.isReadable(device)) {
                return "PCI " + Files.readString(vendor).strip() + ":" + Files.readString(device).strip();
            }
        } catch (IOException ignored) {
            // Se usa el nombre genérico del adaptador.
        }
        return card.getFileName().toString();
    }
}
