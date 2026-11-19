package com.x504.optimizer.compatibility;

import com.x504.optimizer.system.SystemInfo;
import java.util.ArrayList;
import java.util.List;

/** Calcula compatibilidad con evidencia detectada; no inventa una puntuación fija. */
public final class CompatibilityAnalyzer {
    public CompatibilityReport analyze(SystemInfo info) {
        List<CompatibilityItem> items = new ArrayList<>();
        items.add(new CompatibilityItem("Sistema operativo",
                info.linux() ? CompatibilityStatus.COMPATIBLE : CompatibilityStatus.INCOMPATIBLE,
                info.linux() ? "Se detectó Linux." : "El proyecto está diseñado para Linux."));
        items.add(new CompatibilityItem("Identificación de distribución",
                info.osReleaseAvailable() ? CompatibilityStatus.COMPATIBLE : CompatibilityStatus.UNKNOWN,
                info.osReleaseAvailable() ? info.distro() : "No se pudo leer /etc/os-release."));
        items.add(new CompatibilityItem("Arquitectura",
                isKnownArchitecture(info.architecture()) ? CompatibilityStatus.COMPATIBLE : CompatibilityStatus.UNKNOWN,
                info.architecture()));
        items.add(new CompatibilityItem("Entorno de escritorio",
                "Desconocido".equalsIgnoreCase(info.desktop()) || "No detectado".equalsIgnoreCase(info.desktop())
                        ? CompatibilityStatus.UNKNOWN : CompatibilityStatus.PARTIAL,
                info.desktop()));
        int points = 0;
        int known = 0;
        int unknown = 0;
        for (CompatibilityItem item : items) {
            if (item.status() == CompatibilityStatus.UNKNOWN) unknown++;
            else {
                known++;
                points += switch (item.status()) {
                    case COMPATIBLE -> 100;
                    case PARTIAL -> 60;
                    case INCOMPATIBLE -> 0;
                    case UNKNOWN -> 0;
                };
            }
        }
        int percentage = known == 0 ? 0 : (int) Math.round((double) points / known);
        return new CompatibilityReport(items, percentage, known, unknown);
    }

    private boolean isKnownArchitecture(String arch) {
        return List.of("amd64", "x86_64", "aarch64", "arm64").contains(arch.toLowerCase());
    }
}
