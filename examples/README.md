# Ejemplos

## Análisis normal

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
```

## Ayuda

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --help
```

## Microbenchmark CPU

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --benchmark
```

Los respaldos se consumen mediante la API Java `BackupManager`, que requiere que la aplicación o una futura interfaz reciba explícitamente los archivos seleccionados y la carpeta de respaldo/destino. No se respaldan archivos del sistema automáticamente.
