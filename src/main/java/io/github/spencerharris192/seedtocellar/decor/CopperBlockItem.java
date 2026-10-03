package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** A copper station's item: named for its weathering and wax ("Waxed Exposed Brew Kettle"), which it places back as it was. */
public class CopperBlockItem extends BlockItem {
    public CopperBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return CopperWeathering.name(stack, super.getName(stack));
    }

    /** The two-block Pot Still's. */
    public static class DoubleHigh extends DoubleHighBlockItem {
        public DoubleHigh(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public Component getName(ItemStack stack) {
            return CopperWeathering.name(stack, super.getName(stack));
        }
    }
}
