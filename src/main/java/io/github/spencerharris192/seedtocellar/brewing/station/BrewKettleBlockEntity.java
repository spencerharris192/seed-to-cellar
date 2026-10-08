package io.github.spencerharris192.seedtocellar.brewing.station;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MixingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.Containers;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Brew Kettle logic. Four ingredient slots, a container slot (bowls, bottles) and an output slot.
 * <ul>
 *   <li>MASH: water + malt or grist (any ingredient slots) + heat -> sweet wort (same volume) +
 *       spent grain. Strength comes from grist per bucket; malt shares from the grist mix. Cornmeal and
 *       potatoes mash too, but only with some malt.</li>
 *   <li>BOIL: sweet wort + 1 dried hops per bucket (any ingredient slots) + heat -> hopped wort.</li>
 *   <li>MIX: a {@link MixingRecipe}: its liquid + so many of an ingredient per bucket + heat -> the whole
 *       kettle becomes another liquid (water + honey -> honey water, for mead); or the liquid alone boiled down
 *       (cane juice -> molasses).</li>
 *   <li>COOK: a {@link CookingRecipe} (GDD section 15): its ingredients, its liquid from the tank,
 *       its container -> one serving in the output slot. Repeats while there's enough for another.</li>
 * </ul>
 * Brewing comes first when both could run. Without heat, work pauses.
 * Automation: top and sides = ingredients and containers, bottom = the output; any side = liquids.
 */
public class BrewKettleBlockEntity extends SyncedBlockEntity implements MenuProvider, HydrometerReadable {
    public static final int CAPACITY = 4000;
    public static final int INGREDIENTS = 4;
    public static final int CONTAINER = 4;
    public static final int OUTPUT = 5;
    public static final int SLOTS = 6;
    public static final int MASH_TICKS = 600;
    public static final int BOIL_TICKS = 600;

    public enum Stage { IDLE, MASHING, BOILING, COOKING, MIXING }

