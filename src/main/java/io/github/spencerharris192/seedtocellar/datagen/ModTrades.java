package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Villager trades (GDD section 18.3), as data: each trade is an entry of its own, joined to its profession's level by a tag,
 * and each level's trade set offers two of them at random, as vanilla's do. The wandering trader's join vanilla's common
 * pool (it offers five from it).
 */
public final class ModTrades {
    /** How many of a level's trades a villager offers. */
    private static final int OFFERED = 2;
    private static final float DISCOUNT = 0.05F;

    private record Trade(ResourceKey<VillagerTrade> key, TagKey<VillagerTrade> tag, Supplier<VillagerTrade> trade) {}

    private static final List<Trade> ALL = new ArrayList<>();
    private static final String[] PROFESSIONS = {"vintner", "brewer"};

    static {
        vintner();
        brewer();
        wanderer();
    }

    /**
     * The Vintner: buys grapes, apples, honey and, higher up, good wine; sells cuttings, trellises, bottles, wine yeast,
     * saplings and wine. The wine it buys must have at least the listed quality checks.
     */
    private static void vintner() {
        add("vintner", 1, "red_grapes", () -> buy(Crops.RED_GRAPE.produce(), 16, 1, 16, 2));
        add("vintner", 1, "white_grapes", () -> buy(Crops.WHITE_GRAPE.produce(), 16, 1, 16, 2));
        add("vintner", 1, "red_grape_cuttings", () -> sell(Crops.RED_GRAPE.seeds(), 2, 1, 12, 1));
        add("vintner", 1, "white_grape_cuttings", () -> sell(Crops.WHITE_GRAPE.seeds(), 2, 1, 12, 1));
        add("vintner", 2, "apples", () -> buy(Items.APPLE, 12, 1, 16, 10));
        add("vintner", 2, "trellis", () -> sell(ModItems.TRELLIS.get(), 4, 1, 12, 5));
        add("vintner", 2, "wine_bottles", () -> sell(ModItems.WINE_BOTTLE.get(), 3, 1, 12, 5));
        add("vintner", 3, "honey", () -> buy(Items.HONEY_BOTTLE, 3, 1, 12, 20));
        add("vintner", 3, "wine_yeast", () -> sell(ModItems.WINE_YEAST.get(), 1, 2, 8, 10));
        add("vintner", 3, "red_wine", () -> sellDrink(Drinks.RED_WINE, new BrewQuality(true, true, false, false), 0, 3, 8, 10));
        add("vintner", 4, "good_red_wine", () -> buyDrink(Drinks.RED_WINE, false, 2, 12, 20));
        add("vintner", 4, "good_white_wine", () -> buyDrink(Drinks.WHITE_WINE, false, 2, 12, 20));
        add("vintner", 4, "apple_sapling", () -> sell(FruitTrees.APPLE.saplingItem(), 1, 4, 8, 15));
        add("vintner", 5, "fine_red_wine", () -> buyDrink(Drinks.RED_WINE, true, 6, 12, 30));
        add("vintner", 5, "aged_red_wine", () -> sellDrink(Drinks.RED_WINE, new BrewQuality(true, true, true, false), 2, 8, 4, 30));
    }

    /**
     * The Brewer: buys barley, hops, wheat and, higher up, good beer; sells mugs, malts, hop rhizomes, ale yeast, taps and
     * beer. Like the vintner, it takes any beer at least as good as it asks for.
     */
    private static void brewer() {
        add("brewer", 1, "barley", () -> buy(Crops.BARLEY.produce(), 20, 1, 16, 2));
        add("brewer", 1, "hop_cones", () -> buy(ModItems.HOP_CONES.get(), 12, 1, 16, 2));
        add("brewer", 1, "mugs", () -> sell(ModItems.MUG.get(), 2, 1, 12, 1));
        add("brewer", 2, "wheat", () -> buy(Items.WHEAT, 20, 1, 16, 10));
        add("brewer", 2, "pale_malt", () -> sell(ModItems.PALE_MALT.get(), 6, 1, 12, 5));
        add("brewer", 2, "hop_rhizome", () -> sell(ModItems.HOP_RHIZOME.get(), 1, 2, 8, 5));
        add("brewer", 3, "dried_hops", () -> buy(ModItems.DRIED_HOPS.get(), 10, 1, 12, 20));
        add("brewer", 3, "ale_yeast", () -> sell(ModItems.ALE_YEAST.get(), 1, 2, 8, 10));
        add("brewer", 3, "amber_malt", () -> sell(ModItems.AMBER_MALT.get(), 4, 1, 12, 10));
        add("brewer", 3, "pale_ale", () -> sellDrink(Drinks.PALE_ALE, new BrewQuality(true, true, false, false), 0, 2, 8, 10));
        add("brewer", 4, "good_pale_ale", () -> buyDrink(Drinks.PALE_ALE, false, 2, 12, 20));
        add("brewer", 4, "good_stout", () -> buyDrink(Drinks.STOUT, false, 2, 12, 20));
        add("brewer", 4, "black_malt", () -> sell(ModItems.BLACK_MALT.get(), 4, 1, 12, 15));
        add("brewer", 4, "taps", () -> sell(ModItems.TAP.get(), 2, 1, 8, 15));
        add("brewer", 5, "fine_old_ale", () -> buyDrink(Drinks.OLD_ALE, true, 6, 12, 30));
        add("brewer", 5, "aged_old_ale", () -> sellDrink(Drinks.OLD_ALE, new BrewQuality(true, true, true, false), 2, 8, 4, 30));
    }

