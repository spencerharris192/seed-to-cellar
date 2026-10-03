package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Gin: the Gin Basket on the Pot Still and its botanicals (GDD sections 9.4, 10.3, 11). */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GinTests {
    private static final String EMPTY = "empty";
    private static final BlockPos STILL = new BlockPos(1, 1, 1);

    /** A heated still with a Gin Basket fitted by a player, and a pot of vodka. */
    private static PotStillBlockEntity stillWithBasket(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);   // no fire until everything is in
        BlockState lower = ModBlocks.POT_STILL.get().defaultBlockState();
        helper.setBlock(STILL, lower);
        helper.setBlock(STILL.above(), lower.setValue(PotStillBlock.HALF, DoubleBlockHalf.UPPER));
        PotStillBlockEntity still = (PotStillBlockEntity) helper.getBlockEntity(STILL);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GIN_BASKET.get()));
        helper.assertTrue(still.useHeldItem(player, InteractionHand.MAIN_HAND), "right-clicking with a Gin Basket fits it");
        helper.assertTrue(still.hasBasket(), "the still has its basket");
        helper.assertBlockProperty(STILL.above(), PotStillBlock.BASKET, true);
        FluidStack vodka = new BrewQuality(true, true, true, false).applyTo(new FluidStack(ModFluids.VODKA.get(), 2000));
        vodka.getOrCreateTag().putInt(CraftStep.RUNS, 3);
        still.pot().fill(vodka, IFluidHandler.FluidAction.EXECUTE);
        return still;
    }

    private static void basket(PotStillBlockEntity still, Item... botanicals) {
        for (int i = 0; i < botanicals.length; i++) {
            ItemStack rest = still.items().insertItem(PotStillBlockEntity.BASKET + i, new ItemStack(botanicals[i], 2), false);
            if (!rest.isEmpty()) throw new IllegalStateException("the basket refused " + botanicals[i]);
        }
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void juniperAndTwoMoreMakeGin(GameTestHelper helper) {
        PotStillBlockEntity still = stillWithBasket(helper);
        basket(still, Crops.JUNIPER.produce(), Crops.CORIANDER.produce(), ModItems.LEMON_PEEL.get());
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.CAMPFIRE);
        helper.succeedWhen(() -> {
            FluidStack gin = still.receiver().getFluid();
            helper.assertTrue(gin.getFluid() == ModFluids.GIN.get(), "vodka through juniper and two more botanicals is gin");
            helper.assertFalse(BrewQuality.of(gin).craft(), "three botanicals: gin, but not its craft star");
            helper.assertTrue(still.items().getStackInSlot(PotStillBlockEntity.BASKET).getCount() == 1, "each run uses one of each");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void fourBotanicalsEarnTheStar(GameTestHelper helper) {
        PotStillBlockEntity still = stillWithBasket(helper);
        basket(still, Crops.JUNIPER.produce(), Crops.CORIANDER.produce(), ModItems.ORANGE_PEEL.get(), Crops.LAVENDER.produce());
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.CAMPFIRE);
        helper.succeedWhen(() -> {
            FluidStack gin = still.receiver().getFluid();
            helper.assertTrue(gin.getFluid() == ModFluids.GIN.get() && BrewQuality.of(gin).craft(), "juniper and three more: the star");
            helper.assertTrue(BrewQuality.of(gin).stars() == 4, "4 stars with the vodka's yeast and temperature");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void noJuniperNoGin(GameTestHelper helper) {
        PotStillBlockEntity still = stillWithBasket(helper);
        basket(still, Crops.CORIANDER.produce(), ModItems.ORANGE_PEEL.get(), Crops.MINT.produce());
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.CAMPFIRE);
        helper.succeedWhen(() -> {
            helper.assertTrue(still.receiver().getFluid().getFluid() == ModFluids.VODKA.get(), "without juniper it's just vodka again");
            helper.assertTrue(still.items().getStackInSlot(PotStillBlockEntity.BASKET).getCount() == 2, "and the botanicals aren't used");
        });
    }

    @GameTest(template = EMPTY)
    public static void basketTakesOnlyBotanicals(GameTestHelper helper) {
        PotStillBlockEntity still = stillWithBasket(helper);
        ItemStack rest = still.items().insertItem(PotStillBlockEntity.BASKET, new ItemStack(net.minecraft.world.item.Items.DIRT), false);
        helper.assertFalse(rest.isEmpty(), "dirt isn't a botanical");
        helper.succeed();
    }
}
