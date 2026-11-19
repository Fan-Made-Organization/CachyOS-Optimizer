package com.x504.optimizer.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BackupManagerTest {
    @TempDir Path temp;

    @Test
    void createsLoadsAndRestoresVerifiedBackupToChosenDirectory() throws Exception {
        Path source = temp.resolve("settings.conf");
        Files.writeString(source, "safe=true\n");
        Path backupRoot = temp.resolve("backups");
        BackupManager manager = new BackupManager();

        var snapshot = manager.createBackup(List.of(source), backupRoot);
        var loaded = manager.loadManifest(snapshot.directory());
        assertEquals(1, loaded.entries().size());
        assertEquals(snapshot.manifest().entries().getFirst().sha256(), loaded.entries().getFirst().sha256());

        Path restoreRoot = temp.resolve("restored");
        var restored = manager.restoreTo(snapshot.directory(), loaded, restoreRoot);
        assertEquals(1, restored.size());
        assertEquals("safe=true\n", Files.readString(restored.getFirst()));
        assertNotEquals(source, restored.getFirst());
    }

    @Test
    void refusesToRestoreTamperedBackup() throws Exception {
        Path source = temp.resolve("original.txt");
        Files.writeString(source, "original");
        BackupManager manager = new BackupManager();
        var snapshot = manager.createBackup(List.of(source), temp.resolve("backups"));
        Path stored = snapshot.directory().resolve(snapshot.manifest().entries().getFirst().backupPath());
        Files.writeString(stored, "tampered");
        assertThrows(java.io.IOException.class,
                () -> manager.restoreTo(snapshot.directory(), snapshot.manifest(), temp.resolve("restore")));
    }

    @Test
    void rejectsSymbolicLinkSources() throws Exception {
        Path source = temp.resolve("source.txt");
        Path link = temp.resolve("link.txt");
        Files.writeString(source, "data");
        try {
            Files.createSymbolicLink(link, source);
        } catch (UnsupportedOperationException | java.io.IOException | SecurityException exception) {
            return;
        }
        assertThrows(java.io.IOException.class,
                () -> new BackupManager().createBackup(List.of(link), temp.resolve("backups")));
    }
}
