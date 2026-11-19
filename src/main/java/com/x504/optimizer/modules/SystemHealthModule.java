package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Resumen conservador de salud: memoria, carga y espacio del sistema de archivos raíz. */
public final class SystemHealthModule implements DiagnosticModule {
    @Override public String id() { return "system-health"; }
    @Override public String name() { return "System Health Monitor"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (String line : LinuxProbe.lines(Path.of("/proc/meminfo"))) {
            if (line.startsWith("MemTotal:") || line.startsWith("MemAvailable:") || line.startsWith("SwapTotal:") || line.startsWith("SwapFree:")) findings.add(line.strip());
        }
        findings.add("Carga promedio (1, 5, 15 min): " + LinuxProbe.unknown(LinuxProbe.firstLine(Path.of("/proc/loadavg"))));
        try {
            var store = Files.getFileStore(Path.of("/"));
            long total = store.getTotalSpace(), usable = store.getUsableSpace();
            findings.add(String.format("Raíz: %.1f GiB libres de %.1f GiB (%.1f%% disponible)",
                    usable / 1073741824.0, total / 1073741824.0, total == 0 ? 0 : usable * 100.0 / total));
            if (total > 0 && usable * 100.0 / total < 10) warnings.add("Menos del 10% del espacio de la raíz está disponible; revisa antes de instalar actualizaciones.");
        } catch (Exception ex) { warnings.add("No se pudo consultar el espacio de la raíz."); }
        Path thermal = Path.of("/sys/class/thermal");
        if (Files.isDirectory(thermal)) {
            try (var stream = Files.list(thermal)) {
                long sensors = stream.filter(p -> p.getFileName().toString().startsWith("thermal_zone") && Files.isReadable(p.resolve("temp"))).count();
                findings.add("Zonas térmicas legibles: " + sensors);
            } catch (Exception ex) { warnings.add("No se pudo contar las zonas térmicas."); }
        }
        warnings.add("La carga promedio no equivale directamente al porcentaje de uso de CPU. Las zonas térmicas no se etiquetan como temperatura de CPU sin identificar su tipo.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
}