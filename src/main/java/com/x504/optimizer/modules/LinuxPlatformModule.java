package com.x504.optimizer.modules;

import com.x504.optimizer.system.LinuxPlatformSupport;
import java.util.List;

/** Identifica la familia de distribución y el gestor de paquetes disponible. */
public final class LinuxPlatformModule implements DiagnosticModule {
    @Override public String id() { return "linux-platform"; }
    @Override public String name() { return "Compatibilidad de distribución Linux"; }
    @Override public ModuleReport inspect() {
        var platform = new LinuxPlatformSupport().detect();
        return new ModuleReport(id(), name(),
                List.of("Distribución: " + platform.name(), "ID: " + platform.id(),
                        "Versión: " + (platform.version().isBlank() ? "No disponible" : platform.version()),
                        "Familia: " + platform.family(), "Gestor detectado: " + platform.packageManager()),
                List.of("La detección de familia no garantiza que todos los paquetes tengan el mismo nombre o estén disponibles en repositorios habilitados.",
                        "La instalación automática de herramientas está limitada a familias con comandos y paquetes explícitamente aprobados."));
    }
}