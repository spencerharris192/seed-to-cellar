package io.github.spencerharris192.seedtocellar.winery;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/** A station's input as machines see it: hoppers can put things in, but not pull them back out. */
public record InsertOnlyItems(IItemHandler inner) implements IItemHandler {
    @Override
    public int getSlots() {
        return inner.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inner.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return inner.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inner.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return inner.isItemValid(slot, stack);
    }
}
