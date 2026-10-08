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
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

/**
 * Preserving Jar logic: 4 item slots + 1 bucket of liquid. Closing the lid on contents that
 * match a jar recipe starts it; when time is up the contents turn into the result.
 * <p>
 * Vinegar (GDD section 8): beer (any liquid tagged {@code seedtocellar:sours_to_vinegar}) left in
 * an <i>open</i> jar with nothing else in it sours into vinegar over two days, and a Mother of
 * Vinegar forms. With a mother already in the jar it takes one day, and the mother grows another.
 * <p>
 * Uses the fermentation time multiplier. Timestamp-based like the vat: a start/end time plus a
 * scheduled block tick, never per-tick work.
 */
public class PreservingJarBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int CAPACITY = 1000;
    public static final int SLOTS = 4;
    public static final int SLOT_LIMIT = 16;
    public static final int SOUR_TICKS = 48000;
    public static final int SOUR_TICKS_WITH_MOTHER = 24000;

    private final StationTank tank = new StationTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
            updateSouring();
        }
    };

    private final StationItems items = new StationItems(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
            updateSouring();
        }

        @Override
        public int getSlotLimit(int slot) {
            return SLOT_LIMIT;
        }
    };

    private boolean working;
    private long end;
    private long souringEnd = -1;   // when the open beer turns to vinegar, or -1
    private Identifier recipeId;

    public PreservingJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESERVING_JAR.get(), pos, state);
    }

    public StationTank tank() {
        return tank;
    }

    public StationItems items() {
        return items;
    }

    public boolean isWorking() {
        return working;
    }

    public boolean isOpen() {
        return getBlockState().getValue(PreservingJarBlock.OPEN);
    }

    public long ticksLeft() {
        return working && level != null ? Math.max(0, end - level.getGameTime()) : 0;
    }

    public InteractionResult onUse(Player player, InteractionHand hand) {
        boolean client = level.isClientSide();
        if (!client) advance();
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            if (!client) {
                if (player.isShiftKeyDown()) takeItems(player);
                else setLid(!isOpen(), player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!isOpen() || working) {
            if (!client) player.sendOverlayMessage(Component.translatable("message.seedtocellar.jar.closed").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS;
        }
        if (holdsLiquidContainer(player, hand)) {
            if (!client) pourWith(player, hand, tank);
            return InteractionResult.SUCCESS;
        }
        if (!client) {
            ItemStack remainder = items.insertItemStacked(held.copyWithCount(1), false);
            if (remainder.isEmpty() && !player.getAbilities().instabuild) held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private void takeItems(Player player) {
        if (working) return;
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.getStackInSlot(i).isEmpty()) {
                give(player, items.extractItem(i, SLOT_LIMIT, false));
                return;
            }
        }
    }

    /** The recipe the contents match, whatever the temperature. */
    private Optional<JarRecipe> matchingRecipe() {
        return matchingHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<JarRecipe>> matchingHolder() {
        if (level == null) return Optional.empty();
        return Recipes.holders(level, ModRecipes.JAR.get()).filter(h -> h.value().matches(items, tank.getFluid())).findFirst();
    }

    /** The recipe that can start here: the contents match, and the jar stands at a temperature it works at. */
    private Optional<RecipeHolder<JarRecipe>> findRecipe() {
        if (level == null) return Optional.empty();
        io.github.spencerharris192.seedtocellar.brewing.Temperature here = io.github.spencerharris192.seedtocellar.brewing.Temperature.at(level, worldPosition);
        return matchingHolder().filter(h -> h.value().suits(here));
    }

    public void setLid(boolean open, Player player) {
        if (level == null || isOpen() == open) return;
        if (open && working) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.jar.working").withStyle(ChatFormatting.YELLOW));
            }
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(PreservingJarBlock.OPEN, open), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.5F, open ? 1.3F : 0.9F);
        updateSouring();
        if (!open) {
            matchingRecipe().filter(r -> findRecipe().isEmpty()).ifPresent(r -> {
                if (player != null) {
                    player.sendOverlayMessage(Component.translatable("message.seedtocellar.jar.temperature", r.temperatures().get(0).displayName(),
                            io.github.spencerharris192.seedtocellar.brewing.Temperature.at(level, worldPosition).displayName())
                            .withStyle(ChatFormatting.YELLOW));
                }
            });
            findRecipe().ifPresent(holder -> {
                JarRecipe recipe = holder.value();
                working = true;
                recipeId = holder.id().identifier();
                long ticks = Math.max(1, Math.round(recipe.time() * ModConfigs.SERVER.fermentationTimeMultiplier.get()));
                end = level.getGameTime() + ticks;
                level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) Math.min(Integer.MAX_VALUE, ticks));
                setChanged();
            });
        }
    }

    // --- vinegar ---------------------------------------------------------------------------

    /** Open, holding beer, and nothing in it but (optionally) Mother of Vinegar. */
    private boolean canSour() {
        if (level == null || working || !isOpen()) return false;
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty() || !fluid.getFluid().is(ModTags.Fluids.SOURS_TO_VINEGAR)) return false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && !stack.is(ModItems.MOTHER_OF_VINEGAR.get())) return false;
        }
        return true;
    }

    private boolean hasMother() {
        for (int i = 0; i < SLOTS; i++) if (items.getStackInSlot(i).is(ModItems.MOTHER_OF_VINEGAR.get())) return true;
        return false;
    }

    public boolean isSouring() {
        return souringEnd >= 0;
    }

    /** Starts the souring clock when the jar is set up for it; stops it when it isn't any more. */
    private void updateSouring() {
        if (level == null || level.isClientSide()) return;
        if (!canSour()) {
            souringEnd = -1;
            return;
        }
        if (souringEnd >= 0) return;
        long ticks = Math.max(1, Math.round((hasMother() ? SOUR_TICKS_WITH_MOTHER : SOUR_TICKS)
                * ModConfigs.SERVER.fermentationTimeMultiplier.get()));
        souringEnd = level.getGameTime() + ticks;
        level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) Math.min(Integer.MAX_VALUE, ticks));
        setChanged();
    }

    private void finishSouring() {
        souringEnd = -1;
        tank.setFluid(new FluidStack(ModFluids.VINEGAR.get(), tank.getFluidAmount()));
        items.insertItemStacked(new ItemStack(ModItems.MOTHER_OF_VINEGAR.get()), false);   // one forms (or grows)
        level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4F, 0.7F);
        sync();
    }

    /** Finishes the recipe (or the souring) when its time is up. */
    public void advance() {
        if (level == null || level.isClientSide()) return;
        if (souringEnd >= 0 && level.getGameTime() >= souringEnd) {
            if (canSour()) finishSouring();
            else souringEnd = -1;
        }
        if (!working) return;
        if (level.getGameTime() < end) return;
        working = false;
        Optional<JarRecipe> recipe = Recipes.byId(level, recipeId, JarRecipe.class);
        recipeId = null;
        if (recipe.isPresent() && recipe.get().matches(items, tank.getFluid())) {
            JarRecipe r = recipe.get();
            List<ItemStack> leftovers = new java.util.ArrayList<>();   // honey or syrup bottles come back
            for (int i = 0; i < SLOTS; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (stack.getItem().getCraftingRemainder() != null) leftovers.add(stack.getItem().getCraftingRemainder().create().copyWithCount(stack.getCount()));
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
            if (r.resultFluid() != null) {
                // The whole liquid steeps into the result, keeping its quality (a liqueur keeps its spirit's stars).
                FluidStack in = tank.getFluid();
                CompoundTag kept = new CompoundTag();
                if (io.github.spencerharris192.seedtocellar.brewing.BrewQuality.has(in)) kept.put(io.github.spencerharris192.seedtocellar.brewing.BrewQuality.TAG, BrewData.get(in).getCompoundOrEmpty(io.github.spencerharris192.seedtocellar.brewing.BrewQuality.TAG));
                // The secret: only a perfect spirit wears the crown (anything less steeps as the plain recipe would).
                var quality = io.github.spencerharris192.seedtocellar.brewing.BrewQuality.of(in);
                if (r.crowns() && quality.stars() == io.github.spencerharris192.seedtocellar.brewing.BrewQuality.PERFECT) {
                    kept.put(io.github.spencerharris192.seedtocellar.brewing.BrewQuality.TAG, quality.withCrowned(true).save());
                }
                FluidStack steeped = new FluidStack(r.resultFluid(), in.getAmount());
                BrewData.set(steeped, kept);
                tank.setFluid(steeped);
            } else {
                items.setStackInSlot(0, r.result());
                if (r.liquid() != null) tank.drain(r.fluidAmount(), StationTank.Action.EXECUTE);
            }
            for (ItemStack leftover : leftovers) items.insertItemStacked(leftover, false);
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5F, 1.4F);
        }
        sync();
    }

    @Override
    public List<Component> hydrometerLines() {
        advance();
        String h = "hydrometer.seedtocellar.";
        if (working) return List.of(Component.translatable(h + "jar.working", Math.max(1, ticksLeft() / 1200)));
        if (souringEnd >= 0 && level != null) {
            return List.of(Component.translatable(h + "jar.souring", Math.max(1, (souringEnd - level.getGameTime()) / 1200)));
        }
        if (!isOpen()) return List.of(Component.translatable(h + "jar.closed"));
        return List.of(Component.translatable(h + "jar.open"));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) return;
        for (int i = 0; i < SLOTS; i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("Tank"));
        items.serialize(output.child("Items"));
        output.putBoolean("Working", working);
        output.putLong("End", end);
        output.putLong("SouringEnd", souringEnd);
        if (recipeId != null) output.putString("Recipe", recipeId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
        items.deserialize(input.childOrEmpty("Items"));
        working = input.getBooleanOr("Working", false);
        end = input.getLongOr("End", 0L);
        souringEnd = input.getLongOr("SouringEnd", -1L);
        recipeId = input.getString("Recipe").map(Identifier::tryParse).orElse(null);
    }

    /** Hoppers and pipes reach the slots and the liquid only while the lid is up (not while it's working). */
    private final ResourceHandler<ItemResource> automationItems = new DelegatingResourceHandler<>(items) {
        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return working ? 0 : super.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return working ? 0 : super.extract(index, resource, amount, transaction);
        }
    };

    private final ResourceHandler<FluidResource> automationFluids = new DelegatingResourceHandler<>(tank) {
        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return working ? 0 : super.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return working ? 0 : super.extract(index, resource, amount, transaction);
        }
    };

    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return automationItems;
    }

    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return automationFluids;
    }

}
