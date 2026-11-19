package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Comprueba herramientas de gestión de paquetes y deja claro qué análisis requiere consulta adicional. */
public final class PackageIntelligenceModule implements DiagnosticModule {
    @Override public String id() { return "package-intelligence"; }
    @Override public String name() { return "Package Intelligence"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean pacman = onPath("pacman");
        boolean yay = onPath("yay");
        boolean paru = onPath("paru");
        findings.add("Pacman: " + (pacman ? "disponible" : "no encontrado"));
        findings.add("yay: " + (yay ? "disponible" : "no encontrado"));
        findings.add("paru: " + (paru ? "disponible" : "no encontrado"));
        if (pacman) {
            findings.add("Base local de paquetes: " + (Files.isDirectory(Path.of("/var/lib/pacman/local")) ? "presente" : "no visible"));
            warnings.add("La lista de paquetes huérfanos, actualizaciones y conflictos requiere una consulta específica a pacman; esta inspección no ejecuta transacciones.");
        } else warnings.add("No se detectó pacman; este equipo podría no usar una distribución basada en Arch.");
        warnings.add("No se instala, actualiza, elimina ni modifica ningún paquete.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
    private static boolean onPath(String binary) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String dir : path.split(java.io.File.pathSeparator)) {
            try { if (Files.isExecutable(Path.of(dir, binary))) return true; }
            catch (RuntimeException ignored) { }
        }
        return false;
    }
}