package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inventario de kernel activo, módulos instalados, headers y entorno de arranque. Solo lectura. */
public final class KernelModule implements DiagnosticModule {
    @Override public String id() { return "kernel"; }
    @Override public String name() { return "Kernel y arranque"; }
    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String release = System.getProperty("os.version", "Desconocido");
        findings.add("Kernel activo: " + release);
        findings.add("Arquitectura: " + System.getProperty("os.arch", "Desconocida"));
        Path modules = Path.of("/usr/lib/modules");
        if (!Files.isDirectory(modules)) modules = Path.of("/lib/modules");
        if (Files.isDirectory(modules)) {
            try (var dirs = Files.list(modules)) {
                List<String> versions = dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
                findings.add("Kernels con árbol de módulos instalado: " + (versions.isEmpty() ? "ninguno detectado" : String.join(", ", versions)));
                findings.add("Kernel activo presente en inventario: " + (versions.contains(release) ? "sí" : "no confirmado"));
            } catch (Exception ex) { warnings.add("No se pudo enumerar /usr/lib/modules."); }
        } else warnings.add("No se encontró /usr/lib/modules ni /lib/modules.");
        Path build = Path.of("/lib/modules", release, "build");
        Path source = Path.of("/lib/modules", release, "source");
        findings.add("Headers/build del kernel activo: " + (Files.exists(build) ? "detectados" : "no detectados"));
        findings.add("Fuentes del kernel activo: " + (Files.exists(source) ? "detectadas" : "no detectadas"));
        String cmdline = read(Path.of("/proc/cmdline"));
        if (!cmdline.isBlank()) findings.add("Parámetros de arranque: " + cmdline);
        Path lockdown = Path.of("/sys/kernel/security/lockdown");
        String lock = read(lockdown);
        if (!lock.isBlank()) findings.add("Kernel lockdown: " + lock);
        findings.add("UEFI: " + (Files.isDirectory(Path.of("/sys/firmware/efi")) ? "detectado" : "no detectado (o modo heredado)"));
        warnings.add("El módulo no instala, elimina ni cambia kernels, initramfs, bootloader o parámetros de arranque.");
        warnings.add("La presencia de headers/build puede variar según la distribución y el empaquetado.");
        return new ModuleReport(id(), name(), findings, warnings);
    }
    private static String read(Path path) { try { return Files.isReadable(path) ? Files.readString(path).strip() : ""; } catch(Exception ex) { return ""; } }
}