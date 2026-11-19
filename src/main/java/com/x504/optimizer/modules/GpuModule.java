package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Diagnóstico de adaptadores gráficos DRM y controladores asociados. */
public final class GpuModule implements DiagnosticModule {
    public String id() { return "gpu"; }
    public String name() { return "Gráficos"; }
    public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        Path drm = Path.of("/sys/class/drm");
        if (Files.isDirectory(drm)) {
            try (var paths = Files.list(drm)) {
                paths.filter(p -> p.getFileName().toString().matches("card[0-9]+"))
                        .forEach(p -> {
                            String driver = "Desconocido";
                            try { driver = p.resolve("device/driver").toRealPath().getFileName().toString(); }
                            catch (Exception ignored) { }
                            findings.add(p.getFileName() + " — controlador: " + driver);
                        });
            } catch (Exception exception) { findings.add("No se pudo consultar DRM."); }
        }
        if (findings.isEmpty()) findings.add("No se detectaron adaptadores DRM.");
        findings.add("OpenGL/Vulkan: la disponibilidad debe verificarse con herramientas específicas.");
        return new ModuleReport(id(), name(), findings, List.of("No se cambió ningún controlador."));
    }
}
