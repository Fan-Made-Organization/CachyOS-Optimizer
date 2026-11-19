# Módulos del sistema

Los módulos de diagnóstico implementan `DiagnosticModule` y producen un `ModuleReport` con hallazgos y advertencias. El coordinador `ModuleManager` captura fallos individuales para que un módulo no impida el resto del análisis.

## Módulos actuales

- CPU y memoria.
- GPU y almacenamiento.
- Energía, temperatura y red.
- Audio (PipeWire/PulseAudio).
- Gaming, KDE Plasma, Steam y Proton.
- Diagnóstico general.

## Flujo

1. Cada módulo consulta interfaces disponibles del sistema.
2. Los datos ausentes se informan como desconocidos o no disponibles.
3. El asesor usa evidencia de los informes para crear recomendaciones.
4. El CLI muestra el resultado y registra un resumen local.

Los módulos son de diagnóstico. No deben modificar controladores, servicios, gobernadores ni parámetros del kernel.
