package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lee sensores térmicos y fuentes de alimentación cuando el kernel los expone. */
public final class PowerModule implements DiagnosticModule {
    public String id() { return "power"; }
    public String name() { return "Energía y temperatura"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        Path thermal = Path.of("/sys/class/thermal");
        if (Files.isDirectory(thermal)) {
            try (var paths = Files.list(thermal)) {
                paths.filter(p -> p.getFileName().toString().startsWith("thermal_zone"))
                        .forEach(p -> {
                            String temperature = LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("temp")));
                            findings.add(p.getFileName() + ": " + temperature + " (mili °C)");
                        });
            } catch (Exception exception) { findings.add("No se pudieron consultar sensores térmicos."); }
        }
        Path power = Path.of("/sys/class/power_supply");
        if (Files.isDirectory(power)) {
            try (var paths = Files.list(power)) {
                paths.forEach(p -> findings.add(p.getFileName() + ": " +
                        LinuxProbe.unknown(LinuxProbe.firstLine(p.resolve("status")))));
            } catch (Exception exception) { findings.add("No se pudieron consultar fuentes de energía."); }
        }
        if (findings.isEmpty()) findings.add("No hay sensores expuestos por el kernel.");
        return new ModuleReport(id(), name(), findings, List.of("Las temperaturas se muestran tal como las expone el kernel."));
    }
}
