package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Drying Rack (GDD section 7), run 100x faster via the processing-time multiplier. */
public final class DryingTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest(template = EMPTY, timeoutTicks = 600)
    public static void rackDriesHopsAndMeatThenHoppersTakeThemOut(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(POS, ModBlocks.DRYING_RACK.get());
        DryingRackBlockEntity rack = (DryingRackBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);

        // Something that doesn't dry isn't hung.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
        helper.useBlock(POS, player);
        helper.assertTrue(rack.contents().isEmpty(), "stone doesn't hang on a drying rack");

        // Hop cones one at a time; sneaking with beef fills the remaining spots.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HOP_CONES.get(), 5));
        helper.useBlock(POS, player);
        helper.assertTrue(player.getMainHandItem().getCount() == 4 && rack.contents().size() == 1, "one hop cone per click");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 5));
        player.setShiftKeyDown(true);
        helper.useBlock(POS, player);
        helper.assertTrue(rack.contents().size() == DryingRackBlockEntity.SLOTS, "sneaking fills every free spot");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "three beef hung, two left");

        Handlers.Items items = Handlers.items(rack, Direction.DOWN);
        helper.assertTrue(items.extractItem(0, 1, false).isEmpty(), "nothing comes out before it's dry");

        helper.succeedWhen(() -> {
            for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) helper.assertTrue(rack.isDone(slot), "still drying");
            helper.assertTrue(rack.stack(0).is(ModItems.DRIED_HOPS.get()), "hop cones dry into dried hops");
            helper.assertTrue(rack.stack(1).is(ModItems.JERKY.get()), "beef dries into jerky");
            helper.assertTrue(items.extractItem(0, 1, false).is(ModItems.DRIED_HOPS.get()), "a hopper takes dried hops out");
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.useBlock(POS, player);
            helper.assertTrue(player.getInventory().countItem(ModItems.JERKY.get()) == 1, "an empty hand takes a dried item");
        });
    }

    @GameTest(template = EMPTY)
    public static void sneakingTakesBackAnUndriedItem(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.DRYING_RACK.get());
        DryingRackBlockEntity rack = (DryingRackBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HOP_CONES.get()));
        helper.useBlock(POS, player);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "hung");

        helper.useBlock(POS, player);   // empty hand, not sneaking: nothing dried yet, so nothing comes off
        helper.assertTrue(rack.contents().size() == 1, "undried items stay unless you sneak");
        player.setShiftKeyDown(true);
        helper.useBlock(POS, player);
        helper.assertTrue(rack.contents().isEmpty() && player.getInventory().countItem(ModItems.HOP_CONES.get()) == 1,
                "sneaking takes the hop cone back");
        helper.succeed();
    }

    private DryingTests() {}
}
