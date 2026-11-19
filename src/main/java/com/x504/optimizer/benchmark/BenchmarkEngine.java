package com.x504.optimizer.benchmark;

import java.time.Instant;
import java.util.Arrays;

/**
 * Microbenchmark CPU acotado y reproducible. No instala nada ni modifica el sistema.
 * No debe interpretarse como FPS, rendimiento de GPU o rendimiento real de juegos.
 */
public final class BenchmarkEngine {
    private static volatile long resultSink;
    public static final int DEFAULT_ITERATIONS = 250_000;
    public static final int DEFAULT_RUNS = 3;

    public BenchmarkResult runCpuMicrobenchmark(int iterationsPerRun, int runs) {
        if (iterationsPerRun < 10_000 || iterationsPerRun > 2_000_000) {
            throw new IllegalArgumentException("Las iteraciones deben estar entre 10 000 y 2 000 000.");
        }
        if (runs < 1 || runs > 5) {
            throw new IllegalArgumentException("Las repeticiones deben estar entre 1 y 5.");
        }

        long[] durations = new long[runs];
        for (int run = 0; run < runs; run++) {
            long value = 0x9E3779B97F4A7C15L;
            long start = System.nanoTime();
            for (int i = 0; i < iterationsPerRun; i++) {
                value ^= (value << 13);
                value ^= (value >>> 7);
                value ^= (value << 17);
                value += i * 0x45d9f3bL;
            }
            durations[run] = Math.max(0, System.nanoTime() - start);
            resultSink = value;
        }
        long[] sorted = durations.clone();
        Arrays.sort(sorted);
        long median = sorted[sorted.length / 2];
        long totalOps = (long) iterationsPerRun * runs;
        long totalNanos = Arrays.stream(durations).sum();
        long opsPerSecond = totalNanos == 0 ? 0
                : Math.round(totalOps * 1_000_000_000.0 / totalNanos);
        return new BenchmarkResult("CPU integer microbenchmark", Instant.now(),
                iterationsPerRun, runs, sorted[0], median, sorted[sorted.length - 1],
                opsPerSecond, "Carga breve de CPU; no representa FPS ni rendimiento de GPU/juegos.");
    }

    public BenchmarkResult runDefaultCpuMicrobenchmark() {
        return runCpuMicrobenchmark(DEFAULT_ITERATIONS, DEFAULT_RUNS);
    }
}
