package com.x504.optimizer.modules;

import com.x504.optimizer.system.CommandExecutor;
import com.x504.optimizer.system.CommandResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspecciona la pila gráfica de usuario sin cambiar configuración:
 * Vulkan, OpenGL/Mesa, VA-API, VDPAU y componentes DXVK comunes.
 */
public final class GraphicsStackModule implements DiagnosticModule {
    private static final Duration TIMEOUT = Duration.ofSeconds(8);
    private final CommandExecutor commands;

    public GraphicsStackModule() { this(new CommandExecutor()); }
    GraphicsStackModule(CommandExecutor commands) { this.commands = commands; }

    @Override public String id() { return "graphics-stack"; }
    @Override public String name() { return "Pila gráfica: Vulkan, OpenGL y vídeo"; }

    @Override public ModuleReport inspect() {
        List<String> findings = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        inspectCommand(findings, warnings, "Vulkan", "vulkaninfo", List.of("vulkaninfo", "--summary"));
        inspectCommand(findings, warnings, "OpenGL/Mesa", "glxinfo", List.of("glxinfo", "-B"));
        inspectCommand(findings, warnings, "VA-API", "vainfo", List.of("vainfo"));
        inspectCommand(findings, warnings, "VDPAU", "vdpauinfo", List.of("vdpauinfo"));

        Path steam = Path.of(System.getProperty("user.home"), ".steam/steam");
        List<Path> roots = List.of(
                steam.resolve("steamapps/common"),
                Path.of(System.getProperty("user.home"), ".local/share/Steam/steamapps/common"),
                Path.of(System.getProperty("user.home"), ".var/app/com.valvesoftware.Steam/.local/share/Steam/steamapps/common")
        );
        List<String> dxvk = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) continue;
            try (var entries = Files.list(root)) {
                entries.filter(p -> p.getFileName().toString().toLowerCase().contains("proton"))
                        .forEach(proton -> {
                            for (String relative : List.of("files/lib/wine/dxvk",
                                    "files/lib64/wine/dxvk", "dist/lib/wine/dxvk",
                                    "dist/lib64/wine/dxvk")) {
                                if (Files.isDirectory(proton.resolve(relative))) {
                                    dxvk.add(proton.getFileName() + " (" + relative + ")");
                                }
                            }
                        });
            } catch (IOException exception) {
                warnings.add("No se pudo inspeccionar una biblioteca Steam: " + root);
            }
        }
        findings.add("DXVK: " + (dxvk.isEmpty()
                ? "No se detectaron directorios DXVK en las rutas Proton inspeccionadas."
                : String.join("; ", dxvk.stream().distinct().toList())));
        warnings.add("La presencia de DXVK en una carpeta no confirma que un juego lo esté usando; puede usar otra ruta o una versión integrada.");
        warnings.add("VA-API/VDPAU dependen del controlador, permisos y sesión gráfica. Una herramienta ausente no significa que la GPU carezca de soporte.");
        return new ModuleReport(id(), name(), findings, warnings);
    }

    private void inspectCommand(List<String> findings, List<String> warnings,
                                String label, String binary, List<String> arguments) {
        if (!onPath(binary)) {
            findings.add(label + ": herramienta " + binary + " no encontrada en PATH.");
            return;
        }
        try {
            CommandResult result = commands.executeReadOnly(arguments, TIMEOUT);
            if (result.timedOut()) {
                findings.add(label + ": consulta agotó el límite de " + TIMEOUT.toSeconds() + " segundos.");
                return;
            }
            String output = result.standardOutput().isBlank() ? result.standardError() : result.standardOutput();
            if (output.isBlank()) {
                findings.add(label + ": herramienta disponible, pero no devolvió información.");
                if (result.exitCode() != 0) warnings.add(binary + " terminó con código " + result.exitCode() + ".");
                return;
            }
            String compact = output.lines().map(String::strip).filter(line -> !line.isEmpty())
                    .limit(14).reduce((a, b) -> a + " | " + b).orElse("");
            findings.add(label + (result.successful() ? ": " : " (consulta con advertencias): ") + compact);
            if (!result.successful()) warnings.add(binary + " terminó con código " + result.exitCode() + ".");
        } catch (IOException exception) {
            findings.add(label + ": no se pudo ejecutar la herramienta.");
            warnings.add(binary + ": " + exception.getClass().getSimpleName());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            findings.add(label + ": consulta interrumpida.");
        } catch (RuntimeException exception) {
            findings.add(label + ": consulta no disponible (" + exception.getClass().getSimpleName() + ").");
        }
    }

    private static boolean onPath(String binary) {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) return false;
        for (String directory : path.split(java.io.File.pathSeparator)) {
            try {
                if (Files.isExecutable(Path.of(directory, binary))) return true;
            } catch (RuntimeException ignored) { }
        }
        return false;
    }
}
