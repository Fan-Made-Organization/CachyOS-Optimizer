# Auditoría de la base funcional

Fecha de revisión: 2026-11-18.

Este documento registra qué está realmente implementado en el código compilado por Maven, qué es parcial y qué sigue pendiente. No se marca una capacidad como completa solo porque exista una clase con un nombre relacionado.

## Resultado de la revisión

| Área | Estado actual | Observación |
|---|---|---|
| CPU | Parcial | Lee modelo, núcleos lógicos, gobernador y límites de frecuencia expuestos por procfs/sysfs. No ofrece todavía topología detallada ni monitorización continua. |
| GPU y controlador | Parcial | Enumera dispositivos DRM y trata de resolver el controlador enlazado. No identifica de forma fiable todos los modelos/variantes ni el controlador en uso para cada API gráfica. |
| RAM y swap | Parcial | Lee MemTotal, MemAvailable, SwapTotal y SwapFree; detecta nombres de dispositivos zram. |
| Almacenamiento | Parcial | Enumera dispositivos de bloque, rotación y soporte anunciado de discard. No mide salud SMART ni rendimiento. |
| Vulkan/OpenGL/Mesa | Pendiente | El módulo actual solo indica que se requieren herramientas específicas; no recopila todavía versiones, dispositivos ni proveedores reales. |
| Aceleración de vídeo | Pendiente | No hay inspección VA-API/VDPAU ni prueba de perfiles de códec. |
| Steam/Proton/Gamescope/DXVK | Parcial | Detecta algunos binarios y carpetas comunes de Steam/Proton. No cubre todavía Flatpak, bibliotecas personalizadas, DXVK por prefijo ni todas las rutas alternativas. |
| Temperatura y energía | Parcial | Lee zonas térmicas y estado de fuentes de alimentación cuando sysfs los expone. No normaliza todos los sensores ni calcula consumo energético fiable. |
| Utilización en vivo | Pendiente | No existe aún un monitor de muestreo continuo de CPU/GPU/RAM/disco/red. |
| Comparación antes/después | Pendiente | No hay capturas normalizadas ni comparación de sesiones. |
| Recomendaciones | Inicial | Reglas sencillas basadas en texto de informes; no es todavía un motor contextual por modelo de hardware. |
| Aplicación de ajustes y reversión | Deshabilitada | El motor valida/prepara acciones, pero no ejecuta modificaciones del sistema. Se mantiene así hasta disponer de operaciones específicas y pruebas de reversión. |
| Informes exportables | Pendiente | Actualmente la salida principal es texto en terminal; todavía no se exporta un informe estructurado JSON/HTML. |

## Correcciones realizadas en esta auditoría

- El lanzador ahora vuelve a empaquetar el JAR cuando el código fuente o `pom.xml` es más reciente que el JAR existente. Evita ejecutar una compilación antigua después de actualizar con Git.
- Los scripts `launch`, `package`, `build` y `test` comparten el repositorio Maven local `.cache/maven-repository`, coherente con el bootstrap. Evita descargar dependencias en una caché distinta por accidente.
- Se comprobó que el CLI enruta el menú predeterminado, `--diagnostics`, `--benchmark`, `--backup`, `--restore` y `--help` a sus manejadores correspondientes.
- Se mantuvo el modo diagnóstico de solo lectura; no se activó ejecución de ajustes del sistema.

## Validación automatizada

La CI de GitHub ejecuta validación de sintaxis Bash, `git diff --check` y `mvn clean verify` con Java 21. Consulta el estado actual en la pestaña Actions del repositorio. Un resultado verde demuestra que compila y pasan las pruebas existentes; no demuestra por sí solo que cada sensor o herramienta opcional funcione en todas las distribuciones y equipos.

## Criterio para cerrar esta etapa

Antes de implementar optimizaciones de mayor riesgo, cada módulo básico debe tener:

1. Pruebas unitarias con salidas simuladas y casos de datos ausentes o malformados.
2. Pruebas de integración en CachyOS/Arch Linux, además de CI en un entorno limpio.
3. Distinción entre detectado, no disponible, no compatible y error de consulta.
4. Lecturas acotadas, tiempos límite para herramientas externas y ninguna modificación del sistema.
5. Documentación que describa qué se inspecciona realmente y sus limitaciones.

Las capacidades marcadas como pendientes no deben presentarse como funciones ya disponibles.

## Mantenimiento de esta auditoría

Esta auditoría describe el estado observado en una revisión concreta y debe actualizarse cuando cambie el comportamiento. Cada función marcada como disponible necesita una ruta ejecutable, pruebas y documentación de sus condiciones. La detección de una distribución no demuestra que todas sus acciones estén soportadas. Para validar cambios multiplataforma registra distribución, versión, arquitectura, kernel, gestor y herramientas disponibles; las pruebas simuladas no sustituyen pruebas de integración reales.
