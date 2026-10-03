package io.github.spencerharris192.seedtocellar.world;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Map;

/**
 * Villages (GDD section 18): the Vintner's and the Brewer's trades, the wandering trader's exotic saplings, and the
 * Vineyard (plains and savanna villages) and Brewhouse (plains, taiga and snowy villages), added to their villages'
 * building lists when a world starts (the way most mods add village buildings, so no vanilla file is replaced).
 */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class Villages {
    /**
     * How often each building is tried among a village's houses, by village (vanilla houses weigh 1-4). Ours are big and
     * often don't fit where they're tried, so they weigh more; tuned so each turns up in roughly 40-50% of its villages
     * (VillageTests checks a good share do).
     */
    private static final Map<String, Integer> VINEYARD_WEIGHTS = Map.of("plains", 10, "savanna", 12);
    private static final Map<String, Integer> BREWHOUSE_WEIGHTS = Map.of("plains", 18, "taiga", 10, "snowy", 16);
    /** The Brewhouse's cellar template is this many blocks tall, built under the hall's floor. */
    private static final int BREWHOUSE_CELLAR_DEPTH = 4;

    @SubscribeEvent
    public static void addVillageBuildings(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        if (ModConfigs.worldgenEnabled("vineyards")) {
            VINEYARD_WEIGHTS.forEach((village, weight) ->
                    // single (not legacy) elements place the template's air, so the plot is cleared
                    addBuilding(pools, village, StructurePoolElement.single(SeedToCellar.id("village/" + village + "/vineyard").toString())
                            .apply(StructureTemplatePool.Projection.RIGID), weight));
        }
        if (ModConfigs.worldgenEnabled("brewhouses")) {
            BREWHOUSE_WEIGHTS.forEach((village, weight) ->
                    addBuilding(pools, village, CellarPoolElement.of(SeedToCellar.id("village/" + village + "/brewhouse"),
                            SeedToCellar.id("village/" + village + "/brewhouse_cellar"), BREWHOUSE_CELLAR_DEPTH), weight));
        }
    }

    private static void addBuilding(Registry<StructureTemplatePool> pools, String village, StructurePoolElement element, int weight) {
        StructureTemplatePool target = pools.get(SeedToCellar.rl("minecraft", "village/" + village + "/houses"));
        if (target == null) return;
        // Villages draw from the pool's expanded list (each building repeated by its weight).
        ObjectArrayList<StructurePoolElement> templates = ObfuscationReflectionHelper.getPrivateValue(StructureTemplatePool.class, target,
                "f_210560_");
        if (templates == null) return;
        for (int i = 0; i < weight; i++) templates.add(element);
    }

    // --- trades ------------------------------------------------------------------------------

    /**
     * The Vintner (GDD section 18.3): buys grapes, apples, honey and, higher up, good wine; sells cuttings, trellises,
     * bottles, wine yeast, saplings and wine. The wine it buys must have at least the listed quality checks.
     */
    @SubscribeEvent
    public static void vintnerTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.VINTNER.get()) return;
        var trades = event.getTrades();
        trades.get(1).addAll(List.of(
                buy(new ItemStack(Crops.RED_GRAPE.produce(), 16), 1, 16, 2),
                buy(new ItemStack(Crops.WHITE_GRAPE.produce(), 16), 1, 16, 2),
                sell(Crops.RED_GRAPE.seeds(), 2, 1, 12, 1),
                sell(Crops.WHITE_GRAPE.seeds(), 2, 1, 12, 1)));
        trades.get(2).addAll(List.of(
                buy(new ItemStack(Items.APPLE, 12), 1, 16, 10),
                sell(ModItems.TRELLIS.get(), 4, 1, 12, 5),
                sell(ModItems.WINE_BOTTLE.get(), 3, 1, 12, 5)));
        trades.get(3).addAll(List.of(
                buy(new ItemStack(Items.HONEY_BOTTLE, 3), 1, 12, 20),
                sell(ModItems.WINE_YEAST.get(), 1, 2, 8, 10),
                new BasicItemListing(3, drink(Drinks.RED_WINE, new BrewQuality(true, true, false, false), 0), 8, 10)));
        trades.get(4).addAll(List.of(
                buy(drinkCost(Drinks.RED_WINE, false), 2, 12, 20),
                buy(drinkCost(Drinks.WHITE_WINE, false), 2, 12, 20),
                sell(FruitTrees.APPLE.saplingItem(), 1, 4, 8, 15)));
        trades.get(5).addAll(List.of(
                buy(drinkCost(Drinks.RED_WINE, true), 6, 12, 30),
                new BasicItemListing(8, drink(Drinks.RED_WINE, new BrewQuality(true, true, true, false), 2), 4, 30)));
    }

    /**
     * The Brewer (GDD section 18.3): buys barley, hops, wheat and, higher up, good beer; sells mugs, malts, hop rhizomes,
     * ale yeast, taps and beer. Like the vintner, it takes any beer at least as good as it asks for.
     */
    @SubscribeEvent
    public static void brewerTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.BREWER.get()) return;
        var trades = event.getTrades();
        trades.get(1).addAll(List.of(
                buy(new ItemStack(Crops.BARLEY.produce(), 20), 1, 16, 2),
                buy(new ItemStack(ModItems.HOP_CONES.get(), 12), 1, 16, 2),
                sell(ModItems.MUG.get(), 2, 1, 12, 1)));
        trades.get(2).addAll(List.of(
                buy(new ItemStack(Items.WHEAT, 20), 1, 16, 10),
                sell(ModItems.PALE_MALT.get(), 6, 1, 12, 5),
                sell(ModItems.HOP_RHIZOME.get(), 1, 2, 8, 5)));
        trades.get(3).addAll(List.of(
                buy(new ItemStack(ModItems.DRIED_HOPS.get(), 10), 1, 12, 20),
                sell(ModItems.ALE_YEAST.get(), 1, 2, 8, 10),
                sell(ModItems.AMBER_MALT.get(), 4, 1, 12, 10),
                new BasicItemListing(2, drink(Drinks.PALE_ALE, new BrewQuality(true, true, false, false), 0), 8, 10)));
        trades.get(4).addAll(List.of(
                buy(drinkCost(Drinks.PALE_ALE, false), 2, 12, 20),
                buy(drinkCost(Drinks.STOUT, false), 2, 12, 20),
                sell(ModItems.BLACK_MALT.get(), 4, 1, 12, 15),
                sell(ModItems.TAP.get(), 2, 1, 8, 15)));
        trades.get(5).addAll(List.of(
                buy(drinkCost(Drinks.OLD_ALE, true), 6, 12, 30),
                new BasicItemListing(8, drink(Drinks.OLD_ALE, new BrewQuality(true, true, true, false), 2), 4, 30)));
    }

    /** The wandering trader: the hot-climate crops that are hardest to find (GDD section 18.3). */
    @SubscribeEvent
    public static void wandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().addAll(List.of(
                sell(FruitTrees.LEMON.saplingItem(), 1, 5, 8, 1),
                sell(FruitTrees.ORANGE.saplingItem(), 1, 5, 8, 1),
                sell(FruitTrees.OLIVE.saplingItem(), 1, 5, 8, 1),
                sell(FruitTrees.PEACH.saplingItem(), 1, 5, 8, 1),
                sell(Crops.SORGHUM.seeds(), 3, 1, 12, 1),
                sell(Crops.AGAVE.seeds(), 2, 2, 8, 1),
                sell(io.github.spencerharris192.seedtocellar.registry.ModItems.VANILLA_POD.get(), 2, 2, 8, 1),
                sell(Crops.COFFEE.seeds(), 3, 2, 8, 1)));
    }

    private static VillagerTrades.ItemListing buy(ItemStack cost, int emeralds, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(cost.copy(), new ItemStack(Items.EMERALD, emeralds), maxUses, xp, 0.05F);
    }

    private static VillagerTrades.ItemListing sell(ItemLike item, int count, int emeralds, int maxUses, int xp) {
        return new BasicItemListing(emeralds, new ItemStack(item, count), maxUses, xp);
    }

    /** A drink with the given checks (and, if aged, years in oak on the label). */
    private static ItemStack drink(Drinks.Drink drink, BrewQuality quality, int years) {
        FluidStack fluid = quality.applyTo(new FluidStack(drink.fluid().get(), DrinkItem.SERVING));
        if (years > 0) {
            fluid.getOrCreateTag().putInt(DrinkItem.AGE, years);
            fluid.getOrCreateTag().putString(DrinkItem.WOOD, CaskWood.OAK.id());
        }
        return DrinkItem.fromFluid(fluid);
    }

    /**
     * What a vintner or brewer will buy: a drink with at least cultured yeast and the right temperature (3 stars), or with
     * every check (5 stars). Villagers match only the quality keys listed, so a better drink is accepted too.
     */
    private static ItemStack drinkCost(Drinks.Drink drink, boolean fiveStars) {
        CompoundTag brew = new CompoundTag();
        brew.putBoolean("Yeast", true);
        brew.putBoolean("Temperature", true);
        if (fiveStars) {
            brew.putBoolean("Craft", true);
            brew.putBoolean("Aged", true);
        }
        ItemStack stack = new ItemStack(drink.item().get());
        stack.getOrCreateTag().put(BrewQuality.TAG, brew);
        return stack;
    }

    private Villages() {}
}
