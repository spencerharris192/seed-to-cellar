package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The performance check (final phase): our only stations that tick every game tick (Brew Kettle, Kiln, Pot Still) cost
 * little even left heated and idle, as a village's Brewhouse leaves its kettle; and the drink lookup the shelves' renderers
 * use every frame is quick. Times go to the log as "[benchmark]", beside a vanilla furnace's for scale.
 */
public final class PerformanceTests {
    private static final String EMPTY = "empty";
    private static final int WARMUP = 2_000;
    private static final int RUNS = 20_000;
    /** Microseconds a tick our stations must stay under (a busy server has 50,000 for everything). */
    private static final double BUDGET_US = 20;

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void idleStationsTickCheaply(GameTestHelper helper) {
        var level = helper.getLevel();
        // A kettle over a fire with water and a carrot: nothing it can make, so it keeps looking.
        helper.setBlock(new BlockPos(0, 0, 0), Blocks.CAMPFIRE);
        helper.setBlock(new BlockPos(0, 1, 0), ModBlocks.BREW_KETTLE.get());
        BlockPos kettlePos = helper.absolutePos(new BlockPos(0, 1, 0));
        var kettle = (BrewKettleBlockEntity) level.getBlockEntity(kettlePos);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 2000), StationTank.Action.EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(Items.CARROT));
        // A still over a fire with water in the pot: nothing to distil.
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.CAMPFIRE);
        var lower = ModBlocks.POT_STILL.get().defaultBlockState();
        helper.setBlock(new BlockPos(2, 1, 0), lower);
        helper.setBlock(new BlockPos(2, 2, 0), lower.setValue(PotStillBlock.HALF, DoubleBlockHalf.UPPER));
        BlockPos stillPos = helper.absolutePos(new BlockPos(2, 1, 0));
        var still = (PotStillBlockEntity) level.getBlockEntity(stillPos);
        still.pot().fill(new FluidStack(Fluids.WATER, 1000), StationTank.Action.EXECUTE);
        // A kiln with fuel and something it can't roast.
        helper.setBlock(new BlockPos(0, 0, 2), ModBlocks.KILN.get());
        BlockPos kilnPos = helper.absolutePos(new BlockPos(0, 0, 2));
        var kiln = (KilnBlockEntity) level.getBlockEntity(kilnPos);
        kiln.items().setStackInSlot(KilnBlockEntity.INPUT, new ItemStack(Items.DIRT));
        kiln.items().setStackInSlot(KilnBlockEntity.FUEL, new ItemStack(Items.COAL));
        // Vanilla's furnace, the same way, for scale.
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.FURNACE);
        BlockPos furnacePos = helper.absolutePos(new BlockPos(2, 0, 2));
        var furnace = (AbstractFurnaceBlockEntity) level.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(Items.DIRT));
        furnace.setItem(1, new ItemStack(Items.COAL));

        double kettleUs = time(() -> BrewKettleBlockEntity.serverTick(level, kettlePos, level.getBlockState(kettlePos), kettle));
        double stillUs = time(() -> PotStillBlockEntity.serverTick(level, stillPos, level.getBlockState(stillPos), still));
        double kilnUs = time(() -> KilnBlockEntity.serverTick(level, kilnPos, level.getBlockState(kilnPos), kiln));
        double furnaceUs = time(() -> AbstractFurnaceBlockEntity.serverTick(level, furnacePos, level.getBlockState(furnacePos), furnace));
        var fluid = Drinks.MELON_JUICE.fluid().get();   // the last drink in the list
        double lookupUs = time(() -> Drinks.byFluid(fluid));
        SeedToCellar.LOGGER.info("[benchmark] idle heated, per tick: Brew Kettle {} us, Pot Still {} us, Kiln {} us (vanilla furnace {} us); "
                + "drink lookup {} us", fmt(kettleUs), fmt(stillUs), fmt(kilnUs), fmt(furnaceUs), fmt(lookupUs));
        helper.assertTrue(kettleUs < BUDGET_US && stillUs < BUDGET_US && kilnUs < BUDGET_US,
                "an idle station costs under " + BUDGET_US + " us a tick");
        helper.succeed();
    }

    /** Average microseconds per call, after a warm-up. */
    private static double time(Runnable task) {
        for (int i = 0; i < WARMUP; i++) task.run();
        long start = System.nanoTime();
        for (int i = 0; i < RUNS; i++) task.run();
        return (System.nanoTime() - start) / 1000.0 / RUNS;
    }

    private static String fmt(double us) {
        return String.format(java.util.Locale.ROOT, "%.2f", us);
    }

    private PerformanceTests() {}
}
