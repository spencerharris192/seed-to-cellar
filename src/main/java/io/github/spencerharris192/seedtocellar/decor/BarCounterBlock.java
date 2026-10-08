package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * A Bar Counter (GDD section 17.3): a paneled wooden bar with a brass foot rail, its front (FACING) toward the
 * customers. Its top is a full surface, so drinks set down on it stand there. Side by side, counters make one long bar,
 * and like stairs they turn corners (SHAPE): a corner sticking out toward the customers is paneled on both sides; one
 * turning in, round them, closes up to a notch where the two fronts meet. Any woods join.
 */
public class BarCounterBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    /** Left and right as the bartender sees it, facing the customers: OUTER_LEFT is paneled on the front and the left. */
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;

    public BarCounterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SHAPE, StairsShape.STRAIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    /** Placed with its front toward you: you're the customer. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return state.setValue(SHAPE, shape(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction.getAxis().isHorizontal() ? state.setValue(SHAPE, shape(state, level, pos))
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    /**
     * How it meets the counters round it, the way stairs do. A counter behind it facing across makes it the corner of an
     * L sticking out toward the customers (fronts on both outside faces); one in front of it facing across, the inside
     * corner of an L wrapped round them. Not if it's in the middle of a straight run that the other counter only ends at.
     */
    public static StairsShape shape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction front = state.getValue(FACING);
        BlockState behind = level.getBlockState(pos.relative(front.getOpposite()));
        if (behind.getBlock() instanceof BarCounterBlock) {
            Direction side = behind.getValue(FACING);
            if (side.getAxis() != front.getAxis() && !sameRun(state, level, pos.relative(side))) {
                return side == front.getCounterClockWise() ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }
        BlockState ahead = level.getBlockState(pos.relative(front));
        if (ahead.getBlock() instanceof BarCounterBlock) {
            Direction side = ahead.getValue(FACING);
            if (side.getAxis() != front.getAxis() && !sameRun(state, level, pos.relative(side.getOpposite()))) {
                return side == front.getCounterClockWise() ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    /** A counter at `pos` facing the same way: the run carries on through there. */
    private static boolean sameRun(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState other = level.getBlockState(pos);
        return other.getBlock() instanceof BarCounterBlock && other.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /** Mirrored, the front flips if it points across the mirror, and a corner always turns the other way. */
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        StairsShape flipped = switch (state.getValue(SHAPE)) {
            case OUTER_LEFT -> StairsShape.OUTER_RIGHT;
            case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
            case INNER_LEFT -> StairsShape.INNER_RIGHT;
            case INNER_RIGHT -> StairsShape.INNER_LEFT;
            default -> StairsShape.STRAIGHT;
        };
        return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(SHAPE, flipped);
    }
}
