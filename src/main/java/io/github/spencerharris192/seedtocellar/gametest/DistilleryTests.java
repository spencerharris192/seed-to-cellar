package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Comparator;
import java.util.Map;

/** The Pot Still, the washes that feed it, and stillage on the fields (GDD sections 9.4, 11). */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DistilleryTests {
    private static final String EMPTY = "empty";
    private static final BlockPos HEAT = new BlockPos(1, 0, 1);
    private static final BlockPos STILL = new BlockPos(1, 1, 1);
    private static final IFluidHandler.FluidAction EXECUTE = IFluidHandler.FluidAction.EXECUTE;

    private static void fast() {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
    }

    /** A still over a lit campfire (or over stone, with no heat). */
    private static PotStillBlockEntity placeStill(GameTestHelper helper, boolean heated) {
        helper.setBlock(HEAT, heated ? Blocks.CAMPFIRE : Blocks.STONE);
        BlockState lower = ModBlocks.POT_STILL.get().defaultBlockState();
        helper.setBlock(STILL, lower);
        helper.setBlock(STILL.above(), lower.setValue(PotStillBlock.HALF, DoubleBlockHalf.UPPER));
        return (PotStillBlockEntity) helper.getBlockEntity(STILL);
    }

    private static FluidStack brewed(Fluid fluid, int amount, boolean yeast, boolean temperature, int runs) {
        FluidStack stack = new BrewQuality(yeast, temperature, false, false).applyTo(new FluidStack(fluid, amount));
        if (runs > 0) stack.getOrCreateTag().putInt(CraftStep.RUNS, runs);
        return stack;
    }

    private static int runs(FluidStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(CraftStep.RUNS);
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void plainAleDistilsIntoWhiskeyHalfAsMuch(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, true);
        helper.assertTrue(still.pot().fill(brewed(ModFluids.PLAIN_ALE.get(), 4000, true, true, 0), EXECUTE) == 4000,
                "the pot takes 4 buckets of Plain Ale (the malt wash)");
        helper.succeedWhen(() -> {
            FluidStack spirit = still.receiver().getFluid();
            helper.assertTrue(spirit.getFluid() == ModFluids.MALT_WHISKEY.get() && spirit.getAmount() == 2000,
                    "one run: half the wash comes over as malt whiskey, was " + spirit.getAmount() + " mB of " + spirit.getDisplayName().getString());
            helper.assertTrue(still.stillage().getFluidAmount() == 2000 && still.pot().isEmpty(), "the other half is left as stillage");
            BrewQuality q = BrewQuality.of(spirit);
            helper.assertTrue(runs(spirit) == 1 && q.yeast() && q.temperature() && !q.craft() && q.stars() == 3,
                    "the wash's yeast and temperature stars carry over; one run is no craft star yet (was " + q.stars() + ")");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void runningItAgainEarnsTheCraftStar(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, false);   // no fire yet: nothing starts while we set up
        still.receiver().fill(brewed(ModFluids.MALT_WHISKEY.get(), 2000, true, true, 1), EXECUTE);
        helper.assertTrue(still.runAgain(), "Run again pours the receiver back into the empty pot");
        helper.assertTrue(still.receiver().isEmpty() && still.pot().getFluidAmount() == 2000, "all of it, into the pot");
        helper.setBlock(HEAT, Blocks.CAMPFIRE);
        helper.succeedWhen(() -> {
            FluidStack spirit = still.receiver().getFluid();
            helper.assertTrue(spirit.getFluid() == ModFluids.MALT_WHISKEY.get() && spirit.getAmount() == 1000, "half again: 1000 mB");
            BrewQuality q = BrewQuality.of(spirit);
            helper.assertTrue(runs(spirit) == 2 && q.craft() && q.stars() == 4, "distilled twice: the craft star, 4 stars in all");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void charcoalFilterMakesVodkaAndIsUsedUp(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, true);
        still.items().setStackInSlot(PotStillBlockEntity.FILTER, new ItemStack(Items.CHARCOAL, 2));
        still.pot().fill(brewed(ModFluids.PLAIN_ALE.get(), 1000, false, false, 0), EXECUTE);
        helper.succeedWhen(() -> {
            FluidStack spirit = still.receiver().getFluid();
            helper.assertTrue(spirit.getFluid() == ModFluids.VODKA.get() && spirit.getAmount() == 500, "malt wash through charcoal: vodka");
            helper.assertTrue(BrewQuality.of(spirit).craft() && spirit.getTag().getBoolean(CraftStep.FILTERED),
                    "filtered vodka has its craft star after one run");
            helper.assertTrue(still.items().getStackInSlot(PotStillBlockEntity.FILTER).getCount() == 1, "one charcoal per run");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void thirdRunOfWhiskeyIsVodka(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, true);
        still.pot().fill(brewed(ModFluids.MALT_WHISKEY.get(), 1000, true, false, 2), EXECUTE);
        helper.succeedWhen(() -> {
            FluidStack spirit = still.receiver().getFluid();
            helper.assertTrue(spirit.getFluid() == ModFluids.VODKA.get() && runs(spirit) == 3, "a third run makes grain spirit vodka");
            BrewQuality q = BrewQuality.of(spirit);
            helper.assertTrue(q.craft() && q.yeast() && !q.temperature(), "three runs earn vodka's craft star; the wash's stars stay as they were");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void potatoWashMakesVodkaWithoutTheStarUntilThreeRuns(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, true);
        still.pot().fill(brewed(ModFluids.POTATO_WASH.get(), 2000, true, true, 0), EXECUTE);
        helper.succeedWhen(() -> {
            FluidStack spirit = still.receiver().getFluid();
            helper.assertTrue(spirit.getFluid() == ModFluids.VODKA.get() && spirit.getAmount() == 1000, "potato wash makes vodka");
            helper.assertFalse(BrewQuality.of(spirit).craft(), "vodka's star wants three runs or a filter");
        });
    }

    @GameTest(template = EMPTY)
    public static void stillNeedsHeat(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, false);
        still.pot().fill(brewed(ModFluids.PLAIN_ALE.get(), 1000, false, false, 0), EXECUTE);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(!still.isRunning() && still.pot().getFluidAmount() == 1000 && still.receiver().isEmpty(),
                    "no fire under it: nothing distils");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void receiverMustHoldTheSameSpirit(GameTestHelper helper) {
        fast();
        PotStillBlockEntity still = placeStill(helper, true);
        still.receiver().fill(brewed(ModFluids.VODKA.get(), 500, false, false, 1), EXECUTE);
        still.pot().fill(brewed(ModFluids.PLAIN_ALE.get(), 1000, false, false, 0), EXECUTE);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(!still.isRunning() && still.pot().getFluidAmount() == 1000, "won't run whiskey into a receiver of vodka");
            helper.assertTrue(still.blocked(still.expectedRecipe().orElseThrow()) != null, "and says why");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void pipesFillThePotAndDrainSpiritOrStillage(GameTestHelper helper) {
        PotStillBlockEntity still = placeStill(helper, false);
        IFluidHandler side = still.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.NORTH).orElseThrow(IllegalStateException::new);
        IFluidHandler bottom = still.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElseThrow(IllegalStateException::new);
        helper.assertTrue(side.fill(new FluidStack(ModFluids.APPLE_JUICE.get(), 1000), EXECUTE) == 0, "juice won't distil: refused");
        helper.assertTrue(side.fill(brewed(ModFluids.CORN_WASH.get(), 1000, false, false, 0), EXECUTE) == 1000, "a wash goes in");
        helper.assertTrue(bottom.fill(brewed(ModFluids.CORN_WASH.get(), 1000, false, false, 0), EXECUTE) == 0, "nothing goes in from below");
        still.receiver().fill(brewed(ModFluids.BOURBON.get(), 500, false, false, 1), EXECUTE);
        still.stillage().fill(new FluidStack(ModFluids.STILLAGE.get(), 1000), EXECUTE);
        helper.assertTrue(side.drain(250, IFluidHandler.FluidAction.SIMULATE).getFluid() == ModFluids.BOURBON.get(), "the sides give spirit");
        helper.assertTrue(bottom.drain(1000, EXECUTE).getFluid() == ModFluids.STILLAGE.get(), "the bottom gives stillage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void breakingTheHeadTakesTheWholeStill(GameTestHelper helper) {
        placeStill(helper, false);
        helper.destroyBlock(STILL.above());
        helper.assertBlockNotPresent(ModBlocks.POT_STILL.get(), STILL);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void stillageFeedsTheFarmlandAround(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(0, 1, 0), ModBlocks.FERTILE_FARMLAND.get().defaultBlockState().setValue(FertileFarmlandBlock.FERTILITY, 2));
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModFluids.STILLAGE.bucket.get()));
        BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
        player.getItemInHand(InteractionHand.MAIN_HAND).useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(center), Direction.UP, center, false)));
        helper.assertBlockProperty(new BlockPos(2, 1, 2), FertileFarmlandBlock.FERTILITY, 1);
        helper.assertBlockProperty(new BlockPos(0, 1, 0), FertileFarmlandBlock.FERTILITY, 3);
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BUCKET), "the bucket comes back empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void cornNeedsMaltToMash(GameTestHelper helper) {
        fast();
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CAMPFIRE);
        BlockPos kettlePos = new BlockPos(1, 2, 1);
        helper.setBlock(kettlePos, ModBlocks.BREW_KETTLE.get());
        BrewKettleBlockEntity kettle = (BrewKettleBlockEntity) helper.getBlockEntity(kettlePos);
        kettle.tank().fill(new FluidStack(Fluids.WATER, 2000), EXECUTE);
        kettle.items().setStackInSlot(0, new ItemStack(ModItems.CORNMEAL.get(), 3));
        helper.assertTrue(kettle.needsMalt(), "cornmeal alone needs malt");
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(kettle.tank().getFluid().getFluid() == Fluids.WATER, "and doesn't mash without it");
            kettle.items().setStackInSlot(1, new ItemStack(ModItems.PALE_GRIST.get(), 1));
            helper.succeedWhen(() -> {
                FluidStack wort = kettle.tank().getFluid();
                helper.assertTrue(wort.getFluid() == ModFluids.SWEET_WORT.get(), "with some malt it mashes");
                helper.assertTrue(WortData.of(wort).share(MaltType.CORN) >= 0.7F, "three parts in four corn");
            });
        });
    }

    @GameTest(template = EMPTY)
    public static void cornWortFermentsIntoCornWashMildOrWarm(GameTestHelper helper) {
        FluidStack wort = new WortData(Map.of(MaltType.CORN, 0.75F, MaltType.PALE, 0.25F), WortData.Strength.NORMAL)
                .applyTo(new FluidStack(ModFluids.SWEET_WORT.get(), 1000));
        FermentingRecipe recipe = helper.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get()).stream()
                .filter(r -> r.matches(wort, YeastType.ALE)).max(Comparator.comparingInt(FermentingRecipe::priority)).orElseThrow();
        helper.assertTrue(recipe.result() == ModFluids.CORN_WASH.get(), "mostly corn ferments into corn wash, not Plain Ale");
        helper.assertTrue(recipe.suits(Temperature.MILD) && recipe.suits(Temperature.WARM) && !recipe.suits(Temperature.COOL),
                "washes take Mild or Warm");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void spiritsDontConditionInCasks(GameTestHelper helper) {
        long twoDays = 2L * CaskBlockEntity.DAY;
        FluidStack once = CaskBlockEntity.serving(brewed(ModFluids.MALT_WHISKEY.get(), 1000, true, true, 1), twoDays, CaskWood.OAK);
        helper.assertFalse(BrewQuality.of(once).craft(), "resting doesn't give a spirit its craft star: the still does");
        FluidStack twice = CaskBlockEntity.serving(new BrewQuality(true, true, true, false).applyTo(
                new FluidStack(ModFluids.MALT_WHISKEY.get(), 1000)), twoDays, CaskWood.OAK);
        helper.assertTrue(BrewQuality.of(twice).craft(), "a double-distilled whiskey keeps its star in the cask");
        helper.succeed();
    }
}
