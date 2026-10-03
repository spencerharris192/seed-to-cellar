package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;

/**
 * Compost (GDD section 6.5): use it on farmland to make Fertile Farmland, or on fertile
 * farmland to top it back up to full. Using it on a crop works too: it goes into the soil.
 * Dispensers can spread it (see SeedToCellar's common setup).
 */
public class CompostItem extends Item {
    public CompostItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!spread(level, context.getClickedPos())) return InteractionResult.PASS;
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Works the compost into the soil at {@code pos}, or under the crop there. False if there's no farmland. */
    public static boolean spread(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof BushBlock) pos = pos.below();   // the crop: feed its soil
        return FertileFarmlandBlock.fertilize(level, pos, ModBlocks.FERTILE_FARMLAND.get());
    }
}
