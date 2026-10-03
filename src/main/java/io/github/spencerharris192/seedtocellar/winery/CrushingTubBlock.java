package io.github.spencerharris192.seedtocellar.winery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crushing Tub (GDD section 7): a low, wide wooden tub. Put fruit in (right-click, or a hopper), then
 * jump in and stomp: every landing crushes toward the next fruit, and its juice (or, for red grapes,
 * must) collects in the tub. Buckets, bottles and pipes take it out. Stomping is gentle on the legs.
 */
public class CrushingTubBlock extends BaseEntityBlock {
    /** The fruit's surface: high enough that whoever stands in the tub is standing on the tub. */
    public static final int FLOOR = 5;
    public static final int RIM = 12;
    private static final VoxelShape OUTLINE = box(0, 0, 0, 16, RIM, 16);
    private static final VoxelShape COLLISION = Shapes.or(box(0, 0, 0, 16, FLOOR, 16),
            box(0, FLOOR, 0, 2, RIM, 16), box(14, FLOOR, 0, 16, RIM, 16), box(2, FLOOR, 0, 14, RIM, 2), box(2, FLOOR, 14, 14, RIM, 16));

    public CrushingTubBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrushingTubBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub ? tub.onUse(player, hand) : InteractionResult.PASS;
    }

    /** Landing on the fruit (a jump, not just walking in) is a stomp. Soft fruit: almost no fall damage. */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide && fallDistance > 0.4F && entity instanceof LivingEntity
                && level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub) {
            tub.stomp(entity);
        }
        super.fallOn(level, state, pos, entity, fallDistance * 0.2F);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Comparators read how full of juice the tub is (0-15). */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub
                ? (int) Math.ceil(15.0 * tub.tank().getFluidAmount() / CrushingTubBlockEntity.CAPACITY) : 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CrushingTubBlockEntity tub) tub.dropContents();
        super.onRemove(state, level, pos, newState, moved);
    }
}
