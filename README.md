# X504 CachyOS Optimizer

Optimizador y centro de diagnóstico para Linux, con prioridad en CachyOS y Arch Linux.

## Estado del proyecto

La versión inicial construye una base de diagnóstico de solo lectura. Detecta información del sistema, calcula una compatibilidad basada en datos observados y define controles de seguridad para futuras acciones.

## Requisitos

- Java 21 (JDK)
- Maven 3.9 o posterior
- Linux para la detección completa del sistema

## Compilar y ejecutar

```bash
mvn clean verify
mvn package
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar
```

## Principios de seguridad

- El diagnóstico es de solo lectura.
- No se ejecutan ajustes automáticamente.
- Cada futura optimización debe declarar riesgo, reversibilidad y privilegios necesarios.
- Los cambios del sistema requerirán aprobación explícita y respaldo cuando corresponda.
- Los comandos externos deben invocarse mediante argumentos separados, nunca concatenando órdenes de shell.

## Estructura Java

El código fuente se encuentra en `src/main/java` y los recursos en `src/main/resources`, siguiendo la estructura estándar de Maven.

## Idioma

La documentación y los mensajes de la aplicación están en español. Los nombres de archivos, paquetes, clases, métodos y variables están en inglés.
