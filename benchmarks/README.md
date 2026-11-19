# Benchmarks

El proyecto incluye un microbenchmark CPU acotado accesible con:

```bash
java -jar target/linux-optimizer-0.1.0-SNAPSHOT.jar --benchmark
```

La prueba ejecuta una carga aritmética pequeña y repetida, informa mediana, mínimo, máximo y operaciones aproximadas por segundo. El número de iteraciones y repeticiones está limitado para evitar cargas arbitrarias.

**Límites importantes:**
- No mide FPS, GPU, latencia de juegos ni rendimiento de almacenamiento.
- Los resultados dependen de temperatura, frecuencia, procesos en segundo plano y JVM.
- Compara resultados únicamente bajo condiciones similares.
- No debe usarse como puntuación universal ni como promesa de mejora.
- El benchmark no modifica configuración del sistema.

## Interpretación y reproducibilidad

Los resultados sirven para comparar ejecuciones realizadas en el mismo equipo y bajo condiciones similares; no son una clasificación universal entre procesadores. Para obtener comparaciones útiles, cierra tareas intensivas, conecta el equipo a una fuente de energía estable cuando corresponda, registra el perfil energético y ejecuta varias rondas. Anota kernel, versión de Java, distribución, arquitectura y carga de fondo junto al CSV.

No compares directamente resultados obtenidos con distintos tamaños de prueba, versiones de Java o condiciones térmicas. El calentamiento, el escalado dinámico de frecuencia, la memoria disponible y las cachés pueden alterar el resultado. Las pruebas no son pruebas de estabilidad prolongadas ni deben utilizarse para certificar refrigeración o hardware.
