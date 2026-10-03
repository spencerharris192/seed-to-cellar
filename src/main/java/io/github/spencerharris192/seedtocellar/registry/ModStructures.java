package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.world.CellarPoolElement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Structure pieces of our own (saved with the structures they're part of, so the IDs are permanent). */
public final class ModStructures {
    public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, SeedToCellar.MOD_ID);

    /** A village building with a cellar built under it (world/CellarPoolElement). */
    public static final RegistryObject<StructurePoolElementType<CellarPoolElement>> CELLAR_BUILDING =
            POOL_ELEMENT_TYPES.register("cellar_building", () -> () -> CellarPoolElement.CODEC);

    private ModStructures() {}
}
