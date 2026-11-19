package com.x504.optimizer.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Diagnóstico de CPU, frecuencia y gobernador sin modificar valores. */
public final class CpuModule implements DiagnosticModule {
    public String id() { return "cpu"; }
    public String name() { return "Procesador"; }
    public ModuleReport inspect() {
        String model = "Desconocido";
        for (String line : LinuxProbe.lines(Path.of("/proc/cpuinfo"))) {
            if (line.startsWith("model name") || line.startsWith("Hardware")) {
                int split = line.indexOf(':');
                if (split > 0) { model = line.substring(split + 1).strip(); break; }
            }
        }
        List<String> findings = new ArrayList<>();
        findings.add("Modelo: " + model);
        findings.add("Núcleos lógicos: " + Runtime.getRuntime().availableProcessors());
        String governor = LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"));
        String min = LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"));
        String max = LinuxProbe.firstLine(Path.of("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"));
        findings.add("Gobernador: " + LinuxProbe.unknown(governor));
        findings.add("Frecuencia mínima/máxima: " + LinuxProbe.unknown(min) + " / " + LinuxProbe.unknown(max) + " kHz");
        return new ModuleReport(id(), name(), findings, List.of("Los datos de frecuencia dependen del controlador del kernel."));
    }
}
