package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Revisa disponibilidad de registros habituales sin alterar archivos. */
public final class DiagnosticsModule implements DiagnosticModule {
    public String id() { return "diagnostics"; }
    public String name() { return "Diagnóstico"; }
    public ModuleReport inspect() {
        List<String> paths = List.of("/etc/os-release", "/proc/cpuinfo", "/proc/meminfo",
                "/sys/class/drm", "/sys/block", "/sys/class/net");
        return new ModuleReport(id(), name(), paths.stream()
                .map(p -> p + ": " + (Files.exists(Path.of(p)) ? "disponible" : "no disponible")).toList(), List.of());
    }
}
