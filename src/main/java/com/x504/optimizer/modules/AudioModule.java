package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Detecta dispositivos ALSA y el socket de usuario de PipeWire. */
public final class AudioModule implements DiagnosticModule {
    public String id() { return "audio"; }
    public String name() { return "Audio"; }
    public ModuleReport inspect() {
        String data = LinuxProbe.read(Path.of("/proc/asound/cards"));
        List<String> findings = data.isBlank() ? List.of("No se detectaron tarjetas ALSA.") :
                data.lines().filter(line -> !line.isBlank()).toList();
        String runtime = System.getenv("XDG_RUNTIME_DIR");
        boolean pipewire = runtime != null && Files.exists(Path.of(runtime, "pipewire-0"));
        return new ModuleReport(id(), name(), findings,
                List.of(pipewire ? "Socket PipeWire detectado" : "Socket PipeWire no detectado; puede no estar activo."));
    }
}
