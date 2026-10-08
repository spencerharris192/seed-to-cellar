package io.github.spencerharris192.seedtocellar.brewing.station;

import org.jspecify.annotations.Nullable;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;

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

    private final StationTank tank = new StationTank(CAPACITY, fluid -> fluid.getFluid().is(FluidTags.WATER)) {
        @Override
        protected void onContentsChanged() {
            changed();
        }
    };

    private final StationItems items = new StationItems(2) {
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
    private Identifier recipeId;

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
        if (level != null && !level.isClientSide()) {
            tryStart();
            updateBlockState();
        }
    }

    private Optional<RecipeHolder<MaltingRecipe>> findRecipe(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return Recipes.find(level, ModRecipes.MALTING.get(), new SingleRecipeInput(stack));
    }

    private Optional<MaltingRecipe> currentRecipe() {
        return Recipes.byId(level, recipeId, MaltingRecipe.class);
    }

    private void tryStart() {
        if (phase != Phase.IDLE || !output().isEmpty() || tank.getFluidAmount() < CAPACITY || input().isEmpty()) return;
        findRecipe(input()).ifPresent(holder -> {
            phase = Phase.STEEPING;
            recipeId = holder.id().identifier();
            phaseEnd = level.getGameTime() + ModConfigs.processTicks(holder.value().steepTime());
            scheduleCheck();
            level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6F, 1.2F);
        });
    }

    /** Moves through any phases whose end time has passed. Called by the scheduled tick and on use. */
    public void advance() {
        if (level == null || level.isClientSide() || phase == Phase.IDLE) return;
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
                ItemStack result = recipe.get().result();
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
        boolean client = level.isClientSide();
        if (!client) advance();
        ItemStack held = player.getItemInHand(hand);

        if (holdsLiquidContainer(player, hand)) {
            if (phase != Phase.IDLE) return InteractionResult.PASS;
            if (!client) pourWith(player, hand, tank);
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty() && phase == Phase.IDLE && output().isEmpty() && findRecipe(held).isPresent()) {
            if (!client) {
                ItemStack remainder = items.insertItem(INPUT, held.copy(), false);
                if (remainder.getCount() != held.getCount()) {
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, remainder);
                    level.playSound(null, worldPosition, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 0.8F);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            if (!client) {
                if (!output().isEmpty()) {
                    give(player, items.extractItem(OUTPUT, 64, false));
                } else if (phase == Phase.IDLE && !input().isEmpty()) {
                    give(player, items.extractItem(INPUT, 64, false));
                }
            }
            return InteractionResult.SUCCESS;
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("Tank"));
        items.serialize(output.child("Items"));
        output.putString("Phase", phase.name());
        output.putLong("PhaseEnd", phaseEnd);
        if (recipeId != null) output.putString("Recipe", recipeId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
        items.deserialize(input.childOrEmpty("Items"));
        try {
            phase = Phase.valueOf(input.getStringOr("Phase", ""));
        } catch (IllegalArgumentException e) {
            phase = Phase.IDLE;
        }
        phaseEnd = input.getLongOr("PhaseEnd", 0L);
        recipeId = input.getString("Recipe").map(Identifier::tryParse).orElse(null);
    }

    // --- automation ------------------------------------------------------------------------

    /** Hoppers: grain in (only while idle and empty of malt), green malt out. */
    private final ResourceHandler<ItemResource> automationItems = new DelegatingResourceHandler<>(items) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == INPUT && super.isValid(index, resource);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != INPUT || phase != Phase.IDLE || !output().isEmpty()) return 0;
            return super.insert(index, resource, amount, transaction);
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            return insert(INPUT, resource, amount, transaction);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != OUTPUT) return 0;
            advance();
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            return extract(OUTPUT, resource, amount, transaction);
        }
    };

    /** Pipes: water in or out, but never while a batch is steeping. */
    private final ResourceHandler<FluidResource> automationFluids = new DelegatingResourceHandler<>(tank) {
        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return phase == Phase.IDLE ? super.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return phase == Phase.IDLE ? super.extract(index, resource, amount, transaction) : 0;
        }
    };

    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return automationItems;
    }

    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return automationFluids;
    }
}
