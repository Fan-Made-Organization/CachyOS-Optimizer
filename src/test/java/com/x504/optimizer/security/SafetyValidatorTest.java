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
    void blocksReadOnlyActionsThatRequestAdministratorPrivileges() {
        var action = new OptimizationAction("scan", "Diagnóstico", RiskLevel.READ_ONLY, true, true);
        assertFalse(validator.validate(action, true, true).allowed());
    }

    @Test
    void blocksChangesWithoutExplicitApproval() {
        var action = new OptimizationAction("tweak", "Ajuste", RiskLevel.LOW, true, false);
        assertFalse(validator.validate(action, false, true).allowed());
    }

    @Test
    void blocksPrivilegedChangesWithoutBackup() {
        var action = new OptimizationAction("service", "Cambiar servicio", RiskLevel.MEDIUM, true, true);
        assertFalse(validator.validate(action, true, false).allowed());
    }

    @Test
    void allowsApprovedReversibleChangeWithRequiredBackup() {
        var action = new OptimizationAction("service", "Cambiar servicio", RiskLevel.MEDIUM, true, true);
        assertTrue(validator.validate(action, true, true).allowed());
    }

    @Test
    void blocksNonReversibleHighRiskChanges() {
        var action = new OptimizationAction("kernel", "Cambiar parámetro", RiskLevel.HIGH, false, true);
        assertFalse(validator.validate(action, true, true).allowed());
    }

    @Test
    void blocksCriticalOperations() {
        var action = new OptimizationAction("danger", "Operación crítica", RiskLevel.CRITICAL, false, true);
        assertFalse(validator.validate(action, true, true).allowed());
    }

    @Test
    void rejectsMissingAction() {
        assertFalse(validator.validate(null, true, true).allowed());
    }

    @Test
    void rejectsBlankIdentifiers() {
        assertThrows(IllegalArgumentException.class,
                () -> new OptimizationAction("  ", "Diagnóstico", RiskLevel.READ_ONLY, true, false));
    }
}
