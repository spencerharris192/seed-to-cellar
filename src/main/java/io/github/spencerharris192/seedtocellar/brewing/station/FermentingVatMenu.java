package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Vat screen contents: yeast slot, lees slot, and a lid button (menu button id 0). */
public class FermentingVatMenu extends AbstractContainerMenu {
    public static final int BUTTON_LID = 0;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final FermentingVatBlockEntity vat;

    public FermentingVatMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof FermentingVatBlockEntity v ? v : null,
                new ItemStackHandler(2), new SimpleContainerData(5));
    }

    public FermentingVatMenu(int id, Inventory inventory, FermentingVatBlockEntity vat, IItemHandler items, ContainerData data) {
        super(ModMenus.FERMENTING_VAT.get(), id);
        this.vat = vat;
        this.data = data;
        this.access = vat != null ? ContainerLevelAccess.create(vat.getLevel(), vat.getBlockPos()) : ContainerLevelAccess.NULL;
        addSlot(new SlotItemHandler(items, FermentingVatBlockEntity.YEAST, 44, 17));
        addSlot(new SlotItemHandler(items, FermentingVatBlockEntity.LEES, 44, 53) {
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

    public FermentingVatBlockEntity vat() {
        return vat;
    }

    public float progress() { return data.get(0) / 1000F; }
    public boolean fermenting() { return data.get(1) != 0; }
    public Temperature temperature() { return Temperature.values()[Math.floorMod(data.get(2), Temperature.values().length)]; }
    public boolean temperatureOk() { return data.get(3) != 0; }
    public boolean open() { return data.get(4) != 0; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_LID && vat != null) {
            vat.toggleLid(player);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, FermentingVatBlockEntity.YEAST, FermentingVatBlockEntity.YEAST + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.FERMENTING_VAT.get());
    }
}
