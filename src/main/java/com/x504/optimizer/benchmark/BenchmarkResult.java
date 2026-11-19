package com.x504.optimizer.benchmark;

import java.time.Instant;

/** Resultado descriptivo de una prueba local; no equivale a un benchmark de juegos. */
public record BenchmarkResult(String name, Instant measuredAt, int iterationsPerRun,
                              int runs, long minimumNanos, long medianNanos,
                              long maximumNanos, long operationsPerSecond,
                              String note) {
    public BenchmarkResult {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (measuredAt == null) throw new NullPointerException("La fecha es obligatoria");
        if (iterationsPerRun < 1 || runs < 1 || minimumNanos < 0
                || medianNanos < minimumNanos || maximumNanos < medianNanos) {
            throw new IllegalArgumentException("Las métricas del benchmark no son válidas");
        }
        note = note == null ? "" : note.strip();
    }
}
