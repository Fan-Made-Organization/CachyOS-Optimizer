# Instalación

## Requisitos

- Linux recomendado; CachyOS y Arch Linux son los objetivos principales.
- Linux x86_64 o ARM64 para descargar el JDK automáticamente.
- `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`.
- Conexión a Internet durante la preparación inicial.

## Preparar y compilar

```bash
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
bash scripts/bootstrap.sh
```

El bootstrap descarga Temurin JDK 21 si falta, verifica SHA-256 y lo guarda en `.tools/`. También descarga Maven 3.9.9 si falta y verifica SHA-512. No instala software globalmente ni ejecuta comandos privilegiados.

## Ejecutar

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
```

Para ejecutar el microbenchmark CPU opcional:

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --benchmark
```

Para respaldar archivos seleccionados y restaurarlos en una carpeta separada:

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --backup "$HOME/x504-backups" "$HOME/.config/example.conf"
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --restore "$HOME/x504-backups/backup-..." "$HOME/x504-restored"
```

La ruta exacta del directorio de respaldo aparece al crearlo. La restauración verifica SHA-256 y no sobrescribe archivos existentes. Consulta [Gestor de respaldos](BACKUPS.md).

Para ver opciones:

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --help
```

El análisis normal es de solo lectura. Las herramientas opcionales se detectan, pero no se instalan automáticamente.
