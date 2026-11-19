package com.x504.optimizer.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;

/** Registro local de eventos sin almacenar contraseñas ni contenido sensible. */
public final class AuditLogger {
    private final Path logFile;

    public AuditLogger(Path logFile) {
        this.logFile = Objects.requireNonNull(logFile, "La ruta del registro es obligatoria")
                .toAbsolutePath().normalize();
    }

    public synchronized void record(String event, String details) throws IOException {
        String safeEvent = sanitize(event, "evento");
        String safeDetails = sanitize(details, "sin detalles");
        Path parent = logFile.getParent();
        if (parent != null) Files.createDirectories(parent);
        String line = Instant.now() + "\t" + safeEvent + "\t" + safeDetails + System.lineSeparator();
        Files.writeString(logFile, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    public Path logFile() { return logFile; }

    private String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').strip();
    }
}
