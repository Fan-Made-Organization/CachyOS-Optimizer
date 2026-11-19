package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inventario de dispositivos de bloque y soporte TRIM anunciado. */
public final class StorageModule implements DiagnosticModule {
    public String id() { return "storage"; }
    public String name() { return "Almacenamiento"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        Path block = Path.of("/sys/block");
        if (Files.isDirectory(block)) {
            try (var paths = Files.list(block)) {
                paths.forEach(p -> {
                    String device = p.getFileName().toString();
                    String rotational = LinuxProbe.firstLine(p.resolve("queue/rotational"));
                    String discard = LinuxProbe.firstLine(p.resolve("queue/discard_max_bytes"));
                    findings.add(device + " — " + ("0".equals(rotational) ? "no rotacional" :
                            "1".equals(rotational) ? "rotacional" : "tipo desconocido") +
                            ", TRIM: " + ("0".equals(discard) || discard.isBlank() ? "no anunciado" : "anunciado"));
                });
            } catch (Exception exception) { findings.add("No se pudo leer /sys/block."); }
        }
        return new ModuleReport(id(), name(), findings, List.of("El diagnóstico no ejecuta TRIM ni modifica discos."));
    }
}
