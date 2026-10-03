package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Vanilla (GDD growth style J): an orchid vine that climbs the side of a jungle log, like cocoa. FACING points at the log.
 * Four looks: a shoot, a leafy vine, flowering, and hung with green pods. Right-click the podded vine to pick 1-2 vanilla
 * pods; it goes back to leafy and flowers again. Planted with a pod on a jungle log's side.
 */
public class VanillaVineBlock extends HorizontalDirectionalBlock implements BonemealableBlock, ClimateCrop {
    public static final int MAX_AGE = 3;
    public static final int LEAFY = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    /** One growth try in this many random ticks (cocoa uses 5). */
    public static final int GROWTH_CHANCE = 4;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        // a thin layer against the log (drawn facing north, the log beyond z = 0)
        SHAPES.put(Direction.NORTH, box(2, 0, 0, 14, 16, 4));
        SHAPES.put(Direction.SOUTH, box(2, 0, 12, 14, 16, 16));
        SHAPES.put(Direction.WEST, box(0, 0, 2, 4, 16, 14));
        SHAPES.put(Direction.EAST, box(12, 0, 2, 16, 16, 14));
    }

    private final Climate climate;

    public VanillaVineBlock(Properties properties, Climate climate) {
        super(properties);
        this.climate = climate;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AGE, 0));
    }

    @Override
    public Climate climate() {
        return climate;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    // --- on its log ----------------------------------------------------------------------------

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING))).is(BlockTags.JUNGLE_LOGS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (clicked.getAxis().isHorizontal()) {   // the side of the log that was clicked: the vine faces into it
            BlockState state = defaultBlockState().setValue(FACING, clicked.getOpposite());
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
        }
        for (Direction dir : context.getNearestLookingDirections()) {
            if (!dir.getAxis().isHorizontal()) continue;
            BlockState state = defaultBlockState().setValue(FACING, dir);
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING) && !state.canSurvive(level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    // --- growing ---------------------------------------------------------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts; i++) {
            BlockState now = level.getBlockState(pos);
            if (!now.is(this) || now.getValue(AGE) >= MAX_AGE) return;
            if (ForgeHooks.onCropsGrowPre(level, pos, now, random.nextInt(GROWTH_CHANCE) == 0)) {
                level.setBlock(pos, now.setValue(AGE, now.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
                ForgeHooks.onCropsGrowPost(level, pos, now);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
    }

    // --- picking ---------------------------------------------------------------------------------

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) < MAX_AGE || !ModConfigs.COMMON.rightClickHarvest.get()) return InteractionResult.PASS;
        if (!level.isClientSide) pick(level, pos, state);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Picks the pods (1-2) and sets the vine back to leafy, to flower again. */
    public static void pick(Level level, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(ModItems.VANILLA_POD.get(), 1 + level.random.nextInt(2)));
        level.setBlock(pos, state.setValue(AGE, LEAFY), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
    }
}
