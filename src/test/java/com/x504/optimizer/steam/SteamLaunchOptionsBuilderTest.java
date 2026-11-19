package com.x504.optimizer.steam;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SteamLaunchOptionsBuilderTest {
    private final SteamLaunchOptionsBuilder builder = new SteamLaunchOptionsBuilder();

    @Test void createsBasicAndCombinedOptions() {
        assertEquals("%command%", builder.build(new SteamLaunchOptionsBuilder.Options(false,false,false,false,false)));
        assertEquals("gamemoderun mangohud %command%", builder.build(new SteamLaunchOptionsBuilder.Options(true,true,false,false,false)));
        assertEquals("gamescope -f -- %command%", builder.build(new SteamLaunchOptionsBuilder.Options(false,false,true,false,false)));
    }

    @Test void addsProtonEnvironmentFlagsWithoutReplacingSteamPlaceholder() {
        assertEquals("PROTON_LOG=1 WINE_FULLSCREEN_FSR=1 WINE_FULLSCREEN_FSR_STRENGTH=2 gamemoderun %command%",
                builder.build(new SteamLaunchOptionsBuilder.Options(true,false,false,true,true)));
    }

    @Test void rejectsNullOptionsAndProvidesPresets() {
        assertThrows(IllegalArgumentException.class, () -> builder.build(null));
        assertEquals(7, builder.presets().size());
    }
}