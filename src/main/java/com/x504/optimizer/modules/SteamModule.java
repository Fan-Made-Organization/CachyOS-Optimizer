package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Diagnóstico básico de Steam y sus carpetas de usuario. */
public final class SteamModule implements DiagnosticModule {
    public String id() { return "steam"; }
    public String name() { return "Steam"; }
    public ModuleReport inspect() {
        Path root = Path.of(System.getProperty("user.home"), ".steam/steam");
        return new ModuleReport(id(), name(), List.of(
                "Directorio Steam: " + (Files.isDirectory(root) ? "detectado" : "no detectado"),
                "Carpeta compatdata: " + (Files.isDirectory(root.resolve("steamapps/compatdata")) ? "detectada" : "no detectada"),
                "Carpeta shadercache: " + (Files.isDirectory(root.resolve("steamapps/shadercache")) ? "detectada" : "no detectada")
        ), List.of("No se modifican bibliotecas ni prefijos de Proton."));
    }
}
