package com.x504.optimizer.cleaner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Limpieza conservadora: solo cachés regenerables explícitamente permitidas bajo HOME. */
public final class SystemCleaner {
    private static final List<String> ALLOWED = List.of("thumbnails", "fontconfig");
    private final Path home;
    private final Duration minimumAge;

    public SystemCleaner() { this(Path.of(System.getProperty("user.home")), Duration.ofDays(30)); }
    public SystemCleaner(Path home, Duration minimumAge) {
        this.home = home.toAbsolutePath().normalize();
        if (minimumAge == null || minimumAge.isNegative() || minimumAge.isZero()) throw new IllegalArgumentException("La antigüedad mínima debe ser positiva.");
        this.minimumAge = minimumAge;
    }

    public List<Entry> preview() throws IOException {
        Path cache = home.resolve(".cache").normalize();
        if (!cache.startsWith(home) || Files.isSymbolicLink(cache) || !Files.isDirectory(cache, LinkOption.NOFOLLOW_LINKS)) return List.of();
        Instant cutoff = Instant.now().minus(minimumAge);
        List<Entry> entries = new ArrayList<>();
        for (String name : ALLOWED) {
            Path root = cache.resolve(name);
            if (Files.isSymbolicLink(root) || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) continue;
            long bytes = 0; int files = 0;
            try (var walk = Files.walk(root)) {
                for (Path path : walk.filter(p -> !Files.isSymbolicLink(p) && Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).toList()) {
                    if (Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant().isBefore(cutoff)) {
                        bytes = Math.addExact(bytes, Files.size(path)); files++;
                    }
                }
            }
            if (files > 0) entries.add(new Entry(name, root, files, bytes));
        }
        return List.copyOf(entries);
    }

    /** Borra únicamente archivos antiguos encontrados en una vista previa y vuelve a validar rutas. */
    public int clean(String category) throws IOException {
        if (!ALLOWED.contains(category)) throw new IllegalArgumentException("Categoría no permitida.");
        Path cache = home.resolve(".cache").normalize();
        Path root = cache.resolve(category).normalize();
        if (!root.startsWith(cache) || Files.isSymbolicLink(cache) || Files.isSymbolicLink(root)
                || !Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return 0;
        Instant cutoff = Instant.now().minus(minimumAge); int deleted = 0;
        try (var walk = Files.walk(root)) {
            List<Path> paths = walk.filter(p -> !Files.isSymbolicLink(p)).sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                        && Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant().isBefore(cutoff)) {
                    Files.deleteIfExists(path); deleted++;
                }
            }
            for (Path path : paths) {
                if (!path.equals(root) && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    try (var children = Files.list(path)) { if (children.findAny().isEmpty()) Files.deleteIfExists(path); }
                }
            }
        }
        return deleted;
    }

    public record Entry(String category, Path directory, int files, long bytes) {}
}