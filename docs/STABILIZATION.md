# Cierre de estabilización — Linux Optimizer

Fecha: 2026-11-19

## Estado automatizado verificado

La revisión del commit `4aeb6dd4aee105335bbfed1ae7afd4bfcde814fe` terminó correctamente en GitHub Actions:

- Entorno: Ubuntu (Linux), Java 21.
- Validación sintáctica de todos los scripts Bash: correcta.
- `git diff --check`: correcto.
- `mvn clean verify`: correcto.
- Ejecución: [GitHub Actions #44545321329](https://github.com/Fan-Made-Organization/Linux-Optimizer/actions/runs/44545321329).

La compilación y la suite automatizada están verdes para ese commit. Esto no demuestra compatibilidad universal ni reemplaza pruebas en hardware real.

## Alcance que se considera estable

- Proyecto Java/Maven con Java 21.
- CI limitada a Linux.
- Diagnóstico principalmente de solo lectura.
- Acciones de optimización opcionales y explícitas; no se aplican al iniciar.
- Parámetros de Steam generados como texto para que el usuario los revise y copie.
- Detección de herramientas y dispositivos condicionada a lo que exponga cada distribución y kernel.

## Comprobaciones manuales todavía necesarias

Estas comprobaciones requieren el equipo y el escritorio real; no se marcan como aprobadas por CI:

1. Abrir la GUI en KDE Plasma y revisar navegación, tamaños y contenido en distintas resoluciones.
2. Abrir el generador Steam, comprobar visibilidad de casillas/campos y generar/copiar una línea.
3. Confirmar que los parámetros generados se limitan a las opciones seleccionadas.
4. Probar detección de hardware en Arch/CachyOS y registrar los componentes que no estén disponibles.
5. Verificar mensajes claros cuando falten herramientas opcionales o permisos.
6. Revisar que los cambios que requieren privilegios pidan confirmación y expliquen cómo revertirlos.

## Límites conocidos

- Los resultados de sensores, controladores, Vulkan/OpenGL y herramientas de gaming varían según hardware, kernel, paquetes y distribución.
- Una herramienta detectada no garantiza que esté configurada o funcionando para todos los juegos.
- No se prometen aumentos de FPS.
- La CI no ejecuta cambios privilegiados ni prueba la GUI visualmente.
- Las capacidades marcadas como parciales o pendientes en `BASELINE_AUDIT.md` siguen teniendo ese estado.

## Criterio para publicar

No publicar una versión estable solo porque compile. Primero completar las comprobaciones manuales anteriores, corregir cualquier regresión y ejecutar de nuevo `mvn clean verify` y la CI de Linux.
