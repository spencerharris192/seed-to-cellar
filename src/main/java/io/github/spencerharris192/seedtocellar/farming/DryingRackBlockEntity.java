package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.DryingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Drying Rack logic (GDD section 7). Four hanging spots, each drying one item by its drying
 * recipe; a finished item turns into its result and stays on the rack until taken.
 * <p>
 * Weather matters: sun on the rack dries twice as fast, rain or snow on it pauses drying, and
 * anywhere else (shade, indoors, night) it dries at the normal rate. Progress is added up from
 * the game time elapsed at each check (a scheduled block tick every {@link #CHECK_TICKS} while
 * something is drying, plus whenever the rack is used), never per tick, and the rate at each
 * check covers the time since the last one, so it also catches up after the chunk was unloaded.
 */
public class DryingRackBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int SLOTS = 4;
    public static final int CHECK_TICKS = 100;

    /** How the weather is treating the rack right now. */
    public enum Weather {
        SUN(2), SHADE(1), RAIN(0);

        public final int rate;

        Weather(int rate) {
            this.rate = rate;
        }
    }

    private final ItemStack[] stacks = new ItemStack[SLOTS];
    private final int[] progress = new int[SLOTS];
    private final int[] total = new int[SLOTS];   // 0 = not drying (empty, or already dried)
    private long lastCheck = -1;
    private final LazyOptional<IItemHandler> items = LazyOptional.of(Handler::new);

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
        java.util.Arrays.fill(stacks, ItemStack.EMPTY);
    }

    public static Optional<DryingRecipe> recipeFor(Level level, ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.DRYING.get(), new SimpleContainer(stack), level);
    }

    public ItemStack stack(int slot) {
        return stacks[slot];
    }

    public boolean isDrying(int slot) {
        return total[slot] > 0;
    }

    public boolean isDone(int slot) {
        return !stacks[slot].isEmpty() && total[slot] == 0;
    }

    public Weather weather() {
        if (level == null) return Weather.SHADE;
        BlockPos above = worldPosition.above();
        if (!level.canSeeSky(above)) return Weather.SHADE;
        if (level.isRaining() && level.getBiome(above).value().getPrecipitationAt(above) != Biome.Precipitation.NONE) return Weather.RAIN;
        return level.isDay() ? Weather.SUN : Weather.SHADE;
    }

    /** Hangs one item (with a drying recipe) in the first free spot. Returns false if it can't. */
    public boolean hang(ItemStack stack) {
        if (level == null || level.isClientSide) return false;
        int slot = firstFree();
        Optional<DryingRecipe> recipe = recipeFor(level, stack);
        if (slot < 0 || recipe.isEmpty()) return false;
        catchUp();
        stacks[slot] = stack.copyWithCount(1);
        progress[slot] = 0;
        total[slot] = ModConfigs.processTicks(recipe.get().time());
        if (!level.getBlockTicks().hasScheduledTick(worldPosition, getBlockState().getBlock())) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), CHECK_TICKS);
        }
        sync();
        return true;
    }

    /** Takes a dried item off (or, if {@code anything}, the last item hung, dried or not). */
    public ItemStack take(boolean anything) {
        catchUp();
        for (int slot = 0; slot < SLOTS; slot++) {
            if (isDone(slot)) return remove(slot);
        }
        if (anything) {
            for (int slot = SLOTS - 1; slot >= 0; slot--) {
                if (!stacks[slot].isEmpty()) return remove(slot);
            }
        }
        return ItemStack.EMPTY;
    }

    public int firstFree() {
        for (int slot = 0; slot < SLOTS; slot++) if (stacks[slot].isEmpty()) return slot;
        return -1;
    }

    public List<ItemStack> contents() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack stack : stacks) if (!stack.isEmpty()) list.add(stack);
        return list;
    }

    private ItemStack remove(int slot) {
        ItemStack out = stacks[slot];
        stacks[slot] = ItemStack.EMPTY;
        progress[slot] = 0;
        total[slot] = 0;
        sync();
        return out;
    }

    /** The scheduled check: add up drying, and check again later if anything is still drying. */
    public void check() {
        catchUp();
        if (level != null && anyDrying()) level.scheduleTick(worldPosition, getBlockState().getBlock(), CHECK_TICKS);
    }

    /** Adds the drying done since the last check at the current weather's rate; finishes what's dry. */
    private void catchUp() {
        if (level == null || level.isClientSide) return;
        long now = level.getGameTime();
        long elapsed = lastCheck < 0 ? 0 : now - lastCheck;
        lastCheck = now;
        if (elapsed <= 0 || !anyDrying()) return;
        int rate = weather().rate;
        boolean changed = false;
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!isDrying(slot) || rate == 0) continue;
            progress[slot] = (int) Math.min(total[slot], progress[slot] + elapsed * rate);
            changed = true;
            if (progress[slot] >= total[slot]) {
                ItemStack result = recipeFor(level, stacks[slot]).map(r -> r.result().copy()).orElse(stacks[slot]);
                stacks[slot] = result;
                total[slot] = 0;
                progress[slot] = 0;
            }
        }
        if (changed) sync();
    }

    private boolean anyDrying() {
        for (int slot = 0; slot < SLOTS; slot++) if (isDrying(slot)) return true;
        return false;
    }

    /** 0-100, counting time since the last check at the current rate (so read-outs don't lag). */
    public int percent(int slot) {
        if (!isDrying(slot)) return isDone(slot) ? 100 : 0;
        long extra = level == null || lastCheck < 0 ? 0 : (level.getGameTime() - lastCheck) * weather().rate;
        return (int) Math.min(99, (progress[slot] + extra) * 100 / total[slot]);
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = stacks[slot];
            if (stack.isEmpty()) continue;
            lines.add(isDone(slot)
                    ? Component.translatable("hydrometer.seedtocellar.dried", stack.getHoverName()).withStyle(ChatFormatting.GREEN)
                    : Component.translatable("hydrometer.seedtocellar.drying", stack.getHoverName(), percent(slot)));
        }
        if (lines.isEmpty()) {
            lines.add(Component.translatable("hydrometer.seedtocellar.rack_empty").withStyle(ChatFormatting.GRAY));
        } else if (anyDrying()) {
            Weather weather = weather();
            lines.add(Component.translatable("hydrometer.seedtocellar.rack_" + weather.name().toLowerCase(java.util.Locale.ROOT))
                    .withStyle(weather == Weather.RAIN ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        }
        return lines;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int slot = 0; slot < SLOTS; slot++) {
            if (stacks[slot].isEmpty()) continue;
            CompoundTag entry = stacks[slot].save(new CompoundTag());
            entry.putByte("Slot", (byte) slot);
            entry.putInt("Progress", progress[slot]);
            entry.putInt("Total", total[slot]);
            list.add(entry);
        }
        tag.put("Items", list);
        tag.putLong("LastCheck", lastCheck);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        java.util.Arrays.fill(stacks, ItemStack.EMPTY);
        java.util.Arrays.fill(progress, 0);
        java.util.Arrays.fill(total, 0);
        ListTag list = tag.getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot");
            if (slot < 0 || slot >= SLOTS) continue;
            stacks[slot] = ItemStack.of(entry);
            progress[slot] = entry.getInt("Progress");
            total[slot] = entry.getInt("Total");
        }
        lastCheck = tag.contains("LastCheck") ? tag.getLong("LastCheck") : -1;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? items.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        items.invalidate();
    }

    /** Hoppers hang dryable items in free spots and take only dried ones out. */
    private class Handler implements IItemHandler {
        @Override public int getSlots() { return SLOTS; }

        @Override public ItemStack getStackInSlot(int slot) { return stacks[slot]; }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            // Only into the next free spot, so the spots fill in order (as they do by hand).
            if (level == null || slot != firstFree() || recipeFor(level, stack).isEmpty()) return stack;
            if (!simulate) hang(stack);
            ItemStack rest = stack.copy();
            rest.shrink(1);
            return rest;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || !isDone(slot)) return ItemStack.EMPTY;
            return simulate ? stacks[slot].copy() : remove(slot);
        }

        @Override public int getSlotLimit(int slot) { return 1; }

        @Override public boolean isItemValid(int slot, ItemStack stack) { return level != null && recipeFor(level, stack).isPresent(); }
    }
}
