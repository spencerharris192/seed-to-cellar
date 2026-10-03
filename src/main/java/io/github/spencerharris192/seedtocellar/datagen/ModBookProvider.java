package io.github.spencerharris192.seedtocellar.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The Brewer's Almanac (GDD section 19), our Patchouli guidebook: chapters that follow the advancement tree, from finding a
 * wild crop to aging a whiskey. Patchouli reads book.json from data and the chapters from assets; without Patchouli these
 * files do nothing. Written here so every item it shows is a real one (a typo fails the build, not the book).
 */
public class ModBookProvider implements DataProvider {
    public static final String BOOK = "brewers_almanac";
    private final PackOutput output;
    private final List<JsonObject> categories = new ArrayList<>();
    private final List<String> categoryIds = new ArrayList<>();
    private final List<JsonObject> entries = new ArrayList<>();
    private final List<String> entryPaths = new ArrayList<>();

    public ModBookProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public String getName() {
        return "Patchouli book: The Brewer's Almanac";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        write();
        List<CompletableFuture<?>> saves = new ArrayList<>();
        Path data = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(SeedToCellar.MOD_ID + "/patchouli_books/" + BOOK);
        saves.add(DataProvider.saveStable(cache, book(), data.resolve("book.json")));
        Path assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(SeedToCellar.MOD_ID + "/patchouli_books/" + BOOK + "/en_us");
        for (int i = 0; i < categories.size(); i++) {
            saves.add(DataProvider.saveStable(cache, categories.get(i), assets.resolve("categories/" + categoryIds.get(i) + ".json")));
        }
        for (int i = 0; i < entries.size(); i++) {
            saves.add(DataProvider.saveStable(cache, entries.get(i), assets.resolve("entries/" + entryPaths.get(i) + ".json")));
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static JsonObject book() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "The Brewer's Almanac");
        json.addProperty("landing_text", "Everything a farm can become: grains and fruit, breads and pies, beer, wine and spirits, "
                + "and the cellar that makes them better with age.$(br2)Every chapter follows an advancement branch. "
                + "Hover over any of our items for a hint about what it is and what to do next.");
        json.addProperty("version", 1);
        json.addProperty("subtitle", "Seed to Cellar");
        json.addProperty("creative_tab", SeedToCellar.MOD_ID + ":main");
        json.addProperty("model", "patchouli:book_brown");
        json.addProperty("book_texture", "patchouli:textures/gui/book_brown.png");
        json.addProperty("filler_texture", SeedToCellar.MOD_ID + ":textures/gui/almanac_filler.png");   // a page left blank
        json.addProperty("show_progress", false);
        json.addProperty("use_resource_pack", true);
        json.addProperty("i18n", false);
        return json;
    }

    // --- the chapters ---------------------------------------------------------------------------------------------

