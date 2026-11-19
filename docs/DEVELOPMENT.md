# Desarrollo

## Entorno

- Java 21.
- Maven.
- JUnit 5.

## Validación

```bash
mvn clean verify
```

## Convenciones

- Código, paquetes, clases, métodos y variables en inglés.
- Documentación y mensajes para usuarios en español.
- Diagnóstico de solo lectura como valor predeterminado.
- Pruebas unitarias para toda lógica nueva.
- No inventar porcentajes ni capacidades a partir de datos ausentes.
- No concatenar entradas en comandos de shell.
- Mantener las acciones privilegiadas fuera del proceso normal de diagnóstico.

## Arquitectura

Los módulos implementan `DiagnosticModule`. `ModuleManager` recopila informes; `OptimizationAdvisor` produce recomendaciones informativas; `SafetyValidator` valida acciones propuestas, pero no las ejecuta. `BackupManager` crea respaldos de archivos seleccionados y restaura copias verificadas en una carpeta elegida. `AuditLogger` registra eventos resumidos localmente.

## Lista de verificación para contribuciones

- Mantener separadas detección, recomendación y ejecución de cambios.
- Añadir pruebas para entradas válidas, malformadas, archivos ausentes y comandos externos no disponibles.
- Usar `ProcessBuilder` con argumentos separados, tiempos de espera y captura controlada de salida.
- No asumir que `systemd`, `sudo`, `pacman` o rutas de Arch existen en todas las distribuciones.
- Documentar familias y condiciones exactas en las que una función está habilitada.
- Actualizar README y documentación específica cuando cambie el comportamiento.
- Ejecutar `mvn clean verify`, validar con `bash -n` cada script y revisar `git diff --check`.
