package io.github.spencerharris192.seedtocellar.gametest;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Right-clicking a block as a player's game does it: with the item in hand first, then, if the block allows, empty-handed. */
final class Interact {
    static InteractionResult use(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult withItem = state.useItemOn(player.getItemInHand(hand), level, player, hand, hit);
        if (withItem instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
            return state.useWithoutItem(level, player, hit);
        }
        return withItem;
    }

    private Interact() {}
}
