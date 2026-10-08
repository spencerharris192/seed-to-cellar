package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.function.Supplier;

/**
 * A fruit tree's leaves (GDD growth style H). They decay like any leaves, and on a real tree they
 * cycle: plain, blossom, unripe fruit, ripe fruit.
 * <ul>
 *   <li>Each stage takes about 9 minutes on average (a 1-in-8 chance per random tick), at the
 *       climate-adjusted speed every crop uses ({@link ClimateRules}); bone meal and bees help.</li>
 *   <li>Right-click ripe leaves to pick one fruit; the leaves go back to plain.</li>
 *   <li>Ripe fruit with open air below falls on its own after a while (so a hopper under the tree
 *       collects it); the leaves go back to plain.</li>
 *   <li>Leaves a player placed are decoration: they keep their look but don't grow.</li>
 * </ul>
 */
public class FruitLeavesBlock extends TintedParticleLeavesBlock implements BonemealableBlock, ClimateCrop {
    public static final int PLAIN = 0, BLOSSOM = 1, UNRIPE = 2, RIPE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    /** 1 in this many random ticks moves a stage on. */
    private static final int GROWTH_CHANCE = 8;
    /** 1 in this many random ticks drops ripe fruit that has air below it. */
    private static final int FALL_CHANCE = 4;

    private final Supplier<? extends Item> fruit;
    private final Climate climate;

    public FruitLeavesBlock(Properties properties, Supplier<? extends Item> fruit, Climate climate) {
        super(0.01F, properties);   // falling leaf particles, like oak leaves
        this.fruit = fruit;
        this.climate = climate;
        registerDefaultState(defaultBlockState().setValue(AGE, PLAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    public Item fruit() {
        return fruit.get();
    }

    @Override
    public Climate climate() {
        return climate;
    }

    /** On a tree (not placed by a player): only these grow fruit. */
    public static boolean growing(BlockState state) {
        return !state.getValue(PERSISTENT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return growing(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            super.randomTick(state, level, pos, random);   // falls apart, dropping its loot
            return;
        }
        if (!growing(state)) return;
        if (state.getValue(AGE) == RIPE) {
            if (random.nextInt(FALL_CHANCE) == 0) fall(level, pos, state);
            return;
        }
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts; i++) {
            BlockState now = level.getBlockState(pos);
            if (!now.is(this) || now.getValue(AGE) == RIPE) return;
            if (CommonHooks.canCropGrow(level, pos, now, random.nextInt(GROWTH_CHANCE) == 0)) {
                level.setBlock(pos, now.setValue(AGE, now.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
                CommonHooks.fireCropGrowPost(level, pos, now);
            }
        }
    }

    /** Ripe fruit with air under it drops out of the bottom of the leaves. True if it fell. */
    public boolean fall(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) != RIPE || !level.getBlockState(pos.below()).isAir()) return false;
        popResourceFromFace(level, pos, Direction.DOWN, new ItemStack(fruit()));
        level.setBlock(pos, state.setValue(AGE, PLAIN), Block.UPDATE_CLIENTS);
        return true;
    }

    /** Picks the fruit off ripe leaves, toward `side`. True if there was fruit. */
    public boolean pick(Level level, BlockPos pos, BlockState state, Direction side) {
        if (state.getValue(AGE) != RIPE) return false;
        popResourceFromFace(level, pos, side, new ItemStack(fruit()));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.setBlock(pos, state.setValue(AGE, PLAIN), Block.UPDATE_CLIENTS);
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) != RIPE) return InteractionResult.PASS;
        if (!level.isClientSide()) pick(level, pos, state, hit.getDirection());
        return InteractionResult.SUCCESS;
    }

    // --- bone meal (bees pollinating the blossom count too) ---

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return growing(state) && state.getValue(AGE) < RIPE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        level.setBlock(pos, state.setValue(AGE, Math.min(RIPE, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }
}
