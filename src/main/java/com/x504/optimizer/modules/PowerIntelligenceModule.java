package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Detecta gestores de energía y políticas de frecuencia, sin cambiar perfiles. */
public final class PowerIntelligenceModule implements DiagnosticModule {
    @Override public String id() { return "power-intelligence"; }
    @Override public String name() { return "Power Intelligence"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        findings.add("Gobernador CPU0: " + LinuxProbe.unknown(LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"))));
        findings.add("Controlador de frecuencia: " + LinuxProbe.unknown(LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_driver"))));
        for (String service : List.of("power-profiles-daemon", "tlp", " tuned")) {
            String normalized = service.strip();
            Path unit = Path.of("/usr/lib/systemd/system", normalized + ".service");
            Path unitLocal = Path.of("/etc/systemd/system", normalized + ".service");
            findings.add(normalized + ": " + (Files.exists(unit) || Files.exists(unitLocal) ? "unidad instalada" : "no detectada"));
        }
        Path supply = Path.of("/sys/class/power_supply");
        if (Files.isDirectory(supply)) {
            try (var stream = Files.list(supply)) {
                for (Path p : stream.toList()) {
                    String type = LinuxProbe.firstLine(p.resolve("type"));
                    if ("Battery".equalsIgnoreCase(type)) {
                        findings.add("Batería " + p.getFileName() + ": " +
                                LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("capacity"))) + "%; estado " +
                                LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("status"))));
                    }
                }
            } catch (Exception ex) { warnings.add("No se pudo leer el estado de batería."); }
        }
        warnings.add("La presencia de una unidad no demuestra que el servicio esté activo. No se cambia el gobernador ni se inicia/detiene ningún servicio.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
}