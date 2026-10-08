package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitSaplingBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Fruit trees: saplings grow into their trees, and the leaves fruit, are picked, and drop ripe fruit. */
public final class OrchardTests {
    private static final String EMPTY = "empty";
    private static final BlockPos TRUNK = new BlockPos(1, 1, 1);
    private static final BlockPos LEAVES = TRUNK.above();

    /** Leaves on a tree (not placed by a player), next to its trunk, at a given stage. */
    private static BlockState treeLeaves(FruitTree tree, int age) {
        return tree.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, 1)
                .setValue(FruitLeavesBlock.AGE, age);
    }

    @GameTest(template = EMPTY)
    public static void ripeLeavesArePickedBackToPlain(GameTestHelper helper) {
        for (FruitTree tree : FruitTrees.all()) {
            helper.setBlock(TRUNK, tree.log.get());
            helper.setBlock(LEAVES, treeLeaves(tree, FruitLeavesBlock.RIPE));
            helper.useBlock(LEAVES);
            helper.assertBlockProperty(LEAVES, FruitLeavesBlock.AGE, FruitLeavesBlock.PLAIN);
            helper.assertItemEntityPresent(tree.fruit(), LEAVES, 2.0);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ripeFruitFallsOnlyWithAirBelow(GameTestHelper helper) {
        FruitTree tree = FruitTrees.PEAR;
        BlockPos high = LEAVES.above();
        helper.setBlock(TRUNK, tree.log.get());
        helper.setBlock(high, treeLeaves(tree, FruitLeavesBlock.RIPE));
        helper.setBlock(LEAVES, Blocks.STONE);
        FruitLeavesBlock block = (FruitLeavesBlock) tree.leaves();
        ServerLevel level = helper.getLevel();
        helper.assertFalse(block.fall(level, helper.absolutePos(high), helper.getBlockState(high)), "no room under it: it stays on the tree");
        helper.setBlock(LEAVES, Blocks.AIR);
        helper.assertTrue(block.fall(level, helper.absolutePos(high), helper.getBlockState(high)), "air below: the fruit falls");
        helper.assertBlockProperty(high, FruitLeavesBlock.AGE, FruitLeavesBlock.PLAIN);
        helper.assertItemEntityPresent(tree.fruit(), LEAVES, 2.0);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void placedLeavesAreOnlyDecoration(GameTestHelper helper) {
        FruitTree tree = FruitTrees.CHERRY;
        BlockState placed = tree.leaves().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        helper.assertFalse(placed.isRandomlyTicking(), "leaves a player placed don't grow fruit");
        helper.assertFalse(((FruitLeavesBlock) tree.leaves()).isValidBonemealTarget(helper.getLevel(), helper.absolutePos(LEAVES), placed, net.minecraft.world.level.block.BonemealSource.INTERACTION),
                "and bone meal does nothing on them");
        helper.assertTrue(treeLeaves(tree, FruitLeavesBlock.PLAIN).isRandomlyTicking(), "leaves on a tree do grow");
        helper.succeed();
    }

    // --- one test per tree: its sapling grows into it ---

    @GameTestGenerator
    public static List<GameTestGenerator.Case> saplingsGrowIntoTheirTrees() {
        List<GameTestGenerator.Case> tests = new ArrayList<>();
        for (FruitTree tree : FruitTrees.all()) {
            tests.add(new GameTestGenerator.Case("sapling_grows_into_" + tree.name + "_tree", "empty_tree", 100, helper -> saplingGrows(helper, tree)));
        }
        return tests;
    }

    private static void saplingGrows(GameTestHelper helper, FruitTree tree) {
        BlockPos ground = new BlockPos(3, 1, 3);
        BlockPos plant = ground.above();
        helper.setBlock(ground, Blocks.GRASS_BLOCK);
        helper.setBlock(plant, tree.sapling());
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(plant);
        FruitSaplingBlock sapling = (FruitSaplingBlock) tree.sapling();
        for (int i = 0; i < 2 && level.getBlockState(abs).is(tree.sapling()); i++) {
            sapling.advanceTree(level, abs, level.getBlockState(abs), level.getRandom());
        }
        helper.assertBlockPresent(tree.log.get(), plant);
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(0, 2, 0, 6, 9, 6)) {
            if (helper.getBlockState(pos).is(tree.leaves())) leaves++;
        }
        helper.assertTrue(leaves >= 8, "a " + tree.name + " tree should have its fruiting leaves, found " + leaves);
        helper.succeed();
    }

    private OrchardTests() {}
}
