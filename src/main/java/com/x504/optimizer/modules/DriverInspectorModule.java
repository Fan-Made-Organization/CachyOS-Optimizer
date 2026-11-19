package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inspecciona asociaciones de controladores PCI mediante sysfs, sin cargar ni descargar módulos. */
public final class DriverInspectorModule implements DiagnosticModule {
    @Override public String id() { return "driver-inspector"; }
    @Override public String name() { return "Driver Inspector"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Path devices = Path.of("/sys/bus/pci/devices");
        int inspected = 0, bound = 0, unbound = 0;
        if (Files.isDirectory(devices)) {
            try (var stream = Files.list(devices)) {
                for (Path device : stream.sorted().toList()) {
                    if (inspected >= 80) { warnings.add("Inventario limitado a 80 dispositivos PCI para mantener la consulta acotada."); break; }
                    inspected++;
                    String id = device.getFileName().toString();
                    String vendor = LinuxProbe.firstLine(device.resolve("vendor"));
                    String product = LinuxProbe.firstLine(device.resolve("device"));
                    Path driver = device.resolve("driver");
                    String name = "sin controlador asociado";
                    if (Files.isSymbolicLink(driver)) {
                        try { name = driver.toRealPath().getFileName().toString(); bound++; }
                        catch (Exception ignored) { name = "asociación no resoluble"; }
                    } else unbound++;
                    findings.add(id + " [" + vendor + ":" + product + "] → " + name);
                }
            } catch (Exception ex) { warnings.add("No se pudo completar la lectura del inventario PCI."); }
        } else warnings.add("sysfs PCI no está disponible en este entorno.");
        findings.add(0, "Resumen PCI: " + inspected + " inspeccionados, " + bound + " con controlador asociado, " + unbound + " sin asociación visible.");
        findings.add("Módulos del kernel cargados: " + LinuxProbe.lines(Path.of("/proc/modules")).size());
        warnings.add("Un dispositivo sin enlace driver en sysfs no siempre representa un problema: algunos dispositivos no necesitan controlador activo.");
        warnings.add("No se instala, elimina, carga ni modifica ningún controlador.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
}