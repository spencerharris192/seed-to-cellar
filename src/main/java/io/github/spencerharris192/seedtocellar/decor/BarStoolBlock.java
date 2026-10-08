package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Bar Stool (GDD section 17.3): a tall stool with a leather cushion, just the height for a Bar Counter. Right-click it
 * to sit (on an invisible {@link SeatEntity}); sneak to stand up. One sitter at a time.
 */
public class BarStoolBlock extends Block {
    /** How high the cushion's top is, in blocks: where the sitter's hips go. */
    public static final double SEAT = 14 / 16.0;
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 14, 13);

    public BarStoolBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSecondaryUseActive() || player.isPassenger() || !player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty()) return InteractionResult.PASS;   // taken
        SeatEntity seat = new SeatEntity(level, pos, SEAT);
        level.addFreshEntity(seat);
        player.startRiding(seat);
        return InteractionResult.CONSUME;
    }
}
