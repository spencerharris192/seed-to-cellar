package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A fruit tree sapling: grows like a vanilla sapling (light 9 or more, two steps), at the
 * climate-adjusted speed every crop uses, into the tree's `<tree>_tree` feature.
 */
public class FruitSaplingBlock extends SaplingBlock implements ClimateCrop {
    private final Climate climate;

    public FruitSaplingBlock(String name, Climate climate, Properties properties) {
        super(new TreeGrower(SeedToCellar.MOD_ID + "_" + name, WeightedList.of(treeKey(name)), WeightedList.of(), WeightedList.of(),
                treeKey(name)), properties);
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

    /** The feature a tree grows from: seedtocellar:&lt;name&gt;_tree. */
    public static ResourceKey<Feature> treeKey(String name) {
        return ResourceKey.create(Registries.FEATURE, SeedToCellar.id(name + "_tree"));
    }
}
