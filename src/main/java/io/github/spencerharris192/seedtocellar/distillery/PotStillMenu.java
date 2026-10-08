package io.github.spencerharris192.seedtocellar.distillery;

import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import io.github.spencerharris192.seedtocellar.brewing.station.StationItems;
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

/**
 * Still screen contents: the charcoal filter slot, the four Gin Basket slots (only while a basket is fitted), and a
 * "Run again" button (menu button 0) that pours the spirit back into the pot.
 */
public class PotStillMenu extends AbstractContainerMenu {
    public static final int BUTTON_RUN_AGAIN = 0;
    public static final int FILTER_X = 39, FILTER_Y = 54;
    public static final int[][] BASKET_XY = {{30, 17}, {48, 17}, {30, 35}, {48, 35}};
    public static final int INVENTORY_Y = 104;
    private static final int OWN_SLOTS = PotStillBlockEntity.SLOTS;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final PotStillBlockEntity still;

    public PotStillMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof PotStillBlockEntity s ? s : null,
                new StationItems(PotStillBlockEntity.SLOTS), new SimpleContainerData(5));
    }

    public PotStillMenu(int id, Inventory inventory, PotStillBlockEntity still, StationItems items, ContainerData data) {
        super(ModMenus.POT_STILL.get(), id);
        this.still = still;
        this.data = data;
        this.access = still != null ? ContainerLevelAccess.create(still.getLevel(), still.getBlockPos()) : ContainerLevelAccess.NULL;
        addSlot(new ResourceHandlerSlot(items, items::set, PotStillBlockEntity.FILTER, FILTER_X, FILTER_Y));
        for (int i = 0; i < PotStillBlockEntity.BASKET_SLOTS; i++) {
            addSlot(new ResourceHandlerSlot(items, items::set, PotStillBlockEntity.BASKET + i, BASKET_XY[i][0], BASKET_XY[i][1]) {
                @Override
                public boolean isActive() {
                    return hasBasket();
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, INVENTORY_Y + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, INVENTORY_Y + 58));
        addDataSlots(data);
    }

    public PotStillBlockEntity still() {
        return still;
    }

    public int progress() { return data.get(0); }
    public int total() { return data.get(1); }
    public boolean heated() { return data.get(2) != 0; }
    public boolean running() { return data.get(3) != 0; }
    public boolean hasBasket() { return data.get(4) != 0; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id == BUTTON_RUN_AGAIN && still != null && still.runAgain();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < OWN_SLOTS) {
            if (!moveItemStackTo(stack, OWN_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, OWN_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.POT_STILL.get());
    }
}
