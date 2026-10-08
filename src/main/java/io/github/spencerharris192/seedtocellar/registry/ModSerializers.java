package io.github.spencerharris192.seedtocellar.registry;

import com.mojang.serialization.MapCodec;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.FertileHarvestModifier;
import io.github.spencerharris192.seedtocellar.farming.StrawModifier;
import io.github.spencerharris192.seedtocellar.world.ConfigAddFeaturesModifier;
import io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Data-file formats we add: loot modifiers and biome modifiers. */
public final class ModSerializers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SeedToCellar.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, SeedToCellar.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GrassSeedsModifier>> GRASS_SEEDS =
            LOOT_MODIFIERS.register("grass_seeds", () -> GrassSeedsModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<FertileHarvestModifier>> FERTILE_HARVEST =
            LOOT_MODIFIERS.register("fertile_harvest", () -> FertileHarvestModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<StrawModifier>> STRAW =
            LOOT_MODIFIERS.register("straw", () -> StrawModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<io.github.spencerharris192.seedtocellar.world.ChestLootModifier>> CHEST_LOOT =
            LOOT_MODIFIERS.register("chest_loot", () -> io.github.spencerharris192.seedtocellar.world.ChestLootModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigAddFeaturesModifier>> CONFIG_ADD_FEATURES =
            BIOME_MODIFIERS.register("config_add_features", () -> ConfigAddFeaturesModifier.CODEC);

    private ModSerializers() {}
}
