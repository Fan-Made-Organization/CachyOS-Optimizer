package com.x504.optimizer.modules;

import java.util.ArrayList;
import java.util.List;

/** Coordina módulos de diagnóstico sin ejecutar optimizaciones. */
public final class ModuleManager {
    private final List<DiagnosticModule> modules;
    public ModuleManager(List<DiagnosticModule> modules) { this.modules = List.copyOf(modules); }
    public List<DiagnosticModule.ModuleReport> inspectAll() {
        List<DiagnosticModule.ModuleReport> reports = new ArrayList<>();
        for (DiagnosticModule module : modules) {
            try { reports.add(module.inspect()); }
            catch (RuntimeException exception) {
                reports.add(new DiagnosticModule.ModuleReport(module.id(), module.name(),
                        List.of("No se pudo completar el diagnóstico."),
                        List.of(exception.getClass().getSimpleName())));
            }
        }
        return List.copyOf(reports);
    }
}
