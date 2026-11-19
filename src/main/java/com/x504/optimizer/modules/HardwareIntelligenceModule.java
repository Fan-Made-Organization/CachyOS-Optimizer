package com.x504.optimizer.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Amplía el inventario de hardware con topología y capacidades anunciadas por el kernel. */
public final class HardwareIntelligenceModule implements DiagnosticModule {
    @Override public String id() { return "hardware-intelligence"; }
    @Override public String name() { return "Hardware Intelligence"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> cpu = LinuxProbe.lines(Path.of("/proc/cpuinfo"));
        String model = value(cpu, "model name");
        if (model.isBlank()) model = value(cpu, "Hardware");
        findings.add("CPU: " + LinuxProbe.unknown(model));
        findings.add("Procesadores lógicos disponibles: " + Runtime.getRuntime().availableProcessors());
        findings.add("Núcleos físicos: " + LinuxProbe.unknown(LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/topology/core_id"))));
        String flags = value(cpu, "flags");
        if (flags.isBlank()) flags = value(cpu, "Features");
        if (!flags.isBlank()) {
            for (String feature : List.of("sse4_2", "avx", "avx2", "avx512f", "aes", "fma")) {
                findings.add("Instrucción " + feature.toUpperCase() + ": " + (List.of(flags.split("\\s+")).contains(feature) ? "anunciada" : "no anunciada"));
            }
        } else warnings.add("El kernel no expuso las capacidades de CPU en /proc/cpuinfo.");
        String mem = LinuxProbe.firstLine(Path.of("/proc/meminfo"));
        findings.add("Memoria: " + LinuxProbe.unknown(mem));
        Path pci = Path.of("/sys/bus/pci/devices");
        if (java.nio.file.Files.isDirectory(pci)) {
            try (var devices = java.nio.file.Files.list(pci)) {
                long count = devices.count();
                findings.add("Dispositivos PCI detectados: " + count);
            } catch (Exception ex) { warnings.add("No se pudo contar el inventario PCI."); }
        } else warnings.add("Inventario PCI no disponible.");
        warnings.add("Las capacidades anunciadas no garantizan que una aplicación pueda utilizarlas; depende del sistema operativo y de las bibliotecas.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
    private static String value(List<String> lines, String key) {
        return lines.stream().filter(line -> line.startsWith(key + "\t") || line.startsWith(key + " "))
                .map(line -> line.substring(line.indexOf(':') + 1).strip()).findFirst().orElse("");
    }
}