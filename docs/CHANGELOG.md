# Registro de cambios

## Desarrollo actual

- Renombrado el producto y el artefacto Maven a Linux Optimizer; actualizado el lanzador, el CLI y las instrucciones de clonación. Se conservan rutas locales heredadas donde son necesarias para no romper datos y acciones reversibles de instalaciones anteriores.

- Ampliada la detección de distribuciones Linux a familias Arch, Debian, Fedora/RHEL y SUSE.
- Añadido instalador interactivo de herramientas de diagnóstico con comandos específicos para Pacman, APT, DNF/YUM y Zypper; sin confirmación automática.
- Añadido diagnóstico de kernel/arranque: variantes, árboles de módulos, headers, módulos cargados, UEFI, lockdown y detección orientativa de GRUB/systemd-boot.
- Añadido comando `--linux-support` y opción 10 del menú principal.
- Añadido gestor guiado de kernels por familia: CachyOS/Arch (estándar/LTS/Zen), Debian/Ubuntu, Fedora y openSUSE; incluye headers/devel y consulta de paquetes instalados.

- Incorporada licencia GNU GPL v3.0 (GPL-3.0-only).
- Añadido asesor de optimización basado en evidencia.
- Añadido panel ASCII de análisis del sistema con datos detectados y estado de compatibilidad.
- Añadido gestor de respaldos de archivos seleccionados con manifiesto y verificación SHA-256.
- Añadida restauración a una carpeta elegida, sin sobrescribir archivos existentes.
- Añadido registro local de auditoría.
- Añadido microbenchmark CPU opcional y acotado.
- Añadidas pruebas unitarias para panel, auditoría, respaldo y benchmark.
- Integradas las nuevas capacidades en el CLI.

## Criterios de registro

Las entradas deben describir cambios verificables, indicar limitaciones relevantes y separar funciones implementadas de trabajo planificado. No se debe anunciar una optimización como disponible cuando solo existe detección o recomendación. Los cambios de nombre del proyecto no migran por sí solos los datos de configuración locales ni amplían automáticamente la compatibilidad.


## Cambios recientes — diagnóstico y compatibilidad ampliados

- Se agregó un catálogo de componentes gráficos por familia Linux para Intel, AMD, NVIDIA y perfiles comunes, con instalación mediante el gestor detectado y confirmación explícita.
- Los perfiles NVIDIA propietarios se mantienen deshabilitados en Fedora, SUSE y Alpine cuando la instalación puede depender de repositorios externos o módulos específicos no validados. Los perfiles no disponibles no ejecutan comandos.
- Se añadió asesor energético que distingue laptop por presencia de batería y escritorio cuando sysfs está disponible, recomendando un perfil sin aplicarlo automáticamente.
- Se añadió `--advanced-diagnostics` y la opción 11 del menú para revisar dispositivos de bloque, unidades systemd fallidas, módulos del kernel y herramientas de gaming.
- Se ampliaron pruebas automatizadas para el catálogo gráfico multidistribución y las recomendaciones energéticas.
- Estas funciones son diagnósticos y planes guiados; no cambian sistemas de archivos, parámetros NVMe, servicios, kernels ni controladores sin una acción explícita y validada.


## Estabilización — 2026-11-19

- Se registró la ejecución CI Linux/Java 21 que completó correctamente validación Bash, `git diff --check` y `mvn clean verify` para el generador de parámetros de Steam y su límite opcional de FPS DXVK.
- Se añadió `docs/STABILIZATION.md` con el estado comprobado, los límites de la CI y la lista de validaciones manuales necesarias antes de publicar una versión estable.
- La auditoría base ahora distingue explícitamente compilación/pruebas automatizadas de verificación visual en KDE Plasma y pruebas sobre hardware/distribuciones reales.
- No se añadieron cambios automáticos ni agresivos al sistema durante esta fase de estabilización.
