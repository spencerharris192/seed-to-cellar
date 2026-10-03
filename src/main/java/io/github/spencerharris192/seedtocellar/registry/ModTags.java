package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Tag keys. We join every common convention (see GDD section 22.1) so other mods'
 * ingredients work in our recipes and ours work in theirs.
 */
public final class ModTags {
    public static final class Items {
        public static final TagKey<Item> CROPS = forge("crops");
        public static final TagKey<Item> CROPS_BARLEY = crop("barley");
        public static final TagKey<Item> CROPS_HOPS = crop("hops");
        public static final TagKey<Item> GRAIN = forge("grain");
        public static final TagKey<Item> VEGETABLES = forge("vegetables");
        public static final TagKey<Item> FRUITS = forge("fruits");
        /** Farmer's Delight's berry tag (sweet berries and the like). */
        public static final TagKey<Item> BERRIES = forge("berries");
        public static final TagKey<Item> SEEDS = forge("seeds");
        /** Leftovers the Compost Bin takes on top of everything the vanilla composter takes. */
        public static final TagKey<Item> COMPOSTABLES = ItemTags.create(SeedToCellar.id("compostables"));
        public static final TagKey<Item> SEEDS_HOPS = seeds("hops");
        /** Every served drink: mugs, wine bottles and bottles of juice. */
        public static final TagKey<Item> DRINKS = ItemTags.create(SeedToCellar.id("drinks"));
        /** Used like sugar in cooking and baking: sugar and sorghum syrup (other mods' syrups can join). */
        public static final TagKey<Item> SWEETENERS = ItemTags.create(SeedToCellar.id("sweeteners"));
        /** Every wine, fruit wine and mead (the Good Year advancement; other mods' wines can join). */
        public static final TagKey<Item> WINES = ItemTags.create(SeedToCellar.id("wines"));
        /** What a Bottle Shelf holds: every served drink (mugs, wine bottles, bottles of juice; other mods can join). */
        public static final TagKey<Item> SHELF_DRINKS = ItemTags.create(SeedToCellar.id("shelf_drinks"));
        /** Bottles the Wine Rack holds (our wines and meads; other mods' bottles can join). */
        public static final TagKey<Item> WINE_RACK_BOTTLES = ItemTags.create(SeedToCellar.id("wine_rack_bottles"));
        /** What hangs on a Mug Rack: empty mugs. */
        public static final TagKey<Item> MUG_RACK_ITEMS = ItemTags.create(SeedToCellar.id("mug_rack_items"));
        /** Hops accepted by the kettle's boil (dried hops; other mods' dried hops can join). */
        public static final TagKey<Item> BOIL_HOPS = ItemTags.create(SeedToCellar.id("boil_hops"));
        public static final TagKey<Item> FLOUR = forge("flour");
        public static final TagKey<Item> FLOUR_WHEAT = forge("flour/wheat");
        public static final TagKey<Item> FLOUR_RYE = forge("flour/rye");
        public static final TagKey<Item> CORNMEAL = forge("flour/corn");
        public static final TagKey<Item> DOUGH = forge("dough");
        public static final TagKey<Item> DOUGH_WHEAT = forge("dough/wheat");
        public static final TagKey<Item> DOUGH_RYE = forge("dough/rye");
        public static final TagKey<Item> BREAD = forge("bread");
        /** Every beer (a mug of any ale), for cooking and baking with ale. */
        public static final TagKey<Item> ALES = ItemTags.create(SeedToCellar.id("ales"));
        /** Every jam (jam toast takes any). */
        public static final TagKey<Item> JAMS = ItemTags.create(SeedToCellar.id("jams"));
        /** Berries the Drying Rack turns into dried berries. */
        public static final TagKey<Item> DRYABLE_BERRIES = ItemTags.create(SeedToCellar.id("dryable_berries"));
        /** Red and white grapes (forge:fruits/grapes: the name other mods use for any grape). */
        public static final TagKey<Item> GRAPES = forge("fruits/grapes");
        /** Any dried fruit: dried berries, raisins (granola takes any). */
        public static final TagKey<Item> DRIED_FRUITS = ItemTags.create(SeedToCellar.id("dried_fruits"));
        /** Chili for cooking: fresh (forge:crops/chili) or dried. */
        public static final TagKey<Item> CHILIES = ItemTags.create(SeedToCellar.id("chilies"));
        /** Cooked rice for rice balls: ours, and Farmer's Delight's (its Cooking Pot cooks our rice into its own). */
        public static final TagKey<Item> COOKED_RICE = ItemTags.create(SeedToCellar.id("cooked_rice"));
        /** Croptopia's flat naming style. */
        public static final TagKey<Item> HOPS_FLAT = forge("hops");
        /** Sickles, ours and other mods' (a ripe grain harvested with one gives straw). */
        public static final TagKey<Item> SICKLES = forge("tools/sickles");
        public static final TagKey<Item> TOOLS = forge("tools");
        /** Straw for thatch (Farmer's Delight's straw joins if present). */
        public static final TagKey<Item> STRAW = ItemTags.create(SeedToCellar.id("straw"));
        /** Raw meats the Drying Rack turns into jerky. */
        public static final TagKey<Item> JERKY_MEATS = ItemTags.create(SeedToCellar.id("jerky_meats"));
        public static final TagKey<Item> STORAGE_BLOCKS = forge("storage_blocks");
        /** Every spirit (the distilling advancements; other mods' spirits can join). */
        public static final TagKey<Item> SPIRITS = ItemTags.create(SeedToCellar.id("spirits"));
        /** Every liqueur and bitters (other mods' can join). */
        public static final TagKey<Item> LIQUEURS = ItemTags.create(SeedToCellar.id("liqueurs"));
        /** What goes in the Pot Still's filter: charcoal (other mods' activated charcoal can join). */
        public static final TagKey<Item> FILTER_CHARCOAL = ItemTags.create(SeedToCellar.id("filter_charcoal"));
        /** What the Gin Basket holds: juniper, coriander, citrus peel, herbs... (GDD section 22.1; other mods' can join). */
        public static final TagKey<Item> BOTANICALS = ItemTags.create(SeedToCellar.id("botanicals"));

