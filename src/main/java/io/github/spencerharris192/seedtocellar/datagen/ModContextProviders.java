package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/** Our numbers the game looks up by name: fuel burn times, divided like vanilla's in a smoker or blast furnace. */
public final class ModContextProviders {
    public static void bootstrap(BootstrapContext<ContextIntProvider> context) {
        HolderGetter<LootItemCondition> predicates = context.lookup(Registries.PREDICATE);
        HolderGetter<ContextIntProvider> numbers = context.lookup(Registries.CONTEXT_INT_PROVIDER);
        Holder<ContextIntProvider> divisor = Holder.direct(new ConditionalValue(predicates.getOrThrow(LootPredicates.FAST_FURNACE),
                numbers.getOrThrow(ContextIntProviders.COOKING_FAST_BURN_TIME_REDUCTION_FACTOR),
                numbers.getOrThrow(ContextIntProviders.COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR)));
        context.register(ModItems.OLIVE_POMACE_BURN_TIME, ContextIntProviders.div(ContextIntProviders.exactly(400), divisor).value());
    }

    private ModContextProviders() {}
}
