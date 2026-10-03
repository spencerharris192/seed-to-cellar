package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;

import java.util.function.Supplier;

/**
 * A paddy crop (GDD growth style C: rice), planted in water one block deep over dirt, mud or
 * farmland.
 * <ul>
 *   <li>The block holds its water (it's waterlogged), so breaking it leaves the water behind,
 *       and a bucket can't scoop the water out from under the rice.</li>
 *   <li>A paddy is always wet, so it grows at the pace of a well-watered wheat field.</li>
 *   <li>Its model reaches up out of the water: the grain heads show above the waterline.</li>
 * </ul>
 */
public class PaddyCropBlock extends ModCropBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /** One growth step per this many random ticks on average (a well-watered wheat field is about 3). */
    private static final int GROWTH_CHANCE = 3;

    public PaddyCropBlock(Properties properties, Supplier<? extends Item> seeds, Climate climate) {
        super(properties, seeds, climate);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    /** What a paddy can be dug in: dirt, mud, grass, farmland. */
    public static boolean isPaddySoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    /** Water one block deep: this block holds water and the block above doesn't. */
    public static boolean isShallowWater(LevelReader level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return isPaddySoil(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return fluid.getType() == Fluids.WATER ? defaultBlockState() : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Checked directly: Forge's usual crop check only accepts farmland under a crop.
        BlockPos below = pos.below();
        return state.getValue(WATERLOGGED) && isShallowWater(level, pos)
                && mayPlaceOn(level.getBlockState(below), level, below)
                && (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    /** The water belongs to the paddy: a bucket can't take it while rice grows there. */
    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts && level.getBlockState(pos).is(this); i++) growTick(level.getBlockState(pos), level, pos, random);
    }

    private void growTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1) || level.getRawBrightness(pos, 0) < 9 || isMaxAge(state)) return;
        if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(GROWTH_CHANCE) == 0)) {
            level.setBlock(pos, getStateForAge(getAge(state) + 1), Block.UPDATE_CLIENTS);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }
}
