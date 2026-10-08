package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.fluids.FluidStack;

/** Apple Crown Whiskey, and its secret: the only six-star drink. */
public final class AppleCrownTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final BrewQuality PERFECT = new BrewQuality(true, true, true, true);
    private static final BrewQuality FOUR_STARS = new BrewQuality(true, true, true, false);

    private static PreservingJarBlockEntity steeping(GameTestHelper helper, BrewQuality whiskey, ItemStack... items) {
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
        helper.setBlock(POS, ModBlocks.PRESERVING_JAR.get());
        PreservingJarBlockEntity jar = (PreservingJarBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        jar.tank().fill(whiskey.applyTo(new FluidStack(ModFluids.MALT_WHISKEY.get(), 1000)), StationTank.Action.EXECUTE);
        for (int i = 0; i < items.length; i++) jar.items().setStackInSlot(i, items[i]);
        jar.setLid(false, null);
        return jar;
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void applesAndHoneySteepMaltWhiskey(GameTestHelper helper) {
        PreservingJarBlockEntity jar = steeping(helper, FOUR_STARS, new ItemStack(Items.APPLE, 2), new ItemStack(Items.HONEY_BOTTLE));
        helper.succeedWhen(() -> {
            jar.advance();
            FluidStack out = jar.tank().getFluid();
            helper.assertTrue(out.getFluid() == ModFluids.APPLE_CROWN_WHISKEY.get(), "malt whiskey, apples and honey make Apple Crown Whiskey");
            helper.assertTrue(BrewQuality.of(out).stars() == 4 && !BrewQuality.of(out).crowned(), "keeping the whiskey's stars");
            helper.assertTrue(jar.items().getStackInSlot(0).is(Items.GLASS_BOTTLE), "the honey's bottle comes back");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void aGoldenAppleCrownsAPerfectWhiskey(GameTestHelper helper) {
        PreservingJarBlockEntity jar = steeping(helper, PERFECT, new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.HONEY_BOTTLE));
        helper.succeedWhen(() -> {
            jar.advance();
            FluidStack out = jar.tank().getFluid();
            helper.assertTrue(out.getFluid() == ModFluids.APPLE_CROWN_WHISKEY.get(), "a golden apple steeps it too");
            helper.assertTrue(BrewQuality.of(out).crowned() && BrewQuality.of(out).stars() == 6, "and crowns a perfect whiskey: six stars");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void onlyAPerfectWhiskeyWearsTheCrown(GameTestHelper helper) {
        PreservingJarBlockEntity jar = steeping(helper, FOUR_STARS, new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.HONEY_BOTTLE));
        helper.succeedWhen(() -> {
            jar.advance();
            FluidStack out = jar.tank().getFluid();
            helper.assertTrue(out.getFluid() == ModFluids.APPLE_CROWN_WHISKEY.get(), "a four-star whiskey still steeps");
            helper.assertTrue(!BrewQuality.of(out).crowned() && BrewQuality.of(out).stars() == 4, "but isn't crowned");
        });
    }

    @GameTest(template = EMPTY)
    public static void theCrownIsASecretAndAShimmer(GameTestHelper helper) {
        var jars = io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(helper.getLevel(), ModRecipes.JAR.get()).toList();
        helper.assertTrue(jars.stream().filter(JarRecipe::crowns).count() == 1, "one recipe crowns (JEI hides it)");
        helper.assertFalse(PERFECT.save().contains("Crowned"), "uncrowned drinks' data is unchanged, so old bottles still stack");
        helper.assertTrue(DrinkItem.stars(6).getString().chars().filter(c -> c == '★').count() == 6, "six stars show");
        helper.assertTrue(DrinkItem.stars(5).getString().equals("★★★★★"), "five stars as ever");

        ItemStack crowned = DrinkItem.fromFluid(PERFECT.withCrowned(true).applyTo(new FluidStack(ModFluids.APPLE_CROWN_WHISKEY.get(), 250)));
        helper.assertTrue(crowned.hasFoil() && crowned.getRarity() == Rarity.EPIC, "a crowned bottle shimmers, its name in purple");
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        crowned.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(MobEffects.ABSORPTION) && player.hasEffect(MobEffects.REGENERATION),
                "it drinks like a golden apple");
        helper.succeed();
    }
}
