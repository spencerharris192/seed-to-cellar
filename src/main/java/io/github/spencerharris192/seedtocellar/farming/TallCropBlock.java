package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealSource;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.function.Supplier;

/**
 * A two-block crop (GDD growth style B: corn), in the pattern of vanilla's pitcher plant.
 * <ul>
 *   <li>Ages 0-3 fill the lower block; from age {@link #UPPER_FROM} the plant also fills the
 *       block above. The top half copies the age, so both halves show the right stage.</li>
 *   <li>The lower half does the growing (random ticks, bone meal, Forge crop events). It stops
 *       at age 3 if the space above isn't free.</li>
 *   <li>Right-click either half when ripe to harvest and replant. Breaking either half breaks the
 *       whole plant; drops always come from the lower half's loot, so nothing drops twice.</li>
 * </ul>
 * It's still a CropBlock, so farmer villagers and other mods' harvesters treat it as a crop.
 */
public class TallCropBlock extends ModCropBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** First age at which the plant reaches into the block above. */
    public static final int UPPER_FROM = 4;

    private static final VoxelShape[] LOWER_SHAPES = {
            box(0, 0, 0, 16, 3, 16), box(0, 0, 0, 16, 6, 16), box(0, 0, 0, 16, 10, 16), box(0, 0, 0, 16, 14, 16),
            box(0, 0, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 16)};
    private static final VoxelShape[] UPPER_SHAPES = {
            box(0, 0, 0, 16, 2, 16), box(0, 0, 0, 16, 2, 16), box(0, 0, 0, 16, 2, 16), box(0, 0, 0, 16, 2, 16),
            box(0, 0, 0, 16, 5, 16), box(0, 0, 0, 16, 9, 16), box(0, 0, 0, 16, 13, 16), box(0, 0, 0, 16, 16, 16)};

    public TallCropBlock(Properties properties, Supplier<? extends Item> seeds, Climate climate) {
        super(properties, seeds, climate);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    public static BlockPos lowerPos(BlockPos pos, BlockState state) {
        return isUpper(state) ? pos.below() : pos;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (isUpper(state) ? UPPER_SHAPES : LOWER_SHAPES)[getAge(state)];
    }

    // --- growing -----------------------------------------------------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !isUpper(state) && super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isUpper(state)) return;
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts && level.getBlockState(pos).is(this); i++) growTick(level.getBlockState(pos), level, pos, random);
    }

    private void growTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1) || level.getRawBrightness(pos, 0) < 9) return;
        int age = getAge(state);
        if (age >= getMaxAge()) return;
        float speed = getGrowthSpeed(state, level, pos);   // like wheat: wetter, fuller fields grow faster
        if (CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (25.0F / speed) + 1) == 0)
                && setAge(level, pos, age + 1)) {
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    /** Whether the block above the lower half is free (or already our top half). */
    private boolean roomAbove(LevelReader level, BlockPos lower) {
        BlockState above = level.getBlockState(lower.above());
        return above.isAir() || above.is(this);
    }

    /** Sets the whole plant (given its lower half) to `age`, adding or updating the top. False if there's no room. */
    protected boolean setAge(Level level, BlockPos lower, int age) {
        if (age >= UPPER_FROM && !roomAbove(level, lower)) return false;
        level.setBlock(lower, getStateForAge(age), Block.UPDATE_CLIENTS);
        if (age >= UPPER_FROM) {
            level.setBlock(lower.above(), getStateForAge(age).setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos lower = lowerPos(pos, state);
        BlockState bottom = level.getBlockState(lower);
        if (!bottom.is(this) || isMaxAge(bottom)) return false;
        return getAge(bottom) + 1 < UPPER_FROM || roomAbove(level, lower);
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        BlockPos lower = lowerPos(pos, state);
        BlockState bottom = level.getBlockState(lower);
        if (!bottom.is(this)) return;
        int age = Math.min(getMaxAge(), getAge(bottom) + getBonemealAgeIncrease(level));
        if (!setAge(level, lower, age)) setAge(level, lower, Math.min(age, UPPER_FROM - 1));
    }

    // --- staying whole -----------------------------------------------------------------------

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!isUpper(state)) return super.canSurvive(state, level, pos);
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && !isUpper(below) && getAge(below) >= UPPER_FROM;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        // The top was removed some other way (a player takes the whole plant): cut back to one block.
        if (!isUpper(state) && direction == Direction.UP && getAge(state) >= UPPER_FROM && !neighbor.is(this)) {
            return getStateForAge(UPPER_FROM - 1);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && isUpper(state)) {
            BlockPos lower = pos.below();
            BlockState bottom = level.getBlockState(lower);
            if (bottom.is(this) && !isUpper(bottom)) {
                // Breaking the top takes the whole plant, dropping what the bottom half would.
                if (!player.isCreative()) dropResources(bottom, level, lower, null, player, player.getMainHandItem());
                level.setBlock(lower, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, lower, Block.getId(bottom));   // break particles and sound
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // --- harvesting --------------------------------------------------------------------------

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos lower = lowerPos(pos, state);
        BlockState bottom = level.getBlockState(lower);
        if (!bottom.is(this) || !isMaxAge(bottom) || !ModConfigs.COMMON.rightClickHarvest.get()) return InteractionResult.PASS;
        // Replanting drops the plant back to age 0, and the now-unsupported top half disappears.
        if (level instanceof ServerLevel server) harvest(server, lower, bottom, player, getStateForAge(0));
        return InteractionResult.SUCCESS;
    }
}
