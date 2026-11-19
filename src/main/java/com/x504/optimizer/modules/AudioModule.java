package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Detecta dispositivos ALSA expuestos por el kernel. */
public final class AudioModule implements DiagnosticModule {
    public String id() { return "audio"; }
    public String name() { return "Audio"; }
    public ModuleReport inspect() {
        Path cards = Path.of("/proc/asound/cards");
        String data = LinuxProbe.read(cards);
        List<String> findings = data.isBlank() ? List.of("No se detectaron tarjetas ALSA.") :
                data.lines().filter(line -> !line.isBlank()).toList();
        String pipewire = Files.exists(Path.of("/run/user", System.getProperty("user.name", ""), "pipewire-0"))
                ? "Socket PipeWire detectado" : "Estado PipeWire no confirmado por socket";
        return new ModuleReport(id(), name(), findings, List.of(pipewire));
    }
}
