package com.x504.optimizer.modules;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModuleManagerTest {
    @Test
    void inspectsEveryRegisteredModule() {
        DiagnosticModule first = new StubModule("first");
        DiagnosticModule second = new StubModule("second");
        var reports = new ModuleManager(List.of(first, second)).inspectAll();
        assertEquals(2, reports.size());
        assertEquals("first", reports.get(0).moduleId());
        assertEquals("second", reports.get(1).moduleId());
    }

    @Test
    void convertsModuleFailureIntoReportAndContinues() {
        DiagnosticModule broken = new DiagnosticModule() {
            public String id() { return "broken"; }
            public String name() { return "Módulo defectuoso"; }
            public ModuleReport inspect() { throw new IllegalStateException("test"); }
        };
        var reports = new ModuleManager(List.of(broken, new StubModule("after"))).inspectAll();
        assertEquals(2, reports.size());
        assertEquals("No se pudo completar el diagnóstico.", reports.get(0).findings().getFirst());
        assertEquals("after", reports.get(1).moduleId());
    }

    private record StubModule(String id) implements DiagnosticModule {
        public String name() { return id; }
        public ModuleReport inspect() {
            return new ModuleReport(id, id, List.of("Correcto"), List.of());
        }
    }
}
