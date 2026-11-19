# Arquitectura del proyecto

## Objetivo
X504 CachyOS Optimizer es una aplicación Java 21 para diagnosticar Linux, analizar compatibilidad y preparar recomendaciones con un enfoque de seguridad primero.

## Capas principales
- `system`: detección del sistema, panel ASCII y ejecución acotada de consultas.
- `hardware`: detección de CPU, memoria y adaptadores gráficos.
- `compatibility`: evaluación basada en comprobaciones observables; los datos desconocidos no se presentan como compatibles.
- `modules`: diagnósticos independientes que generan informes.
- `advisor`: recomendaciones informativas basadas en evidencia de los informes.
- `engine`: preparación y validación de acciones; no ejecuta modificaciones del sistema.
- `security`: clasificación de riesgo y validación central.
- `backup`: manifiestos, SHA-256, respaldo de archivos seleccionados y restauración verificada a una carpeta elegida.
- `audit`: registro local resumido con límite y rotación.
- `benchmark`: microbenchmark CPU opcional y acotado.
- `profiles`: catálogo informativo de perfiles.

## Flujo de diagnóstico
1. Detectar sistema y hardware.
2. Evaluar compatibilidad con evidencia disponible.
3. Inspeccionar módulos y recoger informes.
4. Generar recomendaciones únicamente cuando existe evidencia concreta.
5. Mostrar panel, resultados y siguientes pasos.
6. Registrar un resumen local sin almacenar rutas de archivos respaldados ni datos sensibles.

## Flujo de respaldo
1. El usuario selecciona explícitamente archivos y directorio de respaldo.
2. Se rechazan archivos fuente que sean enlaces simbólicos o no sean archivos regulares legibles.
3. Se copian los archivos y se genera un manifiesto con SHA-256.
4. La restauración verifica cada copia y la coloca en una carpeta de destino seleccionada.
5. No se sobrescriben archivos existentes ni se restaura directamente sobre las rutas originales.

## Seguridad
- El diagnóstico normal es de solo lectura.
- El motor valida propuestas, pero la ejecución de cambios de sistema permanece deshabilitada.
- Las operaciones críticas se bloquean.
- No se usa shell para construir comandos con entrada del usuario.
- No se ejecuta el proceso completo como administrador.

## Convenciones
El código Java vive en `src/main/java`; recursos y configuraciones viven en `src/main/resources`; pruebas en `src/test/java`. Código y nombres técnicos están en inglés; la interfaz y documentación para usuarios están en español.
