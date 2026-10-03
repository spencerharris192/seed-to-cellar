package io.github.spencerharris192.seedtocellar.gametest;

import net.minecraft.world.item.Items;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlock;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

/** Casks in every wood (ideal woods, nether woods, the wood on the label) and the Wine Rack. */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CaskWoodTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final long DAY = CaskBlockEntity.DAY;

    private static FluidStack redWine() {
        return new BrewQuality(true, true, false, false).applyTo(new FluidStack(ModFluids.RED_WINE.get(), 1000));
    }

    @GameTest(template = EMPTY)
    public static void idealWoodGivesTheStarOnTimeOtherWoodsTakeTwiceAsLong(GameTestHelper helper) {
        // red wine: star at 3 years in oak, dark oak or mangrove
        helper.assertTrue(BrewQuality.of(CaskBlockEntity.serving(redWine(), 3 * DAY, CaskWood.OAK)).aged(), "3 years in oak");
        helper.assertTrue(BrewQuality.of(CaskBlockEntity.serving(redWine(), 3 * DAY, CaskWood.DARK_OAK)).aged(), "3 years in dark oak");
        helper.assertFalse(BrewQuality.of(CaskBlockEntity.serving(redWine(), 3 * DAY, CaskWood.SPRUCE)).aged(), "not yet in spruce");
        helper.assertFalse(BrewQuality.of(CaskBlockEntity.serving(redWine(), 5 * DAY, CaskWood.SPRUCE)).aged(), "still not at 5");
        FluidStack spruce = CaskBlockEntity.serving(redWine(), 6 * DAY, CaskWood.SPRUCE);
        helper.assertTrue(BrewQuality.of(spruce).aged() && BrewQuality.of(spruce).stars() == 5, "6 years in spruce: the star");
        helper.assertTrue(spruce.getTag().getInt(DrinkItem.AGE) == 6 && spruce.getTag().getString(DrinkItem.WOOD).equals("spruce"),
                "the label says 6 years in spruce: " + spruce.getTag());
        // a drink that ages elsewhere keeps the label of the cask that gave it the most years
        FluidStack moved = CaskBlockEntity.serving(spruce, 2 * DAY, CaskWood.OAK);
        helper.assertTrue(moved.getTag().getInt(DrinkItem.AGE) == 6 && moved.getTag().getString(DrinkItem.WOOD).equals("spruce"),
                "2 years in oak don't relabel it: " + moved.getTag());
        // a keg (no wood) doesn't age or label
        helper.assertFalse(CaskBlockEntity.serving(redWine(), 9 * DAY, null).getOrCreateTag().contains(DrinkItem.AGE), "kegs don't age");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void netherWoodAgesTwiceAsFastButNeverGivesTheStar(GameTestHelper helper) {
        // (compared with oak, so the result doesn't depend on the aging-speed config other tests change)
        helper.assertTrue(CaskBlockEntity.agedTicks(1000, CaskWood.CRIMSON) == 2 * CaskBlockEntity.agedTicks(1000, CaskWood.OAK),
                "crimson counts double");
        FluidStack old = CaskBlockEntity.serving(redWine(), 40 * DAY, CaskWood.WARPED);
        helper.assertTrue(BrewQuality.of(old).craft(), "it still conditions");
        helper.assertFalse(BrewQuality.of(old).aged(), "but never earns the aging star");
        helper.assertTrue(old.getTag().getInt(DrinkItem.AGE) == 40 && old.getTag().getString(DrinkItem.WOOD).equals("warped"), "40 years in warped");
        helper.assertTrue(Drinks.OLD_ALE.profile().starYears(CaskWood.CRIMSON) < 0, "no star year in crimson");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyWoodPlacesAWorkingCask(GameTestHelper helper) {
        for (CaskWood wood : CaskWood.values()) {
            helper.setBlock(POS, ModBlocks.CASKS.get(wood).get());
            if (!(helper.getBlockEntity(POS) instanceof CaskBlockEntity cask) || cask.wood() != wood || !cask.ages()) {
                helper.fail("the " + wood.id() + " cask should age as " + wood.id());
                return;
            }
            helper.assertTrue(cask.handler().fill(redWine(), IFluidHandler.FluidAction.EXECUTE) == 1000,
                    wood.id() + " cask takes wine");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void wineRackHoldsSixBottlesAndNothingElse(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.WINE_RACK.get());
        WineRackBlockEntity rack = (WineRackBlockEntity) helper.getBlockEntity(POS);
        IItemHandler items = rack.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP)
                .orElseThrow(() -> new IllegalStateException("hoppers need an item handler"));
        ItemStack wine = new ItemStack(Drinks.RED_WINE.item().get(), 2);
        helper.assertTrue(items.insertItem(0, wine, false).getCount() == 1, "one bottle per hole");
        helper.assertTrue(items.insertItem(1, new ItemStack(Drinks.PALE_ALE.item().get()), false).getCount() == 1, "no mugs");
        helper.assertTrue(items.insertItem(1, new ItemStack(Drinks.APPLE_JUICE.item().get()), false).getCount() == 1, "no juice bottles");
        for (int slot = 1; slot < items.getSlots(); slot++) items.insertItem(slot, new ItemStack(Drinks.MEAD.item().get()), false);
        helper.assertTrue(rack.count() == 6, "six bottles");
        helper.assertTrue(ModBlocks.WINE_RACK.get().defaultBlockState().getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(POS)) == 15,
                "a full rack reads 15");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void clickingAHoleRacksABottleAndTakesItBack(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.WINE_RACK.get().defaultBlockState().setValue(WineRackBlock.FACING, Direction.NORTH));
        WineRackBlockEntity rack = (WineRackBlockEntity) helper.getBlockEntity(POS);
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Drinks.WHITE_WINE.item().get()));
        BlockPos abs = helper.absolutePos(POS);
        // facing the rack's north front, the bottom-left hole is at the east end (high x), low down: slot 3
        BlockHitResult bottomLeft = new BlockHitResult(new Vec3(abs.getX() + 0.85, abs.getY() + 0.25, abs.getZ()), Direction.NORTH, abs, false);
        helper.useBlock(POS, player, bottomLeft);
        helper.assertTrue(rack.bottle(3).is(Drinks.WHITE_WINE.item().get()), "the bottle went in the bottom-left hole");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "and left the hand");
        helper.useBlock(POS, player, bottomLeft);
        helper.assertTrue(rack.bottle(3).isEmpty(), "clicking the full hole takes it out");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Drinks.WHITE_WINE.item().get())), "back to the player");
        // the side isn't a hole
        helper.assertTrue(WineRackBlock.slotAt(rack.getBlockState(), abs, Vec3.atCenterOf(abs), Direction.EAST) == -1, "sides have no holes");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void shelvesTakeAnyDrinkDisplaysOnlyWine(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.BOTTLE_SHELF.get());
        WineRackBlockEntity shelf = (WineRackBlockEntity) helper.getBlockEntity(POS);
        helper.assertTrue(shelf.bottles().getSlots() == 6, "a shelf has six spots");
        helper.assertTrue(shelf.bottles().insertItem(0, new ItemStack(Drinks.PALE_ALE.item().get()), false).isEmpty(), "a mug of ale");
        helper.assertTrue(shelf.bottles().insertItem(1, new ItemStack(Drinks.APPLE_JUICE.item().get()), false).isEmpty(), "a bottle of juice");
        helper.assertTrue(shelf.bottles().insertItem(2, new ItemStack(Items.BREAD), false).getCount() == 1, "but not bread");

        helper.setBlock(POS, ModBlocks.WINE_DISPLAY.get());
        WineRackBlockEntity display = (WineRackBlockEntity) helper.getBlockEntity(POS);
        helper.assertTrue(display.bottles().getSlots() == 3, "a display has three shelves");
        helper.assertTrue(display.bottles().insertItem(0, new ItemStack(Drinks.PALE_ALE.item().get()), false).getCount() == 1, "no mugs");
        helper.assertTrue(display.bottles().insertItem(0, new ItemStack(Drinks.RED_WINE.item().get()), false).isEmpty(), "wine");
        // the shelves count from the top: a click low on the front is the bottom shelf
        BlockPos abs = helper.absolutePos(POS);
        helper.assertTrue(WineRackBlock.slotAt(display.getBlockState(), abs, new Vec3(abs.getX() + 0.5, abs.getY() + 0.1, abs.getZ()),
                Direction.NORTH) == 2, "the bottom shelf is the third place");
        helper.succeed();
    }

    private CaskWoodTests() {}
}
