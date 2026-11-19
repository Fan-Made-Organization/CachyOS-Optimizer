package com.x504.optimizer.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafetyValidatorTest {
    private final SafetyValidator validator = new SafetyValidator();

    @Test
    void allowsReadOnlyDiagnosticsWithoutApproval() {
        var action = new OptimizationAction("scan", "Diagnóstico", RiskLevel.READ_ONLY, true, false);
        assertTrue(validator.validate(action, false, false).allowed());
    }

    @Test
    void blocksChangesWithoutExplicitApproval() {
        var action = new OptimizationAction("tweak", "Ajuste", RiskLevel.LOW, true, false);
        assertFalse(validator.validate(action, false, true).allowed());
    }

    @Test
    void blocksCriticalOperations() {
        var action = new OptimizationAction("danger", "Operación crítica", RiskLevel.CRITICAL, false, true);
        assertFalse(validator.validate(action, true, true).allowed());
    }
}
