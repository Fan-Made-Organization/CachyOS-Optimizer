package com.x504.optimizer.core;

import com.x504.optimizer.modules.DiagnosticModule;
import com.x504.optimizer.modules.ModuleManager;
import java.time.Instant;
import java.util.List;

/** Núcleo de diagnóstico: crea una captura coherente y compartible entre componentes. */
public final class SystemCore {
    private final ModuleManager modules;
    public SystemCore(ModuleManager modules) { this.modules = modules; }
    public SystemSnapshot capture() {
        List<DiagnosticModule.ModuleReport> reports = modules.inspectAll();
        return new SystemSnapshot(Instant.now(), reports);
    }
    public record SystemSnapshot(Instant capturedAt, List<DiagnosticModule.ModuleReport> reports) {
        public SystemSnapshot { reports = List.copyOf(reports); }
    }
}