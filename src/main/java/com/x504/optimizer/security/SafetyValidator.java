package com.x504.optimizer.security;

import java.util.Objects;

/**
 * Puerta de seguridad central. Validar una acción nunca la ejecuta.
 * Los cambios requieren aprobación explícita y, si necesitan privilegios,
 * un respaldo disponible.
 */
public final class SafetyValidator {
    public ValidationResult validate(OptimizationAction action, boolean userApproved,
                                     boolean backupAvailable) {
        if (action == null) {
            return new ValidationResult(false, "No se puede validar una acción inexistente.");
        }
        Objects.requireNonNull(action.risk(), "El nivel de riesgo es obligatorio");

        if (action.risk() == RiskLevel.CRITICAL) {
            return new ValidationResult(false, "Las operaciones críticas están bloqueadas.");
        }
        if (action.risk() == RiskLevel.READ_ONLY) {
            if (action.requiresAdministrator()) {
                return new ValidationResult(false,
                        "Un diagnóstico de solo lectura no debe solicitar privilegios de administrador.");
            }
            return new ValidationResult(true, "Operación de solo lectura permitida.");
        }
        if (!userApproved) {
            return new ValidationResult(false, "Se requiere aprobación explícita.");
        }
        if (action.requiresAdministrator() && !backupAvailable) {
            return new ValidationResult(false,
                    "Se requiere un respaldo antes de solicitar privilegios.");
        }
        if (action.risk() == RiskLevel.HIGH && !action.reversible()) {
            return new ValidationResult(false,
                    "No se permiten cambios de alto riesgo sin un mecanismo de reversión.");
        }
        return new ValidationResult(true,
                "Validación superada; la ejecución aún requiere una confirmación independiente.");
    }

    public record ValidationResult(boolean allowed, String reason) {
        public ValidationResult {
            Objects.requireNonNull(reason, "La razón de validación es obligatoria");
        }
    }
}
