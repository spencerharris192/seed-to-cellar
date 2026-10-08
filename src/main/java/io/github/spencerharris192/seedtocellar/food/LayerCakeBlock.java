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
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A tall layer cake on the table (GDD section 15.2: Black Forest Cake): six slices, cut from the west side one at a time
 * like vanilla's cake, each slice taken as an item. The last slice takes the whole cake. It needs something solid under it.
 */
public class LayerCakeBlock extends Block {
    public static final int SLICES = 6;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);
    /** The cake's footprint (x and z 2-14) and height; each slice is 2 pixels of its width. */
    public static final int MIN = 2, MAX = 14, HEIGHT = 8, SLICE_WIDTH = 2;
    private static final VoxelShape[] SHAPES = new VoxelShape[SLICES];

    static {
        for (int bites = 0; bites < SLICES; bites++) SHAPES[bites] = box(MIN + SLICE_WIDTH * bites, 0, MIN, MAX, HEIGHT, MAX);
    }

    private final Supplier<? extends Item> slice;

    public LayerCakeBlock(Properties properties, Supplier<? extends Item> slice) {
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
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.9F);
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

    /** Comparators read how much cake is left. */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return (SLICES - state.getValue(BITES)) * 2;
    }
}
