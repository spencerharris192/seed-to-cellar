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
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.world.Containers;
import net.minecraft.world.item.crafting.RecipeHolder;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Fermenting Vat logic (GDD sections 8, 11, 12). The start and end game time are stored and a
 * single scheduled tick finishes the batch, so it ferments in unloaded chunks at no cost.
 * Quality checks earned here: the right yeast family, and the right temperature throughout.
 * Automation: top = yeast in, bottom = lees out, any side = liquids (only while not fermenting).
 */
public class FermentingVatBlockEntity extends SyncedBlockEntity implements MenuProvider, HydrometerReadable {
    public static final int CAPACITY = 8000;
    public static final int YEAST = 0;
    public static final int LEES = 1;
    public static final float WILD_SLOWDOWN = 1.5F;

    private final StationTank tank = new StationTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final StationItems items = new StationItems(2) {
        @Override
        protected void onContentsChanged(int slot) {
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == YEAST && isYeast(stack);
        }
    };

    private boolean fermenting;
    private long start;
    private long end;
    private YeastType yeastUsed = YeastType.WILD;
    private boolean temperatureOk;
    private Identifier recipeId;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> Math.round(progress() * 1000);
                case 1 -> fermenting ? 1 : 0;
                case 2 -> level == null ? 2 : Temperature.at(level, worldPosition).ordinal();
                case 3 -> temperatureOk ? 1 : 0;
                default -> isOpen() ? 1 : 0;
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

