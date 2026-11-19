package com.x504.optimizer.modules;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Diagnóstico de CPU, frecuencia actual/máxima y gobernador sin modificar valores. */
public final class CpuModule implements DiagnosticModule {
    public String id() { return "cpu"; }
    public String name() { return "Procesador"; }
    public ModuleReport inspect() {
        String model = "Desconocido";
        for (String line : LinuxProbe.lines(Path.of("/proc/cpuinfo"))) {
            if (line.startsWith("model name") || line.startsWith("Hardware")) { int split = line.indexOf(':'); if (split > 0) { model = line.substring(split + 1).strip(); break; } }
        }
        List<String> findings = new ArrayList<>();
        findings.add("Modelo: " + model);
        findings.add("Núcleos lógicos: " + Runtime.getRuntime().availableProcessors());
        Path cpufreq = Path.of("/sys/devices/system/cpu/cpu0/cpufreq");
        String governor = LinuxProbe.firstLine(cpufreq.resolve("scaling_governor"));
        String min = LinuxProbe.firstLine(cpufreq.resolve("scaling_min_freq"));
        String max = LinuxProbe.firstLine(cpufreq.resolve("scaling_max_freq"));
        String current = LinuxProbe.firstLine(cpufreq.resolve("scaling_cur_freq"));
        findings.add("Gobernador: " + LinuxProbe.unknown(governor));
        findings.add("Frecuencia actual reportada: " + LinuxProbe.unknown(current) + " kHz");
        findings.add("Rango mínimo/máximo: " + LinuxProbe.unknown(min) + " / " + LinuxProbe.unknown(max) + " kHz");
        return new ModuleReport(id(), name(), findings, List.of("La frecuencia puede variar rápidamente y depende del controlador del kernel; una lectura aislada no mide rendimiento ni throttling."));
    }
}