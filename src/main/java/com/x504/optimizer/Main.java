package com.x504.optimizer;

import com.x504.optimizer.compatibility.CompatibilityAnalyzer;
import com.x504.optimizer.hardware.HardwareDetector;
import com.x504.optimizer.system.LinuxSystemDetector;
import com.x504.optimizer.system.SystemInfo;

/** Punto de entrada de X504 CachyOS Optimizer. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        System.out.println("=== X504 CachyOS Optimizer ===");
        SystemInfo info = new LinuxSystemDetector().detect();
        System.out.println(info.toDisplayString());
        System.out.println();
        System.out.println("=== Hardware ===");
        System.out.println(new HardwareDetector().detect().toDisplayString());
        System.out.println();
        System.out.println("=== Compatibilidad ===");
        System.out.println(new CompatibilityAnalyzer().analyze(info).toDisplayString());
        System.out.println();
        System.out.println("Modo actual: diagnóstico de solo lectura.");
        System.out.println("Ningún ajuste del sistema fue modificado.");
    }
}
