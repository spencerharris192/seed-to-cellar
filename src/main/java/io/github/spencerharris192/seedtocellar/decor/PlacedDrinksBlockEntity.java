package io.github.spencerharris192.seedtocellar.decor;

import io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** The drinks set down on one spot, in the order they were put there (up to four), each keeping its stars and age. */
public class PlacedDrinksBlockEntity extends SyncedBlockEntity {
    public static final int SLOTS = 4;
    private final List<ItemStack> drinks = new ArrayList<>();

    public PlacedDrinksBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLACED_DRINKS.get(), pos, state);
    }

    public List<ItemStack> drinks() {
        return drinks;
    }

    public int count() {
        return drinks.size();
    }

    /** Adds one drink, if there's room. */
    public boolean add(ItemStack drink) {
        if (drinks.size() >= SLOTS || drink.isEmpty()) return false;
        drinks.add(drink.copyWithCount(1));
        sync();
        return true;
    }

    /** Takes back the drink set down last. */
    public ItemStack takeLast() {
        if (drinks.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = drinks.remove(drinks.size() - 1);
        sync();
        return taken;
    }

    public ItemStack last() {
        return drinks.isEmpty() ? ItemStack.EMPTY : drinks.get(drinks.size() - 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemStack drink : drinks) list.add(drink.save(new CompoundTag()));
        tag.put("Drinks", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        drinks.clear();
        for (Tag entry : tag.getList("Drinks", Tag.TAG_COMPOUND)) {
            ItemStack drink = ItemStack.of((CompoundTag) entry);
            if (!drink.isEmpty() && drinks.size() < SLOTS) drinks.add(drink);
        }
    }
}
