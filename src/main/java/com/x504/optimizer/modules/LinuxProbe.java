package com.x504.optimizer.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class LinuxProbe {
    private LinuxProbe() {}
    static String read(Path path) {
        try { return Files.isReadable(path) ? Files.readString(path).strip() : ""; }
        catch (IOException exception) { return ""; }
    }
    static List<String> lines(Path path) {
        try { return Files.isReadable(path) ? Files.readAllLines(path) : List.of(); }
        catch (IOException exception) { return List.of(); }
    }
    static String firstLine(Path path) {
        List<String> lines = lines(path);
        return lines.isEmpty() ? "" : lines.getFirst().strip();
    }
    static String unknown(String value) { return value == null || value.isBlank() ? "No disponible" : value; }
}
