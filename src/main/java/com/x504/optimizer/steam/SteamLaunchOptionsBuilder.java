package com.x504.optimizer.steam;

import java.util.ArrayList;
import java.util.List;

/** Construye opciones de lanzamiento de Steam con combinaciones conocidas y sin shell personalizado. */
public final class SteamLaunchOptionsBuilder {
    public record Options(boolean gameMode, boolean mangoHud, boolean gamescope, boolean protonLog, boolean fsr) {}

    public String build(Options options) {
        if (options == null) throw new IllegalArgumentException("Debes elegir una configuración.");
        List<String> parts = new ArrayList<>();
        if (options.protonLog()) parts.add("PROTON_LOG=1");
        if (options.fsr()) {
            parts.add("WINE_FULLSCREEN_FSR=1");
            parts.add("WINE_FULLSCREEN_FSR_STRENGTH=2");
        }
        String command = "%command%";
        if (options.gamescope()) command = "gamescope -f -- " + command;
        if (options.mangoHud()) command = "mangohud " + command;
        if (options.gameMode()) command = "gamemoderun " + command;
        parts.add(command);
        return String.join(" ", parts);
    }

    public List<String> presets() {
        return List.of(
                "Básico: %command%",
                "GameMode: gamemoderun %command%",
                "MangoHud: mangohud %command%",
                "GameMode + MangoHud: gamemoderun mangohud %command%",
                "Gamescope: gamescope -f -- %command%",
                "Registro Proton: PROTON_LOG=1 %command%",
                "FSR de Proton: WINE_FULLSCREEN_FSR=1 WINE_FULLSCREEN_FSR_STRENGTH=2 %command%"
        );
    }
}