package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.brewing.station.AbstractCaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A cask or keg item. If it was broken while full, it shows what's inside and how it would pour:
 * the same stars, conditioning and age the placed block would give (resting pauses until placed).
 */
public class CaskItem extends BlockItem {
    public CaskItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** Broken off a charred cask (its blockstate rides along on the item). */
    public static boolean charred(ItemStack stack) {
        BlockItemStateProperties state = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        return Boolean.TRUE.equals(state.get(CaskBlock.CHARRED));
    }

    @Override
    public Component getName(ItemStack stack) {
        return charred(stack) ? Component.translatable("block.seedtocellar.charred_cask", super.getName(stack)) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CaskContents contents = stack.get(ModComponents.CASK_CONTENTS.get());
        if (contents == null || contents.fluid().isEmpty()) return;
        FluidStack fluid = contents.fluid().copy();
        CaskWood wood = getBlock() instanceof AbstractCaskBlock cask ? cask.wood() : null;
        long ageTicks = CaskBlockEntity.agedTicks(contents.ageTicks(), wood);
        FluidStack serving = CaskBlockEntity.serving(fluid, ageTicks, wood, charred(stack));
        boolean ageable = wood != null && Drinks.byFluid(fluid.getFluid()).map(d -> d.profile().ageable()).orElse(false);

        tooltip.accept(serving.getHoverName().copy().append(" " + fluid.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
        BrewQuality quality = BrewQuality.of(serving);
        List<Component> lines = new ArrayList<>();
        DrinkItem.addQualityLines(quality, serving.getFluid(), BrewData.orNull(serving), lines);
        lines.forEach(tooltip);
        boolean rests = Drinks.byFluid(fluid.getFluid()).map(d -> d.profile().craft().byResting()).orElse(true);
        boolean conditioning = rests && !quality.craft(); // (a drink conditioned elsewhere keeps its star; spirits don't condition)
        if (conditioning) {
            tooltip.accept(Component.translatable("hydrometer.seedtocellar.conditioning", ageTicks * 100 / CaskBlockEntity.DAY)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (conditioning || (ageable && !quality.aged())) {
            tooltip.accept(Component.translatable("tooltip.seedtocellar.resting_paused").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
