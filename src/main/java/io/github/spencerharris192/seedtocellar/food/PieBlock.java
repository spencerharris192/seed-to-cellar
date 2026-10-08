package io.github.spencerharris192.seedtocellar.food;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A placed pie (GDD section 15.2): four slices. Right-click to take one; the pie loses a quarter
 * each time, and the last slice takes the plate with it. Like a cake, it needs something solid
 * underneath. Four slices craft back into a whole pie, so pies travel well either way.
 */
public class PieBlock extends Block {
    public static final int SLICES = 4;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);
    public static final int HEIGHT = 4;
    /** The four quarters, in the order they're taken: north-west, north-east, south-east, south-west. */
    public static final int[][] QUARTERS = {{2, 2, 8, 8}, {8, 2, 14, 8}, {8, 8, 14, 14}, {2, 8, 8, 14}};
    private static final VoxelShape[] SHAPES = new VoxelShape[SLICES];

    static {
        for (int bites = 0; bites < SLICES; bites++) {
            VoxelShape shape = Shapes.empty();
            for (int q = bites; q < SLICES; q++) {
                int[] b = QUARTERS[q];
                shape = Shapes.or(shape, box(b[0], 0, b[1], b[2], HEIGHT, b[3]));
            }
            SHAPES[bites] = shape;
        }
    }

    private final Supplier<? extends Item> slice;

    public PieBlock(Properties properties, Supplier<? extends Item> slice) {
        super(properties);
        this.slice = slice;
        registerDefaultState(stateDefinition.any().setValue(BITES, 0));
    }

    public Item slice() {
        return slice.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BITES)];
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack piece = new ItemStack(slice.get());
        io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity.give(player, piece);
        int bites = state.getValue(BITES);
        if (bites + 1 >= SLICES) {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        } else {
            level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    /** Comparators read how much pie is left (like a cake). */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return (SLICES - state.getValue(BITES)) * 3;
    }
}
