package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Our finds in vanilla's chests (GDD section 18.4), added on top of vanilla's loot. */
public final class ChestLootTests {
    private static final String EMPTY = "empty";

    /** Everything 200 chests of this kind held. */
    private static List<ItemStack> open(GameTestHelper helper, net.minecraft.resources.ResourceKey<LootTable> table) {
        LootTable loot = helper.getLevel().getServer().reloadableRegistries().getLootTable(table);
        List<ItemStack> all = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            LootParams params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))))
                    .create(LootContextParamSets.CHEST);
            all.addAll(loot.getRandomItems(params));
        }
        return all;
    }

    private static boolean holds(List<ItemStack> loot, Item item) {
        return loot.stream().anyMatch(s -> s.is(item));
    }

    @GameTest(template = EMPTY)
    public static void villageHousesHoldTheirRegionsSeeds(GameTestHelper helper) {
        List<ItemStack> plains = open(helper, BuiltInLootTables.VILLAGE_PLAINS_HOUSE);
        helper.assertTrue(holds(plains, Crops.BARLEY.seeds()) && holds(plains, ModItems.HOP_RHIZOME.get()), "plains houses: barley and hops");
        helper.assertTrue(holds(plains, net.minecraft.world.item.Items.BREAD) || holds(plains, net.minecraft.world.item.Items.WHEAT_SEEDS),
                "vanilla's loot is still there");
        helper.assertTrue(holds(open(helper, BuiltInLootTables.VILLAGE_DESERT_HOUSE), Crops.AGAVE.seeds()), "desert houses: agave pups");
        helper.assertTrue(holds(open(helper, BuiltInLootTables.VILLAGE_SNOWY_HOUSE), Crops.RYE.seeds()), "snowy houses: rye");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void templesHideTheExoticCrops(GameTestHelper helper) {
        helper.assertTrue(holds(open(helper, BuiltInLootTables.DESERT_PYRAMID), Crops.AGAVE.seeds()), "desert pyramids: agave pups");
        List<ItemStack> jungle = open(helper, BuiltInLootTables.JUNGLE_TEMPLE);
        helper.assertTrue(holds(jungle, ModItems.VANILLA_POD.get()) && holds(jungle, Crops.COFFEE.seeds()), "jungle temples: vanilla and coffee");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void shipwrecksKeepAgedRum(GameTestHelper helper) {
        List<ItemStack> rum = open(helper, BuiltInLootTables.SHIPWRECK_SUPPLY).stream().filter(s -> s.is(Drinks.RUM.item().get())).toList();
        helper.assertFalse(rum.isEmpty(), "shipwrecks hold rum");
        helper.assertTrue(rum.stream().allMatch(s -> io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(s) != null && io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(s).contains(BrewQuality.TAG)), "with a quality of its own");
        helper.assertTrue(rum.stream().anyMatch(s -> io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(s).getIntOr(DrinkItem.AGE, 0) > 0), "some of it aged");
        helper.assertTrue(rum.stream().map(s -> DrinkItem.quality(s).stars()).distinct().count() > 1, "not all of it alike");
        helper.assertTrue(rum.stream().allMatch(s -> io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(s).getIntOr(io.github.spencerharris192.seedtocellar.brewing.CraftStep.RUNS, 0)
                == (DrinkItem.quality(s).craft() ? 2 : 1)), "its label says how many runs gave it its craft star");
        helper.succeed();
    }
}
