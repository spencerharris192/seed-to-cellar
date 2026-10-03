package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;

/**
 * Climate preference (GDD section 6.5): speed only, never a hard stop.
 * <ul>
 *   <li>A place's climate comes from its biome's temperature: Cold under 0.3 (taiga, snow),
 *       Temperate under 0.9 (plains, forests), Warm under 1.5 (jungles), Hot above (savannas,
 *       deserts, badlands).</li>
 *   <li>A greenhouse counts as Warm: glass anywhere in the 8 blocks above the crop.</li>
 *   <li>Outside its climate a crop loses a share of its growth (config climateSlowdown, half by
 *       default). With Serene Seasons installed, seasons take over and this switches off unless
 *       the config says otherwise, so the two penalties don't stack.</li>
 *   <li>The global cropGrowthMultiplier applies on top, to all of our crops.</li>
 * </ul>
 */
public final class ClimateRules {
    public static final int GREENHOUSE_HEIGHT = 8;

    public static Climate at(LevelReader level, BlockPos pos) {
        if (underGlass(level, pos)) return Climate.WARM;
        float temperature = level.getBiome(pos).value().getBaseTemperature();
        if (temperature < 0.3F) return Climate.COLD;
        if (temperature < 0.9F) return Climate.TEMPERATE;
        if (temperature < 1.5F) return Climate.WARM;
        return Climate.HOT;
    }

    public static boolean underGlass(LevelReader level, BlockPos pos) {
        for (int dy = 1; dy <= GREENHOUSE_HEIGHT; dy++) {
            if (level.getBlockState(pos.above(dy)).is(Tags.Blocks.GLASS)) return true;
        }
        return false;
    }

    /** Whether climate preference is in effect at all (config, and Serene Seasons). */
    public static boolean active() {
        if (ModConfigs.COMMON.climateSlowdown.get() <= 0) return false;
        return !ModList.get().isLoaded("sereneseasons") || ModConfigs.COMMON.climateWithSereneSeasons.get();
    }

    public static boolean suits(Climate preferred, LevelReader level, BlockPos pos) {
        return !active() || at(level, pos) == preferred;
    }

    /**
     * How many growth attempts a crop gets from this random tick: usually 1; fewer outside its
     * climate (a chance of 0); more if the growth multiplier is above 1.
     */
    public static int growthAttempts(Climate preferred, LevelReader level, BlockPos pos, RandomSource random) {
        double chance = ModConfigs.COMMON.cropGrowthMultiplier.get();
        if (active() && at(level, pos) != preferred) chance *= 1.0 - ModConfigs.COMMON.climateSlowdown.get();
        int whole = (int) chance;
        return whole + (random.nextDouble() < chance - whole ? 1 : 0);
    }

    private ClimateRules() {}
}
