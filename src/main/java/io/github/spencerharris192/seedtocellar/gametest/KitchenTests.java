package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.food.FeastBlock;
import io.github.spencerharris192.seedtocellar.food.PieBlock;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/** Kitchen (GDD section 15): flours, doughs, breads. */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class KitchenTests {
    private static final String EMPTY = "empty";
    private static final BlockPos KETTLE = new BlockPos(1, 2, 1);

    @GameTest(template = EMPTY)
    public static void millstoneGrindsRyeCornAndOats(GameTestHelper helper) {
        assertMills(helper, Crops.RYE.produce(), ModItems.RYE_FLOUR.get());
        assertMills(helper, Crops.CORN.produce(), ModItems.CORNMEAL.get());
        assertMills(helper, Crops.OATS.produce(), ModItems.ROLLED_OATS.get());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sourdoughStarterIsKeptWhenMixingDough(GameTestHelper helper) {
        TransientCraftingContainer grid = new TransientCraftingContainer(new NoMenu(), 3, 3);
        grid.setItem(0, new ItemStack(ModItems.DOUGH.get()));
        grid.setItem(1, new ItemStack(ModItems.SOURDOUGH_STARTER.get()));
        Optional<CraftingRecipe> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "dough + starter should craft");
        helper.assertTrue(recipe.get().assemble(grid, helper.getLevel().registryAccess()).is(ModItems.SOURDOUGH_DOUGH.get()),
                "dough + starter makes sourdough dough");
        NonNullList<ItemStack> left = recipe.get().getRemainingItems(grid);
        helper.assertTrue(left.stream().anyMatch(s -> s.is(ModItems.SOURDOUGH_STARTER.get())), "the starter comes back");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sourdoughBreadCuresAHangover(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.addEffect(new MobEffectInstance(ModEffects.HANGOVER.get(), 2400));
        ItemStack bread = new ItemStack(ModItems.SOURDOUGH_BREAD.get());
        bread.finishUsingItem(helper.getLevel(), player);
        helper.assertFalse(player.hasEffect(ModEffects.HANGOVER.get()), "sourdough bread cures a hangover");
        player.addEffect(new MobEffectInstance(ModEffects.HANGOVER.get(), 2400));
        new ItemStack(ModItems.RYE_BREAD.get()).finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(ModEffects.HANGOVER.get()), "ordinary bread doesn't");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void doughsBakeInAFurnace(GameTestHelper helper) {
        assertBakes(helper, ModItems.DOUGH.get(), Items.BREAD);
        assertBakes(helper, ModItems.RYE_DOUGH.get(), ModItems.RYE_BREAD.get());
        assertBakes(helper, ModItems.SOURDOUGH_DOUGH.get(), ModItems.SOURDOUGH_BREAD.get());
        assertBakes(helper, ModItems.SPENT_GRAIN_DOUGH.get(), ModItems.SPENT_GRAIN_BREAD.get());
        assertBakes(helper, ModItems.BEER_BREAD_DOUGH.get(), ModItems.BEER_BREAD.get());
        assertBakes(helper, ModItems.CORNBREAD_BATTER.get(), ModItems.CORNBREAD.get());
        assertBakes(helper, ModItems.MASA.get(), ModItems.TORTILLA.get());
        assertBakes(helper, Crops.CORN.produce(), ModItems.ROASTED_CORN.get());
        helper.succeed();
    }

    // --- Brew Kettle cooking ---

    private static BrewKettleBlockEntity heatedKettle(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(KETTLE.below(), Blocks.CAMPFIRE);
        helper.setBlock(KETTLE, ModBlocks.BREW_KETTLE.get());
        return (BrewKettleBlockEntity) helper.getBlockEntity(KETTLE);
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void kettleCooksTomatoSoupIntoBowls(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(Crops.TOMATO.produce(), 2));
        kettle.items().setStackInSlot(3, new ItemStack(Crops.ONION.produce(), 2));   // any slots, any order
        kettle.items().setStackInSlot(BrewKettleBlockEntity.CONTAINER, new ItemStack(Items.BOWL, 2));
        helper.succeedWhen(() -> {
            ItemStack out = kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT);
            helper.assertTrue(out.is(ModItems.TOMATO_SOUP.get()) && out.getCount() == 2, "two bowls of tomato soup");
            helper.assertTrue(kettle.tank().getFluidAmount() == 500, "250 mB of water per serving");
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).isEmpty(), "both bowls used");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void kettleCooksPorridgeWithMilkAndGivesBackTheHoneyBottle(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(ForgeMod.MILK.get(), 250), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(kettle.tank().getFluidAmount() == 250, "the kettle takes milk");
        kettle.items().setStackInSlot(1, new ItemStack(ModItems.ROLLED_OATS.get()));
        kettle.items().setStackInSlot(2, new ItemStack(Items.HONEY_BOTTLE));
        kettle.items().setStackInSlot(BrewKettleBlockEntity.CONTAINER, new ItemStack(Items.BOWL));
        helper.succeedWhen(() -> {
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).is(ModItems.PORRIDGE.get()), "porridge");
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.CONTAINER).is(Items.GLASS_BOTTLE),
                    "the honey's bottle goes into the empty bowl slot");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void kettleWontCookWithoutTheBowl(GameTestHelper helper) {
        BrewKettleBlockEntity kettle = heatedKettle(helper);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(Crops.TOMATO.produce()));
        kettle.items().setStackInSlot(1, new ItemStack(Crops.ONION.produce()));
        helper.assertTrue(kettle.matchingRecipe().isPresent(), "tomato + onion is tomato soup");
        helper.assertTrue(kettle.missingForCooking() != null, "but it needs a bowl");
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(kettle.items().getStackInSlot(BrewKettleBlockEntity.OUTPUT).isEmpty(), "nothing cooked without a bowl");
            helper.assertTrue(kettle.items().getStackInSlot(0).getCount() == 1, "ingredients untouched");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void riceBallsGiveTheBowlBack(GameTestHelper helper) {
        TransientCraftingContainer grid = new TransientCraftingContainer(new NoMenu(), 3, 3);
        grid.setItem(4, new ItemStack(ModItems.COOKED_RICE.get()));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel()).isEmpty(),
                "rice alone isn't enough: it needs kelp");
        grid.setItem(5, new ItemStack(Items.DRIED_KELP));
        Optional<CraftingRecipe> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().assemble(grid, helper.getLevel().registryAccess()).is(ModItems.RICE_BALL.get()),
                "cooked rice + dried kelp makes rice balls");
        helper.assertTrue(recipe.get().getRemainingItems(grid).stream().anyMatch(s -> s.is(Items.BOWL)), "the bowl comes back");
        helper.succeed();
    }

    // --- Preserving Jar: vinegar and pickles ---

    private static PreservingJarBlockEntity jar(GameTestHelper helper) {
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);   // a day becomes 24 ticks
        helper.setBlock(KETTLE, ModBlocks.PRESERVING_JAR.get());
        return (PreservingJarBlockEntity) helper.getBlockEntity(KETTLE);
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void openBeerSoursIntoVinegarAndGrowsAMother(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper);
        jar.tank().fill(new FluidStack(ModFluids.PALE_ALE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(jar.isSouring(), "beer in an open, otherwise empty jar starts souring");
        helper.succeedWhen(() -> {
            helper.assertTrue(jar.tank().getFluid().getFluid() == ModFluids.VINEGAR.get(), "the beer became vinegar");
            helper.assertTrue(jar.tank().getFluidAmount() == 1000, "all of it");
            helper.assertTrue(jar.items().getStackInSlot(0).is(ModItems.MOTHER_OF_VINEGAR.get()), "a mother of vinegar formed");
        });
    }

    @GameTest(template = EMPTY)
    public static void beerDoesntSourWithTheLidOnOrWithOtherThingsInIt(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper);
        jar.tank().fill(new FluidStack(ModFluids.PALE_ALE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        jar.setLid(false, null);
        helper.assertFalse(jar.isSouring(), "a closed jar doesn't sour");
        jar.setLid(true, null);
        helper.assertTrue(jar.isSouring(), "opened again, it does");
        jar.items().setStackInSlot(0, new ItemStack(Crops.CUCUMBER.produce()));
        helper.assertFalse(jar.isSouring(), "not with a cucumber in it");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void cucumbersPickleInVinegar(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper);
        jar.tank().fill(new FluidStack(ModFluids.VINEGAR.get(), 250), IFluidHandler.FluidAction.EXECUTE);
        jar.items().setStackInSlot(0, new ItemStack(Crops.CUCUMBER.produce(), 2));
        jar.items().setStackInSlot(1, new ItemStack(Crops.GARLIC.produce()));
        jar.items().setStackInSlot(2, new ItemStack(Crops.CORIANDER.produce()));
        jar.setLid(false, null);
        helper.assertTrue(jar.isWorking(), "closing the lid starts the pickling");
        helper.succeedWhen(() -> {
            ItemStack out = jar.items().getStackInSlot(0);
            helper.assertTrue(out.is(ModItems.PICKLES.get()) && out.getCount() == 4, "four pickles");
            helper.assertTrue(jar.tank().isEmpty(), "the vinegar went into them");
        });
    }

    // --- Pies ---

    @GameTest(template = EMPTY)
    public static void aPlacedPieGivesFourSlicesThenIsGone(GameTestHelper helper) {
        helper.setBlock(KETTLE.below(), Blocks.STONE);
        helper.setBlock(KETTLE, Pies.BLUEBERRY.block().get());
        Player player = helper.makeMockPlayer();
        for (int i = 0; i < PieBlock.SLICES - 1; i++) {
            helper.useBlock(KETTLE, player);
            helper.assertBlockProperty(KETTLE, PieBlock.BITES, i + 1);
        }
        helper.useBlock(KETTLE, player);
        helper.assertBlockNotPresent(Pies.BLUEBERRY.block().get(), KETTLE);
        helper.assertTrue(player.getInventory().countItem(Pies.BLUEBERRY.slice().get()) == PieBlock.SLICES, "four slices");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theHarvestFeastServesSixWithBowlsThenLeavesABone(GameTestHelper helper) {
        helper.setBlock(KETTLE.below(), Blocks.STONE);
        helper.setBlock(KETTLE, ModBlocks.HARVEST_FEAST.get());
        Player player = helper.makeMockPlayer();
        helper.useBlock(KETTLE, player);   // no bowl: nothing happens
        helper.assertBlockProperty(KETTLE, FeastBlock.SERVINGS, FeastBlock.SERVINGS_MAX);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL, 8));
        for (int i = 0; i < FeastBlock.SERVINGS_MAX; i++) helper.useBlock(KETTLE, player);
        helper.assertBlockProperty(KETTLE, FeastBlock.SERVINGS, 0);
        helper.assertTrue(player.getInventory().countItem(ModItems.HARVEST_FEAST_SERVING.get()) == FeastBlock.SERVINGS_MAX, "six servings");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "one bowl each");
        helper.useBlock(KETTLE, player);   // clear the leftovers
        helper.assertBlockNotPresent(ModBlocks.HARVEST_FEAST.get(), KETTLE);
        helper.assertItemEntityPresent(Items.BONE, KETTLE, 2.0);
        helper.succeed();
    }

    private static void assertMills(GameTestHelper helper, Item input, Item output) {
        Optional<MillingRecipe> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(ModRecipes.MILLING.get(), new SimpleContainer(new ItemStack(input)), helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().result().is(output), input + " should mill into " + output);
    }

    private static void assertBakes(GameTestHelper helper, Item input, Item output) {
        var recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack(input)), helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().getResultItem(helper.getLevel().registryAccess()).is(output),
                input + " should bake into " + output);
    }

    /** A crafting grid needs a menu to tell about changes; this one ignores them. */
    private static final class NoMenu extends AbstractContainerMenu {
        NoMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private KitchenTests() {}
}
