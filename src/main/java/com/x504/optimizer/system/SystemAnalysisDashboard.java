package com.x504.optimizer.system;

import com.x504.optimizer.compatibility.CompatibilityReport;
import com.x504.optimizer.compatibility.CompatibilityStatus;
import com.x504.optimizer.hardware.HardwareSnapshot;
import java.util.Objects;

/** Presentación compacta de resultados detectados; nunca inventa datos ausentes. */
public final class SystemAnalysisDashboard {
    private static final int WIDTH = 44;

    public String render(SystemInfo system, HardwareSnapshot hardware, CompatibilityReport compatibility) {
        Objects.requireNonNull(system, "El sistema es obligatorio");
        Objects.requireNonNull(hardware, "El hardware es obligatorio");
        Objects.requireNonNull(compatibility, "La compatibilidad es obligatoria");

        String distro = system.distro();
        String base = inferBase(distro);
        String kernel = normalizeKernel(system.kernel(), system.linux());
        String desktop = normalizeDesktop(system.desktop());
        String architecture = normalizeArchitecture(system.architecture());
        String state = stateOf(compatibility);
        String score = compatibility.percentage() + "%";
        String checks = compatibility.evaluatedComponents() + " evaluados | "
                + compatibility.unknownComponents() + " desconocidos";
        String cpu = hardware.processor();
        String gpu = hardware.graphicsAdapters().isEmpty()
                ? "No detectada" : String.join(", ", hardware.graphicsAdapters());
        String ram = hardware.totalMemoryKiB() > 0
                ? String.format("%.2f GiB", hardware.totalMemoryKiB() / 1024.0 / 1024.0)
                : "No disponible";

        StringBuilder out = new StringBuilder();
        out.append(top()).append('\n');
        out.append(row("X504-CACHYOS-OPTIMIZER")).append('\n');
        out.append(row("SYSTEM ANALYSIS")).append('\n');
        out.append(mid()).append('\n');
        out.append(row("Sistema:       " + distro)).append('\n');
        out.append(row("Base:          " + base)).append('\n');
        out.append(row("Kernel:        " + kernel)).append('\n');
        out.append(row("Escritorio:    " + desktop)).append('\n');
        out.append(row("Arquitectura:  " + architecture)).append('\n');
        out.append(mid()).append('\n');
        out.append(row("Compatibilidad: " + score)).append('\n');
        out.append(row("Datos: " + checks)).append('\n');
        out.append(row("Estado:        " + state)).append('\n');
        out.append(mid()).append('\n');
        out.append(row("CPU: " + cpu)).append('\n');
        out.append(row("GPU: " + gpu)).append('\n');
        out.append(row("RAM: " + ram)).append('\n');
        out.append(bottom());
        return out.toString();
    }

    private String stateOf(CompatibilityReport report) {
        boolean incompatible = report.items().stream()
                .anyMatch(item -> item.status() == CompatibilityStatus.INCOMPATIBLE);
        if (incompatible) return "INCOMPATIBLE";
        if (report.unknownComponents() > 0) return "PARCIAL / DATOS INCOMPLETOS";
        if (report.percentage() >= 90) return "EXCELENTE";
        if (report.percentage() >= 70) return "BUENO";
        return "REVISAR COMPATIBILIDAD";
    }

    private String normalizeKernel(String kernel, boolean linux) {
        if (!linux || kernel == null || kernel.isBlank()
                || kernel.equalsIgnoreCase("desconocido")) return kernel;
        return kernel.toLowerCase(java.util.Locale.ROOT).startsWith("linux")
                ? kernel : "Linux " + kernel;
    }

    private String normalizeDesktop(String desktop) {
        if (desktop == null || desktop.isBlank()) return "No detectado";
        String normalized = desktop.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("kde") || normalized.contains("plasma")) return "KDE Plasma";
        if (normalized.contains("gnome")) return "GNOME";
        if (normalized.contains("xfce")) return "Xfce";
        if (normalized.contains("cinnamon")) return "Cinnamon";
        if (normalized.contains("mate")) return "MATE";
        if (normalized.contains("lxqt")) return "LXQt";
        return desktop;
    }

    private String normalizeArchitecture(String architecture) {
        if (architecture == null) return "Desconocida";
        return switch (architecture.toLowerCase(java.util.Locale.ROOT)) {
            case "amd64" -> "x86_64";
            case "arm64" -> "aarch64";
            default -> architecture;
        };
    }

    private String inferBase(String distro) {
        String normalized = distro.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("cachyos") || normalized.contains("arch")) return "Arch Linux";
        if (normalized.contains("debian") || normalized.contains("ubuntu") || normalized.contains("mint")) return "Debian";
        if (normalized.contains("fedora") || normalized.contains("nobara")) return "Fedora";
        if (normalized.contains("opensuse") || normalized.contains("suse")) return "openSUSE";
        return "No identificada";
    }

    private String top() { return "╔" + "═".repeat(WIDTH) + "╗"; }
    private String mid() { return "╠" + "═".repeat(WIDTH) + "╣"; }
    private String bottom() { return "╚" + "═".repeat(WIDTH) + "╝"; }

    private String row(String value) {
        String safe = value == null ? "Desconocido" : value.replace('\n', ' ').replace('\r', ' ');
        int count = safe.codePointCount(0, safe.length());
        if (count > WIDTH) safe = safe.substring(0, safe.offsetByCodePoints(0, WIDTH - 1)) + "…";
        int visible = safe.codePointCount(0, safe.length());
        return "║" + safe + " ".repeat(WIDTH - visible) + "║";
    }
}
