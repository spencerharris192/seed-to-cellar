package io.github.spencerharris192.seedtocellar.registry;

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
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** All items. IDs are permanent once released. Registration order = creative tab order. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SeedToCellar.MOD_ID);

    // --- Farming --- (row crops such as barley register themselves: see farming.Crops)
    public static final RegistryObject<Item> HOP_RHIZOME = ITEMS.register("hop_rhizome", () -> new TrellisPlantItem(ModBlocks.HOPS, new Item.Properties()));
    public static final RegistryObject<Item> HOP_CONES = ITEMS.register("hop_cones", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRELLIS = ITEMS.register("trellis", () -> new TrellisItem(ModBlocks.TRELLIS.get(), new Item.Properties()));
    public static final RegistryObject<Item> WILD_HOPS = ITEMS.register("wild_hops", () -> new BlockItem(ModBlocks.WILD_HOPS.get(), new Item.Properties()));
    public static final RegistryObject<Item> COMPOST_BIN = block("compost_bin", ModBlocks.COMPOST_BIN);
    public static final RegistryObject<Item> COMPOST = ITEMS.register("compost", () -> new CompostItem(new Item.Properties()));
    public static final RegistryObject<Item> WOODEN_SICKLE = sickle("wooden_sickle", Tiers.WOOD, new Item.Properties());
    public static final RegistryObject<Item> STONE_SICKLE = sickle("stone_sickle", Tiers.STONE, new Item.Properties());
    public static final RegistryObject<Item> IRON_SICKLE = sickle("iron_sickle", Tiers.IRON, new Item.Properties());
    public static final RegistryObject<Item> GOLDEN_SICKLE = sickle("golden_sickle", Tiers.GOLD, new Item.Properties());
    public static final RegistryObject<Item> DIAMOND_SICKLE = sickle("diamond_sickle", Tiers.DIAMOND, new Item.Properties());
    public static final RegistryObject<Item> NETHERITE_SICKLE = sickle("netherite_sickle", Tiers.NETHERITE, new Item.Properties().fireResistant());
    /** Every sickle, weakest first. */
    public static final List<RegistryObject<Item>> SICKLES = List.of(WOODEN_SICKLE, STONE_SICKLE, IRON_SICKLE, GOLDEN_SICKLE,
            DIAMOND_SICKLE, NETHERITE_SICKLE);
    public static final RegistryObject<Item> STRAW = simple("straw");
    public static final RegistryObject<Item> DRYING_RACK = block("drying_rack", ModBlocks.DRYING_RACK);
    public static final RegistryObject<Item> DRIED_CHILI = simple("dried_chili");
    public static final RegistryObject<Item> JERKY = ITEMS.register("jerky", () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).meat().fast().build())));
    public static final RegistryObject<Item> THATCH = block("thatch", ModBlocks.THATCH);
    public static final Map<CaskWood, RegistryObject<Item>> BAR_COUNTERS = new EnumMap<>(CaskWood.class);
    public static final Map<CaskWood, RegistryObject<Item>> BAR_STOOLS = new EnumMap<>(CaskWood.class);

    static {
        ModBlocks.BAR_COUNTERS.forEach((wood, block) -> BAR_COUNTERS.put(wood, block(wood.id() + "_bar_counter", block)));
        ModBlocks.BAR_STOOLS.forEach((wood, block) -> BAR_STOOLS.put(wood, block(wood.id() + "_bar_stool", block)));
    }

    public static final RegistryObject<Item> MUG_RACK = block("mug_rack", ModBlocks.MUG_RACK);
    public static final RegistryObject<Item> HOP_GARLAND = block("hop_garland", ModBlocks.HOP_GARLAND);
    public static final RegistryObject<Item> TAVERN_SIGN = block("tavern_sign", ModBlocks.TAVERN_SIGN);
    public static final RegistryObject<Item> HOP_BUNDLE = block("hop_bundle", ModBlocks.HOP_BUNDLE);
    public static final RegistryObject<Item> LAVENDER_BUNDLE = block("lavender_bundle", ModBlocks.LAVENDER_BUNDLE);
    public static final RegistryObject<Item> GARLIC_BRAID = block("garlic_braid", ModBlocks.GARLIC_BRAID);
    public static final RegistryObject<Item> CHILI_STRING = block("chili_string", ModBlocks.CHILI_STRING);
    public static final RegistryObject<Item> THATCH_STAIRS = block("thatch_stairs", ModBlocks.THATCH_STAIRS);
    public static final RegistryObject<Item> THATCH_SLAB = block("thatch_slab", ModBlocks.THATCH_SLAB);
    static {
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) block(storage.block().getId().getPath(), storage.block());
    }

    // --- Brewing stations ---
    public static final RegistryObject<Item> MALTING_TUB = block("malting_tub", ModBlocks.MALTING_TUB);
    public static final RegistryObject<Item> KILN = block("kiln", ModBlocks.KILN);
    public static final RegistryObject<Item> MILLSTONE = block("millstone", ModBlocks.MILLSTONE);
    public static final RegistryObject<Item> BREW_KETTLE = ITEMS.register("brew_kettle",
            () -> new io.github.spencerharris192.seedtocellar.decor.CopperBlockItem(ModBlocks.BREW_KETTLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> FERMENTING_VAT = block("fermenting_vat", ModBlocks.FERMENTING_VAT);
    public static final RegistryObject<Item> PRESERVING_JAR = block("preserving_jar", ModBlocks.PRESERVING_JAR);

    // --- Malt house ingredients ---
    public static final RegistryObject<Item> DRIED_HOPS = simple("dried_hops");
    public static final RegistryObject<Item> GREEN_BARLEY_MALT = simple("green_barley_malt");
    public static final RegistryObject<Item> PALE_MALT = simple("pale_malt");
    public static final RegistryObject<Item> AMBER_MALT = simple("amber_malt");
    public static final RegistryObject<Item> BLACK_MALT = simple("black_malt");
    public static final RegistryObject<Item> PALE_GRIST = simple("pale_grist");
    public static final RegistryObject<Item> AMBER_GRIST = simple("amber_grist");
    public static final RegistryObject<Item> BLACK_GRIST = simple("black_grist");
    public static final RegistryObject<Item> WHEAT_FLOUR = simple("wheat_flour");
    // Wheat malt for wheat beer
    public static final RegistryObject<Item> GREEN_WHEAT_MALT = simple("green_wheat_malt");
    public static final RegistryObject<Item> WHEAT_MALT = simple("wheat_malt");
    public static final RegistryObject<Item> WHEAT_GRIST = simple("wheat_grist");

    // --- Brewhouse ---
    public static final RegistryObject<Item> SPENT_GRAIN = simple("spent_grain");
    public static final RegistryObject<Item> SOURDOUGH_STARTER = ITEMS.register("sourdough_starter",
            () -> new SourdoughStarterItem(new Item.Properties()));
    public static final RegistryObject<Item> ALE_YEAST = simple("ale_yeast");
    public static final RegistryObject<Item> WINE_YEAST = simple("wine_yeast");
    /** Ale yeast cultured in the cold: lager. */
    public static final RegistryObject<Item> LAGER_YEAST = simple("lager_yeast");

    // --- Cellar ---
    public static final Map<CaskWood, RegistryObject<Item>> CASKS = new EnumMap<>(CaskWood.class);

    static {
        ModBlocks.CASKS.forEach((wood, block) -> CASKS.put(wood,
                ITEMS.register(block.getId().getPath(), () -> new CaskItem(block.get(), new Item.Properties()))));
    }

    public static final RegistryObject<Item> OAK_CASK = CASKS.get(CaskWood.OAK);
    public static final RegistryObject<Item> KEG = ITEMS.register("keg", () -> new CaskItem(ModBlocks.KEG.get(), new Item.Properties()));
    public static final RegistryObject<Item> WINE_RACK = block("wine_rack", ModBlocks.WINE_RACK);
    public static final RegistryObject<Item> BOTTLE_SHELF = block("bottle_shelf", ModBlocks.BOTTLE_SHELF);
    public static final RegistryObject<Item> WINE_DISPLAY = block("wine_display", ModBlocks.WINE_DISPLAY);
    public static final RegistryObject<Item> TAP = simple("tap");
    public static final RegistryObject<Item> MUG = ITEMS.register("mug", () -> new VesselItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> WINE_BOTTLE = ITEMS.register("wine_bottle", () -> new VesselItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPIRIT_BOTTLE = ITEMS.register("spirit_bottle", () -> new VesselItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HYDROMETER = ITEMS.register("hydrometer", () -> new HydrometerItem(new Item.Properties().stacksTo(1)));

    // --- Kitchen: flours, doughs and breads (GDD sections 9.6, 15) ---
    public static final RegistryObject<Item> RYE_FLOUR = simple("rye_flour");
    public static final RegistryObject<Item> CORNMEAL = simple("cornmeal");
    public static final RegistryObject<Item> ROLLED_OATS = simple("rolled_oats");
    public static final RegistryObject<Item> DOUGH = simple("dough");
    public static final RegistryObject<Item> RYE_DOUGH = simple("rye_dough");
    public static final RegistryObject<Item> SOURDOUGH_DOUGH = simple("sourdough_dough");
    public static final RegistryObject<Item> SPENT_GRAIN_DOUGH = simple("spent_grain_dough");
    public static final RegistryObject<Item> BEER_BREAD_DOUGH = simple("beer_bread_dough");
    public static final RegistryObject<Item> CORNBREAD_BATTER = simple("cornbread_batter");
    public static final RegistryObject<Item> MASA = simple("masa");
    public static final RegistryObject<Item> RYE_BREAD = food("rye_bread", ModFoods.RYE_BREAD);
    public static final RegistryObject<Item> SOURDOUGH_BREAD = ITEMS.register("sourdough_bread",
            () -> new DishItem(new Item.Properties().food(ModFoods.SOURDOUGH_BREAD), null, true));
    public static final RegistryObject<Item> CORNBREAD = food("cornbread", ModFoods.CORNBREAD);
    public static final RegistryObject<Item> SPENT_GRAIN_BREAD = food("spent_grain_bread", ModFoods.SPENT_GRAIN_BREAD);
    public static final RegistryObject<Item> BEER_BREAD = food("beer_bread", ModFoods.BEER_BREAD);
    public static final RegistryObject<Item> TORTILLA = food("tortilla", ModFoods.TORTILLA);
    public static final RegistryObject<Item> ROASTED_CORN = food("roasted_corn", ModFoods.ROASTED_CORN);

    // --- Kitchen: cooked in the Brew Kettle (GDD section 15) ---
    public static final RegistryObject<Item> BEEF_AND_ALE_STEW = bowl("beef_and_ale_stew", ModFoods.BEEF_AND_ALE_STEW, false);
    public static final RegistryObject<Item> MUTTON_AND_BARLEY_STEW = bowl("mutton_and_barley_stew", ModFoods.MUTTON_AND_BARLEY_STEW, false);
    public static final RegistryObject<Item> CHILI_CON_CARNE = bowl("chili_con_carne", ModFoods.CHILI_CON_CARNE, false);
    public static final RegistryObject<Item> BORSCHT = bowl("borscht", ModFoods.BORSCHT, false);
    public static final RegistryObject<Item> TOMATO_SOUP = bowl("tomato_soup", ModFoods.TOMATO_SOUP, false);
    public static final RegistryObject<Item> PORRIDGE = bowl("porridge", ModFoods.PORRIDGE, true);
    public static final RegistryObject<Item> COOKED_RICE = bowl("cooked_rice", ModFoods.COOKED_RICE, false);
    public static final RegistryObject<Item> RICE_BALL = food("rice_ball", ModFoods.RICE_BALL);
    public static final RegistryObject<Item> POPCORN = food("popcorn", ModFoods.POPCORN);
    public static final RegistryObject<Item> CRANBERRY_SAUCE = bowl("cranberry_sauce", ModFoods.CRANBERRY_SAUCE, false);
    public static final RegistryObject<Item> BLUEBERRY_JAM = jam("blueberry_jam");
    public static final RegistryObject<Item> BLACKBERRY_JAM = jam("blackberry_jam");
    public static final RegistryObject<Item> ELDERBERRY_JAM = jam("elderberry_jam");
    public static final RegistryObject<Item> SWEET_BERRY_JAM = jam("sweet_berry_jam");
    public static final RegistryObject<Item> CHERRY_JAM = jam("cherry_jam");
    public static final RegistryObject<Item> PLUM_JAM = jam("plum_jam");
    public static final RegistryObject<Item> PEACH_JAM = jam("peach_jam");
    /** The jam family's orange member, peel and all. */
    public static final RegistryObject<Item> MARMALADE = jam("marmalade");
    /** Every jam, for recipes that take any of them. */
    public static final List<RegistryObject<Item>> JAMS = List.of(BLUEBERRY_JAM, BLACKBERRY_JAM, ELDERBERRY_JAM, SWEET_BERRY_JAM,
            CHERRY_JAM, PLUM_JAM, PEACH_JAM, MARMALADE);
    public static final RegistryObject<Item> JAM_TOAST = food("jam_toast", ModFoods.JAM_TOAST);

    // --- Kitchen: preserves (GDD sections 8, 9.5, 15) ---
    public static final RegistryObject<Item> MOTHER_OF_VINEGAR = simple("mother_of_vinegar");
    public static final RegistryObject<Item> PICKLES = food("pickles", ModFoods.PICKLES);
    public static final RegistryObject<Item> SAUERKRAUT = food("sauerkraut", ModFoods.SAUERKRAUT);
    public static final RegistryObject<Item> KIMCHI = food("kimchi", ModFoods.KIMCHI);
    public static final RegistryObject<Item> DRIED_BERRIES = food("dried_berries", ModFoods.DRIED_BERRIES);
    public static final RegistryObject<Item> GRANOLA = food("granola", ModFoods.GRANOLA);
    public static final RegistryObject<Item> HARVEST_FEAST = block("harvest_feast", ModBlocks.HARVEST_FEAST);

    // --- Orchard and vineyard (GDD sections 6.2, 9.7, 15) ---
    /** Cut from grape vines with shears (stuffed grape leaves). */
    public static final RegistryObject<Item> GRAPE_LEAVES = simple("grape_leaves");
    public static final RegistryObject<Item> RAISINS = food("raisins", ModFoods.RAISINS);
    public static final RegistryObject<Item> GOLDEN_RAISINS = food("golden_raisins", ModFoods.RAISINS);
    public static final RegistryObject<Item> PRUNES = food("prunes", ModFoods.RAISINS);
    public static final RegistryObject<Item> DRIED_CHERRIES = food("dried_cherries", ModFoods.RAISINS);
    public static final RegistryObject<Item> DRIED_APPLES = food("dried_apples", ModFoods.RAISINS);
    public static final RegistryObject<Item> DRIED_PEACHES = food("dried_peaches", ModFoods.RAISINS);
    public static final RegistryObject<Item> DRIED_PEARS = food("dried_pears", ModFoods.RAISINS);

    // --- Orchard kitchen (GDD sections 10.5, 15, 17) ---
    /** Olive oil in a glass bottle, filled at the press like a drink; recipes give the bottle back. */
    public static final RegistryObject<Item> OLIVE_OIL = ITEMS.register("olive_oil", () -> new BottledLiquidItem(() -> ModFluids.OLIVE_OIL.get(),
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    /** Sorghum juice boiled down in the kettle: a sweetener, used like sugar (the bottle comes back). */
    public static final RegistryObject<Item> SORGHUM_SYRUP = ITEMS.register("sorghum_syrup",
            () -> new Item(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> CURED_OLIVES = food("cured_olives", ModFoods.CURED_OLIVES);
    public static final RegistryObject<Item> STUFFED_GRAPE_LEAVES = food("stuffed_grape_leaves", ModFoods.STUFFED_GRAPE_LEAVES);
    public static final RegistryObject<Item> BRUSCHETTA = food("bruschetta", ModFoods.BRUSCHETTA);
    public static final RegistryObject<Item> GARLIC_BREAD = food("garlic_bread", ModFoods.GARLIC_BREAD);
    public static final RegistryObject<Item> SALAD = bowl("salad", ModFoods.SALAD, false);
    public static final RegistryObject<Item> RISOTTO = bowl("risotto", ModFoods.RISOTTO, false);
    public static final RegistryObject<Item> COQ_AU_VIN = bowl("coq_au_vin", ModFoods.COQ_AU_VIN, false);

    // --- Winery (GDD sections 7, 9.2, 9.7) ---
    public static final RegistryObject<Item> CRUSHING_TUB = block("crushing_tub", ModBlocks.CRUSHING_TUB);
    public static final RegistryObject<Item> FRUIT_PRESS = block("fruit_press", ModBlocks.FRUIT_PRESS);
    /** Byproducts of the press: compost, fuel, paper, and (later) grappa. */
    public static final RegistryObject<Item> GRAPE_POMACE = simple("grape_pomace");
    public static final RegistryObject<Item> FRUIT_POMACE = simple("fruit_pomace");
    public static final RegistryObject<Item> OLIVE_POMACE = fuel("olive_pomace", 400);
    public static final RegistryObject<Item> BAGASSE = fuel("bagasse", 200);
    public static final RegistryObject<Item> HARVEST_FEAST_SERVING = bowl("harvest_feast_serving", ModFoods.HARVEST_FEAST_SERVING, false);

    public static final RegistryObject<Item> BLACK_FOREST_CAKE = block("black_forest_cake", ModBlocks.BLACK_FOREST_CAKE);
    public static final RegistryObject<Item> BLACK_FOREST_CAKE_SLICE = food("black_forest_cake_slice", ModFoods.CAKE_SLICE);

    // --- Sake (GDD section 9.3): polished, steamed, grown into koji ---
    public static final RegistryObject<Item> POLISHED_RICE = simple("polished_rice");
    public static final RegistryObject<Item> RICE_BRAN = simple("rice_bran");
    public static final RegistryObject<Item> STEAMED_RICE = food("steamed_rice", ModFoods.STEAMED_RICE);
    public static final RegistryObject<Item> KOJI_RICE = simple("koji_rice");
    // --- Coffee (GDD sections 6.2, 10.5): green beans roasted, then ground ---
    public static final RegistryObject<Item> ROASTED_COFFEE = simple("roasted_coffee");
    public static final RegistryObject<Item> GROUND_COFFEE = simple("ground_coffee");

    // --- Distillery (GDD sections 7, 9.4) ---
    public static final RegistryObject<Item> POT_STILL = ITEMS.register("pot_still",
            () -> new io.github.spencerharris192.seedtocellar.decor.CopperBlockItem.DoubleHigh(ModBlocks.POT_STILL.get(), new Item.Properties()));
    /** Cane juice boiled down: bottled from the kettle like olive oil, a sweetener; fermented, the rum wash. */
    public static final RegistryObject<Item> MOLASSES = ITEMS.register("molasses", () -> new BottledLiquidItem(() -> ModFluids.MOLASSES.get(),
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    /** Agave juice boiled down into a bottle: a sweetener. */
    public static final RegistryObject<Item> AGAVE_SYRUP = ITEMS.register("agave_syrup",
            () -> new Item(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
    /** An agave heart roasted in the kiln, sweet and caramel-brown: pressed for agave juice. */
    public static final RegistryObject<Item> ROASTED_AGAVE = simple("roasted_agave");
    /** Green vanilla pods: planted on a jungle log's side, or cured on the Drying Rack. */
    public static final RegistryObject<Item> VANILLA_POD = ITEMS.register("vanilla_pod",
            () -> new net.minecraft.world.item.ItemNameBlockItem(ModBlocks.VANILLA.get(), new Item.Properties()));
    /** Dark, fragrant cured vanilla: spiced rum and baking. */
    public static final RegistryObject<Item> CURED_VANILLA = simple("cured_vanilla");
    /** Fits on the Pot Still's swan neck: botanicals in it flavor vodka into gin. */
    public static final RegistryObject<Item> GIN_BASKET = simple("gin_basket");
    /** Zest of citrus: botanicals for gin, flavoring for bitters and spiced rum. */
    public static final RegistryObject<Item> LEMON_PEEL = simple("lemon_peel");
    public static final RegistryObject<Item> ORANGE_PEEL = simple("orange_peel");
    /** Pressed agave fiber: string, or compost. */
    public static final RegistryObject<Item> AGAVE_FIBER = simple("agave_fiber");

    private static RegistryObject<Item> sickle(String name, Tier tier, Item.Properties properties) {
        return ITEMS.register(name, () -> new SickleItem(tier, properties));
    }

    /** A soup, stew or other dish served in a bowl; the bowl comes back. Stacks to 16. */
    private static RegistryObject<Item> bowl(String name, FoodProperties food, boolean curesHangover) {
        return ITEMS.register(name, () -> new DishItem(new Item.Properties().food(food).stacksTo(16), () -> Items.BOWL, curesHangover));
    }

    /** A jam in a small glass jar (a glass bottle); the bottle comes back. */
    private static RegistryObject<Item> jam(String name) {
        return ITEMS.register(name, () -> new DishItem(new Item.Properties().food(ModFoods.JAM).stacksTo(16), () -> Items.GLASS_BOTTLE, false));
    }

    private static RegistryObject<Item> food(String name, FoodProperties food) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().food(food)));
    }

    /** Burns in a furnace for `ticks` (a stick is 100, a plank 300). */
    private static RegistryObject<Item> fuel(String name, int ticks) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()) {
            @Override
            public int getBurnTime(ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.item.crafting.RecipeType<?> type) {
                return ticks;
            }
        });
    }

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> block(String name, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
