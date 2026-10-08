package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Compost Bin logic (GDD section 7). Holds up to {@link #CAPACITY} organic leftovers; once
 * full it composts for one in-game day (stored as a start time, finished by a scheduled
 * block tick, so it keeps working in unloaded chunks), then holds {@link #YIELD} Compost.
 * Hoppers put leftovers in from above or the sides and take Compost out from below.
 */
public class CompostBinBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int CAPACITY = 16;
    public static final int YIELD = 4;
    public static final int BASE_TICKS = 24000;

    private int count;
    private long startTime = -1;

    public CompostBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPOST_BIN.get(), pos, state);
    }

    /** What the bin takes: anything the vanilla composter takes, plus our compostables tag (spent grain, rotten flesh...). */
    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && (stack.has(DataComponents.COMPOSTABLE) || stack.is(ModTags.Items.COMPOSTABLES));
    }

    public static int duration() {
        return ModConfigs.processTicks(BASE_TICKS);
    }

    public int count() {
        return count;
    }

    public boolean isComposting() {
        return startTime >= 0 && !isReady();
    }

    /** Done composting, and the compost not yet taken. */
    public boolean isReady() {
        return startTime >= 0 && getBlockState().getValue(CompostBinBlock.READY);
    }

    private int room() {
        return level == null || startTime >= 0 ? 0 : CAPACITY - count;
    }

    /** Puts up to `amount` leftovers in; returns how many it took. */
    public int add(int amount, boolean simulate) {
        int taken = Math.max(0, Math.min(amount, room()));
        if (taken <= 0 || simulate) return taken;
        long startBefore = startTime;
        fill(taken);
        applyState(startBefore);
        return taken;
    }

    private void fill(int taken) {
        count += taken;
        if (count >= CAPACITY) startTime = level.getGameTime();
    }

    /** Called by the block's scheduled tick: done composting? */
    public void checkFinished() {
        if (level != null && startTime >= 0 && !isReady() && level.getGameTime() - startTime >= duration()) {
            level.setBlock(worldPosition, getBlockState().setValue(CompostBinBlock.READY, true), Block.UPDATE_CLIENTS);
            sync();
        } else if (level != null && startTime >= 0 && !isReady()) {
            // Woken early (e.g. the time setting changed): check again when it should be done.
            level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) Math.max(1, duration() - (level.getGameTime() - startTime)));
        }
    }

    /** Takes the finished compost out (empty if not ready) and resets the bin. */
    public ItemStack takeCompost() {
        if (level == null || !isReady()) return ItemStack.EMPTY;
        long startBefore = startTime;
        count = 0;
        startTime = -1;
        applyState(startBefore);
        return new ItemStack(ModItems.COMPOST.get(), YIELD);
    }

    /** Brings the blockstate (fill level, ready) in line with the contents, and starts the composting timer. */
    private void applyState(long startBefore) {
        if (level == null) return;
        BlockState state = getBlockState();
        BlockState next = state.setValue(CompostBinBlock.LEVEL, levelFor(count));
        if (startTime < 0) next = next.setValue(CompostBinBlock.READY, false);
        if (next != state) level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        if (startTime >= 0 && startBefore < 0) level.scheduleTick(worldPosition, next.getBlock(), duration());
        sync();
    }

    /** Broken, a bin of finished compost gives it up (compost still rotting is lost). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && isReady()) Block.popResource(level, pos, new ItemStack(ModItems.COMPOST.get(), YIELD));
    }

    /** Blockstate fill level 0-4 for a count of leftovers. */
    public static int levelFor(int count) {
        return count <= 0 ? 0 : Math.min(CompostBinBlock.MAX_LEVEL, 1 + (count - 1) * CompostBinBlock.MAX_LEVEL / CAPACITY);
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        if (isReady()) {
            lines.add(Component.translatable("hydrometer.seedtocellar.compost_ready", YIELD));
        } else if (startTime >= 0 && level != null) {
            long pct = Math.min(99, (level.getGameTime() - startTime) * 100 / duration());
            lines.add(Component.translatable("hydrometer.seedtocellar.composting", pct));
        } else {
            lines.add(Component.translatable("hydrometer.seedtocellar.compost_filling", count, CAPACITY).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Count", count);
        output.putLong("StartTime", startTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.getIntOr("Count", 0);
        startTime = input.getLongOr("StartTime", -1L);
    }

    // --- automation ------------------------------------------------------------------------

    private record Snapshot(int count, long startTime) {}

    /** Hoppers' changes, held until their transaction commits (then the blockstate and timer follow). */
    private final SnapshotJournal<Snapshot> journal = new SnapshotJournal<>() {
        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(count, startTime);
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            count = snapshot.count();
            startTime = snapshot.startTime();
        }

        @Override
        protected void onRootCommit(Snapshot original) {
            applyState(original.startTime());
        }
    };

    /** Index 0 takes leftovers in; index 1 gives the compost out when it's ready. */
    private final ResourceHandler<ItemResource> handler = new ResourceHandler<>() {
        @Override public int size() { return 2; }

        @Override
        public ItemResource getResource(int index) {
            return index == 1 && isReady() ? ItemResource.of(ModItems.COMPOST.get()) : ItemResource.EMPTY;
        }

        @Override public long getAmountAsLong(int index) { return index == 1 && isReady() ? YIELD : 0; }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return index == 0 ? CAPACITY : YIELD; }
        @Override public boolean isValid(int index, ItemResource resource) { return index == 0 && accepts(resource.toStack()); }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || !accepts(resource.toStack())) return 0;
            int taken = Math.min(amount, room());
            if (taken <= 0) return 0;
            journal.updateSnapshots(transaction);
            fill(taken);
            return taken;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 1 || amount < YIELD || !isReady() || !resource.is(ModItems.COMPOST.get())) return 0;
            journal.updateSnapshots(transaction);
            count = 0;
            startTime = -1;
            return YIELD;
        }
    };

    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return handler;
    }
}
