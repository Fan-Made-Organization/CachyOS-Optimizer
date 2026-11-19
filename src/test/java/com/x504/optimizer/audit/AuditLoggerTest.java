package com.x504.optimizer.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {
    @TempDir Path temp;

    @Test
    void appendsSanitizedEvents() throws Exception {
        Path log = temp.resolve("nested/audit.log");
        AuditLogger logger = new AuditLogger(log);
        logger.record("diagnostic\ncomplete", "12 modules\tchecked");
        logger.record("recommendations", "none");
        String content = Files.readString(log);
        assertTrue(content.contains("diagnostic complete"));
        assertTrue(content.contains("12 modules checked"));
        assertTrue(content.contains("recommendations"));
        assertEquals(2, content.lines().count());
    }
}