    /** The wandering trader: the hot-climate crops that are hardest to find. */
    private static void wanderer() {
        TagKey<VillagerTrade> common = VillagerTradeTags.WANDERING_TRADER_COMMON;
        add(common, "lemon_sapling", () -> sell(FruitTrees.LEMON.saplingItem(), 1, 5, 8, 1));
        add(common, "orange_sapling", () -> sell(FruitTrees.ORANGE.saplingItem(), 1, 5, 8, 1));
        add(common, "olive_sapling", () -> sell(FruitTrees.OLIVE.saplingItem(), 1, 5, 8, 1));
        add(common, "peach_sapling", () -> sell(FruitTrees.PEACH.saplingItem(), 1, 5, 8, 1));
        add(common, "sorghum_seeds", () -> sell(Crops.SORGHUM.seeds(), 3, 1, 12, 1));
        add(common, "agave_pups", () -> sell(Crops.AGAVE.seeds(), 2, 2, 8, 1));
        add(common, "vanilla_pods", () -> sell(ModItems.VANILLA_POD.get(), 2, 2, 8, 1));
        add(common, "coffee_seeds", () -> sell(Crops.COFFEE.seeds(), 3, 2, 8, 1));
    }

    private static TagKey<VillagerTrade> levelTag(String profession, int level) {
        return TagKey.create(Registries.VILLAGER_TRADE, ModVillagers.tradeSet(profession, level).identifier());
    }

    private static void add(String profession, int level, String name, Supplier<VillagerTrade> trade) {
        ALL.add(new Trade(ResourceKey.create(Registries.VILLAGER_TRADE, SeedToCellar.id(profession + "/" + level + "/" + name)),
                levelTag(profession, level), trade));
    }

    private static void add(TagKey<VillagerTrade> tag, String name, Supplier<VillagerTrade> trade) {
        ALL.add(new Trade(ResourceKey.create(Registries.VILLAGER_TRADE, SeedToCellar.id("wandering_trader/" + name)), tag, trade));
    }

    /** The villager buys {@code count} of {@code item} for {@code emeralds}. */
    private static VillagerTrade buy(ItemLike item, int count, int emeralds, int maxUses, int xp) {
        return VillagerTrade.builder(new TradeCost(item, count), new ItemStackTemplate(Items.EMERALD, emeralds), maxUses, xp, DISCOUNT).build();
    }

    /** The villager sells {@code count} of {@code item} for {@code emeralds}. */
    private static VillagerTrade sell(ItemLike item, int count, int emeralds, int maxUses, int xp) {
        return VillagerTrade.builder(new TradeCost(Items.EMERALD, emeralds), new ItemStackTemplate(item.asItem(), count), maxUses, xp, DISCOUNT).build();
    }

    /** The villager sells a drink with the given checks (and, if aged, years in oak on the label). */
    private static VillagerTrade sellDrink(Drinks.Drink drink, BrewQuality quality, int years, int emeralds, int maxUses, int xp) {
        CompoundTag brew = new CompoundTag();
        brew.put(BrewQuality.TAG, quality.save());
        if (years > 0) {
            brew.putInt(DrinkItem.AGE, years);
            brew.putString(DrinkItem.WOOD, CaskWood.OAK.id());
        }
        return VillagerTrade.builder(new TradeCost(Items.EMERALD, emeralds), new ItemStackTemplate(drink.item().get(), BrewData.patch(brew)),
                maxUses, xp, DISCOUNT).build();
    }

    /**
     * The villager buys a drink with at least cultured yeast and the right temperature (3 stars), or with every check (5
     * stars). A trade matches only the components it lists, so a better drink is accepted too.
     */
    private static VillagerTrade buyDrink(Drinks.Drink drink, boolean fiveStars, int emeralds, int maxUses, int xp) {
        Item item = drink.item().get();
        DataComponentExactPredicate.Builder checks = DataComponentExactPredicate.builder()
                .expect(ModComponents.QUALITY_YEAST.get(), Unit.INSTANCE)
                .expect(ModComponents.QUALITY_TEMPERATURE.get(), Unit.INSTANCE);
        if (fiveStars) {
            checks.expect(ModComponents.QUALITY_CRAFT.get(), Unit.INSTANCE).expect(ModComponents.QUALITY_AGED.get(), Unit.INSTANCE);
        }
        return VillagerTrade.builder(new TradeCost(item.builtInRegistryHolder(), ContextIntProviders.exactly(1), checks.build()),
                new ItemStackTemplate(Items.EMERALD, emeralds), maxUses, xp, DISCOUNT).build();
    }

    static void trades(BootstrapContext<VillagerTrade> context) {
        for (Trade trade : ALL) context.register(trade.key(), trade.trade().get());
    }

    static void tradeSets(BootstrapContext<TradeSet> context) {
        for (String profession : PROFESSIONS) {
            for (int level = 1; level <= 5; level++) {
                ResourceKey<TradeSet> key = ModVillagers.tradeSet(profession, level);
                context.register(key, new TradeSet(context.lookup(Registries.VILLAGER_TRADE).getOrThrow(levelTag(profession, level)),
                        ContextIntProviders.exactly(OFFERED), false, Optional.of(key.identifier().withPrefix("trade_set/"))));
            }
        }
    }

    /** Which trades belong to which level (and the wandering trader's to vanilla's pool). */
    public static class Tags extends TagsProvider<VillagerTrade> {
        public Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, Registries.VILLAGER_TRADE, lookup, SeedToCellar.MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            for (Trade trade : ALL) tag(trade.tag()).add(trade.key());
        }
    }

    private ModTrades() {}
}
