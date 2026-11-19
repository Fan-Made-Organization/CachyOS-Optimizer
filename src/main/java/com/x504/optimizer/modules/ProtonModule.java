package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inspecciona instalaciones de Proton en ubicaciones comunes de Steam. */
public final class ProtonModule implements DiagnosticModule {
    public String id() { return "proton"; }
    public String name() { return "Proton y compatibilidad"; }
    public ModuleReport inspect() {
        Path home = Path.of(System.getProperty("user.home"));
        List<Path> roots = List.of(home.resolve(".steam/steam/steamapps/common"),
                home.resolve(".local/share/Steam/steamapps/common"));
        List<String> found = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) continue;
            try (var entries = Files.list(root)) {
                entries.filter(p -> p.getFileName().toString().toLowerCase().contains("proton"))
                        .forEach(p -> found.add(p.getFileName().toString()));
            } catch (Exception ignored) { }
        }
        return new ModuleReport(id(), name(),
                found.isEmpty() ? List.of("No se encontraron carpetas Proton en rutas comunes.") :
                        found.stream().distinct().map(v -> "Versión/directorio: " + v).toList(),
                List.of("La disponibilidad de Proton no garantiza compatibilidad de cada juego."));
    }
}
