package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Kiln screen contents: input, fuel, output, and three roast buttons (menu button ids 0-2). */
public class KilnMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final KilnBlockEntity kiln;

    /** Client side: built from the position the server sent. */
    public KilnMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof KilnBlockEntity k ? k : null,
                new ItemStackHandler(3), new SimpleContainerData(5));
    }

    public KilnMenu(int id, Inventory inventory, KilnBlockEntity kiln, IItemHandler items, ContainerData data) {
        super(ModMenus.KILN.get(), id);
        this.kiln = kiln;
        this.data = data;
        this.access = kiln != null ? ContainerLevelAccess.create(kiln.getLevel(), kiln.getBlockPos()) : ContainerLevelAccess.NULL;

        addSlot(new SlotItemHandler(items, KilnBlockEntity.INPUT, 56, 17));
        addSlot(new SlotItemHandler(items, KilnBlockEntity.FUEL, 56, 53));
        addSlot(new SlotItemHandler(items, KilnBlockEntity.OUTPUT, 116, 35) {
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

    public int burnTime() { return data.get(0); }
    public int burnDuration() { return data.get(1); }
    public int progress() { return data.get(2); }
    public int totalTime() { return data.get(3); }

    public RoastLevel roast() {
        return RoastLevel.values()[Math.floorMod(data.get(4), RoastLevel.values().length)];
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (kiln != null && id >= 0 && id < RoastLevel.values().length) {
            kiln.setRoast(RoastLevel.values()[id]);
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
        int machineSlots = 3;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (ForgeHooks.getBurnTime(stack, null) > 0) {
            if (!moveItemStackTo(stack, KilnBlockEntity.FUEL, KilnBlockEntity.FUEL + 1, false)
                    && !moveItemStackTo(stack, KilnBlockEntity.INPUT, KilnBlockEntity.INPUT + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, KilnBlockEntity.INPUT, KilnBlockEntity.INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.KILN.get());
    }
}
