package io.github.spencerharris192.seedtocellar.winery;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * The bottles in a rack (GDD section 16): one per place, top row first, left to right as you face it, as many as its
 * {@link RackLayout} has. Hoppers and pipes put bottles in and take them out; the renderer shows each one.
 */
public class WineRackBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    private final RackLayout layout;
    private final ItemStackHandler bottles;
    private final LazyOptional<IItemHandler> itemCap;

    public WineRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINE_RACK.get(), pos, state);
        this.layout = state.getBlock() instanceof WineRackBlock rack ? rack.layout() : RackLayout.WINE_RACK;
        this.bottles = new ItemStackHandler(layout.slots()) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.is(layout.accepts());
            }

            @Override
            protected void onContentsChanged(int slot) {
                sync();
                if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        };
        this.itemCap = LazyOptional.of(() -> bottles);
    }

    /** How many different drinks it holds (Tavern Keeper: a Bottle Shelf of six). */
    public int differentDrinks() {
        java.util.Set<net.minecraft.world.item.Item> kinds = new java.util.HashSet<>();
        for (int slot = 0; slot < bottles().getSlots(); slot++) {
            if (!bottle(slot).isEmpty()) kinds.add(bottle(slot).getItem());
        }
        return kinds.size();
    }

    public RackLayout layout() {
        return layout;
    }

    public ItemStackHandler bottles() {
        return bottles;
    }

    public ItemStack bottle(int slot) {
        return bottles.getStackInSlot(slot);
    }

    public int count() {
        int count = 0;
        for (int i = 0; i < bottles.getSlots(); i++) if (!bottles.getStackInSlot(i).isEmpty()) count++;
        return count;
    }

    /** How many bottles, then each one with its stars (the Hydrometer and Jade); a Mug Rack just counts its mugs. */
    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        if (layout == RackLayout.MUG_RACK) {
            lines.add(Component.translatable("hydrometer.seedtocellar.mugs", count(), bottles.getSlots()));
            return lines;
        }
        lines.add(Component.translatable("hydrometer.seedtocellar.bottles", count(), bottles.getSlots()));
        for (int i = 0; i < bottles.getSlots(); i++) {
            ItemStack bottle = bottles.getStackInSlot(i);
            if (bottle.isEmpty()) continue;
            MutableComponent line = bottle.getHoverName().copy().withStyle(ChatFormatting.GRAY);
            if (bottle.getItem() instanceof DrinkItem drink && drink.profile().graded()) {
                line.append(" ").append(DrinkItem.stars(DrinkItem.quality(bottle).stars()));
            }
            lines.add(line);
        }
        return lines;
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < bottles.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), bottles.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Bottles", bottles.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag saved = tag.getCompound("Bottles");
        saved.putInt("Size", bottles.getSlots());   // the layout decides the size, whatever was saved
        bottles.deserializeNBT(saved);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }
}
