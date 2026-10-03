package io.github.spencerharris192.seedtocellar.gametest;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.Component;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.VesselFluidHandler;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** The orchard kitchen: bottled olive oil, sorghum syrup, lemonade, and wine in the pot. */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OrchardKitchenTests {
    private static final String EMPTY = "empty";
    private static final BlockPos KETTLE = new BlockPos(1, 2, 1);
    private static final IFluidHandler.FluidAction EXECUTE = IFluidHandler.FluidAction.EXECUTE;

    private static BrewKettleBlockEntity heatedKettle(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(KETTLE.below(), Blocks.CAMPFIRE);
        helper.setBlock(KETTLE, ModBlocks.BREW_KETTLE.get());
        return (BrewKettleBlockEntity) helper.getBlockEntity(KETTLE);
    }

    @GameTest(template = EMPTY)
    public static void glassBottlesFillWithOliveOilAndPourItBack(GameTestHelper helper) {
        IFluidHandlerItem glass = new ItemStack(Items.GLASS_BOTTLE).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .orElseThrow(() -> new IllegalStateException("a glass bottle should take olive oil"));
        helper.assertTrue(glass.fill(new FluidStack(ModFluids.OLIVE_OIL.get(), 1000), EXECUTE) == 250, "one bottle is 250 mB");
        helper.assertTrue(glass.getContainer().is(ModItems.OLIVE_OIL.get()), "a bottle of olive oil");
        VesselFluidHandler bottle = new VesselFluidHandler(glass.getContainer());
        FluidStack back = bottle.drain(250, EXECUTE);
        helper.assertTrue(back.getFluid() == ModFluids.OLIVE_OIL.get() && back.getAmount() == 250, "and pours back out");
        helper.assertTrue(bottle.getContainer().is(Items.GLASS_BOTTLE), "leaving the glass bottle");
        VesselFluidHandler mug = new VesselFluidHandler(new ItemStack(ModItems.MUG.get()));
        helper.assertTrue(mug.fill(new FluidStack(ModFluids.OLIVE_OIL.get(), 250), EXECUTE) == 0, "oil doesn't go in a mug");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kettleBoilsSorghumJuiceIntoSyrup(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(ModFluids.SORGHUM_JUICE.get(), 500), EXECUTE);
        kettle.items().setStackInSlot(BrewKettleBlockEntity.CONTAINER, new ItemStack(Items.GLASS_BOTTLE, 4));
        helper.succeedWhen(() -> {
            ItemStack out = kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT);
            helper.assertTrue(out.is(ModItems.SORGHUM_SYRUP.get()) && out.getCount() == 2, "250 mB of juice per bottle of syrup");
            helper.assertTrue(kettle.tank().isEmpty(), "the juice is used up");
        });
    }

    @GameTest(template = EMPTY)
    public static void anIdleKettleDoesNotAskForSorghumJuice(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 1000), EXECUTE);
        helper.assertTrue(kettle.matchingRecipe().isEmpty(), "nothing in the slots, water in the tank: no recipe");
        helper.assertTrue(kettle.missingForCooking() == null, "and no 'needs sorghum juice' hint");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kettleSweetensLemonJuiceIntoLemonade(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(ModFluids.LEMON_JUICE.get(), 1000), EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(ModItems.SORGHUM_SYRUP.get(), 2));   // syrup works like sugar
        helper.succeedWhen(() -> {
            helper.assertTrue(kettle.tank().getFluid().getFluid() == ModFluids.LEMONADE.get() && kettle.tank().getFluidAmount() == 1000,
                    "a bucket of lemonade");
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).is(Items.GLASS_BOTTLE)
                    && kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).getCount() == 2, "the syrup bottles come back");
            IFluidHandlerItem glass = new ItemStack(Items.GLASS_BOTTLE).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(
                    IllegalStateException::new);
            glass.fill(kettle.tank().getFluid(), EXECUTE);
            helper.assertTrue(glass.getContainer().is(Drinks.LEMONADE.item().get()), "it bottles as a drink");
        });
    }

    @GameTest(template = EMPTY)
    public static void theKettleSaysWhatLemonadeStillNeeds(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(ModFluids.LEMON_JUICE.get(), 4000), EXECUTE);
        Component hint = kettle.missingForMixing();
        helper.assertTrue(hint != null && hint.getContents() instanceof TranslatableContents t && t.getKey().endsWith("stir_in"),
                "lemon juice alone: what to stir in");
        kettle.items().setStackInSlot(0, new ItemStack(ModItems.SORGHUM_SYRUP.get(), 2));
        hint = kettle.missingForMixing();
        helper.assertTrue(hint != null && hint.getContents() instanceof TranslatableContents t && t.getKey().endsWith("needs_more")
                && Integer.valueOf(6).equals(t.getArgs()[0]), "4 buckets need 8 sweeteners: 6 more");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void risottoCooksWithWhiteWine(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(ModFluids.WHITE_WINE.get(), 250), EXECUTE);
        helper.assertTrue(kettle.tank().getFluidAmount() == 250, "the kettle takes white wine");
        kettle.items().setStackInSlot(0, new ItemStack(Crops.RICE.produce()));
        kettle.items().setStackInSlot(1, new ItemStack(Crops.ONION.produce()));
        kettle.items().setStackInSlot(2, new ItemStack(Items.BROWN_MUSHROOM));
        kettle.items().setStackInSlot(BrewKettleBlockEntity.CONTAINER, new ItemStack(Items.BOWL));
        helper.succeedWhen(() -> helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).is(ModItems.RISOTTO.get()),
                "risotto"));
    }

    private OrchardKitchenTests() {}
}
