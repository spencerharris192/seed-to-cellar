package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock;
import io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/** Decor and the tavern (GDD sections 16 and 17.3). */
public final class DecorTests {
    private static final String EMPTY = "empty";
    private static final BlockPos TABLE = new BlockPos(1, 1, 1);

    /** A sneaking click with `stack` on the top of the block at `on`. */
    private static void sneakClickTop(GameTestHelper helper, Player player, ItemStack stack, BlockPos on) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setShiftKeyDown(true);
        BlockPos at = helper.absolutePos(on);
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(at).add(0, 0.5, 0), Direction.UP, at, false)));
    }

    @GameTest(template = EMPTY)
    public static void drinksSetDownStandTogetherFourAtMost(GameTestHelper helper) {
        helper.setBlock(TABLE, Blocks.OAK_PLANKS);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack whiskey = new ItemStack(Drinks.MALT_WHISKEY.item().get());
        io.github.spencerharris192.seedtocellar.brewing.BrewData.copy(new BrewQuality(true, true, true, false).applyTo(new FluidStack(ModFluids.MALT_WHISKEY.get(), 250)), whiskey);
        sneakClickTop(helper, player, whiskey.copy(), TABLE);
        BlockPos spot = TABLE.above();
        helper.assertBlockPresent(ModBlocks.PLACED_DRINKS.get(), spot);
        helper.assertBlockProperty(spot, PlacedDrinksBlock.DRINKS, 1);
        for (int i = 0; i < 4; i++) sneakClickTop(helper, player, new ItemStack(Drinks.PALE_ALE.item().get()), spot);
        helper.assertBlockProperty(spot, PlacedDrinksBlock.DRINKS, 4);   // the fifth found no room

        // An empty hand takes the last one back; the first keeps its stars.
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockPos at = helper.absolutePos(spot);
        Interact.use(helper.getBlockState(spot), helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        helper.assertTrue(player.getMainHandItem().is(Drinks.PALE_ALE.item().get()), "an empty hand takes the last one back");
        helper.assertBlockProperty(spot, PlacedDrinksBlock.DRINKS, 3);
        PlacedDrinksBlockEntity drinks = (PlacedDrinksBlockEntity) helper.getBlockEntity(spot, net.minecraft.world.level.block.entity.BlockEntity.class);
        helper.assertTrue(DrinkItem.quality(drinks.drinks().get(0)).stars() == 4, "a set-down drink keeps its stars");

        // Take the table away and they fall as items, nothing lost.
        helper.setBlock(TABLE, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.PLACED_DRINKS.get(), spot);
        helper.assertItemEntityCountIs(Drinks.MALT_WHISKEY.item().get(), spot, 2, 1);
        helper.assertItemEntityCountIs(Drinks.PALE_ALE.item().get(), spot, 2, 2);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bundlesHangUnderABlockOrOnAWall(GameTestHelper helper) {
        BlockPos beam = new BlockPos(1, 2, 1);
        helper.setBlock(beam, Blocks.OAK_PLANKS);
        var hanging = ModBlocks.HOP_BUNDLE.get().defaultBlockState();
        helper.assertTrue(hanging.canSurvive(helper.getLevel(), helper.absolutePos(beam.below())), "a bundle hangs under a block");
        helper.setBlock(beam.below(), hanging);
        helper.setBlock(beam, Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.HOP_BUNDLE.get(), beam.below());
        helper.assertItemEntityPresent(io.github.spencerharris192.seedtocellar.registry.ModItems.HOP_BUNDLE.get(), beam.below(), 2);

        helper.setBlock(new BlockPos(1, 1, 2), Blocks.STONE);   // a wall to the south
        var onWall = ModBlocks.GARLIC_BRAID.get().defaultBlockState()
                .setValue(io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock.WALL, true)
                .setValue(io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock.FACING, Direction.NORTH);
        BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.assertTrue(onWall.canSurvive(helper.getLevel(), at), "or from a nail on a wall, facing out");
        helper.assertFalse(onWall.setValue(io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock.FACING, Direction.SOUTH)
                .canSurvive(helper.getLevel(), at), "but not with nothing behind it");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void aBarStoolSeatsOneAndTheCounterTakesDrinks(GameTestHelper helper) {
        BlockPos stool = new BlockPos(1, 1, 1);
        helper.setBlock(stool, ModBlocks.BAR_STOOLS.get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.OAK).get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos at = helper.absolutePos(stool);
        Interact.use(helper.getBlockState(stool), helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        helper.assertTrue(player.getVehicle() instanceof io.github.spencerharris192.seedtocellar.decor.SeatEntity, "right-click a stool to sit on it");
        Player second = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        Interact.use(helper.getBlockState(stool), helper.getLevel(), second, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
        helper.assertFalse(second.isPassenger(), "one sitter at a time");
        var seat = player.getVehicle();
        helper.setBlock(stool, Blocks.AIR);   // the stool goes: the sitter stands, the seat goes too
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(player.isPassenger() || seat.isAlive(), "no stool, no seat");
            BlockPos counter = new BlockPos(1, 1, 2);
            helper.setBlock(counter, ModBlocks.BAR_COUNTERS.get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.SPRUCE).get());
            sneakClickTop(helper, player, new ItemStack(Drinks.STOUT.item().get()), counter);
            helper.assertBlockPresent(ModBlocks.PLACED_DRINKS.get(), counter.above());   // a counter's top takes drinks
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void aMugRackHangsFourEmptyMugs(GameTestHelper helper) {
        BlockPos at = new BlockPos(1, 1, 1);
        helper.setBlock(at, ModBlocks.MUG_RACK.get().defaultBlockState().setValue(io.github.spencerharris192.seedtocellar.winery.WineRackBlock.FACING,
                Direction.NORTH));
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(at);
        // The front's right-hand quarter, as the world sees it, is the viewer's leftmost peg.
        BlockHitResult leftPeg = new BlockHitResult(Vec3.atLowerCornerOf(abs).add(0.875, 0.75, 0.5), Direction.NORTH, abs, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Drinks.PALE_ALE.item().get()));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, leftPeg);
        var rack = (io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity) helper.getBlockEntity(at, net.minecraft.world.level.block.entity.BlockEntity.class);
        helper.assertTrue(rack.bottle(0).isEmpty(), "a full mug doesn't hang on a peg");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(io.github.spencerharris192.seedtocellar.registry.ModItems.MUG.get(), 2));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, leftPeg);
        helper.assertTrue(rack.bottle(0).is(io.github.spencerharris192.seedtocellar.registry.ModItems.MUG.get()), "an empty one does");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "one mug to a peg");
        helper.assertTrue(rack.bottles().getSlots() == 4, "four pegs");
        helper.setBlock(at, Blocks.AIR);
        helper.assertItemEntityPresent(io.github.spencerharris192.seedtocellar.registry.ModItems.MUG.get(), at, 2);   // its mug drops
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aTavernSignRepaintsAndNeedsItsWall(GameTestHelper helper) {
        BlockPos wall = new BlockPos(1, 1, 2), at = new BlockPos(1, 1, 1);
        helper.setBlock(wall, Blocks.STONE);
        var facingNorth = ModBlocks.TAVERN_SIGN.get().defaultBlockState().setValue(io.github.spencerharris192.seedtocellar.decor.WallDecorBlock.FACING,
                Direction.NORTH);
        helper.assertTrue(facingNorth.canSurvive(helper.getLevel(), helper.absolutePos(at)), "a sign hangs out from a wall");
        helper.assertFalse(facingNorth.setValue(io.github.spencerharris192.seedtocellar.decor.WallDecorBlock.FACING, Direction.SOUTH)
                .canSurvive(helper.getLevel(), helper.absolutePos(at)), "but not from thin air");
        helper.setBlock(at, facingNorth);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(at);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.EAST, abs, false);
        var emblem = io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.EMBLEM;
        helper.assertBlockProperty(at, emblem, io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.ALE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Drinks.RED_WINE.item().get()));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(at, emblem, io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.WINE);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Drinks.MALT_WHISKEY.item().get()));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(at, emblem, io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.SPIRITS);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(io.github.spencerharris192.seedtocellar.registry.ModItems.CASKS
                .get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.OAK).get()));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(at, emblem, io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.CASK);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Drinks.LEMONADE.item().get()));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(at, emblem, io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.CASK);   // no picture for juice
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        // A garland beside it; take the wall away and both fall, dropping themselves.
        BlockPos garland = new BlockPos(0, 1, 1);
        helper.setBlock(new BlockPos(0, 1, 2), Blocks.STONE);
        helper.setBlock(garland, ModBlocks.HOP_GARLAND.get().defaultBlockState()
                .setValue(io.github.spencerharris192.seedtocellar.decor.WallDecorBlock.FACING, Direction.NORTH));
        helper.setBlock(wall, Blocks.AIR);
        helper.setBlock(new BlockPos(0, 1, 2), Blocks.AIR);
        helper.assertBlockNotPresent(ModBlocks.TAVERN_SIGN.get(), at);
        helper.assertBlockNotPresent(ModBlocks.HOP_GARLAND.get(), garland);
        helper.assertItemEntityPresent(io.github.spencerharris192.seedtocellar.registry.ModItems.TAVERN_SIGN.get(), at, 2);
        helper.assertItemEntityPresent(io.github.spencerharris192.seedtocellar.registry.ModItems.HOP_GARLAND.get(), garland, 2);
        helper.succeed();
    }

    /** The Hydrometer reads a station that opens a screen on a right-click (it reads first), and leaves stone alone. */
    @GameTest(template = EMPTY)
    public static void theHydrometerReadsBeforeAScreenOpens(GameTestHelper helper) {
        BlockPos kettle = new BlockPos(1, 1, 1);
        helper.setBlock(kettle, ModBlocks.BREW_KETTLE.get());
        helper.setBlock(kettle.east(), Blocks.STONE);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack hydrometer = new ItemStack(io.github.spencerharris192.seedtocellar.registry.ModItems.HYDROMETER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hydrometer);
        java.util.function.Function<BlockPos, InteractionResult> read = at -> {
            BlockPos abs = helper.absolutePos(at);
            return hydrometer.onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        };
        helper.assertTrue(read.apply(kettle).consumesAction(), "the kettle is read, not opened");
        helper.assertTrue(read.apply(kettle.east()) == InteractionResult.PASS, "stone has nothing to read");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void barCountersTurnCornersLikeStairs(GameTestHelper helper) {
        var shape = io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.SHAPE;
        var facing = io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.FACING;
        var oak = ModBlocks.BAR_COUNTERS.get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.OAK).get().defaultBlockState();
        var spruce = ModBlocks.BAR_COUNTERS.get(io.github.spencerharris192.seedtocellar.brewing.CaskWood.SPRUCE).get().defaultBlockState();
        // An L sticking out toward the customers: a counter facing north with one behind it facing west (any wood joins).
        BlockPos corner = new BlockPos(1, 1, 1);
        helper.setBlock(corner, oak.setValue(facing, Direction.NORTH));
        helper.assertBlockProperty(corner, shape, net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT);
        helper.setBlock(corner.south(), spruce.setValue(facing, Direction.WEST));
        helper.assertBlockProperty(corner, shape, net.minecraft.world.level.block.state.properties.StairsShape.OUTER_LEFT);
        // In the middle of a straight run, it stays straight.
        helper.setBlock(corner.west(), oak.setValue(facing, Direction.NORTH));
        helper.assertBlockProperty(corner, shape, net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT);
        // An L wrapped round the customers: one in front of it facing across, to the right this time.
        BlockPos inside = new BlockPos(1, 2, 1);
        helper.setBlock(inside, oak.setValue(facing, Direction.NORTH));
        helper.setBlock(inside.north(), oak.setValue(facing, Direction.EAST));
        helper.assertBlockProperty(inside, shape, net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void copperWeathersWaxesAndScrapes(GameTestHelper helper) {
        var stage = io.github.spencerharris192.seedtocellar.decor.CopperWeathering.STAGE;
        var waxed = io.github.spencerharris192.seedtocellar.decor.CopperWeathering.WAXED;
        BlockPos at = new BlockPos(1, 1, 1);
        BlockPos abs = helper.absolutePos(at);
        helper.setBlock(at, ModBlocks.BREW_KETTLE.get());
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(7);
        for (int i = 0; i < 5000 && helper.getBlockState(at).getValue(stage) == io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.UNAFFECTED; i++) {
            helper.getBlockState(at).randomTick(helper.getLevel(), abs, random);
        }
        helper.assertBlockProperty(at, stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.EXPOSED);   // it turns, a stage at a time

        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.HONEYCOMB, 2));
        Interact.use(helper.getBlockState(at), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertBlockProperty(at, waxed, true);
        helper.assertFalse(helper.getBlockState(at).isRandomlyTicking(), "waxed copper keeps its look");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "waxing takes a honeycomb");

        // An axe: the wax first, then a stage of patina, then nothing more to do.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.IRON_AXE));
        // (26.3 axes change blocks through data; ours are handled by the block itself, first, as a player's click does)
        java.util.function.Supplier<InteractionResult> chop = () -> Interact.use(helper.getBlockState(at), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);
        chop.get();
        helper.assertBlockProperty(at, waxed, false);
        helper.assertBlockProperty(at, stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.EXPOSED);
        chop.get();
        helper.assertBlockProperty(at, stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.UNAFFECTED);
        chop.get();
        helper.assertBlockProperty(at, stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.UNAFFECTED);
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 2, "fresh copper has nothing to scrape (the axe isn't worn)");

        // Broken fresh, it's a plain kettle that stacks with new ones.
        helper.getLevel().destroyBlock(abs, true);
        var kettles = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(abs).inflate(2));
        helper.assertTrue(kettles.size() == 1 && kettles.get(0).getItem().is(io.github.spencerharris192.seedtocellar.registry.ModItems.BREW_KETTLE.get())
                && kettles.get(0).getItem().isComponentsPatchEmpty(), "a fresh kettle drops a plain kettle");
        kettles.forEach(net.minecraft.world.entity.Entity::discard);

        // The still's two halves stay in step, whichever is touched; broken, it keeps its patina and wax.
        BlockPos pot = new BlockPos(0, 1, 0);
        var still = ModBlocks.POT_STILL.get().defaultBlockState().setValue(stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.WEATHERED);
        helper.setBlock(pot, still);
        helper.setBlock(pot.above(), still.setValue(io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        BlockPos head = helper.absolutePos(pot.above());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.HONEYCOMB));
        Interact.use(helper.getBlockState(pot.above()), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(head), Direction.NORTH, head, false));
        helper.assertBlockProperty(pot, waxed, true);   // waxing the head waxed the pot
        helper.setBlock(pot, helper.getBlockState(pot).setValue(stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.OXIDIZED));
        helper.assertBlockProperty(pot.above(), stage, io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.OXIDIZED);
        helper.getLevel().destroyBlock(helper.absolutePos(pot), true);
        var stills = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(pot)).inflate(2));
        helper.assertTrue(stills.size() == 1, "one still drops, from the pot");
        ItemStack dropped = stills.get(0).getItem();
        helper.assertTrue(io.github.spencerharris192.seedtocellar.decor.CopperWeathering.stage(dropped)
                == io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.OXIDIZED
                && io.github.spencerharris192.seedtocellar.decor.CopperWeathering.waxed(dropped), "the item keeps its patina and wax");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinksOnlyStandOnSomethingSolid(GameTestHelper helper) {
        helper.setBlock(TABLE, Blocks.FLOWER_POT);   // nothing to stand on at its top
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        sneakClickTop(helper, player, new ItemStack(Drinks.RED_WINE.item().get()), TABLE);
        helper.assertBlockNotPresent(ModBlocks.PLACED_DRINKS.get(), TABLE.above());
        helper.setBlock(TABLE, Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
                net.minecraft.world.level.block.state.properties.SlabType.TOP));   // a slab in the top half has a full top
        sneakClickTop(helper, player, new ItemStack(Drinks.RED_WINE.item().get()), TABLE);
        helper.assertBlockPresent(ModBlocks.PLACED_DRINKS.get(), TABLE.above());
        helper.succeed();
    }
}
