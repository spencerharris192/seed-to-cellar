package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

/**
 * What you plant a trellis vine from (a hop rhizome, a grape cutting): use it on the bottom trellis
 * of a stack, the one standing on dirt, grass or farmland.
 */
public class TrellisPlantItem extends Item {
    private final Supplier<? extends Block> vine;

    public TrellisPlantItem(Supplier<? extends Block> vine, Properties properties) {
        super(properties);
        this.vine = vine;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState trellis = level.getBlockState(pos);
        if (!trellis.is(ModBlocks.TRELLIS.get()) || !TrellisVineBlock.isSoil(level.getBlockState(pos.below()))
                || !(vine.get() instanceof TrellisVineBlock block)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, block.planted(trellis.getValue(TrellisBlock.AXIS)), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, pos);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
