package com.x504.optimizer;

import com.x504.optimizer.advisor.OptimizationAdvisor;
import com.x504.optimizer.audit.AuditLogger;
import com.x504.optimizer.benchmark.BenchmarkEngine;
import com.x504.optimizer.compatibility.CompatibilityAnalyzer;
import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.modules.*;
import com.x504.optimizer.profiles.ProfileCatalog;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.SystemAnalysisDashboard;
import com.x504.optimizer.system.SystemInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;

/** Punto de entrada de X504 CachyOS Optimizer. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--help")) {
            printHelp();
            return;
        }

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
                new PowerModule(), new NetworkModule(), new AudioModule(), new GamingModule(),
                new KdeModule(), new SteamModule(), new ProtonModule(), new DiagnosticsModule()
        ));
        System.out.println("\n=== Diagnósticos por módulo ===");
        var reports = manager.inspectAll();
        for (DiagnosticModule.ModuleReport report : reports) {
            System.out.println("\n[" + report.title() + "]");
            report.findings().forEach(line -> System.out.println("  • " + line));
            report.warnings().forEach(line -> System.out.println("  ! " + line));
        }

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
        }

        writeAuditLog(reports.size(), recommendations.size());
        System.out.println("\nModo actual: diagnóstico de solo lectura.");
        System.out.println("No se aplicaron cambios de configuración al sistema.");
        System.out.println("Usa --benchmark para ejecutar la prueba breve de CPU; --help para ver opciones.");
    }

    private static void writeAuditLog(int modules, int recommendations) {
        String configuredStateHome = System.getenv("XDG_STATE_HOME");
        Path stateHome = configuredStateHome != null && !configuredStateHome.isBlank()
                && Path.of(configuredStateHome).isAbsolute()
                ? Path.of(configuredStateHome)
                : Path.of(System.getProperty("user.home"), ".local", "state");
        Path log = stateHome.resolve("x504-cachyos-optimizer").resolve("audit.log");
        try {
            new AuditLogger(log).record("diagnostic_completed",
                    modules + " módulos inspeccionados; " + recommendations + " recomendaciones generadas");
        } catch (IOException exception) {
            System.err.println("Aviso: no se pudo guardar el registro local de auditoría: " + exception.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("""
                X504 CachyOS Optimizer
                Uso: java -jar cachyos-optimizer.jar [opciones]

                Opciones:
                  --help       Muestra esta ayuda.
                  --benchmark  Ejecuta un microbenchmark breve de CPU.
                               No mide FPS ni rendimiento de GPU/juegos.

                El análisis normal es de solo lectura. No aplica ajustes al sistema.
                """);
    }
}
