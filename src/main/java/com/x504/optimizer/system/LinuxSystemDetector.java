package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Detecta datos básicos sin ejecutar comandos ni cambiar la configuración. */
public final class LinuxSystemDetector {
    public SystemInfo detect() {
        boolean linux = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
        Map<String, String> release = readOsRelease();
        String distro = release.getOrDefault("PRETTY_NAME",
                release.getOrDefault("NAME", linux ? "Linux (no identificado)" : "No es Linux"));
        String version = release.getOrDefault("VERSION_ID", "Desconocida");
        String desktop = firstNonBlank(System.getenv("XDG_CURRENT_DESKTOP"),
                System.getenv("DESKTOP_SESSION"), "No detectado");
        return new SystemInfo(System.getProperty("os.name"), distro, version,
                System.getProperty("os.version"), System.getProperty("os.arch"),
                desktop, linux, !release.isEmpty());
    }

    private Map<String, String> readOsRelease() {
        Path path = Path.of("/etc/os-release");
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) return values;
        try {
            for (String line : Files.readAllLines(path)) {
                int separator = line.indexOf('=');
                if (separator <= 0 || line.startsWith("#")) continue;
                String key = line.substring(0, separator).strip();
                String value = line.substring(separator + 1).strip();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException ignored) {
            // La detección continúa con los datos disponibles.
        }
        return values;
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return fallback;
    }
}
