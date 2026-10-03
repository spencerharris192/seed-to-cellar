package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import java.util.function.Supplier;

/**
 * A desert succulent (GDD growth style I): agave. It grows slowly on sand, red sand, coarse dirt or terracotta
 * (seedtocellar:succulent_soil) and needs no water. Four growing looks, then a ripe rosette that sends up a tall flower
 * spike, the sign it's ready: cut it down for its heart (piña) and 1-2 pups to plant again.
 */
public class SucculentCropBlock extends ModCropBlock {
    /** One growth try in this many random ticks: a slow desert plant (about 1.5 in-game days from pup to flower). */
    public static final int GROWTH_CHANCE = 3;
    private static final VoxelShape[] SHAPES = {
            box(5, 0, 5, 11, 4, 11), box(4, 0, 4, 12, 6, 12), box(3, 0, 3, 13, 9, 13), box(2, 0, 2, 14, 12, 14), box(2, 0, 2, 14, 16, 14)};

    public SucculentCropBlock(Properties properties, Supplier<? extends Item> seeds, Climate climate) {
        super(properties, seeds, climate);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModTags.Blocks.SUCCULENT_SOIL);
    }

    /** Any desert soil under it and some light; unlike field crops it never asks the soil to "sustain" a crop. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    /** Slow and steady, no water needed; at the climate's speed (see {@link ClimateRules}). */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts; i++) {
            BlockState now = level.getBlockState(pos);
            if (!now.is(this) || isMaxAge(now) || level.getRawBrightness(pos, 0) < 9) return;
            if (ForgeHooks.onCropsGrowPre(level, pos, now, random.nextInt(GROWTH_CHANCE) == 0)) {
                level.setBlock(pos, getStateForAge(getAge(now) + 1), 2);
                ForgeHooks.onCropsGrowPost(level, pos, now);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Math.min(SHAPES.length - 1, getAge(state) * SHAPES.length / (getMaxAge() + 1))];
    }
}
