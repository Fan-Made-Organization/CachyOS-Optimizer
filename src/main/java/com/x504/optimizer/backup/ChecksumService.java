package com.x504.optimizer.backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Calcula y verifica SHA-256 para comprobar la integridad de archivos. */
public final class ChecksumService {
    public String sha256(Path file) throws IOException {
        Objects.requireNonNull(file, "El archivo es obligatorio");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM.", exception);
        }
    }

    /** Compara el checksum esperado con el archivo sin modificarlo. */
    public boolean matches(Path file, String expectedSha256) throws IOException {
        Objects.requireNonNull(expectedSha256, "El checksum esperado es obligatorio");
        if (!expectedSha256.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException("El checksum esperado debe ser SHA-256 hexadecimal.");
        }
        byte[] expected = HexFormat.of().parseHex(expectedSha256);
        byte[] actual = HexFormat.of().parseHex(sha256(file));
        return MessageDigest.isEqual(expected, actual);
    }
}
