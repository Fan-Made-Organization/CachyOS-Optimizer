package com.x504.optimizer.cleaner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.nio.file.attribute.FileTime;
import static org.junit.jupiter.api.Assertions.*;

class SystemCleanerTest {
    @TempDir Path home;
    @Test void previewOnlyListsOldFilesInAllowlistedCacheFolders() throws Exception {
        Path old = Files.createDirectories(home.resolve(".cache/thumbnails")).resolve("old.png");
        Files.writeString(old, "thumbnail");
        Files.setLastModifiedTime(old, FileTime.from(Instant.now().minus(Duration.ofDays(60))));
        Path shader = Files.createDirectories(home.resolve(".cache/mesa_shader_cache")).resolve("keep");
        Files.writeString(shader, "shader");
        var result = new SystemCleaner(home, Duration.ofDays(30)).preview();
        assertEquals(1, result.size());
        assertEquals("thumbnails", result.getFirst().category());
        assertTrue(Files.exists(old));
        assertTrue(Files.exists(shader));
    }
    @Test void cleanDeletesOnlyOldFilesAndRejectsUnknownCategories() throws Exception {
        Path dir = Files.createDirectories(home.resolve(".cache/fontconfig"));
        Path old = dir.resolve("old.cache"); Files.writeString(old, "old");
        Files.setLastModifiedTime(old, FileTime.from(Instant.now().minus(Duration.ofDays(60))));
        Path fresh = dir.resolve("fresh.cache"); Files.writeString(fresh, "fresh");
        var cleaner = new SystemCleaner(home, Duration.ofDays(30));
        assertEquals(1, cleaner.clean("fontconfig"));
        assertFalse(Files.exists(old)); assertTrue(Files.exists(fresh));
        assertThrows(IllegalArgumentException.class, () -> cleaner.clean("mesa_shader_cache"));
    }
}