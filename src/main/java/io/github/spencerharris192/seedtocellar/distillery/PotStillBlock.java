package io.github.spencerharris192.seedtocellar.distillery;

import io.github.spencerharris192.seedtocellar.decor.CopperWeathering;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Pot Still (GDD section 7): a copper pot two blocks tall that sits over heat like the Brew Kettle. The lower half is the
 * pot (it holds the block entity); the upper half is the head and the swan neck, whose pipe comes down into a glass spirit
 * safe on the front. Right-click with a bucket of wash, wine or spirit to fill the pot; with an empty bucket or bottle to
 * take the spirit (or, once that's gone, the stillage); with an empty hand to open the screen. Either half works.
 */
public class PotStillBlock extends BaseEntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    /** A Gin Basket is fitted on the swan neck (both halves carry it; the head shows it). */
    public static final BooleanProperty BASKET = BooleanProperty.create("basket");

    private static final Map<Direction, VoxelShape> LOWER = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> UPPER = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // Drawn facing north (the spirit safe at the front, z 0-3), then turned.
            LOWER.put(facing, Shapes.or(rotated(facing, 2, 0, 3, 14, 16, 15), rotated(facing, 5, 0, 0, 11, 6, 3),
                    rotated(facing, 7, 6, 1, 9, 16, 3)));
            UPPER.put(facing, Shapes.or(rotated(facing, 5, 0, 6, 11, 13, 12), rotated(facing, 7, 0, 1, 9, 11, 6),
                    rotated(facing, 5.5, 7.5, 1.5, 10.5, 12.5, 5)));
        }
    }

    public PotStillBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false).setValue(BASKET, false)
                .setValue(CopperWeathering.STAGE, CopperWeathering.Stage.UNAFFECTED).setValue(CopperWeathering.WAXED, false));
    }

    /** A box given facing north, turned to face {@code facing}. */
    private static VoxelShape rotated(Direction facing, double x0, double y0, double z0, double x1, double y1, double z1) {
        return switch (facing) {
            case SOUTH -> box(16 - x1, y0, 16 - z1, 16 - x0, y1, 16 - z0);
            case WEST -> box(z0, y0, 16 - x1, z1, y1, 16 - x0);
            case EAST -> box(16 - z1, y0, x0, 16 - z0, y1, x1);
            default -> box(x0, y0, z0, x1, y1, z1);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, ACTIVE, BASKET, CopperWeathering.STAGE, CopperWeathering.WAXED);
    }

    // --- its copper weathers (the pot ticks; the head follows it) -------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !isUpper(state) && CopperWeathering.weathers(state);
    }

    @Override
    public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        CopperWeathering.randomTick(state, level, pos, random);
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, net.minecraft.world.item.context.UseOnContext context,
                                           net.minecraftforge.common.ToolAction action, boolean simulate) {
        BlockState axed = CopperWeathering.axed(state, action);
        return axed != null ? axed : super.getToolModifiedState(state, context, action, simulate);
    }

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    /** The pot (lower half) of the still at {@code pos}. */
    public static BlockPos lowerPos(BlockPos pos, BlockState state) {
        return isUpper(state) ? pos.below() : pos;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (isUpper(state) ? UPPER : LOWER).get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // --- two halves --------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!isUpper(state)) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && !isUpper(below);
    }

    /**
     * Losing either half takes the other with it (the lower half drops the still, like a door); otherwise each half keeps
     * the other's weathering and wax, whichever half changed.
     */
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        boolean upper = isUpper(state);
        if (direction == (upper ? Direction.DOWN : Direction.UP)) {
            if (!(neighbor.is(this) && isUpper(neighbor) != upper)) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            state = CopperWeathering.matching(state, neighbor);
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && isUpper(state)) {
            // In creative, breaking the head takes the pot without dropping a still.
            BlockPos lower = pos.below();
            BlockState pot = level.getBlockState(lower);
            if (pot.is(this) && !isUpper(pot)) {
                level.setBlock(lower, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, lower, Block.getId(pot));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !isUpper(state) && level.getBlockEntity(pos) instanceof PotStillBlockEntity still) {
            still.dropContents();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    // --- the pot -----------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isUpper(state) ? null : new PotStillBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || isUpper(state) ? null
                : createTickerHelper(type, ModBlockEntities.POT_STILL.get(), PotStillBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos lower = lowerPos(pos, state);
        if (!(level.getBlockEntity(lower) instanceof PotStillBlockEntity still)) return InteractionResult.PASS;
        InteractionResult waxed = CopperWeathering.wax(state, level, pos, player, player.getItemInHand(hand));
        if (waxed != null) return waxed;
        if (CopperWeathering.axeWorks(state, player.getItemInHand(hand))) return InteractionResult.PASS;   // the axe scrapes it
        if (still.useHeldItem(player, hand)) return InteractionResult.sidedSuccess(level.isClientSide);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, still, lower);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** While it runs: drips into the spirit safe and the odd bubble below, a wisp of vapor from the head above. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockState pot = level.getBlockState(lowerPos(pos, state));
        if (!pot.is(this) || !pot.getValue(ACTIVE)) return;
        Direction front = state.getValue(FACING);
        if (isUpper(state)) {
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 0, 0.02, 0);
            }
            return;
        }
        // The pipe meets the safe 2 pixels in from the front.
        double x = pos.getX() + 0.5 + front.getStepX() * (0.5 - 2 / 16.0);
        double z = pos.getZ() + 0.5 + front.getStepZ() * (0.5 - 2 / 16.0);
        if (random.nextInt(2) == 0) level.addParticle(ParticleTypes.DRIPPING_WATER, x, pos.getY() + 6 / 16.0, z, 0, 0, 0);
        if (random.nextInt(24) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    SoundSource.BLOCKS, 0.4F, 0.9F, false);
        }
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
