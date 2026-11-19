# Compatibilidad

El porcentaje de compatibilidad se calcula a partir de comprobaciones reales disponibles. No representa una garantía de rendimiento ni de estabilidad.

## Plataformas Linux detectadas

El detector lee `/etc/os-release` y usa `ID`/ `ID_LIKE` para clasificar familias:
- Arch: CachyOS, Arch, Manjaro, EndeavourOS y derivadas que declaren esa familia.
- Debian: Debian, Ubuntu, Linux Mint, Pop!_OS y derivadas que declaren esa familia.
- Fedora/RHEL: Fedora, RHEL, CentOS Stream, Rocky y AlmaLinux cuando declaran esa familia.
- SUSE: openSUSE Leap/Tumbleweed y derivadas que declaren esa familia.

El gestor de paquetes se detecta por familia (Pacman, APT, DNF/YUM o Zypper). La detección de familia no garantiza que los repositorios estén habilitados ni que cada paquete tenga el mismo nombre. Las distribuciones no reconocidas siguen pudiendo ejecutar diagnósticos de solo lectura; la instalación automática queda desactivada.

## Datos considerados actualmente
- Sistema operativo Linux y distribución.
- Disponibilidad de `/etc/os-release`.
- Arquitectura reconocida.
- Entorno de escritorio detectado.
- Kernel activo, árboles de módulos, headers/build y elementos comunes del arranque.

## Kernels
Se inspeccionan versiones instaladas en `/usr/lib/modules` o `/lib/modules`, la versión activa, headers/build, módulos cargados, parámetros de arranque, UEFI, lockdown y rutas comunes de GRUB/systemd-boot. Se reconocen etiquetas habituales LTS, Zen, Hardened, CachyOS, XanMod y Liquorix si aparecen en la versión. Es una clasificación orientativa, no una verificación del paquete exacto.

La gestión de kernels permanece en modo solo lectura: no se instala/elimina ningún kernel, no se regenera initramfs y no se modifica GRUB/systemd-boot. Debe conservarse un kernel de respaldo arrancable.

## Límites
La detección inicial no certifica todos los controladores ni módulos. La compatibilidad de CPU, GPU, Vulkan, OpenGL, kernel y herramientas de juego debe evaluarse por separado antes de activar cada función.