    private void write() {
        category("basics", "Getting Started", "Where everything starts: a wild plant, a Hydrometer and a few stars.",
                Crops.BARLEY.produce(), 0);
        entry("basics", "welcome", "Welcome", Crops.BARLEY.produce(),
                text("One farm feeds a web of foods and drinks. Grow grain, malt it, brew it, then distil it and age it in a "
                        + "cask. Every step can be done well or badly, and each drink keeps a record of it in its "
                        + "$(l:basics/stars)stars$(/l)."),
                text("Hover over any of our items: it says what it is and what to do next. The $(l:basics/hydrometer)"
                        + "Hydrometer$(/l), Jade or The One Probe shows what a station is doing.$(br2)These chapters follow "
                        + "the advancements, from a wild crop to a cellar of aged spirits."),
                scene("Seed to Cellar", "Barley in the field, a kettle over a fire, a cask to age it in: the whole story.",
                        new String[][]{{"b K  "}, {"f 0 c"}}, "b", M + "barley_crop[age=7]", "f", "minecraft:farmland[moisture=7]",
                        "K", M + "brew_kettle", "0", "minecraft:campfire[lit=true]", "c", M + "oak_cask[facing=south,tap=true]"));
        entry("basics", "finding_crops", "Finding Crops", Crops.RYE.seeds(),
                text("Most crops grow wild: look for small patches in fitting biomes, and break them for seeds (a seed's "
                        + "tooltip says where its plant grows). Breaking grass sometimes gives $(item)barley$() and "
                        + "$(item)oat$() seeds, and $(item)rye$() in cold biomes, so beer is possible on day one."),
                text("Village houses keep their region's seeds in their chests. Desert pyramids hide $(item)agave pups$(), "
                        + "jungle temples $(item)vanilla$() and $(item)coffee$(). The wandering trader sells the exotic crops "
                        + "too."));
        entry("basics", "climate", "Climate", Crops.CHILI.seeds(),
                text("Each crop has a favorite climate (Cold, Temperate, Warm or Hot), shown on its seeds. Elsewhere it grows "
                        + "at half speed, but it never stops."),
                text("Glass within 8 blocks above a crop makes a greenhouse: it counts as Warm.$(br2)With Serene Seasons "
                        + "installed, its seasons decide instead: each crop grows best in its own seasons."));
        entry("basics", "hydrometer", "The Hydrometer", ModItems.HYDROMETER.get(),
                spotlight(ModItems.HYDROMETER.get(), "Right-click a station, cask, jar or crop with it for a read-out: progress, "
                        + "temperature, the stars a drink will get, its age and wood; for a crop, its growth, climate and "
                        + "soil."),
                crafting("hydrometer", "Jade and The One Probe show the same at a glance."));
        entry("basics", "stars", "Stars", Drinks.OLD_ALE.item().get(),
                text("A drink earns up to five stars: one to start, then one each for $(item)cultured yeast$(), the "
                        + "$(item)right temperature$(), its $(item)craft step$() and $(item)aging$(). Hold Shift over a drink to "
                        + "see which it earned."),
                text("The craft step: a day's rest in a cask or keg for beer and wine; a second run through the still for "
                        + "spirits. Aging: years in a cask of the right wood.$(br2)Better stars mean longer effects and gentler "
                        + "hangovers."));

        category("farming", "The Farm", "Harvesting, compost, trellises, paddies, bushes, orchards and exotic crops.",
                ModItems.IRON_SICKLE.get(), 1);
        entry("farming", "harvesting", "Harvesting", ModItems.IRON_SICKLE.get(),
                text("Right-click a ripe crop to harvest it and replant it. A $(item)Sickle$() harvests every ripe crop in a "
                        + "3x3 at once, leaves unripe ones alone, takes Fortune, and gives $(item)straw$() from grain."),
                crafting("iron_sickle", "Wood, stone, iron, gold, diamond or netherite."));
        entry("farming", "fertile_farmland", "Compost", ModItems.COMPOST.get(),
                text("A $(item)Compost Bin$() takes 16 leftovers (spent grain, pomace, bran, plant scraps). Full, it turns "
                        + "them into 4 $(item)Compost$() in a day. Hoppers can fill and empty it."),
                text("Use compost on farmland to make $(item)Fertile Farmland$(): crops on it grow about half again as fast "
                        + "and often give an extra harvest. It wears out slowly. A bucket of the still's "
                        + "$(l:distilling/pot_still)stillage$(/l) fertilizes farmland too."),
                scene("Compost", "A full Compost Bin, and barley and oats ripe on the fertile farmland it made.",
                        new String[][]{{" bo"}, {"0FF"}}, "0", M + "compost_bin", "F", M + "fertile_farmland[fertility=3,moisture=7]",
                        "b", M + "barley_crop[age=7]", "o", M + "oat_crop[age=7]"),
                crafting("compost_bin", null));
        entry("farming", "trellis", "Hops and Grapes", ModItems.HOP_RHIZOME.get(),
                text("Hops and grapes climb a $(item)Trellis$(). Stack trellises on soil and plant on the bottom one: a "
                        + "$(item)hop rhizome$() or a $(item)grape cutting$(). Pick the cones or bunches when they hang ripe; "
                        + "the vine stays. Shears prune grape vines for $(item)grape leaves$()."),
                scene("On the Trellis", "Hops hanging with ripe cones, and a grape vine with its bunches.",
                        new String[][]{{"HG"}, {"H0"}, {"DD"}}, "H", M + "hops[age=5,axis=x]", "G", M + "red_grape_vine[age=5,axis=x]",
                        "0", M + "red_grape_vine[age=5,axis=x]", "D", "minecraft:grass_block"),
                crafting("trellis", null));
        entry("farming", "paddy", "Rice", Crops.RICE.seeds(),
                text("Rice grows in water: plant it in water one block deep, over dirt or farmland. Its tops show above the "
                        + "water as it ripens."));
        entry("farming", "bushes", "Bushes and Herbs", Crops.BLUEBERRY.produce(),
                text("Berries (blueberry, blackberry, elderberry, cranberry, juniper), tomatoes, chili, cucumbers and coffee "
                        + "grow as bushes on grass or dirt; cranberries want water beside them. Pick them ripe with a "
                        + "right-click, and they grow back."),
                text("Mint, lavender and wormwood are herbs: plant them on grass, dirt or farmland, and pick them the same "
                        + "way. Elderberry bushes flower first: pick the $(item)elderflowers$(), or wait for the berries."),
                scene("Ripe Bushes", "Blueberries, blackberries and chilies, ready to pick.",
                        new String[][]{{"BKC"}, {"D0D"}}, "B", M + "blueberry_bush[age=3]", "K", M + "blackberry_bush[age=3]",
                        "C", M + "chili_bush[age=3]", "D", "minecraft:grass_block", "0", "minecraft:grass_block"));
        entry("farming", "exotic", "Exotic Crops", Crops.AGAVE.seeds(),
                text("$(item)Agave$() grows on sand, red sand, coarse dirt or terracotta, with no water, slowly. When it sends "
                        + "up its tall yellow flower spike it's ripe: harvest its heart, and it replants itself. Broken in "
                        + "flower, it gives pups too."),
                text("$(item)Vanilla$() climbs jungle logs: plant a pod on a log's side. It flowers, then hangs with green "
                        + "pods to pick. Cure them on a Drying Rack.$(br2)Coffee is a bush; lemons, oranges and olives grow on "
                        + "trees."));
        entry("farming", "orchards", "Fruit Trees", FruitTrees.APPLE.saplingItem(),
                text("Eight fruit trees grow from saplings found on wild trees and in wild orchards, or traded. They "
                        + "blossom, set fruit and ripen on the leaves: right-click ripe leaves to pick a fruit, and they bear "
                        + "again. Ripe fruit falls by itself in time."),
                text("Fruit packs into $(item)crates$() for storage. Apples, pears, cherries, plums and peaches make juice, "
                        + "wine and brandy; lemons and oranges peel for liqueurs and marmalade; olives press into oil."),
                scene("An Apple Tree", "Ripe apples on the leaves: right-click to pick one.",
                        new String[][]{{" L ", "LLL", " L "}, {"LLL", "LOL", "LLL"}, {" L ", "LOL", " L "}, {"   ", " O ", "   "},
                                {"DDD", "D0D", "DDD"}},
                        "L", M + "apple_leaves[age=3]", "O", "minecraft:oak_log", "D", "minecraft:grass_block", "0", "minecraft:grass_block"));
        entry("farming", "drying", "The Drying Rack", ModItems.DRYING_RACK.get(),
                text("Hang hops, fruit, chili, meat or vanilla on a $(item)Drying Rack$(). Sun on the rack dries twice as "
                        + "fast; rain pauses it. Dried hops are what beer wants."),
                crafting("drying_rack", null));

        category("kitchen", "The Kitchen", "Kettle cooking, breads, the Preserving Jar and foods to place.", Items.BREAD, 2);
        entry("kitchen", "kettle_cooking", "Kettle Cooking", ModItems.BREW_KETTLE.get(),
                text("The $(item)Brew Kettle$() cooks as well as brews. Set it over heat (a lit campfire, fire, magma or "
                        + "lava), add water and the ingredients, and soups, stews, jams and syrups come out. Bowls or bottles "
                        + "go in its container slot."),
                crafting("brew_kettle", null));
        entry("kitchen", "breads", "Breads", Items.BREAD,
                text("Mill grain into flour, mix flour with a water bucket at a crafting table for dough, and bake it in a "
                        + "furnace or smoker. Rye, cornbread, spent-grain bread and sourdough each have their own dough; beer "
                        + "bread takes an ale."));
        entry("kitchen", "jar", "The Preserving Jar", ModItems.PRESERVING_JAR.get(),
                text("The $(item)Preserving Jar$() works slowly with its lid closed: pickles in vinegar, sauerkraut, kimchi, "
                        + "a $(item)sourdough starter$() from flour and water, and the cultures brewers need. Right-click to "
                        + "open or close the lid."),
                text("Where it stands matters for some: koji grows only somewhere warm, lager yeast only somewhere cold "
                        + "(beside ice). Wine left in an open jar with no yeast turns to vinegar."),
                crafting("preserving_jar", null));
        entry("kitchen", "placed_foods", "Foods to Place", ModItems.HARVEST_FEAST.get(),
                text("Pies, the Black Forest Cake and the Harvest Feast are placed on a surface and eaten a slice or a "
                        + "serving at a time, by anyone at the table."),
                scene("A Laid Table", "An apple pie, a Harvest Feast and a Black Forest Cake, set out to share.",
                        new String[][]{{"AHC"}, {"T0T"}}, "A", M + "apple_pie", "H", M + "harvest_feast[facing=south]",
                        "C", M + "black_forest_cake", "T", "minecraft:spruce_planks", "0", "minecraft:spruce_planks"));

        category("brewing", "Brewing Beer", "From barley to a pint: malting, mashing, yeast and fermenting.",
                Drinks.PALE_ALE.item().get(), 3);
        entry("brewing", "malting", "Malting", ModItems.MALTING_TUB.get(),
                text("Fill a $(item)Malting Tub$() with water and barley (or wheat). The grain steeps, the water drains, and "
                        + "it sprouts into $(item)green malt$()."),
                text("Roast green malt in the $(item)Kiln$(): Light gives $(item)pale malt$(), Medium $(item)amber$(), Dark "
                        + "$(item)black$(). The kiln burns its own fuel. It also dries hops (Light) and roasts coffee and "
                        + "agave (Medium)."),
                scene("Grain to Grist", "Steep and sprout in the Malting Tub, roast in the Kiln, grind on the Millstone.",
                        new String[][]{{"T0M"}}, "T", M + "malting_tub[contents=done]", "0", M + "kiln[facing=south,lit=true]",
                        "M", M + "millstone"),
                crafting("malting_tub", null), crafting("kiln", null));
        entry("brewing", "milling", "Milling", ModItems.MILLSTONE.get(),
                text("Grind malt into $(item)grist$() on the $(item)Millstone$(): put it in, then right-click to crank (or "
                        + "pulse it with redstone). It also grinds flour, polished rice (with bran), cornmeal and coffee. "
                        + "Hoppers feed it."),
                crafting("millstone", null));
        entry("brewing", "mashing", "Mash and Boil", ModItems.PALE_GRIST.get(),
                text("Over heat, fill the $(item)Brew Kettle$() with water and add grist: 2 a bucket make a normal wort, 4 a "
                        + "strong one. It mashes into $(item)sweet wort$(), leaving $(item)spent grain$() (compost or bread)."),
                text("Add 1 $(item)dried hops$() a bucket and it boils into $(item)hopped wort$(). The mix of malts you used "
                        + "(the malt bill) rides along and decides the $(l:brewing/styles)beer's style$(/l). Unhopped sweet "
                        + "wort ferments into Plain Ale."),
                scene("Over the Fire", "A Brew Kettle on a lit campfire: fire, lava or magma under it work too.",
                        new String[][]{{"K"}, {"0"}}, "K", M + "brew_kettle", "0", "minecraft:campfire[lit=true]"));
        entry("brewing", "yeast", "Yeast", ModItems.ALE_YEAST.get(),
                text("Almost anything ferments without yeast, on the wild yeast in the air: slower, and a star short (only "
                        + "lager needs its own). Each batch "
                        + "leaves its yeast in the vat's bottom slot as $(item)lees$(): one after a wild ferment, two when you "
                        + "added one."),
                text("So your first wild beer gives you $(item)Ale Yeast$(), your first wild juice $(item)Wine Yeast$(). Ale "
                        + "yeast also grows from a $(item)sourdough starter$() in a jar with a bottle of sweet wort; "
                        + "$(item)Lager Yeast$() from ale yeast in a jar beside ice."));
        entry("brewing", "fermenting", "Fermenting", ModItems.FERMENTING_VAT.get(),
                text("Pour wort into a $(item)Fermenting Vat$() and add $(l:brewing/yeast)yeast$(/l): it ferments in about "
                        + "a day. The vat's screen shows the temperature and whether it's right for what's inside."),
                text("Temperature comes from the biome, a step cooler out of the open sky (indoors or underground), Cold "
                        + "beside ice and Warm beside fire. Ales like Mild, lager Cold, white wines and cider Cool."),
                text("With Serene Seasons, winter makes a vat a step cooler and summer a step warmer, except in tropical "
                        + "biomes (deserts, savannas, jungles), whose seasons are wet and dry instead."),
                scene("Fermenting Vats", "One open to pour in, one closed and working.",
                        new String[][]{{"V0"}}, "V", M + "fermenting_vat[open=true]", "0", M + "fermenting_vat"),
                crafting("fermenting_vat", null));
        entry("brewing", "styles", "Beer Styles", Drinks.STOUT.item().get(),
                text("$(item)Pale Ale$(): pale malt. $(item)Amber Ale$(): a quarter amber or more. $(item)Stout$(): a quarter "
                        + "black. $(item)Old Ale$(): strong, with amber; it ages. $(item)Wheat Beer$(): half wheat malt."),
                text("$(item)Lager$(): pale, fermented Cold with Lager Yeast. $(item)Plain Ale$(): no hops. Anything else is "
                        + "$(item)Table Beer$(), so nothing is ever wasted."));
        entry("brewing", "serving", "Serving", ModItems.KEG.get(),
                text("Pour beer into a $(item)Keg$() or cask and leave it a day: it conditions, earning its craft star. Fill "
                        + "$(item)mugs$() from the tap. Kegs keep their contents when you break them."),
                scene("On Tap", "Two tapped casks, and a keg on top to carry the beer away.",
                        new String[][]{{"k "}, {"0c"}}, "k", M + "keg[facing=south]", "0", M + "oak_cask[facing=south,tap=true]",
                        "c", M + "spruce_cask[facing=south,tap=true]"),
                crafting("keg", null), crafting("mug", null));

        category("winemaking", "The Winery", "Stomping, pressing, wine, sake, soft drinks and vinegar.", Drinks.RED_WINE.item().get(), 4);
        entry("winemaking", "crushing", "Stomping and Pressing", ModItems.CRUSHING_TUB.get(),
                text("Drop fruit into a $(item)Crushing Tub$() and jump on it: red grapes give $(item)must$(), skins and all, "
                        + "for red wine; other fruit gives juice."),
                text("The $(item)Fruit Press$() squeezes clear juice out of fruit (crank it, or pulse it with redstone), "
                        + "leaving pomace. It also presses sugar cane, sorghum, olives and roasted agave."),
                scene("The Winery", "A Crushing Tub to stomp in, and a Fruit Press beside it.",
                        new String[][]{{"T0"}}, "T", M + "crushing_tub", "0", M + "fruit_press[facing=south]"),
                crafting("crushing_tub", null), crafting("fruit_press", null));
        entry("winemaking", "wines", "Wines", Drinks.WHITE_WINE.item().get(),
                text("Ferment with $(item)Wine Yeast$(): red must makes $(item)red wine$() (Mild); white grape juice "
                        + "$(item)white wine$() and red grape juice $(item)rosé$() (Cool). Apple juice is $(item)cider$(), pear "
                        + "juice $(item)perry$()."),
                text("Stir 2 honey bottles a bucket into a heated kettle of water for $(item)honey water$(): it ferments into "
                        + "$(item)mead$() (Mild). Every other fruit juice makes its own fruit wine, fermented Cool."),
                crafting("wine_bottle", null));
        entry("winemaking", "sake", "Sake", Drinks.SAKE.item().get(),
                text("Mill rice into $(item)polished rice$() and steam it in a kettle of water (no bowl). Two steamed rice in "
                        + "a jar somewhere Warm grow into two $(item)koji rice$() in two days."),
                text("Stir 2 koji rice a bucket into a heated kettle of water for $(item)sake mash$(). Ferment it Cool with "
                        + "Wine Yeast, and bottle the $(item)sake$() in wine bottles."));
        entry("winemaking", "soft_drinks", "Soft Drinks", Drinks.LEMONADE.item().get(),
                text("Juice is a drink in itself: pour it into a vat and fill glass bottles from it. In a heated kettle, lemon "
                        + "juice and sugar make $(item)lemonade$(); water, elderflowers, sugar and a lemon "
                        + "$(item)elderflower cordial$(); red wine, an orange and sugar $(item)mulled wine$()."),
                text("Roast coffee beans in the Kiln (Medium), grind them, and brew them in a kettle of water with glass "
                        + "bottles: $(item)coffee$(). Ginger, sugar and ale yeast in a jar of water brew into $(item)ginger "
                        + "beer$()."));
        entry("winemaking", "vinegar", "Vinegar", ModItems.MOTHER_OF_VINEGAR.get(),
                text("Wine, cider or beer left in a Preserving Jar with no yeast and the lid open sours into "
                        + "$(item)vinegar$(), growing a $(item)Mother of Vinegar$() that makes more. Pickle with it."));

        category("distilling", "The Distillery", "The Pot Still, washes, gin and liqueurs.", ModItems.POT_STILL.get(), 5);
        entry("distilling", "pot_still", "The Pot Still", ModItems.POT_STILL.get(),
                text("The $(item)Pot Still$() stands two blocks tall over heat. Pour a wash into its pot (up to 4 buckets): "
                        + "a run boils off half of it as spirit into the glass safe, and the rest stays behind as "
                        + "$(item)stillage$()."),
                text("One run makes a drinkable spirit; press $(item)Run again$() on its screen for a second run, which "
                        + "earns the craft star and halves it again. Fill $(item)spirit bottles$() from it. An empty bucket "
                        + "takes the spirit, then the stillage: pour that on farmland as fertilizer."),
                text("Copper weathers. Left long enough, a Pot Still or Brew Kettle turns green in patches, like a copper "
                        + "block, and then all over. It works just the same. Wax it with $(item)honeycomb$() to keep the look "
                        + "it has; an $(item)axe$() takes the wax off, or else scrapes back a stage. Broken, it keeps both."),
                scene("Copper Weathers", "Pot Stills over campfires: fresh copper, weathered and oxidized.",
                        new String[][]{{"UVW"}, {"0ab"}, {"FFF"}},
                        "U", M + "pot_still[half=upper,facing=south]", "V", M + "pot_still[half=upper,facing=south,weathering=weathered]",
                        "W", M + "pot_still[half=upper,facing=south,weathering=oxidized]", "0", M + "pot_still[half=lower,facing=south]",
                        "a", M + "pot_still[half=lower,facing=south,weathering=weathered]",
                        "b", M + "pot_still[half=lower,facing=south,weathering=oxidized]", "F", "minecraft:campfire[lit=true]"),
                crafting("pot_still", null), crafting("spirit_bottle", null));
        entry("distilling", "washes", "Washes and Spirits", Drinks.MALT_WHISKEY.item().get(),
                text("$(item)Plain Ale$() distils into malt whiskey; wine into $(item)brandy$(); cider, perry, cherry, plum and "
                        + "peach wine into fruit brandies."),
                text("Mash $(item)cornmeal$() or $(item)potatoes$() in the kettle with some malt grist, half or more of the "
                        + "mash, and ferment it with ale yeast: a corn wash distils into $(item)bourbon$(), a potato wash into "
                        + "$(item)vodka$()."),
                text("Boil cane juice alone in the kettle into molasses, and ferment it into rum wash: $(item)rum$(). Roast "
                        + "agave hearts, press them and ferment the juice: $(item)tequila$(). Steep grape pomace in a kettle "
                        + "of water and ferment it with wine yeast: $(item)grappa$()."));
        entry("distilling", "gin", "Gin and the Filter", ModItems.GIN_BASKET.get(),
                text("Fit a $(item)Gin Basket$() to the still and fill it with $(item)juniper$() and other botanicals, then "
                        + "run vodka through it: $(item)gin$(). Juniper and three others earn its craft star."),
                text("Vodka earns its star from three runs, or one run through $(item)charcoal$() in the still's filter slot."),
                crafting("gin_basket", null));
        entry("distilling", "liqueurs", "Liqueurs", Drinks.LIMONCELLO.item().get(),
                text("Steep a spirit in a Preserving Jar with fruit or herbs and something sweet, lid closed: the whole jar "
                        + "becomes a liqueur, keeping the spirit's stars. Lemon peel and vodka: limoncello."),
                text("Spiced rum, aromatic bitters (a hangover cure), herbal liqueur, coffee liqueur, and a liqueur for most "
                        + "fruits. Whiskey steeped with apples and honey has a name of its own."));

        category("cellar", "The Cellar", "Casks, woods, aging and charring.", ModItems.CASKS.get(CaskWood.OAK).get(), 6);
        entry("cellar", "casks", "Casks", ModItems.CASKS.get(CaskWood.OAK).get(),
                text("A $(item)cask$() holds 16 buckets. Fit a $(item)Tap$() to its front to pour from it. Drinks age in it: "
                        + "one in-game day is one year on the label, even while you're away."),
                text("Each wood has a character and suits some drinks: in an ideal wood a drink earns its aging star on "
                        + "time, elsewhere twice as slowly. Crimson and warped casks age twice as fast but never give the star."),
                scene("A Cellar", "Oak, spruce, birch and dark oak casks, tapped.",
                        new String[][]{{"sb"}, {"0d"}}, "0", M + "oak_cask[facing=south,tap=true]", "s", M + "spruce_cask[facing=south,tap=true]",
                        "b", M + "birch_cask[facing=south,tap=true]", "d", M + "dark_oak_cask[facing=south,tap=true]"),
                crafting("oak_cask", null), crafting("tap", null));
        entry("cellar", "names", "Names That Change", Drinks.BOURBON.item().get(),
                text("Spirits change their names as they age: New Make becomes Malt Whiskey at 3 years, Eau-de-vie becomes "
                        + "Brandy at 2, White Rum turns Gold, then Dark; Tequila Blanco becomes Reposado, then Añejo. Their "
                        + "color deepens too."));
        entry("cellar", "charring", "Charring", Items.FLINT_AND_STEEL,
                text("Use flint and steel on an empty cask to char it inside. Any charred cask is ideal for whiskey, and "
                        + "bourbon is only called Bourbon after 2 years in $(item)charred oak$(). Nether wood won't char."),
                scene("Charred", "Oak as it comes, and charred inside: its staves and hoops darkened.",
                        new String[][]{{"0c"}}, "0", M + "oak_cask[facing=south]", "c", M + "oak_cask[charred=true,facing=south]"));
        entry("cellar", "display", "Racks and Shelves", ModItems.BOTTLE_SHELF.get(),
                text("Show your bottles off: a $(item)Wine Rack$() holds six lying down, a $(item)Bottle Shelf$() six "
                        + "standing, a $(item)Wine Display$() three along its shelves. Every spirit has its own bottle. Hoppers "
                        + "fill and empty them, and a comparator reads how full they are."),
                scene("Racks and Shelves", "A Wine Rack, a Bottle Shelf and a Wine Display.",
                        new String[][]{{"R0D"}}, "R", M + "wine_rack[facing=south]", "0", M + "bottle_shelf[facing=south]",
                        "D", M + "wine_display[facing=south]"),
                crafting("bottle_shelf", null));

        category("drinking", "Drinking", "What drinks do, and the morning after.", Drinks.MEAD.item().get(), 7);
        entry("drinking", "effects", "Effects", Drinks.MEAD.item().get(),
                text("Every drink does something, shown in its tooltip: $(item)Refreshed$() (hunger drains slower), "
                        + "$(item)Warmth$() (no freezing), $(item)Courage$() (less knockback), and a few of vanilla's. More "
                        + "stars, longer effects."),
                text("Refreshed, Warmth and Courage outlast a drink's alcohol by minutes: one now and then keeps them "
                        + "going without ever getting tipsy."));
        entry("drinking", "tipsy", "Tipsy", Drinks.RUM.item().get(),
                text("Alcohol adds up and wears off over time: Merry, then Tipsy (the view sways), Drunk, and Smashed (slow, "
                        + "and the hiccups). Milk sobers you up at once."),
                text("Drunk or worse, you'll have a $(item)Hangover$() when you sober up: slower mining for a few minutes, "
                        + "shorter after good drinks. Water, milk, a hearty breakfast or $(item)aromatic bitters$() cure it."));

        category("tavern", "The Tavern", "A bar, stools, signs and garlands: somewhere to pour what you've made.",
                ModItems.TAVERN_SIGN.get(), 8);
        entry("tavern", "set_down", "Setting Drinks Down", Drinks.PALE_ALE.item().get(),
                text("Set a drink down anywhere: sneak and right-click the top of a block with it. Up to four stand "
                        + "together, each keeping its stars. Right-click with an empty hand to take the last one back."),
                text("Anything with a solid top will hold them: a table, a bar counter, a slab in the top half of its "
                        + "block. Take the block away and the drinks fall, nothing lost."));
        entry("tavern", "bar", "The Bar", ModItems.BAR_COUNTERS.get(CaskWood.OAK).get(),
                text("Every cask wood makes a $(item)Bar Counter$(): a paneled bar with a brass foot rail. Put them side by "
                        + "side for one long bar, and set drinks down on its top. Like stairs, they turn corners: out "
                        + "toward the customers, or round them."),
                text("A $(item)Bar Stool$() stands at the bar's height. Right-click it to sit down, and sneak to stand "
                        + "up again. One sitter to a stool."),
                scene("Round the Corner", "A bar wrapped round its customers, stools in front.",
                        new String[][]{{"I0C", "E S", "ES "}}, "I", M + "oak_bar_counter[facing=south,shape=inner_left]",
                        "0", M + "oak_bar_counter[facing=south]", "C", M + "oak_bar_counter[facing=south]",
                        "E", M + "oak_bar_counter[facing=east]", "S", M + "oak_bar_stool"),
                scene("A Corner Out", "And one turning out toward them.",
                        new String[][]{{"SS  ", "C0O ", "  ES", "  E "}}, "C", M + "spruce_bar_counter[facing=north]",
                        "0", M + "spruce_bar_counter[facing=north]", "O", M + "spruce_bar_counter[facing=north,shape=outer_right]",
                        "E", M + "spruce_bar_counter[facing=east]", "S", M + "spruce_bar_stool"),
                crafting("oak_bar_counter", null), crafting("oak_bar_stool", null));
        entry("tavern", "walls", "On the Walls", ModItems.TAVERN_SIGN.get(),
                text("A $(item)Tavern Sign$() hangs out from a wall on its iron bracket. Repaint it with what you serve: "
                        + "right-click it with a mug of beer for an alehouse, wine for a wine bar, a spirit for a distillery, "
                        + "or a cask for a cellar."),
                text("A $(item)Mug Rack$() holds four empty mugs, hung by their handles from its pegs. A $(item)Hop "
                        + "Garland$() is fresh bines strung along a wall: hang several in a row and they join into one."),
                scene("A Tavern Wall", "A hop garland strung along the top, a Mug Rack and a sign for a wine bar.",
                        new String[][]{{"GG ", "WWW"}, {"M s", "W0W"}}, "G", M + "hop_garland[facing=north]",
                        "M", M + "mug_rack[facing=north]", "s", M + "tavern_sign[emblem=wine,facing=north]",
                        "W", "minecraft:spruce_planks", "0", "minecraft:spruce_planks"),
                crafting("tavern_sign", null), crafting("mug_rack", null), crafting("hop_garland", null));
        entry("tavern", "bundles", "Hanging Bundles", ModItems.HOP_BUNDLE.get(),
                text("Nine hop cones, lavender, garlic or chilies tie into a bundle to hang from the rafters: under a "
                        + "block, or from a nail on a wall if you click its side. Untie one in the crafting grid to get the "
                        + "nine back."),
                scene("From the Rafters", "Hops, lavender, garlic and chilies, hung to dry.",
                        new String[][]{{"0WWW"}, {"hlgc"}}, "0", "minecraft:spruce_planks", "W", "minecraft:spruce_planks",
                        "h", M + "hop_bundle", "l", M + "lavender_bundle", "g", M + "garlic_braid", "c", M + "chili_string"),
                crafting("hop_bundle", null));
    }

