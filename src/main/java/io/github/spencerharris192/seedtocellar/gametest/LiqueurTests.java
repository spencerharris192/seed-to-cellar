package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFeatures;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Liqueurs and bitters steeped in the jar, and vanilla on jungle logs (GDD sections 6.1, 9.5, 10.4). */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LiqueurTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private static PreservingJarBlockEntity jar(GameTestHelper helper, FluidStack liquid, ItemStack... items) {
        ModConfigs.SERVER.fermentationTimeMultiplier.set(0.001);
        helper.setBlock(POS, ModBlocks.PRESERVING_JAR.get());
        PreservingJarBlockEntity jar = (PreservingJarBlockEntity) helper.getBlockEntity(POS);
        jar.tank().fill(liquid, IFluidHandler.FluidAction.EXECUTE);
        for (int i = 0; i < items.length; i++) jar.items().setStackInSlot(i, items[i]);
        jar.setLid(false, null);
        return jar;
    }

    private static FluidStack spirit(net.minecraft.world.level.material.Fluid fluid, int amount) {
        return new BrewQuality(true, true, true, false).applyTo(new FluidStack(fluid, amount));
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void vodkaAndLemonPeelSteepIntoLimoncello(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper, spirit(ModFluids.VODKA.get(), 1000),
                new ItemStack(ModItems.LEMON_PEEL.get(), 2), new ItemStack(ModItems.SORGHUM_SYRUP.get()));
        helper.succeedWhen(() -> {
            jar.advance();
            FluidStack out = jar.tank().getFluid();
            helper.assertTrue(out.getFluid() == ModFluids.LIMONCELLO.get() && out.getAmount() == 1000, "the whole jar becomes limoncello");
            helper.assertTrue(BrewQuality.of(out).stars() == 4, "keeping the vodka's four stars");
            helper.assertTrue(jar.items().getStackInSlot(0).is(Items.GLASS_BOTTLE), "the syrup's bottle comes back");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void rumSteepsIntoSpicedRum(GameTestHelper helper) {
        PreservingJarBlockEntity jar = jar(helper, spirit(ModFluids.RUM.get(), 500), new ItemStack(ModItems.CURED_VANILLA.get()),
                new ItemStack(ModItems.ORANGE_PEEL.get()), new ItemStack(Crops.GINGER.produce()));
        helper.succeedWhen(() -> {
            jar.advance();
            helper.assertTrue(jar.tank().getFluid().getFluid() == ModFluids.SPICED_RUM.get(), "vanilla, orange peel and ginger make spiced rum");
        });
    }

    @GameTest(template = EMPTY)
    public static void liqueursDontGoBackThroughTheStill(GameTestHelper helper) {
        helper.assertFalse(ModFluids.LIMONCELLO.get().defaultFluidState().is(ModTags.Fluids.SPIRITS), "limoncello isn't a spirit to re-run");
        helper.assertTrue(Drinks.liqueurs().contains(Drinks.AROMATIC_BITTERS) && !Drinks.spirits().contains(Drinks.SPICED_RUM),
                "bitters and spiced rum are liqueurs");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bittersCureAHangover(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.addEffect(new MobEffectInstance(ModEffects.HANGOVER.get(), 2400));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
        ItemStack bitters = DrinkItem.fromFluid(spirit(ModFluids.AROMATIC_BITTERS.get(), 250));
        bitters.finishUsingItem(helper.getLevel(), player);
        helper.assertFalse(player.hasEffect(ModEffects.HANGOVER.get()), "bitters cure a hangover");
        helper.assertFalse(player.hasEffect(MobEffects.CONFUSION), "and nausea");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void vanillaClimbsAJungleLogAndIsPicked(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.JUNGLE_LOG);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.VANILLA_POD.get()));
        BlockPos log = helper.absolutePos(POS);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(log).add(0, 0, -0.5), Direction.NORTH, log, false)));
        BlockPos vine = POS.north();
        helper.assertBlockPresent(ModBlocks.VANILLA.get(), vine);
        helper.assertBlockProperty(vine, VanillaVineBlock.FACING, Direction.SOUTH);   // facing its log
        helper.setBlock(vine, helper.getBlockState(vine).setValue(VanillaVineBlock.AGE, VanillaVineBlock.MAX_AGE));
        helper.useBlock(vine, player);
        helper.assertBlockProperty(vine, VanillaVineBlock.AGE, VanillaVineBlock.LEAFY);
        helper.assertItemEntityPresent(ModItems.VANILLA_POD.get(), vine, 2.0);
        helper.setBlock(POS, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.VANILLA.get(), vine);   // no log, no vine
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void blackForestCakeGivesSixSlices(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, ModBlocks.BLACK_FOREST_CAKE.get());
        Player player = helper.makeMockPlayer();
        for (int i = 0; i < io.github.spencerharris192.seedtocellar.food.LayerCakeBlock.SLICES; i++) {
            helper.assertBlockPresent(ModBlocks.BLACK_FOREST_CAKE.get(), POS);
            helper.useBlock(POS, player);
        }
        helper.assertBlockNotPresent(ModBlocks.BLACK_FOREST_CAKE.get(), POS);
        helper.assertTrue(player.getInventory().countItem(ModItems.BLACK_FOREST_CAKE_SLICE.get()) == 6, "six slices in all");
        helper.succeed();
    }

    @GameTest(template = "empty_tree")
    public static void wildVanillaFindsJungleTrunks(GameTestHelper helper) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                if ((x + z) % 2 == 0) for (int y = 1; y < 10; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.JUNGLE_LOG);
            }
        }
        boolean placed = ModFeatures.WILD_VANILLA.get().place(NoneFeatureConfiguration.INSTANCE, helper.getLevel(),
                helper.getLevel().getChunkSource().getGenerator(), helper.getLevel().random, helper.absolutePos(new BlockPos(3, 0, 3)));
        helper.assertTrue(placed, "vines climb the trunks around");
        helper.succeed();
    }
}
