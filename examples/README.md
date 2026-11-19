# Ejemplos

## Análisis normal

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar
```

## Ayuda

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar --help
```

## Microbenchmark CPU

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar --benchmark
```

Los respaldos se consumen mediante la API Java `BackupManager`, que requiere que la aplicación o una futura interfaz reciba explícitamente los archivos seleccionados y la carpeta de respaldo/destino. No se respaldan archivos del sistema automáticamente.

## Ejecutar desde el repositorio

Para usar la versión de desarrollo sin recordar el nombre del JAR, el método recomendado es `bash scripts/launch.sh [opciones]`. El lanzador prepara el entorno, compila cuando es necesario y reenvía los argumentos a la aplicación. Consulta `bash scripts/launch.sh --help` para ver las opciones de la versión actual. Los ejemplos que ejecutan directamente el JAR son útiles después de compilar con Maven, pero el nombre del artefacto puede cambiar entre versiones.
