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
}
