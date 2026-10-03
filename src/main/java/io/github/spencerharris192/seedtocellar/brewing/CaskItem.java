package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.brewing.station.AbstractCaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

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
        CompoundTag state = stack.getTagElement(BLOCK_STATE_TAG);
        return state != null && "true".equals(state.getString(io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock.CHARRED.getName()));
    }

    @Override
    public Component getName(ItemStack stack) {
        return charred(stack) ? Component.translatable("block.seedtocellar.charred_cask", super.getName(stack)) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag data = getBlockEntityData(stack);
        if (data == null) return;
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(data.getCompound("Tank"));
        if (fluid.isEmpty()) return;
        CaskWood wood = getBlock() instanceof AbstractCaskBlock cask ? cask.wood() : null;
        long ageTicks = CaskBlockEntity.agedTicks(data.getLong("AgeTicks"), wood);
        FluidStack serving = CaskBlockEntity.serving(fluid, ageTicks, wood, charred(stack));
        boolean ageable = wood != null && Drinks.byFluid(fluid.getFluid()).map(d -> d.profile().ageable()).orElse(false);

        tooltip.add(serving.getDisplayName().copy().append(" " + fluid.getAmount() + " mB").withStyle(ChatFormatting.GRAY));
        BrewQuality quality = BrewQuality.of(serving);
        DrinkItem.addQualityLines(quality, serving.getFluid(), serving.getTag(), tooltip);
        boolean rests = Drinks.byFluid(fluid.getFluid()).map(d -> d.profile().craft().byResting()).orElse(true);
        boolean conditioning = rests && !quality.craft(); // (a drink conditioned elsewhere keeps its star; spirits don't condition)
        if (conditioning) {
            tooltip.add(Component.translatable("hydrometer.seedtocellar.conditioning", ageTicks * 100 / CaskBlockEntity.DAY)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (conditioning || (ageable && !quality.aged())) {
            tooltip.add(Component.translatable("tooltip.seedtocellar.resting_paused").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
