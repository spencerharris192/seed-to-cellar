package io.github.spencerharris192.seedtocellar.registry;

import com.google.common.collect.ImmutableSet;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Villager professions (GDD section 18.3). The Vintner works at a Fruit Press (every Vineyard has one), the Brewer at a
 * Brew Kettle (every Brewhouse has one): a villager with no job takes up an unclaimed one nearby. Both job sites are in
 * {@code minecraft:acquirable_job_site} (ModPoiTypeTagsProvider). Trades are in world/Villages.
 */
public final class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, SeedToCellar.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, SeedToCellar.MOD_ID);

    /** Any Fruit Press, whichever way it faces, is a vintner's job site (one villager each). */
    public static final RegistryObject<PoiType> VINTNER_POI = POI_TYPES.register("vintner",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.FRUIT_PRESS.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final RegistryObject<VillagerProfession> VINTNER = PROFESSIONS.register("vintner",
            () -> new VillagerProfession("vintner", poi -> poi.is(VINTNER_POI.getKey()), poi -> poi.is(VINTNER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_FARMER));

    /** Any Brew Kettle is a brewer's job site. */
    public static final RegistryObject<PoiType> BREWER_POI = POI_TYPES.register("brewer",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.BREW_KETTLE.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final RegistryObject<VillagerProfession> BREWER = PROFESSIONS.register("brewer",
            () -> new VillagerProfession("brewer", poi -> poi.is(BREWER_POI.getKey()), poi -> poi.is(BREWER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC));

    private ModVillagers() {}
}
