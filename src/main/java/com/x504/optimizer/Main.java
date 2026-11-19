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
import com.x504.optimizer.steam.SteamLaunchOptionsBuilder;
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
        if (args.length > 0 && args[0].equals("--steam-options")) { runSteamOptions(); return; }
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
            System.out.println("5) Almacenamiento: administrar TRIM programado");
            System.out.println("6) Kernel: información y compatibilidad");
            System.out.println("7) Steam: creador de parámetros de lanzamiento");
            System.out.println("0) Volver");
            System.out.print("Selecciona una categoría: ");
            if (!scanner.hasNextLine()) return;
            String choice = scanner.nextLine().strip();
            try {
                switch (choice) {
                    case "1" -> runCpuOptimization(center, scanner);
                    case "2" -> runRamOptimization(center, scanner);
                    case "3" -> {
                        System.out.println("\n=== GPU Y CONTROLADORES ===");
                        center.gpuSummary().forEach(line -> System.out.println("• " + line));
                        System.out.println("No se instala ni reemplaza ningún controlador automáticamente.");
                        System.out.println("Intel suele usar i915/xe; AMD usa amdgpu; NVIDIA puede usar nouveau o el controlador propietario.");
                        System.out.println("La elección depende de la GPU, kernel y sesión gráfica. Revisa también Vulkan/OpenGL en el diagnóstico.");
                    }
                    case "4" -> runInstallTools(scanner);
                    case "5" -> runStorageOptimization(center, scanner);
                    case "6" -> {
                        System.out.println("\nKernel activo: " + System.getProperty("os.version"));
                        System.out.println("Kernels instalados en /usr/lib/modules:");
                        var kernels = center.installedKernels();
                        if (kernels.isEmpty()) System.out.println("  No se encontraron directorios de módulos.");
                        else kernels.forEach(kernel -> System.out.println("  • " + kernel));
                        System.out.println("No se elimina ni reemplaza ningún kernel. Conserva siempre un kernel de respaldo arrancable.");
                    }
                    case "7" -> runSteamOptions(scanner);
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

    private static void runSteamOptions() {
        try (var scanner = new java.util.Scanner(System.in)) { runSteamOptions(scanner); }
    }

    private static void runSteamOptions(java.util.Scanner scanner) {
        SteamLaunchOptionsBuilder builder = new SteamLaunchOptionsBuilder();
        System.out.println("\n=== CREADOR AVANZADO DE PARÁMETROS DE STEAM ===");
        System.out.println("1) Básico   2) GameMode   3) MangoHud   4) GameMode + MangoHud");
        System.out.println("5) Gamescope   6) Registro Proton   7) FSR Proton");
        System.out.println("8) Combinación personalizada   9) Gamescope con resolución/FPS");
        System.out.println("10) Perfiles de juego predefinidos");
        System.out.print("Elige: ");
        if (!scanner.hasNextLine()) return;
        String choice = scanner.nextLine().strip();
        SteamLaunchOptionsBuilder.Options options = null;
        SteamLaunchOptionsBuilder.AdvancedOptions advanced = null;
        switch (choice) {
            case "1" -> options = new SteamLaunchOptionsBuilder.Options(false,false,false,false,false);
            case "2" -> options = new SteamLaunchOptionsBuilder.Options(true,false,false,false,false);
            case "3" -> options = new SteamLaunchOptionsBuilder.Options(false,true,false,false,false);
            case "4" -> options = new SteamLaunchOptionsBuilder.Options(true,true,false,false,false);
            case "5" -> options = new SteamLaunchOptionsBuilder.Options(false,false,true,false,false);
            case "6" -> options = new SteamLaunchOptionsBuilder.Options(false,false,false,true,false);
            case "7" -> options = new SteamLaunchOptionsBuilder.Options(false,false,false,false,true);
            case "8" -> {
                boolean gm = askYesNo(scanner, "¿Activar GameMode?");
                boolean hud = askYesNo(scanner, "¿Activar MangoHud?");
                boolean gs = askYesNo(scanner, "¿Usar Gamescope?");
                boolean log = askYesNo(scanner, "¿Activar registro de Proton?");
                boolean fsr = askYesNo(scanner, "¿Activar FSR de Proton?");
                options = new SteamLaunchOptionsBuilder.Options(gm,hud,gs,log,fsr);
            }
            case "9" -> {
                boolean gm = askYesNo(scanner, "¿Activar GameMode?");
                boolean hud = askYesNo(scanner, "¿Activar MangoHud?");
                boolean fsr = askYesNo(scanner, "¿Activar FSR de Proton?");
                int width = readBoundedInt(scanner, "Ancho (640-7680)", 1280, 640, 7680);
                int height = readBoundedInt(scanner, "Alto (480-4320)", 720, 480, 4320);
                int hz = readBoundedInt(scanner, "Frecuencia objetivo (30-360 Hz)", 60, 30, 360);
                int strength = readBoundedInt(scanner, "Intensidad FSR (0-5)", 2, 0, 5);
                advanced = new SteamLaunchOptionsBuilder.AdvancedOptions(gm,hud,true,false,fsr,width,height,hz,strength);
            }
            case "10" -> {
                System.out.println("Perfiles: 1) Competitivo  2) Equilibrado  3) Diagnóstico  4) Escalado");
                System.out.print("Elige perfil: ");
                if (!scanner.hasNextLine()) return;
                String profile = scanner.nextLine().strip();
                advanced = switch (profile) {
                    case "1" -> new SteamLaunchOptionsBuilder.AdvancedOptions(true,true,false,false,false,1920,1080,60,2);
                    case "2" -> new SteamLaunchOptionsBuilder.AdvancedOptions(true,false,false,false,false,1920,1080,60,2);
                    case "3" -> new SteamLaunchOptionsBuilder.AdvancedOptions(false,true,false,true,false,1920,1080,60,2);
                    case "4" -> new SteamLaunchOptionsBuilder.AdvancedOptions(true,true,true,false,true,1280,720,60,2);
                    default -> null;
                };
            }
            default -> { System.out.println("Opción no válida."); return; }
        }
        if (advanced == null && options == null) { System.out.println("Perfil no válido."); return; }
        try {
            String launchOptions = advanced != null ? builder.build(advanced) : builder.build(options);
            System.out.println("\nCopia esta línea en Steam → Propiedades → General → Parámetros de lanzamiento:");
            System.out.println("\n" + launchOptions + "\n");
            System.out.println("GameMode, MangoHud y Gamescope deben estar instalados si se seleccionaron.");
            System.out.println("FSR depende de Proton/juego; resolución y Hz son objetivos de Gamescope, no garantizan FPS.");
            if (askYesNo(scanner, "¿Guardar la línea en un archivo local?")) {
                Path directory = Path.of(System.getProperty("user.home"), ".local", "share", "cachyos-optimizer");
                Path destination = directory.resolve("steam-launch-options-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
                java.nio.file.Files.createDirectories(directory);
                java.nio.file.Files.writeString(destination, launchOptions + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE_NEW, java.nio.file.StandardOpenOption.WRITE);
                System.out.println("Guardado en: " + destination);
                writeAuditEvent("steam_launch_options_created", "Se generaron parámetros Steam");
            }
        } catch (IllegalArgumentException | IOException ex) { System.err.println("No se pudo generar/guardar: " + ex.getMessage()); }
    }

    private static boolean askYesNo(java.util.Scanner scanner, String prompt) {
        System.out.print(prompt + " [s/N]: ");
        return scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s");
    }

    private static int readBoundedInt(java.util.Scanner scanner, String prompt, int fallback, int minimum, int maximum) {
        System.out.print(prompt + " [" + fallback + "]: ");
        if (!scanner.hasNextLine()) return fallback;
        String value = scanner.nextLine().strip();
        if (value.isEmpty()) return fallback;
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < minimum || parsed > maximum) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException ex) {
            System.out.println("Valor inválido; se usará " + fallback + ".");
            return fallback;
        }
    }
    private static void runCpuOptimization(OptimizationCenter center, java.util.Scanner scanner)
            throws java.io.IOException, InterruptedException {
        try {
            System.out.println("\nPerfil de energía actual: " + center.currentPowerProfile());
        } catch (java.io.IOException ex) {
            System.out.println("\nPerfil de energía no disponible: " + ex.getMessage());
        }
        System.out.println("1) Equilibrado (recomendado para uso diario)");
        System.out.println("2) Rendimiento (más consumo y temperatura potencial)");
        System.out.println("3) Ahorro de energía");
        System.out.println("4) Consultar/cambiar gobernador CPU");
        System.out.println("5) Restaurar gobernador anterior");
        System.out.println("6) Restaurar perfil de energía anterior");
        System.out.print("Elige: ");
        if (!scanner.hasNextLine()) return;
        String selected = scanner.nextLine().strip();
        if (selected.equals("4")) {
            System.out.println("Gobernador actual: " + center.currentGovernor());
            System.out.println("Aviso: power-profiles-daemon u otros servicios pueden volver a cambiar el gobernador.");
            var available = center.availableGovernors();
            System.out.println("Disponibles: " + String.join(", ", available));
            System.out.print("Escribe uno de los gobernadores disponibles (o Enter para cancelar): ");
            if (!scanner.hasNextLine()) return;
            String governor = scanner.nextLine().strip();
            if (governor.isEmpty()) return;
            System.out.print("Aplicar '" + governor + "'? Requiere sudo y guarda el valor anterior. [s/N]: ");
            if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
                center.applyGovernor(governor);
                writeAuditEvent("cpu_governor_changed", "Gobernador aplicado: " + governor);
                System.out.println("Gobernador aplicado. Puedes restaurarlo desde esta opción.");
            }
            return;
        }
        if (selected.equals("5")) {
            if (!center.canRestoreGovernor()) { System.out.println("No hay un gobernador anterior guardado."); return; }
            System.out.print("¿Restaurar el gobernador anterior? [s/N]: ");
            if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
                center.restoreGovernor();
                writeAuditEvent("cpu_governor_restored", "Se restauró el gobernador anterior");
                System.out.println("Gobernador anterior restaurado.");
            }
            return;
        }
        if (selected.equals("6")) {
            if (!center.canRestorePowerProfile()) { System.out.println("No hay un perfil anterior guardado."); return; }
            System.out.print("¿Restaurar el perfil de energía anterior? [s/N]: ");
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
    private static void runRamOptimization(OptimizationCenter center, java.util.Scanner scanner)
            throws java.io.IOException, InterruptedException {
        System.out.println("\n=== MEMORIA Y ZRAM ===");
        System.out.println("zram activo: " + (center.isZramActive() ? "sí" : "no"));
        System.out.println("Configuración administrada por la app: /etc/systemd/zram-generator.conf (si se creó aquí).");
        System.out.println("El perfil usa zstd y un tamaño máximo de la mitad de la RAM, limitado a 4 GiB.");
        System.out.println("No se purgan cachés ni se cierran procesos.");
        if (center.isZramActive() && !center.isManagedZram()) {
            System.out.println("zram ya está activo, pero no fue configurado por CachyOS Optimizer. No se modificará.");
            return;
        }
        if (center.isZramActive()) {
            System.out.print("¿Desactivar y retirar solamente la configuración creada por esta app? [s/N]: ");
            if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
                int exit = center.configureManagedZram(false);
                if (exit == 0) writeAuditEvent("zram_disabled", "Se retiró la configuración zram administrada");
                else System.err.println("No se pudo desactivar zram; código " + exit);
            }
            return;
        }
        System.out.println("La activación requiere zram-generator instalado y sudo.");
        System.out.print("¿Crear y activar una configuración administrada y reversible? [s/N]: ");
        if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
            int exit = center.configureManagedZram(true);
            if (exit == 0) {
                writeAuditEvent("zram_enabled", "Se creó y activó la configuración zram administrada");
                System.out.println("zram activado. La configuración puede retirarse desde este menú.");
            } else System.err.println("No se pudo activar zram; código " + exit + ". Revisa si zram-generator está instalado y si ya existe una configuración propia.");
        }
    }
    private static void runStorageOptimization(OptimizationCenter center, java.util.Scanner scanner)
            throws java.io.IOException, InterruptedException {
        boolean enabled = center.isTrimTimerEnabled();
        System.out.println("Estado de fstrim.timer: " + (enabled ? "habilitado" : "deshabilitado o no disponible"));
        if (enabled) {
            System.out.print("¿Deshabilitar TRIM programado? Requiere sudo; puedes volver a activarlo. [s/N]: ");
            if (scanner.hasNextLine() && scanner.nextLine().strip().equalsIgnoreCase("s")) {
                int exit = center.disableTrimTimer();
                if (exit == 0) {
                    System.out.println("fstrim.timer deshabilitado.");
                    writeAuditEvent("trim_timer_disabled", "Temporizador TRIM deshabilitado");
                } else System.err.println("systemctl terminó con código " + exit);
            }
            return;
        }
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
                System.out.println("║  6) Instalar herramientas de optimización ║");
                System.out.println("║  7) Centro de optimización               ║");
                System.out.println("║  8) Creador de parámetros de Steam       ║");
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
                    case "8" -> runSteamOptions(scanner);
                    case "0" -> { System.out.println("Hasta luego."); return; }
                    default -> System.out.println("Opción no válida. Elige una opción del 0 al 8.");
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
                  --compare-benchmarks A B  Compara dos CSV (antes/después).
                  --install-tools          Instala herramientas recomendadas en Arch/CachyOS.
                  --optimizer              Abre el centro de optimización guiada.
                  --steam-options          Crea parámetros de lanzamiento para Steam.
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
