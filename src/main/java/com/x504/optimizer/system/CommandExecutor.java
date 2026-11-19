package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Ejecutor restringido a consultas conocidas; no usa shell ni acepta cadenas arbitrarias. */
public final class CommandExecutor {
    private static final Set<String> ALLOWED = Set.of(
            "uname", " lscpu", "lsblk", "lspci", "lsusb", "free", "df",
            "glxinfo", "vulkaninfo", "inxi", "systemctl"
    );

    public CommandResult executeReadOnly(List<String> arguments, Duration timeout)
            throws IOException, InterruptedException {
        if (arguments == null || arguments.isEmpty()) throw new IllegalArgumentException("Comando vacío");
        String executable = arguments.get(0);
        if (!ALLOWED.contains(executable)) throw new SecurityException("Comando no permitido: " + executable);
        if (arguments.stream().anyMatch(arg -> arg == null || arg.contains("\u0000"))) {
            throw new IllegalArgumentException("Argumento no válido");
        }
        Process process = new ProcessBuilder(arguments).redirectErrorStream(false).start();
        boolean finished = process.waitFor(Math.max(1, timeout.toSeconds()), TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new CommandResult(-1, "", "Tiempo de espera agotado", true);
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return new CommandResult(process.exitValue(), stdout, stderr, false);
    }
}
