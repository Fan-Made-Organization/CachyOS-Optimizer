package com.x504.optimizer.profiles;

import java.util.List;

/** Catálogo inicial de perfiles sin ejecución automática de cambios. */
public final class ProfileCatalog {
    private final List<OptimizationProfile> profiles = List.of(
            new OptimizationProfile("balanced", "Equilibrado",
                    "Prioriza estabilidad y respuesta cotidiana.",
                    List.of("Conservar el gobernador administrado por el sistema.",
                            "Evitar cambios de kernel sin una necesidad comprobada.")),
            new OptimizationProfile("performance", "Rendimiento",
                    "Evalúa rendimiento sostenido y límites térmicos.",
                    List.of("Comprobar temperaturas antes de cargas prolongadas.",
                            "Comparar resultados antes y después de cada cambio.")),
            new OptimizationProfile("battery_saver", "Ahorro de energía",
                    "Reduce consumo sin desactivar funciones esenciales.",
                    List.of("Revisar el perfil de energía activo.",
                            "Identificar procesos de alto consumo antes de actuar.")),
            new OptimizationProfile("gaming", "Gaming",
                    "Prepara una sesión de juego con herramientas compatibles.",
                    List.of("Verificar Vulkan y controlador gráfico.",
                            "Comprobar GameMode, MangoHud y Gamescope si están instalados."))
    );
    public List<OptimizationProfile> all() { return profiles; }
    public OptimizationProfile find(String id) {
        return profiles.stream().filter(p -> p.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil desconocido: " + id));
    }
}
