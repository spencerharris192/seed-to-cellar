package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

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

    private final StationItems items = new StationItems(3) {
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
                case FUEL -> Fuel.isFuel(stack);
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

    private final ResourceHandler<ItemResource> inputSide = RangedResourceHandler.ofSingleIndex(items, INPUT);
    private final ResourceHandler<ItemResource> fuelSide = RangedResourceHandler.ofSingleIndex(items, FUEL);
    private final ResourceHandler<ItemResource> outputSide = RangedResourceHandler.ofSingleIndex(items, OUTPUT);

    public KilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KILN.get(), pos, state);
    }

    public StationItems items() {
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
            if (kiln.burnTime <= 0) kiln.consumeFuel((ServerLevel) level);
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
        return Recipes.stream(level, ModRecipes.KILNING.get()).filter(r -> r.matches(input, roast)).findFirst();
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
        return ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void consumeFuel(ServerLevel level) {
        ItemStack fuel = items.getStackInSlot(FUEL);
        int time = Fuel.burnTicks(level, this, fuel);
        if (time <= 0) return;
        burnTime = burnDuration = time;
        if (fuel.getItem().getCraftingRemainder() != null) {
            items.setStackInSlot(FUEL, fuel.getItem().getCraftingRemainder().create());
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(i));
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output.child("Items"));
        output.putInt("BurnTime", burnTime);
        output.putInt("BurnDuration", burnDuration);
        output.putInt("Progress", progress);
        output.putInt("TotalTime", totalTime);
        output.putString("Roast", roast.getSerializedName());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input.childOrEmpty("Items"));
        burnTime = input.getIntOr("BurnTime", 0);
        burnDuration = input.getIntOr("BurnDuration", 0);
        progress = input.getIntOr("Progress", 0);
        totalTime = input.getIntOr("TotalTime", 0);
        roast = RoastLevel.byName(input.getStringOr("Roast", ""));
    }

    // --- automation ------------------------------------------------------------------------

    /** Hoppers and pipes: input from above, output from below, fuel from the sides. */
    public ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        if (side == Direction.UP) return inputSide;
        if (side == Direction.DOWN) return outputSide;
        return fuelSide;
    }
}
