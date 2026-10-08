package io.github.spencerharris192.seedtocellar.registry;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import io.github.spencerharris192.seedtocellar.brewing.BottledLiquidItem;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.CaskItem;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerItem;
import io.github.spencerharris192.seedtocellar.brewing.VesselItem;
import io.github.spencerharris192.seedtocellar.farming.CompostItem;
import io.github.spencerharris192.seedtocellar.food.DishItem;
import io.github.spencerharris192.seedtocellar.food.ModFoods;
import io.github.spencerharris192.seedtocellar.food.SourdoughStarterItem;
import io.github.spencerharris192.seedtocellar.farming.TrellisPlantItem;
import io.github.spencerharris192.seedtocellar.farming.SickleItem;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.farming.TrellisItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** All items. IDs are permanent once released. Registration order = creative tab order. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SeedToCellar.MOD_ID);

    // --- Farming --- (row crops such as barley register themselves: see farming.Crops)
    public static final DeferredItem<Item> HOP_RHIZOME = ITEMS.registerItem("hop_rhizome", p -> new TrellisPlantItem(ModBlocks.HOPS, p), () -> new Item.Properties());
    public static final DeferredItem<Item> HOP_CONES = ITEMS.registerItem("hop_cones", Item::new, () -> new Item.Properties());
    public static final DeferredItem<Item> TRELLIS = ITEMS.registerItem("trellis", p -> new TrellisItem(ModBlocks.TRELLIS.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> WILD_HOPS = ITEMS.registerItem("wild_hops", p -> new BlockItem(ModBlocks.WILD_HOPS.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> COMPOST_BIN = block("compost_bin", ModBlocks.COMPOST_BIN);
    public static final DeferredItem<Item> COMPOST = ITEMS.registerItem("compost", CompostItem::new, () -> new Item.Properties());
    public static final DeferredItem<Item> WOODEN_SICKLE = sickle("wooden_sickle", ToolMaterial.WOOD, new Item.Properties());
    public static final DeferredItem<Item> STONE_SICKLE = sickle("stone_sickle", ToolMaterial.STONE, new Item.Properties());
    public static final DeferredItem<Item> IRON_SICKLE = sickle("iron_sickle", ToolMaterial.IRON, new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_SICKLE = sickle("golden_sickle", ToolMaterial.GOLD, new Item.Properties());
    public static final DeferredItem<Item> DIAMOND_SICKLE = sickle("diamond_sickle", ToolMaterial.DIAMOND, new Item.Properties());
    public static final DeferredItem<Item> NETHERITE_SICKLE = sickle("netherite_sickle", ToolMaterial.NETHERITE, new Item.Properties().fireResistant());
    /** Every sickle, weakest first. */
    public static final List<DeferredItem<Item>> SICKLES = List.of(WOODEN_SICKLE, STONE_SICKLE, IRON_SICKLE, GOLDEN_SICKLE,
            DIAMOND_SICKLE, NETHERITE_SICKLE);
    public static final DeferredItem<Item> STRAW = simple("straw");
    public static final DeferredItem<Item> DRYING_RACK = block("drying_rack", ModBlocks.DRYING_RACK);
    public static final DeferredItem<Item> DRIED_CHILI = simple("dried_chili");
    public static final DeferredItem<Item> JERKY = ITEMS.registerItem("jerky", Item::new, () -> new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build(), Consumables.defaultFood().consumeSeconds(0.8F).build()));
    public static final DeferredItem<Item> THATCH = block("thatch", ModBlocks.THATCH);
    public static final Map<CaskWood, DeferredItem<Item>> BAR_COUNTERS = new EnumMap<>(CaskWood.class);
    public static final Map<CaskWood, DeferredItem<Item>> BAR_STOOLS = new EnumMap<>(CaskWood.class);

    static {
        ModBlocks.BAR_COUNTERS.forEach((wood, block) -> BAR_COUNTERS.put(wood, block(wood.id() + "_bar_counter", block)));
        ModBlocks.BAR_STOOLS.forEach((wood, block) -> BAR_STOOLS.put(wood, block(wood.id() + "_bar_stool", block)));
    }

    public static final DeferredItem<Item> MUG_RACK = block("mug_rack", ModBlocks.MUG_RACK);
    public static final DeferredItem<Item> HOP_GARLAND = block("hop_garland", ModBlocks.HOP_GARLAND);
    public static final DeferredItem<Item> TAVERN_SIGN = block("tavern_sign", ModBlocks.TAVERN_SIGN);
    public static final DeferredItem<Item> HOP_BUNDLE = block("hop_bundle", ModBlocks.HOP_BUNDLE);
    public static final DeferredItem<Item> LAVENDER_BUNDLE = block("lavender_bundle", ModBlocks.LAVENDER_BUNDLE);
    public static final DeferredItem<Item> GARLIC_BRAID = block("garlic_braid", ModBlocks.GARLIC_BRAID);
    public static final DeferredItem<Item> CHILI_STRING = block("chili_string", ModBlocks.CHILI_STRING);
    public static final DeferredItem<Item> THATCH_STAIRS = block("thatch_stairs", ModBlocks.THATCH_STAIRS);
    public static final DeferredItem<Item> THATCH_SLAB = block("thatch_slab", ModBlocks.THATCH_SLAB);
    static {
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) block(storage.block().getId().getPath(), storage.block());
    }

    // --- Brewing stations ---
    public static final DeferredItem<Item> MALTING_TUB = block("malting_tub", ModBlocks.MALTING_TUB);
    public static final DeferredItem<Item> KILN = block("kiln", ModBlocks.KILN);
    public static final DeferredItem<Item> MILLSTONE = block("millstone", ModBlocks.MILLSTONE);
    public static final DeferredItem<Item> BREW_KETTLE = ITEMS.registerItem("brew_kettle", p -> new io.github.spencerharris192.seedtocellar.decor.CopperBlockItem(ModBlocks.BREW_KETTLE.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> FERMENTING_VAT = block("fermenting_vat", ModBlocks.FERMENTING_VAT);
    public static final DeferredItem<Item> PRESERVING_JAR = block("preserving_jar", ModBlocks.PRESERVING_JAR);

    // --- Malt house ingredients ---
    public static final DeferredItem<Item> DRIED_HOPS = simple("dried_hops");
    public static final DeferredItem<Item> GREEN_BARLEY_MALT = simple("green_barley_malt");
    public static final DeferredItem<Item> PALE_MALT = simple("pale_malt");
    public static final DeferredItem<Item> AMBER_MALT = simple("amber_malt");
    public static final DeferredItem<Item> BLACK_MALT = simple("black_malt");
    public static final DeferredItem<Item> PALE_GRIST = simple("pale_grist");
    public static final DeferredItem<Item> AMBER_GRIST = simple("amber_grist");
    public static final DeferredItem<Item> BLACK_GRIST = simple("black_grist");
    public static final DeferredItem<Item> WHEAT_FLOUR = simple("wheat_flour");
    // Wheat malt for wheat beer
    public static final DeferredItem<Item> GREEN_WHEAT_MALT = simple("green_wheat_malt");
    public static final DeferredItem<Item> WHEAT_MALT = simple("wheat_malt");
    public static final DeferredItem<Item> WHEAT_GRIST = simple("wheat_grist");

    // --- Brewhouse ---
    public static final DeferredItem<Item> SPENT_GRAIN = simple("spent_grain");
    public static final DeferredItem<Item> SOURDOUGH_STARTER = ITEMS.registerItem("sourdough_starter", SourdoughStarterItem::new, () -> new Item.Properties());
    public static final DeferredItem<Item> ALE_YEAST = simple("ale_yeast");
    public static final DeferredItem<Item> WINE_YEAST = simple("wine_yeast");
    /** Ale yeast cultured in the cold: lager. */
    public static final DeferredItem<Item> LAGER_YEAST = simple("lager_yeast");

    // --- Cellar ---
    public static final Map<CaskWood, DeferredItem<Item>> CASKS = new EnumMap<>(CaskWood.class);

    static {
        ModBlocks.CASKS.forEach((wood, block) -> CASKS.put(wood,
                ITEMS.registerItem(block.getId().getPath(), p -> new CaskItem(block.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix())));
    }

    public static final DeferredItem<Item> OAK_CASK = CASKS.get(CaskWood.OAK);
    public static final DeferredItem<Item> KEG = ITEMS.registerItem("keg", p -> new CaskItem(ModBlocks.KEG.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    public static final DeferredItem<Item> WINE_RACK = block("wine_rack", ModBlocks.WINE_RACK);
    public static final DeferredItem<Item> BOTTLE_SHELF = block("bottle_shelf", ModBlocks.BOTTLE_SHELF);
    public static final DeferredItem<Item> WINE_DISPLAY = block("wine_display", ModBlocks.WINE_DISPLAY);
    public static final DeferredItem<Item> TAP = simple("tap");
    public static final DeferredItem<Item> MUG = ITEMS.registerItem("mug", VesselItem::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> WINE_BOTTLE = ITEMS.registerItem("wine_bottle", VesselItem::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SPIRIT_BOTTLE = ITEMS.registerItem("spirit_bottle", VesselItem::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> HYDROMETER = ITEMS.registerItem("hydrometer", HydrometerItem::new, () -> new Item.Properties().stacksTo(1));

    // --- Kitchen: flours, doughs and breads (GDD sections 9.6, 15) ---
    public static final DeferredItem<Item> RYE_FLOUR = simple("rye_flour");
    public static final DeferredItem<Item> CORNMEAL = simple("cornmeal");
    public static final DeferredItem<Item> ROLLED_OATS = simple("rolled_oats");
    public static final DeferredItem<Item> DOUGH = simple("dough");
    public static final DeferredItem<Item> RYE_DOUGH = simple("rye_dough");
    public static final DeferredItem<Item> SOURDOUGH_DOUGH = simple("sourdough_dough");
    public static final DeferredItem<Item> SPENT_GRAIN_DOUGH = simple("spent_grain_dough");
    public static final DeferredItem<Item> BEER_BREAD_DOUGH = simple("beer_bread_dough");
    public static final DeferredItem<Item> CORNBREAD_BATTER = simple("cornbread_batter");
    public static final DeferredItem<Item> MASA = simple("masa");
    public static final DeferredItem<Item> RYE_BREAD = food("rye_bread", ModFoods.RYE_BREAD);
    public static final DeferredItem<Item> SOURDOUGH_BREAD = ITEMS.registerItem("sourdough_bread", p -> new DishItem(p, true), () -> ModFoods.properties(ModFoods.SOURDOUGH_BREAD));
    public static final DeferredItem<Item> CORNBREAD = food("cornbread", ModFoods.CORNBREAD);
    public static final DeferredItem<Item> SPENT_GRAIN_BREAD = food("spent_grain_bread", ModFoods.SPENT_GRAIN_BREAD);
    public static final DeferredItem<Item> BEER_BREAD = food("beer_bread", ModFoods.BEER_BREAD);
    public static final DeferredItem<Item> TORTILLA = food("tortilla", ModFoods.TORTILLA);
    public static final DeferredItem<Item> ROASTED_CORN = food("roasted_corn", ModFoods.ROASTED_CORN);

    // --- Kitchen: cooked in the Brew Kettle (GDD section 15) ---
    public static final DeferredItem<Item> BEEF_AND_ALE_STEW = bowl("beef_and_ale_stew", ModFoods.BEEF_AND_ALE_STEW, false);
    public static final DeferredItem<Item> MUTTON_AND_BARLEY_STEW = bowl("mutton_and_barley_stew", ModFoods.MUTTON_AND_BARLEY_STEW, false);
    public static final DeferredItem<Item> CHILI_CON_CARNE = bowl("chili_con_carne", ModFoods.CHILI_CON_CARNE, false);
    public static final DeferredItem<Item> BORSCHT = bowl("borscht", ModFoods.BORSCHT, false);
    public static final DeferredItem<Item> TOMATO_SOUP = bowl("tomato_soup", ModFoods.TOMATO_SOUP, false);
    public static final DeferredItem<Item> PORRIDGE = bowl("porridge", ModFoods.PORRIDGE, true);
    public static final DeferredItem<Item> COOKED_RICE = bowl("cooked_rice", ModFoods.COOKED_RICE, false);
    public static final DeferredItem<Item> RICE_BALL = food("rice_ball", ModFoods.RICE_BALL);
    public static final DeferredItem<Item> POPCORN = food("popcorn", ModFoods.POPCORN);
    public static final DeferredItem<Item> CRANBERRY_SAUCE = bowl("cranberry_sauce", ModFoods.CRANBERRY_SAUCE, false);
    public static final DeferredItem<Item> BLUEBERRY_JAM = jam("blueberry_jam");
    public static final DeferredItem<Item> BLACKBERRY_JAM = jam("blackberry_jam");
    public static final DeferredItem<Item> ELDERBERRY_JAM = jam("elderberry_jam");
    public static final DeferredItem<Item> SWEET_BERRY_JAM = jam("sweet_berry_jam");
    public static final DeferredItem<Item> CHERRY_JAM = jam("cherry_jam");
    public static final DeferredItem<Item> PLUM_JAM = jam("plum_jam");
    public static final DeferredItem<Item> PEACH_JAM = jam("peach_jam");
    /** The jam family's orange member, peel and all. */
    public static final DeferredItem<Item> MARMALADE = jam("marmalade");
    /** Every jam, for recipes that take any of them. */
    public static final List<DeferredItem<Item>> JAMS = List.of(BLUEBERRY_JAM, BLACKBERRY_JAM, ELDERBERRY_JAM, SWEET_BERRY_JAM,
            CHERRY_JAM, PLUM_JAM, PEACH_JAM, MARMALADE);
    public static final DeferredItem<Item> JAM_TOAST = food("jam_toast", ModFoods.JAM_TOAST);

    // --- Kitchen: preserves (GDD sections 8, 9.5, 15) ---
    public static final DeferredItem<Item> MOTHER_OF_VINEGAR = simple("mother_of_vinegar");
    public static final DeferredItem<Item> PICKLES = food("pickles", ModFoods.PICKLES);
    public static final DeferredItem<Item> SAUERKRAUT = food("sauerkraut", ModFoods.SAUERKRAUT);
    public static final DeferredItem<Item> KIMCHI = food("kimchi", ModFoods.KIMCHI);
    public static final DeferredItem<Item> DRIED_BERRIES = food("dried_berries", ModFoods.DRIED_BERRIES);
    public static final DeferredItem<Item> GRANOLA = food("granola", ModFoods.GRANOLA);
    public static final DeferredItem<Item> HARVEST_FEAST = block("harvest_feast", ModBlocks.HARVEST_FEAST);

    // --- Orchard and vineyard (GDD sections 6.2, 9.7, 15) ---
    /** Cut from grape vines with shears (stuffed grape leaves). */
    public static final DeferredItem<Item> GRAPE_LEAVES = simple("grape_leaves");
    public static final DeferredItem<Item> RAISINS = food("raisins", ModFoods.RAISINS);
    public static final DeferredItem<Item> GOLDEN_RAISINS = food("golden_raisins", ModFoods.RAISINS);
    public static final DeferredItem<Item> PRUNES = food("prunes", ModFoods.RAISINS);
    public static final DeferredItem<Item> DRIED_CHERRIES = food("dried_cherries", ModFoods.RAISINS);
    public static final DeferredItem<Item> DRIED_APPLES = food("dried_apples", ModFoods.RAISINS);
    public static final DeferredItem<Item> DRIED_PEACHES = food("dried_peaches", ModFoods.RAISINS);
    public static final DeferredItem<Item> DRIED_PEARS = food("dried_pears", ModFoods.RAISINS);

    // --- Orchard kitchen (GDD sections 10.5, 15, 17) ---
    /** Olive oil in a glass bottle, filled at the press like a drink; recipes give the bottle back. */
    public static final DeferredItem<Item> OLIVE_OIL = ITEMS.registerItem("olive_oil", p -> new BottledLiquidItem(() -> ModFluids.OLIVE_OIL.get(), p), () -> new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    /** Sorghum juice boiled down in the kettle: a sweetener, used like sugar (the bottle comes back). */
    public static final DeferredItem<Item> SORGHUM_SYRUP = ITEMS.registerItem("sorghum_syrup", Item::new, () -> new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    public static final DeferredItem<Item> CURED_OLIVES = food("cured_olives", ModFoods.CURED_OLIVES);
    public static final DeferredItem<Item> STUFFED_GRAPE_LEAVES = food("stuffed_grape_leaves", ModFoods.STUFFED_GRAPE_LEAVES);
    public static final DeferredItem<Item> BRUSCHETTA = food("bruschetta", ModFoods.BRUSCHETTA);
    public static final DeferredItem<Item> GARLIC_BREAD = food("garlic_bread", ModFoods.GARLIC_BREAD);
    public static final DeferredItem<Item> SALAD = bowl("salad", ModFoods.SALAD, false);
    public static final DeferredItem<Item> RISOTTO = bowl("risotto", ModFoods.RISOTTO, false);
    public static final DeferredItem<Item> COQ_AU_VIN = bowl("coq_au_vin", ModFoods.COQ_AU_VIN, false);

    // --- Winery (GDD sections 7, 9.2, 9.7) ---
    public static final DeferredItem<Item> CRUSHING_TUB = block("crushing_tub", ModBlocks.CRUSHING_TUB);
    public static final DeferredItem<Item> FRUIT_PRESS = block("fruit_press", ModBlocks.FRUIT_PRESS);
    /** Byproducts of the press: compost, fuel, paper, and (later) grappa. */
    public static final DeferredItem<Item> GRAPE_POMACE = simple("grape_pomace");
    public static final DeferredItem<Item> FRUIT_POMACE = simple("fruit_pomace");
    /** Olive pomace burns 400 ticks in a furnace (half that in a smoker or blast furnace, like vanilla fuels); datagen writes it. */
    public static final ResourceKey<ContextIntProvider> OLIVE_POMACE_BURN_TIME = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER,
            SeedToCellar.id("cooking/time_olive_pomace"));
    public static final DeferredItem<Item> OLIVE_POMACE = fuel("olive_pomace", OLIVE_POMACE_BURN_TIME);
    public static final DeferredItem<Item> BAGASSE = fuel("bagasse", ContextIntProviders.COOKING_TIME_WOOD_ITEMS_LARGE);
    public static final DeferredItem<Item> HARVEST_FEAST_SERVING = bowl("harvest_feast_serving", ModFoods.HARVEST_FEAST_SERVING, false);

    public static final DeferredItem<Item> BLACK_FOREST_CAKE = block("black_forest_cake", ModBlocks.BLACK_FOREST_CAKE);
    public static final DeferredItem<Item> BLACK_FOREST_CAKE_SLICE = food("black_forest_cake_slice", ModFoods.CAKE_SLICE);

    // --- Sake (GDD section 9.3): polished, steamed, grown into koji ---
    public static final DeferredItem<Item> POLISHED_RICE = simple("polished_rice");
    public static final DeferredItem<Item> RICE_BRAN = simple("rice_bran");
    public static final DeferredItem<Item> STEAMED_RICE = food("steamed_rice", ModFoods.STEAMED_RICE);
    public static final DeferredItem<Item> KOJI_RICE = simple("koji_rice");
    // --- Coffee (GDD sections 6.2, 10.5): green beans roasted, then ground ---
    public static final DeferredItem<Item> ROASTED_COFFEE = simple("roasted_coffee");
    public static final DeferredItem<Item> GROUND_COFFEE = simple("ground_coffee");

    // --- Distillery (GDD sections 7, 9.4) ---
    public static final DeferredItem<Item> POT_STILL = ITEMS.registerItem("pot_still", p -> new io.github.spencerharris192.seedtocellar.decor.CopperBlockItem.DoubleHigh(ModBlocks.POT_STILL.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    /** Cane juice boiled down: bottled from the kettle like olive oil, a sweetener; fermented, the rum wash. */
    public static final DeferredItem<Item> MOLASSES = ITEMS.registerItem("molasses", p -> new BottledLiquidItem(() -> ModFluids.MOLASSES.get(), p), () -> new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    /** Agave juice boiled down into a bottle: a sweetener. */
    public static final DeferredItem<Item> AGAVE_SYRUP = ITEMS.registerItem("agave_syrup", Item::new, () -> new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    /** An agave heart roasted in the kiln, sweet and caramel-brown: pressed for agave juice. */
    public static final DeferredItem<Item> ROASTED_AGAVE = simple("roasted_agave");
    /** Green vanilla pods: planted on a jungle log's side, or cured on the Drying Rack. */
    public static final DeferredItem<Item> VANILLA_POD = ITEMS.registerItem("vanilla_pod", p -> new BlockItem(ModBlocks.VANILLA.get(), p), () -> new Item.Properties());
    /** Dark, fragrant cured vanilla: spiced rum and baking. */
    public static final DeferredItem<Item> CURED_VANILLA = simple("cured_vanilla");
    /** Fits on the Pot Still's swan neck: botanicals in it flavor vodka into gin. */
    public static final DeferredItem<Item> GIN_BASKET = simple("gin_basket");
    /** Zest of citrus: botanicals for gin, flavoring for bitters and spiced rum. */
    public static final DeferredItem<Item> LEMON_PEEL = simple("lemon_peel");
    public static final DeferredItem<Item> ORANGE_PEEL = simple("orange_peel");
    /** Pressed agave fiber: string, or compost. */
    public static final DeferredItem<Item> AGAVE_FIBER = simple("agave_fiber");

    private static DeferredItem<Item> sickle(String name, ToolMaterial tier, Item.Properties properties) {
        return ITEMS.registerItem(name, p -> new SickleItem(tier, p), () -> properties);
    }

    /** A soup, stew or other dish served in a bowl; the bowl comes back. Stacks to 16. */
    private static DeferredItem<Item> bowl(String name, FoodProperties food, boolean curesHangover) {
        return ITEMS.registerItem(name, p -> new DishItem(p, curesHangover),
                () -> ModFoods.properties(food).stacksTo(16).usingConvertsTo(Items.BOWL).craftRemainder(Items.BOWL));
    }

    /** A jam in a small glass jar (a glass bottle); the bottle comes back. */
    private static DeferredItem<Item> jam(String name) {
        return ITEMS.registerItem(name, p -> new DishItem(p, false),
                () -> ModFoods.properties(ModFoods.JAM).stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE).craftRemainder(Items.GLASS_BOTTLE));
    }

    private static DeferredItem<Item> food(String name, FoodProperties food) {
        return ITEMS.registerItem(name, Item::new, () -> ModFoods.properties(food));
    }

    /** A fuel; burn times are vanilla's context providers (a stick is 100 ticks, a plank 300). */
    private static DeferredItem<Item> fuel(String name, ResourceKey<ContextIntProvider> burnTime) {
        return ITEMS.registerItem(name, Item::new, () -> new Item.Properties().cookingFuel(burnTime));
    }

    private static DeferredItem<Item> simple(String name) {
        return ITEMS.registerItem(name, Item::new, () -> new Item.Properties());
    }

    private static DeferredItem<Item> block(String name, DeferredBlock<Block> block) {
        return ITEMS.registerItem(name, p -> new BlockItem(block.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
    }

    private ModItems() {}
}
