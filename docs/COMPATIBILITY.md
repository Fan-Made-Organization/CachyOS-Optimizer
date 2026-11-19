# Compatibilidad

El porcentaje de compatibilidad se calcula a partir de comprobaciones reales disponibles. No representa una garantía de rendimiento ni de estabilidad.

## Plataformas Linux detectadas

El detector lee `/etc/os-release` (incluidos enlaces simbólicos estándar) y, si no puede obtener datos, consulta `/usr/lib/os-release` y marcadores clásicos de algunas distribuciones. Usa `ID` y `ID_LIKE`, sin inferir una familia solo porque encuentre un ejecutable de gestor de paquetes.

Familias reconocidas:
- **Arch:** CachyOS, Arch, Manjaro, EndeavourOS, Garuda, ArcoLinux, Artix, RebornOS, SteamOS, HoloISO, Archcraft y blendOS.
- **Debian:** Debian, Ubuntu y sabores, Linux Mint, Pop!_OS, elementary OS, Kali, Zorin, MX Linux, Deepin, Raspberry Pi OS, Parrot, Devuan, KDE neon y derivadas reconocidas.
- **Fedora/RHEL:** Fedora, RHEL, CentOS, Rocky, AlmaLinux, Oracle Linux, Nobara, Ultramarine, Bazzite y variantes inmutables basadas en Fedora cuando declaran esos IDs.
- **SUSE:** openSUSE Leap/Tumbleweed, SLES/SLED y GeckoLinux.
- **Alpine, Gentoo, Void y NixOS:** se reconocen como familias independientes.
- **UNKNOWN:** se conserva cuando no hay evidencia suficiente; no se fuerza una familia por detectar `pacman`, `apt` u otro ejecutable.

El gestor se detecta dentro de la familia: Pacman, APT/APT-GET, DNF5/DNF/YUM, Zypper, APK, Emerge, XBPS o Nix. La detección del gestor no implica permiso para instalar paquetes.

## Instalador de herramientas

El catálogo de herramientas se organiza en categorías seleccionables: diagnóstico/hardware, gráficos/vídeo, almacenamiento, red, monitorización y gaming. Los nombres de paquete se adaptan a los perfiles implementados para Arch, Debian, Fedora, SUSE y un subconjunto de Alpine. Gentoo, Void y NixOS se detectan, pero permanecen en solo lectura para este instalador hasta validar listas y particularidades de sus repositorios.

El instalador muestra la distribución, ID, versión, familia, gestor, categoría y paquetes. Requiere escribir `INSTALAR` antes de lanzar el gestor; el gestor conserva su propia confirmación. No usa aceptación automática, no ejecuta actualizaciones globales, no elimina paquetes, no instala drivers/kernels y no habilita servicios. La existencia de un paquete puede variar por versión, arquitectura y repositorios habilitados.
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

## Interpretación de resultados

La detección de una distribución, la disponibilidad de un gestor y la posibilidad de ejecutar una acción son comprobaciones independientes. Una derivada puede cambiar repositorios, nombres de paquetes, kernel, sistema de inicio o políticas de solo lectura. En equipos híbridos, máquinas virtuales, contenedores y sistemas inmutables, parte del hardware puede estar oculto o gestionado por otra capa. Un dato desconocido no equivale a incompatibilidad; un controlador detectado tampoco garantiza aceleración funcional.


## Controladores gráficos por distribución

El catálogo multiplataforma contiene perfiles de paquetes para Intel/AMD y componentes comunes en Arch, Debian, Fedora, SUSE y Alpine. NVIDIA propietario solo se ofrece en perfiles donde se han definido paquetes de repositorio base (Arch y Debian); en Fedora/SUSE/Alpine se mantiene deshabilitado porque puede exigir repositorios externos, módulos compatibles y configuración específica. En Arch el perfil `nvidia-open` no es válido para todas las GPU. Los perfiles no mezclan fabricantes ni eliminan controladores existentes. Gentoo, Void y NixOS permanecen en solo lectura para esta acción.

La disponibilidad de paquetes depende de versión, arquitectura, repositorios y variantes. Una lista de paquetes es un plan revisable, no una certificación de compatibilidad. DXVK/VKD3D son traducción Direct3D→Vulkan y no controladores DirectX nativos.

## Distribuciones inmutables y basadas en imágenes

SteamOS, Bazzite, Bluefin, Fedora Silverblue/Kinoite, Aurora y Fedora CoreOS se detectan como sistemas donde no debe asumirse que una instalación genérica con gestores como pacman o dnf sea segura. Linux Optimizer bloquea los perfiles genéricos de instalación de herramientas y controladores en estos sistemas, y mantiene disponibles los diagnósticos de solo lectura. Usa el mecanismo de capas, extensiones o imágenes recomendado por el proveedor de la distribución.

La detección también consulta indicadores del sistema como /run/ostree-booted; no pretende reconocer todas las variantes inmutables existentes. Si tu distribución usa un modelo especial de despliegue, trata la instalación automática como no compatible hasta que exista un perfil específico y probado.
