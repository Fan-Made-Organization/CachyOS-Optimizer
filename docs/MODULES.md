# Módulos del sistema

Los módulos de diagnóstico implementan `DiagnosticModule` y producen un `ModuleReport` con hallazgos y advertencias. El coordinador `ModuleManager` captura fallos individuales para que un módulo no impida el resto del análisis.

## Módulos actuales

- CPU y memoria.
- GPU y almacenamiento.
- Energía, temperatura y red.
- Audio (PipeWire/PulseAudio).
- Gaming, KDE Plasma, Steam y Proton.
- Diagnóstico general.
- Kernel y arranque (versión activa, árboles de módulos, headers, módulos cargados, UEFI y rutas comunes del bootloader).
- Compatibilidad de distribución Linux (familia y gestor de paquetes).

## Flujo

1. Cada módulo consulta interfaces disponibles del sistema.
2. Los datos ausentes se informan como desconocidos o no disponibles.
3. El asesor usa evidencia de los informes para crear recomendaciones.
4. El CLI muestra el resultado y registra un resumen local.

Los módulos son de diagnóstico. No modifican controladores, servicios, gobernadores ni parámetros del kernel. La inspección de kernels es compatible con distintas familias Linux y permanece en modo solo lectura.
