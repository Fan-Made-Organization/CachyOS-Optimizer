package com.x504.optimizer.advisor;

import com.x504.optimizer.modules.DiagnosticModule.ModuleReport;
import com.x504.optimizer.security.RiskLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Motor de recomendaciones de solo lectura.
 * Solo propone comprobaciones cuando existe evidencia explícita en los informes.
 */
public final class OptimizationAdvisor {
    public List<OptimizationRecommendation> analyze(List<ModuleReport> reports) {
        Objects.requireNonNull(reports, "Los informes son obligatorios");
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        for (ModuleReport report : List.copyOf(reports)) {
            if (report == null) continue;
            String module = safe(report.moduleId()).toLowerCase(Locale.ROOT);
            String evidenceText = String.join("\n", report.findings()).toLowerCase(Locale.ROOT);
            if (module.equals("gaming")) {
                suggestMissingTool(recommendations, evidenceText, "gamemoded",
                        "Revisar GameMode", "GameMode puede aplicar ajustes temporales durante juegos compatibles.",
                        "No se detectó el ejecutable gamemoded.", "Verificar el paquete disponible en los repositorios de tu distribución antes de instalarlo.");
                suggestMissingTool(recommendations, evidenceText, "mangohud",
                        "Revisar MangoHud", "MangoHud permite visualizar métricas durante una sesión de juego.",
                        "No se detectó el ejecutable mangohud.", "Comprobar si MangoHud está disponible y si realmente necesitas superposición de métricas.");
                suggestMissingTool(recommendations, evidenceText, "gamescope",
                        "Revisar Gamescope", "Gamescope puede proporcionar una sesión/compositor anidado para ciertos juegos.",
                        "No se detectó el ejecutable gamescope.", "Comprobar compatibilidad con el entorno gráfico y los juegos antes de instalarlo.");
                suggestMissingTool(recommendations, evidenceText, "vulkaninfo",
                        "Comprobar Vulkan", "La herramienta vulkaninfo permite inspeccionar la implementación Vulkan detectada.",
                        "No se detectó vulkaninfo.", "Instalar solo la herramienta de diagnóstico apropiada para la distribución y volver a ejecutar el análisis.");
            }
            if (module.equals("gpu") && evidenceText.contains("no se detectaron adaptadores drm")) {
                recommendations.add(readOnly("gpu-drm-check", "Investigar detección gráfica",
                        "El sistema no expuso adaptadores DRM en la ruta inspeccionada. Esto no demuestra que no exista una GPU.",
                        "El módulo gráfico informó que no detectó adaptadores DRM.",
                        "Identificar correctamente el controlador y la pila gráfica antes de plantear cambios.",
                        "Comprobar /sys/class/drm, permisos y registros del kernel; no reinstalar controladores a ciegas."));
            }
            if (module.equals("cpu") && evidenceText.contains("desconocido")) {
                recommendations.add(readOnly("cpu-frequency-check", "Verificar telemetría de CPU",
                        "Parte de la información de frecuencia o del gobernador no está disponible.",
                        "El informe de CPU contiene campos desconocidos.",
                        "Obtener una lectura fiable antes de evaluar perfiles de energía.",
                        "Comprobar el controlador de frecuencia del kernel y las interfaces cpufreq disponibles."));
            }
        }
        return List.copyOf(recommendations);
    }

    private static void suggestMissingTool(List<OptimizationRecommendation> out, String evidence,
                                           String binary, String title, String explanation,
                                           String expectedEvidence, String nextStep) {
        if (evidence.contains(binary + ": no encontrado")) {
            out.add(readOnly("gaming-" + binary, title, explanation, expectedEvidence,
                    "Mejorar la capacidad de diagnóstico o las opciones disponibles; no garantiza más FPS.",
                    nextStep));
        }
    }

    private static OptimizationRecommendation readOnly(String id, String title, String explanation,
                                                        String evidence, String benefit, String nextStep) {
        return new OptimizationRecommendation(id, title, explanation, evidence, benefit,
                RiskLevel.READ_ONLY, true, false, false, nextStep);
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
