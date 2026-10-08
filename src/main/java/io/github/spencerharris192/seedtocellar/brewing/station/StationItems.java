package io.github.spencerharris192.seedtocellar.brewing.station;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * A station's item slots. Hoppers and other mods reach them through NeoForge's item handler (insert and extract inside
 * transactions); the station's own code uses the plain helpers here: read or set a slot, put a stack in, take some out.
 * Subclasses override {@link #onContentsChanged(int)}, {@link #isItemValid} and {@link #getSlotLimit}.
 */
public class StationItems extends ItemStacksResourceHandler {
    public StationItems(int size) {
        super(size);
    }

    public int getSlots() {
        return size();
    }

    /** Loads saved slots, keeping this station's number of slots whatever was saved. */
    @Override
    public void deserialize(ValueInput input) {
        int size = size();
        super.deserialize(input);
        if (size() != size) {
            NonNullList<ItemStack> fixed = NonNullList.withSize(size, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(size, size()); i++) fixed.set(i, stacks.get(i));
            setStacks(fixed);
        }
    }

    /** The slot's live stack: change it through {@link #setStackInSlot} (or call {@link #onContentsChanged(int)} after). */
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    /** Called after a slot changes (from our code at once, from a transaction when it commits). */
    protected void onContentsChanged(int slot) {
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    @Override
    protected final void onContentsChanged(int index, ItemStack previousContents) {
        onContentsChanged(index);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource.toStack());
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return Math.min(getSlotLimit(index), super.getCapacity(index, resource));
    }

    /** Puts as much of the stack in the slot as fits; returns what's left over. */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        ItemStack current = getStackInSlot(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
        int room = Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - current.getCount();
        if (room <= 0) return stack;
        int moved = Math.min(room, stack.getCount());
        if (!simulate) setStackInSlot(slot, stack.copyWithCount(current.getCount() + moved));
        return stack.copyWithCount(stack.getCount() - moved);
    }

    /** Puts the stack into whichever slots will take it, joining stacks first; returns what's left over. */
    public ItemStack insertItemStacked(ItemStack stack, boolean simulate) {
        ItemStack left = stack;
        for (int slot = 0; slot < getSlots() && !left.isEmpty(); slot++) {
            if (!getStackInSlot(slot).isEmpty()) left = insertItem(slot, left, simulate);
        }
        for (int slot = 0; slot < getSlots() && !left.isEmpty(); slot++) {
            if (getStackInSlot(slot).isEmpty()) left = insertItem(slot, left, simulate);
        }
        return left;
    }

    /** Takes up to {@code amount} out of the slot; returns what was taken. */
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack current = getStackInSlot(slot);
        if (amount <= 0 || current.isEmpty()) return ItemStack.EMPTY;
        int taken = Math.min(amount, current.getCount());
        ItemStack out = current.copyWithCount(taken);
        if (!simulate) setStackInSlot(slot, current.copyWithCount(current.getCount() - taken));
        return out;
    }
}
