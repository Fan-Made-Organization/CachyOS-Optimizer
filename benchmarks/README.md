# Benchmarks

El proyecto incluye un microbenchmark CPU acotado accesible con:

```bash
java -jar target/cachyos-optimizer-0.1.0-SNAPSHOT.jar --benchmark
```

La prueba ejecuta una carga aritmética pequeña y repetida, informa mediana, mínimo, máximo y operaciones aproximadas por segundo. El número de iteraciones y repeticiones está limitado para evitar cargas arbitrarias.

**Límites importantes:**
- No mide FPS, GPU, latencia de juegos ni rendimiento de almacenamiento.
- Los resultados dependen de temperatura, frecuencia, procesos en segundo plano y JVM.
- Compara resultados únicamente bajo condiciones similares.
- No debe usarse como puntuación universal ni como promesa de mejora.
- El benchmark no modifica configuración del sistema.
