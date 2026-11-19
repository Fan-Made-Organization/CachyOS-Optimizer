package com.x504.optimizer.profiles;

import java.util.List;

/** Catálogo inicial de perfiles sin ejecución automática de cambios. */
public final class ProfileCatalog {
    private final List<OptimizationProfile> profiles = List.of(
            new OptimizationProfile("balanced", "Equilibrado", "Uso diario: estabilidad y respuesta constante.", List.of("Revisar perfil energético y gobernador.", "Comprobar presión de memoria y zram.", "Evitar cambios de kernel sin un problema reproducible.")),
            new OptimizationProfile("performance", "Rendimiento", "Sesiones exigentes: detectar límites antes de cambiar ajustes.", List.of("Comparar benchmarks antes/después.", "Vigilar temperatura, frecuencia y límites térmicos.", "Usar performance solo si está disponible y restaurar el perfil anterior.")),
            new OptimizationProfile("battery_saver", "Ahorro de energía", "Portátiles: prioriza autonomía sin desactivar funciones esenciales.", List.of("Usar power-saver si está disponible.", "Revisar batería y procesos de alto consumo.", "No aplicar límites térmicos manuales.")),
            new OptimizationProfile("gaming", "Gaming", "Prepara el stack gráfico y herramientas de juego sin prometer FPS.", List.of("Verificar controlador, OpenGL, Vulkan y VA-API.", "Comprobar GameMode, MangoHud, Gamescope y VKBasalt.", "WineD3D es fallback de compatibilidad, no acelerador universal.")),
            new OptimizationProfile("diagnostics", "Diagnóstico", "Investiga fallos sin alterar la configuración.", List.of("Exportar informes JSON/HTML.", "Revisar controladores y comparar benchmarks.", "Un dispositivo sin driver visible no confirma un conflicto.")),
            new OptimizationProfile("maintenance", "Mantenimiento", "Revisa almacenamiento, paquetes y cachés regenerables.", List.of("Inspeccionar SMART y espacio disponible.", "Revisar TRIM programado y actualizaciones.", "Limpiar solo miniaturas y caché de fuentes antiguas."))
    );
    public List<OptimizationProfile> all() { return profiles; }
    public OptimizationProfile find(String id) {
        return profiles.stream().filter(p -> p.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perfil desconocido: " + id));
    }
}
