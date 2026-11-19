package com.x504.optimizer.security;

import java.util.Objects;

public record OptimizationAction(String id, String description, RiskLevel risk,
                                 boolean reversible, boolean requiresAdministrator) {
    public OptimizationAction {
        id = Objects.requireNonNull(id);
        description = Objects.requireNonNull(description);
        risk = Objects.requireNonNull(risk);
    }
}
