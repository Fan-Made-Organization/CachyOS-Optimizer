package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inventario de interfaces, enlace y contadores acumulados de red (solo lectura). */
public final class NetworkModule implements DiagnosticModule {
    public String id() { return "network"; }
    public String name() { return "Red"; }
    public ModuleReport inspect() {
        Path interfaces = Path.of("/sys/class/net");
        List<String> findings = new ArrayList<>();
        try (var paths = Files.list(interfaces)) {
            for (Path p : paths.sorted().toList()) {
                String name = p.getFileName().toString();
                String state = read(p.resolve("operstate"));
                String carrier = read(p.resolve("carrier"));
                String speed = read(p.resolve("speed"));
                String rx = read(p.resolve("statistics/rx_bytes"));
                String tx = read(p.resolve("statistics/tx_bytes"));
                findings.add(name + " — estado: " + state + ", enlace: " + ("1".equals(carrier) ? "conectado" : "no confirmado"));
                if (!speed.isBlank() && !"-1".equals(speed)) findings.add(name + " — velocidad anunciada: " + speed + " Mb/s");
                if (!rx.isBlank() && !tx.isBlank()) findings.add(name + " — tráfico acumulado: RX " + human(Long.parseLong(rx)) + ", TX " + human(Long.parseLong(tx)));
            }
        } catch (Exception exception) { findings.add("No se pudieron detectar interfaces o leer sus contadores."); }
        return new ModuleReport(id(), name(), findings, List.of("Los contadores son acumulados desde el último reinicio/interfaz; no representan velocidad instantánea, latencia ni pérdida de paquetes."));
    }
    private static String read(Path path) { try { return Files.readString(path).strip(); } catch (Exception ex) { return ""; } }
    private static String human(long bytes) { return String.format("%.1f MiB", bytes / 1048576.0); }
}