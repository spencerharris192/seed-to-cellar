package io.github.spencerharris192.seedtocellar.registry;

import com.mojang.serialization.MapCodec;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.world.WildVanillaFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** World generation feature types of our own (the rest use vanilla's: patches, trees); the features themselves are data. */
public final class ModFeatures {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURE_TYPES = DeferredRegister.create(Registries.FEATURE_TYPE, SeedToCellar.MOD_ID);

    /** Vanilla vines climbing jungle tree trunks. */
    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<WildVanillaFeature>> WILD_VANILLA =
            FEATURE_TYPES.register("wild_vanilla", () -> WildVanillaFeature.CODEC);

    private ModFeatures() {}
}
