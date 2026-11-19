# Instalación

## Requisitos

- Linux recomendado; CachyOS y Arch Linux son los objetivos principales.
- JDK 21.
- `curl`, `tar`, `sha512sum`, `awk` y `sed` para el bootstrap.
- Conexión a Internet durante la primera compilación.

## Preparar y compilar

```bash
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
bash scripts/bootstrap.sh
```

El bootstrap descarga Maven localmente si no existe y verifica su archivo con SHA-512. No instala Java ni ejecuta comandos privilegiados.

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

Para ver opciones:

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --help
```

El análisis normal es de solo lectura. Las herramientas opcionales se detectan, pero no se instalan automáticamente.
