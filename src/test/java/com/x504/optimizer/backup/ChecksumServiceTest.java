package com.x504.optimizer.backup;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumServiceTest {
    @Test
    void calculatesStableSha256() throws Exception {
        var file = Files.createTempFile("x504-checksum", ".txt");
        try {
            Files.writeString(file, "X504");
            String checksum = new ChecksumService().sha256(file);
            assertEquals(64, checksum.length());
            assertEquals(checksum, new ChecksumService().sha256(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void verifiesMatchingChecksum() throws Exception {
        var file = Files.createTempFile("x504-checksum-match", ".txt");
        try {
            Files.writeString(file, "X504");
            var service = new ChecksumService();
            assertTrue(service.matches(file, service.sha256(file).toUpperCase()));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void detectsModifiedFile() throws Exception {
        var file = Files.createTempFile("x504-checksum-change", ".txt");
        try {
            Files.writeString(file, "original");
            var service = new ChecksumService();
            String original = service.sha256(file);
            Files.writeString(file, "modified");
            assertFalse(service.matches(file, original));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void rejectsMalformedChecksum() throws Exception {
        var file = Files.createTempFile("x504-checksum-invalid", ".txt");
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> new ChecksumService().matches(file, "not-a-checksum"));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
