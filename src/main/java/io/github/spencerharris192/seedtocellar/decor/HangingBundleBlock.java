package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A hanging bundle (GDD section 17.3): hop cones, lavender, a garlic braid or a chili string, made from nine of the crop.
 * Placed under a block it hangs from a string; placed on a wall it hangs from a nail, facing out. It needs its support
 * and falls (dropping itself) without it. Purely decorative: you walk through it.
 */
public class HangingBundleBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** On a wall (from a nail, FACING away from the wall) rather than under a block. */
    public static final BooleanProperty WALL = BooleanProperty.create("wall");
    private static final VoxelShape CEILING = box(5, 0, 5, 11, 16, 11);
    private static final VoxelShape[] WALL_SHAPES = {
            box(5, 0, 10, 11, 15, 16),   // facing north (wall to the south)
            box(0, 0, 5, 6, 15, 11),     // facing east
            box(5, 0, 0, 11, 15, 6),     // facing south
            box(10, 0, 5, 16, 15, 11)};  // facing west

    public HangingBundleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WALL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(WALL)) return CEILING;
        return WALL_SHAPES[switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        }];
    }

    /** Under a block (clicking its underside), or on a wall's side; never standing on the floor. */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockState state;
        if (face == Direction.DOWN) {
            state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        } else if (face.getAxis().isHorizontal()) {
            state = defaultBlockState().setValue(WALL, true).setValue(FACING, face);
        } else {
            return null;
        }
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!state.getValue(WALL)) return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction support = state.getValue(WALL) ? state.getValue(FACING).getOpposite() : Direction.UP;
        return direction == support && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
