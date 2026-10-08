package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** Just for fun (GDD sections 14, 18.5): Tavern Keeper's shelf, and drinks saying what they do. */
public final class FunTests {
    private static final String EMPTY = "empty";
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest(template = EMPTY)
    public static void aBottleShelfCountsItsDifferentDrinks(GameTestHelper helper) {
        helper.setBlock(POS, ModBlocks.BOTTLE_SHELF.get());
        WineRackBlockEntity shelf = (WineRackBlockEntity) helper.getBlockEntity(POS, net.minecraft.world.level.block.entity.BlockEntity.class);
        List<Drinks.Drink> six = List.of(Drinks.PALE_ALE, Drinks.STOUT, Drinks.RED_WINE, Drinks.CIDER, Drinks.MALT_WHISKEY, Drinks.GIN);
        for (int slot = 0; slot < six.size(); slot++) shelf.bottles().insertItem(slot, new ItemStack(six.get(slot).item().get()), false);
        helper.assertTrue(shelf.differentDrinks() == 6, "six different drinks: a Tavern Keeper's shelf");
        shelf.bottles().extractItem(5, 1, false);
        shelf.bottles().insertItem(5, new ItemStack(Drinks.PALE_ALE.item().get()), false);
        helper.assertTrue(shelf.differentDrinks() == 5, "two pale ales count once");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinksSayWhatTheyDo(GameTestHelper helper) {
        ItemStack ale = new ItemStack(Drinks.PALE_ALE.item().get());
        helper.assertTrue(((DrinkItem) ale.getItem()).effectLines(ale).size() == 1, "a pale ale: Refreshed");
        ItemStack bitters = new ItemStack(Drinks.AROMATIC_BITTERS.item().get());
        helper.assertTrue(((DrinkItem) bitters.getItem()).effectLines(bitters).size() == 2, "bitters: their effect and what they cure");
        ItemStack crowned = new ItemStack(Drinks.APPLE_CROWN_WHISKEY.item().get());
        io.github.spencerharris192.seedtocellar.brewing.BrewData.copy(new BrewQuality(true, true, true, true, true).applyTo(new FluidStack(ModFluids.APPLE_CROWN_WHISKEY.get(), 250)), crowned);
        helper.assertTrue(((DrinkItem) crowned.getItem()).effectLines(crowned).size() == 3, "a crowned whiskey: Warmth and a golden apple's two");
        helper.succeed();
    }
}
