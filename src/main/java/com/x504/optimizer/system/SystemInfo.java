package com.x504.optimizer.system;

import java.util.Objects;

/** Información detectada del sistema anfitrión. */
public record SystemInfo(String osName, String distro, String distroVersion,
                         String kernel, String architecture, String desktop,
                         boolean linux, boolean osReleaseAvailable) {
    public SystemInfo {
        osName = clean(osName);
        distro = clean(distro);
        distroVersion = clean(distroVersion);
        kernel = clean(kernel);
        architecture = clean(architecture);
        desktop = clean(desktop);
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "Desconocido").strip();
    }

    public String toDisplayString() {
        return """
                Sistema operativo: %s
                Distribución: %s
                Versión: %s
                Kernel: %s
                Arquitectura: %s
                Entorno de escritorio: %s""".formatted(osName, distro, distroVersion,
                kernel, architecture, desktop);
    }
}
