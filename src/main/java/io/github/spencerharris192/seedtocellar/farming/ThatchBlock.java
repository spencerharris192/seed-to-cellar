package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Thatch (GDD section 17): straw roofing. Burns like a hay bale, and the full block softens a
 * fall like one. Stairs and slab for roofs share the burning.
 */
public class ThatchBlock extends Block {
    // Same as vanilla's hay bale: catches and spreads fire readily.
    static final int FLAMMABILITY = 20;
    static final int FIRE_SPREAD = 60;

    public ThatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        entity.causeFallDamage(distance, 0.2F, level.damageSources().fall());
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return FLAMMABILITY;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return FIRE_SPREAD;
    }

    public static class Stairs extends StairBlock {
        public Stairs(Supplier<BlockState> base, Properties properties) {
            super(base, properties);
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return FLAMMABILITY;
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return FIRE_SPREAD;
        }
    }

    public static class Slab extends SlabBlock {
        public Slab(Properties properties) {
            super(properties);
        }

        @Override
        public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return FLAMMABILITY;
        }

        @Override
        public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return FIRE_SPREAD;
        }
    }
}
