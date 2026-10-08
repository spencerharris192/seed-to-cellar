package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BonemealSource;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A trellis with a vine growing on it (GDD growth style D: trellis vine, perennial): hops, and the
 * grapes in {@link Crops}.
 * <ul>
 *   <li>Ages 0-2 grow, 3 is leafy, 4 flowers, 5 is ripe.</li>
 *   <li>Right-click a ripe vine to pick its harvest; it drops back to leafy (3) and regrows.</li>
 *   <li>Vines with prunings (grapes): shears on a leafy, flowering or ripe vine cut 1-2 leaves off and
 *       set it back to young (2), so leaves cost some growth.</li>
 *   <li>Once leafy, the vine climbs into an empty trellis above, up to {@link #MAX_HEIGHT} blocks.</li>
 *   <li>The ROOT segment sits on soil and drops what you planted when broken.</li>
 *   <li>If the soil is replaced (or the vine below withers) it withers, leaving a bare trellis; with
 *       nothing left underneath, the column collapses like a bare one, dropping everything.</li>
 *   <li>Grows at the climate-adjusted speed every crop uses ({@link ClimateRules}).</li>
 * </ul>
 */
public class TrellisVineBlock extends TrellisBlock implements BonemealableBlock, ClimateCrop {
    public static final int MAX_AGE = 5;
    public static final int YOUNG = 2;
    public static final int LEAFY = 3;
    public static final int MAX_HEIGHT = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    public static final BooleanProperty ROOT = BooleanProperty.create("root");

    /** What the player planted, what a ripe vine gives, and (grapes) what shears cut off it. */
    public record Harvest(Supplier<? extends Item> plant, Supplier<? extends Item> produce, int min, int max,
                          @Nullable Supplier<? extends Item> prunings) {}

    private final Harvest harvest;
    private final Climate climate;

    public TrellisVineBlock(Properties properties, Harvest harvest, Climate climate) {
        super(properties);
        this.harvest = harvest;
        this.climate = climate;
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(AGE, 0).setValue(ROOT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE, ROOT);
    }

    public Harvest harvest() {
        return harvest;
    }

    @Override
    public Climate climate() {
        return climate;
    }

    public static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    /** The vine planted on the bottom trellis of a stack. */
    public BlockState planted(Direction.Axis axis) {
        return defaultBlockState().setValue(AXIS, axis).setValue(AGE, 0).setValue(ROOT, true);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return state.getValue(ROOT) ? isSoil(below) : below.is(this);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        // Unsupported: stay a vine so the collapse (scheduled by TrellisBlock) drops what's planted and ripe too.
        if (direction == Direction.DOWN && isSupported(level, pos) && !canSurvive(state, level, pos)) {
            return ModBlocks.TRELLIS.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos.above(), 0) < 9) return;
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts; i++) {
            BlockState now = level.getBlockState(pos);
            if (!now.is(this)) return;
            int age = now.getValue(AGE);
            if (age < MAX_AGE && CommonHooks.canCropGrow(level, pos, now, random.nextInt(4) == 0)) {
                level.setBlock(pos, now.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
                CommonHooks.fireCropGrowPost(level, pos, now);
            }
        }
        BlockState now = level.getBlockState(pos);
        if (now.is(this) && now.getValue(AGE) >= LEAFY && random.nextInt(4) == 0) climb(level, pos, now);
    }

    /** Converts an empty trellis directly above (same facing) into a young vine segment. */
    private boolean climb(Level level, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.is(ModBlocks.TRELLIS.get()) || aboveState.getValue(AXIS) != state.getValue(AXIS)) return false;
        if (heightBelowAndIncluding(level, pos) >= MAX_HEIGHT) return false;
        level.setBlock(above, state.setValue(AGE, 0).setValue(ROOT, false), Block.UPDATE_ALL);
        return true;
    }

    private int heightBelowAndIncluding(BlockGetter level, BlockPos pos) {
        int height = 0;
        BlockPos cursor = pos;
        while (level.getBlockState(cursor).is(this)) {
            height++;
            cursor = cursor.below();
        }
        return height;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        // Holding a trellis means "stack one on top" (TrellisItem), even on a ripe vine.
        if (held.is(ModItems.TRELLIS.get())) return InteractionResult.PASS;
        int age = state.getValue(AGE);
        if (harvest.prunings() != null && held.is(Tags.Items.TOOLS_SHEAR) && age >= LEAFY) {
            if (!level.isClientSide()) {
                popResource(level, pos, new ItemStack(harvest.prunings().get(), 1 + level.getRandom().nextInt(2)));
                level.playSound(null, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, state.setValue(AGE, YOUNG), Block.UPDATE_CLIENTS);
                held.hurtAndBreak(1, player, hand);
            }
            return InteractionResult.SUCCESS;
        }
        if (age != MAX_AGE) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            int count = harvest.min() + level.getRandom().nextInt(harvest.max() - harvest.min() + 1);
            popResource(level, pos, new ItemStack(harvest.produce().get(), count));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            level.setBlock(pos, state.setValue(AGE, LEAFY), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(harvest.plant().get());
    }

    // --- bone meal ---

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        int age = Math.min(MAX_AGE, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
        if (age >= LEAFY) climb(level, pos, state);
    }
}
