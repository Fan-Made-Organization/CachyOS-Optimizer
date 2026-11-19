# Hoja de ruta de Linux Optimizer

Este documento separa las funciones disponibles de los objetivos de desarrollo. Una carpeta, clase o archivo de configuración no significa que una función esté terminada.

## Dirección del proyecto

Linux Optimizer será una herramienta **Linux-first, terminal-first**, con prioridad de compatibilidad para CachyOS y Arch Linux. No se planea una interfaz gráfica: la experiencia principal será un CLI claro, con menú interactivo opcional, salida legible y comandos automatizables.

La meta es que una persona pueda clonar el repositorio y ejecutar `bash scripts/launch.sh` sin instalar Java ni Maven globalmente.

## Principios que no se negocian

- Diagnóstico de solo lectura como comportamiento predeterminado.
- No inventar porcentajes de compatibilidad, temperaturas, rendimiento ni beneficios.
- No ejecutar comandos de shell construidos concatenando texto del usuario.
- No pedir ni usar privilegios de administrador para el análisis normal.
- No cambiar kernel, controladores, gobernadores, servicios ni parámetros sin una acción concreta, explicación y aprobación.
- Cada cambio futuro debe documentar compatibilidad, riesgo, reversión y pruebas.
- CachyOS/Arch son el objetivo principal; otras distribuciones deben identificarse y tratarse explícitamente.

## Estado y fases

### Fase 1 — Base confiable y experiencia de terminal
Estado: **en progreso**.

- [x] Proyecto Java 21 con Maven.
- [x] Preparación local de JDK/Maven con verificación de hashes.
- [x] Lanzador para clonar y ejecutar desde Linux.
- [x] Diagnóstico básico de sistema y hardware.
- [x] Módulos iniciales para CPU, memoria, GPU, almacenamiento, energía, red, audio, KDE, gaming, Steam y Proton.
- [x] Panel ASCII y recomendaciones de solo lectura.
- [x] Menú interactivo de terminal.
- [x] Pruebas automatizadas y CI.
- [ ] Validar el flujo de bootstrap en instalaciones limpias de CachyOS y Arch Linux x86_64/ARM64.
- [ ] Añadir pruebas de integración con salidas simuladas de herramientas Linux.

### Fase 2 — Diagnóstico profundo y detección de herramientas
Estado: **planificada**.

- Inventario de herramientas opcionales (por ejemplo, `lspci`, `inxi`, `vulkaninfo`, `glxinfo`, `sensors`, `upower`, `wpctl`, `pactl`, `lsblk`).
- Indicar claramente qué herramienta falta y qué diagnóstico queda limitado por ello.
- Detectar GPU y controlador activo sin asumir que un paquete instalado está en uso.
- Detectar Vulkan/OpenGL, aceleración de vídeo y estado de Steam/Proton cuando los datos estén disponibles.
- Distinguir datos detectados, inferidos, desconocidos y no aplicables.
- Exportar informes en JSON para comparaciones y automatización, sin exponer datos personales por defecto.

### Fase 3 — Monitorización y benchmarks
Estado: **parcial**.

- [x] Microbenchmark CPU acotado.
- [ ] Monitor de CPU, memoria, GPU, temperatura, energía y almacenamiento con intervalos configurables.
- [ ] Registrar muestras con límites de duración y tamaño.
- [ ] Comparación antes/después usando la misma carga y condiciones.
- [ ] Benchmarks específicos de GPU y juegos solo cuando existan herramientas fiables y pruebas reproducibles.
- [ ] Evitar presentar un microbenchmark sintético como predicción de FPS.

### Fase 4 — Asesor y perfiles inteligentes
Estado: **parcial**.

- [x] Asesor inicial basado en hallazgos de diagnóstico.
- [x] Catálogo informativo de perfiles.
- [x] Motor que prepara y valida planes sin ejecutarlos.
- [ ] Perfiles adaptados a escritorio, portátil, gaming y ahorro energético según hardware detectado.
- [ ] Justificar cada sugerencia con evidencia, beneficio esperado, incertidumbre, riesgo y reversibilidad.
- [ ] Permitir simular un plan y mostrar exactamente qué cambiaría antes de solicitar autorización.

### Fase 5 — Optimizaciones reales, reversibles y optativas
Estado: **no habilitada**.

No se habilitarán ajustes masivos ni "tweaks" universales. Cada acción se implementará individualmente, solo después de pruebas.

Requisitos para cada acción:
1. Compatibilidad validada para la distribución, versión, kernel y hardware aplicables.
2. Comprobación del estado actual y detección de conflictos.
3. Vista previa del cambio exacto.
4. Confirmación explícita del usuario.
5. Respaldo específico y método de reversión probado.
6. Registro de auditoría.
7. Pruebas que demuestren que el cambio se aplica y se revierte correctamente.

Las acciones que requieran privilegios deberán usar un mecanismo acotado y documentado. Nunca se ejecutará toda la aplicación como root ni se solicitará una contraseña para un conjunto opaco de comandos.

### Fase 6 — Seguridad, respaldos y mantenimiento
Estado: **parcial**.

- [x] Manifiestos y sumas SHA-256 para respaldos seleccionados.
- [x] Restauración verificada en un directorio elegido, sin sobrescribir.
- [x] Registro local de auditoría con rotación básica.
- [ ] Comprobación de espacio disponible, permisos y enlaces simbólicos antes de cada operación.
- [ ] Pruebas de recuperación para cada tipo de cambio reversible.
- [ ] Mantenimiento de cachés y temporales con vista previa y confirmación.
- [ ] Revisión de dependencias y cadena de suministro en CI.

## Experiencia de terminal prevista

- Menú interactivo sencillo para uso cotidiano.
- Comandos directos para scripts y usuarios avanzados.
- Salida compacta por defecto y opción detallada.
- Códigos de salida consistentes para automatización.
- Informes legibles y exportables.
- Sin interfaz gráfica: el CLI es la interfaz principal del proyecto.

## Definición de "listo para usuarios"

Una función solo se considera terminada cuando:
- Está implementada en el código compilado por Maven.
- Tiene pruebas automatizadas relevantes.
- Maneja herramientas ausentes, errores y permisos limitados.
- Documenta sus limitaciones y riesgos.
- Está integrada en el CLI y en el README.
- La CI pasa en una versión limpia del repositorio.

La prioridad es entregar funciones pequeñas, comprobables y útiles, no marcar todos los módulos como completos prematuramente.

## Criterios para declarar una función lista

Una función solo pasa a **Disponible** cuando tiene implementación alcanzable desde CLI o menú, validación de entradas, manejo de errores, pruebas automatizadas, documentación de requisitos y límites, y una ruta segura para cancelar o revertir cuando modifica el sistema. Para soporte multiplataforma se requiere evidencia por familia; detectar el nombre de una distribución no basta. Las prioridades deben favorecer precisión de diagnósticos, seguridad de acciones y cobertura de pruebas antes que ampliar listas de distribuciones o paquetes sin validación real.
