package com.x504.optimizer.backup;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BackupManifestTest {
    private static final String HASH = "a".repeat(64);

    @Test
    void acceptsValidEntryAndMakesListImmutable() {
        var entry = new BackupManifest.BackupEntry("/etc/example.conf", "backup/example.conf", HASH);
        var source = new java.util.ArrayList<>(List.of(entry));
        var manifest = new BackupManifest(Instant.EPOCH, source);
        source.clear();
        assertEquals(1, manifest.entries().size());
        assertThrows(UnsupportedOperationException.class, () -> manifest.entries().clear());
    }

    @Test
    void rejectsMalformedChecksum() {
        assertThrows(IllegalArgumentException.class,
                () -> new BackupManifest.BackupEntry("/etc/example.conf", "backup/example.conf", "bad"));
    }

    @Test
    void rejectsDuplicateOriginalPaths() {
        var first = new BackupManifest.BackupEntry("/etc/example.conf", "backup/one", HASH);
        var second = new BackupManifest.BackupEntry("/etc/example.conf", "backup/two", HASH);
        assertThrows(IllegalArgumentException.class,
                () -> new BackupManifest(Instant.EPOCH, List.of(first, second)));
    }

    @Test
    void rejectsEmptyPaths() {
        assertThrows(IllegalArgumentException.class,
                () -> new BackupManifest.BackupEntry(" ", "backup/file", HASH));
    }
}
