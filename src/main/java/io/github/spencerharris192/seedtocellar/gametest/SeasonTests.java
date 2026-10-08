package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.compat.SereneSeasonsCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.fml.ModList;

/** Serene Seasons moves fermentation temperature a step (its part runs with -PwithCompat, when it's loaded). */
public final class SeasonTests {
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void seasonsMoveTheTemperatureAStep(GameTestHelper helper) {
        helper.assertTrue(Temperature.shift(Temperature.COOL, -1) == Temperature.COLD
                && Temperature.shift(Temperature.COLD, -1) == Temperature.COLD
                && Temperature.shift(Temperature.WARM, 1) == Temperature.WARM,
                "a season moves the temperature one step, within Cold to Warm");
        if (ModList.get().isLoaded("sereneseasons")) {
            // Winter a step cooler and summer a step warmer in plains; nothing in savanna, which Serene Seasons gives wet and
            // dry seasons instead. The season is set and put back within this tick, so no other test sees it.
            var level = helper.getLevel();
            var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
            var plains = biomes.getOrThrow(Biomes.PLAINS);
            var savanna = biomes.getOrThrow(Biomes.SAVANNA);
            var commands = level.getServer().createCommandSourceStack().withLevel(level).withSuppressedOutput();
            String before = SereneSeasonsCompat.subSeason(level);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set mid_winter");
            int plainsWinter = SereneSeasonsCompat.seasonStep(level, plains), savannaWinter = SereneSeasonsCompat.seasonStep(level, savanna);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set mid_summer");
            int plainsSummer = SereneSeasonsCompat.seasonStep(level, plains), savannaSummer = SereneSeasonsCompat.seasonStep(level, savanna);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set " + before);
            helper.assertTrue(plainsWinter == -1 && plainsSummer == 1,
                    "plains: winter a step cooler, summer a step warmer (got " + plainsWinter + ", " + plainsSummer + ")");
            helper.assertTrue(savannaWinter == 0 && savannaSummer == 0, "savanna has wet and dry seasons: no change");
        }
        helper.succeed();
    }

    private SeasonTests() {}
}
