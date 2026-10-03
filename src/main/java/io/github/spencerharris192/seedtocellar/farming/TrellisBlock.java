package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A garden lattice panel that climbing crops (hops, grapes) grow up.
 * <ul>
 *   <li>Stack them to make taller supports (see {@link TrellisItem}).</li>
 *   <li>The panel faces the player who places it, or lines up with the trellis below.</li>
 *   <li>It stands on the ground (soil, or any block with a solid top) or on another trellis.
 *       Take away the support and the whole column above breaks, one piece after another,
 *       dropping its items (like scaffolding or sugar cane).</li>
 * </ul>
 */
public class TrellisBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape SHAPE_X = box(0, 0, 7, 16, 16, 9);
    private static final VoxelShape SHAPE_Z = box(7, 0, 0, 9, 16, 16);
    /** Ticks between one piece of a column breaking and the next: fast, but visibly top-down. */
    private static final int COLLAPSE_DELAY = 2;

    public TrellisBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // On top of another trellis: line up with it, so the column stays straight and hops can climb it.
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        if (below.getBlock() instanceof TrellisBlock) return defaultBlockState().setValue(AXIS, below.getValue(AXIS));
        return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    // --- support -----------------------------------------------------------------------------

    /** Whether a trellis at {@code pos} has something to stand on. */
    public static boolean isSupported(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.getBlock() instanceof TrellisBlock || TrellisVineBlock.isSoil(below)
                || below.isFaceSturdy(level, belowPos, Direction.UP, SupportType.CENTER);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !isSupported(level, pos)) level.scheduleTick(pos, this, COLLAPSE_DELAY);
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isSupported(level, pos)) level.destroyBlock(pos, true); // the piece above notices and follows
    }
}
