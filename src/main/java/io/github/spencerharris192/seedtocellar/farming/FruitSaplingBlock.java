package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * A fruit tree sapling: grows like a vanilla sapling (light 9 or more, two steps), at the
 * climate-adjusted speed every crop uses, into the tree's `<tree>_tree` feature.
 */
public class FruitSaplingBlock extends SaplingBlock implements ClimateCrop {
    private final Climate climate;

    public FruitSaplingBlock(ResourceKey<ConfiguredFeature<?, ?>> tree, Climate climate, Properties properties) {
        super(new Grower(tree), properties);
        this.climate = climate;
    }

    @Override
    public Climate climate() {
        return climate;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts && level.getBlockState(pos).is(this); i++) {
            super.randomTick(level.getBlockState(pos), level, pos, random);
        }
    }

    private static final class Grower extends AbstractTreeGrower {
        private final ResourceKey<ConfiguredFeature<?, ?>> tree;

        Grower(ResourceKey<ConfiguredFeature<?, ?>> tree) {
            this.tree = tree;
        }

        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            return tree;
        }
    }

    /** The configured feature a tree grows from: seedtocellar:&lt;name&gt;_tree. */
    public static ResourceKey<ConfiguredFeature<?, ?>> treeKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, io.github.spencerharris192.seedtocellar.SeedToCellar.id(name + "_tree"));
    }
}
