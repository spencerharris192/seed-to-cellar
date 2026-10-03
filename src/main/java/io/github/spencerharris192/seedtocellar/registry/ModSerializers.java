package io.github.spencerharris192.seedtocellar.registry;

import com.mojang.serialization.Codec;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.FertileHarvestModifier;
import io.github.spencerharris192.seedtocellar.farming.StrawModifier;
import io.github.spencerharris192.seedtocellar.world.ConfigAddFeaturesModifier;
import io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Data-file formats we add: loot modifiers and biome modifiers. */
public final class ModSerializers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SeedToCellar.MOD_ID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SeedToCellar.MOD_ID);

    public static final RegistryObject<Codec<GrassSeedsModifier>> GRASS_SEEDS =
            LOOT_MODIFIERS.register("grass_seeds", GrassSeedsModifier.CODEC);
    public static final RegistryObject<Codec<FertileHarvestModifier>> FERTILE_HARVEST =
            LOOT_MODIFIERS.register("fertile_harvest", FertileHarvestModifier.CODEC);
    public static final RegistryObject<Codec<StrawModifier>> STRAW =
            LOOT_MODIFIERS.register("straw", StrawModifier.CODEC);
    public static final RegistryObject<Codec<io.github.spencerharris192.seedtocellar.world.ChestLootModifier>> CHEST_LOOT =
            LOOT_MODIFIERS.register("chest_loot", io.github.spencerharris192.seedtocellar.world.ChestLootModifier.CODEC);
    public static final RegistryObject<Codec<ConfigAddFeaturesModifier>> CONFIG_ADD_FEATURES =
            BIOME_MODIFIERS.register("config_add_features", () -> ConfigAddFeaturesModifier.CODEC);

    private ModSerializers() {}
}
