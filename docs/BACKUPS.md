# Gestor de respaldos

El CLI permite respaldar archivos seleccionados explícitamente y restaurar copias verificadas en otro directorio.

## Crear respaldo

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar --backup "$HOME/x504-backups" "$HOME/.config/example.conf"
```

Se crea un directorio nuevo `backup-...` dentro del directorio indicado. El programa imprime su ruta exacta y el SHA-256 de cada archivo. Conserva esa ruta para restaurar.

## Restaurar

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar --restore "$HOME/x504-backups/backup-..." "$HOME/x504-restored"
```

La restauración:
- Carga el manifiesto del respaldo.
- Verifica SHA-256 antes y después de copiar.
- Rechaza rutas relativas que intenten salir del directorio de respaldo.
- Se niega a sobrescribir archivos existentes.
- Escribe en la carpeta destino seleccionada; no toca las rutas originales.

## Límites actuales

- Selecciona únicamente archivos que tengas permiso de leer.
- No hay copia automática de directorios completos, permisos especiales, enlaces, ACL ni atributos extendidos.
- No se restaura sobre `/etc`, `/usr` ni otras rutas originales.
- No es un mecanismo completo de rollback del sistema.
- Guarda los respaldos en un medio confiable y conserva una copia externa si los datos son importantes.

## Alcance, integridad y recuperación

El respaldo es una copia de los archivos seleccionados, no una imagen del sistema ni una instantánea de particiones. El manifiesto SHA-256 permite detectar cambios o corrupción en el contenido respaldado, pero no conserva por sí solo permisos especiales, ACL, atributos extendidos, enlaces duros ni metadatos de todos los sistemas de archivos.

Antes de restaurar, comprueba el destino y conserva una copia de cualquier archivo que vayas a reemplazar manualmente. La herramienta evita sobrescribir archivos existentes y restaura en una ubicación separada. Para recuperación de sistema, arranque, controladores o particiones utiliza las herramientas específicas de la distribución; este gestor no sustituye un plan de recuperación completo.
