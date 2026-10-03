package io.github.spencerharris192.seedtocellar.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

/**
 * Serene Seasons (GDD section 12): winter makes a vat one step cooler, summer one step warmer. Only touched when Serene
 * Seasons is installed (Temperature checks first), so its classes are never needed otherwise.
 */
public final class SereneSeasonsCompat {
    /**
     * -1 in winter, +1 in summer, 0 in spring and autumn; 0 always where Serene Seasons has no seasons (a dimension it
     * isn't set to run in: by default all but the Overworld) or only wet and dry ones (its tropical biomes).
     */
    public static int seasonStep(Level level, BlockPos pos) {
        return seasonStep(level, level.getBiome(pos));
    }

    /** The same for a biome in this level (a test can ask about plains and savanna wherever it stands). */
    public static int seasonStep(Level level, net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome) {
        if (!hasSeasons(level) || SeasonHelper.usesTropicalSeasons(biome)) return 0;
        Season season = SeasonHelper.getSeasonState(level).getSeason();
        return season == Season.WINTER ? -1 : season == Season.SUMMER ? 1 : 0;
    }

    /** Serene Seasons' own list of dimensions with seasons (outside its API, so a version without it means the Overworld). */
    private static boolean hasSeasons(Level level) {
        try {
            var config = sereneseasons.init.ModConfig.seasons;
            if (config != null) return config.isDimensionWhitelisted(level.dimension());
        } catch (LinkageError ignored) {
            // an older or newer Serene Seasons
        }
        return level.dimension() == Level.OVERWORLD;
    }

    /** The current sub-season's name, as Serene Seasons' /season set command takes it ("mid_winter"). */
    public static String subSeason(Level level) {
        return SeasonHelper.getSeasonState(level).getSubSeason().getSerializedName();
    }

    private SereneSeasonsCompat() {}
}
