package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.PaddyCropBlock;
import io.github.spencerharris192.seedtocellar.farming.TallCropBlock;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Sickles (GDD section 6.6) and straw. */
public final class SickleTests {
    private static final String EMPTY = "empty";
    private static final BlockPos CENTER = new BlockPos(1, 2, 1);   // plants at y=2, soil at y=1 (template is 3x3x3)

    @GameTest(template = EMPTY)
    public static void sickleHarvestsRipeCropsAroundAndLeavesUnripe(GameTestHelper helper) {
        // Ripe wheat on the corners, unripe in the middle (where the sickle is aimed), one ripe just outside.
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
        }
        BlockPos[] corners = {new BlockPos(0, 2, 0), new BlockPos(2, 2, 0), new BlockPos(0, 2, 2), new BlockPos(2, 2, 2)};
        for (BlockPos corner : corners) place(helper, corner, ripe(Blocks.WHEAT));
        place(helper, CENTER, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 3));
        BlockPos outside = new BlockPos(3, 2, 1);   // one step beyond the 3x3
        helper.setBlock(outside.below(), Blocks.FARMLAND);
        place(helper, outside, ripe(Blocks.WHEAT));

        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.IRON_SICKLE.get()));
        helper.assertTrue(swing(helper, player, CENTER).consumesAction(), "the sickle should act on the ripe crops around");
        for (BlockPos corner : corners) helper.assertBlockProperty(corner, CropBlock.AGE, 0);   // harvested and replanted
        helper.assertBlockProperty(CENTER, CropBlock.AGE, 3);
        helper.assertBlockProperty(outside, CropBlock.AGE, CropBlock.MAX_AGE);
        helper.assertTrue(count(helper, Items.WHEAT) == 4, "one wheat from each of the 4 ripe crops");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "one point of durability per swing");

        // Nothing ripe left in reach: the sickle does nothing (and isn't worn).
        helper.assertFalse(swing(helper, player, CENTER).consumesAction(), "nothing ripe to harvest");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "no wear without a harvest");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sickleHandlesCornRiceAndBushes(GameTestHelper helper) {
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
        }
        BlockPos corn = new BlockPos(0, 2, 1);
        BlockState ripeCorn = ripe(Crops.CORN.block());
        place(helper, corn, ripeCorn);
        place(helper, corn.above(), ripeCorn.setValue(TallCropBlock.HALF, DoubleBlockHalf.UPPER));
        BlockPos rice = new BlockPos(2, 2, 1);
        helper.setBlock(rice.below(), Blocks.DIRT);
        place(helper, rice, ripe(Crops.RICE.block()));
        BlockPos bush = new BlockPos(1, 2, 0);
        place(helper, bush, ((BushCropBlock) Crops.BLUEBERRY.block()).ripe());

        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.WOODEN_SICKLE.get()));
        swing(helper, player, corn.above());   // aimed at the top of the corn: the 3x3 is at that height...
        helper.assertBlockProperty(corn, CropBlock.AGE, 0);   // ...and still harvests the corn itself
        swing(helper, player, CENTER.below());   // aimed at the soil: works on the plants above it
        helper.assertBlockProperty(rice, CropBlock.AGE, 0);
        helper.assertBlockProperty(rice, PaddyCropBlock.WATERLOGGED, true);
        helper.assertBlockProperty(bush, BushCropBlock.AGE, BushCropBlock.PICKED_AGE);
        helper.assertTrue(count(helper, Crops.CORN.produce()) == 2, "ripe corn gives 2 ears");
        helper.assertTrue(count(helper, Crops.BLUEBERRY.produce()) >= 2, "a ripe bush gives 2-3 berries");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void grainCutWithASickleGivesStraw(GameTestHelper helper) {
        helper.setBlock(CENTER.below(), Blocks.FARMLAND);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);

        // By hand: no straw.
        helper.setBlock(CENTER, ripe(Crops.BARLEY.block()));
        helper.useBlock(CENTER, player);   // right-click harvest
        helper.assertTrue(count(helper, ModItems.STRAW.get()) == 0, "no straw without a sickle");

        // With a sickle: barley gives straw; carrots don't.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.STONE_SICKLE.get()));
        helper.setBlock(CENTER, ripe(Crops.BARLEY.block()));
        swing(helper, player, CENTER);
        helper.assertTrue(count(helper, ModItems.STRAW.get()) == 1, "one straw from ripe barley");
        helper.setBlock(CENTER, ripe(Blocks.CARROTS));
        swing(helper, player, CENTER);
        helper.assertTrue(count(helper, ModItems.STRAW.get()) == 1, "carrots aren't grain");
        helper.succeed();
    }

    /**
     * Places a plant without updating its neighbors. Test areas have no light worked out, so a crop that gets
     * a neighbor update (a harvest next to it, say) thinks it's too dark to live and pops off; a real world is
     * lit. That's also why the test fields never put a harvested crop right beside another crop.
     */
    private static void place(GameTestHelper helper, BlockPos pos, BlockState state) {
        helper.getLevel().setBlock(helper.absolutePos(pos), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private static BlockState ripe(Block block) {
        CropBlock crop = (CropBlock) block;
        return crop.getStateForAge(crop.getMaxAge());
    }

    /** Right-clicks the top of `relative` with the held sickle, as a player would. */
    private static InteractionResult swing(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        return player.getMainHandItem().onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    /** How many of an item lie on the ground around the test area. */
    private static int count(GameTestHelper helper, Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(CENTER)).inflate(4)).stream()
                .filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }

    private SickleTests() {}
}
