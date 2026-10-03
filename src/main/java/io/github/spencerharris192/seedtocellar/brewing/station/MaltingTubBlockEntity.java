package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

/**
 * Malting Tub logic. Timing is stored as the game time each phase ends, and a single
 * scheduled tick finishes it, so it works in unloaded chunks and costs nothing meanwhile.
 * Automation: pipes can add water; hoppers insert grain (while idle) and extract green malt.
 */
public class MaltingTubBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int CAPACITY = 1000;
    public static final int MAX_GRAIN = 16;
    private static final int INPUT = 0;
    private static final int OUTPUT = 1;

    public enum Phase { IDLE, STEEPING, SPROUTING }

    private final FluidTank tank = new FluidTank(CAPACITY, fluid -> fluid.getFluid().is(FluidTags.WATER)) {
        @Override
        protected void onContentsChanged() {
            changed();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            changed();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT ? MAX_GRAIN : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != INPUT || findRecipe(stack).isPresent();
        }
    };

    private Phase phase = Phase.IDLE;
    private long phaseEnd;
    private ResourceLocation recipeId;

    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(AutomationItems::new);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(AutomationFluids::new);

    public MaltingTubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MALTING_TUB.get(), pos, state);
    }

    // --- state ------------------------------------------------------------------------------

    public Phase phase() {
        return phase;
    }

    /** 0..1 progress through the current phase, for Jade and the Hydrometer. */
    public float phaseProgress() {
        if (phase == Phase.IDLE || level == null) return 0;
        Optional<MaltingRecipe> recipe = currentRecipe();
        if (recipe.isEmpty()) return 0;
        int length = ModConfigs.processTicks(phase == Phase.STEEPING ? recipe.get().steepTime() : recipe.get().sproutTime());
        return 1F - Math.max(0, phaseEnd - level.getGameTime()) / (float) length;
    }

    public FluidStack water() {
        return tank.getFluid();
    }

    public ItemStack input() {
        return items.getStackInSlot(INPUT);
    }

    public ItemStack output() {
        return items.getStackInSlot(OUTPUT);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            tryStart();
            updateBlockState();
        }
    }

    private Optional<MaltingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.MALTING.get(), new SimpleContainer(stack), level);
    }

    private Optional<MaltingRecipe> currentRecipe() {
        if (level == null || recipeId == null) return Optional.empty();
        return level.getRecipeManager().byKey(recipeId).filter(MaltingRecipe.class::isInstance).map(MaltingRecipe.class::cast);
    }

    private void tryStart() {
        if (phase != Phase.IDLE || !output().isEmpty() || tank.getFluidAmount() < CAPACITY || input().isEmpty()) return;
        findRecipe(input()).ifPresent(recipe -> {
            phase = Phase.STEEPING;
            recipeId = recipe.getId();
            phaseEnd = level.getGameTime() + ModConfigs.processTicks(recipe.steepTime());
            scheduleCheck();
            level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6F, 1.2F);
        });
    }

    /** Moves through any phases whose end time has passed. Called by the scheduled tick and on use. */
    public void advance() {
        if (level == null || level.isClientSide || phase == Phase.IDLE) return;
        Optional<MaltingRecipe> recipe = currentRecipe();
        if (recipe.isEmpty()) { // recipe removed by a datapack: give the grain back
            phase = Phase.IDLE;
            changed();
            return;
        }
        long now = level.getGameTime();
        boolean moved = false;
        while (phase != Phase.IDLE && now >= phaseEnd) {
            moved = true;
            if (phase == Phase.STEEPING) {
                tank.setFluid(FluidStack.EMPTY); // the steep water drains away
                phase = Phase.SPROUTING;
                phaseEnd += ModConfigs.processTicks(recipe.get().sproutTime());
            } else {
                ItemStack result = recipe.get().result().copy();
                result.setCount(result.getCount() * input().getCount());
                phase = Phase.IDLE;
                recipeId = null;
                items.setStackInSlot(INPUT, ItemStack.EMPTY);
                items.setStackInSlot(OUTPUT, result);
            }
        }
        if (phase != Phase.IDLE) scheduleCheck();
        if (moved) changed();
    }

    private void scheduleCheck() {
        long delay = Math.max(1, Math.min(Integer.MAX_VALUE, phaseEnd - level.getGameTime()));
        level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) delay);
    }

    private void updateBlockState() {
        MaltingTubBlock.Contents contents;
        if (phase == Phase.STEEPING) contents = MaltingTubBlock.Contents.STEEPING;
        else if (phase == Phase.SPROUTING) contents = MaltingTubBlock.Contents.SPROUTING;
        else if (!output().isEmpty()) contents = MaltingTubBlock.Contents.DONE;
        else if (!input().isEmpty()) contents = MaltingTubBlock.Contents.GRAIN;
        else if (!tank.isEmpty()) contents = MaltingTubBlock.Contents.WATER;
        else contents = MaltingTubBlock.Contents.EMPTY;
        BlockState state = getBlockState();
        if (state.getValue(MaltingTubBlock.CONTENTS) != contents) {
            level.setBlock(worldPosition, state.setValue(MaltingTubBlock.CONTENTS, contents), Block.UPDATE_ALL);
        }
    }

    // --- player interaction ----------------------------------------------------------------

    public InteractionResult onUse(Player player, InteractionHand hand, BlockHitResult hit) {
        boolean client = level.isClientSide;
        if (!client) advance();
        ItemStack held = player.getItemInHand(hand);

        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (phase != Phase.IDLE) return InteractionResult.PASS;
            if (!client) FluidUtil.interactWithFluidHandler(player, hand, tank);
            return InteractionResult.sidedSuccess(client);
        }
        if (!held.isEmpty() && phase == Phase.IDLE && output().isEmpty() && findRecipe(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (remainder.getCount() != held.getCount()) {
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, remainder);
                    level.playSound(null, worldPosition, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 0.8F);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (held.isEmpty()) {
            if (!client) {
                if (!output().isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(OUTPUT, 64, false));
                } else if (phase == Phase.IDLE && !input().isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, items.extractItem(INPUT, 64, false));
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        return InteractionResult.PASS;
    }

    @Override
    public List<Component> hydrometerLines() {
        advance();
        int percent = Math.round(phaseProgress() * 100);
        String h = "hydrometer.seedtocellar.";
        if (phase == Phase.STEEPING) return List.of(Component.translatable(h + "steeping", percent));
        if (phase == Phase.SPROUTING) return List.of(Component.translatable(h + "sprouting", percent));
        if (!output().isEmpty()) return List.of(Component.translatable(h + "ready", output().getHoverName()));
        if (!input().isEmpty()) return List.of(Component.translatable(h + "tub.add_water"));
        if (!tank.isEmpty()) return List.of(Component.translatable(h + "tub.add_grain"));
        return List.of(Component.translatable(h + "empty"));
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
        tag.putString("Phase", phase.name());
        tag.putLong("PhaseEnd", phaseEnd);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
        items.deserializeNBT(tag.getCompound("Items"));
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (IllegalArgumentException e) {
            phase = Phase.IDLE;
        }
        phaseEnd = tag.getLong("PhaseEnd");
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
    }

    // --- automation ------------------------------------------------------------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

    /** Hoppers: grain in (only while idle and empty of malt), green malt out. */
    private class AutomationItems implements IItemHandler {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != INPUT || phase != Phase.IDLE || !output().isEmpty()) return stack;
            return items.insertItem(INPUT, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != OUTPUT) return ItemStack.EMPTY;
            advance();
            return items.extractItem(OUTPUT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT && items.isItemValid(slot, stack);
        }
    }

    /** Pipes: water in or out, but never while a batch is steeping. */
    private class AutomationFluids implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return phase == Phase.IDLE ? tank.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return phase == Phase.IDLE ? tank.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return phase == Phase.IDLE ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }
}
