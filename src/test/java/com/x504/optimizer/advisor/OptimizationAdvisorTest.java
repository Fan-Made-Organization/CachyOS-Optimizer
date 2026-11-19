package com.x504.optimizer.advisor;

import com.x504.optimizer.modules.DiagnosticModule.ModuleReport;
import com.x504.optimizer.security.RiskLevel;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationAdvisorTest {
    private final OptimizationAdvisor advisor = new OptimizationAdvisor();

    @Test
    void recommendsOnlyToolsExplicitlyReportedAsMissing() {
        var reports = List.of(new ModuleReport("gaming", "Gaming",
                List.of("gamemoded: no encontrado", "mangohud: disponible"), List.of()));
        var result = advisor.analyze(reports);
        assertEquals(1, result.size());
        assertEquals("gaming-gamemoded", result.get(0).id());
        assertEquals(RiskLevel.READ_ONLY, result.get(0).risk());
        assertFalse(result.get(0).requiresApproval());
        assertFalse(result.get(0).requiresBackup());
    }

    @Test
    void doesNotInventRecommendationsWhenEvidenceIsAbsent() {
        var reports = List.of(new ModuleReport("gaming", "Gaming",
                List.of("steam: disponible", "vulkaninfo: disponible"), List.of()));
        assertTrue(advisor.analyze(reports).isEmpty());
    }

    @Test
    void reportsMissingDrmWithoutClaimingGpuIsAbsent() {
        var reports = List.of(new ModuleReport("gpu", "Gráficos",
                List.of("No se detectaron adaptadores DRM."), List.of()));
        var result = advisor.analyze(reports);
        assertEquals(1, result.size());
        assertTrue(result.get(0).explanation().contains("no demuestra que no exista una GPU"));
    }

    @Test
    void rejectsReadOnlyRecommendationThatRequestsApproval() {
        assertThrows(IllegalArgumentException.class, () -> new OptimizationRecommendation(
                "invalid", "Inválida", "Explicación", "Evidencia", "Beneficio",
                RiskLevel.READ_ONLY, true, true, false, "Paso siguiente"));
    }

    @Test
    void rejectsNullReports() {
        assertThrows(NullPointerException.class, () -> advisor.analyze(null));
    }
}
