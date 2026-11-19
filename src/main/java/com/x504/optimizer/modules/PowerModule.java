package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lee sensores térmicos, refrigeración y fuentes de alimentación expuestos por el kernel. */
public final class PowerModule implements DiagnosticModule {
    public String id() { return "power"; }
    public String name() { return "Energía y temperatura"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        Path thermal = Path.of("/sys/class/thermal");
        if (Files.isDirectory(thermal)) {
            try (var paths = Files.list(thermal)) {
                paths.filter(p -> p.getFileName().toString().startsWith("thermal_zone")).sorted().forEach(p -> {
                    String type = LinuxProbe.firstLine(p.resolve("type"));
                    String raw = LinuxProbe.firstLine(p.resolve("temp"));
                    String temperature = "No disponible";
                    try { temperature = String.format("%.1f °C", Long.parseLong(raw) / 1000.0); } catch (NumberFormatException ignored) { }
                    findings.add(p.getFileName() + " — tipo: " + LinuxProbe.unknown(type) + ", temperatura: " + temperature);
                });
            } catch (Exception exception) { findings.add("No se pudieron consultar sensores térmicos."); }
        }
        Path cooling = Path.of("/sys/class/thermal");
        if (Files.isDirectory(cooling)) {
            try (var paths = Files.list(cooling)) {
                paths.filter(p -> p.getFileName().toString().startsWith("cooling_device")).sorted().forEach(p ->
                        findings.add(p.getFileName() + " — estado: " + LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("cur_state"))) +
                                ", máximo: " + LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("max_state")))));
            } catch (Exception exception) { findings.add("No se pudieron consultar dispositivos de refrigeración."); }
        }
        Path power = Path.of("/sys/class/power_supply");
        if (Files.isDirectory(power)) {
            try (var paths = Files.list(power)) {
                paths.sorted().forEach(p -> {
                    String type = LinuxProbe.firstLine(p.resolve("type"));
                    String status = LinuxProbe.firstLine(p.resolve("status"));
                    String capacity = LinuxProbe.firstLine(p.resolve("capacity"));
                    findings.add(p.getFileName() + " — tipo: " + LinuxProbe.unknown(type) + ", estado: " + LinuxProbe.unknown(status) +
                            (capacity.isBlank() ? "" : ", capacidad: " + capacity + "%"));
                });
            } catch (Exception exception) { findings.add("No se pudieron consultar fuentes de energía."); }
        }
        if (findings.isEmpty()) findings.add("No hay sensores expuestos por el kernel.");
        return new ModuleReport(id(), name(), findings, List.of("Los tipos de zona térmica los define el firmware/kernel; no se asume que toda zona sea la CPU. No se cambió ninguna política térmica."));
    }
}