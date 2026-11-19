# CachyOS Optimizer

<p align="center">
  <strong>Diagnóstico, análisis de compatibilidad y preparación para optimizar Linux con seguridad.</strong><br>
  Diseñado con prioridad en CachyOS y Arch Linux.
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> ·
  <a href="README.en.md">🇬🇧 English</a> ·
  <a href="README.pt-BR.md">🇧🇷 Português</a>
</p>

> **Estado actual: versión temprana (0.1.0-SNAPSHOT).** Incluye diagnósticos y algunas acciones optativas y reversibles para CPU, zram y TRIM, además de un creador de parámetros de Steam. No aplica cambios a escondidas ni promete ganancias fijas de rendimiento. Consulta [el estado real del proyecto](#estado-real-del-proyecto).

## ¿Qué es CachyOS Optimizer?

CachyOS Optimizer es un proyecto libre y en desarrollo para reunir en una sola herramienta información del sistema, hardware, compatibilidad y componentes relacionados con el uso diario y los videojuegos en Linux.

Su objetivo es ayudar a entender el equipo antes de cambiar configuraciones: detectar el entorno, mostrar información útil, señalar herramientas que no estén disponibles y preparar recomendaciones que puedan evaluarse de forma segura.

**No promete aumentos mágicos de FPS ni modifica el sistema a escondidas.** Primero observa y explica; las futuras acciones de optimización deberán ser específicas, reversibles y autorizadas por el usuario.

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
| CPU | Información del procesador y datos accesibles de la CPU. |
| Memoria | Información de memoria disponible para el sistema. |
| GPU | Información gráfica expuesta por Linux y herramientas disponibles. |
| Almacenamiento | Dispositivos y datos de almacenamiento detectables. |
| Energía y temperatura | Información energética y sensores térmicos disponibles. |
| Red | Interfaces y datos básicos de red. |
| Audio | Entorno de audio y señales disponibles de PipeWire/PulseAudio. |
| Gaming | Indicadores y herramientas relacionadas con juegos en Linux. |
| KDE Plasma | Información del entorno KDE cuando está presente. |
| Steam | Detección de Steam y elementos relacionados que puedan inspeccionarse. |
| Proton | Información disponible sobre Proton y compatibilidad de juegos. |
| Diagnóstico general | Agrupa las comprobaciones generales del sistema. |
| Hardware Intelligence | Capacidades anunciadas de CPU, topología básica y recuento PCI. |
| Driver Inspector | Asociaciones de dispositivos PCI con controladores del kernel y módulos cargados. |
| System Health Monitor | Memoria, carga promedio, espacio raíz y disponibilidad de sensores. |
| Power Intelligence | Gobernador, controlador de frecuencia, unidades de gestión energética y batería. |
| Package Intelligence | Detección de Pacman y ayudantes AUR; señala comprobaciones de paquetes pendientes. |

La presencia de un módulo no significa que todos los controladores, dispositivos o herramientas estén instalados. Cada módulo depende de lo que el sistema permita detectar.

### 5. Catálogo de perfiles
El proyecto incluye un catálogo inicial de perfiles y descripciones para distintos enfoques de uso. En esta etapa son **referencias informativas**: seleccionarlos no cambia gobernadores, controladores, servicios ni parámetros del sistema.

### 6. Interfaz de terminal y panel de análisis del sistema

CachyOS Optimizer utiliza una interfaz de terminal interactiva, sin interfaz gráfica. Al ejecutar `bash scripts/launch.sh` se abre un menú para iniciar el diagnóstico, ejecutar el benchmark CPU o consultar la ayuda. También se pueden usar los comandos directamente desde la terminal.

El CLI presenta un panel ASCII con distribución, base inferida, kernel, escritorio, arquitectura, CPU, GPU, RAM y evaluación de compatibilidad. Los valores desconocidos se muestran como tales; el estado no se declara excelente si existen comprobaciones desconocidas.

```text
╔════════════════════════════════════════════╗
║CACHYOS-OPTIMIZER                     ║
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

Ejemplos:
```bash
bash scripts/launch.sh --monitor 30
bash scripts/launch.sh --benchmark-memory 32
bash scripts/launch.sh --benchmark-storage 16
bash scripts/launch.sh --benchmark-suite 16
bash scripts/launch.sh --compare-benchmarks antes.csv despues.csv
```

Los benchmarks de RAM y almacenamiento son sintéticos: influyen JIT, cachés, sistema de archivos y carga del equipo. No equivalen a mediciones de laboratorio ni a FPS. No se incluye todavía un benchmark de GPU real; no se inventan puntuaciones gráficas cuando no hay un motor gráfico de pruebas disponible.

El inventario sin controlador asociado no demuestra por sí solo un fallo o conflicto. Para confirmar problemas se necesita correlacionar modelo, kernel y registros.

### 10. Instalador de herramientas de optimización

El menú incluye **Instalar herramientas de optimización** y el comando `--install-tools`. En CachyOS/Arch instala un conjunto aprobado de utilidades:

- Diagnóstico y hardware: `inxi`, `lshw`, `dmidecode`, `smartmontools`, `lm_sensors`.
- Gráficos y aceleración: `vulkan-tools`, `mesa-utils`, `libva-utils`, `vdpauinfo`, `nvtop`.
- CPU y memoria: `power-profiles-daemon`, `cpupower`, `zram-generator`.
- Gaming y monitorización: `gamemode`, `mangohud`, `gamescope`, `goverlay`.

Antes de empezar, muestra la lista completa y pide confirmación. Después ejecuta `sudo pacman -S --needed` sin `--noconfirm`, por lo que Pacman también presenta su resumen y solicita aprobación. Los paquetes que ya estén instalados se conservan.

**Alcance importante:** esta opción instala herramientas de usuario; no instala controladores a ciegas ni cambia kernel, servicios o parámetros de arranque. Los cambios de perfil/gobernador se realizan desde el centro con confirmación. Esos cambios dependen del hardware y requieren módulos específicos, comprobaciones y reversión. Si Pacman no encuentra un paquete o una dependencia, la transacción puede cancelarse sin completar.

Ejecutar directamente:

```bash
bash scripts/launch.sh --install-tools
```

### 11. Centro de optimización guiada

Abre el centro desde el menú principal (opción 7) o ejecuta `bash scripts/launch.sh --optimizer`. Reúne seis áreas:

- **CPU:** consulta y cambia entre `balanced`, `performance` y `power-saver` mediante `power-profiles-daemon`. También permite consultar y cambiar gobernadores CPU realmente disponibles (`performance`, `powersave`, `schedutil`, etc.) mediante `cpupower`. Guarda el valor anterior y permite restaurarlo. El controlador de frecuencia o el servicio de energía puede limitar qué opciones están disponibles.
- **RAM:** detecta zram y permite crear/activar una configuración administrada con `zram-generator` (zstd; tamaño objetivo hasta la mitad de la RAM, máximo 4 GiB). Solo actúa si no existe ya `/etc/systemd/zram-generator.conf`; guarda un marcador para reconocer su propia configuración y ofrece retirarla desde el mismo menú. No toca una configuración zram ajena ni purga cachés.
- **GPU:** enumera tarjetas DRM, fabricante PCI y controlador asociado; muestra orientación general para Intel/AMD/NVIDIA. No instala ni reemplaza controladores automáticamente.
- **Gaming:** instala el conjunto de herramientas opcionales (GameMode, MangoHud, Gamescope, GOverlay y utilidades de diagnóstico).
- **Almacenamiento:** si detecta un dispositivo no rotacional, permite habilitar `fstrim.timer` con `sudo systemctl enable --now fstrim.timer`. Se puede revertir desde el mismo menú o con `sudo systemctl disable --now fstrim.timer`. El menú también muestra el estado y permite volver a habilitarlo.
- **Kernel:** muestra la versión activa y los directorios de módulos/kernels instalados. No elimina ni sustituye kernels.

Las operaciones CPU y TRIM solicitan confirmación. El perfil de energía se puede restaurar desde el mismo centro; TRIM se revierte deshabilitando el temporizador. No se prometen ganancias fijas de rendimiento.

**Importante:** zram, perfiles de energía, gobernadores y TRIM requieren confirmación; zram solo puede revertirse automáticamente si la configuración sigue coincidiendo con la que creó la app. La gestión de controladores GPU y la sustitución/eliminación de kernels siguen siendo informativas: requieren selección manual y recuperación específica para cada equipo.

### 12. Creador de parámetros de Steam

Desde el menú principal (opción 8), el centro de optimización (opción 7) o con `bash scripts/launch.sh --steam-options`, abre un asistente para generar una línea lista para pegar en **Steam → Propiedades → General → Parámetros de lanzamiento**. Incluye plantillas para `%command%`, GameMode, MangoHud, Gamescope, registro de Proton y FSR de Proton, combinaciones personalizadas, resolución y frecuencia objetivo de Gamescope, y perfiles de juego (competitivo, equilibrado, diagnóstico y escalado). Puede guardar la línea en `~/.local/share/cachyos-optimizer/` sin sobrescribir archivos existentes.

Las opciones son plantillas: no instalan herramientas ni garantizan compatibilidad con todos los juegos. GameMode, MangoHud y Gamescope deben estar instalados; FSR depende de la versión de Proton y del juego. La resolución y frecuencia son objetivos para Gamescope, no promesas de FPS ni cambios automáticos de los ajustes gráficos internos del juego. El registro de Proton es para diagnóstico y puede generar archivos de log.

### 13. Límites de seguridad
- Las acciones se validan y preparan, pero el motor no ejecuta modificaciones del sistema.
- Las operaciones críticas se bloquean.
- Los cambios requieren aprobación explícita y las operaciones administrativas requieren respaldo.
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
| Centro guiado CPU/RAM/GPU/Gaming/almacenamiento/kernel | Implementado parcialmente; perfiles de energía, gobernadores CPU, zram administrado y TRIM son acciones optativas; GPU/kernel son inspección guiada |
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
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
bash scripts/launch.sh
```

**Eso es todo para la primera ejecución.** El lanzador:
1. Comprueba si Java 21 y Maven están disponibles.
2. Si faltan, descarga Temurin JDK 21 y/o Maven 3.9.9 dentro de `.tools/` y verifica sus hashes.
3. Ejecuta la preparación y compilación necesarias.
4. Genera el JAR si todavía no existe.
5. Inicia CachyOS Optimizer.

En los siguientes inicios, desde la carpeta del proyecto, solo ejecuta:

```bash
bash scripts/launch.sh
```

Puedes pasar opciones directamente al programa:

```bash
bash scripts/launch.sh                 # abre el menú de terminal
bash scripts/launch.sh --diagnostics   # diagnóstico directo
bash scripts/launch.sh --benchmark      # diagnóstico + microbenchmark CPU
bash scripts/launch.sh --install-tools  # instala herramientas recomendadas
bash scripts/launch.sh --optimizer      # centro de optimización
bash scripts/launch.sh --steam-options  # creador de parámetros de Steamdas (Arch/CachyOS)\nbash scripts/launch.sh --optimizer     # centro de optimización guiada
bash scripts/launch.sh --help
```

### Opción 2: usar una interfaz gráfica para clonar

En GitHub, pulsa **Code → Open with GitHub Desktop** (si tienes GitHub Desktop configurado), clona el repositorio y abre una terminal dentro de la carpeta `CachyOS-Optimizer`. Después ejecuta:

```bash
bash scripts/launch.sh
```

También puedes usar cualquier cliente Git que permita clonar el repositorio. El paso importante es ejecutar el lanzador desde la carpeta clonada.

### Requisitos del equipo

- Linux x86_64 o ARM64.
- Conexión a Internet en la primera ejecución para descargar Java/Maven si no están presentes.
- Herramientas base del sistema: `bash`, `git`, `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`.

Java 21, Maven y las dependencias Java se guardan dentro del repositorio (`.tools/` y `.cache/`); no se instalan globalmente ni se necesitan permisos de administrador. Las herramientas base de Linux y las herramientas opcionales de diagnóstico no se descargan automáticamente: el programa informa cuando alguna no está disponible.

### Compilar, probar y usar Maven manualmente (opcional)

Si ya tienes Java 21 y Maven configurados:

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
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

**CachyOS Optimizer** — entender el sistema primero; optimizar con responsabilidad después.
