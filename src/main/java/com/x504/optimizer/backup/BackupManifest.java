package com.x504.optimizer.backup;

import java.time.Instant;
import java.util.List;

public record BackupManifest(Instant createdAt, List<BackupEntry> entries) {
    public BackupManifest { entries = List.copyOf(entries); }
    public record BackupEntry(String originalPath, String backupPath, String sha256) {}
}
