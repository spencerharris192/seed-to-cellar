package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/** Sake and koji, coffee, lager yeast and lager, wheat beer (GDD sections 8, 9.3, 10.1, 10.5). */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SakeCoffeeLagerTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest(template = EMPTY)
    public static void millingRiceLeavesBran(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.MILLSTONE.get());
        MillstoneBlockEntity mill = (MillstoneBlockEntity) helper.getBlockEntity(POS);
        mill.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.UP)
                .orElseThrow(IllegalStateException::new).insertItem(0, new ItemStack(Crops.RICE.produce(), 2), false);
        mill.crank();
        helper.runAfterDelay(MillstoneBlockEntity.CRANK_TICKS + 1, () -> {
            helper.assertTrue(mill.output().is(ModItems.POLISHED_RICE.get()) && mill.byproduct().is(ModItems.RICE_BRAN.get()),
                    "rice mills into polished rice, and its bran beside it");
            var below = mill.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, net.minecraft.core.Direction.DOWN)
                    .orElseThrow(IllegalStateException::new);
            helper.assertTrue(below.getSlots() == 2, "a hopper below takes both");
            helper.succeed();
        });
    }

    private static PreservingJarBlockEntity jar(GameTestHelper helper, FluidStack liquid, ItemStack... items) {
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
        helper.setBlock(POS, ModBlocks.PRESERVING_JAR.get());
        PreservingJarBlockEntity jar = (PreservingJarBlockEntity) helper.getBlockEntity(POS);
        if (!liquid.isEmpty()) jar.tank().fill(liquid, IFluidHandler.FluidAction.EXECUTE);
        for (int i = 0; i < items.length; i++) jar.items().setStackInSlot(i, items[i]);
        return jar;
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kojiGrowsOnlyWhereItsWarm(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper, FluidStack.EMPTY, new ItemStack(ModItems.STEAMED_RICE.get(), 2));
        helper.setBlock(POS.east(), Blocks.ICE);    // cold beside it: nothing grows
        jar.setLid(false, null);
        helper.assertFalse(jar.isWorking(), "koji won't grow in the cold");
        jar.setLid(true, null);
        helper.setBlock(POS.east(), Blocks.CAMPFIRE);   // warm beside it
        jar.setLid(false, null);
        helper.assertTrue(jar.isWorking(), "it grows where it's warm");
        helper.succeedWhen(() -> {
            jar.advance();
            helper.assertTrue(jar.items().getStackInSlot(0).is(ModItems.KOJI_RICE.get()) && jar.items().getStackInSlot(0).getCount() == 2,
                    "two steamed rice become two koji rice");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aleYeastBecomesLagerYeastInTheCold(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper, new FluidStack(Fluids.WATER, 250), new ItemStack(ModItems.ALE_YEAST.get()),
                new ItemStack(net.minecraft.world.item.Items.SUGAR));
        helper.setBlock(POS.east(), Blocks.PACKED_ICE);
        jar.setLid(false, null);
        helper.succeedWhen(() -> {
            jar.advance();
            helper.assertTrue(jar.items().getStackInSlot(0).is(ModItems.LAGER_YEAST.get()), "cold ale yeast becomes lager yeast");
        });
    }

    private static Optional<FermentingRecipe> best(GameTestHelper helper, FluidStack wort, YeastType yeast) {
        return helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get()).stream()
                .filter(r -> r.matches(wort, yeast)).max(Comparator.comparingInt(FermentingRecipe::priority));
    }

    @GameTest(template = EMPTY)
    public static void lagerYeastBrewsLagerFromPaleWort(GameTestHelper helper) {
        FluidStack pale = new WortData(Map.of(MaltType.PALE, 1F), WortData.Strength.NORMAL).applyTo(new FluidStack(ModFluids.HOPPED_WORT.get(), 1000));
        helper.assertTrue(best(helper, pale, YeastType.LAGER).map(FermentingRecipe::result).orElse(null) == ModFluids.LAGER.get(),
                "pale wort with lager yeast is lager");
        helper.assertTrue(best(helper, pale, YeastType.ALE).map(FermentingRecipe::result).orElse(null) == ModFluids.PALE_ALE.get(),
                "with ale yeast it's still pale ale");
        FluidStack amber = new WortData(Map.of(MaltType.PALE, 0.5F, MaltType.AMBER, 0.5F), WortData.Strength.NORMAL)
                .applyTo(new FluidStack(ModFluids.HOPPED_WORT.get(), 1000));
        helper.assertTrue(best(helper, amber, YeastType.LAGER).isPresent(), "lager yeast still ferments an amber wort (no dead ends)");
        FluidStack wheat = new WortData(Map.of(MaltType.PALE, 0.4F, MaltType.WHEAT, 0.6F), WortData.Strength.NORMAL)
                .applyTo(new FluidStack(ModFluids.HOPPED_WORT.get(), 1000));
        helper.assertTrue(best(helper, wheat, YeastType.ALE).map(FermentingRecipe::result).orElse(null) == ModFluids.WHEAT_BEER.get(),
                "half or more wheat malt makes wheat beer");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sakeAndCoffeeHaveTheirRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(recipes.getAllRecipesFor(ModRecipes.MIXING.get()).stream()
                .anyMatch(r -> r.result() == ModFluids.SAKE_MASH.get() && r.ingredient().test(new ItemStack(ModItems.KOJI_RICE.get()))),
                "koji rice stirs into water as sake mash");
        FluidStack mash = new FluidStack(ModFluids.SAKE_MASH.get(), 1000);
        helper.assertTrue(recipes.getAllRecipesFor(ModRecipes.FERMENTING.get()).stream().anyMatch(r -> r.matches(mash, YeastType.WINE)
                && r.result() == ModFluids.SAKE.get()), "sake mash ferments into sake with wine yeast");
        helper.assertTrue(recipes.getAllRecipesFor(ModRecipes.KILNING.get()).stream()
                .anyMatch(r -> r.ingredient().test(new ItemStack(Crops.COFFEE.produce())) && r.result().is(ModItems.ROASTED_COFFEE.get())),
                "coffee beans roast in the kiln");
        helper.assertTrue(recipes.getAllRecipesFor(ModRecipes.COOKING.get()).stream()
                .anyMatch(r -> r.result().is(io.github.spencerharris192.seedtocellar.brewing.Drinks.COFFEE.item().get())),
                "ground coffee brews in the kettle");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void gingerBeerBrewsInAJar(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper, new FluidStack(Fluids.WATER, 1000), new ItemStack(Crops.GINGER.produce()),
                new ItemStack(net.minecraft.world.item.Items.SUGAR), new ItemStack(ModItems.ALE_YEAST.get()));
        jar.setLid(false, null);
        helper.succeedWhen(() -> {
            jar.advance();
            helper.assertTrue(jar.tank().getFluid().getFluid() == ModFluids.GINGER_BEER.get(), "ginger, sugar and ale yeast make ginger beer");
        });
    }
}
