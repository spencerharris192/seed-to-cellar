package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Kiln logic: like a furnace, but it roasts the whole input stack as one batch at the chosen
 * roast setting. Fuel is only started when there is something to roast.
 * Automation: top = input, sides = fuel, bottom = output.
 */
public class KilnBlockEntity extends SyncedBlockEntity implements MenuProvider, HydrometerReadable {
    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int OUTPUT = 2;
    public static final int MAX_BATCH = 16;

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == FUEL) setChanged();
            else sync(); // input/output show on top of the kiln
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT ? MAX_BATCH : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case FUEL -> ForgeHooks.getBurnTime(stack, null) > 0;
                case OUTPUT -> false;
                default -> true;
            };
        }
    };

    private int burnTime;
    private int burnDuration;
    private int progress;
    private int totalTime;
    private RoastLevel roast = RoastLevel.LIGHT;

    /** Synced to the open menu: burn, burn max, progress, progress max, roast. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnDuration;
                case 2 -> progress;
                case 3 -> totalTime;
                default -> roast.ordinal();
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> burnDuration = value;
                case 2 -> progress = value;
                case 3 -> totalTime = value;
                default -> roast = RoastLevel.values()[Math.floorMod(value, RoastLevel.values().length)];
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private final LazyOptional<IItemHandler> inputCap = LazyOptional.of(() -> new RangedWrapper(items, INPUT, INPUT + 1));
    private final LazyOptional<IItemHandler> fuelCap = LazyOptional.of(() -> new RangedWrapper(items, FUEL, FUEL + 1));
    private final LazyOptional<IItemHandler> outputCap = LazyOptional.of(() -> new RangedWrapper(items, OUTPUT, OUTPUT + 1));

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KILN.get(), pos, state);
    }

    public ItemStackHandler items() {
        return items;
    }

    public RoastLevel roast() {
        return roast;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public float progressFraction() {
        return totalTime <= 0 ? 0 : progress / (float) totalTime;
    }

    public void setRoast(RoastLevel level) {
        if (roast != level) {
            roast = level;
            progress = 0;
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KilnBlockEntity kiln) {
        boolean wasLit = kiln.burnTime > 0;
        if (kiln.burnTime > 0) kiln.burnTime--;

        Optional<KilningRecipe> recipe = kiln.findRecipe();
        if (recipe.isPresent() && kiln.canOutput(recipe.get())) {
            if (kiln.burnTime <= 0) kiln.consumeFuel();
            kiln.totalTime = ModConfigs.processTicks(recipe.get().time());
            if (kiln.burnTime > 0) {
                if (++kiln.progress >= kiln.totalTime) {
                    kiln.finish(recipe.get());
                }
            } else {
                kiln.progress = Math.max(0, kiln.progress - 2); // cools off without fuel
            }
            kiln.setChanged();
        } else if (kiln.progress != 0) {
            kiln.progress = 0;
            kiln.setChanged();
        }

        boolean lit = kiln.burnTime > 0;
        if (wasLit != lit) {
            level.setBlock(pos, state.setValue(KilnBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    public Optional<KilningRecipe> findRecipe() {
        ItemStack input = items.getStackInSlot(INPUT);
        if (level == null || input.isEmpty()) return Optional.empty();
        SimpleContainer container = new SimpleContainer(input);
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.KILNING.get()).stream()
                .filter(r -> r.matches(container, roast)).findFirst();
    }

    private ItemStack batchResult(KilningRecipe recipe) {
        ItemStack result = recipe.result().copy();
        result.setCount(result.getCount() * items.getStackInSlot(INPUT).getCount());
        return result;
    }

    private boolean canOutput(KilningRecipe recipe) {
        ItemStack result = batchResult(recipe);
        ItemStack output = items.getStackInSlot(OUTPUT);
        if (output.isEmpty()) return result.getCount() <= result.getMaxStackSize();
        return ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void consumeFuel() {
        ItemStack fuel = items.getStackInSlot(FUEL);
        int time = ForgeHooks.getBurnTime(fuel, null);
        if (time <= 0) return;
        burnTime = burnDuration = time;
        if (fuel.hasCraftingRemainingItem()) {
            items.setStackInSlot(FUEL, fuel.getCraftingRemainingItem());
        } else {
            fuel.shrink(1);
            items.setStackInSlot(FUEL, fuel);
        }
    }

    private void finish(KilningRecipe recipe) {
        ItemStack result = batchResult(recipe);
        ItemStack output = items.getStackInSlot(OUTPUT);
        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT, result);
        } else {
            output.grow(result.getCount());
            items.setStackInSlot(OUTPUT, output);
        }
        items.setStackInSlot(INPUT, ItemStack.EMPTY);
        progress = 0;
    }

    @Override
    public List<Component> hydrometerLines() {
        String h = "hydrometer.seedtocellar.";
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.seedtocellar.kiln.roast", roast.displayName()));
        if (findRecipe().isPresent()) lines.add(Component.translatable(h + "progress", Math.round(progressFraction() * 100)));
        else if (!items.getStackInSlot(INPUT).isEmpty()) lines.add(Component.translatable(h + "kiln.no_recipe"));
        lines.add(Component.translatable(isBurning() ? h + "kiln.burning" : h + "kiln.cold"));
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
        return new KilnMenu(id, inventory, this, items, data);
    }

    // --- save / load -----------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
        tag.putInt("Progress", progress);
        tag.putInt("TotalTime", totalTime);
        tag.putString("Roast", roast.getSerializedName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
        progress = tag.getInt("Progress");
        totalTime = tag.getInt("TotalTime");
        roast = RoastLevel.byName(tag.getString("Roast"));
    }

    // --- automation ------------------------------------------------------------------------

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP) return inputCap.cast();
            if (side == Direction.DOWN) return outputCap.cast();
            return fuelCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCap.invalidate();
        fuelCap.invalidate();
        outputCap.invalidate();
    }
}
