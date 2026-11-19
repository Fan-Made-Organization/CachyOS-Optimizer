package com.x504.optimizer.engine;

import com.x504.optimizer.security.OptimizationAction;
import java.time.Instant;
import java.util.Objects;

/** Resultado de preparar una acción; no contiene ni ejecuta comandos del sistema. */
public record OptimizationPlan(OptimizationAction action, State state, String explanation, Instant createdAt) {
    public OptimizationPlan {
        Objects.requireNonNull(action, "La acción es obligatoria");
        Objects.requireNonNull(state, "El estado es obligatorio");
        Objects.requireNonNull(createdAt, "La fecha es obligatoria");
        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("La explicación es obligatoria");
        }
        explanation = explanation.strip();
    }

    public enum State {
        REJECTED,
        VALIDATED_READ_ONLY,
        VALIDATED_NOT_EXECUTED
    }
}
