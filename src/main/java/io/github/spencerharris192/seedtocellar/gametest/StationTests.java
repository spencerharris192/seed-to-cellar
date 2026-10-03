package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

/** Malt house stations, run 100x faster via the processing-time multiplier. */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private static void fast() {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void maltingTubTurnsBarleyIntoGreenMalt(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.MALTING_TUB.get());
        MaltingTubBlockEntity tub = (MaltingTubBlockEntity) helper.getBlockEntity(POS);
        IItemHandler items = tub.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
        IFluidHandler fluids = tub.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);

        items.insertItem(0, new ItemStack(Crops.BARLEY.produce(), 4), false);
        helper.assertBlockProperty(POS, MaltingTubBlock.CONTENTS, MaltingTubBlock.Contents.GRAIN);
        fluids.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(tub.phase() == MaltingTubBlockEntity.Phase.STEEPING, "water + grain should start steeping");
        helper.assertTrue(items.insertItem(0, new ItemStack(Crops.BARLEY.produce()), true).getCount() == 1,
                "no more grain while steeping");

        helper.succeedWhen(() -> {
            ItemStack out = items.extractItem(1, 64, true);
            helper.assertTrue(out.is(ModItems.GREEN_BARLEY_MALT.get()) && out.getCount() == 4, "expected 4 green malt");
            helper.assertBlockProperty(POS, MaltingTubBlock.CONTENTS, MaltingTubBlock.Contents.DONE);
            helper.assertTrue(tub.water().isEmpty(), "steep water should have drained");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kilnRoastsWholeBatchAtChosenSetting(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.KILN.get());
        KilnBlockEntity kiln = (KilnBlockEntity) helper.getBlockEntity(POS);
        kiln.setRoast(RoastLevel.DARK);
        kiln.items().setStackInSlot(KilnBlockEntity.INPUT, new ItemStack(ModItems.GREEN_BARLEY_MALT.get(), 4));
        kiln.items().setStackInSlot(KilnBlockEntity.FUEL, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> {
            ItemStack out = kiln.items().getStackInSlot(KilnBlockEntity.OUTPUT);
            helper.assertTrue(out.is(ModItems.BLACK_MALT.get()) && out.getCount() == 4, "expected 4 black malt");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void millstoneGrindsOneItemPerCrank(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.MILLSTONE.get());
        MillstoneBlockEntity mill = (MillstoneBlockEntity) helper.getBlockEntity(POS);
        IItemHandler items = mill.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
        items.insertItem(0, new ItemStack(ModItems.PALE_MALT.get(), 3), false);

        helper.assertTrue(mill.crank(), "first crank should turn");
        helper.assertFalse(mill.crank(), "a second crank in the same tick should wait for the first");
        helper.runAfterDelay(MillstoneBlockEntity.CRANK_TICKS, mill::crank);
        helper.runAfterDelay(MillstoneBlockEntity.CRANK_TICKS * 2L, () -> {
            helper.assertTrue(mill.output().is(ModItems.PALE_GRIST.get()) && mill.output().getCount() == 2, "two cranks, two grist");
            helper.assertTrue(mill.input().getCount() == 1, "one malt left");
            helper.succeed();
        });
    }

    private StationTests() {}
}
