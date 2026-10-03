package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.VesselFluidHandler;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.Intoxication;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CellarTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final IFluidHandler.FluidAction EXECUTE = IFluidHandler.FluidAction.EXECUTE;

    private static FluidStack beer(ModFluids.Entry fluid, int amount) {
        return new BrewQuality(true, true, false, false).applyTo(new FluidStack(fluid.get(), amount));
    }

    private static CaskBlockEntity cask(GameTestHelper helper, boolean tapped) {
        helper.setBlock(POS, ModBlocks.OAK_CASK.get().defaultBlockState().setValue(CaskBlock.TAP, tapped));
        return (CaskBlockEntity) helper.getBlockEntity(POS);
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void oakCaskConditionsAndAgesOldAleToFiveStars(GameTestHelper helper) {
        ModConfigs.SERVER.agingSpeedMultiplier.set(1000.0); // 1 tick = 1000 ticks of aging
        CaskBlockEntity cask = cask(helper, true);
        cask.handler().fill(beer(ModFluids.OLD_ALE, 4000), EXECUTE);
        helper.succeedWhen(() -> {
            helper.assertTrue(cask.years() >= 3, "waiting for 3 years");
            FluidStack poured = cask.handler().drain(DrinkItem.SERVING, EXECUTE);
            BrewQuality q = BrewQuality.of(poured);
            helper.assertTrue(q.craft() && q.aged() && q.stars() == 5, "old ale aged 3 years in oak should be 5 stars, was " + q.stars());
            helper.assertTrue(poured.getTag().getInt(DrinkItem.AGE) >= 3, "age should be on the drink");
        });
    }

    @GameTest(template = EMPTY)
    public static void caskWithoutTapWontPour(GameTestHelper helper) {
        CaskBlockEntity cask = cask(helper, false);
        cask.handler().fill(beer(ModFluids.PALE_ALE, 1000), EXECUTE);
        helper.assertTrue(cask.handler().drain(250, EXECUTE).isEmpty(), "no tap, no pour");
        helper.assertTrue(cask.tank().getFluidAmount() == 1000, "still full");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void toppingUpBlendsAgesByVolume(GameTestHelper helper) {
        // Works at any aging speed, so it leaves the (shared) config alone for tests running alongside.
        CaskBlockEntity cask = cask(helper, true);
        cask.handler().fill(beer(ModFluids.PALE_ALE, 1000), EXECUTE);
        helper.runAfterDelay(40, () -> {
            long before = cask.ageTicks();
            cask.handler().fill(beer(ModFluids.PALE_ALE, 1000), EXECUTE);
            long after = cask.ageTicks();
            helper.assertTrue(Math.abs(after - before / 2) <= 1, "equal volumes should halve the age: " + before + " -> " + after);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void kegConditionsButDoesNotAge(GameTestHelper helper) {
        ModConfigs.SERVER.agingSpeedMultiplier.set(1000.0);
        helper.setBlock(POS, ModBlocks.KEG.get());
        CaskBlockEntity keg = (CaskBlockEntity) helper.getBlockEntity(POS);
        keg.handler().fill(beer(ModFluids.OLD_ALE, 1000), EXECUTE);
        helper.succeedWhen(() -> {
            helper.assertTrue(keg.years() >= 3, "waiting");
            BrewQuality q = BrewQuality.of(keg.handler().drain(250, EXECUTE));
            helper.assertTrue(q.craft(), "a keg conditions");
            helper.assertFalse(q.aged(), "a keg does not age");
        });
    }

    @GameTest(template = EMPTY)
    public static void kegItemTooltipShowsStarsItWouldPour(GameTestHelper helper) {
        // A keg broken after a day's rest: base star + yeast + temperature + conditioned = 4.
        ItemStack keg = new ItemStack(ModItems.KEG.get());
        CompoundTag data = new CompoundTag();
        data.put("Tank", beer(ModFluids.PALE_ALE, 2000).writeToNBT(new CompoundTag()));
        data.putLong("AgeTicks", CaskBlockEntity.DAY);
        BlockItem.setBlockEntityData(keg, ModBlockEntities.CASK.get(), data);
        List<Component> lines = new ArrayList<>();
        keg.getItem().appendHoverText(keg, helper.getLevel(), lines, TooltipFlag.NORMAL);
        helper.assertTrue(lines.stream().anyMatch(line -> line.getString().equals("★★★★☆")),
                "keg tooltip should show 4 stars: " + lines.stream().map(Component::getString).toList());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void mugsFillAndEmptyThroughTheStandardSystem(GameTestHelper helper) {
        VesselFluidHandler mug = new VesselFluidHandler(new ItemStack(ModItems.MUG.get()));
        FluidStack serving = beer(ModFluids.STOUT, 1000);
        helper.assertTrue(mug.fill(serving, EXECUTE) == 250, "a mug takes 250 mB");
        helper.assertTrue(mug.getContainer().is(Drinks.STOUT.item().get()), "the mug becomes a stout");
        helper.assertTrue(DrinkItem.quality(mug.getContainer()).yeast(), "quality travels into the mug");
        FluidStack back = mug.drain(250, EXECUTE);
        helper.assertTrue(back.getFluid() == ModFluids.STOUT.get() && BrewQuality.of(back).yeast(), "and back out");
        helper.assertTrue(mug.getContainer().is(ModItems.MUG.get()), "leaving an empty mug");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinkingGivesEffectTipsinessAndTheMugBack(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ItemStack stout = DrinkItem.fromFluid(beer(ModFluids.STOUT, 250));
        ItemStack left = stout.getItem().finishUsingItem(stout, helper.getLevel(), player);
        helper.assertTrue(left.is(ModItems.MUG.get()), "the empty mug comes back");
        helper.assertTrue(player.hasEffect(ModEffects.WARMTH.get()), "stout warms you");
        helper.assertTrue(Intoxication.units(player) > 0, "and adds to tipsiness");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void heavyDrinkingIsDrunkAndMilkSobersYouUp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Intoxication.drink(player, 8, 3);
        helper.assertTrue(player.hasEffect(ModEffects.TIPSY.get()) && player.getEffect(ModEffects.TIPSY.get()).getAmplifier() == 2,
                "8 units is the Drunk stage (Tipsy III)");
        player.removeAllEffects(); // what drinking milk does
        helper.assertTrue(Intoxication.units(player) == 0, "milk resets the meter");
        helper.succeed();
    }

    private CellarTests() {}
}
