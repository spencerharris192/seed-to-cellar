package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;

@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BrewhouseTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private static void fast() {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
    }

    private static FluidStack wort(Fluid fluid, int amount, Map<MaltType, Float> shares, WortData.Strength strength) {
        return new WortData(shares, strength).applyTo(new FluidStack(fluid, amount));
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kettleMashesGristIntoWortWithStrengthAndShares(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, Blocks.CAMPFIRE);
        BlockPos kettlePos = POS.above();
        helper.setBlock(kettlePos, ModBlocks.BREW_KETTLE.get());
        BrewKettleBlockEntity kettle = (BrewKettleBlockEntity) helper.getBlockEntity(kettlePos);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 2000), IFluidHandler.FluidAction.EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(ModItems.PALE_GRIST.get(), 3));
        kettle.items().setStackInSlot(1, new ItemStack(ModItems.AMBER_GRIST.get(), 1));
        helper.succeedWhen(() -> {
            FluidStack fluid = kettle.tank().getFluid();
            helper.assertTrue(fluid.getFluid() == ModFluids.SWEET_WORT.get() && fluid.getAmount() == 2000, "2000 mB sweet wort expected");
            WortData data = WortData.of(fluid);
            helper.assertTrue(data.strength() == WortData.Strength.NORMAL, "4 grist in 2 buckets should be Normal, was " + data.strength());
            helper.assertTrue(Math.abs(data.share(MaltType.AMBER) - 0.25F) < 0.01F, "amber share should be 25%");
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).getCount() == 2, "4 grist -> 2 spent grain");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kettleBoilsWithOneHopPerBucket(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, Blocks.MAGMA_BLOCK);
        BlockPos kettlePos = POS.above();
        helper.setBlock(kettlePos, ModBlocks.BREW_KETTLE.get());
        BrewKettleBlockEntity kettle = (BrewKettleBlockEntity) helper.getBlockEntity(kettlePos);
        kettle.tank().setFluid(wort(ModFluids.SWEET_WORT.get(), 2000, Map.of(MaltType.PALE, 1F), WortData.Strength.NORMAL));
        kettle.items().setStackInSlot(3, new ItemStack(ModItems.DRIED_HOPS.get(), 3));   // hops in any ingredient slot
        helper.succeedWhen(() -> {
            helper.assertTrue(kettle.tank().getFluid().getFluid() == ModFluids.HOPPED_WORT.get(), "should become hopped wort");
            helper.assertTrue(kettle.items().getStackInSlot(3).getCount() == 1, "2 buckets use 2 hops");
            helper.assertTrue(WortData.of(kettle.tank().getFluid()).share(MaltType.PALE) == 1F, "malt data carries over");
        });
    }

    @GameTest(template = EMPTY)
    public static void beerStylesFollowTheRules(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        FermentingVatBlockEntity vat = (FermentingVatBlockEntity) helper.getBlockEntity(POS);
        Fluid hopped = ModFluids.HOPPED_WORT.get();
        var normal = WortData.Strength.NORMAL;
        expectStyle(helper, vat, wort(hopped, 1000, Map.of(MaltType.PALE, 1F), normal), ModFluids.PALE_ALE.get());
        expectStyle(helper, vat, wort(hopped, 1000, Map.of(MaltType.PALE, 0.75F, MaltType.AMBER, 0.25F), normal), ModFluids.AMBER_ALE.get());
        expectStyle(helper, vat, wort(hopped, 1000, Map.of(MaltType.PALE, 0.75F, MaltType.BLACK, 0.25F), normal), ModFluids.STOUT.get());
        expectStyle(helper, vat, wort(hopped, 1000, Map.of(MaltType.PALE, 0.5F, MaltType.AMBER, 0.5F), WortData.Strength.STRONG), ModFluids.OLD_ALE.get());
        expectStyle(helper, vat, wort(ModFluids.SWEET_WORT.get(), 1000, Map.of(MaltType.PALE, 1F), normal), ModFluids.PLAIN_ALE.get());
        expectStyle(helper, vat, wort(hopped, 1000, Map.of(MaltType.PALE, 1F), WortData.Strength.LIGHT), ModFluids.TABLE_BEER.get());
        helper.succeed();
    }

    private static void expectStyle(GameTestHelper helper, FermentingVatBlockEntity vat, FluidStack wort, Fluid expected) {
        vat.tank().setFluid(wort);
        Optional<FermentingRecipe> recipe = vat.expectedRecipe();
        helper.assertTrue(recipe.isPresent() && recipe.get().result() == expected,
                "expected " + expected + " but got " + recipe.map(FermentingRecipe::result).orElse(null));
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void cultivatedYeastEarnsYeastStarAndGivesMoreLees(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        FermentingVatBlockEntity vat = (FermentingVatBlockEntity) helper.getBlockEntity(POS);
        vat.tank().fill(wort(ModFluids.HOPPED_WORT.get(), 2000, Map.of(MaltType.PALE, 1F), WortData.Strength.NORMAL), IFluidHandler.FluidAction.EXECUTE);
        vat.items().setStackInSlot(FermentingVatBlockEntity.YEAST, new ItemStack(ModItems.ALE_YEAST.get()));
        boolean mild = Temperature.at(helper.getLevel(), helper.absolutePos(POS)) == Temperature.MILD;
        vat.setLid(false, null);
        helper.assertTrue(vat.isFermenting(), "closing the lid should start fermenting");
        helper.succeedWhen(() -> {
            FluidStack beer = vat.tank().getFluid();
            helper.assertTrue(beer.getFluid() == ModFluids.PALE_ALE.get() && beer.getAmount() == 2000, "2000 mB pale ale expected");
            BrewQuality q = BrewQuality.of(beer);
            helper.assertTrue(q.yeast(), "ale yeast should earn the yeast star");
            helper.assertTrue(q.temperature() == mild, "temperature star should match whether it's Mild here");
            helper.assertTrue(vat.items().getStackInSlot(FermentingVatBlockEntity.LEES).getCount() == 2, "cultured yeast gives 2 lees");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void wildFermentationWorksWithoutTheYeastStar(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        FermentingVatBlockEntity vat = (FermentingVatBlockEntity) helper.getBlockEntity(POS);
        vat.tank().fill(wort(ModFluids.SWEET_WORT.get(), 1000, Map.of(MaltType.PALE, 1F), WortData.Strength.NORMAL), IFluidHandler.FluidAction.EXECUTE);
        vat.setLid(false, null);
        helper.succeedWhen(() -> {
            helper.assertTrue(vat.tank().getFluid().getFluid() == ModFluids.PLAIN_ALE.get(), "sweet wort -> plain ale");
            helper.assertFalse(BrewQuality.of(vat.tank().getFluid()).yeast(), "wild yeast earns no yeast star");
            helper.assertTrue(vat.items().getStackInSlot(FermentingVatBlockEntity.LEES).is(ModItems.ALE_YEAST.get()), "lees become ale yeast");
        });
    }

    @GameTest(template = EMPTY)
    public static void iceMakesColdAndFireMakesWarm(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.FERMENTING_VAT.get());
        helper.setBlock(POS.east(), Blocks.PACKED_ICE);
        helper.assertTrue(Temperature.at(helper.getLevel(), helper.absolutePos(POS)) == Temperature.COLD, "ice next to it should be Cold");
        helper.setBlock(POS.west(), Blocks.MAGMA_BLOCK);
        helper.assertTrue(Temperature.at(helper.getLevel(), helper.absolutePos(POS)) == Temperature.WARM, "heat beats ice: Warm");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void jarMakesSourdoughStarter(GameTestHelper helper) {
        fast();
        helper.setBlock(POS, ModBlocks.PRESERVING_JAR.get());
        PreservingJarBlockEntity jar = (PreservingJarBlockEntity) helper.getBlockEntity(POS);
        jar.items().setStackInSlot(0, new ItemStack(ModItems.WHEAT_FLOUR.get(), 2));
        jar.tank().fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
        jar.setLid(false, null);
        helper.assertTrue(jar.isWorking(), "closing the lid on flour + water should start");
        helper.succeedWhen(() -> {
            helper.assertTrue(jar.items().getStackInSlot(0).is(ModItems.SOURDOUGH_STARTER.get()), "expected sourdough starter");
            helper.assertTrue(jar.tank().isEmpty(), "the water is used up");
        });
    }

    private BrewhouseTests() {}
}
