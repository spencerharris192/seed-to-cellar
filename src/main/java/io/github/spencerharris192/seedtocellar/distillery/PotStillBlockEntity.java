package io.github.spencerharris192.seedtocellar.distillery;

import io.github.spencerharris192.seedtocellar.brewing.station.StationItems;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.Containers;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
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
import net.neoforged.neoforge.fluids.FluidStack;
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

    private final StationTank pot = new StationTank(CAPACITY, this::isDistillable) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private final StationTank receiver = new StationTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };
    private final StationTank stillage = new StationTank(CAPACITY, stack -> stack.getFluid() == ModFluids.STILLAGE.get()) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final StationItems items = new StationItems(SLOTS) {
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
    @Nullable private Identifier recipeId;

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

    public PotStillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POT_STILL.get(), pos, state);
    }

    // --- queries ---------------------------------------------------------------------------

    public StationTank pot() { return pot; }
    public StationTank receiver() { return receiver; }
    public StationTank stillage() { return stillage; }
    public StationItems items() { return items; }
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
        return Recipes.stream(level, ModRecipes.DISTILLING.get()).toList();
    }

    /** Something some recipe distils (a wash, a wine, a spirit). */
    private boolean isDistillable(FluidStack stack) {
        FluidStack probe = stack.copyWithAmount(Math.max(1, stack.getAmount()));
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
        return expectedHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<DistillingRecipe>> expectedHolder() {
        if (pot.isEmpty()) return Optional.empty();
        boolean filter = filterPresent();
        List<ItemStack> basket = basketStacks();
        return Recipes.holders(level, ModRecipes.DISTILLING.get()).filter(h -> h.value().matches(pot.getFluid(), filter, basket))
                .max(Comparator.comparingInt(h -> h.value().priority()));
    }

    private Optional<DistillingRecipe> currentRecipe() {
        return Recipes.byId(level, recipeId, DistillingRecipe.class);
    }

    /** What a run of the pot with {@code recipe} would put in the receiver: half the volume, one more run, its stars. */
    public FluidStack output(DistillingRecipe recipe) {
        FluidStack in = pot.getFluid();
        CompoundTag before = BrewData.orNull(in);
        CompoundTag tag = new CompoundTag();
        tag.putInt(CraftStep.RUNS, DistillingRecipe.runs(in) + 1);
        if (recipe.filter() || before != null && before.getBooleanOr(CraftStep.FILTERED, false)) tag.putBoolean(CraftStep.FILTERED, true);
        if (recipe.basket() != null) tag.putInt(CraftStep.BOTANICAL_COUNT, DistillingRecipe.Basket.distinct(basketStacks()));
        FluidStack out = new FluidStack(recipe.resultFor(in), in.getAmount() / 2);
        BrewData.set(out, tag);
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
        if (receiver.fill(out, StationTank.Action.SIMULATE) < out.getAmount()) {
            return Component.translatable(receiver.isEmpty() || FluidStack.isSameFluidSameComponents(receiver.getFluid(), out) ? k + "receiver_full" : k + "receiver_other");
        }
        int left = pot.getFluidAmount() - out.getAmount();
        if (stillage.fill(new FluidStack(ModFluids.STILLAGE.get(), left), StationTank.Action.SIMULATE) < left) {
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
            still.expectedHolder().filter(h -> still.blocked(h.value()) == null).ifPresent(still::begin);
        }
        boolean active = still.running && still.heated;
        if (state.getValue(PotStillBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(PotStillBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private void begin(RecipeHolder<DistillingRecipe> recipe) {
        running = true;
        recipeId = recipe.id().identifier();
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
        receiver.fill(out, StationTank.Action.EXECUTE);
        stillage.fill(new FluidStack(ModFluids.STILLAGE.get(), left), StationTank.Action.EXECUTE);
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
            if (level != null && !level.isClientSide() && fitBasket() && !player.getAbilities().instabuild) held.shrink(1);
            return true;
        }
        if (!holdsLiquidContainer(player, hand)) return false;
        if (level != null && !level.isClientSide()) {
            boolean full = !FluidUtil.getFirstStackContained(held).isEmpty();
            if (full && running) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.still.busy").withStyle(ChatFormatting.YELLOW));
            } else if (!pourWith(player, hand, new Pipes(false)) && full) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.still.wont_distil").withStyle(ChatFormatting.YELLOW));
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
                : Component.translatable(k + "pot", in.getHoverName(), in.getAmount()));
        Optional<DistillingRecipe> recipe = running ? currentRecipe() : expectedRecipe();
        recipe.ifPresent(r -> lines.add(makesLine(r)));
        if (!heated) lines.add(Component.translatable(k + "no_heat").withStyle(ChatFormatting.RED));
        else if (running) lines.add(Component.translatable(h + "progress", Math.round(progress() * 100)));
        if (!receiver.isEmpty()) {
            FluidStack out = receiver.getFluid();
            lines.add(Component.translatable(k + "receiver", out.getHoverName(), out.getAmount()));
            lines.add(DrinkItem.stars(BrewQuality.of(out).stars()));
        }
        if (!stillage.isEmpty()) lines.add(Component.translatable(k + "stillage", stillage.getFluidAmount()).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    /** "Makes: Malt Whiskey (run 1)". */
    public Component makesLine(DistillingRecipe recipe) {
        FluidStack out = new FluidStack(recipe.resultFor(pot.getFluid()), 1);
        return Component.translatable("gui.seedtocellar.still.makes", out.getHoverName(), DistillingRecipe.runs(pot.getFluid()) + 1);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        pot.serialize(output.child("Pot"));
        receiver.serialize(output.child("Receiver"));
        stillage.serialize(output.child("Stillage"));
        items.serialize(output.child("Items"));
        output.putBoolean("Running", running);
        output.putInt("Progress", progress);
        output.putInt("Total", total);
        if (recipeId != null) output.putString("Recipe", recipeId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        pot.deserialize(input.childOrEmpty("Pot"));
        receiver.deserialize(input.childOrEmpty("Receiver"));
        stillage.deserialize(input.childOrEmpty("Stillage"));
        items.deserialize(input.childOrEmpty("Items"));
        running = input.getBooleanOr("Running", false);
        progress = input.getIntOr("Progress", 0);
        total = input.getIntOr("Total", 0);
        recipeId = input.getString("Recipe").map(Identifier::tryParse).orElse(null);
    }

    // --- automation ------------------------------------------------------------------------

    /**
     * What buckets and pipes see. Filling always goes into the pot (never while it runs); draining takes the spirit, or
     * (from the bottom, or for an empty bucket once the spirit is gone) the stillage.
     */
    private class Pipes implements ResourceHandler<FluidResource> {
        private final boolean bottom;

        Pipes(boolean bottom) {
            this.bottom = bottom;
        }

        private StationTank drainTank() {
            return bottom || receiver.isEmpty() ? stillage : receiver;
        }

        // index 0: the pot (fill only); index 1: the spirit, or the stillage (drain only)
        @Override public int size() { return 2; }
        @Override public FluidResource getResource(int index) { return index == 0 ? pot.getResource(0) : drainTank().getResource(0); }
        @Override public long getAmountAsLong(int index) { return index == 0 ? pot.getAmountAsLong(0) : drainTank().getAmountAsLong(0); }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return CAPACITY; }
        @Override public boolean isValid(int index, FluidResource resource) { return index == 0 && !bottom && pot.isValid(0, resource); }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return index == 0 && !bottom && !running ? pot.insert(0, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return index == 1 ? drainTank().extract(0, resource, amount, transaction) : 0;
        }
    }

    private final ResourceHandler<FluidResource> sidePipes = new Pipes(false);
    private final ResourceHandler<FluidResource> bottomPipes = new Pipes(true);
    private final ResourceHandler<ItemResource> filterSide = RangedResourceHandler.ofSingleIndex(items, FILTER);
    private final ResourceHandler<ItemResource> basketSide = RangedResourceHandler.of(items, BASKET, SLOTS);

    /** Pipes: the wash in from any side but the bottom, the spirit out (the stillage from below). */
    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? bottomPipes : sidePipes;
    }

    /** Hoppers: botanicals from the top, charcoal for the filter from the sides and below. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return side == Direction.UP ? basketSide : filterSide;
    }
}
