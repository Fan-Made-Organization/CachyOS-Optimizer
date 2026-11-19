# Registro de cambios

## Desarrollo actual

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
