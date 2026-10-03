package io.github.spencerharris192.seedtocellar.brewing.station;

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

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            sync();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(2) {
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
    private ResourceLocation recipeId;

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

    private final LazyOptional<IItemHandler> yeastCap = LazyOptional.of(() -> new RangedWrapper(items, YEAST, YEAST + 1));
    private final LazyOptional<IItemHandler> leesCap = LazyOptional.of(() -> new RangedWrapper(items, LEES, LEES + 1));
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(AutomationFluids::new);

    public FermentingVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERMENTING_VAT.get(), pos, state);
    }

    // --- queries ---------------------------------------------------------------------------

    public FluidTank tank() {
        return tank;
    }

    public ItemStackHandler items() {
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
        if (level == null || tank.isEmpty()) return Optional.empty();
        YeastType yeast = YeastType.of(items.getStackInSlot(YEAST));
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get()).stream()
                .filter(r -> r.matches(tank.getFluid(), yeast))
                .max(Comparator.comparingInt(FermentingRecipe::priority));
    }

    private Optional<FermentingRecipe> currentRecipe() {
        if (level == null || recipeId == null) return Optional.empty();
        return level.getRecipeManager().byKey(recipeId).filter(FermentingRecipe.class::isInstance).map(FermentingRecipe.class::cast);
    }

    // --- player actions --------------------------------------------------------------------

    public void useFluidContainer(Player player, InteractionHand hand) {
        if (fermenting) {
            player.displayClientMessage(Component.translatable("message.seedtocellar.vat.busy").withStyle(ChatFormatting.YELLOW), true);
        } else if (!isOpen()) {
            player.displayClientMessage(Component.translatable("message.seedtocellar.vat.closed").withStyle(ChatFormatting.YELLOW), true);
        } else {
            FluidUtil.interactWithFluidHandler(player, hand, tank);
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
                player.displayClientMessage(Component.translatable("message.seedtocellar.vat.busy_progress",
                        Math.round(progress() * 100)).withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }
        level.setBlock(worldPosition, getBlockState().setValue(FermentingVatBlock.OPEN, open), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 1.0F);
        if (!open) tryStart(player);
    }

    private void tryStart(Player player) {
        if (fermenting || tank.isEmpty()) return;
        Optional<FermentingRecipe> recipe = expectedRecipe();
        if (recipe.isEmpty()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.seedtocellar.vat.nothing").withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }
        ItemStack yeast = items.getStackInSlot(YEAST);
        yeastUsed = YeastType.of(yeast);
        if (yeastUsed != YeastType.WILD) items.extractItem(YEAST, 1, false);
        float ticks = recipe.get().time() * ModConfigs.SERVER.fermentationTimeMultiplier.get().floatValue();
        if (yeastUsed == YeastType.WILD) ticks *= WILD_SLOWDOWN;
        recipeId = recipe.get().getId();
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
        if (!fermenting || level == null || level.isClientSide) return;
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
        } else if (ItemStack.isSameItemSameTags(current, lees)) {
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
        lines.add(fluid.isEmpty() ? Component.translatable(h + "empty") : fluid.getDisplayName().copy().append(" " + fluid.getAmount() + " mB"));
        Optional<FermentingRecipe> recipe = fermenting ? currentRecipe() : expectedRecipe();
        recipe.ifPresent(r -> {
            if (fermenting) lines.add(Component.translatable("gui.seedtocellar.vat.fermenting", Math.round(progress() * 100)));
            else lines.add(Component.translatable("gui.seedtocellar.vat.makes", new FluidStack(r.result(), 1).getDisplayName()));
            Temperature now = Temperature.at(level, worldPosition);
            boolean ok = fermenting ? temperatureOk && r.suits(now) : r.suits(now);
            lines.add(Component.translatable(h + "temperature", now.displayName(), r.idealName())
                    .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED));
        });
        if (!fluid.isEmpty() && fluid.getTag() != null && fluid.getTag().contains(BrewQuality.TAG)) {
            lines.add(DrinkItem.stars(BrewQuality.of(fluid).stars()));
        }
        return lines;
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
        return new FermentingVatMenu(id, inventory, this, items, data);
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Items", items.serializeNBT());
        tag.putBoolean("Fermenting", fermenting);
        tag.putLong("Start", start);
        tag.putLong("End", end);
        tag.putString("Yeast", yeastUsed.key);
        tag.putBoolean("TemperatureOk", temperatureOk);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
        items.deserializeNBT(tag.getCompound("Items"));
        fermenting = tag.getBoolean("Fermenting");
        start = tag.getLong("Start");
        end = tag.getLong("End");
        yeastUsed = YeastType.byKey(tag.getString("Yeast"));
        temperatureOk = tag.getBoolean("TemperatureOk");
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
    }

    // --- automation ------------------------------------------------------------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return side == Direction.DOWN ? leesCap.cast() : yeastCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        yeastCap.invalidate();
        leesCap.invalidate();
        fluidCap.invalidate();
    }

    /** Pipes may fill (lid open) and drain, but never while a batch is fermenting. */
    private class AutomationFluids implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int t) { return tank.getFluid(); }
        @Override public int getTankCapacity(int t) { return CAPACITY; }
        @Override public boolean isFluidValid(int t, FluidStack stack) { return tank.isFluidValid(stack); }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return !fermenting && isOpen() ? tank.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            advance();
            return fermenting ? FluidStack.EMPTY : tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            advance();
            return fermenting ? FluidStack.EMPTY : tank.drain(maxDrain, action);
        }
    }
}
