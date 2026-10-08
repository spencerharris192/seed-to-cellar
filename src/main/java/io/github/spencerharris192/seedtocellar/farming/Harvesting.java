package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Harvest-and-replant of one ripe plant, used by the Sickle. Covers every crop (vanilla ones
 * too, corn from either half, rice keeping its water), our bushes and herbs, sweet berries and
 * trellis vines (hops, grapes), and ripe fruit on fruit tree leaves. Unripe plants are left alone. Works regardless of the right-click harvest setting.
 */
public final class Harvesting {
    /** Something a sickle works on (ripe or not): decides which layer a click aims at. */
    public static boolean isPlant(BlockState state) {
        return state.getBlock() instanceof VegetationBlock || state.getBlock() instanceof TrellisVineBlock
                || state.getBlock() instanceof FruitLeavesBlock;
    }

    /** Ripe and ready to harvest. Safe on both sides (only reads the block state). */
    public static boolean isRipe(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BushCropBlock) return state.getValue(BushCropBlock.AGE) == BushCropBlock.MAX_AGE;
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);   // corn: both halves carry the age
        if (block instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) == SweetBerryBushBlock.MAX_AGE;
        if (block instanceof TrellisVineBlock) return state.getValue(TrellisVineBlock.AGE) == TrellisVineBlock.MAX_AGE;
        if (block instanceof FruitLeavesBlock) return state.getValue(FruitLeavesBlock.AGE) == FruitLeavesBlock.RIPE;
        return false;
    }

    /** Harvests the plant at {@code pos} if it's ripe, leaving it replanted. True if it harvested. */
    public static boolean harvest(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!isRipe(state)) return false;
        Block block = state.getBlock();
        if (block instanceof TallCropBlock tall) {
            BlockPos lower = TallCropBlock.lowerPos(pos, state);
            ModCropBlock.harvest(level, lower, level.getBlockState(lower), player, tall.getStateForAge(0));
        } else if (block instanceof FruitLeavesBlock leaves) {
            leaves.pick(level, pos, state, Direction.DOWN);
        } else if (block instanceof BushCropBlock bush) {
            bush.pick(level, pos, state, player);
        } else if (block instanceof CropBlock crop) {
            ModCropBlock.harvest(level, pos, state, player, crop.getStateForAge(0));
        } else {
            // Sweet berries and trellis vines: picked just as a right-click picks them.
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            if (state.useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit) instanceof InteractionResult.TryEmptyHandInteraction) {
                state.useWithoutItem(level, player, hit);
            }
        }
        return true;
    }

    private Harvesting() {}
}
