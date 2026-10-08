package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/** Rum (cane juice, molasses) and tequila (the agave, a desert succulent), GDD sections 6.1, 9.4, 10.3. */
public final class RumTequilaTests {
    private static final String EMPTY = "empty";
    private static final BlockPos GROUND = new BlockPos(1, 1, 1);
    private static final BlockPos PLANT = GROUND.above();

    private static void plantPup(GameTestHelper helper, Player player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Crops.AGAVE.seeds()));
        BlockPos abs = helper.absolutePos(GROUND);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false)));
    }

    @GameTest(template = EMPTY)
    public static void agavePupsPlantOnDesertSoilOnly(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.setBlock(GROUND, Blocks.SAND);
        plantPup(helper, player);
        helper.assertBlockPresent(Crops.AGAVE.block(), PLANT);
        for (var soil : new net.minecraft.world.level.block.Block[]{Blocks.RED_SAND, Blocks.COARSE_DIRT, Blocks.TERRACOTTA}) {
            helper.setBlock(PLANT, Blocks.AIR);
            helper.setBlock(GROUND, soil);
            plantPup(helper, player);
            helper.assertBlockPresent(Crops.AGAVE.block(), PLANT);
        }
        for (var soil : new net.minecraft.world.level.block.Block[]{Blocks.FARMLAND, Blocks.GRASS_BLOCK}) {
            helper.setBlock(PLANT, Blocks.AIR);
            helper.setBlock(GROUND, soil);
            plantPup(helper, player);
            helper.assertBlockNotPresent(Crops.AGAVE.block(), PLANT);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void agaveInFlowerGivesItsHeartAndReplants(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.SAND);
        helper.setBlock(PLANT, Crops.AGAVE.block().defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE));
        helper.useBlock(PLANT, helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
        helper.assertBlockProperty(PLANT, CropBlock.AGE, 0);
        helper.assertItemEntityPresent(Crops.AGAVE.produce(), PLANT, 2.0);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void agaveGrowsOnSandWithBoneMeal(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.SAND);
        helper.setBlock(PLANT, Crops.AGAVE.block());
        BlockPos abs = helper.absolutePos(PLANT);
        for (int i = 0; i < 20; i++) {
            var state = helper.getLevel().getBlockState(abs);
            if (!(state.getBlock() instanceof CropBlock crop) || crop.isMaxAge(state)) break;
            crop.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), abs, state, net.minecraft.world.level.block.BonemealSource.INTERACTION);
        }
        helper.assertBlockProperty(PLANT, CropBlock.AGE, CropBlock.MAX_AGE);
        helper.assertTrue(Crops.AGAVE.block().defaultBlockState().canSurvive(helper.getLevel(), abs), "and stays put on sand");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void caneJuiceBoilsDownToMolasses(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CAMPFIRE);
        BlockPos kettlePos = new BlockPos(1, 2, 1);
        helper.setBlock(kettlePos, ModBlocks.BREW_KETTLE.get());
        BrewKettleBlockEntity kettle = (BrewKettleBlockEntity) helper.getBlockEntity(kettlePos, net.minecraft.world.level.block.entity.BlockEntity.class);
        helper.assertTrue(kettle.tank().fill(new FluidStack(ModFluids.CANE_JUICE.get(), 2000), StationTank.Action.EXECUTE) == 2000,
                "the kettle takes cane juice");
        helper.succeedWhen(() -> helper.assertTrue(kettle.tank().getFluid().getFluid() == ModFluids.MOLASSES.get()
                && kettle.tank().getFluidAmount() == 2000, "boiled down alone, it turns to molasses"));
    }

    @GameTest(template = EMPTY)
    public static void molassesBottlesAsASweetener(GameTestHelper helper) {
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        var handler = Handlers.fluids(bottle);
        helper.assertTrue(handler.fill(new FluidStack(ModFluids.MOLASSES.get(), 250), StationTank.Action.EXECUTE) == 250,
                "a glass bottle takes molasses");
        helper.assertTrue(handler.getContainer().is(ModItems.MOLASSES.get()), "and becomes a bottle of molasses");
        helper.assertTrue(handler.getContainer().is(io.github.spencerharris192.seedtocellar.registry.ModTags.Items.SWEETENERS),
                "which sweetens like sugar");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void washesFermentAndDistil(GameTestHelper helper) {
        var recipes = helper.getLevel().recipeAccess();
        for (Fluid[] chain : new Fluid[][]{{ModFluids.MOLASSES.get(), ModFluids.RUM_WASH.get(), ModFluids.RUM.get()},
                {ModFluids.AGAVE_JUICE.get(), ModFluids.AGAVE_WASH.get(), ModFluids.TEQUILA.get()}}) {
            FluidStack in = new FluidStack(chain[0], 1000);
            FermentingRecipe ferment = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.FERMENTING.get())
                    .filter(r -> r.matches(in, YeastType.ALE)).findFirst().orElseThrow();
            helper.assertTrue(ferment.result() == chain[1] && ferment.suits(Temperature.WARM), "ferments into its wash at Mild or Warm");
            FluidStack wash = new FluidStack(chain[1], 1000);
            var distil = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.DISTILLING.get())
                    .filter(r -> r.matches(wash, false, java.util.List.of()))
                    .max(java.util.Comparator.comparingInt(r -> r.priority())).orElseThrow();
            helper.assertTrue(distil.resultFor(wash) == chain[2], "and the wash distils into its spirit");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void agaveIsRoastedThenPressed(GameTestHelper helper) {
        var recipes = helper.getLevel().recipeAccess();
        KilningRecipe roast = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.KILNING.get())
                .filter(r -> r.ingredient().test(new ItemStack(Crops.AGAVE.produce()))).findFirst().orElseThrow();
        helper.assertTrue(roast.roast() == RoastLevel.MEDIUM && roast.result().is(ModItems.ROASTED_AGAVE.get()), "Medium in the kiln");
        PressingRecipe press = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.PRESSING.get())
                .filter(r -> r.ingredient().test(new ItemStack(ModItems.ROASTED_AGAVE.get()))).findFirst().orElseThrow();
        helper.assertTrue(press.result().getFluid() == ModFluids.AGAVE_JUICE.get(), "pressed into agave juice");
        PressingRecipe cane = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.PRESSING.get())
                .filter(r -> r.ingredient().test(new ItemStack(Items.SUGAR_CANE))).findFirst().orElseThrow();
        helper.assertTrue(cane.result().getFluid() == ModFluids.CANE_JUICE.get(), "sugar cane presses into cane juice");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rumAndTequilaChangeNameWithAge(GameTestHelper helper) {
        FluidStack rum = new BrewQuality(true, true, true, false).applyTo(new FluidStack(ModFluids.RUM.get(), 1000));
        helper.assertTrue("drink.seedtocellar.white_rum".equals(Drinks.nameKey(rum.getFluid(), io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(rum))), "young rum is White Rum");
        FluidStack gold = CaskBlockEntity.serving(rum, 3 * CaskBlockEntity.DAY, CaskWood.JUNGLE);
        helper.assertTrue("drink.seedtocellar.gold_rum".equals(Drinks.nameKey(gold.getFluid(), io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(gold))), "Gold at 2+");
        FluidStack dark = CaskBlockEntity.serving(rum, 6 * CaskBlockEntity.DAY, CaskWood.JUNGLE);
        helper.assertTrue("drink.seedtocellar.dark_rum".equals(Drinks.nameKey(dark.getFluid(), io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(dark))) && BrewQuality.of(dark).aged(),
                "Dark at 6, with its star in jungle wood");
        FluidStack tequila = new BrewQuality(true, true, true, false).applyTo(new FluidStack(ModFluids.TEQUILA.get(), 1000));
        FluidStack anejo = CaskBlockEntity.serving(tequila, 4 * CaskBlockEntity.DAY, CaskWood.MANGROVE);
        helper.assertTrue("drink.seedtocellar.tequila_anejo".equals(Drinks.nameKey(anejo.getFluid(), io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(anejo))) && BrewQuality.of(anejo).aged(),
                "Añejo at 4 years, starred in mangrove");
        helper.succeed();
    }
}