        /** forge:storage_blocks/<name>: a block packed with the item (a bale of nine, a sack or crate of eight). */
        public static TagKey<Item> storage(String name) {
            return forge("storage_blocks/" + name);
        }

        /** forge:crops/<name>: the harvested crop. */
        public static TagKey<Item> crop(String name) {
            return forge("crops/" + name);
        }

        /** forge:seeds/<name>: what you plant. */
        public static TagKey<Item> seeds(String name) {
            return forge("seeds/" + name);
        }

        /** forge:grain/<name> (Farmer's Delight style). */
        public static TagKey<Item> grain(String name) {
            return forge("grain/" + name);
        }

        /** forge:vegetables/<name> (Farmer's Delight style). */
        public static TagKey<Item> vegetable(String name) {
            return forge("vegetables/" + name);
        }

        /** forge:fruits/<name> (HarvestCraft, Simple Farming style). */
        public static TagKey<Item> fruit(String name) {
            return forge("fruits/" + name);
        }

        /** forge:<name>, Croptopia's flat style. */
        public static TagKey<Item> flat(String name) {
            return forge(name);
        }

        private static TagKey<Item> forge(String path) {
            return ItemTags.create(SeedToCellar.rl("forge", path));
        }

        private Items() {}
    }

    public static final class Blocks {
        /** Blocks that heat a kettle or still above them (lit campfires, fire, magma, lava...). */
        public static final TagKey<Block> HEAT_SOURCES = mod("heat_sources");
        /** Blocks that chill a fermenting vessel next to them (ice, snow...). */
        public static final TagKey<Block> COOLING = mod("cooling_blocks");
        /** Soil that gives Fertile Farmland's extra harvests without ever wearing out (Farmer's Delight's Rich Soil Farmland). */
        public static final TagKey<Block> ALWAYS_FERTILE = mod("always_fertile");
        /** Where desert succulents (agave) grow: sand, red sand, coarse dirt, terracotta. */
        public static final TagKey<Block> SUCCULENT_SOIL = mod("succulent_soil");
        /** What a sickle cuts quickly: plants, crops, leaves. */
        public static final TagKey<Block> MINEABLE_WITH_SICKLE = mod("mineable/sickle");
        /** Ripe crops here give Straw when harvested with a sickle: the grains (not corn). */
        public static final TagKey<Block> STRAW_CROPS = mod("straw_crops");
        public static final TagKey<Block> STORAGE_BLOCKS = BlockTags.create(SeedToCellar.rl("forge", "storage_blocks"));