    public FermentingVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERMENTING_VAT.get(), pos, state);
    }

    // --- queries ---------------------------------------------------------------------------

    public StationTank tank() {
        return tank;
    }

    public StationItems items() {
        return items;
    }

    public boolean isFermenting() {
        return fermenting;
    }

    public boolean isOpen() {
        return getBlockState().getValue(FermentingVatBlock.OPEN);
    }

    public boolean temperatureOk() {
        return temperatureOk;
    }

    public float progress() {
        if (!fermenting || level == null || end <= start) return 0;
        return Math.min(1F, (level.getGameTime() - start) / (float) (end - start));
    }

    public boolean isYeast(ItemStack stack) {
        return YeastType.of(stack) != YeastType.WILD;
    }

    /** What the current contents would ferment into with the current yeast (client and server). */
    public Optional<FermentingRecipe> expectedRecipe() {
        return expectedHolder().map(RecipeHolder::value);
    }

    private Optional<RecipeHolder<FermentingRecipe>> expectedHolder() {
        if (level == null || tank.isEmpty()) return Optional.empty();
        YeastType yeast = YeastType.of(items.getStackInSlot(YEAST));
        return Recipes.holders(level, ModRecipes.FERMENTING.get())
                .filter(h -> h.value().matches(tank.getFluid(), yeast))
                .max(Comparator.comparingInt(h -> h.value().priority()));
    }

    private Optional<FermentingRecipe> currentRecipe() {
        return Recipes.byId(level, recipeId, FermentingRecipe.class);
    }

    // --- player actions --------------------------------------------------------------------

    public void useFluidContainer(Player player, InteractionHand hand) {
        if (fermenting) {
            player.sendOverlayMessage(Component.translatable("message.seedtocellar.vat.busy").withStyle(ChatFormatting.YELLOW));
        } else if (!isOpen()) {
            player.sendOverlayMessage(Component.translatable("message.seedtocellar.vat.closed").withStyle(ChatFormatting.YELLOW));
        } else {
            pourWith(player, hand, tank);
        }
    }

    public boolean insertYeast(Player player, InteractionHand hand) {
        if (fermenting) return false;
        ItemStack held = player.getItemInHand(hand);
        ItemStack remainder = items.insertItem(YEAST, held.copy(), false);
        if (remainder.getCount() == held.getCount()) return false;
        if (!player.getAbilities().instabuild) player.setItemInHand(hand, remainder);
        return true;
    }

    public void toggleLid(Player player) {
        setLid(!isOpen(), player);
    }

    /** Opens or closes the lid. Closing starts fermentation if the contents can ferment. */
    public void setLid(boolean open, Player player) {
        if (level == null || isOpen() == open) return;
        if (open && fermenting) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.vat.busy_progress",
                        Math.round(progress() * 100)).withStyle(ChatFormatting.YELLOW));
            }
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(FermentingVatBlock.OPEN, open), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 1.0F);
        if (!open) tryStart(player);
    }

    private void tryStart(Player player) {
        if (fermenting || tank.isEmpty()) return;
        Optional<RecipeHolder<FermentingRecipe>> holder = expectedHolder();
        Optional<FermentingRecipe> recipe = holder.map(RecipeHolder::value);
        if (recipe.isEmpty()) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.vat.nothing").withStyle(ChatFormatting.YELLOW));
            }
            return;
        }
        ItemStack yeast = items.getStackInSlot(YEAST);
        yeastUsed = YeastType.of(yeast);
        if (yeastUsed != YeastType.WILD) items.extractItem(YEAST, 1, false);
        float ticks = recipe.get().time() * ModConfigs.SERVER.fermentationTimeMultiplier.get().floatValue();
        if (yeastUsed == YeastType.WILD) ticks *= WILD_SLOWDOWN;
        recipeId = holder.get().id().identifier();
        start = level.getGameTime();
        end = start + Math.max(1, Math.round(ticks));
        temperatureOk = recipe.get().suits(Temperature.at(level, worldPosition));
        fermenting = true;
        level.setBlock(worldPosition, getBlockState().setValue(FermentingVatBlock.FERMENTING, true), Block.UPDATE_ALL);
        level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) Math.min(Integer.MAX_VALUE, end - start));
        sync();
    }

    /** A neighbor changed (ice melted, a campfire lit...). Losing the right temperature loses the star. */
    public void recheckTemperature() {
        if (!fermenting || level == null) return;
        currentRecipe().ifPresent(r -> {
            if (!r.suits(Temperature.at(level, worldPosition)) && temperatureOk) {
                temperatureOk = false;
                setChanged();
            }
        });
    }

    /** Finishes the batch if its time is up. Called by the scheduled tick and when drained. */
    public void advance() {
        if (!fermenting || level == null || level.isClientSide()) return;
        if (level.getGameTime() < end) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), (int) Math.max(1, end - level.getGameTime()));
            return;
        }
        Optional<FermentingRecipe> recipe = currentRecipe();
        fermenting = false;
        if (recipe.isPresent()) {
            FermentingRecipe r = recipe.get();
            recheckTemperature();
            BrewQuality quality = new BrewQuality(yeastUsed == r.yeast(), temperatureOk, false, false);   // lager yeast in an ale: no yeast star
            tank.setFluid(quality.applyTo(new FluidStack(r.result(), tank.getFluidAmount())));
            addLees(new ItemStack(r.yeast().leesItem(), yeastUsed == YeastType.WILD ? 1 : 2));
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.1F);
        }
        recipeId = null;
        level.setBlock(worldPosition, getBlockState().setValue(FermentingVatBlock.FERMENTING, false), Block.UPDATE_ALL);
        sync();
    }

    /** Lees go to the lees slot; anything that doesn't fit pops out on top of the vat. */
    private void addLees(ItemStack lees) {
        ItemStack current = items.getStackInSlot(LEES);
        ItemStack remainder = lees;
        if (current.isEmpty()) {
            items.setStackInSlot(LEES, lees);
            remainder = ItemStack.EMPTY;
        } else if (ItemStack.isSameItemSameComponents(current, lees)) {
            int moved = Math.min(lees.getCount(), current.getMaxStackSize() - current.getCount());
            items.setStackInSlot(LEES, current.copyWithCount(current.getCount() + moved));
            remainder = lees.copyWithCount(lees.getCount() - moved);
        }
        if (!remainder.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, remainder);
        }
    }

    @Override
    public List<Component> hydrometerLines() {
        advance();
        List<Component> lines = new ArrayList<>();
        String h = "hydrometer.seedtocellar.";
        FluidStack fluid = tank.getFluid();
        lines.add(fluid.isEmpty() ? Component.translatable(h + "empty") : fluid.getHoverName().copy().append(" " + fluid.getAmount() + " mB"));
        Optional<FermentingRecipe> recipe = fermenting ? currentRecipe() : expectedRecipe();
        recipe.ifPresent(r -> {
            if (fermenting) lines.add(Component.translatable("gui.seedtocellar.vat.fermenting", Math.round(progress() * 100)));
            else lines.add(Component.translatable("gui.seedtocellar.vat.makes", new FluidStack(r.result(), 1).getHoverName()));
            Temperature now = Temperature.at(level, worldPosition);
            boolean ok = fermenting ? temperatureOk && r.suits(now) : r.suits(now);
            lines.add(Component.translatable(h + "temperature", now.displayName(), r.idealName())
                    .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
        });
        if (!fluid.isEmpty() && BrewQuality.has(fluid)) {
            lines.add(DrinkItem.stars(BrewQuality.of(fluid).stars()));
        }
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
        return new FermentingVatMenu(id, inventory, this, items, data);
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("Tank"));
        items.serialize(output.child("Items"));
        output.putBoolean("Fermenting", fermenting);
        output.putLong("Start", start);
        output.putLong("End", end);
        output.putString("Yeast", yeastUsed.key);
        output.putBoolean("TemperatureOk", temperatureOk);
        if (recipeId != null) output.putString("Recipe", recipeId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
        items.deserialize(input.childOrEmpty("Items"));
        fermenting = input.getBooleanOr("Fermenting", false);
        start = input.getLongOr("Start", 0L);
        end = input.getLongOr("End", 0L);
        yeastUsed = YeastType.byKey(input.getStringOr("Yeast", ""));
        temperatureOk = input.getBooleanOr("TemperatureOk", false);
        recipeId = input.getString("Recipe").map(Identifier::tryParse).orElse(null);
    }

    // --- automation ------------------------------------------------------------------------

    /** Pipes may fill (lid open) and drain, but never while a batch is fermenting. */
    private final ResourceHandler<FluidResource> automationFluids = new DelegatingResourceHandler<>(tank) {
        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return !fermenting && isOpen() ? super.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            advance();
            return fermenting ? 0 : super.extract(index, resource, amount, transaction);
        }
    };

    private final ResourceHandler<ItemResource> yeastSide = RangedResourceHandler.ofSingleIndex(items, YEAST);
    private final ResourceHandler<ItemResource> leesSide = RangedResourceHandler.ofSingleIndex(items, LEES);

    /** Hoppers: yeast in from the top and sides, lees out from below. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? leesSide : yeastSide;
    }

    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return automationFluids;
    }
}
