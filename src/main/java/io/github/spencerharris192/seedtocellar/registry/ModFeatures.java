package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.world.WildVanillaFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** World generation features of our own (the rest use vanilla's: patches, trees). */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, SeedToCellar.MOD_ID);

    /** Vanilla vines climbing jungle tree trunks. */
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WILD_VANILLA = FEATURES.register("wild_vanilla", WildVanillaFeature::new);

    private ModFeatures() {}
}
