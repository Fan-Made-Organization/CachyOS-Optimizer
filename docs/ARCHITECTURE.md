# Arquitectura del proyecto

## Objetivo
X504 CachyOS Optimizer es una aplicación Java 21 para diagnosticar sistemas Linux y, en fases posteriores, proponer optimizaciones controladas.

## Capas principales
- `system`: información del sistema y ejecución acotada de consultas.
- `hardware`: detección de CPU, memoria y adaptadores gráficos.
- `compatibility`: evaluación basada en comprobaciones observables.
- `security`: clasificación de riesgo y validación de operaciones.
- `cpu`, `gpu`, `memory`, `storage`, `gaming`, `network`, `audio`, `kde`: módulos funcionales previstos.

## Convenciones
El código Java vive en `src/main/java`. Los datos y propiedades viven en `src/main/resources`. Las pruebas se ubican en `src/test/java`.

## Flujo
1. Detectar el sistema y el hardware.
2. Comprobar compatibilidad y disponibilidad de herramientas.
3. Presentar un informe con datos desconocidos claramente identificados.
4. En futuras versiones, proponer acciones con riesgo y reversibilidad declarados.
5. Solicitar aprobación antes de cualquier cambio y registrar el resultado.

La aplicación actual implementa diagnóstico de solo lectura; los módulos de optimización todavía no deben considerarse funcionales hasta que tengan implementación y pruebas.
