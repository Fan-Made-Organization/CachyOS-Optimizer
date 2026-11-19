package com.x504.optimizer;

import com.x504.optimizer.advisor.OptimizationAdvisor;
import com.x504.optimizer.audit.AuditLogger;
import com.x504.optimizer.backup.BackupManager;
import com.x504.optimizer.benchmark.BenchmarkEngine;
import com.x504.optimizer.benchmark.BenchmarkSuite;
import com.x504.optimizer.benchmark.BenchmarkHistory;
import com.x504.optimizer.core.SystemCore;
import com.x504.optimizer.monitor.SystemMonitor;
import com.x504.optimizer.install.OptimizationToolsInstaller;
import com.x504.optimizer.optimize.OptimizationCenter;
import com.x504.optimizer.compatibility.CompatibilityAnalyzer;
import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.modules.*;
import com.x504.optimizer.profiles.ProfileCatalog;
import com.x504.optimizer.reports.ReportExporter;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.SystemAnalysisDashboard;
import com.x504.optimizer.system.SystemInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Punto de entrada de CachyOS Optimizer. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--menu")) { runTerminalMenu(); return; }
        if (args.length > 0 && (args[0].equals("--diagnostics") || args[0].equals("--benchmark") || args[0].equals("--report-json") || args[0].equals("--report-html"))) { runDiagnostics(args); return; }
        if (args.length > 0 && args[0].equals("--monitor")) { runMonitor(args); return; }
        if (args.length > 0 && (args[0].equals("--benchmark-memory") || args[0].equals("--benchmark-storage"))) { runExtendedBenchmark(args); return; }
        if (args.length > 0 && args[0].equals("--benchmark-suite")) { runBenchmarkSuite(args); return; }
        if (args.length > 0 && args[0].equals("--compare-benchmarks")) { compareBenchmarks(args); return; }
        if (args.length > 0 && args[0].equals("--install-tools")) { runInstallTools(); return; }
        if (args.length > 0 && args[0].equals("--optimizer")) { runOptimizationCenter(); return; }
        if (args.length > 0 && args[0].equals("--help")) {
            printHelp();
            return;
        }
        if (args.length > 0 && args[0].equals("--backup")) {
            runBackup(args);
            return;
        }
        if (args.length > 0 && args[0].equals("--restore")) {
            runRestore(args);
            return;
        }
        printHelp();
    }

    private static void runDiagnostics(String[] args) {
        SystemInfo info = new LinuxSystemDetector().detect();
        var hardware = new HardwareDetector().detect();
        var compatibility = new CompatibilityAnalyzer().analyze(info);

        System.out.println(new SystemAnalysisDashboard().render(info, hardware, compatibility));
        System.out.println("\n=== Información detallada del sistema ===");
        System.out.println(info.toDisplayString());
        System.out.println("\n=== Hardware ===");
        System.out.println(hardware.toDisplayString());
        System.out.println("\n=== Evaluación de compatibilidad ===");
        System.out.println(compatibility.toDisplayString());

        ModuleManager manager = new ModuleManager(List.of(
                new CpuModule(), new MemoryModule(), new GpuModule(), new StorageModule(),
                new PowerModule(), new NetworkModule(), new AudioModule(), new GamingModule(), new GraphicsStackModule(),
                new HardwareIntelligenceModule(), new DriverInspectorModule(), new SystemHealthModule(),
                new PowerIntelligenceModule(), new PackageIntelligenceModule(),
                new DeviceInventoryModule(), new DriverConflictModule(),
                new KdeModule(), new SteamModule(), new ProtonModule(), new DiagnosticsModule()
        ));
        System.out.println("\n=== Diagnósticos por módulo ===");
        var snapshot = new SystemCore(manager).capture();
        var reports = snapshot.reports();
        for (DiagnosticModule.ModuleReport report : reports) {
            System.out.println("\n[" + report.title() + "]");
            report.findings().forEach(line -> System.out.println("  • " + line));
            report.warnings().forEach(line -> System.out.println("  ! " + line));
        }

        exportRequestedReports(args, reports);

        System.out.println("\n=== Recomendaciones inteligentes (solo lectura) ===");
        var recommendations = new OptimizationAdvisor().analyze(reports);
        if (recommendations.isEmpty()) {
            System.out.println("No hay recomendaciones adicionales respaldadas por los datos detectados.");
        } else {
            recommendations.forEach(item -> {
                System.out.println("\n[" + item.title() + "]");
                System.out.println("  Evidencia: " + item.evidence());
                System.out.println("  Análisis: " + item.explanation());
                System.out.println("  Beneficio esperado: " + item.expectedBenefit());
                System.out.println("  Riesgo: " + item.risk() + " | Reversible: " + (item.reversible() ? "Sí" : "No"));
                System.out.println("  Siguiente paso: " + item.nextStep());
            });
        }

        System.out.println("\n=== Perfiles disponibles ===");
        new ProfileCatalog().all().forEach(profile ->
                System.out.println("- " + profile.name() + ": " + profile.description()));

        if (Arrays.asList(args).contains("--benchmark")) {
            System.out.println("\n=== Benchmark CPU (opcional) ===");
            var result = new BenchmarkEngine().runDefaultCpuMicrobenchmark();
            System.out.printf("Prueba: %s%nRepeticiones: %d x %,d operaciones%n",
                    result.name(), result.runs(), result.iterationsPerRun());
            System.out.printf("Tiempo mediano: %.2f ms | Operaciones/segundo: %,d%n",
                    result.medianNanos() / 1_000_000.0, result.operationsPerSecond());
            System.out.println(result.note());
            writeAuditEvent("cpu_benchmark_completed", result.runs() + " repeticiones; " + result.operationsPerSecond() + " operaciones/segundo");
        }

        writeAuditEvent("diagnostic_completed",
                reports.size() + " módulos inspeccionados; " + recommendations.size() + " recomendaciones generadas");
        System.out.println("\nModo actual: diagnóstico de solo lectura.");
        System.out.println("No se aplicaron cambios de configuración al sistema.");
        System.out.println("Usa --benchmark para la prueba CPU, --backup/--restore para copias verificadas o --help.");
    }

    private static void runBenchmarkSuite(String[] args) {
        int mib = 16;
        if (args.length > 1) try { mib = Integer.parseInt(args[1]); }
        catch (NumberFormatException ex) { System.err.println("Tamaño inválido en MiB."); return; }
        try {
            var cpu = new BenchmarkEngine().runDefaultCpuMicrobenchmark();
            var suite = new BenchmarkSuite();
            var ram = suite.benchmarkMemory(mib);
            var storage = suite.benchmarkStorage(Math.min(32, Math.max(8, mib)));
            Path history = Path.of(System.getProperty("user.home"), ".local", "state", "cachyos-optimizer", "benchmarks.csv");
            new BenchmarkHistory().append(history, mib, cpu.operationsPerSecond(), ram, storage);
            System.out.printf("CPU: %,d ops/s | RAM lectura: %.1f MiB/s | Disco lectura: %.1f MiB/s%n", cpu.operationsPerSecond(), ram.readMiBs(), storage.readMiBs());
            System.out.println("Historial guardado en: " + history);
            System.out.println("Repite la prueba tras tus cambios y conserva una copia del CSV para comparar resultados.");
        } catch (IOException | IllegalArgumentException ex) { System.err.println("Suite incompleta: " + ex.getMessage()); }
    }

    private static void compareBenchmarks(String[] args) {
        if (args.length != 3) { System.err.println("Uso: --compare-benchmarks <antes.csv> <despues.csv>"); return; }
        try {
            var result = new BenchmarkHistory().compare(Path.of(args[1]), Path.of(args[2]));
            System.out.printf("CPU: %+.1f%% | RAM lectura: %+.1f%% | Disco lectura: %+.1f%%%n", result.cpuPercent(), result.ramReadPercent(), result.storageReadPercent());
            System.out.println("Antes: " + result.beforeTimestamp() + " | Después: " + result.afterTimestamp());
            System.out.println("Compara el mismo equipo y condiciones similares.");
        } catch (IOException | RuntimeException ex) { System.err.println("No se pudo comparar: " + ex.getMessage()); }
    }
    private static void runInstallTools() {
        try (var scanner = new java.util.Scanner(System.in)) { runInstallTools(scanner); }
    }

    private static void runInstallTools(java.util.Scanner scanner) {
        OptimizationToolsInstaller installer = new OptimizationToolsInstaller();
        if (!installer.isArchBased()) {
            System.err.println("Instalación cancelada: esta función está limitada a Arch Linux y CachyOS.");
            return;
        }
        System.out.println("\n=== Instalador de herramientas de optimización ===");
        System.out.println("Se instalarán herramientas de diagnóstico, gráficos y gaming:");
        installer.packages(true).forEach(pkg -> System.out.println("  • " + pkg));
        System.out.println("\nNo se instalarán controladores propietarios ni se cambiarán kernel, servicios,");
        System.out.println("gobernadores, parámetros de arranque o configuraciones del sistema.");
        System.out.println("Pacman mostrará su resumen y pedirá confirmación antes de instalar.");
        System.out.print("\n¿Continuar? [s/N]: ");
        if (!scanner.hasNextLine() || !scanner.nextLine().strip().equalsIgnoreCase("s")) {
            System.out.println("Instalación cancelada por el usuario.");
            return;
        }
        try {
            System.out.println("\nIniciando instalador. Introduce tu contraseña de sudo si se solicita.");
            int exit = installer.install(true);
            if (exit == 0) {
                System.out.println("Herramientas instaladas o ya presentes.");
                writeAuditEvent("optimization_tools_installed", "Paquete completo de herramientas de diagnóstico y gaming");
            } else {
                System.err.println("Pacman terminó con código " + exit + ". Revisa el mensaje anterior.");
                writeAuditEvent("optimization_tools_install_failed", "Código de salida " + exit);
            }
        } catch (java.io.IOException ex) {
            System.err.println("No se pudo iniciar Pacman: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.err.println("Instalación interrumpida.");
        } catch (IllegalStateException ex) {
            System.err.println("No se pudo instalar: " + ex.getMessage());
        }
    }

    private static void runOptimizationCenter() {
        try (var scanner = new java.util.Scanner(System.in)) { runOptimizationCenter(scanner); }
    }

    private static void runOptimizationCenter(java.util.Scanner scanner) {
        OptimizationCenter center = new OptimizationCenter();
        while (true) {
            System.out.println("\n=== CENTRO DE OPTIMIZACIÓN ===");
            center.categoryStatus().forEach(System.out::println);
            System.out.println("\n1) CPU: perfiles de energía");
            System.out.println("2) RAM: revisar memoria y zram");
            System.out.println("3) GPU: revisar controlador y herramientas");
            System.out.println("4) Gaming: instalar herramientas");
            System.out.println("5) Almacenamiento: activar TRIM programado");
            System.out.println("6) Kernel: información y compatibilidad");
            System.out.println("0) Volver");
            System.out.print("Selecciona una categoría: ");
            if (!scanner.hasNextLine()) return;
            String choice = scanner.nextLine().strip();
            try {
                switch (choice) {
                    case "1" -> runCpuOptimization(center, scanner);
                    case "2" -> {
                        System.out.println("\n=== MEMORIA ===");
                        System.out.println("zram: " + (java.nio.file.Files.exists(Path.of("/sys/block/zram0")) ? "detectado" : "no detectado"));
                        System.out.println("No se borrarán cachés ni se finalizarán procesos automáticamente.");
                        System.out.println("Si zram no está configurado, el instalador de herramientas incluye zram-generator; su activación requiere una configuración revisada y reinicio.");
                    }
                    case "3" -> {
                        System.out.println("\n=== GPU ===");
                        System.out.println("Se conserva el controlador actual. No se instalarán controladores propietarios sin identificar modelo, kernel y sesión gráfica.");
                        System.out.println("Ejecuta la opción 1 (diagnóstico) para consultar GPU, Vulkan, OpenGL y módulos asociados.");
                    }
                    case "4" -> runInstallTools(scanner);
                    case "5" -> runStorageOptimization(center, scanner);
                    case "6" -> {
                        System.out.println("\nKernel activo: " + System.getProperty("os.version"));
                        System.out.println("No se sustituye ni elimina el kernel activo automáticamente.");
                        System.out.println("Primero se verifica compatibilidad de GPU, initramfs, módulos y entradas de arranque.");
                    }
                    case "0" -> { return; }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (java.io.IOException ex) {
                System.err.println("No se pudo completar la operación: " + ex.getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                System.err.println("Operación interrumpida.");
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.err.println("Operación no disponible: " + ex.getMessage());
            }
        }
    }

    private static void runCpuOptimization(OptimizationCenter center, java.util.Scanner scanner)
            throws java.io.IOException, InterruptedException {
        System.out.println("\nPerfil actual: " + center.currentPowerProfile());
        System.out.println("1) Equilibrado (recomendado para uso diario)");
        System.out.println("2) Rendimiento (más consumo y temperatura potencial)");
        System.out.println("3) Ahorro de energía");
        System.out.println("4) Restaurar perfil anterior guardado");
        System.out.print("Elige: ");
        if (!scanner.hasNextLine()) return;
        String selected = scanner.nextLine().strip();
        if (selected.equals("4")) {
            if (!center.canRestorePowerProfile()) { System.out.println("No hay un perfil anterior guardado."); return; }
            System.out.print("¿Restaurar el perfil anterior? [s/N]: ");
            if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
                center.restorePowerProfile();
                System.out.println("Perfil anterior restaurado.");
            }
            return;
        }
        String profile = switch (selected) {
            case "1" -> "balanced";
            case "2" -> "performance";
            case "3" -> "power-saver";
            default -> null;
        };
        if (profile == null) { System.out.println("Selección no válida."); return; }
        System.out.print("¿Aplicar " + profile + "? Se guardará el perfil anterior para poder revertirlo. [s/N]: ");
        if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
            center.applyPowerProfile(profile);
            writeAuditEvent("power_profile_changed", "Perfil aplicado: " + profile);
            System.out.println("Perfil aplicado. Puedes revertirlo desde esta misma opción.");
        }
    }

    private static void runStorageOptimization(OptimizationCenter center, java.util.Scanner scanner)
            throws java.io.IOException, InterruptedException {
        if (!center.hasSolidStateDrive()) {
            System.out.println("No se detectó SSD/NVMe. No se activará TRIM automáticamente.");
            return;
        }
        System.out.println("Se detectó almacenamiento no rotacional. TRIM programado usa fstrim.timer.");
        System.out.print("¿Activar el temporizador de TRIM? Requiere sudo y es reversible. [s/N]: ");
        if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
            int exit = center.enableTrimTimer();
            if (exit == 0) {
                System.out.println("fstrim.timer habilitado.");
                writeAuditEvent("trim_timer_enabled", "Temporizador TRIM habilitado");
            } else System.err.println("systemctl terminó con código " + exit);
        }
    }

    private static void runMonitor(String[] args) {
        int seconds = 15;
        if (args.length > 1) {
            try { seconds = Integer.parseInt(args[1]); }
            catch (NumberFormatException ex) { System.err.println("Duración inválida. Usa --monitor 15."); return; }
        }
        try {
            System.out.println("Monitor en vivo; duración máxima: 300 segundos.");
            for (var sample : new SystemMonitor().monitor(java.time.Duration.ofSeconds(seconds), java.time.Duration.ofSeconds(1))) {
                System.out.printf("%s | CPU: %s | RAM: %s | Carga: %s%n", sample.time(),
                    sample.cpuPercent() < 0 ? "N/D" : String.format("%.1f%%", sample.cpuPercent()),
                    sample.memoryPercent() < 0 ? "N/D" : String.format("%.1f%%", sample.memoryPercent()), sample.loadAverage());
            }
        } catch (InterruptedException ex) { Thread.currentThread().interrupt(); System.out.println("Monitor interrumpido."); }
        catch (IllegalArgumentException ex) { System.err.println(ex.getMessage()); }
    }

    private static void runExtendedBenchmark(String[] args) {
        int mib = 16;
        if (args.length > 1) try { mib = Integer.parseInt(args[1]); }
        catch (NumberFormatException ex) { System.err.println("Tamaño inválido en MiB."); return; }
        BenchmarkSuite suite = new BenchmarkSuite();
        try {
            if (args[0].equals("--benchmark-memory")) {
                var r = suite.benchmarkMemory(mib);
                System.out.printf("RAM sintética (%d MiB): escritura %.1f MiB/s; lectura %.1f MiB/s%n", r.mebibytes(), r.writeMiBs(), r.readMiBs());
                System.out.println(r.note());
            } else {
                var r = suite.benchmarkStorage(mib);
                System.out.printf("Almacenamiento temporal (%d MiB): escritura %.1f MiB/s; lectura %.1f MiB/s%n", r.mebibytes(), r.writeMiBs(), r.readMiBs());
                System.out.println(r.note());
            }
        } catch (IOException | IllegalArgumentException ex) { System.err.println("Benchmark no completado: " + ex.getMessage()); }
    }
    private static void exportRequestedReports(String[] args, List<DiagnosticModule.ModuleReport> reports) {
        for (int i = 0; i < args.length - 1; i++) {
            if (!args[i].equals("--report-json") && !args[i].equals("--report-html")) continue;
            try {
                Path destination = Path.of(args[i + 1]);
                ReportExporter exporter = new ReportExporter();
                Path written = args[i].equals("--report-json")
                        ? exporter.exportJson(reports, destination)
                        : exporter.exportHtml(reports, destination);
                System.out.println("Informe exportado: " + written);
            } catch (IOException | RuntimeException exception) {
                System.err.println("No se pudo exportar el informe: " + exception.getMessage());
            }
            i++;
        }
    }
    private static void runTerminalMenu() {
        try (var scanner = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("╔══════════════════════════════════════════╗");
                System.out.println("║       CACHYOS-OPTIMIZER             ║");
                System.out.println("║             TERMINAL                    ║");
                System.out.println("╠══════════════════════════════════════════╣");
                System.out.println("║  1) Analizar y diagnosticar el sistema   ║");
                System.out.println("║  2) Diagnóstico + benchmark CPU          ║");
                System.out.println("║  3) Mostrar ayuda y comandos             ║");
                System.out.println("║  4) Monitor CPU/RAM (15 segundos)        ║");
                System.out.println("║  5) Benchmark completo                   ║");
                System.out.println("║  6) Instalar herramientas de optimización ║
                System.out.println("║  7) Centro de optimización               ║");");
                System.out.println("║  0) Salir                                ║");
                System.out.println("╚══════════════════════════════════════════╝");
                System.out.print("Selecciona una opción: ");
                if (!scanner.hasNextLine()) return;
                String choice = scanner.nextLine().strip();
                switch (choice) {
                    case "1" -> runDiagnostics(new String[]{"--diagnostics"});
                    case "2" -> runDiagnostics(new String[]{"--benchmark"});
                    case "3" -> printHelp();
                    case "4" -> runMonitor(new String[]{"--monitor", "15"});
                    case "5" -> runBenchmarkSuite(new String[]{"--benchmark-suite"});
                    case "6" -> runInstallTools(scanner);
                    case "7" -> runOptimizationCenter(scanner);
                    case "0" -> { System.out.println("Hasta luego."); return; }
                    default -> System.out.println("Opción no válida. Elige 1, 2, 3, 4, 5, 6, 7 o 0.");
                }
            }
        }
    }
    private static void runBackup(String[] args) {
        if (args.length < 3) {
            System.err.println("Uso: --backup <directorio-respaldo> <archivo1> [archivo2 ...]");
            return;
        }
        try {
            var snapshot = new BackupManager().createBackup(
                    Arrays.stream(args, 2, args.length).map(Path::of).toList(), Path.of(args[1]));
            System.out.println("Respaldo creado correctamente: " + snapshot.directory());
            snapshot.manifest().entries().forEach(entry ->
                    System.out.println("- " + Path.of(entry.originalPath()).getFileName()
                            + " | SHA-256: " + entry.sha256()));
            writeAuditEvent("backup_created", snapshot.manifest().entries().size() + " archivos respaldados");
            System.out.println("Para restaurar, usa --restore <directorio-copia> <directorio-destino>.");
        } catch (IOException | RuntimeException exception) {
            System.err.println("No se pudo crear el respaldo: " + exception.getMessage());
        }
    }

    private static void runRestore(String[] args) {
        if (args.length != 3) {
            System.err.println("Uso: --restore <directorio-copia> <directorio-destino>");
            return;
        }
        try {
            BackupManager manager = new BackupManager();
            Path snapshot = Path.of(args[1]);
            var manifest = manager.loadManifest(snapshot);
            var restored = manager.restoreTo(snapshot, manifest, Path.of(args[2]));
            System.out.println("Restauración verificada en: " + Path.of(args[2]).toAbsolutePath().normalize());
            restored.forEach(path -> System.out.println("- " + path.getFileName()));
            writeAuditEvent("backup_restored", restored.size() + " archivos restaurados en una carpeta elegida");
        } catch (IOException | RuntimeException exception) {
            System.err.println("No se pudo restaurar el respaldo: " + exception.getMessage());
        }
    }

    private static void writeAuditEvent(String event, String details) {
        String configuredStateHome = System.getenv("XDG_STATE_HOME");
        Path stateHome = configuredStateHome != null && !configuredStateHome.isBlank()
                && Path.of(configuredStateHome).isAbsolute()
                ? Path.of(configuredStateHome)
                : Path.of(System.getProperty("user.home"), ".local", "state");
        Path log = stateHome.resolve("x504-cachyos-optimizer").resolve("audit.log");
        try {
            new AuditLogger(log).record(event, details);
        } catch (IOException exception) {
            System.err.println("Aviso: no se pudo guardar el registro local de auditoría: " + exception.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
                CachyOS Optimizer
                Uso: java -jar cachyos-optimizer.jar [opciones]

                Opciones:
                  --menu        Abre el menú interactivo de terminal.
                  --diagnostics Ejecuta el análisis completo directamente.
                  --report-json ARCHIVO  Exporta el diagnóstico a JSON.
                  --report-html ARCHIVO  Exporta el diagnóstico a HTML.
                  --help       Muestra esta ayuda.
                  --benchmark  Ejecuta un microbenchmark breve de CPU.
                  --benchmark-memory [MiB]  Prueba RAM sintética (8-64 MiB).
                  --benchmark-storage [MiB] Prueba temporal de disco (8-32 MiB).
                  --monitor [segundos]      Monitor CPU/RAM (1-300 s).
                  --benchmark-suite [MiB]   CPU + RAM + disco; guarda historial.
                  --compare-benchmarks A B  Compara dos CSV (antes/después).\n                  --install-tools          Instala herramientas recomendadas en Arch/CachyOS.\n                  --optimizer              Abre el centro de optimización guiada.
                  --backup DIR ARCHIVO... 
                               Respalda archivos elegidos en un directorio nuevo.
                  --restore COPIA DESTINO
                               Verifica SHA-256 y restaura copias en DESTINO,
                               sin sobrescribir archivos existentes.

                El análisis normal es de solo lectura. --install-tools instala solo herramientas aprobadas; no cambia ajustes del sistema.
                La restauración nunca escribe directamente en las rutas originales.
                """);
    }
}