    private final StationTank tank = new StationTank(CAPACITY, stack -> stack.getFluid().is(ModTags.Fluids.KETTLE_LIQUIDS)) {
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
            if (slot < INGREDIENTS) return isIngredient(stack);
            if (slot == CONTAINER) return isContainer(stack);
            return false;
        }
    };

    private Stage stage = Stage.IDLE;
    private int progress;
    private int total;
    private boolean heated;
    @Nullable private Identifier cookingId;   // the recipe being cooked (COOKING) or mixed (MIXING)
    /**
     * Something changed (contents, heat) since an idle kettle last looked for work. Looking every tick cost a heated, idle
     * kettle (every village Brewhouse has one) three times a furnace's tick; now it looks when something changes, and every
     * 5 seconds anyway (a data pack reload can add a recipe).
     */
    private boolean recheck = true;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> total;
                case 2 -> stage.ordinal();
                default -> heated ? 1 : 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> total = value;
                case 2 -> stage = Stage.values()[Math.floorMod(value, Stage.values().length)];
                default -> heated = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREW_KETTLE.get(), pos, state);
    }

    @Override
    protected void sync() {
        recheck = true;
        super.sync();
    }

    public StationTank tank() {
        return tank;
    }

    public StationItems items() {
        return items;
    }

    public Stage stage() {
        return stage;
    }

    public boolean isHeated() {
        return heated;
    }

    // --- what goes where -------------------------------------------------------------------

    /** Malt, grist, hops, or anything some cooking recipe uses. */
    public boolean isIngredient(ItemStack stack) {
        if (MaltType.of(stack) != null || stack.is(ModTags.Items.BOIL_HOPS)) return true;
        if (mixingRecipes().stream().anyMatch(r -> r.ingredient().map(i -> i.test(stack)).orElse(false))) return true;
        return cookingRecipes().stream().anyMatch(r -> r.ingredients().stream().anyMatch(i -> i.test(stack)));
    }

    /** A bowl, bottle or whatever some cooking recipe serves into. */
    public boolean isContainer(ItemStack stack) {
        return cookingRecipes().stream().anyMatch(r -> takesContainer(r, stack));
    }

    private static boolean takesContainer(CookingRecipe recipe, ItemStack stack) {
        return recipe.container().map(c -> c.test(stack)).orElse(false);
    }

    /** The first item an ingredient takes, to name it on the screen. */
    private static Component example(Ingredient ingredient) {
        return ingredient.items().findFirst().map(item -> new ItemStack(item).getHoverName()).orElse(Component.literal("?"));
    }

    private List<MixingRecipe> mixingRecipes() {
        return Recipes.stream(level, ModRecipes.MIXING.get()).toList();
    }

    private List<CookingRecipe> cookingRecipes() {
        return Recipes.stream(level, ModRecipes.COOKING.get()).toList();
    }

    private List<ItemStack> ingredientStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < INGREDIENTS; i++) stacks.add(items.getStackInSlot(i));
        return stacks;
    }

    // --- processing ------------------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrewKettleBlockEntity kettle) {
        boolean heated = Temperature.isHeatSource(level.getBlockState(pos.below()));
        if (heated != kettle.heated) kettle.recheck = true;
        kettle.heated = heated;
        if (kettle.stage != Stage.IDLE && !kettle.canContinue()) {
            kettle.stage = Stage.IDLE;
            kettle.progress = 0;
            kettle.cookingId = null;
            kettle.recheck = true;
        }
        if (kettle.heated) {
            boolean look = kettle.recheck || level.getGameTime() % 100 == Math.floorMod(pos.asLong(), 100);
            if (kettle.stage == Stage.IDLE && look) {
                kettle.recheck = false;
                if (kettle.canMash()) {
                    kettle.begin(Stage.MASHING, MASH_TICKS);
                } else if (kettle.canBoil()) {
                    kettle.begin(Stage.BOILING, BOIL_TICKS);
                } else if (kettle.mixableHolder().isPresent()) {
                    RecipeHolder<MixingRecipe> recipe = kettle.mixableHolder().get();
                    kettle.begin(Stage.MIXING, recipe.value().time());
                    kettle.cookingId = recipe.id().identifier();
                } else {
                    kettle.cookableHolder().ifPresent(recipe -> {
                        kettle.begin(Stage.COOKING, recipe.value().time());
                        kettle.cookingId = recipe.id().identifier();
                    });
                }
            } else if (kettle.stage != Stage.IDLE) {
                if (++kettle.progress >= kettle.total) {
                    switch (kettle.stage) {
                        case MASHING -> kettle.finishMash();
                        case BOILING -> kettle.finishBoil();
                        case COOKING -> kettle.finishCooking();
                        case MIXING -> kettle.finishMix();
                        default -> { }
                    }
                    kettle.stage = Stage.IDLE;
                    kettle.progress = 0;
                    kettle.cookingId = null;
                    level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 0.8F);
                }
                kettle.setChanged();
            }
        }
        boolean active = kettle.heated && kettle.stage != Stage.IDLE;
        if (state.getValue(BrewKettleBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(BrewKettleBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private boolean canContinue() {
        return switch (stage) {
            case MASHING -> canMash();
            case BOILING -> canBoil();
            case COOKING -> cookableHolder().map(r -> r.id().identifier().equals(cookingId)).orElse(false);
            case MIXING -> mixableHolder().map(r -> r.id().identifier().equals(cookingId)).orElse(false);
            default -> true;
        };
    }

    private void begin(Stage next, int baseTicks) {
        stage = next;
        progress = 0;
        total = ModConfigs.processTicks(baseTicks);
    }

    /** Can the output slot take this stack? */
    private boolean outputFits(ItemStack stack) {
        ItemStack out = items.getStackInSlot(OUTPUT);
        return out.isEmpty() || ItemStack.isSameItemSameComponents(out, stack) && out.getCount() + stack.getCount() <= out.getMaxStackSize();
    }

    // brewing ---

    /** Total mash weight per malt type in the ingredient slots. */
    public Map<MaltType, Float> mashWeights() {
        Map<MaltType, Float> weights = new EnumMap<>(MaltType.class);
        for (int i = 0; i < INGREDIENTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            MaltType type = MaltType.of(stack);
            if (type != null) weights.merge(type, type.weightOf(stack) * stack.getCount(), Float::sum);
        }
        return weights;
    }

    private int grainCount() {
        int count = 0;
        for (int i = 0; i < INGREDIENTS; i++) {
            if (MaltType.of(items.getStackInSlot(i)) != null) count += items.getStackInSlot(i).getCount();
        }
        return count;
    }

    /** Corn or potatoes in the slots with no malt to convert them (for the screen). */
    public boolean needsMalt() {
        Map<MaltType, Float> weights = mashWeights();
        return !weights.isEmpty() && weights.keySet().stream().allMatch(t -> t.adjunct);
    }

    private boolean canMash() {
        if (!tank.getFluid().getFluid().is(FluidTags.WATER) || tank.getFluidAmount() < 250 || mashWeights().isEmpty() || needsMalt()) return false;
        return outputFits(new ItemStack(ModItems.SPENT_GRAIN.get(), (grainCount() + 1) / 2));
    }

    /** The strength this kettle's contents would mash to right now (for the screen). */
    public WortData.Strength previewStrength() {
        int volume = tank.getFluid().getFluid().is(FluidTags.WATER) ? tank.getFluidAmount() : 0;
        if (volume == 0) return null;
        return WortData.fromMash(mashWeights(), volume).strength();
    }

    public int hopsNeeded() {
        return (tank.getFluidAmount() + 999) / 1000;
    }

    private int hopsCount() {
        int count = 0;
        for (int i = 0; i < INGREDIENTS; i++) {
            if (items.getStackInSlot(i).is(ModTags.Items.BOIL_HOPS)) count += items.getStackInSlot(i).getCount();
        }
        return count;
    }

    private boolean canBoil() {
        return tank.getFluid().getFluid() == ModFluids.SWEET_WORT.get() && hopsCount() >= hopsNeeded();
    }

    private void finishMash() {
        int volume = tank.getFluidAmount();
        WortData wort = WortData.fromMash(mashWeights(), volume);
        int spent = (grainCount() + 1) / 2;
        for (int i = 0; i < INGREDIENTS; i++) {
            if (MaltType.of(items.getStackInSlot(i)) != null) items.setStackInSlot(i, ItemStack.EMPTY);
        }
        ItemStack out = items.getStackInSlot(OUTPUT);
        items.setStackInSlot(OUTPUT, new ItemStack(ModItems.SPENT_GRAIN.get(), out.getCount() + spent));
        tank.setFluid(wort.applyTo(new FluidStack(ModFluids.SWEET_WORT.get(), volume)));
        sync();
    }

    private void finishBoil() {
        int needed = hopsNeeded();
        for (int i = 0; i < INGREDIENTS && needed > 0; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.is(ModTags.Items.BOIL_HOPS)) continue;
            int used = Math.min(needed, stack.getCount());
            stack.shrink(used);
            needed -= used;
        }
        tank.setFluid(tank.getFluid().transmuteCopy(ModFluids.HOPPED_WORT.get()));   // the same wort's data, hopped
        sync();
    }

    // mixing ---

    private int count(Ingredient ingredient) {
        int n = 0;
        for (int i = 0; i < INGREDIENTS; i++) if (ingredient.test(items.getStackInSlot(i))) n += items.getStackInSlot(i).getCount();
        return n;
    }

    /**
     * For the screen: what a mixing recipe for the liquid in the tank still needs. Short of its ingredient: how many more
     * (it takes enough for the whole kettle). Nothing added yet, to a liquid that's only for mixing: what to stir in.
     */
    @Nullable
    public Component missingForMixing() {
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty()) return null;
        boolean slotsEmpty = ingredientStacks().stream().allMatch(ItemStack::isEmpty);
        for (MixingRecipe recipe : mixingRecipes()) {
            if (!recipe.matchesLiquid(fluid) || recipe.boilsDown()) continue;
            Ingredient ingredient = recipe.ingredient().orElseThrow();
            int have = count(ingredient);
            int needed = recipe.needed(fluid.getAmount());
            if (have >= needed) continue;
            if (have > 0) {
                ItemStack added = ingredientStacks().stream().filter(ingredient).findFirst().orElse(ItemStack.EMPTY);
                return Component.translatable("gui.seedtocellar.kettle.needs_more", needed - have, added.getHoverName(), recipe.perBucket());
            }
            if (slotsEmpty && !fluid.getFluid().is(FluidTags.WATER)) {
                return Component.translatable("gui.seedtocellar.kettle.stir_in", recipe.perBucket(), example(ingredient));
            }
        }
        return null;
    }

    /** The mixing recipe that can run now: the right liquid, and enough of its ingredient for the whole kettle. */
    public Optional<MixingRecipe> mixable() {
        return mixableHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<MixingRecipe>> mixableHolder() {
        FluidStack fluid = tank.getFluid();
        boolean slotsEmpty = ingredientStacks().stream().allMatch(ItemStack::isEmpty);
        return Recipes.holders(level, ModRecipes.MIXING.get()).filter(h -> {
            MixingRecipe r = h.value();
            return r.matchesLiquid(fluid) && (r.boilsDown() ? slotsEmpty : count(r.ingredient().orElseThrow()) >= r.needed(fluid.getAmount()));
        }).findFirst();
    }

    private void finishMix() {
        Optional<MixingRecipe> found = mixable();
        if (found.isEmpty() || level == null) return;
        MixingRecipe recipe = found.get();
        int volume = tank.getFluidAmount();
        int needed = recipe.needed(volume);
        List<ItemStack> leftovers = new ArrayList<>();
        for (int i = 0; i < INGREDIENTS && needed > 0 && recipe.ingredient().isPresent(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!recipe.ingredient().get().test(stack)) continue;
            int used = Math.min(needed, stack.getCount());
            if (stack.getItem().getCraftingRemainder() != null) leftovers.add(stack.getItem().getCraftingRemainder().create().copyWithCount(used));
            stack.shrink(used);
            needed -= used;
        }
        tank.setFluid(new FluidStack(recipe.result(), volume));
        for (ItemStack leftover : leftovers) {
            ItemStack rest = items.insertItem(CONTAINER, leftover, false);   // honey bottles leave glass bottles
            if (!rest.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, rest);
        }
        sync();
    }

    // cooking ---

    /**
     * The cooking recipe the ingredient slots match right now, if any (ignoring liquid and container). A recipe with no
     * ingredients (the liquid boiled down alone, like sorghum syrup) only matches while its liquid is in the tank, so an
     * empty kettle doesn't ask for it.
     */
    public Optional<CookingRecipe> matchingRecipe() {
        return matchingHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<CookingRecipe>> matchingHolder() {
        List<ItemStack> stacks = ingredientStacks();
        return Recipes.holders(level, ModRecipes.COOKING.get()).filter(h -> h.value().matchesItems(stacks)
                && (!h.value().ingredients().isEmpty() || h.value().liquid() != null && h.value().liquid().test(tank.getFluid()))).findFirst();
    }

    /** What's missing to cook the matching recipe (null if it can cook, or nothing matches). For the screen. */
    @Nullable
    public Component missingForCooking() {
        Optional<CookingRecipe> recipe = matchingRecipe();
        if (recipe.isEmpty()) return null;
        CookingRecipe r = recipe.get();
        String k = "gui.seedtocellar.kettle.";
        if (r.liquid() != null && !r.liquid().test(tank.getFluid())) {
            FluidStack example = r.liquid().examples().stream().findFirst().orElse(FluidStack.EMPTY);
            return Component.translatable(k + "needs_liquid", r.liquid().amount(), example.getHoverName());
        }
        if (r.needsContainer() && !takesContainer(r, items.getStackInSlot(CONTAINER))) {
            return Component.translatable(k + "needs_container", example(r.container().get()));
        }
        if (!outputFits(r.result())) return Component.translatable(k + "output_full");
        return null;
    }

    /** The recipe that can cook right now: ingredients match, liquid and container present, room for the result. */
    private Optional<CookingRecipe> cookable() {
        return cookableHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<CookingRecipe>> cookableHolder() {
        return matchingHolder().filter(h -> {
            CookingRecipe r = h.value();
            return (r.liquid() == null || r.liquid().test(tank.getFluid()))
                    && (!r.needsContainer() || takesContainer(r, items.getStackInSlot(CONTAINER)))
                    && outputFits(r.result());
        });
    }

    private void finishCooking() {
        Optional<CookingRecipe> found = cookable();
        if (found.isEmpty() || level == null) return;
        CookingRecipe recipe = found.get();
        List<ItemStack> leftovers = new ArrayList<>();
        for (int i = 0; i < INGREDIENTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem().getCraftingRemainder() != null) leftovers.add(stack.getItem().getCraftingRemainder().create());   // honey bottle -> glass bottle
            stack.shrink(1);
        }
        if (recipe.liquid() != null) tank.drain(recipe.liquid().amount(), StationTank.Action.EXECUTE);
        if (recipe.needsContainer()) items.getStackInSlot(CONTAINER).shrink(1);
        ItemStack out = items.getStackInSlot(OUTPUT);
        ItemStack result = recipe.result();
        if (out.isEmpty()) items.setStackInSlot(OUTPUT, result);
        else out.grow(result.getCount());
        for (ItemStack leftover : leftovers) {
            ItemStack rest = items.insertItem(CONTAINER, leftover, false);   // a glass bottle back into the bottle slot
            if (!rest.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, rest);
        }
        sync();
    }

    /** The recipe currently cooking (for the screen and Jade). */
    public Optional<CookingRecipe> cooking() {
        if (stage != Stage.COOKING || level == null) return Optional.empty();
        return matchingRecipe();
    }

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        FluidStack fluid = tank.getFluid();
        lines.add(fluid.isEmpty() ? Component.translatable(h + "empty") : fluid.getHoverName().copy().append(" " + fluid.getAmount() + " mB"));
        if (fluid.getFluid() == ModFluids.SWEET_WORT.get() || fluid.getFluid() == ModFluids.HOPPED_WORT.get()) {
            lines.add(Component.translatable("gui.seedtocellar.kettle.strength", WortData.of(fluid).strength().displayName()));
        }
        cooking().ifPresent(r -> lines.add(Component.translatable("gui.seedtocellar.kettle.makes", r.result().getHoverName())));
        if (!heated) lines.add(Component.translatable("gui.seedtocellar.kettle.no_heat"));
        else if (stage != Stage.IDLE) lines.add(Component.translatable(h + "progress", total == 0 ? 0 : progress * 100 / total));
        return lines;
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
        return new BrewKettleMenu(id, inventory, this, items, data);
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("Tank"));
        items.serialize(output.child("Items"));
        output.putString("Stage", stage.name());
        output.putInt("Progress", progress);
        output.putInt("Total", total);
        if (cookingId != null) output.putString("Cooking", cookingId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
        items.deserialize(input.childOrEmpty("Items"));
        try {
            stage = Stage.valueOf(input.getStringOr("Stage", ""));
        } catch (IllegalArgumentException e) {
            stage = Stage.IDLE;
        }
        progress = input.getIntOr("Progress", 0);
        total = input.getIntOr("Total", 0);
        cookingId = input.getString("Cooking").map(Identifier::tryParse).orElse(null);
    }

    // --- automation ------------------------------------------------------------------------

    private final ResourceHandler<ItemResource> inputSide = RangedResourceHandler.of(items, 0, CONTAINER + 1);
    private final ResourceHandler<ItemResource> outputSide = RangedResourceHandler.ofSingleIndex(items, OUTPUT);

    /** Hoppers: ingredients and containers in from the top and sides, the dish out from below. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? outputSide : inputSide;
    }

    /** Pipes: liquids in and out on every side. */
    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return tank;
    }

}
