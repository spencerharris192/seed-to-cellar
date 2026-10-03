package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wild version of a crop, found in the world. Breaking it gives seeds; shears pick the plant
 * itself. It grows on dirt, grass and farmland, and beach plants (sea beet, wild cabbage)
 * on sand too.
 */
public class WildPlantBlock extends BushBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 13, 14);
    private final boolean onSand;

    public WildPlantBlock(Properties properties) {
        this(properties, false);
    }

    public WildPlantBlock(Properties properties, boolean onSand) {
        super(properties);
        this.onSand = onSand;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || (onSand && state.is(BlockTags.SAND));
    }

    /** Checked directly (Forge's plant-type check would refuse sand for a plains plant). */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
