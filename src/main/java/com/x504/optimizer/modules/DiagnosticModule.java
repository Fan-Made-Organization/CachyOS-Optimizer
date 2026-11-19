package com.x504.optimizer.modules;

import java.util.List;

/** Contrato común de los módulos de diagnóstico. */
public interface DiagnosticModule {
    String id();
    String name();
    ModuleReport inspect();
    record ModuleReport(String moduleId, String title, List<String> findings, List<String> warnings) {
        public ModuleReport {
            findings = List.copyOf(findings);
            warnings = List.copyOf(warnings);
        }
    }
}
