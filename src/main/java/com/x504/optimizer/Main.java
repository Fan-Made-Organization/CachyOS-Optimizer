package com.x504.optimizer;

import com.x504.optimizer.compatibility.CompatibilityAnalyzer;
import com.x504.optimizer.advisor.OptimizationAdvisor;
import com.x504.optimizer.advisor.OptimizationAdvisor;
import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.modules.*;
import com.x504.optimizer.profiles.ProfileCatalog;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.SystemInfo;
import java.util.List;

/** Punto de entrada de X504 CachyOS Optimizer. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        System.out.println("=== X504 CachyOS Optimizer ===");
        SystemInfo info = new LinuxSystemDetector().detect();
        System.out.println(info.toDisplayString());
        System.out.println("\n=== Hardware ===");
        System.out.println(new HardwareDetector().detect().toDisplayString());
        System.out.println("\n=== Compatibilidad ===");
        System.out.println(new CompatibilityAnalyzer().analyze(info).toDisplayString());

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
        new ProfileCatalog().all().forEach(profile -> System.out.println("- " + profile.name() + ": " + profile.description()));
        System.out.println("\nModo actual: diagnóstico de solo lectura.");
        System.out.println("Ningún ajuste del sistema fue modificado.");
    }
}
