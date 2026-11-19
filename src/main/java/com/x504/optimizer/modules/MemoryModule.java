package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Diagnóstico de RAM, swap, zram y presión de memoria del kernel. */
public final class MemoryModule implements DiagnosticModule {
    public String id() { return "memory"; }
    public String name() { return "Memoria"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> meminfo = LinuxProbe.lines(Path.of("/proc/meminfo"));
        for (String key : List.of("MemTotal", "MemAvailable", "SwapTotal", "SwapFree", "Cached", "SReclaimable", "Dirty")) {
            String value = meminfo.stream().filter(line -> line.startsWith(key + ":")).findFirst().orElse("");
            if (!value.isBlank()) findings.add(value.strip());
        }
        Path zram = Path.of("/sys/block");
        try (var paths = Files.list(zram)) {
            List<String> devices = paths.map(p -> p.getFileName().toString()).filter(n -> n.startsWith("zram")).sorted().toList();
            findings.add("Dispositivos ZRAM: " + (devices.isEmpty() ? "No detectados" : String.join(", ", devices)));
            for (String device : devices) {
                Path base = zram.resolve(device);
                String algorithm = LinuxProbe.firstLine(base.resolve("comp_algorithm"));
                String disksize = LinuxProbe.firstLine(base.resolve("disksize"));
                findings.add(device + " — compresión: " + LinuxProbe.unknown(algorithm) + ", tamaño configurado: " + LinuxProbe.unknown(disksize) + " bytes");
            }
        } catch (Exception exception) { findings.add("Dispositivos ZRAM: No se pudo consultar"); }
        for (String resource : List.of("memory", "cpu", "io")) {
            Path pressure = Path.of("/proc/pressure", resource);
            String line = LinuxProbe.firstLine(pressure);
            if (!line.isBlank()) findings.add("PSI " + resource + ": " + line);
        }
        return new ModuleReport(id(), name(), findings, List.of("PSI indica presión acumulada, no porcentaje de RAM ocupada. No se purgaron cachés ni se cerraron procesos."));
    }
}