package io.github.spencerharris192.seedtocellar.world;

import com.mojang.serialization.MapCodec;
import io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Wild vanilla on jungle trees: from a spot on the ground, looks around for jungle log trunks and climbs a few onto their
 * sides (the vines face their log), at every stage from shoot to podded.
 */
public record WildVanillaFeature() implements Feature {
    public static final MapCodec<WildVanillaFeature> CODEC = MapCodec.unit(WildVanillaFeature::new);

    @Override
    public MapCodec<WildVanillaFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState vine = ModBlocks.VANILLA.get().defaultBlockState();
        int placed = 0;
        for (int tries = 0; tries < 48 && placed < 4; tries++) {
            BlockPos pos = origin.offset(random.nextInt(7) - 3, random.nextInt(8) + 1, random.nextInt(7) - 3);
            if (!level.isEmptyBlock(pos)) continue;
            Direction toward = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            if (!level.getBlockState(pos.relative(toward)).is(BlockTags.JUNGLE_LOGS)) continue;
            BlockState state = vine.setValue(VanillaVineBlock.FACING, toward).setValue(VanillaVineBlock.AGE, random.nextInt(4));
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            placed++;
        }
        return placed > 0;
    }
}
