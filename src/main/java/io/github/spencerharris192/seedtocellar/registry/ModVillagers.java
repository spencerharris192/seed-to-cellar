package io.github.spencerharris192.seedtocellar.registry;

import com.google.common.collect.ImmutableSet;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Villager professions (GDD section 18.3). The Vintner works at a Fruit Press (every Vineyard has one), the Brewer at a
 * Brew Kettle (every Brewhouse has one): a villager with no job takes up an unclaimed one nearby. Both job sites are in
 * {@code minecraft:acquirable_job_site} (ModPoiTypeTagsProvider). Their trades are data files: trade sets
 * {@code seedtocellar:<profession>/level_<n>} drawing on villager trades tagged {@code seedtocellar:<profession>/level_<n>}
 * (datagen writes them; pack makers can add to the tags).
 */
public final class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, SeedToCellar.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, SeedToCellar.MOD_ID);

    /** Any Fruit Press, whichever way it faces, is a vintner's job site (one villager each). */
    public static final DeferredHolder<PoiType, PoiType> VINTNER_POI = POI_TYPES.register("vintner",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.FRUIT_PRESS.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> VINTNER = PROFESSIONS.register("vintner",
            () -> new VillagerProfession(Component.translatable("entity.seedtocellar.villager.vintner"),
                    poi -> poi.is(VINTNER_POI.key()), poi -> poi.is(VINTNER_POI.key()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FARMER, tradeSets("vintner")));

    /** Any Brew Kettle is a brewer's job site. */
    public static final DeferredHolder<PoiType, PoiType> BREWER_POI = POI_TYPES.register("brewer",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.BREW_KETTLE.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BREWER = PROFESSIONS.register("brewer",
            () -> new VillagerProfession(Component.translatable("entity.seedtocellar.villager.brewer"),
                    poi -> poi.is(BREWER_POI.key()), poi -> poi.is(BREWER_POI.key()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC, tradeSets("brewer")));

    /** The trade set for one of a profession's five levels (Novice 1 to Master 5). */
    public static ResourceKey<TradeSet> tradeSet(String profession, int level) {
        return ResourceKey.create(Registries.TRADE_SET, SeedToCellar.id(profession + "/level_" + level));
    }

    private static Int2ObjectMap<ResourceKey<TradeSet>> tradeSets(String profession) {
        Int2ObjectMap<ResourceKey<TradeSet>> sets = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) sets.put(level, tradeSet(profession, level));
        return sets;
    }

    private ModVillagers() {}
}
