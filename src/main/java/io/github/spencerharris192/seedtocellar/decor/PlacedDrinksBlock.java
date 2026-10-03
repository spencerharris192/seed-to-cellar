package io.github.spencerharris192.seedtocellar.decor;

import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Drinks set down on a surface (GDD section 16): sneak and right-click the top of a block with a drink, and it stands
 * there as the same little 3D bottle, mug or flask the shelves show. Up to four share a spot (click it with another);
 * an empty hand takes the last one back. Each keeps its stars and age; breaking the spot drops them all.
 */
public class PlacedDrinksBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty DRINKS = IntegerProperty.create("drinks", 1, PlacedDrinksBlockEntity.SLOTS);
    /** Where each drink stands, by how many there are (x, z in pixels, facing north), and each one's turn. */
    public static final float[][][] SPOTS = {
            {{8, 8}},
            {{5.5F, 8}, {10.5F, 8.5F}},
            {{5, 6}, {11, 6.5F}, {8, 11}},
            {{5, 5}, {11, 5.5F}, {5.5F, 11}, {11, 11}}};
    public static final float[] TURN = {0, 28, -22, 41};
    private static final VoxelShape[] SHAPES = new VoxelShape[PlacedDrinksBlockEntity.SLOTS];

    static {
        for (int n = 0; n < SHAPES.length; n++) {
            VoxelShape shape = Shapes.empty();
            for (float[] spot : SPOTS[n]) shape = Shapes.or(shape, box(spot[0] - 2, 0, spot[1] - 2, spot[0] + 2, 8, spot[1] + 2));
            SHAPES[n] = shape;
        }
    }

    public PlacedDrinksBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DRINKS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DRINKS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;   // only the drinks show, drawn by their renderer
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(DRINKS) - 1];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupport(level, pos.below());
    }

    /** A drink can stand on any block whose top is solid in the middle (a slab, a counter, a table...). */
    public static boolean canSupport(LevelReader level, BlockPos below) {
        return Block.canSupportCenter(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    /**
     * Sets `drink` down at the spot a click on `context` points to (adding to drinks already there). True if it found a
     * place; the caller takes it from the hand.
     */
    public static boolean place(BlockPlaceContext context, ItemStack drink) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState there = level.getBlockState(pos);
        if (there.getBlock() instanceof PlacedDrinksBlock) return add(level, pos, there, drink);
        if (!context.canPlace() || !canSupport(level, pos.below())) return false;
        if (level.isClientSide) return true;
        BlockState state = io.github.spencerharris192.seedtocellar.registry.ModBlocks.PLACED_DRINKS.get().defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof PlacedDrinksBlockEntity spot) spot.add(drink.copyWithCount(1));
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.3F);
        return true;
    }

    /** Adds `drink` to the drinks at `pos` if there's room. True if it was added (the caller takes it from the hand). */
    public static boolean add(Level level, BlockPos pos, BlockState state, ItemStack drink) {
        if (state.getValue(DRINKS) >= PlacedDrinksBlockEntity.SLOTS) return false;
        if (level.isClientSide) return true;
        if (!(level.getBlockEntity(pos) instanceof PlacedDrinksBlockEntity spot) || !spot.add(drink.copyWithCount(1))) return false;
        level.setBlock(pos, state.setValue(DRINKS, spot.count()), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.3F);
        return true;
    }

    /** Clicked with a drink: another joins them. With an empty hand: the last one set down comes back. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof DrinkItem) {
            if (!add(level, pos, state, held)) return InteractionResult.PASS;
            if (!level.isClientSide && !player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof PlacedDrinksBlockEntity spot)) return InteractionResult.PASS;
        ItemStack taken = spot.takeLast();
        if (taken.isEmpty()) return InteractionResult.PASS;
        player.setItemInHand(hand, taken);
        level.playSound(null, pos, SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.6F, 1.4F);
        if (spot.count() == 0) level.removeBlock(pos, false);
        else level.setBlock(pos, state.setValue(DRINKS, spot.count()), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }

    /** Pick block gives the drink set down last. */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return level.getBlockEntity(pos) instanceof PlacedDrinksBlockEntity spot ? spot.last().copy() : ItemStack.EMPTY;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PlacedDrinksBlockEntity spot) {
            for (ItemStack drink : spot.drinks()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drink);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlacedDrinksBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
