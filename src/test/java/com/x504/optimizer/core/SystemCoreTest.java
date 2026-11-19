package com.x504.optimizer.core;

import com.x504.optimizer.modules.DiagnosticModule;
import com.x504.optimizer.modules.ModuleManager;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SystemCoreTest {
    @Test void capturesImmutableDiagnosticSnapshot() {
        var module = new DiagnosticModule() {
            public String id(){return "test";}
            public String name(){return "Test";}
            public ModuleReport inspect(){return new ModuleReport(id(),name(),List.of("ok"),List.of());}
        };
        var snapshot = new SystemCore(new ModuleManager(List.of(module))).capture();
        assertNotNull(snapshot.capturedAt());
        assertEquals(1,snapshot.reports().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.reports().clear());
    }
}