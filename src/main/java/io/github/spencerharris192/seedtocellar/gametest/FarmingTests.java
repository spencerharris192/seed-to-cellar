package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.HopsBlock;
import io.github.spencerharris192.seedtocellar.farming.PaddyCropBlock;
import io.github.spencerharris192.seedtocellar.farming.TallCropBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisVineBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FarmingTests {
    private static final String EMPTY = "empty";
    private static final BlockPos GROUND = new BlockPos(1, 1, 1);
    private static final BlockPos PLANT = GROUND.above();

    @GameTest(template = EMPTY)
    public static void everyRipeCropDropsItsCropAndSeeds(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        for (Crop crop : Crops.all()) {
            helper.setBlock(PLANT, ripe(crop));
            helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true); // helper.destroyBlock skips drops
            helper.assertItemEntityPresent(crop.produce(), PLANT, 2.0);
            helper.assertItemEntityPresent(crop.seeds(), PLANT, 2.0);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyRipeCropRightClickHarvestsAndReplants(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        Player player = helper.makeMockPlayer();
        for (Crop crop : Crops.all()) {
            helper.setBlock(PLANT, ripe(crop));
            helper.useBlock(PLANT, player);
            helper.assertBlockPresent(crop.block(), PLANT);
            // Field crops replant at age 0; bushes and herbs drop back to young (1), vines to leafy (3), and regrow.
            if (crop.isPerennial()) helper.assertBlockProperty(PLANT, BushCropBlock.AGE, BushCropBlock.PICKED_AGE);
            else if (crop.style == Crop.Style.VINE) helper.assertBlockProperty(PLANT, TrellisVineBlock.AGE, TrellisVineBlock.LEAFY);
            else helper.assertBlockProperty(PLANT, CropBlock.AGE, 0);
            helper.assertItemEntityPresent(crop.produce(), PLANT, 2.0);
        }
        // Not ripe yet: a right-click does nothing.
        helper.setBlock(PLANT, Crops.RYE.block().defaultBlockState().setValue(CropBlock.AGE, 3));
        helper.useBlock(PLANT, player);
        helper.assertBlockProperty(PLANT, CropBlock.AGE, 3);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyWildPlantGivesSeeds(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.GRASS_BLOCK);
        for (Crop crop : Crops.all()) {
            if (!crop.hasWild()) continue;
            helper.setBlock(PLANT, crop.wildBlock());
            helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true);
            helper.assertItemEntityPresent(crop.seeds(), PLANT, 2.0);
        }
        helper.succeed();
    }

    // --- corn (two blocks tall) ---

    @GameTest(template = EMPTY)
    public static void cornGrowsIntoTwoBlocks(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, Crops.CORN.block());
        grow(helper, PLANT);
        helper.assertBlockProperty(PLANT, CropBlock.AGE, CropBlock.MAX_AGE);
        helper.assertBlockPresent(Crops.CORN.block(), PLANT.above());
        helper.assertBlockProperty(PLANT.above(), TallCropBlock.HALF, DoubleBlockHalf.UPPER);
        helper.assertBlockProperty(PLANT.above(), CropBlock.AGE, CropBlock.MAX_AGE);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void cornStopsBelowAnObstacle(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, Crops.CORN.block());
        helper.setBlock(PLANT.above(), Blocks.STONE);
        grow(helper, PLANT);
        helper.assertBlockProperty(PLANT, CropBlock.AGE, TallCropBlock.UPPER_FROM - 1);
        helper.assertBlockPresent(Blocks.STONE, PLANT.above());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void cornBreaksAsOnePlantAndDropsOnce(GameTestHelper helper) {
        helper.setBlock(GROUND.below(), Blocks.STONE);
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, Crops.CORN.block());
        grow(helper, PLANT);
        helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true);
        helper.assertBlockNotPresent(Crops.CORN.block(), PLANT.above());
        AABB area = new AABB(helper.absolutePos(PLANT)).inflate(3);
        int ears = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(Crops.CORN.produce()))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
        helper.assertTrue(ears == 2, "ripe corn should drop 2 ears once, got " + ears);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void cornHarvestsFromTheTopHalf(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.setBlock(PLANT, Crops.CORN.block());
        grow(helper, PLANT);
        helper.useBlock(PLANT.above(), helper.makeMockPlayer());
        helper.assertBlockProperty(PLANT, CropBlock.AGE, 0);
        helper.assertBlockNotPresent(Crops.CORN.block(), PLANT.above());
        helper.assertItemEntityPresent(Crops.CORN.produce(), PLANT, 2.0);
        helper.succeed();
    }

    // --- rice (paddy) ---

    @GameTest(template = EMPTY)
    public static void ricePlantsOnlyInShallowWater(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Crops.RICE.seeds(), 4));
        // Dry farmland: no.
        helper.setBlock(GROUND, Blocks.FARMLAND);
        useOnTop(helper, player, GROUND);
        helper.assertBlockNotPresent(Crops.RICE.block(), PLANT);
        // Water one block deep over dirt: yes, and the block holds the water.
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, Blocks.WATER);
        useOnTop(helper, player, GROUND);
        helper.assertBlockPresent(Crops.RICE.block(), PLANT);
        helper.assertBlockProperty(PLANT, PaddyCropBlock.WATERLOGGED, true);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void riceHarvestKeepsTheWater(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, ripe(Crops.RICE));
        helper.useBlock(PLANT, helper.makeMockPlayer());
        helper.assertBlockProperty(PLANT, CropBlock.AGE, 0);
        helper.assertBlockProperty(PLANT, PaddyCropBlock.WATERLOGGED, true);
        helper.assertItemEntityPresent(Crops.RICE.produce(), PLANT, 2.0);
        // Broken, the rice leaves its water behind.
        helper.getLevel().destroyBlock(helper.absolutePos(PLANT), true);
        helper.assertBlockPresent(Blocks.WATER, PLANT);
        helper.succeed();
    }

    // --- bushes and herbs ---

    @GameTest(template = EMPTY)
    public static void elderberryFlowersCanBePickedInsteadOfBerries(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.GRASS_BLOCK);
        helper.setBlock(PLANT, Crops.ELDERBERRY.block().defaultBlockState().setValue(BushCropBlock.AGE, 2));
        helper.useBlock(PLANT, helper.makeMockPlayer());
        helper.assertItemEntityPresent(Crops.ELDERBERRY.flowers(), PLANT, 2.0);
        helper.assertBlockProperty(PLANT, BushCropBlock.AGE, BushCropBlock.PICKED_AGE);   // no berries this time
        // Other bushes have nothing to pick while flowering.
        helper.setBlock(PLANT, Crops.BLUEBERRY.block().defaultBlockState().setValue(BushCropBlock.AGE, 2));
        helper.useBlock(PLANT, helper.makeMockPlayer());
        helper.assertBlockProperty(PLANT, BushCropBlock.AGE, 2);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void cranberriesNeedWaterBeside(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        BlockState bush = Crops.CRANBERRY.block().defaultBlockState();
        helper.assertFalse(bush.canSurvive(helper.getLevel(), helper.absolutePos(PLANT)), "no water nearby: no cranberries");
        helper.setBlock(GROUND.east(), Blocks.WATER);
        helper.assertTrue(bush.canSurvive(helper.getLevel(), helper.absolutePos(PLANT)), "water beside the soil: fine");
        helper.succeed();
    }

    /** Bone meal until the crop stops growing. */
    private static void grow(GameTestHelper helper, BlockPos pos) {
        BlockPos abs = helper.absolutePos(pos);
        for (int i = 0; i < 20; i++) {
            BlockState state = helper.getLevel().getBlockState(abs);
            if (!(state.getBlock() instanceof CropBlock crop) || !crop.isValidBonemealTarget(helper.getLevel(), abs, state, false)) return;
            crop.performBonemeal(helper.getLevel(), helper.getLevel().random, abs, state);
        }
    }

    /** Uses the held item on the top face of `relative`, as a player clicking the ground would. */
    private static void useOnTop(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static BlockState ripe(Crop crop) {
        if (crop.style == Crop.Style.VINE) return crop.block().defaultBlockState().setValue(TrellisVineBlock.AGE, TrellisVineBlock.MAX_AGE);
        return crop.isPerennial() ? ((BushCropBlock) crop.block()).ripe()
                : crop.block().defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE);
    }

    // --- grapes (trellis vines planted from cuttings) ---

    @GameTest(template = EMPTY)
    public static void grapeCuttingPlantsOnTrellisAndShearsCutLeaves(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, ModBlocks.TRELLIS.get());
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Crops.RED_GRAPE.seeds()));
        useOn(helper, player, PLANT);
        helper.assertBlockPresent(Crops.RED_GRAPE.block(), PLANT);
        helper.assertBlockProperty(PLANT, TrellisVineBlock.ROOT, true);

        // Shears on a young vine do nothing; on a leafy one they cut grape leaves and set it back to young.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        helper.useBlock(PLANT, player);
        helper.assertBlockProperty(PLANT, TrellisVineBlock.AGE, 0);
        helper.setBlock(PLANT, helper.getBlockState(PLANT).setValue(TrellisVineBlock.AGE, TrellisVineBlock.LEAFY));
        helper.useBlock(PLANT, player);
        helper.assertBlockProperty(PLANT, TrellisVineBlock.AGE, TrellisVineBlock.YOUNG);
        helper.assertItemEntityPresent(ModItems.GRAPE_LEAVES.get(), PLANT, 2.0);
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "pruning wears the shears");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grapeVineOnlyClimbsItsOwnKind(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, Crops.WHITE_GRAPE.block().defaultBlockState());
        BlockState redOnWhite = Crops.RED_GRAPE.block().defaultBlockState().setValue(TrellisVineBlock.ROOT, false);
        helper.assertFalse(redOnWhite.canSurvive(helper.getLevel(), helper.absolutePos(PLANT.above())), "red can't grow on white");
        BlockState whiteOnWhite = Crops.WHITE_GRAPE.block().defaultBlockState().setValue(TrellisVineBlock.ROOT, false);
        helper.assertTrue(whiteOnWhite.canSurvive(helper.getLevel(), helper.absolutePos(PLANT.above())), "white climbs white");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rhizomePlantsOnBottomTrellisOnly(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, ModBlocks.TRELLIS.get());
        helper.setBlock(PLANT.above(), ModBlocks.TRELLIS.get());
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HOP_RHIZOME.get(), 2));

        useOn(helper, player, PLANT.above());   // not on soil: should do nothing
        helper.assertBlockPresent(ModBlocks.TRELLIS.get(), PLANT.above());
        useOn(helper, player, PLANT);           // on soil: becomes a young hop bine
        helper.assertBlockPresent(ModBlocks.HOPS.get(), PLANT);
        helper.assertBlockProperty(PLANT, HopsBlock.ROOT, true);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "one rhizome should be used");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ripeHopsHarvestBackToLeafy(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, hops(HopsBlock.MAX_AGE, true));
        helper.useBlock(PLANT);
        helper.assertBlockProperty(PLANT, HopsBlock.AGE, HopsBlock.LEAFY);
        helper.assertItemEntityPresent(ModItems.HOP_CONES.get(), PLANT, 2.0);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void leafyHopsClimbIntoTrellisAbove(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, hops(HopsBlock.LEAFY, true));
        helper.setBlock(PLANT.above(), ModBlocks.TRELLIS.get());
        BlockState state = helper.getBlockState(PLANT);
        ((HopsBlock) state.getBlock()).performBonemeal(helper.getLevel(), helper.getLevel().random, helper.absolutePos(PLANT), state);
        helper.assertBlockPresent(ModBlocks.HOPS.get(), PLANT.above());
        helper.assertBlockProperty(PLANT.above(), HopsBlock.ROOT, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hopsWitherToTrellisWithoutSoil(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, hops(HopsBlock.LEAFY, true));
        helper.setBlock(PLANT.above(), hops(0, false));
        helper.setBlock(GROUND, Blocks.STONE); // still holds the trellis up, but nothing grows in it
        helper.assertBlockPresent(ModBlocks.TRELLIS.get(), PLANT);
        helper.assertBlockPresent(ModBlocks.TRELLIS.get(), PLANT.above());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void trellisColumnCollapsesWithoutSupport(GameTestHelper helper) {
        helper.setBlock(GROUND.below(), Blocks.STONE); // a floor to catch the drops
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, hops(HopsBlock.MAX_AGE, true));
        helper.setBlock(PLANT.above(), hops(0, false));
        helper.setBlock(PLANT.above(2), ModBlocks.TRELLIS.get().defaultBlockState().setValue(TrellisBlock.AXIS, Direction.Axis.X));
        helper.setBlock(GROUND, Blocks.AIR);
        helper.succeedWhen(() -> {
            for (int dy = 0; dy < 3; dy++) helper.assertBlockPresent(Blocks.AIR, PLANT.above(dy));
            AABB area = new AABB(helper.absolutePos(PLANT)).inflate(3);
            int trellises = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(ModItems.TRELLIS.get()))
                    .stream().mapToInt(e -> e.getItem().getCount()).sum();
            helper.assertTrue(trellises == 3, "every piece should drop its trellis, got " + trellises);
            helper.assertItemEntityPresent(ModItems.HOP_RHIZOME.get(), PLANT, 3.0);
            helper.assertItemEntityPresent(ModItems.HOP_CONES.get(), PLANT, 3.0);
        });
    }

    @GameTest(template = EMPTY)
    public static void trellisNeedsSomethingToStandOn(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.STONE);
        BlockState trellis = ModBlocks.TRELLIS.get().defaultBlockState();
        helper.assertTrue(trellis.canSurvive(helper.getLevel(), helper.absolutePos(PLANT)), "stands on stone");
        helper.assertFalse(trellis.canSurvive(helper.getLevel(), helper.absolutePos(PLANT.above())), "can't float in the air");
        helper.setBlock(GROUND, Blocks.FARMLAND);
        helper.assertTrue(trellis.canSurvive(helper.getLevel(), helper.absolutePos(PLANT)), "stands on farmland (for hops)");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void trellisStacksOnTopOfColumnLikeScaffolding(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(PLANT, hops(HopsBlock.MAX_AGE, true).setValue(TrellisBlock.AXIS, Direction.Axis.Z));
        helper.setBlock(PLANT.above(), ModBlocks.TRELLIS.get().defaultBlockState().setValue(TrellisBlock.AXIS, Direction.Axis.Z));
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.TRELLIS.get(), 4));

        // Holding a trellis, ripe hops don't get picked: the click goes to stacking instead.
        BlockPos abs = helper.absolutePos(PLANT);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
        helper.assertTrue(helper.getBlockState(PLANT).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit) == InteractionResult.PASS,
                "ripe hops should let a held trellis through");
        useOn(helper, player, PLANT); // the side of the bottom (hops) block
        helper.assertBlockPresent(ModBlocks.TRELLIS.get(), PLANT.above(2));
        helper.assertBlockProperty(PLANT.above(2), TrellisBlock.AXIS, Direction.Axis.Z); // lines up with the column
        helper.assertBlockProperty(PLANT, HopsBlock.AGE, HopsBlock.MAX_AGE);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sneakingPlacesTrellisBesideInstead(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.DIRT);
        helper.setBlock(GROUND.north(), Blocks.DIRT);
        helper.setBlock(PLANT, ModBlocks.TRELLIS.get());
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.TRELLIS.get(), 4));
        player.setShiftKeyDown(true);
        useOn(helper, player, PLANT); // clicks the north face
        helper.assertBlockPresent(ModBlocks.TRELLIS.get(), PLANT.north());
        helper.assertBlockNotPresent(ModBlocks.TRELLIS.get(), PLANT.above());
        helper.succeed();
    }

    private static BlockState hops(int age, boolean root) {
        return ModBlocks.HOPS.get().defaultBlockState()
                .setValue(TrellisBlock.AXIS, Direction.Axis.X).setValue(HopsBlock.AGE, age).setValue(HopsBlock.ROOT, root);
    }

    private static void useOn(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private FarmingTests() {}
}
