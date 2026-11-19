package com.x504.optimizer.modules;

import java.util.List;
import java.util.Locale;

/** Identifica KDE Plasma mediante variables de sesión. */
public final class KdeModule implements DiagnosticModule {
    public String id() { return "kde"; }
    public String name() { return "KDE Plasma"; }
    public ModuleReport inspect() {
        String desktop = System.getenv().getOrDefault("XDG_CURRENT_DESKTOP", "Desconocido");
        String session = System.getenv().getOrDefault("KDE_SESSION_VERSION", "Desconocida");
        boolean kde = desktop.toLowerCase(Locale.ROOT).contains("kde");
        return new ModuleReport(id(), name(), List.of("Escritorio: " + desktop,
                "Versión de sesión KDE: " + session,
                "KDE detectado: " + (kde ? "sí" : "no confirmado")),
                List.of("No se modificaron efectos, compositor ni ajustes de Plasma."));
    }
}
