package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Detecta instalaciones y herramientas gaming sin instalarlas ni iniciarlas. */
public final class GamingModule implements DiagnosticModule {
    public String id() { return "gaming"; }
    public String name() { return "Gaming"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        for (String binary : List.of("steam", "gamemoded", "mangohud", "gamescope", "vulkaninfo", "glxinfo")) {
            findings.add(binary + ": " + (findOnPath(binary) ? "disponible" : "no encontrado"));
        }
        Path compat = Path.of(System.getProperty("user.home"), ".steam/steam/compatibilitytools.d");
        findings.add("Herramientas Proton adicionales: " + (Files.isDirectory(compat) ? "directorio presente" : "no detectadas"));
        return new ModuleReport(id(), name(), findings, List.of("La detección no descarga ni ejecuta juegos."));
    }
    private boolean findOnPath(String binary) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String directory : path.split(java.io.File.pathSeparator)) {
            if (Files.isExecutable(Path.of(directory, binary))) return true;
        }
        return false;
    }
}
