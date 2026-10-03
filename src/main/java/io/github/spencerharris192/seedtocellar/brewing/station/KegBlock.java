package io.github.spencerharris192.seedtocellar.brewing.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A small upright keg with a built-in tap (GDD section 7). Holds 4 buckets, conditions beer
 * (+1 star after a day) but doesn't age it. Keeps its contents when broken: carry beer anywhere.
 */
public class KegBlock extends AbstractCaskBlock {
    public static final int CAPACITY = 4000;
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 13, 13);

    public KegBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }

    @Override
    public boolean ages() {
        return false;
    }

    @Override
    public boolean isTapped(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
