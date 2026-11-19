# X504 CachyOS Optimizer

<p align="center">
  <strong>Diagnóstico, análisis de compatibilidad y preparación para optimizar Linux con seguridad.</strong><br>
  Diseñado con prioridad en CachyOS y Arch Linux.
</p>

<p align="center">
  <a href="README.md">🇪🇸 Español</a> ·
  <a href="README.en.md">🇬🇧 English</a> ·
  <a href="README.pt-BR.md">🇧🇷 Português</a>
</p>

> **Estado actual: versión temprana (0.1.0-SNAPSHOT).** El código ejecutable disponible se centra en inspección y diagnóstico de solo lectura. El proyecto incluye bases para perfiles, validación de seguridad y respaldos, pero todavía no aplica automáticamente ajustes de rendimiento. Consulta [qué está implementado y qué está planeado](#estado-real-del-proyecto).

## ¿Qué es X504 CachyOS Optimizer?

X504 es un proyecto libre y en desarrollo para reunir en una sola herramienta información del sistema, hardware, compatibilidad y componentes relacionados con el uso diario y los videojuegos en Linux.

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

La presencia de un módulo no significa que todos los controladores, dispositivos o herramientas estén instalados. Cada módulo depende de lo que el sistema permita detectar.

### 5. Catálogo de perfiles
El proyecto incluye un catálogo inicial de perfiles y descripciones para distintos enfoques de uso. En esta etapa son **referencias informativas**: seleccionarlos no cambia gobernadores, controladores, servicios ni parámetros del sistema.

### 6. Bases de seguridad y respaldos
- Clasificación de niveles de riesgo para acciones futuras.
- Validación inicial de acciones antes de considerarlas ejecutables.
- Clases base para manifiestos de respaldo y comprobación de sumas de verificación.
- Enfoque de cambios explícitos y reversibles.

Estas piezas son una base de desarrollo; todavía no representan un sistema completo de copia, restauración o reversión automática.

## Estado real del proyecto

| Funcionalidad | Estado |
|---|---|
| Detección e información básica de Linux | Implementación inicial |
| Inspección de hardware | Implementación inicial |
| Diagnósticos modulares | Implementación inicial |
| Análisis de compatibilidad | Implementación inicial |
| Catálogo de perfiles | Disponible como catálogo informativo |
| Validación de seguridad | Base implementada; requiere ampliación |
| Manifiestos y checksums de respaldo | Base implementada; no es un flujo completo de backup/restore |
| Aplicación automática de optimizaciones | **Todavía no implementada** |
| Restauración y rollback completos | **Pendiente** |
| Interfaz gráfica | **Pendiente** |
| Comparativas y benchmarks integrados | **Pendiente** |

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
- **JDK 21** (no solamente un runtime Java).
- Conexión a Internet durante la primera preparación para descargar Maven o dependencias que falten.
- Herramientas base: `curl`, `tar`, `sha512sum`, `awk` y `sed` para el script de preparación.

El script puede descargar Maven 3.9.9 en la copia local del proyecto y verificar el archivo con SHA-512. Maven descargará las dependencias Java en una caché local. **Java 21 debe instalarse previamente**; el script no instala el JDK ni ejecuta comandos de administrador. Las herramientas opcionales de diagnóstico no se instalan automáticamente.

## Instalación y ejecución

Clona el repositorio:

```bash
git clone https://github.com/Fan-Made-Organization/X504-CachyOS-Optimizer.git
cd X504-CachyOS-Optimizer
```

Prepara el entorno y ejecuta las pruebas:

```bash
bash scripts/bootstrap.sh
```

También puedes usar Maven instalado en tu sistema:

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
```

Scripts auxiliares disponibles:
- `scripts/build.sh`: compilar el proyecto.
- `scripts/test.sh`: ejecutar las pruebas.
- `scripts/package.sh`: generar el paquete JAR.
- `scripts/launch.sh`: iniciar la aplicación empaquetada.
- `scripts/clean.sh`: limpiar archivos generados.

Si un comando falla, revisa [Instalación](docs/INSTALLATION.md), [Dependencias](docs/DEPENDENCIES.md) y [Solución de problemas](docs/TROUBLESHOOTING.md).

## Seguridad: cómo pretende trabajar

1. Inspeccionar el sistema antes de sugerir cambios.
2. Explicar el propósito, beneficio esperado, riesgo y compatibilidad de cada acción.
3. Solicitar aprobación explícita antes de modificar el equipo.
4. Preparar una forma de volver atrás cuando el cambio lo permita.
5. Registrar qué se propuso y qué se ejecutó.

La aplicación no debe ejecutarse como root para realizar diagnósticos normales. No se deben usar comandos privilegiados silenciosos ni concatenar entradas del usuario dentro de órdenes de shell. Lee [Política de seguridad](docs/SECURITY.md).

## Documentación

- [Arquitectura](docs/ARCHITECTURE.md)
- [Módulos](docs/MODULES.md)
- [Compatibilidad](docs/COMPATIBILITY.md)
- [Dependencias y preparación](docs/DEPENDENCIES.md)
- [Instalación](docs/INSTALLATION.md)
- [Seguridad](docs/SECURITY.md)
- [Enfoque de optimizaciones](docs/OPTIMIZATIONS.md)
- [Desarrollo y contribución](docs/DEVELOPMENT.md)
- [Solución de problemas](docs/TROUBLESHOOTING.md)
- [Registro de cambios](docs/CHANGELOG.md)

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

**La licencia del proyecto todavía no ha sido seleccionada.** El archivo [LICENSE](LICENSE) explica esta situación. Hasta que los mantenedores incorporen una licencia explícita, no debe asumirse que el proyecto puede redistribuirse o modificarse libremente.

---

**X504 CachyOS Optimizer** — entender el sistema primero; optimizar con responsabilidad después.
