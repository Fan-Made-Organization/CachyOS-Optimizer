# Preparación automática del entorno

En Linux, desde la raíz del repositorio:

```bash
bash scripts/bootstrap.sh
```

El script comprueba Java 21. Si Maven falta, descarga Maven 3.9.9 desde el archivo oficial de Apache y verifica su SHA-512 antes de extraerlo en `.tools/`. Después descarga las dependencias Maven en `.cache/maven-repository/` y ejecuta `clean verify`.

Los directorios `.tools/`, `.cache/` y `target/` están excluidos de Git. Las herramientas y dependencias se descargan dentro de la copia local del repositorio, pero no se suben al repositorio remoto ni se mezclan con el código fuente.

## Requisito Java

Java 21 debe estar instalado previamente. El script no instala un JDK ni ejecuta comandos de administrador de forma silenciosa. En CachyOS/Arch, el usuario puede instalar el JDK 21 mediante el gestor de paquetes tras revisar la operación.

Las herramientas opcionales como `vulkaninfo`, `glxinfo` e `inxi` no son necesarias para compilar. Si faltan, el diagnóstico lo informa; no instala controladores ni paquetes del sistema automáticamente.
