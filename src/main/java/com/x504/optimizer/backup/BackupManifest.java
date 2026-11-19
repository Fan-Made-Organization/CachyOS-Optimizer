package com.x504.optimizer.backup;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Inventario validado de archivos incluidos en un respaldo. */
public record BackupManifest(Instant createdAt, List<BackupEntry> entries) {
    public BackupManifest {
        Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
        Objects.requireNonNull(entries, "La lista de archivos es obligatoria");
        entries = List.copyOf(entries);
        Set<String> originalPaths = new HashSet<>();
        for (BackupEntry entry : entries) {
            Objects.requireNonNull(entry, "El manifiesto no puede contener entradas nulas");
            if (!originalPaths.add(entry.originalPath())) {
                throw new IllegalArgumentException(
                        "El manifiesto contiene una ruta original duplicada: " + entry.originalPath());
            }
        }
    }

    public record BackupEntry(String originalPath, String backupPath, String sha256) {
        public BackupEntry {
            originalPath = requireText(originalPath, "La ruta original");
            backupPath = requireText(backupPath, "La ruta de respaldo");
            sha256 = requireText(sha256, "El checksum").toLowerCase();
            if (!sha256.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("El checksum debe contener exactamente 64 caracteres hexadecimales SHA-256.");
            }
        }

        private static String requireText(String value, String label) {
            Objects.requireNonNull(value, label + " es obligatoria");
            String normalized = value.strip();
            if (normalized.isEmpty()) throw new IllegalArgumentException(label + " no puede estar vacía");
            return normalized;
        }
    }
}
