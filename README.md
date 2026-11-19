# Linux Optimizer

<p align="center">
  <strong>Diagnóstico, análisis de compatibilidad y preparación para optimizar Linux con seguridad.</strong><br>
  Diseñado para Linux: detección ampliada para Arch, Debian, Fedora, SUSE, Alpine, Gentoo, Void y NixOS.
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> ·
  <a href="README.en.md">🇬🇧 English</a> ·
  <a href="README.pt-BR.md">🇧🇷 Português</a>
</p>

> **Estado actual: versión temprana (0.1.0-SNAPSHOT).** Incluye diagnósticos y algunas acciones optativas y reversibles para CPU, zram y TRIM, además de un creador de parámetros de Steam. No aplica cambios a escondidas ni promete ganancias fijas de rendimiento. Consulta [el estado real del proyecto](#estado-real-del-proyecto).

## ¿Qué es Linux Optimizer?

Linux Optimizer es un proyecto libre y en desarrollo para reunir en una sola herramienta información del sistema, hardware, compatibilidad, kernels y componentes relacionados con el uso diario y los videojuegos en distintas distribuciones Linux.

Su objetivo es ayudar a entender el equipo antes de cambiar configuraciones: detectar el entorno, mostrar información útil, señalar herramientas que no estén disponibles y preparar recomendaciones que puedan evaluarse de forma segura.

**No promete aumentos mágicos de FPS ni modifica el sistema a escondidas.** Primero observa y explica; las acciones disponibles son específicas, optativas y requieren autorización del usuario.

## Lo que ofrece actualmente

### 1. Información del sistema
- Detección de información básica de Linux.
- Identificación de distribución, kernel, escritorio y arquitectura cuando el sistema expone esos datos.
- Presentación de los datos detectados en la terminal.

### 2. Información del hardware
- Recopilación de información del hardware disponible mediante las interfaces de Linux.
- Resumen del equipo para ayudar a contextualizar los diagnósticos.
- Los datos que el sistema no exponga se muestran como desconocidos o no disponibles; no se inventan.

### 3. Análisis de compatibilidad
- Análisis inicial de compatibilidad a partir de la información detectada.
- Reglas de compatibilidad configurables en los recursos del proyecto.
- La evaluación depende de los datos disponibles y no sustituye pruebas reales de rendimiento o estabilidad.

### 4. Diagnósticos por módulos

| Módulo | Qué inspecciona |
|---|---|
| CPU | Modelo, núcleos lógicos, gobernador y frecuencias reportadas. |
| Memoria | RAM, swap, cachés, dispositivos zram y presión PSI del kernel. |
| GPU | Información gráfica expuesta por Linux y herramientas disponibles. |
| Almacenamiento | Dispositivos y datos de almacenamiento detectables. |
| Energía y temperatura | Sensores térmicos tipados, dispositivos de refrigeración y estado de batería. |
| Red | Estado de enlace, velocidad anunciada y contadores RX/TX acumulados. |
| Audio | Entorno de audio y señales disponibles de PipeWire/PulseAudio. |
| Gaming | Indicadores y herramientas relacionadas con juegos en Linux. |
| KDE Plasma | Información del entorno KDE cuando está presente. |
| Steam | Detección de Steam y elementos relacionados que puedan inspeccionarse. |
| Proton | Información disponible sobre Proton y compatibilidad de juegos. |
| Diagnóstico general | Agrupa las comprobaciones generales del sistema. |
| Kernel y arranque | Kernel activo, variantes identificables, árboles de módulos, headers, módulos cargados, UEFI y rutas comunes de GRUB/systemd-boot. |
| Compatibilidad de distribución Linux | Familia de distribución, versión e identificación del gestor de paquetes. |
| Hardware Intelligence | Capacidades anunciadas de CPU, topología básica y recuento PCI. |
| Driver Inspector | Asociaciones de dispositivos PCI con controladores del kernel y módulos cargados. |
| System Health Monitor | Memoria, carga promedio, espacio raíz y disponibilidad de sensores. |
| Power Intelligence | Gobernador, controlador de frecuencia, unidades de gestión energética y batería. |
| Package Intelligence | Detección de Pacman y ayudantes AUR; señala comprobaciones de paquetes pendientes. |

La presencia de un módulo no significa que todos los controladores, dispositivos o herramientas estén instalados. Cada módulo depende de lo que el sistema permita detectar.

### 5. Perfiles de optimización
El catálogo incluye seis enfoques: Equilibrado, Rendimiento, Ahorro de energía, Gaming, Diagnóstico y Mantenimiento. Son guías de comprobaciones y siguientes pasos; no aplican cambios en lote ni prometen FPS. Las acciones disponibles se realizan por separado desde el centro de optimización, con confirmación y reversión cuando corresponde.

### 6. Interfaz gráfica y panel de análisis del sistema

Linux Optimizer incluye una interfaz gráfica Swing de escritorio, con tema oscuro, navegación lateral, tarjetas de hardware, paneles por categoría y actualización en segundo plano. Se ejecuta con `bash scripts/launch.sh --gui`. La interfaz no bloquea la ventana mientras recopila diagnósticos y mantiene las consultas en modo de solo lectura. También se conserva el menú de terminal: `bash scripts/launch.sh` o `bash scripts/launch.sh --menu`.

El CLI presenta un panel ASCII con distribución, base inferida, kernel, escritorio, arquitectura, CPU, GPU, RAM y evaluación de compatibilidad. Los valores desconocidos se muestran como tales; el estado no se declara excelente si existen comprobaciones desconocidas.

```text
╔════════════════════════════════════════════╗
║LINUX-OPTIMIZER                     ║
║SYSTEM ANALYSIS                            ║
╠════════════════════════════════════════════╣
║Sistema:       CachyOS                     ║
║Base:          Arch Linux                  ║
║Kernel:        Linux 6.x                   ║
║Escritorio:    KDE Plasma                  ║
║Arquitectura:  x86_64                      ║
╠════════════════════════════════════════════╣
║Compatibilidad: 90%                       ║
║Datos: 4 evaluados | 0 desconocidos       ║
║Estado:        EXCELENTE                   ║
╚════════════════════════════════════════════╝
```

Los valores anteriores son ilustrativos; el programa muestra los datos reales detectados en cada equipo.

Para abrir la aplicación gráfica:
```bash
bash scripts/launch.sh --gui
```


### 7. Informes exportables
- Exportación del diagnóstico modular a JSON o HTML.
- Los informes incluyen fecha de generación, hallazgos y advertencias de cada módulo.
- El exportador no sobrescribe archivos existentes.

Ejemplo (ejecuta primero el diagnóstico y añade una o ambas opciones):

```bash
bash scripts/launch.sh --diagnostics --report-json "$HOME/cachyos-report.json"
bash scripts/launch.sh --diagnostics --report-html "$HOME/cachyos-report.html"
```

### 8. Respaldos, auditoría y benchmarks
- `BackupManager`: crea respaldos de archivos seleccionados, genera un manifiesto y verifica SHA-256.
- Restauración a una carpeta elegida, con verificación de integridad y sin sobrescribir archivos existentes.
- `AuditLogger`: registra localmente el resumen de diagnósticos en el directorio de estado del usuario.
- `BenchmarkEngine`: microbenchmark CPU opcional, limitado y sin cambios de configuración.
- No se respaldan ni restauran archivos del sistema automáticamente; la restauración no escribe sobre las rutas originales.

### 9. Monitorización y benchmarks ampliados

- `--monitor [segundos]`: monitor CPU/RAM/carga en vivo; duración de 1 a 300 segundos.
- `--benchmark-memory [MiB]`: prueba sintética acotada de memoria (8-64 MiB).
- `--benchmark-storage [MiB]`: lectura/escritura temporal de 8-32 MiB; el archivo de prueba se elimina al terminar.
- `--benchmark-suite [MiB]`: ejecuta CPU, RAM y almacenamiento, y guarda una fila CSV en `~/.local/state/cachyos-optimizer/benchmarks.csv`.
- `--compare-benchmarks antes.csv despues.csv`: compara las últimas filas de dos historiales CSV.
- `SystemCore`: captura común e inmutable de los informes modulares.
- Inventario PCI/USB y revisión indicativa de dispositivos sin enlace de controlador.
- Diagnóstico avanzado de VRAM cuando el controlador la expone, velocidad de enlace anunciada de red, y disponibilidad de `smartctl`/`nvme` sin ejecutar autopruebas ni modificar discos.

Ejemplos:
```bash
bash scripts/launch.sh --monitor 30
bash scripts/launch.sh --benchmark-memory 32
bash scripts/launch.sh --benchmark-storage 16
bash scripts/launch.sh --benchmark-suite 16
bash scripts/launch.sh --compare-benchmarks antes.csv despues.csv
```

Los benchmarks de RAM y almacenamiento son sintéticos: influyen JIT, cachés, sistema de archivos y carga del equipo. No equivalen a mediciones de laboratorio ni a FPS. No se incluye todavía un benchmark de GPU real; no se inventan puntuaciones gráficas cuando no hay un motor gráfico de pruebas disponible. Los diagnósticos de CPU, memoria, red y energía son lecturas del kernel, no pruebas de estrés.

El inventario sin controlador asociado no demuestra por sí solo un fallo o conflicto. Para confirmar problemas se necesita correlacionar modelo, kernel y registros.

### 10. Limpiador conservador de caché

El menú ofrece **Limpiador seguro de caché** y el comando `--cleaner`. Primero presenta una vista previa de archivos antiguos (más de 30 días) dentro de `~/.cache/thumbnails` y `~/.cache/fontconfig`. Solo elimina la categoría que el usuario escribe y confirma. Rechaza enlaces simbólicos y rutas fuera de las carpetas permitidas. No toca cachés de shaders, navegadores, paquetes, juegos ni archivos personales.

### 11. Instalador de herramientas de optimización

El menú incluye **Instalar herramientas de optimización** y el comando `--install-tools`. El detector identifica la familia y el gestor de paquetes antes de ofrecer un perfil. En las familias con catálogo validado se puede elegir una categoría individual o instalar el conjunto completo:

| Categoría | Utilidad |
|---|---|
| Diagnóstico y hardware | `inxi`, `lshw`, `dmidecode`, `smartmontools`, `lm_sensors`/`lm-sensors`, `pciutils`, `usbutils` |
| Gráficos y vídeo | `vulkan-tools`, utilidades Mesa, VA-API, `vdpauinfo`, `nvtop` según distribución |
| Almacenamiento | `nvme-cli`, `hdparm`, `iotop` |
| Red | `iperf3`, `ethtool`, herramientas DNS y `traceroute` |
| Monitorización | `btop`, `fastfetch`, `htop`, `glances`, `sysstat` según distribución |
| Gaming | `gamemode`, `mangohud`, `gamescope`, `goverlay`, `vkbasalt`, `protontricks` y paquetes multilib cuando corresponda |
| Audio y multimedia | `alsa-utils`, `pavucontrol`, `ffmpeg`/`ffmpeg-free`, `v4l-utils` |
| Desarrollo y depuración | `strace`, `gdb`, `valgrind` |
| Energía y sensores | `powertop`, `acpi`, `upower` |

Los nombres de paquetes se adaptan a Arch, Debian/Ubuntu, Fedora y openSUSE; Alpine tiene un catálogo más reducido. Gentoo, Void y NixOS se reconocen, pero todavía no reciben un perfil automático de herramientas: se mantiene el modo de solo lectura hasta validar sus paquetes y particularidades. No se supone que un mismo nombre exista en todos los repositorios.

El instalador muestra distribución, versión, familia, gestor, categoría y lista exacta de paquetes. Para continuar hay que escribir `INSTALAR`; luego el gestor de paquetes muestra su propia transacción y mantiene sus mecanismos de confirmación. No se usan opciones de aceptación automática, no se actualiza todo el sistema, no se eliminan paquetes y no se activan servicios.

**Alcance importante:** instala utilidades de usuario; no instala controladores gráficos, kernels, firmware ni servicios automáticamente. Las herramientas específicas de GPU solo aportan datos si el hardware es compatible. La disponibilidad puede depender de la versión de la distribución, repositorios habilitados y arquitectura.

Ejecutar directamente:

```bash
bash scripts/launch.sh --install-tools
```
### 12. Compatibilidad ampliada de distribuciones y kernels Linux

El proyecto ya no limita sus diagnósticos a CachyOS/Arch. Detecta la distribución mediante `/etc/os-release` y clasifica familias:

| Familia | Distribuciones de referencia | Gestor reconocido |
|---|---|---|
| Arch | CachyOS, Arch, Manjaro, EndeavourOS, Garuda, Artix, SteamOS y derivadas identificadas | pacman |
| Debian | Debian, Ubuntu, Mint, Pop!_OS, Kali, Zorin, Deepin, MX y derivadas identificadas | apt / apt-get |
| Fedora / RHEL | Fedora, Nobara, Bazzite, RHEL, CentOS, Rocky, AlmaLinux y derivadas identificadas | dnf5 / dnf / yum |
| SUSE | openSUSE Leap/Tumbleweed, SLES/SLED y derivadas identificadas | zypper |
| Alpine | Alpine Linux | apk |
| Gentoo | Gentoo y Calculate | emerge |
| Void | Void Linux | xbps-install |
| NixOS | NixOS | nix (detección; sin instalación automática) |

La clasificación usa `ID` y `ID_LIKE`, permite enlaces simbólicos estándar de `/etc/os-release` y consulta marcadores clásicos si no hay datos. No significa que cada derivada tenga los mismos repositorios o nombres de paquetes. Gentoo, Void y NixOS se reconocen pero permanecen en solo lectura para el instalador de herramientas; las familias desconocidas también.

El instalador de herramientas usa listas pequeñas y específicas por familia. Muestra los paquetes antes de empezar y deja que APT, DNF/YUM, Pacman o Zypper muestren su transacción y soliciten aprobación. No usa opciones de confirmación automática. No instala controladores, kernels, servicios ni gestores de energía.

**Soporte de kernels:** inspecciona la versión activa, kernels con árboles de módulos instalados, presencia de headers/build, módulos cargados, parámetros de arranque, estado de lockdown, UEFI y rutas comunes de GRUB/systemd-boot. Reconoce etiquetas habituales como LTS, Zen, Hardened, CachyOS, XanMod y Liquorix cuando aparecen en la versión. La identificación por nombre es orientativa.

**Gestor guiado de kernels:** la opción 10 ahora propone paquetes oficiales para Arch/CachyOS (estándar, LTS y Zen), Debian/Ubuntu, Fedora y openSUSE, incluyendo los paquetes de headers/devel correspondientes. Antes de ejecutar muestra el comando, inspecciona árboles de módulos y archivos comunes de /boot, y exige escribir `INSTALAR`. Si no detecta un posible kernel alternativo o más de una imagen de kernel, bloquea la instalación. Esta comprobación es heurística: el usuario debe verificar manualmente que el kernel actual y el respaldo realmente arrancan. Distribuciones desconocidas permanecen en solo lectura.

El gestor nunca elimina kernels, no modifica GRUB/systemd-boot, no regenera initramfs manualmente ni cambia parámetros de arranque. Tras instalar, revisa la salida del gestor de paquetes y verifica initramfs/entradas de arranque antes de reiniciar. La guía de recuperación explica cómo seleccionar el kernel anterior desde GRUB/systemd-boot. Los nombres de paquetes pueden variar en derivadas y repositorios personalizados; no se garantiza compatibilidad universal.

Ejecuta el informe con:

```bash
bash scripts/launch.sh --linux-support
```

También aparece como opción 10 en el menú principal y sus datos se incluyen en el diagnóstico modular.

### 13. Instalador de controladores gráficos

El centro de optimización incorpora una sección separada para paquetes gráficos de Arch/CachyOS. Permite elegir un conjunto por fabricante: Intel (Mesa/OpenGL, ANV Vulkan e Intel Media), AMD (Mesa/OpenGL, RADV Vulkan y VA-API), NVIDIA (nvidia-open y bibliotecas de usuario), o compatibilidad DirectX para Wine/Proton (DXVK/VKD3D). También ofrece los componentes comunes Mesa y Vulkan loader. La lista se muestra antes de ejecutar Pacman y requiere confirmación; no se mezclan paquetes de Intel/AMD/NVIDIA ni se desinstala el controlador actual.

**Compatibilidad importante:** el paquete NVIDIA `nvidia-open` requiere una GPU compatible con el módulo abierto y un kernel compatible; no es apropiado para todas las generaciones. Comprueba el modelo exacto y el paquete correspondiente a tu kernel antes de confirmar. En algunos sistemas CachyOS puede ser preferible instalar el paquete NVIDIA específico de su kernel mediante las herramientas oficiales de la distribución. Conserva un kernel de respaldo.

Linux no instala un “driver DirectX” nativo: DXVK y VKD3D traducen Direct3D a Vulkan para Wine/Proton. Proton normalmente incluye versiones propias, así que instalar capas globales no es obligatorio. FSR tampoco es un controlador; depende del juego, Proton o Gamescope. La instalación de paquetes no garantiza que una aplicación o juego utilice una función concreta. Después de instalar, reinicia la sesión si corresponde y ejecuta `--diagnostics` para comprobar OpenGL/Vulkan.

### 14. Centro de optimización guiada

Abre el centro desde el menú principal (opción 7) o ejecuta `bash scripts/launch.sh --optimizer`. Reúne seis áreas:

- **CPU:** consulta y cambia entre `balanced`, `performance` y `power-saver` mediante `power-profiles-daemon`. También permite consultar y cambiar gobernadores CPU realmente disponibles (`performance`, `powersave`, `schedutil`, etc.) mediante `cpupower`. Guarda el valor anterior y permite restaurarlo. El controlador de frecuencia o el servicio de energía puede limitar qué opciones están disponibles.
- **RAM:** detecta zram y permite crear/activar una configuración administrada con `zram-generator` (zstd; tamaño objetivo hasta la mitad de la RAM, máximo 4 GiB). Solo actúa si no existe ya `/etc/systemd/zram-generator.conf`; guarda un marcador para reconocer su propia configuración y ofrece retirarla desde el mismo menú. No toca una configuración zram ajena ni purga cachés.
- **GPU:** enumera tarjetas DRM, fabricante PCI y controlador asociado; muestra orientación general para Intel/AMD/NVIDIA. No instala ni reemplaza controladores automáticamente.
- **Gaming:** instala el conjunto de herramientas opcionales (GameMode, MangoHud, Gamescope, GOverlay y utilidades de diagnóstico).
- **Almacenamiento:** si detecta un dispositivo no rotacional, permite habilitar `fstrim.timer` con `sudo systemctl enable --now fstrim.timer`. Se puede revertir desde el mismo menú o con `sudo systemctl disable --now fstrim.timer`. El menú también muestra el estado y permite volver a habilitarlo.
- **Kernel:** muestra la versión activa y los directorios de módulos/kernels instalados. No elimina ni sustituye kernels.

Las operaciones CPU y TRIM solicitan confirmación. El perfil de energía se puede restaurar desde el mismo centro; TRIM se revierte deshabilitando el temporizador. No se prometen ganancias fijas de rendimiento.

**Importante:** zram, perfiles de energía, gobernadores y TRIM requieren confirmación; zram solo puede revertirse automáticamente si la configuración sigue coincidiendo con la que creó la app. La gestión de controladores GPU y la sustitución/eliminación de kernels siguen siendo informativas: requieren selección manual y recuperación específica para cada equipo.

### 15. Creador de parámetros de Steam

Desde el menú principal (opción 8), el centro de optimización (opción 7) o con `bash scripts/launch.sh --steam-options`, abre un asistente para generar una línea lista para pegar en **Steam → Propiedades → General → Parámetros de lanzamiento**. Incluye plantillas para `%command%`, GameMode, MangoHud, Gamescope, registro de Proton, FSR de Proton y WineD3D (`PROTON_USE_WINED3D=1`), combinaciones personalizadas, resolución y frecuencia objetivo de Gamescope, y perfiles de juego (competitivo, equilibrado, diagnóstico y escalado). Puede guardar la línea en `~/.local/share/cachyos-optimizer/` sin sobrescribir archivos existentes.

Las opciones son plantillas: no instalan herramientas ni garantizan compatibilidad con todos los juegos. GameMode, MangoHud y Gamescope deben estar instalados; FSR depende de la versión de Proton y del juego. WineD3D fuerza la traducción Direct3D a OpenGL en lugar de DXVK/Vulkan. Puede servir como alternativa cuando Vulkan o DXVK presenta problemas, pero no es una optimización universal y suele ofrecer menos rendimiento que DXVK; actívalo solo si el juego lo necesita. La resolución y frecuencia son objetivos para Gamescope, no promesas de FPS ni cambios automáticos de los ajustes gráficos internos del juego. El registro de Proton es para diagnóstico y puede generar archivos de log.

### 16. Límites de seguridad
- El diagnóstico normal es de solo lectura; las acciones disponibles se muestran por separado y requieren confirmación explícita.
- Los comandos administrativos usan una lista cerrada de operaciones y no interpolan parámetros libres de shell.
- Los cambios de CPU, zram y TRIM tienen mecanismos de reversión documentados; GPU permanece en modo de inspección. El gestor de kernels puede instalar paquetes oficiales con confirmación explícita, pero nunca elimina kernels ni modifica el bootloader.
- El respaldo restaura copias verificadas en otra carpeta; no altera automáticamente los archivos originales.

## Estado real del proyecto

| Funcionalidad | Estado |
|---|---|
| Detección e información básica de Linux | Implementación inicial |
| Inspección de hardware | Implementación inicial |
| Diagnósticos modulares (incluye hardware, controladores, salud, energía y paquetes) | Implementación inicial; algunas comprobaciones son indicativas |
| Análisis de compatibilidad | Implementación inicial |
| Catálogo de perfiles | Disponible como catálogo informativo |
| Validación y preparación de acciones | Implementada; algunas acciones optativas se ejecutan con confirmación y reversión documentada |
| Backup de archivos seleccionados y restauración verificada a otra carpeta | Implementado |
| Instalación guiada de herramientas de diagnóstico y gaming en Arch/CachyOS | Implementada; confirmación previa y Pacman interactivo |
| Centro guiado CPU/RAM/GPU/Gaming/almacenamiento/kernel | Implementado parcialmente; CPU, zram y TRIM tienen acciones reversibles; GPU es informativo; gestor de kernels ofrece instalación guiada por distro y guía de recuperación, sin eliminación ni cambios de bootloader |
| Creador de parámetros de Steam | Implementado; plantillas, perfiles de juego, Gamescope con resolución/Hz y guardado local sin sobrescritura |
| Configuración zram administrada y reversible | Implementada con `zram-generator`; no sobrescribe configuración existente |
| Restauración sobre rutas originales y rollback del sistema | **Pendiente** |
| Registro local de auditoría | Implementado con rotación básica |
| Interfaz gráfica | No prevista; el proyecto prioriza una interfaz de terminal. |
| Microbenchmark CPU local | Implementado como prueba opcional; no mide FPS ni GPU |
| Exportación de informes JSON/HTML | Implementación inicial |
| Monitor CPU/RAM y carga en vivo | Implementación inicial, sesión limitada a 5 minutos |
| Benchmarks sintéticos de RAM y almacenamiento | Implementación inicial, tamaños acotados |
| Historial y comparación antes/después de benchmarks | Implementación inicial, CSV |
| Inventario PCI/USB y revisión indicativa de controladores | Implementación inicial |
| System Core y captura común de módulos | Implementación inicial |

El código Java que Maven compila se encuentra en `src/main/java`. Los archivos bajo `src/main/resources/java` son recursos heredados y no forman parte del conjunto de clases compiladas por la configuración Maven actual.

## Compatibilidad y enfoque

- **Objetivo principal:** CachyOS y Arch Linux.
- **Entorno:** Linux.
- **Lenguaje:** Java 21.
- **Escritorio:** se intenta reconocer KDE Plasma cuando está disponible.
- **Gaming:** se inspeccionan componentes relacionados con Steam, Proton y herramientas gráficas cuando pueden detectarse.
- **Otras distribuciones:** pueden aportar datos útiles, pero no se garantiza todavía el mismo nivel de detección ni compatibilidad.

## Requisitos

- Linux para obtener diagnósticos completos.
- Linux x86_64 o ARM64 para la descarga automática del JDK.
- Conexión a Internet durante la primera preparación.
- Herramientas base: `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`.

Si falta Java 21, el bootstrap descarga Temurin JDK 21 y verifica SHA-256; si falta Maven, descarga Maven 3.9.9 y verifica SHA-512. Ambos quedan dentro de `.tools/` en la copia local del repositorio. Las dependencias Java se guardan en `.cache/`. No instala software globalmente ni ejecuta comandos de administrador. Las herramientas opcionales de diagnóstico no se instalan automáticamente.

## Instalación y ejecución

La idea es que puedas clonar el repositorio y arrancar el programa sin configurar Java ni Maven manualmente.

### Opción 1: clonar y ejecutar (recomendada)

Abre una terminal en Linux y ejecuta:

```bash
git clone https://github.com/Fan-Made-Organization/Linux-Optimizer.git
cd Linux-Optimizer
bash scripts/launch.sh
```

**Eso es todo para la primera ejecución.** El lanzador:
1. Comprueba si Java 21 y Maven están disponibles.
2. Si faltan, descarga Temurin JDK 21 y/o Maven 3.9.9 dentro de `.tools/` y verifica sus hashes.
3. Ejecuta la preparación y compilación necesarias.
4. Genera el JAR si todavía no existe.
5. Inicia Linux Optimizer.

En los siguientes inicios, desde la carpeta del proyecto, solo ejecuta:

```bash
bash scripts/launch.sh
```

Puedes pasar opciones directamente al programa:

```bash
bash scripts/launch.sh                 # abre el menú de terminal
bash scripts/launch.sh --diagnostics   # diagnóstico directo
bash scripts/launch.sh --benchmark     # diagnóstico + microbenchmark CPU
bash scripts/launch.sh --install-tools # instala herramientas recomendadas
bash scripts/launch.sh --optimizer     # centro de optimización
bash scripts/launch.sh --steam-options # creador de parámetros de Steam
bash scripts/launch.sh --help
```
### Opción 2: usar una interfaz gráfica para clonar

En GitHub, pulsa **Code → Open with GitHub Desktop** (si tienes GitHub Desktop configurado), clona el repositorio y abre una terminal dentro de la carpeta `Linux-Optimizer`. Después ejecuta:

```bash
bash scripts/launch.sh
```

También puedes usar cualquier cliente Git que permita clonar el repositorio. El paso importante es ejecutar el lanzador desde la carpeta clonada.

### Requisitos del equipo

- Linux x86_64 o ARM64.
- Conexión a Internet en la primera ejecución para descargar Java/Maven si no están presentes.
- Herramientas base del sistema: `bash`, `git`, `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`.

Java 21, Maven y las dependencias Java se guardan dentro del repositorio (`.tools/` y `.cache/`); no se instalan globalmente ni se necesitan permisos de administrador. Las herramientas base de Linux no se descargan automáticamente. Las herramientas opcionales se ofrecen mediante el instalador integrado, que solicita aprobación antes de usar Pacman.

### Compilar, probar y usar Maven manualmente (opcional)

Si ya tienes Java 21 y Maven configurados:

```bash
mvn clean verify
mvn package
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar
```

Para respaldar archivos que selecciones explícitamente y restaurarlos en otra carpeta:

```bash
bash scripts/launch.sh --backup "$HOME/x504-backups" "$HOME/.config/example.conf"
bash scripts/launch.sh --restore "$HOME/x504-backups/backup-..." "$HOME/x504-restored"
```

Scripts auxiliares:
- `scripts/bootstrap.sh`: prepara Java/Maven locales y ejecuta verificación y pruebas.
- `scripts/build.sh`: compilar y verificar.
- `scripts/test.sh`: ejecutar las pruebas.
- `scripts/package.sh`: generar el JAR.
- `scripts/launch.sh`: preparar lo necesario, empaquetar si hace falta e iniciar la aplicación.
- `scripts/clean.sh`: limpiar archivos generados.

Si algo falla, revisa [Instalación](docs/INSTALLATION.md), [Dependencias](docs/DEPENDENCIES.md) y [Solución de problemas](docs/TROUBLESHOOTING.md).

## Seguridad: cómo pretende trabajar

1. Inspeccionar el sistema antes de sugerir cambios.
2. Explicar el propósito, beneficio esperado, riesgo y compatibilidad de cada acción.
3. Solicitar aprobación explícita antes de modificar el equipo.
4. Preparar una forma de volver atrás cuando el cambio lo permita.
5. Registrar qué se propuso y qué se ejecutó.

La aplicación no debe ejecutarse como root para realizar diagnósticos normales. Las operaciones administrativas se invocan con `sudo` solo después de una confirmación visible. La configuración zram se crea únicamente si no existe una configuración principal previa y su reversión comprueba que el archivo siga siendo exactamente el generado por la aplicación. Lee [Política de seguridad](docs/SECURITY.md).

## Documentación

- [Arquitectura](docs/ARCHITECTURE.md)
- [Gestor de respaldos](docs/BACKUPS.md)
- [Módulos](docs/MODULES.md)
- [Compatibilidad](docs/COMPATIBILITY.md)
- [Dependencias y preparación](docs/DEPENDENCIES.md)
- [Instalación](docs/INSTALLATION.md)
- [Seguridad](docs/SECURITY.md)
- [Enfoque de optimizaciones](docs/OPTIMIZATIONS.md)
- [Desarrollo y contribución](docs/DEVELOPMENT.md)
- [Solución de problemas](docs/TROUBLESHOOTING.md)
- [Registro de cambios](docs/CHANGELOG.md)
- [Hoja de ruta](docs/ROADMAP.md)
- [Auditoría de la base funcional](docs/BASELINE_AUDIT.md)

## Desarrollo y contribuciones

Las contribuciones son bienvenidas. Antes de proponer una función:
- Comprueba si ya existe una implementación o una tarea relacionada.
- Mantén el diagnóstico de solo lectura como comportamiento predeterminado.
- No presentes una optimización como disponible si solo existe una clase, configuración o idea.
- Añade pruebas para los nuevos comportamientos.
- Documenta compatibilidad, riesgos, permisos y reversibilidad.
- Mantén nombres de archivos, paquetes, clases, métodos y variables en inglés; la documentación y los mensajes visibles para el usuario se escriben en español.

Para validar los cambios:

```bash
mvn clean verify
```

## Licencia

El proyecto se distribuye bajo la **GNU General Public License v3.0 (GPL-3.0-only)**. Consulta el texto completo en [LICENSE](LICENSE) y la explicación oficial en https://www.gnu.org/licenses/gpl-3.0.html.

---

**Linux Optimizer** — entender el sistema primero; optimizar con responsabilidad después.


## Alcance multiplataforma y límites por distribución

Linux Optimizer adopta un enfoque **Linux-first**: la detección del sistema y los diagnósticos intentan funcionar sin depender de un gestor de paquetes concreto. La disponibilidad de una acción depende, en cambio, de la familia, las herramientas instaladas, el sistema de inicio, el kernel y las políticas de la distribución. Detectar una distribución no equivale a certificar todas sus variantes.

### Qué significa cada nivel de soporte

- **Detectada:** se identifica el nombre, versión, familia o gestor a partir de los datos que expone el sistema.
- **Diagnóstico disponible:** los módulos consultan interfaces estándar como `/proc`, `/sys`, herramientas opcionales y archivos de configuración legibles.
- **Catálogo de paquetes disponible:** existe una lista de herramientas adaptada a una familia y un comando de instalación revisado.
- **Acción optimizable disponible:** existe una operación concreta, con validación, confirmación y una forma documentada de revertirla cuando es posible.

Estos niveles no son equivalentes. En particular, Gentoo, Void y NixOS pueden detectarse aunque algunas operaciones de instalación permanezcan deshabilitadas. Las distribuciones inmutables o basadas en imágenes pueden requerir métodos propios y no deben tratarse como instalaciones tradicionales.

### Recomendaciones antes de aplicar cambios

1. Ejecuta primero `--diagnostics` y conserva el informe.
2. Lee el comando y la lista exacta de paquetes que se muestran antes de aceptar una instalación.
3. Mantén un kernel conocido como funcional y un medio de recuperación cuando cambies componentes de arranque o gráficos.
4. No combines perfiles de energía, gobernadores y herramientas de terceros sin comprobar qué servicio administra cada ajuste.
5. Después de cada cambio, repite el diagnóstico y compara el estado; modifica una sola variable cada vez.

El nombre Linux Optimizer no implica compatibilidad con BSD, Windows, macOS, Android ni con todas las arquitecturas. El soporte actual está centrado en distribuciones GNU/Linux y en las arquitecturas para las que se haya validado el código y el entorno de compilación.

### Compatibilidad de datos locales de versiones anteriores

El nombre del repositorio, la aplicación, el artefacto Maven y los mensajes visibles ahora usan **Linux Optimizer**. Para no perder datos de instalaciones anteriores, algunas ubicaciones internas de estado que contienen `cachyos-optimizer` se conservan temporalmente como rutas heredadas (por ejemplo, historiales y marcadores de acciones reversibles). No son el nombre actual del producto. El lanzador usa la nueva carpeta `~/.config/linux-optimizer/` para preferencias y puede leer la preferencia de idioma anterior; el marcador de primera ejecución también reconoce el archivo heredado.


## Nuevos módulos de soporte gráfico y diagnóstico avanzado

El centro de optimización incorpora un catálogo gráfico por familia Linux para Intel, AMD y NVIDIA. Los perfiles cubren Mesa/OpenGL, Vulkan y componentes de vídeo cuando existe un conjunto de paquetes validado para la familia detectada. La instalación es interactiva y muestra el comando y los paquetes antes de pedir la palabra exacta `INSTALAR`. Las familias sin catálogo permanecen en modo informativo.

**Limitaciones importantes:** Fedora, SUSE y Alpine no reciben un perfil automático de NVIDIA propietario porque puede requerir repositorios externos, módulos específicos y compatibilidad exacta de kernel/Secure Boot. No se añade un repositorio externo automáticamente. NVIDIA en Arch usa el perfil `nvidia-open`, que no es compatible con todos los modelos. Comprueba el modelo de GPU y la documentación de tu distribución antes de continuar. DXVK/VKD3D son capas de traducción Direct3D a Vulkan, no drivers DirectX nativos; Proton suele incluir sus propias versiones.

El asesor energético distingue laptops mediante la presencia de una batería en sysfs y escritorios cuando la interfaz de alimentación existe sin batería. Recomienda un perfil conservador, pero nunca cambia el perfil por sí solo. La disponibilidad real depende de `power-profiles-daemon`.

El comando `--advanced-diagnostics` amplía el diagnóstico con CPU (frecuencias y gobernador expuestos), RAM/swap/PSI, almacenamiento (dispositivos, scheduler y sistemas de archivos montados), interfaces de red/ruta predeterminada, GPU mediante DRM y herramientas gráficas, unidades systemd fallidas, módulos del kernel y herramientas de gaming. Es una revisión de solo lectura: no ejecuta pruebas de velocidad ni estrés, no consulta SMART automáticamente y no cambia servicios, drivers o configuraciones. La frecuencia de CPU es una lectura puntual y la clasificación rotacional no distingue por sí sola SATA SSD de NVMe.


### Actualizaciones automáticas desde GitHub

La aplicación comprueba en segundo plano la última GitHub Release al abrir la interfaz gráfica. También puedes pulsar **Buscar actualizaciones**. Si encuentra una versión nueva, descarga el JAR y su checksum SHA-256, verifica la integridad y lo prepara en `~/.local/share/linux-optimizer/`. El lanzador aplica el archivo verificado en el siguiente inicio; nunca reemplaza el programa mientras está ejecutándose.

Las releases se publican al crear y subir una etiqueta Git con formato `vX.Y.Z`. El flujo `.github/workflows/release.yml` ejecuta `mvn clean verify` y adjunta `linux-optimizer.jar` más su checksum. Hasta que exista una primera GitHub Release, la aplicación indicará que todavía no hay una versión publicada.
