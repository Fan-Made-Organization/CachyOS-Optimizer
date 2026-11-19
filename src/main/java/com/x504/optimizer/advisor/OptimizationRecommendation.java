package com.x504.optimizer.advisor;

import com.x504.optimizer.security.RiskLevel;
import java.util.Objects;

/** Recomendación informativa basada en evidencia del diagnóstico. */
public record OptimizationRecommendation(
        String id,
        String title,
        String explanation,
        String evidence,
        String expectedBenefit,
        RiskLevel risk,
        boolean reversible,
        boolean requiresApproval,
        boolean requiresBackup,
        String nextStep) {
    public OptimizationRecommendation {
        id = requireText(id, "id");
        title = requireText(title, "title");
        explanation = requireText(explanation, "explanation");
        evidence = requireText(evidence, "evidence");
        expectedBenefit = requireText(expectedBenefit, "expectedBenefit");
        risk = Objects.requireNonNull(risk, "risk");
        nextStep = requireText(nextStep, "nextStep");
        if (risk == RiskLevel.READ_ONLY && (requiresApproval || requiresBackup)) {
            throw new IllegalArgumentException("Una recomendación de solo lectura no requiere aprobación ni respaldo.");
        }
        if (requiresBackup && !requiresApproval) {
            throw new IllegalArgumentException("Un respaldo requerido implica aprobación explícita.");
        }
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " es obligatorio");
        String normalized = value.strip();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " no puede estar vacío");
        return normalized;
    }
}
