package com.x504.optimizer.steam;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SteamLaunchOptionsBuilderTest {
    private final SteamLaunchOptionsBuilder builder = new SteamLaunchOptionsBuilder();

    @Test void createsBasicAndCombinedOptions() {
        assertEquals("%command%", builder.build(new SteamLaunchOptionsBuilder.Options(false,false,false,false,false)));
        assertEquals("gamemoderun mangohud %command%", builder.build(new SteamLaunchOptionsBuilder.Options(true,true,false,false,false)));
        assertEquals("gamescope -f -w 1920 -h 1080 -r 60 -- %command%", builder.build(new SteamLaunchOptionsBuilder.Options(false,false,true,false,false)));
    }

    @Test void addsProtonEnvironmentFlagsWithoutReplacingSteamPlaceholder() {
        assertEquals("PROTON_LOG=1 WINE_FULLSCREEN_FSR=1 WINE_FULLSCREEN_FSR_STRENGTH=2 gamemoderun %command%",
                builder.build(new SteamLaunchOptionsBuilder.Options(true,false,false,true,true)));
    }

    @Test void rejectsNullOptionsAndProvidesPresets() {
        assertThrows(IllegalArgumentException.class, () -> builder.build((SteamLaunchOptionsBuilder.Options) null));
        assertEquals(8, builder.presets().size());
    }
    @Test void buildsGamescopeResolutionAndRefreshRate() {
        var advanced = new SteamLaunchOptionsBuilder.AdvancedOptions(true, true, true, false, true, 1280, 720, 120, 3);
        assertEquals("WINE_FULLSCREEN_FSR=1 WINE_FULLSCREEN_FSR_STRENGTH=3 gamemoderun mangohud gamescope -f -w 1280 -h 720 -r 120 -- %command%",
                builder.build(advanced));
    }

    @Test void rejectsUnsafeOrOutOfRangeNumericParameters() {
        assertThrows(IllegalArgumentException.class, () -> builder.build(
                new SteamLaunchOptionsBuilder.AdvancedOptions(false,false,true,false,false,1,720,60,2)));
        assertThrows(IllegalArgumentException.class, () -> builder.build(
                new SteamLaunchOptionsBuilder.AdvancedOptions(false,false,true,false,false,1920,1080,999,2)));
        assertThrows(IllegalArgumentException.class, () -> builder.build(
                new SteamLaunchOptionsBuilder.AdvancedOptions(false,false,false,false,true,1920,1080,60,99)));
    }

    @Test void addsWineD3DEnvironmentFlagWhenRequested() {
        assertEquals("PROTON_USE_WINED3D=1 %command%",\n                builder.build(new SteamLaunchOptionsBuilder.Options(false,false,false,false,false,true)));
        assertEquals("PROTON_USE_WINED3D=1 gamemoderun %command%",\n                builder.build(new SteamLaunchOptionsBuilder.Options(true,false,false,false,false,true)));\n    }
\n    @Test void providesGameUsageProfiles() {
        assertEquals(4, builder.gameProfiles().size());
    }

}