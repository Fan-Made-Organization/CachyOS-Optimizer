package com.x504.optimizer.steam;

import java.util.ArrayList;
import java.util.List;

/** Genera parámetros de lanzamiento conocidos para Steam sin aceptar comandos arbitrarios. */
public final class SteamLaunchOptionsBuilder {
    public record Options(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog, boolean fsr, boolean wined3d) {
        public Options(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog, boolean fsr) {
            this(gameMode, mangoHud, gamescope, protonLog, fsr, false);
        }
    }
    public record AdvancedOptions(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog,
                                  boolean fsr, int width, int height, int refreshRate, int fsrStrength, boolean wined3d,
                                  boolean frameLimitEnabled, int frameLimit) {
        public AdvancedOptions(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog,
                               boolean fsr, int width, int height, int refreshRate, int fsrStrength, boolean wined3d) {
            this(gameMode, mangoHud, gamescope, protonLog, fsr, width, height, refreshRate, fsrStrength, wined3d, false, 60);
        }
        public AdvancedOptions(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog,
                               boolean fsr, int width, int height, int refreshRate, int fsrStrength) {
            this(gameMode, mangoHud, gamescope, protonLog, fsr, width, height, refreshRate, fsrStrength, false, false, 60);
        }
    }

    public String build(Options options) {
        if (options == null) throw new IllegalArgumentException("Debes elegir una configuración.");
        return build(new AdvancedOptions(options.gameMode(), options.mangoHud(), options.gamescope(),
                options.protonLog(), options.fsr(), 1920, 1080, 60, 2, options.wined3d()));
    }

    public String build(AdvancedOptions options) {
        if (options == null) throw new IllegalArgumentException("Debes elegir una configuración.");
        validate(options);
        List<String> parts = new ArrayList<>();
        if (options.protonLog()) parts.add("PROTON_LOG=1");
        if (options.wined3d()) parts.add("PROTON_USE_WINED3D=1");
        if (options.frameLimitEnabled()) parts.add("DXVK_FRAME_RATE=" + options.frameLimit());
        if (options.fsr()) {
            parts.add("WINE_FULLSCREEN_FSR=1");
            parts.add("WINE_FULLSCREEN_FSR_STRENGTH=" + options.fsrStrength());
        }
        String command = "%command%";
        if (options.gamescope()) {
            command = "gamescope -f -w " + options.width() + " -h " + options.height()
                    + " -r " + options.refreshRate() + " -- " + command;
        }
        if (options.mangoHud()) command = "mangohud " + command;
        if (options.gameMode()) command = "gamemoderun " + command;
        parts.add(command);
        return String.join(" ", parts);
    }

    public static void validate(AdvancedOptions o) {
        if (o.width() < 640 || o.width() > 7680 || o.height() < 480 || o.height() > 4320)
            throw new IllegalArgumentException("Resolución fuera del rango permitido (640x480 a 7680x4320).");
        if (o.refreshRate() < 30 || o.refreshRate() > 360)
            throw new IllegalArgumentException("Frecuencia fuera del rango permitido (30-360 Hz).");
        if (o.fsrStrength() < 0 || o.fsrStrength() > 5)
            throw new IllegalArgumentException("La intensidad FSR debe estar entre 0 y 5.");
        if (o.frameLimitEnabled() && (o.frameLimit() < 1 || o.frameLimit() > 360))
            throw new IllegalArgumentException("El límite de FPS debe estar entre 1 y 360.");
    }

    public List<String> presets() {
        return List.of(
                "Básico: %command%",
                "GameMode: gamemoderun %command%",
                "MangoHud: mangohud %command%",
                "GameMode + MangoHud: gamemoderun mangohud %command%",
                "Gamescope: gamescope -f -- %command%",
                "Registro Proton: PROTON_LOG=1 %command%",
                "FSR de Proton: WINE_FULLSCREEN_FSR=1 WINE_FULLSCREEN_FSR_STRENGTH=2 %command%",
                "WineD3D (OpenGL): PROTON_USE_WINED3D=1 %command%"
        );
    }

    public List<String> gameProfiles() {
        return List.of(
                "Competitivo: GameMode + MangoHud (no cambia gráficos del juego)",
                "Equilibrado: GameMode (prioriza una gestión de recursos razonable)",
                "Diagnóstico: MangoHud + registro Proton",
                "Escalado: Gamescope + FSR (si el juego y la sesión lo admiten)"
        );
    }
}