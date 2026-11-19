package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Ejecutor limitado a consultas conocidas; nunca invoca un intérprete de comandos. */
public final class CommandExecutor {
    private static final Set<String> ALLOWED = Set.of(
            "uname", "lscpu", "lsblk", "lspci", "lsusb", "free", "df",
            "glxinfo", "vulkaninfo", "inxi", "systemctl"
    );

    public CommandResult executeReadOnly(List<String> arguments, Duration timeout)
            throws IOException, InterruptedException {
        if (arguments == null || arguments.isEmpty()) throw new IllegalArgumentException("Comando vacío");
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("El tiempo límite debe ser positivo");
        }
        String executable = arguments.get(0);
        if (!ALLOWED.contains(executable)) throw new SecurityException("Comando no permitido: " + executable);
        if (arguments.stream().anyMatch(arg -> arg == null || arg.contains("\u0000"))) {
            throw new IllegalArgumentException("Argumento no válido");
        }

        Process process = new ProcessBuilder(arguments).redirectErrorStream(false).start();
        CompletableFuture<String> stdout = readAsync(process.getInputStream());
        CompletableFuture<String> stderr = readAsync(process.getErrorStream());
        boolean finished = process.waitFor(Math.max(1, timeout.toMillis()), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor();
            return new CommandResult(-1, stdout.join(), stderr.join(), true);
        }
        return new CommandResult(process.exitValue(), stdout.join(), stderr.join(), false);
    }

    private CompletableFuture<String> readAsync(java.io.InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new java.util.concurrent.CompletionException(exception);
            }
        });
    }
}
