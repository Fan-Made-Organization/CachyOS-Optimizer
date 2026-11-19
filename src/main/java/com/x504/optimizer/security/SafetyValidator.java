package com.x504.optimizer.security;

/** Puerta de seguridad central: por defecto solo permite diagnósticos de lectura. */
public final class SafetyValidator {
    public ValidationResult validate(OptimizationAction action, boolean userApproved,
                                     boolean backupAvailable) {
        if (action.risk() == RiskLevel.CRITICAL) {
            return new ValidationResult(false, "Las operaciones críticas están bloqueadas.");
        }
        if (action.risk() == RiskLevel.READ_ONLY) {
            return new ValidationResult(true, "Operación de solo lectura permitida.");
        }
        if (!userApproved) return new ValidationResult(false, "Se requiere aprobación explícita.");
        if (action.requiresAdministrator() && !backupAvailable) {
            return new ValidationResult(false, "Se requiere un respaldo antes de solicitar privilegios.");
        }
        if (!action.reversible() && action.risk() == RiskLevel.HIGH) {
            return new ValidationResult(false, "No se permiten cambios de alto riesgo sin reversión.");
        }
        return new ValidationResult(true, "Validación superada; la ejecución aún requiere confirmación del usuario.");
    }

    public record ValidationResult(boolean allowed, String reason) {}
}
