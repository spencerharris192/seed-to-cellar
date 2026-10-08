package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/** Kettle screen contents. The liquid gauge reads the (synced) block entity directly. */
public class BrewKettleMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BrewKettleBlockEntity kettle;

    public BrewKettleMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof BrewKettleBlockEntity k ? k : null,
                new StationItems(BrewKettleBlockEntity.SLOTS), new SimpleContainerData(4));
    }

    // Slot positions (the item, one pixel inside each slot frame drawn by the GUI texture).
    private static final int[][] INGREDIENT_XY = {{35, 17}, {53, 17}, {35, 35}, {53, 35}};
    public static final int CONTAINER_X = 125, CONTAINER_Y = 13;
    public static final int OUTPUT_X = 125, OUTPUT_Y = 35;

    public BrewKettleMenu(int id, Inventory inventory, BrewKettleBlockEntity kettle, StationItems items, ContainerData data) {
        super(ModMenus.BREW_KETTLE.get(), id);
        this.kettle = kettle;
        this.data = data;
        this.access = kettle != null ? ContainerLevelAccess.create(kettle.getLevel(), kettle.getBlockPos()) : ContainerLevelAccess.NULL;
        for (int i = 0; i < BrewKettleBlockEntity.INGREDIENTS; i++) {
            addSlot(new ResourceHandlerSlot(items, items::set, i, INGREDIENT_XY[i][0], INGREDIENT_XY[i][1]));
        }
        addSlot(new ResourceHandlerSlot(items, items::set, BrewKettleBlockEntity.CONTAINER, CONTAINER_X, CONTAINER_Y));
        addSlot(new ResourceHandlerSlot(items, items::set, BrewKettleBlockEntity.OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }

    public BrewKettleBlockEntity kettle() {
        return kettle;
    }

    public int progress() { return data.get(0); }
    public int total() { return data.get(1); }
    public BrewKettleBlockEntity.Stage stage() {
        return BrewKettleBlockEntity.Stage.values()[Math.floorMod(data.get(2), BrewKettleBlockEntity.Stage.values().length)];
    }
    public boolean heated() { return data.get(3) != 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = BrewKettleBlockEntity.SLOTS;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, BrewKettleBlockEntity.CONTAINER + 1, false)) {   // ingredients, then the bowl slot
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BREW_KETTLE.get());
    }
}
