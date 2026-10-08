package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

/** The Crushing Tub (stomping) and the Fruit Press (cranking). */
public final class WineryTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private static void insert(GameTestHelper helper, ItemStack stack) {
        Handlers.Items items = Handlers.items(helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class), null);
        helper.assertTrue(items.insertItem(0, stack, false).isEmpty(), "a hopper can put " + stack.getHoverName().getString() + " in");
    }

    /** Someone jumping in: the landing is what counts. */
    private static void jumpIn(GameTestHelper helper, Player player) {
        BlockState state = helper.getBlockState(POS);
        state.getBlock().fallOn(helper.getLevel(), state, helper.absolutePos(POS), player, 1.25F);
    }

    @GameTest(template = EMPTY)
    public static void stompingRedGrapesMakesMust(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.CRUSHING_TUB.get());
        CrushingTubBlockEntity tub = (CrushingTubBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        insert(helper, new ItemStack(Crops.RED_GRAPE.produce(), 2));
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        jumpIn(helper, player);
        helper.assertTrue(tub.tank().isEmpty() && tub.fruit().getCount() == 2, "one stomp isn't enough for a bunch");
        jumpIn(helper, player);
        helper.assertTrue(tub.fruit().getCount() == 1, "the second stomp crushes it");
        helper.assertTrue(net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(tub.tank().getFluid(), new FluidStack(ModFluids.RED_GRAPE_MUST.get(), 1))
                && tub.tank().getFluidAmount() == 125, "red grapes give red must, 125 mB each");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void tubKeepsOneLiquidAndOnlyDrains(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.CRUSHING_TUB.get());
        CrushingTubBlockEntity tub = (CrushingTubBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        tub.tank().fill(new FluidStack(ModFluids.RED_GRAPE_MUST.get(), 500), StationTank.Action.EXECUTE);
        insert(helper, new ItemStack(Crops.WHITE_GRAPE.produce(), 1));
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        jumpIn(helper, player);
        jumpIn(helper, player);
        helper.assertTrue(tub.fruit().getCount() == 1 && tub.tank().getFluidAmount() == 500, "white grapes aren't crushed into red must");
        Handlers.Fluids pipes = Handlers.fluids(tub, null);
        helper.assertTrue(pipes.fill(new FluidStack(ModFluids.APPLE_JUICE.get(), 1000), StationTank.Action.EXECUTE) == 0,
                "nothing can be poured in");
        helper.assertTrue(pipes.drain(250, StationTank.Action.EXECUTE).getAmount() == 250, "pipes and bottles take it out");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pressingApplesGivesJuiceAndPomace(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.FRUIT_PRESS.get());
        FruitPressBlockEntity press = (FruitPressBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        insert(helper, new ItemStack(Items.APPLE, 5));
        helper.assertTrue(press.crank(), "first turn of the screw");
        helper.assertFalse(press.crank(), "the next turn waits for the handle to come round");
        for (int i = 1; i < 4; i++) helper.runAfterDelay((long) FruitPressBlockEntity.CRANK_TICKS * i, press::crank);
        helper.runAfterDelay(FruitPressBlockEntity.CRANK_TICKS * 4L, () -> {
            helper.assertTrue(net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(press.tank().getFluid(), new FluidStack(ModFluids.APPLE_JUICE.get(), 1))
                    && press.tank().getFluidAmount() == 500, "4 turns press 4 apples into 500 mB of apple juice");
            helper.assertTrue(press.input().getCount() == 1, "one apple left in the cage");
            helper.assertTrue(press.byproduct().is(ModItems.FRUIT_POMACE.get()), "pomace left behind");
            Handlers.Items below = Handlers.items(press, net.minecraft.core.Direction.DOWN);
            helper.assertTrue(below.extractItem(0, 64, false).is(ModItems.FRUIT_POMACE.get()), "a hopper underneath takes the pomace");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void pressNeedsAFullBatch(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.FRUIT_PRESS.get());
        FruitPressBlockEntity press = (FruitPressBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        insert(helper, new ItemStack(Items.APPLE, 3));
        for (int i = 0; i < 5; i++) helper.runAfterDelay((long) FruitPressBlockEntity.CRANK_TICKS * i, press::crank);
        helper.runAfterDelay(FruitPressBlockEntity.CRANK_TICKS * 5L, () -> {
            helper.assertTrue(press.tank().isEmpty() && press.input().getCount() == 3, "three apples aren't a batch: nothing pressed");
            helper.succeed();
        });
    }

    private WineryTests() {}
}
