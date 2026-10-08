package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FertileHarvestModifier;
import io.github.spencerharris192.seedtocellar.farming.StrawModifier;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier;
import io.github.spencerharris192.seedtocellar.world.ChestLootModifier;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import net.minecraft.world.item.Item;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/** Additions to vanilla loot, without replacing vanilla loot tables. */
public class ModLootModifierProvider extends GlobalLootModifierProvider {
    private static final int PRIORITY = IGlobalLootModifier.DEFAULT_PRIORITY;

    public ModLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SeedToCellar.MOD_ID);
    }

    /** All of these conditions (none: always). */
    private static Optional<Holder<LootItemCondition>> when(LootItemCondition.Builder... conditions) {
        if (conditions.length == 0) return Optional.empty();
        if (conditions.length == 1) return Optional.of(Holder.direct(conditions[0].build()));
        return Optional.of(Holder.direct(AllOfCondition.allOf(conditions).build()));
    }

    @Override
    protected void start() {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
        chestLoot();
        // Fertile Farmland: extra harvests and fertility use, for every crop (see FertileHarvestModifier).
        add("fertile_harvest", new FertileHarvestModifier(when(), PRIORITY));
        // Straw: ripe grain harvested with a sickle (see StrawModifier).
        add("straw", new StrawModifier(when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ModTags.Items.SICKLES))), PRIORITY,
                ModItems.STRAW.get()));

        // Grass and tall grass: a small chance of seeds (5% each for barley, oats, and rye in cold
        // biomes; the grassSeedChances settings), unless cut with shears. Vanilla wheat seeds drop 12.5% of the time.
        for (Crop crop : Crops.all()) {
            if (crop.grassDrop == null) continue;
            add(crop.seedId + "_from_grass", new GrassSeedsModifier(when(
                    AnyOfCondition.anyOf(
                            LootTableIdCondition.builder(SeedToCellar.rl("minecraft", "blocks/short_grass")),
                            LootTableIdCondition.builder(SeedToCellar.rl("minecraft", "blocks/tall_grass"))),
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, Items.SHEARS)).invert()
            ), PRIORITY, crop.seeds(), crop.grassDrop.coldOnly(), crop.grassDrop.chance()));
        }
    }

    /**
     * Our finds in vanilla's chests (GDD section 18.4): each village house holds its region's seeds (a second route to
     * them, after the wild); desert pyramids hide agave pups and jungle temples vanilla and coffee; shipwrecks and buried
     * treasure keep rum that's been aging since, and woodland mansions their cellars' old wine and spirits; an igloo keeps a
     * bottle of vodka against the cold.
     */
    private void chestLoot() {
        chest("village_plains_house", "village/village_plains_house", 0.6F, 2, seeds(Crops.BARLEY, 4, 2, 5), seeds(Crops.OATS, 3, 2, 4),
                seeds(Crops.ONION, 2, 1, 3), seeds(Crops.CABBAGE, 2, 1, 3), find(ModItems.HOP_RHIZOME.get(), 1, 1, 2),
                find(FruitTrees.APPLE.saplingItem(), 1, 1, 1));
        chest("village_savanna_house", "village/village_savanna_house", 0.6F, 2, seeds(Crops.SORGHUM, 4, 2, 5), seeds(Crops.CORN, 3, 2, 4),
                seeds(Crops.CHILI, 2, 1, 3), seeds(Crops.RED_GRAPE, 2, 1, 2), find(FruitTrees.PEACH.saplingItem(), 1, 1, 1));
        chest("village_taiga_house", "village/village_taiga_house", 0.6F, 2, seeds(Crops.RYE, 4, 2, 5), seeds(Crops.BARLEY, 3, 2, 4),
                seeds(Crops.GARLIC, 2, 1, 3), seeds(Crops.BLUEBERRY, 2, 1, 3), seeds(Crops.JUNIPER, 1, 1, 2));
        chest("village_snowy_house", "village/village_snowy_house", 0.6F, 2, seeds(Crops.RYE, 4, 2, 5), seeds(Crops.CABBAGE, 2, 1, 3),
                seeds(Crops.CRANBERRY, 2, 1, 3), seeds(Crops.JUNIPER, 2, 1, 2));
        chest("village_desert_house", "village/village_desert_house", 0.6F, 2, seeds(Crops.AGAVE, 3, 1, 2), seeds(Crops.CHILI, 3, 1, 3),
                seeds(Crops.SORGHUM, 2, 2, 4), seeds(Crops.CORIANDER, 2, 1, 3));
        chest("desert_pyramid", "desert_pyramid", 0.6F, 1, seeds(Crops.AGAVE, 1, 2, 4));
        chest("jungle_temple", "jungle_temple", 0.7F, 2, find(ModItems.VANILLA_POD.get(), 3, 2, 3), seeds(Crops.COFFEE, 3, 2, 4),
                seeds(Crops.GINGER, 2, 1, 3));
        chest("shipwreck_supply", "shipwreck_supply", 0.4F, 1, brewed(Drinks.RUM, 1, 1, 8));
        chest("shipwreck_treasure", "shipwreck_treasure", 0.35F, 1, brewed(Drinks.RUM, 1, 1, 12));
        chest("buried_treasure", "buried_treasure", 0.5F, 1, new ChestLootModifier.Entry(Drinks.RUM.item().get(), 1, 1, 2, true, 12));
        chest("woodland_mansion", "woodland_mansion", 0.5F, 2, brewed(Drinks.RED_WINE, 2, 1, 10), brewed(Drinks.BRANDY, 1, 1, 12),
                brewed(Drinks.MALT_WHISKEY, 1, 1, 15));
        chest("igloo_chest", "igloo_chest", 0.5F, 1, brewed(Drinks.VODKA, 1, 1, 0));
    }

    private void chest(String name, String table, float chance, int rolls, ChestLootModifier.Entry... entries) {
        add("chests/" + name, new ChestLootModifier(when(LootTableIdCondition.builder(SeedToCellar.rl("minecraft", "chests/" + table))),
                PRIORITY, chance, rolls, List.of(entries)));
    }

    private static ChestLootModifier.Entry find(Item item, int weight, int min, int max) {
        return new ChestLootModifier.Entry(item, weight, min, max, false, 0);
    }

    private static ChestLootModifier.Entry seeds(Crop crop, int weight, int min, int max) {
        return find(crop.seeds(), weight, min, max);
    }

    private static ChestLootModifier.Entry brewed(Drinks.Drink drink, int weight, int count, int maxYears) {
        return new ChestLootModifier.Entry(drink.item().get(), weight, count, count, true, maxYears);
    }
}