        /** forge:storage_blocks/<name> (block version). */
        public static TagKey<Block> storage(String name) {
            return BlockTags.create(SeedToCellar.rl("forge", "storage_blocks/" + name));
        }

        private static TagKey<Block> mod(String path) {
            return BlockTags.create(SeedToCellar.id(path));
        }

        private Blocks() {}
    }

    public static final class Fluids {
        /** Every beer ("any ale" in cooking). */
        public static final TagKey<Fluid> ALES = mod("ales");
        /** What the Brew Kettle's tank accepts: water, worts, milk, ales. */
        public static final TagKey<Fluid> KETTLE_LIQUIDS = mod("kettle_liquids");
        /** Left in an open jar, these sour into vinegar: beers, wines, cider, perry and mead. */
        public static final TagKey<Fluid> SOURS_TO_VINEGAR = mod("sours_to_vinegar");
        /** Sweetened in the kettle into lemonade (other mods' lemon juice can join). */
        public static final TagKey<Fluid> LEMON_JUICE = mod("lemon_juice");
        /** Every spirit: any of them can go through the still again. */
        public static final TagKey<Fluid> SPIRITS = mod("spirits");
        /** Grape wines (red, white, rosé): distilled into brandy. */
        public static final TagKey<Fluid> GRAPE_WINES = mod("grape_wines");
        /** Grain spirits (malt whiskey, bourbon): a third run makes them vodka. */
        public static final TagKey<Fluid> GRAIN_SPIRITS = mod("grain_spirits");
        /** Washes and spirits that the still's charcoal filter makes into vodka. */
        public static final TagKey<Fluid> VODKA_SOURCES = mod("vodka_sources");
        /** Spirits a fruit liqueur steeps from (vodka, brandy; other mods' can join). */
        public static final TagKey<Fluid> LIQUEUR_BASES = mod("liqueur_bases");
        /** Poured from a bucket onto farmland, these feed the soil (stillage). */
        public static final TagKey<Fluid> FERTILIZERS = mod("fertilizers");

        private static TagKey<Fluid> mod(String path) {
            return FluidTags.create(SeedToCellar.id(path));
        }

        private Fluids() {}
    }

    public static final class Biomes {
        /** Where each wild plant spawns. Pack makers can change these with a datapack. */
        public static final TagKey<Biome> HAS_WILD_HOPS = mod("has_wild/hops");
        /** Where vanilla climbs the jungle trees. */
        public static final TagKey<Biome> HAS_WILD_VANILLA = mod("has_wild/vanilla");
        /** Where wild orchards grow: temperate ones (apple, cherry, plum, pear) and warm ones (peach, lemon, orange, olive). */
        public static final TagKey<Biome> HAS_ORCHARD_TEMPERATE = mod("has_wild/orchard_temperate");
        public static final TagKey<Biome> HAS_ORCHARD_WARM = mod("has_wild/orchard_warm");

        private static TagKey<Biome> mod(String path) {
            return TagKey.create(Registries.BIOME, SeedToCellar.id(path));
        }

        private Biomes() {}
    }

    private ModTags() {}
}
