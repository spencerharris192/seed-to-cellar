package io.github.spencerharris192.seedtocellar.distillery;

import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pot Still logic (GDD sections 7, 9.4). Three tanks: the pot (what you distil), the receiver (the spirit, which runs into
 * the glass spirit safe) and the stillage (what's left in the pot). Over heat, a run boils the whole pot: half comes over as
 * spirit, half is left as stillage. A wash or wine comes out as its spirit after one run, without the craft star; run it
 * again ("Run again" pours the receiver back into the pot) for the star and half the volume again. A charcoal filter in the
 * still makes grain or potato spirit into vodka. The spirit keeps the yeast and temperature stars its wash earned.
 * <p>
 * Automation: any side but the bottom fills the pot and drains the receiver; the bottom drains the stillage. Hoppers put
 * charcoal in the filter from the sides. The pot is locked while a run is under way.
 */
public class PotStillBlockEntity extends SyncedBlockEntity implements MenuProvider, HydrometerReadable {
    public static final int CAPACITY = 4000;
    public static final int FILTER = 0;
    public static final int BASKET = 1;
    public static final int BASKET_SLOTS = 4;
    public static final int SLOTS = BASKET + BASKET_SLOTS;
    /** Run time per bucket in the pot (20 s), before the process-speed setting; never under 5 s. */
    public static final int TICKS_PER_BUCKET = 400;
    public static final int MIN_TICKS = 100;

    private final FluidTank pot = new FluidTank(CAPACITY, this::isDistillable) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private final FluidTank receiver = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private final FluidTank stillage = new FluidTank(CAPACITY, stack -> stack.getFluid() == ModFluids.STILLAGE.get()) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == FILTER) return stack.is(ModTags.Items.FILTER_CHARCOAL);
            return hasBasket() && stack.is(ModTags.Items.BOTANICALS);
        }
    };

    private boolean running;
    private int progress;
    private int total;
    private boolean heated;
    @Nullable private ResourceLocation recipeId;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> total;
                case 2 -> heated ? 1 : 0;
                case 3 -> running ? 1 : 0;
                default -> hasBasket() ? 1 : 0;
            };
        }

        @Override
        public void set(int i, int value) {
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private final LazyOptional<IItemHandler> filterCap = LazyOptional.of(() -> new RangedWrapper(items, FILTER, FILTER + 1));
    private final LazyOptional<IItemHandler> basketCap = LazyOptional.of(() -> new RangedWrapper(items, BASKET, SLOTS));
    private final LazyOptional<IFluidHandler> pipesCap = LazyOptional.of(() -> new Pipes(false));
    private final LazyOptional<IFluidHandler> stillageCap = LazyOptional.of(() -> new Pipes(true));

    public PotStillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_STILL.get(), pos, state);
    }

    // --- queries ---------------------------------------------------------------------------

    public FluidTank pot() { return pot; }
    public FluidTank receiver() { return receiver; }
    public FluidTank stillage() { return stillage; }
    public ItemStackHandler items() { return items; }
    public boolean isRunning() { return running; }
    public boolean isHeated() { return heated; }
    public float progress() { return total == 0 ? 0 : progress / (float) total; }

    /** A Gin Basket is fitted on the swan neck: its four slots hold botanicals the vapor runs through. */
    public boolean hasBasket() {
        return getBlockState().hasProperty(PotStillBlock.BASKET) && getBlockState().getValue(PotStillBlock.BASKET);
    }

    /** Fits a Gin Basket (on both halves, so the head shows it). False if one is already fitted. */
    public boolean fitBasket() {
        if (level == null || hasBasket()) return false;
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(PotStillBlock.BASKET, true), Block.UPDATE_ALL);
        BlockState head = level.getBlockState(worldPosition.above());
        if (head.is(state.getBlock())) level.setBlock(worldPosition.above(), head.setValue(PotStillBlock.BASKET, true), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1F, 1.2F);
        return true;
    }

    private List<DistillingRecipe> recipes() {
        return level == null ? List.of() : level.getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLING.get());
    }

    /** Something some recipe distils (a wash, a wine, a spirit). */
    private boolean isDistillable(FluidStack stack) {
        FluidStack probe = new FluidStack(stack.getFluid(), Math.max(1, stack.getAmount()), stack.getTag());
        return recipes().stream().anyMatch(r -> r.input().test(probe));
    }

    private boolean filterPresent() {
        return !items.getStackInSlot(FILTER).isEmpty();
    }

    private List<ItemStack> basketStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        if (hasBasket()) for (int i = BASKET; i < SLOTS; i++) stacks.add(items.getStackInSlot(i));
        return stacks;
    }

    /** The recipe the pot would run with now (client and server). */
    public Optional<DistillingRecipe> expectedRecipe() {
        if (pot.isEmpty()) return Optional.empty();
        boolean filter = filterPresent();
        List<ItemStack> basket = basketStacks();
        return recipes().stream().filter(r -> r.matches(pot.getFluid(), filter, basket))
                .max(Comparator.comparingInt(DistillingRecipe::priority));
    }

    private Optional<DistillingRecipe> currentRecipe() {
        if (level == null || recipeId == null) return Optional.empty();
        return level.getRecipeManager().byKey(recipeId).filter(DistillingRecipe.class::isInstance).map(DistillingRecipe.class::cast);
    }

    /** What a run of the pot with {@code recipe} would put in the receiver: half the volume, one more run, its stars. */
    public FluidStack output(DistillingRecipe recipe) {
        FluidStack in = pot.getFluid();
        CompoundTag before = in.getTag();
        CompoundTag tag = new CompoundTag();
        tag.putInt(CraftStep.RUNS, DistillingRecipe.runs(in) + 1);
        if (recipe.filter() || before != null && before.getBoolean(CraftStep.FILTERED)) tag.putBoolean(CraftStep.FILTERED, true);
        if (recipe.basket() != null) tag.putInt(CraftStep.BOTANICAL_COUNT, DistillingRecipe.Basket.distinct(basketStacks()));
        FluidStack out = new FluidStack(recipe.resultFor(in), in.getAmount() / 2, tag);
        boolean craft = Drinks.byFluid(out.getFluid()).map(d -> d.profile().craft().earned(tag)).orElse(false);
        BrewQuality wash = BrewQuality.of(in);
        return new BrewQuality(wash.yeast(), wash.temperature(), craft, false).applyTo(out);
    }

    /** Why the pot can't run with {@code recipe} right now, or null if it can. */
    @Nullable
    public Component blocked(DistillingRecipe recipe) {
        String k = "gui.seedtocellar.still.";
        FluidStack out = output(recipe);
        if (out.isEmpty()) return Component.translatable(k + "too_little");
        if (receiver.fill(out, IFluidHandler.FluidAction.SIMULATE) < out.getAmount()) {
            return Component.translatable(receiver.isEmpty() || receiver.getFluid().isFluidEqual(out) ? k + "receiver_full" : k + "receiver_other");
        }
        int left = pot.getFluidAmount() - out.getAmount();
        if (stillage.fill(new FluidStack(ModFluids.STILLAGE.get(), left), IFluidHandler.FluidAction.SIMULATE) < left) {
            return Component.translatable(k + "stillage_full");
        }
        return null;
    }

    // --- running ---------------------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, PotStillBlockEntity still) {
        still.heated = Temperature.isHeatSource(level.getBlockState(pos.below()));
        if (still.running) {
            Optional<DistillingRecipe> recipe = still.currentRecipe();
            if (recipe.isEmpty() || !recipe.get().matches(still.pot.getFluid(), still.filterPresent(), still.basketStacks())
                    || still.blocked(recipe.get()) != null) {
                still.stop();
            } else if (still.heated && ++still.progress >= still.total) {
                still.finish(recipe.get());
            } else {
                still.setChanged();
            }
        } else if (still.heated) {
            still.expectedRecipe().filter(r -> still.blocked(r) == null).ifPresent(still::begin);
        }
        boolean active = still.running && still.heated;
        if (state.getValue(PotStillBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(PotStillBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private void begin(DistillingRecipe recipe) {
        running = true;
        recipeId = recipe.getId();
        progress = 0;
        int base = Math.max(MIN_TICKS, TICKS_PER_BUCKET * pot.getFluidAmount() / 1000);
        total = ModConfigs.processTicks(base);
        sync();
    }

    private void stop() {
        running = false;
        recipeId = null;
        progress = 0;
        sync();
    }

    private void finish(DistillingRecipe recipe) {
        FluidStack out = output(recipe);
        int left = pot.getFluidAmount() - out.getAmount();
        if (recipe.filter()) items.extractItem(FILTER, 1, false);
        if (recipe.basket() != null) {
            for (int i = BASKET; i < SLOTS; i++) items.extractItem(i, 1, false);   // each botanical gives up one
        }
        running = false;
        recipeId = null;
        progress = 0;
        pot.setFluid(FluidStack.EMPTY);
        receiver.fill(out, IFluidHandler.FluidAction.EXECUTE);
        stillage.fill(new FluidStack(ModFluids.STILLAGE.get(), left), IFluidHandler.FluidAction.EXECUTE);
        if (level != null) level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 0.7F);
        sync();
    }

    /** "Run again": pours the receiver back into an empty pot, to distil it once more. */
    public boolean runAgain() {
        if (running || !pot.isEmpty() || receiver.isEmpty()) return false;
        pot.setFluid(receiver.getFluid().copy());
        receiver.setFluid(FluidStack.EMPTY);
        if (level != null) level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6F, 1.1F);
        sync();
        return true;
    }

    // --- player actions --------------------------------------------------------------------

    /** Buckets and bottles: full ones pour into the pot, empty ones take the spirit (then the stillage). A Gin Basket fits. */
    public boolean useHeldItem(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(io.github.spencerharris192.seedtocellar.registry.ModItems.GIN_BASKET.get()) && !hasBasket()) {
            if (level != null && !level.isClientSide && fitBasket() && !player.getAbilities().instabuild) held.shrink(1);
            return true;
        }
        if (!FluidUtil.getFluidHandler(held).isPresent()) return false;
        if (level != null && !level.isClientSide) {
            boolean full = FluidUtil.getFluidContained(held).isPresent();
            if (full && running) {
                player.displayClientMessage(Component.translatable("message.seedtocellar.still.busy").withStyle(ChatFormatting.YELLOW), true);
            } else if (!FluidUtil.interactWithFluidHandler(player, hand, new Pipes(false)) && full) {
                player.displayClientMessage(Component.translatable("message.seedtocellar.still.wont_distil").withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return true;
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        String k = "gui.seedtocellar.still.";
        FluidStack in = pot.getFluid();
        lines.add(in.isEmpty() ? Component.translatable(k + "pot_empty")
                : Component.translatable(k + "pot", in.getDisplayName(), in.getAmount()));
        Optional<DistillingRecipe> recipe = running ? currentRecipe() : expectedRecipe();
        recipe.ifPresent(r -> lines.add(makesLine(r)));
        if (!heated) lines.add(Component.translatable(k + "no_heat").withStyle(ChatFormatting.RED));
        else if (running) lines.add(Component.translatable(h + "progress", Math.round(progress() * 100)));
        if (!receiver.isEmpty()) {
            FluidStack out = receiver.getFluid();
            lines.add(Component.translatable(k + "receiver", out.getDisplayName(), out.getAmount()));
            lines.add(DrinkItem.stars(BrewQuality.of(out).stars()));
        }
        if (!stillage.isEmpty()) lines.add(Component.translatable(k + "stillage", stillage.getFluidAmount()).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    /** "Makes: Malt Whiskey (run 1)". */
    public Component makesLine(DistillingRecipe recipe) {
        FluidStack out = new FluidStack(recipe.resultFor(pot.getFluid()), 1);
        return Component.translatable("gui.seedtocellar.still.makes", out.getDisplayName(), DistillingRecipe.runs(pot.getFluid()) + 1);
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    // --- menu ------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PotStillMenu(id, inventory, this, items, data);
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Pot", pot.writeToNBT(new CompoundTag()));
        tag.put("Receiver", receiver.writeToNBT(new CompoundTag()));
        tag.put("Stillage", stillage.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("Running", running);
        tag.putInt("Progress", progress);
        tag.putInt("Total", total);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pot.readFromNBT(tag.getCompound("Pot"));
        receiver.readFromNBT(tag.getCompound("Receiver"));
        stillage.readFromNBT(tag.getCompound("Stillage"));
        items.deserializeNBT(tag.getCompound("Items"));
        running = tag.getBoolean("Running");
        progress = tag.getInt("Progress");
        total = tag.getInt("Total");
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
    }

    // --- automation ------------------------------------------------------------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return side == Direction.DOWN ? stillageCap.cast() : pipesCap.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return side == Direction.UP ? basketCap.cast() : filterCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        filterCap.invalidate();
        basketCap.invalidate();
        pipesCap.invalidate();
        stillageCap.invalidate();
    }

    /**
     * What buckets and pipes see. Filling always goes into the pot (never while it runs); draining takes the spirit, or
     * (from the bottom, or for an empty bucket once the spirit is gone) the stillage.
     */
    private class Pipes implements IFluidHandler {
        private final boolean bottom;

        Pipes(boolean bottom) {
            this.bottom = bottom;
        }

        private FluidTank drainTank() {
            return bottom || receiver.isEmpty() ? stillage : receiver;
        }

        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int t) { return t == 0 ? pot.getFluid() : drainTank().getFluid(); }
        @Override public int getTankCapacity(int t) { return CAPACITY; }
        @Override public boolean isFluidValid(int t, FluidStack stack) { return t == 0 && pot.isFluidValid(stack); }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return bottom || running ? 0 : pot.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return drainTank().drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return drainTank().drain(maxDrain, action);
        }
    }
}