    // --- building blocks ----------------------------------------------------------------------------------------

    private void category(String id, String name, String description, ItemLike icon, int sort) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("icon", key(icon));
        json.addProperty("sortnum", sort);
        categories.add(json);
        categoryIds.add(id);
    }

    private void entry(String category, String id, String name, ItemLike icon, JsonObject... pages) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("icon", key(icon));
        json.addProperty("category", SeedToCellar.MOD_ID + ":" + category);
        json.addProperty("sortnum", (int) entryPaths.stream().filter(p -> p.startsWith(category + "/")).count());
        JsonArray array = new JsonArray();
        for (JsonObject page : pages) array.add(page);
        json.add("pages", array);
        entries.add(json);
        entryPaths.add(category + "/" + id);
    }

    private static JsonObject text(String text) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:text");
        json.addProperty("text", text);
        return json;
    }

    /** An item shown large, with a few lines under it. */
    private static JsonObject spotlight(ItemLike item, String text) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:spotlight");
        json.addProperty("item", key(item));
        json.addProperty("text", text);
        return json;
    }

    /** Our blocks' ids, for the scenes. */
    private static final String M = SeedToCellar.MOD_ID + ":";

    /**
     * A 3D scene of real blocks, slowly turning (Shift holds it still). `layers` run top to bottom, each a list of rows
     * from north to south with their characters west to east, as a map is read; `mapping` pairs each character with a
     * block state ("id[property=value]", unnamed properties at their defaults). A space is air, and exactly one '0' marks
     * the middle. Room for 4 lines of text under it.
     * <p>Patchouli reads a layer the other way about (its rows run west to east, each row's characters north to south), so
     * each layer is turned to match; otherwise the scene comes out mirrored across its diagonal, every block facing
     * its own way but its neighbors swapped round.
     */
    private static JsonObject scene(String name, String text, String[][] layers, String... mapping) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:multiblock");
        json.addProperty("name", name);
        json.addProperty("enable_visualize", false);
        JsonArray pattern = new JsonArray();
        for (String[] layer : layers) {
            JsonArray columns = new JsonArray();
            for (int x = 0; x < layer[0].length(); x++) {
                StringBuilder column = new StringBuilder();
                for (String row : layer) column.append(row.charAt(x));
                columns.add(column.toString());
            }
            pattern.add(columns);
        }
        JsonObject map = new JsonObject();
        for (int i = 0; i < mapping.length; i += 2) {
            checkState(name, mapping[i + 1]);
            map.addProperty(mapping[i], mapping[i + 1]);
        }
        checkPattern(name, layers, mapping);
        checkCounters(name, layers, mapping);
        JsonObject multiblock = new JsonObject();
        multiblock.add("pattern", pattern);
        multiblock.add("mapping", map);
        json.add("multiblock", multiblock);
        json.addProperty("text", text);
        return json;
    }

    /**
     * Patchouli can't show a scene with an unknown block or property (and says so only in the log), so a mistake fails
     * the data run instead.
     */
    private static void checkState(String scene, String state) {
        int bracket = state.indexOf('[');
        net.minecraft.resources.ResourceLocation id = SeedToCellar.parse(bracket < 0 ? state : state.substring(0, bracket));
        net.minecraft.world.level.block.Block block = ForgeRegistries.BLOCKS.getValue(id);
        if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
            throw new IllegalStateException("Almanac scene " + scene + ": no block " + id);
        }
        if (bracket < 0) return;
        for (String pair : state.substring(bracket + 1, state.length() - 1).split(",")) {
            String[] kv = pair.split("=");
            var property = block.getStateDefinition().getProperty(kv[0]);
            if (property == null || property.getValue(kv[1]).isEmpty()) {
                throw new IllegalStateException("Almanac scene " + scene + ": " + id + " has no " + pair);
            }
        }
    }

    /** Every layer the same size, every character mapped (or air), and one '0'. */
    private static void checkPattern(String scene, String[][] layers, String... mapping) {
        java.util.Set<Character> mapped = new java.util.HashSet<>(List.of(' '));
        for (int i = 0; i < mapping.length; i += 2) mapped.add(mapping[i].charAt(0));
        int centers = 0;
        for (String[] layer : layers) {
            if (layer.length != layers[0].length) throw new IllegalStateException("Almanac scene " + scene + ": uneven layers");
            for (String row : layer) {
                if (row.length() != layers[0][0].length()) throw new IllegalStateException("Almanac scene " + scene + ": uneven rows");
                for (char c : row.toCharArray()) {
                    if (!mapped.contains(c)) throw new IllegalStateException("Almanac scene " + scene + ": '" + c + "' isn't mapped");
                    if (c == '0') centers++;
                }
            }
        }
        if (centers != 1) throw new IllegalStateException("Almanac scene " + scene + ": needs exactly one '0'");
    }

    /**
     * A scene shows blocks as they're written, without the shapes the game would work out between neighbors; so each bar
     * counter in one must have the corner shape it would really take there.
     */
    private static void checkCounters(String scene, String[][] layers, String... mapping) {
        java.util.Map<Character, net.minecraft.world.level.block.state.BlockState> states = new java.util.HashMap<>();
        for (int i = 0; i < mapping.length; i += 2) {
            try {
                states.put(mapping[i].charAt(0), net.minecraft.commands.arguments.blocks.BlockStateParser.parseForBlock(
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), mapping[i + 1], false).blockState());
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                throw new IllegalStateException("Almanac scene " + scene + ": can't read " + mapping[i + 1], e);
            }
        }
        java.util.Map<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState> world = new java.util.HashMap<>();
        for (int layer = 0; layer < layers.length; layer++) {
            for (int z = 0; z < layers[layer].length; z++) {
                for (int x = 0; x < layers[layer][z].length(); x++) {
                    var state = states.get(layers[layer][z].charAt(x));
                    if (state != null) world.put(new net.minecraft.core.BlockPos(x, layers.length - 1 - layer, z), state);
                }
            }
        }
        net.minecraft.world.level.BlockGetter getter = new net.minecraft.world.level.BlockGetter() {
            @Override
            public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(net.minecraft.core.BlockPos pos) {
                return null;
            }

            @Override
            public net.minecraft.world.level.block.state.BlockState getBlockState(net.minecraft.core.BlockPos pos) {
                return world.getOrDefault(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }

            @Override
            public net.minecraft.world.level.material.FluidState getFluidState(net.minecraft.core.BlockPos pos) {
                return getBlockState(pos).getFluidState();
            }

            @Override
            public int getHeight() {
                return layers.length;
            }

            @Override
            public int getMinBuildHeight() {
                return 0;
            }
        };
        world.forEach((pos, state) -> {
            if (state.getBlock() instanceof io.github.spencerharris192.seedtocellar.decor.BarCounterBlock) {
                var real = io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.shape(state, getter, pos);
                if (real != state.getValue(io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.SHAPE)) {
                    throw new IllegalStateException("Almanac scene " + scene + ": the counter at " + pos.toShortString() + " would be "
                            + real.getSerializedName() + ", not " + state.getValue(io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.SHAPE).getSerializedName());
                }
            }
        });
    }

    /** A crafting-table recipe of ours, shown as its grid (with a line under it, or none). */
    private static JsonObject crafting(String recipe, String text) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "patchouli:crafting");
        json.addProperty("recipe", SeedToCellar.MOD_ID + ":" + recipe);
        if (text != null) json.addProperty("text", text);
        return json;
    }

    private static String key(ItemLike item) {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).toString();
    }
}
