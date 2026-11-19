package com.x504.optimizer;

import com.x504.optimizer.compatibility.CompatibilityAnalyzer;
import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.modules.*;
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
                new KdeModule(), new SteamModule(), new DiagnosticsModule()
        ));
        System.out.println("\n=== Diagnósticos por módulo ===");
        for (DiagnosticModule.ModuleReport report : manager.inspectAll()) {
            System.out.println("\n[" + report.title() + "]");
            report.findings().forEach(line -> System.out.println("  • " + line));
            report.warnings().forEach(line -> System.out.println("  ! " + line));
        }
        System.out.println("\nModo actual: diagnóstico de solo lectura.");
        System.out.println("Ningún ajuste del sistema fue modificado.");
    }
}
