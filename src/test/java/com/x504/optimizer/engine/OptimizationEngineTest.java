package com.x504.optimizer.engine;

import com.x504.optimizer.security.OptimizationAction;
import com.x504.optimizer.security.RiskLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationEngineTest {
    private final OptimizationEngine engine = new OptimizationEngine();

    @Test
    void rejectsChangesWithoutExplicitApproval() {
        var action = new OptimizationAction("cpu-profile", "Cambiar perfil de CPU",
                RiskLevel.LOW, true, false);
        var plan = engine.prepare(action, false, false);
        assertEquals(OptimizationPlan.State.REJECTED, plan.state());
    }

    @Test
    void validatesButDoesNotExecuteApprovedChange() {
        var action = new OptimizationAction("test-change", "Cambio reversible de prueba",
                RiskLevel.LOW, true, false);
        var plan = engine.prepare(action, true, false);
        assertEquals(OptimizationPlan.State.VALIDATED_NOT_EXECUTED, plan.state());
        assertTrue(plan.explanation().contains("no ejecuta"));
    }

    @Test
    void allowsReadOnlyInspectionWithoutApproval() {
        var action = new OptimizationAction("inspect", "Consultar estado",
                RiskLevel.READ_ONLY, true, false);
        var plan = engine.prepare(action, false, false);
        assertEquals(OptimizationPlan.State.VALIDATED_READ_ONLY, plan.state());
    }

    @Test
    void blocksCriticalOperations() {
        var action = new OptimizationAction("critical", "Operación crítica",
                RiskLevel.CRITICAL, false, true);
        assertEquals(OptimizationPlan.State.REJECTED,
                engine.prepare(action, true, true).state());
    }
}
