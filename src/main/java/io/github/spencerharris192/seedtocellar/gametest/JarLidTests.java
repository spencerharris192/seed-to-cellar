package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** The jar's lid opens and closes with an empty hand, whatever else the player carries (playtest, 26.3). */
public final class JarLidTests {
    private static final String EMPTY = "empty";
    private static final BlockPos JAR = new BlockPos(1, 1, 1);

    /** A right-click the way the server takes one from a player's game (events, item first, then the block). */
    private static void click(GameTestHelper helper, ServerPlayer player) {
        BlockPos abs = helper.absolutePos(JAR);
        player.gameMode.useItemOn(player, helper.getLevel(), player.getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
    }

    @GameTest(template = EMPTY)
    public static void theLidWorksWithAWaterBucketInTheInventory(GameTestHelper helper) {
        for (GameType mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
            helper.setBlock(JAR, ModBlocks.PRESERVING_JAR.get());
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(mode);
            player.getInventory().setItem(3, new ItemStack(Items.WATER_BUCKET));
            player.getInventory().setSelectedSlot(0);
            click(helper, player);
            helper.assertBlockProperty(JAR, PreservingJarBlock.OPEN, false);
            click(helper, player);
            helper.assertBlockProperty(JAR, PreservingJarBlock.OPEN, true);
            // pour the bucket in, then click with an empty hand again
            player.getInventory().setSelectedSlot(3);
            click(helper, player);
            player.getInventory().setSelectedSlot(0);
            click(helper, player);
            helper.assertBlockProperty(JAR, PreservingJarBlock.OPEN, false);
            click(helper, player);
            helper.assertBlockProperty(JAR, PreservingJarBlock.OPEN, true);
            player.discard();
        }
        helper.succeed();
    }

    private JarLidTests() {}
}
