package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Trellis item. Like scaffolding: use it on any trellis in a column (bare or with hops) to add
 * one on top of the column. Sneak to place it normally, e.g. beside another trellis.
 */
public class TrellisItem extends BlockItem {
    public TrellisItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos top = context.getClickedPos();
        if ((player != null && player.isSecondaryUseActive()) || !(level.getBlockState(top).getBlock() instanceof TrellisBlock)) {
            return super.useOn(context);
        }
        while (level.getBlockState(top.above()).getBlock() instanceof TrellisBlock) top = top.above();
        // As if the player had clicked the top face of the column's top trellis.
        return place(BlockPlaceContext.at(new BlockPlaceContext(context), top, Direction.UP));
    }
}
