# Instalación y primera ejecución

## Método recomendado: clonar y ejecutar

En una terminal de Linux:

```bash
git clone https://github.com/Fan-Made-Organization/CachyOS-Optimizer.git
cd CachyOS-Optimizer
bash scripts/launch.sh
```

El lanzador es el punto de entrada recomendado. En la primera ejecución:

1. Comprueba si Java 21 y Maven están disponibles.
2. Si hace falta, descarga Temurin JDK 21 y Maven 3.9.9 dentro de `.tools/`.
3. Verifica las descargas mediante SHA-256 (JDK) y SHA-512 (Maven).
4. Ejecuta `clean verify` durante la preparación inicial.
5. Empaqueta el proyecto si todavía no existe el JAR y arranca la aplicación.

En las siguientes ejecuciones basta con:

```bash
bash scripts/launch.sh
```

Los argumentos se pasan directamente al programa:

```bash
bash scripts/launch.sh --help
bash scripts/launch.sh --benchmark
bash scripts/launch.sh --linux-support
bash scripts/launch.sh --install-tools
```

## Compatibilidad Linux

Los diagnósticos se ejecutan en Linux y la detección de distribución reconoce familias Arch, Debian, Fedora/RHEL y SUSE. El instalador de herramientas ofrece listas específicas para esas familias. La opción 10 también presenta planes de kernel/headers para esas familias y una guía de recuperación. Antes de instalar muestra el comando y exige escribir INSTALAR; bloquea la operación si no detecta un posible kernel alternativo. La detección de respaldo es heurística y debe verificarse manualmente. Nunca elimina kernels ni modifica GRUB/systemd-boot o parámetros de arranque. Distribuciones desconocidas mantienen diagnósticos de solo lectura.

## Requisitos base

- Linux x86_64 o ARM64.
- Conexión a Internet durante la primera preparación.
- `bash`, `git`, `curl`, `tar`, `sha256sum`, `sha512sum`, `awk`, `sed`, `python3` y `uname`.

Java 21, Maven y las dependencias Maven se guardan localmente en `.tools/` y `.cache/`. No se instalan globalmente ni se necesitan permisos de administrador. Las herramientas base de Linux no se autoinstalan; si falta alguna, el lanzador indica cuál debes instalar con el gestor de paquetes de tu distribución.

## Clonar con GitHub Desktop u otro cliente

También puedes clonar el repositorio desde GitHub Desktop o cualquier cliente Git. Cuando termine la clonación, abre una terminal dentro de la carpeta `CachyOS-Optimizer` y ejecuta:

```bash
bash scripts/launch.sh
```

## Compilación y pruebas manuales (opcional)

Con Java 21 y Maven ya configurados:

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
```

Scripts disponibles:
- `scripts/bootstrap.sh`: prepara dependencias locales y ejecuta las pruebas.
- `scripts/build.sh`: compila y verifica.
- `scripts/test.sh`: ejecuta las pruebas.
- `scripts/package.sh`: genera el JAR.
- `scripts/launch.sh`: prepara el entorno si hace falta y ejecuta la aplicación.
- `scripts/clean.sh`: elimina los artefactos de compilación.
