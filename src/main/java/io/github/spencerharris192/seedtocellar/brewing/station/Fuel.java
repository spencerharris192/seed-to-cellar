package io.github.spencerharris192.seedtocellar.brewing.station;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;

import java.util.Optional;

/** Furnace fuel for our stations: whatever burns in a vanilla furnace burns as long in ours. */
public final class Fuel {
    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL);
    }

    /** How long the fuel burns in this station, in ticks (as in a plain furnace); 0 for anything that isn't fuel. */
    public static int burnTicks(ServerLevel level, BlockEntity station, ItemStack fuel) {
        if (!isFuel(fuel)) return 0;
        LootContext context = new LootContext.Builder(new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, station.getBlockState())
                .withParameter(LootContextParams.BLOCK_ENTITY, station)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(station.getBlockPos()))
                .withParameter(LootContextParams.CONTAINER, new SimpleContainer(0))
                .withOptionalParameter(NeoForgeLootContextParams.QUERIED_STACK, fuel)
                .create(LootContextParamSets.CONTAINER_PROCESS)).create(Optional.empty());
        return ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }

    private Fuel() {}
}
