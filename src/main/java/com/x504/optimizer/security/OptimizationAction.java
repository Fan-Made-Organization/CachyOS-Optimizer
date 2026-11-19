package com.x504.optimizer.security;

import java.util.Objects;

/** Describe una acción candidata; crearla no ejecuta ningún cambio. */
public record OptimizationAction(String id, String description, RiskLevel risk,
                                 boolean reversible, boolean requiresAdministrator) {
    public OptimizationAction {
        id = requireText(id, "El identificador");
        description = requireText(description, "La descripción");
        risk = Objects.requireNonNull(risk, "El nivel de riesgo es obligatorio");
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " es obligatorio");
        String normalized = value.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException(label + " no puede estar vacío");
        return normalized;
    }
}
