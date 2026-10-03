package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Comparator;

/** Spirits that age (GDD sections 10.3, 13): names and colors by age, charred casks, brandies and grappa. */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AgingSpiritsTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final long DAY = CaskBlockEntity.DAY;

    /** A double-distilled spirit with yeast and temperature stars, as it leaves the still. */
    private static FluidStack spirit(Fluid fluid) {
        FluidStack stack = new BrewQuality(true, true, true, false).applyTo(new FluidStack(fluid, 1000));
        stack.getOrCreateTag().putInt(CraftStep.RUNS, 2);
        return stack;
    }

    @GameTest(template = EMPTY)
    public static void whiskeyIsNewMakeUntilThreeYears(GameTestHelper helper) {
        helper.assertTrue("drink.seedtocellar.new_make".equals(Drinks.nameKey(ModFluids.MALT_WHISKEY.get(), spirit(ModFluids.MALT_WHISKEY.get()).getTag())),
                "straight from the still it's New Make");
        FluidStack two = CaskBlockEntity.serving(spirit(ModFluids.MALT_WHISKEY.get()), 2 * DAY, CaskWood.OAK);
        helper.assertTrue("drink.seedtocellar.new_make".equals(Drinks.nameKey(two.getFluid(), two.getTag())), "still New Make at 2 years");
        FluidStack three = CaskBlockEntity.serving(spirit(ModFluids.MALT_WHISKEY.get()), 3 * DAY, CaskWood.OAK);
        helper.assertTrue(Drinks.nameKey(three.getFluid(), three.getTag()) == null, "Malt Whiskey (its own name) at 3 years");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bourbonNeedsCharredOak(GameTestHelper helper) {
        FluidStack white = spirit(ModFluids.BOURBON.get());
        helper.assertTrue("drink.seedtocellar.white_dog".equals(Drinks.nameKey(white.getFluid(), white.getTag())), "young bourbon is White Dog");
        FluidStack charred = CaskBlockEntity.serving(white, 6 * DAY, CaskWood.OAK, true);
        helper.assertTrue(Drinks.nameKey(charred.getFluid(), charred.getTag()) == null, "2+ years in charred oak: Bourbon");
        helper.assertTrue(BrewQuality.of(charred).aged() && BrewQuality.of(charred).stars() == 5, "and its star at 6 years");
        helper.assertTrue(charred.getTag().getBoolean(DrinkItem.CHARRED), "the label remembers the char");
        FluidStack plain = CaskBlockEntity.serving(white, 6 * DAY, CaskWood.OAK, false);
        helper.assertTrue("drink.seedtocellar.corn_whiskey".equals(Drinks.nameKey(plain.getFluid(), plain.getTag())),
                "aged in plain oak it's Corn Whiskey");
        helper.assertFalse(BrewQuality.of(plain).aged(), "and its star takes twice as long there");
        helper.assertTrue(BrewQuality.of(CaskBlockEntity.serving(white, 12 * DAY, CaskWood.OAK, false)).aged(), "12 years, then");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void anyCharredCaskIsIdealForWhiskey(GameTestHelper helper) {
        FluidStack whiskey = spirit(ModFluids.MALT_WHISKEY.get());
        helper.assertTrue(BrewQuality.of(CaskBlockEntity.serving(whiskey, 8 * DAY, CaskWood.BIRCH, true)).aged(), "charred birch is ideal");
        helper.assertFalse(BrewQuality.of(CaskBlockEntity.serving(whiskey, 8 * DAY, CaskWood.BIRCH, false)).aged(), "plain birch isn't");
        helper.assertFalse(Drinks.BRANDY.profile().idealIn(CaskWood.SPRUCE, true), "charring doesn't help brandy");
        helper.succeed();
    }

    /** Each spirit and liqueur stands on the shelves in its own bottle: no two look the same. */
    @GameTest(template = EMPTY)
    public static void everySpiritHasItsOwnBottle(GameTestHelper helper) {
        var seen = new java.util.HashSet<io.github.spencerharris192.seedtocellar.brewing.BottleLook>();
        for (Drinks.Drink drink : Drinks.all()) {
            var look = io.github.spencerharris192.seedtocellar.brewing.BottleLook.of(drink);
            boolean spiritBottle = drink.vessel() == io.github.spencerharris192.seedtocellar.brewing.Vessel.SPIRIT_BOTTLE;
            helper.assertTrue(spiritBottle == (look != null), drink.name() + ": a bottle look exactly when it's in a spirit bottle");
            if (look != null) helper.assertTrue(seen.add(look), drink.name() + " looks just like another spirit");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void colorDeepensWithAge(GameTestHelper helper) {
        FluidStack young = spirit(ModFluids.MALT_WHISKEY.get());
        FluidStack old = CaskBlockEntity.serving(young, 12 * DAY, CaskWood.OAK);
        int base = ModFluids.MALT_WHISKEY.tint;
        int now = Drinks.tint(young.getFluid(), young.getTag(), base);
        int aged = Drinks.tint(old.getFluid(), old.getTag(), base);
        helper.assertTrue(now == base, "new make keeps its pale color");
        helper.assertTrue((aged >> 8 & 255) < (now >> 8 & 255) && (aged & 255) < (now & 255), "12 years make it amber (less green and blue)");
        helper.assertTrue(Drinks.tint(ModFluids.PALE_ALE.get(), null, 7) == 7, "drinks without a style keep their color");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void flintAndSteelCharsAnEmptyCask(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.OAK_CASK.get());
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        BlockPos at = helper.absolutePos(POS);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(at), Direction.NORTH, at, false);
        BlockState state = helper.getBlockState(POS);
        state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(POS, CaskBlock.CHARRED, true);
        // Again on a charred cask: the cask takes the click, so the flint and steel never lights a fire beside it.
        helper.assertTrue(helper.getBlockState(POS).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit).consumesAction(),
                "flint and steel on a charred cask does nothing else");
        helper.assertTrue(io.github.spencerharris192.seedtocellar.brewing.CaskItem.charred(
                helper.getBlockState(POS).getCloneItemStack(new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(at), Direction.NORTH, at, false),
                        helper.getLevel(), at, player)), "pick block on a charred cask gives a charred cask");

        helper.setBlock(POS.east(), ModBlocks.OAK_CASK.get());
        CaskBlockEntity full = (CaskBlockEntity) helper.getBlockEntity(POS.east());
        full.tank().fill(spirit(ModFluids.MALT_WHISKEY.get()), IFluidHandler.FluidAction.EXECUTE);
        BlockPos east = helper.absolutePos(POS.east());
        helper.getBlockState(POS.east()).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(east), Direction.NORTH, east, false));
        helper.assertBlockProperty(POS.east(), CaskBlock.CHARRED, false);   // only an empty cask chars

        helper.setBlock(POS.west(), ModBlocks.CASKS.get(CaskWood.WARPED).get());
        BlockPos west = helper.absolutePos(POS.west());
        helper.getBlockState(POS.west()).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(west), Direction.NORTH, west, false));
        helper.assertBlockProperty(POS.west(), CaskBlock.CHARRED, false);   // nether wood won't char
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void wineDistilsIntoEauDeVieThenBrandy(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.CAMPFIRE);
        BlockState lower = ModBlocks.POT_STILL.get().defaultBlockState();
        helper.setBlock(POS, lower);
        helper.setBlock(POS.above(), lower.setValue(PotStillBlock.HALF, DoubleBlockHalf.UPPER));
        PotStillBlockEntity still = (PotStillBlockEntity) helper.getBlockEntity(POS);
        still.pot().fill(new BrewQuality(true, true, true, true).applyTo(new FluidStack(ModFluids.WHITE_WINE.get(), 2000)),
                IFluidHandler.FluidAction.EXECUTE);
        helper.succeedWhen(() -> {
            FluidStack out = still.receiver().getFluid();
            helper.assertTrue(out.getFluid() == ModFluids.BRANDY.get(), "white wine distils into brandy");
            helper.assertTrue("drink.seedtocellar.eau_de_vie".equals(Drinks.nameKey(out.getFluid(), out.getTag())), "young brandy is Eau-de-vie");
            BrewQuality q = BrewQuality.of(out);
            helper.assertTrue(q.yeast() && q.temperature() && !q.craft() && !q.aged(), "only the wine's fermenting stars carry over");
        });
    }

    @GameTest(template = EMPTY)
    public static void fruitWinesHaveTheirBrandies(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLING.get());
        for (var pair : java.util.List.of(java.util.List.of(ModFluids.CIDER, ModFluids.APPLE_BRANDY), java.util.List.of(ModFluids.PERRY, ModFluids.PEAR_BRANDY),
                java.util.List.of(ModFluids.CHERRY_WINE, ModFluids.KIRSCH), java.util.List.of(ModFluids.PLUM_WINE, ModFluids.SLIVOVITZ),
                java.util.List.of(ModFluids.PEACH_WINE, ModFluids.PEACH_BRANDY), java.util.List.of(ModFluids.POMACE_WASH, ModFluids.GRAPPA))) {
            FluidStack pot = new FluidStack(pair.get(0).get(), 1000);
            var recipe = recipes.stream().filter(r -> r.matches(pot, false, java.util.List.of()))
                    .max(Comparator.comparingInt(r -> r.priority())).orElseThrow();
            helper.assertTrue(recipe.resultFor(pot) == pair.get(1).get(), pair.get(0).name + " distils into " + pair.get(1).name);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void pomaceMashFermentsWithWineYeast(GameTestHelper helper) {
        FluidStack mash = new FluidStack(ModFluids.POMACE_MASH.get(), 1000);
        FermentingRecipe recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get()).stream()
                .filter(r -> r.matches(mash, YeastType.WINE)).findFirst().orElseThrow();
        helper.assertTrue(recipe.result() == ModFluids.POMACE_WASH.get() && recipe.suits(Temperature.WARM), "pomace mash ferments into pomace wash");
        helper.succeed();
    }
}
