package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.world.attribute.EnvironmentAttributes;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Fermentation temperature from placement (GDD section 12):
 * biome -> one step cooler in a cellar (no sky, never below Cool) -> with Serene Seasons, a step cooler in winter and
 * warmer in summer -> ice next to it makes it Cold -> a heat source next to it makes it Warm. The Nether is always Warm.
 */
public enum Temperature implements StringRepresentable {
    COLD, COOL, MILD, WARM;

    public static final com.mojang.serialization.Codec<Temperature> CODEC = StringRepresentable.fromEnum(Temperature::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public Component displayName() {
        return Component.translatable("temperature.seedtocellar." + getSerializedName());
    }

    public static Temperature byName(String name) {
        for (Temperature t : values()) if (t.getSerializedName().equals(name)) return t;
        return MILD;
    }

    public static Temperature at(Level level, BlockPos pos) {
        boolean heat = false;
        boolean cold = false;
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (isHeatSource(neighbor)) heat = true;
            if (neighbor.is(ModTags.Blocks.COOLING)) cold = true;
        }
        if (heat) return WARM;
        if (cold) return COLD;
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) return WARM;   // the nether

        float biome = level.getBiome(pos).value().getBaseTemperature();
        Temperature base = biome < 0.15F ? COLD : biome < 0.45F ? COOL : biome < 0.9F ? MILD : WARM;
        if (!level.canSeeSky(pos.above()) && (base == MILD || base == WARM)) {
            base = values()[base.ordinal() - 1]; // cellars are cool
        }
        if (SEASONS && io.github.spencerharris192.seedtocellar.config.ModConfigs.seasonsChangeTemperature()) {
            base = shift(base, io.github.spencerharris192.seedtocellar.compat.SereneSeasonsCompat.seasonStep(level, pos));
        }
        return base;
    }

    /** Serene Seasons is installed: the season moves the temperature (only then is its class touched). */
    private static final boolean SEASONS = net.neoforged.fml.ModList.get().isLoaded("sereneseasons");

    /** `steps` warmer (or cooler, if negative), staying within Cold to Warm. */
    public static Temperature shift(Temperature t, int steps) {
        return values()[net.minecraft.util.Mth.clamp(t.ordinal() + steps, 0, values().length - 1)];
    }

    /** A heat-source block that is actually burning (an unlit campfire gives no heat). */
    public static boolean isHeatSource(BlockState state) {
        return state.is(ModTags.Blocks.HEAT_SOURCES) && state.getOptionalValue(BlockStateProperties.LIT).orElse(true);
    }
}
