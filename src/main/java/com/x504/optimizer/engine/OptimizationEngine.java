package com.x504.optimizer.engine;

import com.x504.optimizer.security.OptimizationAction;
import com.x504.optimizer.security.RiskLevel;
import com.x504.optimizer.security.SafetyValidator;
import java.time.Instant;
import java.util.Objects;

/**
 * Orquesta la validación y preparación de propuestas.
 * La ejecución de cambios del sistema permanece deshabilitada deliberadamente.
 */
public final class OptimizationEngine {
    private final SafetyValidator validator;

    public OptimizationEngine() { this(new SafetyValidator()); }

    public OptimizationEngine(SafetyValidator validator) {
        this.validator = Objects.requireNonNull(validator, "El validador es obligatorio");
    }

    public OptimizationPlan prepare(OptimizationAction action, boolean userApproved,
                                    boolean backupAvailable) {
        Objects.requireNonNull(action, "La acción es obligatoria");
        SafetyValidator.ValidationResult validation =
                validator.validate(action, userApproved, backupAvailable);
        if (!validation.allowed()) {
            return new OptimizationPlan(action, OptimizationPlan.State.REJECTED,
                    validation.reason(), Instant.now());
        }
        if (action.risk() == RiskLevel.READ_ONLY) {
            return new OptimizationPlan(action, OptimizationPlan.State.VALIDATED_READ_ONLY,
                    "Consulta validada. No se realizó ningún cambio.", Instant.now());
        }
        return new OptimizationPlan(action, OptimizationPlan.State.VALIDATED_NOT_EXECUTED,
                validation.reason() + " El motor todavía no ejecuta modificaciones del sistema.",
                Instant.now());
    }
}
