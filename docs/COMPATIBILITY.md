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

El gestor guiado ofrece paquetes oficiales de kernel y headers/devel para Arch (estándar/LTS/Zen), Debian, Fedora y SUSE. Muestra el comando antes de ejecutarlo y requiere escribir INSTALAR; distribuciones desconocidas no reciben planes de instalación. La comprobación de respaldo busca otro árbol de módulos o múltiples imágenes de kernel en rutas comunes y es heurística, por lo que el usuario debe verificar manualmente que existe una entrada anterior arrancable. Si no se detecta un posible respaldo, la instalación se bloquea.

No elimina kernels, no cambia GRUB/systemd-boot, no regenera initramfs manualmente y no modifica parámetros de arranque. Después de la instalación, se debe revisar la salida del gestor de paquetes y comprobar initramfs/entradas de arranque antes de reiniciar. La guía integrada explica cómo seleccionar el kernel anterior para recuperación. Los nombres de paquetes y repositorios pueden variar entre derivadas.

## Límites
La detección inicial no certifica todos los controladores ni módulos. La compatibilidad de CPU, GPU, Vulkan, OpenGL, kernel y herramientas de juego debe evaluarse por separado antes de activar cada función.
