package com.x504.optimizer.system;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Ejecutor limitado a herramientas de consulta; nunca invoca un intérprete de comandos.
 * La salida se limita en memoria y el proceso tiene un tiempo máximo de ejecución.
 */
public final class CommandExecutor {
    private static final Set<String> ALLOWED = Set.of(
            "uname", "lscpu", "lsblk", "lspci", "lsusb", "free", "df",
            "glxinfo", "vulkaninfo", "vainfo", "vdpauinfo", "inxi"
    );
    private static final int MAX_OUTPUT_BYTES = 1024 * 1024;
    private static final String TRUNCATED_MARKER = "\n[X504: salida truncada al alcanzar el límite de 1 MiB]\n";

    public CommandResult executeReadOnly(List<String> arguments, Duration timeout)
            throws IOException, InterruptedException {
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("Comando vacío");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("El tiempo límite debe ser positivo");
        }
        if (arguments.stream().anyMatch(arg -> arg == null || arg.contains("\u0000"))) {
            throw new IllegalArgumentException("Argumento no válido");
        }
        String executable = arguments.getFirst();
        if (executable.isBlank() || !ALLOWED.contains(executable)) {
            throw new SecurityException("Comando no permitido: " + executable);
        }

        Process process = new ProcessBuilder(List.copyOf(arguments))
                .redirectErrorStream(false)
                .start();
        CompletableFuture<String> stdout = readBoundedAsync(process.getInputStream());
        CompletableFuture<String> stderr = readBoundedAsync(process.getErrorStream());
        boolean finished = process.waitFor(Math.max(1, timeout.toMillis()), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor();
            return new CommandResult(-1, stdout.join(), stderr.join(), true);
        }
        return new CommandResult(process.exitValue(), stdout.join(), stderr.join(), false);
    }

    private CompletableFuture<String> readBoundedAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream; var output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                int retained = 0;
                boolean truncated = false;
                while ((read = stream.read(buffer)) != -1) {
                    int writable = Math.min(read, MAX_OUTPUT_BYTES - retained);
                    if (writable > 0) {
                        output.write(buffer, 0, writable);
                        retained += writable;
                    }
                    if (writable < read) truncated = true;
                    // Se continúa leyendo para evitar bloquear al proceso por una tubería llena.
                }
                String result = output.toString(StandardCharsets.UTF_8);
                return truncated ? result + TRUNCATED_MARKER : result;
            } catch (IOException exception) {
                throw new java.util.concurrent.CompletionException(exception);
            }
        });
    }
}
