package com.x504.optimizer.monitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Monitor de terminal acotado: CPU (delta de /proc/stat), RAM y carga promedio. */
public final class SystemMonitor {
    public List<Sample> monitor(Duration duration, Duration interval) throws InterruptedException {
        if (duration == null || interval == null || duration.isNegative() || duration.isZero()
                || interval.isNegative() || interval.isZero() || duration.compareTo(Duration.ofMinutes(5)) > 0
                || interval.compareTo(Duration.ofSeconds(1)) < 0) {
            throw new IllegalArgumentException("Duración máxima 5 minutos e intervalo mínimo 1 segundo.");
        }
        List<Sample> samples = new ArrayList<>();
        long end = System.nanoTime() + duration.toNanos();
        CpuTicks previous = readCpu();
        do {
            Thread.sleep(interval.toMillis());
            CpuTicks current = readCpu();
            double cpu = previous == null || current == null ? -1 : current.percentSince(previous);
            previous = current;
            long total = value("MemTotal"), available = value("MemAvailable");
            double used = total <= 0 || available < 0 ? -1 : Math.max(0, Math.min(100, (total - available) * 100.0 / total));
            String load = readFirst(Path.of("/proc/loadavg"));
            samples.add(new Sample(Instant.now(), cpu, used, load));
        } while (System.nanoTime() < end);
        return List.copyOf(samples);
    }
    private static CpuTicks readCpu() {
        try {
            String line = Files.readAllLines(Path.of("/proc/stat")).getFirst();
            String[] p = line.trim().split("\\s+");
            long idle = Long.parseLong(p[4]) + (p.length > 5 ? Long.parseLong(p[5]) : 0);
            long total = 0;
            for (int i=1;i<p.length;i++) total += Long.parseLong(p[i]);
            return new CpuTicks(total,idle);
        } catch (Exception ex) { return null; }
    }
    private static long value(String key) {
        try {
            for(String line:Files.readAllLines(Path.of("/proc/meminfo"))) if(line.startsWith(key+":")) return Long.parseLong(line.replaceAll("[^0-9]",""));
        } catch(Exception ignored) {}
        return -1;
    }
    private static String readFirst(Path path) {
        try { return Files.readString(path).lines().findFirst().orElse("No disponible"); }
        catch(IOException ex) { return "No disponible"; }
    }
    private record CpuTicks(long total,long idle) {
        double percentSince(CpuTicks old) {
            long dt=total-old.total, di=idle-old.idle;
            return dt<=0 ? 0 : Math.max(0,Math.min(100,(dt-di)*100.0/dt));
        }
    }
    public record Sample(Instant time,double cpuPercent,double memoryPercent,String loadAverage) {}
}