package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.decor.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Our entities: only the invisible seat a Bar Stool's sitter rides. */
public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(SeedToCellar.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = ENTITIES.registerEntityType("seat", SeatEntity::new,
            MobCategory.MISC, builder -> builder.sized(0.01F, 0.01F).noSummon().clientTrackingRange(8));

    private ModEntities() {}
}
