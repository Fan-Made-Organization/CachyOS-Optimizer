package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Lista interfaces de red presentes en el sistema. */
public final class NetworkModule implements DiagnosticModule {
    public String id() { return "network"; }
    public String name() { return "Red"; }
    public ModuleReport inspect() {
        Path interfaces = Path.of("/sys/class/net");
        try (var paths = Files.list(interfaces)) {
            List<String> names = paths.map(p -> p.getFileName().toString()).sorted().toList();
            return new ModuleReport(id(), name(), names.stream().map(n -> "Interfaz: " + n).toList(), List.of());
        } catch (Exception exception) {
            return new ModuleReport(id(), name(), List.of("No se pudieron detectar interfaces."), List.of());
        }
    }
}
