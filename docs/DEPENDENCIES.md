# Preparación automática del entorno

En Linux, desde la raíz del repositorio:

```bash
bash scripts/bootstrap.sh
```

El script comprueba si hay un JDK 21 disponible. Si falta, descarga Temurin JDK 21 para Linux x86_64 o ARM64 desde la API oficial de Adoptium, verifica SHA-256 y lo extrae en `.tools/temurin-jdk-21/`. No instala Java en todo el sistema ni necesita privilegios de administrador.

Si Maven falta, descarga Maven 3.9.9 desde el archivo oficial de Apache y verifica su SHA-512 antes de extraerlo en `.tools/`. Después descarga las dependencias Maven en `.cache/maven-repository/` y ejecuta `clean verify`.

## Herramientas base

Se requieren `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`. Son herramientas de preparación; el script no intenta instalar paquetes del sistema ni ejecutar comandos de administrador.

Los directorios `.tools/`, `.cache/` y `target/` están excluidos de Git. Las herramientas y dependencias se descargan dentro de la copia local del repositorio, pero no se suben al repositorio remoto ni se mezclan con el código fuente.

## Herramientas opcionales

Herramientas como `vulkaninfo`, `glxinfo` e `inxi` no son necesarias para compilar. Si faltan, los diagnósticos lo informan; no se instalan controladores ni paquetes del sistema automáticamente.

## Alcance de las descargas

La preparación local instala herramientas de desarrollo dentro de `.tools/` y guarda dependencias Maven en un repositorio local. No instala controladores, paquetes del sistema, servicios ni bibliotecas gráficas globales. Las descargas requieren HTTPS y espacio temporal; las sumas de verificación ayudan a detectar archivos alterados, pero no sustituyen la revisión de fuentes oficiales. Para problemas de descarga comprueba certificados TLS, proxy, arquitectura (`x86_64` o `aarch64`) y espacio libre.
