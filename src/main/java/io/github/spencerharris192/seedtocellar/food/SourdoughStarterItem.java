package io.github.spencerharris192.seedtocellar.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Sourdough Starter: a living culture. Mixing it into dough uses only a little, so crafting gives
 * the starter back, the way a baker keeps a crock of it going. (Stations that use it, like the
 * Preserving Jar turning it into Ale Yeast, still consume it.)
 */
public class SourdoughStarterItem extends Item {
    public SourdoughStarterItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }
}
