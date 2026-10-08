package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Decor hung on a wall's side (the Hop Garland, the Tavern Sign): FACING points out from the wall it hangs on, it needs
 * that wall and falls (dropping itself) without it, and you walk through it.
 */
public class WallDecorBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    /** `northShape`: its outline facing north (the wall to the south); turned for the other facings. */
    public WallDecorBlock(Properties properties, VoxelShape northShape) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        for (Direction facing : Direction.Plane.HORIZONTAL) shapes.put(facing, turn(northShape, facing));
    }

    /** A north-facing shape turned to face `facing`. */
    private static VoxelShape turn(VoxelShape shape, Direction facing) {
        VoxelShape[] out = {Shapes.empty()};
        shape.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            double[] b = switch (facing) {
                case SOUTH -> new double[]{1 - x1, y0, 1 - z1, 1 - x0, y1, 1 - z0};
                case EAST -> new double[]{1 - z1, y0, x0, 1 - z0, y1, x1};
                case WEST -> new double[]{z0, y0, 1 - x1, z1, y1, 1 - x0};
                default -> new double[]{x0, y0, z0, x1, y1, z1};
            };
            out[0] = Shapes.or(out[0], Shapes.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        });
        return out[0];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null;
        BlockState state = defaultBlockState().setValue(FACING, face);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
