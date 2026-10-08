package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.VesselFluidHandler;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Winemaking: wine yeast from wild juice, vessels that only take their own drinks, honey water, vinegar. */
public final class WineTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private static void fast() {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void wildAppleJuiceBecomesCiderAndGivesWineYeast(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        FermentingVatBlockEntity vat = (FermentingVatBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        vat.tank().fill(new FluidStack(ModFluids.APPLE_JUICE.get(), 1000), StationTank.Action.EXECUTE);
        vat.setLid(false, null);
        helper.assertTrue(vat.isFermenting(), "juice ferments wild with no yeast");
        helper.succeedWhen(() -> {
            helper.assertTrue(vat.tank().getFluid().getFluid() == ModFluids.CIDER.get(), "apple juice -> cider");
            helper.assertFalse(BrewQuality.of(vat.tank().getFluid()).yeast(), "wild yeast earns no yeast star");
            helper.assertTrue(vat.items().getStackInSlot(FermentingVatBlockEntity.LEES).is(ModItems.WINE_YEAST.get()), "fruit lees become wine yeast");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void wineYeastEarnsTheStarOnRedMust(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        FermentingVatBlockEntity vat = (FermentingVatBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        vat.tank().fill(new FluidStack(ModFluids.RED_GRAPE_MUST.get(), 2000), StationTank.Action.EXECUTE);
        vat.items().setStackInSlot(FermentingVatBlockEntity.YEAST, new ItemStack(ModItems.WINE_YEAST.get()));
        vat.setLid(false, null);
        helper.succeedWhen(() -> {
            helper.assertTrue(vat.tank().getFluid().getFluid() == ModFluids.RED_WINE.get(), "red must -> red wine");
            helper.assertTrue(BrewQuality.of(vat.tank().getFluid()).yeast(), "wine yeast earns the yeast star");
            helper.assertTrue(vat.items().getStackInSlot(FermentingVatBlockEntity.LEES).getCount() == 2, "cultured yeast gives 2 lees");
        });
    }

    @GameTest(template = EMPTY)
    public static void vesselsOnlyHoldTheirOwnDrinks(GameTestHelper helper) {
        FluidStack juice = new FluidStack(ModFluids.APPLE_JUICE.get(), 250);
        FluidStack wine = new FluidStack(ModFluids.RED_WINE.get(), 250);
        Handlers.Held glass = Handlers.fluids(new ItemStack(Items.GLASS_BOTTLE));
        helper.assertTrue(glass.fill(wine, StationTank.Action.EXECUTE) == 0, "wine doesn't go in a glass bottle");
        helper.assertTrue(glass.fill(juice, StationTank.Action.EXECUTE) == 250, "juice does");
        helper.assertTrue(glass.getContainer().is(Drinks.APPLE_JUICE.item().get()), "a bottle of apple juice");
        helper.assertTrue(Handlers.fluidsOf(new ItemStack(Items.GLASS_BOTTLE)).map(h ->
                h.fill(new FluidStack(Fluids.WATER, 250), StationTank.Action.EXECUTE)).orElse(0) == 0, "water bottles stay vanilla's");

        Handlers.Held mug = new Handlers.Held(new ItemStack(ModItems.MUG.get()));
        helper.assertTrue(mug.fill(wine, StationTank.Action.EXECUTE) == 0, "wine doesn't go in a mug");
        Handlers.Held bottle = new Handlers.Held(new ItemStack(ModItems.WINE_BOTTLE.get()));
        helper.assertTrue(bottle.fill(wine, StationTank.Action.EXECUTE) == 250, "a wine bottle takes wine");
        ItemStack redWine = bottle.getContainer();
        helper.assertTrue(redWine.is(Drinks.RED_WINE.item().get()), "a bottle of red wine");

        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack left = redWine.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(left.is(ModItems.WINE_BOTTLE.get()), "drinking it gives the bottle back");
        helper.assertFalse(((DrinkItem) Drinks.APPLE_JUICE.item().get()).profile().alcoholic(), "juice has no alcohol");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kettleStirsHoneyIntoWater(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, Blocks.CAMPFIRE);
        BlockPos kettlePos = POS.above();
        helper.setBlock(kettlePos, ModBlocks.BREW_KETTLE.get());
        BrewKettleBlockEntity kettle = (BrewKettleBlockEntity) helper.getBlockEntity(kettlePos, net.minecraft.world.level.block.entity.BlockEntity.class);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 2000), StationTank.Action.EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(Items.HONEY_BOTTLE, 5));
        helper.succeedWhen(() -> {
            helper.assertTrue(kettle.tank().getFluid().getFluid() == ModFluids.HONEY_WATER.get()
                    && kettle.tank().getFluidAmount() == 2000, "2 buckets of honey water");
            helper.assertTrue(kettle.items().getStackInSlot(0).getCount() == 1, "2 honey bottles per bucket");
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).is(Items.GLASS_BOTTLE)
                    && kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).getCount() == 4, "the bottles come back");
        });
    }

    @GameTest(template = EMPTY)
    public static void winesSourIntoVinegarButJuicesDont(GameTestHelper helper) {
        helper.assertTrue(ModFluids.RED_WINE.get().is(ModTags.Fluids.SOURS_TO_VINEGAR), "red wine sours");
        helper.assertTrue(ModFluids.CIDER.get().is(ModTags.Fluids.SOURS_TO_VINEGAR), "cider sours");
        helper.assertTrue(ModFluids.MEAD.get().is(ModTags.Fluids.SOURS_TO_VINEGAR), "mead sours");
        helper.assertFalse(ModFluids.APPLE_JUICE.get().is(ModTags.Fluids.SOURS_TO_VINEGAR), "juice doesn't");
        helper.assertTrue(ModFluids.PALE_ALE.get().is(ModTags.Fluids.ALES) && !ModFluids.CIDER.get().is(ModTags.Fluids.ALES),
                "cider isn't an ale");
        helper.succeed();
    }

    private WineTests() {}
}
