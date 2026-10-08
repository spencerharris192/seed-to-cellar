package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Drying Rack (GDD section 7): a wooden frame with two rails; up to four items hang from them
 * and dry in the air. Right-click with something dryable to hang it (sneak to hang as many as
 * fit); right-click with an empty hand to take a dried item (sneak to take anything back).
 * Hoppers put dryable items in and take dried ones out from below.
 */
public class DryingRackBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    // The frame as drawn: two posts, their feet, and the two rails with the space between them filled in
    // (a 1-pixel pane, so the outline matches the rails and the rack is easy to click). Facing north, the
    // rails run east-west; facing east or west, the same shape turned a quarter.
    private static final VoxelShape SHAPE_NS = Shapes.or(box(1, 0, 7, 3, 15, 9), box(13, 0, 7, 15, 15, 9),
            box(0, 0, 4, 4, 1, 12), box(12, 0, 4, 16, 1, 12), box(3, 7, 7.5, 13, 14, 8.5));
    private static final VoxelShape SHAPE_EW = Shapes.or(box(7, 0, 1, 9, 15, 3), box(7, 0, 13, 9, 15, 15),
            box(4, 0, 0, 12, 1, 4), box(4, 0, 12, 12, 1, 16), box(7.5, 7, 3, 8.5, 14, 13));

    public DryingRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && DryingRackBlockEntity.recipeFor(level, held).isPresent()) {
            if (rack.firstFree() < 0) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                int hung = 0;
                do {
                    if (!rack.hang(held)) break;
                    hung++;
                    if (!player.getAbilities().instabuild) held.shrink(1);
                } while (player.isSecondaryUseActive() && !held.isEmpty());
                if (hung > 0) level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ItemStack taken = rack.take(player.isSecondaryUseActive());
            if (taken.isEmpty()) return InteractionResult.PASS;
            if (!player.getInventory().add(taken)) popResource(level, pos, taken);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) rack.check();
    }


    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** Comparators read how many dried items are waiting (0-4, as 0-15). */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) return 0;
        int done = 0;
        for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) if (rack.isDone(slot)) done++;
        return done == 0 ? 0 : Math.min(15, 3 + done * 3);
    }
}
