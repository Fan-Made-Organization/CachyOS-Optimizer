package com.x504.optimizer.modules;

import com.x504.optimizer.system.CommandExecutor;
import com.x504.optimizer.system.CommandResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Analiza el estado consultable de Pacman sin ejecutar transacciones. */
public final class PackageIntelligenceModule implements DiagnosticModule {
    private final CommandExecutor commands;
    public PackageIntelligenceModule() { this(new CommandExecutor()); }
    PackageIntelligenceModule(CommandExecutor commands) { this.commands = commands; }

    @Override public String id() { return "package-intelligence"; }
    @Override public String name() { return "Package Intelligence"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean pacman = onPath("pacman");
        findings.add("Pacman: " + (pacman ? "disponible" : "no encontrado"));
        findings.add("yay: " + (onPath("yay") ? "disponible" : "no encontrado"));
        findings.add("paru: " + (onPath("paru") ? "disponible" : "no encontrado"));
        if (pacman) {
            findings.add("Base local de paquetes: " + (Files.isDirectory(Path.of("/var/lib/pacman/local")) ? "presente" : "no visible"));
            query(findings, warnings, "Paquetes huérfanos", List.of("pacman", "-Qdtq"));
            query(findings, warnings, "Actualizaciones disponibles", List.of("pacman", "-Qu"));
        } else warnings.add("No se detectó pacman; este equipo podría no usar una distribución basada en Arch.");
        warnings.add("Las consultas de paquetes son informativas. No se instala, actualiza, elimina ni modifica ningún paquete.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
    private void query(List<String> findings, List<String> warnings, String label, List<String> args) {
        try {
            CommandResult result = commands.executeReadOnly(args, Duration.ofSeconds(12));
            if (result.timedOut()) { findings.add(label + ": consulta agotó el límite de 12 segundos."); return; }
            if (result.exitCode() == 1 && result.standardOutput().isBlank() && result.standardError().isBlank()) {
                findings.add(label + ": ninguno detectado."); return;
            }
            String output = result.standardOutput().strip();
            if (output.isBlank() && result.exitCode() == 0) { findings.add(label + ": ninguno detectado."); return; }
            if (!result.successful()) {
                findings.add(label + ": no se pudo determinar (código " + result.exitCode() + ").");
                warnings.add("Pacman informó: " + compact(result.standardError()));
                return;
            }
            List<String> entries = output.lines().map(String::strip).filter(s -> !s.isEmpty()).toList();
            findings.add(label + ": " + entries.size() + " encontrados" +
                    (entries.isEmpty() ? "." : " — " + String.join(", ", entries.stream().limit(12).toList()) +
                    (entries.size() > 12 ? " (y " + (entries.size() - 12) + " más)" : "")));
        } catch (IOException ex) {
            warnings.add("No se pudo ejecutar la consulta de " + label + ".");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            warnings.add("Consulta de " + label + " interrumpida.");
        } catch (RuntimeException ex) {
            warnings.add("Consulta de " + label + " no disponible: " + ex.getClass().getSimpleName());
        }
    }
    private static String compact(String value) {
        return value.lines().map(String::strip).filter(s -> !s.isEmpty()).limit(3).reduce((a,b) -> a + " | " + b).orElse("sin detalle");
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