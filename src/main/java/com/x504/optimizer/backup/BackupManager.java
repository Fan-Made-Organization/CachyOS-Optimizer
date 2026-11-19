package com.x504.optimizer.backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Respaldo/restauración de archivos elegidos explícitamente por el usuario.
 * La restauración se realiza en un directorio destino elegido, nunca directamente
 * sobre las rutas originales del sistema.
 */
public final class BackupManager {
    private final ChecksumService checksums = new ChecksumService();

    public BackupSnapshot createBackup(List<Path> selectedFiles, Path backupRoot) throws IOException {
        Objects.requireNonNull(selectedFiles, "La lista de archivos es obligatoria");
        Objects.requireNonNull(backupRoot, "El directorio de respaldo es obligatorio");
        if (selectedFiles.isEmpty()) throw new IllegalArgumentException("Selecciona al menos un archivo.");

        Path root = backupRoot.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(root)) throw new SecurityException("El directorio de respaldo no puede ser un enlace simbólico.");
        Files.createDirectories(root);
        Path snapshot = root.resolve("backup-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID())
                .normalize();
        if (!snapshot.startsWith(root)) throw new SecurityException("Ruta de respaldo no válida.");
        Files.createDirectories(snapshot.resolve("files"));

        List<BackupManifest.BackupEntry> entries = new ArrayList<>();
        try {
            int index = 0;
            for (Path selected : selectedFiles) {
                Path source = Objects.requireNonNull(selected, "La ruta seleccionada no puede ser nula")
                        .toAbsolutePath().normalize();
                if (Files.isSymbolicLink(source) || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
                        || !Files.isReadable(source)) {
                    throw new IOException("Solo se permiten archivos normales legibles, sin enlaces simbólicos: " + source);
                }
                String fileName = source.getFileName() == null ? "file" : source.getFileName().toString();
                String relative = "files/" + String.format("%04d-", ++index) + fileName;
                Path destination = snapshot.resolve(relative).normalize();
                if (!destination.startsWith(snapshot)) throw new SecurityException("Ruta de destino no válida.");
                Files.copy(source, destination, LinkOption.NOFOLLOW_LINKS);
                String hash = checksums.sha256(destination);
                entries.add(new BackupManifest.BackupEntry(source.toString(), relative, hash));
            }
            BackupManifest manifest = new BackupManifest(Instant.now(), entries);
            writeManifest(snapshot.resolve("manifest.tsv"), manifest);
            return new BackupSnapshot(snapshot, manifest);
        } catch (IOException | RuntimeException exception) {
            deleteTree(snapshot);
            throw exception;
        }
    }

    public BackupManifest loadManifest(Path snapshotDirectory) throws IOException {
        Path snapshot = snapshotDirectory.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(snapshot)) throw new SecurityException("El respaldo no puede ser un enlace simbólico.");
        Path manifestPath = snapshot.resolve("manifest.tsv");
        if (!Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(manifestPath)) {
            throw new IOException("No se encontró un manifiesto de respaldo válido.");
        }
        List<String> lines = Files.readAllLines(manifestPath, StandardCharsets.UTF_8);
        if (lines.size() < 2 || !lines.getFirst().equals("X504-BACKUP-1")) {
            throw new IOException("Formato de manifiesto no compatible.");
        }
        Instant createdAt;
        try { createdAt = Instant.parse(lines.get(1)); }
        catch (RuntimeException exception) { throw new IOException("Fecha de respaldo inválida.", exception); }
        List<BackupManifest.BackupEntry> entries = new ArrayList<>();
        try {
            for (int i = 2; i < lines.size(); i++) {
                String[] fields = lines.get(i).split("\\t", -1);
                if (fields.length != 3) throw new IllegalArgumentException("Fila de manifiesto inválida.");
                String original = decode(fields[0]);
                String relative = decode(fields[1]);
                entries.add(new BackupManifest.BackupEntry(original, relative, fields[2]));
            }
            return new BackupManifest(createdAt, entries);
        } catch (RuntimeException exception) {
            throw new IOException("El manifiesto contiene datos inválidos.", exception);
        }
    }

    /** Restaura en un directorio nuevo/seleccionado sin sobrescribir archivos existentes. */
    public List<Path> restoreTo(Path snapshotDirectory, BackupManifest manifest, Path restoreRoot)
            throws IOException {
        Objects.requireNonNull(manifest, "El manifiesto es obligatorio");
        Path snapshot = snapshotDirectory.toAbsolutePath().normalize();
        Path destinationRoot = restoreRoot.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(snapshot) || Files.isSymbolicLink(destinationRoot)) {
            throw new SecurityException("No se permiten directorios raíz mediante enlaces simbólicos.");
        }
        Files.createDirectories(destinationRoot);
        List<Path> restored = new ArrayList<>();
        int index = 0;
        for (BackupManifest.BackupEntry entry : manifest.entries()) {
            Path relative = Path.of(entry.backupPath());
            if (relative.isAbsolute() || relative.normalize().startsWith("..")) {
                throw new SecurityException("El manifiesto contiene una ruta de respaldo insegura.");
            }
            Path source = snapshot.resolve(relative).normalize();
            if (!source.startsWith(snapshot) || Files.isSymbolicLink(source)
                    || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Archivo de respaldo ausente o no válido: " + entry.backupPath());
            }
            Path realSnapshot = snapshot.toRealPath();
            Path realSource = source.toRealPath();
            if (!realSource.startsWith(realSnapshot)) {
                throw new SecurityException("El archivo de respaldo apunta fuera del directorio de la copia.");
            }
            if (!checksums.matches(source, entry.sha256())) {
                throw new IOException("La verificación SHA-256 falló; no se restauró: " + entry.backupPath());
            }
            String name = Path.of(entry.originalPath()).getFileName().toString();
            Path target = destinationRoot.resolve(String.format("%04d-", ++index) + name).normalize();
            if (!target.startsWith(destinationRoot)) throw new SecurityException("Destino de restauración no válido.");
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileAlreadyExistsException(target.toString());
            }
            Path temporary = Files.createTempFile(destinationRoot, ".x504-restore-", ".tmp");
            try {
                Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
                if (!checksums.matches(temporary, entry.sha256())) {
                    throw new IOException("La copia restaurada no superó la verificación SHA-256.");
                }
                try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, target); }
                restored.add(target);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
        return List.copyOf(restored);
    }

    private void writeManifest(Path file, BackupManifest manifest) throws IOException {
        StringBuilder text = new StringBuilder("X504-BACKUP-1\n").append(manifest.createdAt()).append('\n');
        for (BackupManifest.BackupEntry entry : manifest.entries()) {
            text.append(encode(entry.originalPath())).append('\t')
                    .append(encode(entry.backupPath())).append('\t')
                    .append(entry.sha256()).append('\n');
        }
        Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public record BackupSnapshot(Path directory, BackupManifest manifest) {
        public BackupSnapshot {
            Objects.requireNonNull(directory);
            Objects.requireNonNull(manifest);
        }
    }
}
