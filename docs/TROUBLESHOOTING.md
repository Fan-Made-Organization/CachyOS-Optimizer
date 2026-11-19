# Solución de problemas

- **JDK no encontrado:** instala JDK 21 desde los repositorios oficiales de tu distribución y verifica `java -version` y `javac -version`.
- **Maven no se descarga:** comprueba conexión, certificados TLS, espacio disponible y herramientas `curl` y `sha512sum`.
- **Datos desconocidos:** algunos controladores, contenedores o permisos pueden ocultar interfaces de `/proc` o `/sys`. Desconocido no significa incompatible.
- **GPU no detectada:** comprueba `/sys/class/drm`, el controlador activo y los registros del kernel antes de cambiar drivers.
- **KDE no detectado:** verifica `XDG_CURRENT_DESKTOP` y `DESKTOP_SESSION`.
- **Benchmark variable:** cierra cargas pesadas y repite en condiciones térmicas similares. No compares equipos distintos como si fuera una puntuación universal.
- **Respaldo no restaurable:** el gestor verifica SHA-256 y se niega a restaurar datos alterados. Elige una carpeta de destino donde no existan archivos con el mismo nombre.

No ejecutes el programa como root para solucionar problemas de diagnóstico.
