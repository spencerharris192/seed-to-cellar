package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.ClimateRules;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlock;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

/** Compost Bin, Compost, Fertile Farmland and climates (GDD section 6.5). */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SoilTests {
    private static final String EMPTY = "empty";
    private static final BlockPos GROUND = new BlockPos(1, 1, 1);
    private static final BlockPos PLANT = GROUND.above();

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void compostBinFillsThenTurnsLeftoversIntoCompost(GameTestHelper helper) {
        ModConfigs.SERVER.processTimeMultiplier.set(0.01);   // a day becomes 240 ticks
        helper.setBlock(GROUND, ModBlocks.COMPOST_BIN.get());
        CompostBinBlockEntity bin = (CompostBinBlockEntity) helper.getBlockEntity(GROUND);
        IItemHandler items = bin.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);

        helper.assertTrue(items.insertItem(0, new ItemStack(Items.STONE), false).getCount() == 1, "stone isn't compostable");
        helper.assertTrue(items.insertItem(0, new ItemStack(Items.WHEAT_SEEDS, 10), false).isEmpty(), "takes seeds");
        helper.assertBlockProperty(GROUND, CompostBinBlock.LEVEL, 3);
        ItemStack left = items.insertItem(0, new ItemStack(ModItems.SPENT_GRAIN.get(), 10), false);
        helper.assertTrue(left.getCount() == 4, "only 16 fit, so 4 of 10 spent grain should be left over");
        helper.assertTrue(bin.isComposting(), "a full bin starts composting");
        helper.assertTrue(items.insertItem(0, new ItemStack(Items.WHEAT_SEEDS), false).getCount() == 1, "no more while composting");
        helper.assertTrue(items.extractItem(1, 64, true).isEmpty(), "no compost before it's done");

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(GROUND, CompostBinBlock.READY, true);
            Player player = helper.makeMockPlayer();
            helper.useBlock(GROUND, player);
            helper.assertTrue(player.getInventory().countItem(ModItems.COMPOST.get()) == CompostBinBlockEntity.YIELD,
                    "right-clicking a ready bin gives 4 compost");
            helper.assertBlockProperty(GROUND, CompostBinBlock.READY, false);
            helper.assertBlockProperty(GROUND, CompostBinBlock.LEVEL, 0);
        });
    }

    @GameTest(template = EMPTY)
    public static void compostMakesFarmlandFertileAndTopsItUp(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COMPOST.get(), 3));

        helper.setBlock(GROUND, Blocks.DIRT);
        useOnTop(helper, player, GROUND);
        helper.assertBlockPresent(Blocks.DIRT, GROUND);
        helper.assertTrue(player.getMainHandItem().getCount() == 3, "nothing used on plain dirt");

        helper.setBlock(GROUND, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        useOnTop(helper, player, GROUND);
        helper.assertBlockPresent(ModBlocks.FERTILE_FARMLAND.get(), GROUND);
        helper.assertBlockProperty(GROUND, FertileFarmlandBlock.FERTILITY, 3);
        helper.assertBlockProperty(GROUND, FarmBlock.MOISTURE, 7);   // stays wet
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "one compost used");

        useOnTop(helper, player, GROUND);   // already full
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "nothing used on full fertile soil");

        // Used on a crop, it goes into the soil underneath.
        helper.setBlock(GROUND, ModBlocks.FERTILE_FARMLAND.get().defaultBlockState().setValue(FertileFarmlandBlock.FERTILITY, 1));
        helper.setBlock(PLANT, Blocks.WHEAT);
        useOnTop(helper, player, PLANT);
        helper.assertBlockProperty(GROUND, FertileFarmlandBlock.FERTILITY, 3);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "one compost used on the crop's soil");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fertileSoilGivesExtraHarvestsUntilUsedUp(GameTestHelper helper) {
        // Extra crops: 40 ripe wheat harvests, the soil topped up each time, should give well over 40 wheat.
        int harvests = 40;
        for (int i = 0; i < harvests; i++) {
            helper.setBlock(GROUND, ModBlocks.FERTILE_FARMLAND.get());
            harvestRipeWheat(helper);
        }
        int wheat = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(PLANT)).inflate(3)).stream()
                .filter(e -> e.getItem().is(Items.WHEAT)).mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(wheat > harvests + 5, "fertile soil should add extra wheat (got " + wheat + " from " + harvests + ")");

        // Used up: harvesting without topping up ends as ordinary (still wet) farmland.
        helper.setBlock(GROUND, ModBlocks.FERTILE_FARMLAND.get().defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        for (int i = 0; i < 100 && helper.getBlockState(GROUND).is(ModBlocks.FERTILE_FARMLAND.get()); i++) harvestRipeWheat(helper);
        helper.assertBlockPresent(Blocks.FARMLAND, GROUND);
        helper.assertBlockProperty(GROUND, FarmBlock.MOISTURE, 7);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void dispenserSpreadsCompost(GameTestHelper helper) {
        BlockPos dispenser = GROUND;
        BlockPos soil = GROUND.east();
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        ((DispenserBlockEntity) helper.getBlockEntity(dispenser)).setItem(0, new ItemStack(ModItems.COMPOST.get()));
        helper.setBlock(soil, Blocks.FARMLAND);
        helper.setBlock(dispenser.west(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(ModBlocks.FERTILE_FARMLAND.get(), soil);
            helper.assertTrue(((DispenserBlockEntity) helper.getBlockEntity(dispenser)).isEmpty(), "the compost was used");
        });
    }

    @GameTest(template = EMPTY)
    public static void glassAboveMakesAWarmGreenhouse(GameTestHelper helper) {
        BlockPos crop = helper.absolutePos(PLANT);
        helper.setBlock(PLANT.above(5), Blocks.GLASS);
        helper.assertTrue(ClimateRules.at(helper.getLevel(), crop) == Climate.WARM, "glass 5 above should count as warm");
        helper.assertTrue(ClimateRules.suits(Climate.WARM, helper.getLevel(), crop), "a warm crop likes the greenhouse");

        // Out of its climate a crop grows at about half speed; in it, always at full speed.
        int cold = 0;
        int warm = 0;
        for (int i = 0; i < 1000; i++) {
            cold += ClimateRules.growthAttempts(Climate.COLD, helper.getLevel(), crop, helper.getLevel().random);
            warm += ClimateRules.growthAttempts(Climate.WARM, helper.getLevel(), crop, helper.getLevel().random);
        }
        helper.assertTrue(warm == 1000, "in its climate: one growth attempt every time (got " + warm + ")");
        if (ClimateRules.active()) {
            helper.assertTrue(cold > 350 && cold < 650, "out of its climate: about half (got " + cold + ")");
        } else {   // (Serene Seasons installed: its seasons take over from climate preference)
            helper.assertTrue(cold == 1000, "climate preference off: full speed everywhere (got " + cold + ")");
        }

        helper.setBlock(PLANT.above(5), Blocks.AIR);
        helper.setBlock(PLANT.above(ClimateRules.GREENHOUSE_HEIGHT + 1), Blocks.GLASS);
        helper.assertFalse(ClimateRules.underGlass(helper.getLevel(), crop), "glass too high up doesn't count");
        helper.succeed();
    }

    private static void harvestRipeWheat(GameTestHelper helper) {
        helper.setBlock(PLANT, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE));
        helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true);
    }

    /** Uses the held item on the top face of `relative`, as a player clicking it would. */
    private static void useOnTop(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private SoilTests() {}
}
