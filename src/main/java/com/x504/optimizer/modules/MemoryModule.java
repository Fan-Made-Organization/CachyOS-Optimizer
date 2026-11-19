package com.x504.optimizer.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Diagnóstico de RAM, swap y zram. */
public final class MemoryModule implements DiagnosticModule {
    public String id() { return "memory"; }
    public String name() { return "Memoria"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        for (String key : List.of("MemTotal", "MemAvailable", "SwapTotal", "SwapFree")) {
            String value = LinuxProbe.lines(Path.of("/proc/meminfo")).stream()
                    .filter(line -> line.startsWith(key + ":")).findFirst().orElse("");
            findings.add(key + ": " + LinuxProbe.unknown(value));
        }
        Path zram = Path.of("/sys/block");
        try (var paths = Files.list(zram)) {
            List<String> devices = paths.map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("zram")).toList();
            findings.add("Dispositivos ZRAM: " + (devices.isEmpty() ? "No detectados" : String.join(", ", devices)));
        } catch (Exception exception) { findings.add("Dispositivos ZRAM: No se pudo consultar"); }
        return new ModuleReport(id(), name(), findings, List.of());
    }
}
