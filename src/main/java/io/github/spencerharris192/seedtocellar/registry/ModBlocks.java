package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.winery.RackLayout;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.KegBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlock;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlock;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlock;
import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.farming.HopsBlock;
import io.github.spencerharris192.seedtocellar.farming.ThatchBlock;
import io.github.spencerharris192.seedtocellar.food.FeastBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisBlock;
import io.github.spencerharris192.seedtocellar.farming.WildPlantBlock;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlock;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlock;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** All blocks. IDs are permanent once released. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SeedToCellar.MOD_ID);
    /** Every bale and sack, in registration order (datagen makes their recipes, tags, models and names). */
    public static final List<StorageBlocks.Storage> STORAGE = new ArrayList<>();

    // --- Farming --- (row crops such as barley register themselves: see farming.Crops)
    public static final RegistryObject<Block> WILD_HOPS = BLOCKS.register("wild_hops",
            () -> new WildPlantBlock(Crop.wildPlantProperties()));
    public static final RegistryObject<Block> TRELLIS = BLOCKS.register("trellis",
            () -> new TrellisBlock(trellis()));
    public static final RegistryObject<Block> HOPS = BLOCKS.register("hops",
            () -> new HopsBlock(trellis().randomTicks()));
    public static final RegistryObject<Block> FERTILE_FARMLAND = BLOCKS.register("fertile_farmland",
            () -> new FertileFarmlandBlock(BlockBehaviour.Properties.copy(Blocks.FARMLAND).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final RegistryObject<Block> COMPOST_BIN = BLOCKS.register("compost_bin",
            () -> new CompostBinBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> DRYING_RACK = BLOCKS.register("drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> THATCH = BLOCKS.register("thatch", () -> new ThatchBlock(thatch()));
    public static final RegistryObject<Block> THATCH_STAIRS = BLOCKS.register("thatch_stairs",
            () -> new ThatchBlock.Stairs(() -> THATCH.get().defaultBlockState(), thatch()));
    public static final RegistryObject<Block> THATCH_SLAB = BLOCKS.register("thatch_slab", () -> new ThatchBlock.Slab(thatch()));

    // --- Storage: nine to a bale, eight to a sack or crate ---
    public static final RegistryObject<Block> BARLEY_BALE = bale("barley_bale", () -> Crops.BARLEY.produce(), "barley", "Barley Bale", MapColor.COLOR_YELLOW);
    public static final RegistryObject<Block> RYE_BALE = bale("rye_bale", () -> Crops.RYE.produce(), "rye", "Rye Bale", MapColor.SAND);
    public static final RegistryObject<Block> OAT_BALE = bale("oat_bale", () -> Crops.OATS.produce(), "oats", "Oat Bale", MapColor.SAND);
    public static final RegistryObject<Block> RICE_BALE = bale("rice_bale", () -> Crops.RICE.produce(), "rice", "Rice Bale", MapColor.COLOR_YELLOW);
    public static final RegistryObject<Block> WHEAT_FLOUR_SACK = sack("wheat_flour_sack", () -> ModItems.WHEAT_FLOUR.get(), "wheat_flour", "Wheat Flour Sack");
    public static final RegistryObject<Block> PALE_MALT_SACK = sack("pale_malt_sack", () -> ModItems.PALE_MALT.get(), "pale_malt", "Pale Malt Sack");
    public static final RegistryObject<Block> COFFEE_SACK = sack("coffee_sack", () -> Crops.COFFEE.produce(), "coffee_beans", "Coffee Sack");
    public static final RegistryObject<Block> SUGAR_SACK = sack("sugar_sack", () -> Items.SUGAR, "sugar", "Sugar Sack");
    public static final RegistryObject<Block> APPLE_CRATE = crate("apple_crate", () -> Items.APPLE, "apple", "Apple Crate");
    public static final RegistryObject<Block> RED_GRAPE_CRATE = crate("red_grape_crate", () -> Crops.RED_GRAPE.produce(), "red_grape", "Red Grape Crate");
    public static final RegistryObject<Block> WHITE_GRAPE_CRATE = crate("white_grape_crate", () -> Crops.WHITE_GRAPE.produce(), "white_grape", "White Grape Crate");
    public static final RegistryObject<Block> CHERRY_CRATE = crate("cherry_crate", () -> FruitTrees.CHERRY.fruit(), "cherry", "Cherry Crate");
    public static final RegistryObject<Block> PLUM_CRATE = crate("plum_crate", () -> FruitTrees.PLUM.fruit(), "plum", "Plum Crate");
    public static final RegistryObject<Block> PEACH_CRATE = crate("peach_crate", () -> FruitTrees.PEACH.fruit(), "peach", "Peach Crate");
    public static final RegistryObject<Block> PEAR_CRATE = crate("pear_crate", () -> FruitTrees.PEAR.fruit(), "pear", "Pear Crate");
    public static final RegistryObject<Block> LEMON_CRATE = crate("lemon_crate", () -> FruitTrees.LEMON.fruit(), "lemon", "Lemon Crate");
    public static final RegistryObject<Block> ORANGE_CRATE = crate("orange_crate", () -> FruitTrees.ORANGE.fruit(), "orange", "Orange Crate");

    // --- Kitchen ---
    public static final RegistryObject<Block> HARVEST_FEAST = BLOCKS.register("harvest_feast",
            () -> new FeastBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD)
                    .noOcclusion(), () -> ModItems.HARVEST_FEAST_SERVING.get()));

    // --- Brewing stations ---
    public static final RegistryObject<Block> MALTING_TUB = BLOCKS.register("malting_tub",
            () -> new MaltingTubBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> KILN = BLOCKS.register("kiln",
            () -> new KilnBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.5F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(KilnBlock.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> MILLSTONE = BLOCKS.register("millstone",
            () -> new MillstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> BREW_KETTLE = BLOCKS.register("brew_kettle",
            () -> new BrewKettleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.COPPER).noOcclusion()));
    public static final RegistryObject<Block> FERMENTING_VAT = BLOCKS.register("fermenting_vat",
            () -> new FermentingVatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> PRESERVING_JAR = BLOCKS.register("preserving_jar",
            () -> new PreservingJarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3F)
                    .sound(SoundType.GLASS).noOcclusion()));

    // --- Winery ---
    public static final RegistryObject<Block> CRUSHING_TUB = BLOCKS.register("crushing_tub",
            () -> new CrushingTubBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> FRUIT_PRESS = BLOCKS.register("fruit_press",
            () -> new FruitPressBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));

    // --- Cellar ---
    /** A cask in each of the ten vanilla woods (GDD section 13): <wood>_cask. */
    public static final Map<CaskWood, RegistryObject<Block>> CASKS = new EnumMap<>(CaskWood.class);

    static {
        for (CaskWood wood : CaskWood.values()) {
            CASKS.put(wood, BLOCKS.register(wood.id() + "_cask", () -> {
                BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(wood.mapColor()).strength(2.5F)
                        .sound(wood.sound()).noOcclusion();
                return new CaskBlock(wood, wood.nether() ? properties : properties.ignitedByLava());
            }));
        }
    }

    public static final RegistryObject<Block> OAK_CASK = CASKS.get(CaskWood.OAK);
    public static final RegistryObject<Block> KEG = BLOCKS.register("keg",
            () -> new KegBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> WINE_RACK = BLOCKS.register("wine_rack",
            () -> new WineRackBlock(RackLayout.WINE_RACK, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistryObject<Block> BOTTLE_SHELF = BLOCKS.register("bottle_shelf",
            () -> new WineRackBlock(RackLayout.BOTTLE_SHELF, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> MUG_RACK = BLOCKS.register("mug_rack",
            () -> new WineRackBlock(RackLayout.MUG_RACK, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)
                    .sound(SoundType.WOOD).noOcclusion().noCollission().ignitedByLava()));
    /** A garland of fresh hop bines draped along a wall; neighbors join into one. */
    public static final RegistryObject<Block> HOP_GARLAND = BLOCKS.register("hop_garland",
            () -> new io.github.spencerharris192.seedtocellar.decor.WallDecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .noCollission().strength(0.2F).sound(SoundType.GRASS).ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY), Block.box(0, 7, 13, 16, 15, 16)));
    /** A painted pub sign on an iron bracket. */
    public static final RegistryObject<Block> TAVERN_SIGN = BLOCKS.register("tavern_sign",
            () -> new io.github.spencerharris192.seedtocellar.decor.TavernSignBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .noCollission().strength(1F).sound(SoundType.HANGING_SIGN).ignitedByLava()));
    public static final RegistryObject<Block> WINE_DISPLAY = BLOCKS.register("wine_display",
            () -> new WineRackBlock(RackLayout.WINE_DISPLAY, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));

    /** Vanilla: an orchid vine climbing a jungle log (GDD growth style J). */
    /** The tavern set (GDD section 17.3): a Bar Counter and a Bar Stool in every cask wood. */
    public static final Map<CaskWood, RegistryObject<Block>> BAR_COUNTERS = new EnumMap<>(CaskWood.class);
    public static final Map<CaskWood, RegistryObject<Block>> BAR_STOOLS = new EnumMap<>(CaskWood.class);

    static {
        for (CaskWood wood : CaskWood.values()) {
            BAR_COUNTERS.put(wood, BLOCKS.register(wood.id() + "_bar_counter", () -> new io.github.spencerharris192.seedtocellar.decor.BarCounterBlock(
                    woodProperties(wood).strength(2F, 3F).noOcclusion())));
            BAR_STOOLS.put(wood, BLOCKS.register(wood.id() + "_bar_stool", () -> new io.github.spencerharris192.seedtocellar.decor.BarStoolBlock(
                    woodProperties(wood).strength(1.5F).noOcclusion())));
        }
    }

    private static BlockBehaviour.Properties woodProperties(CaskWood wood) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(wood.mapColor()).sound(wood.sound());
        return wood.nether() ? properties : properties.ignitedByLava();
    }

    /** Hanging bundles (GDD section 17.3): decor made from nine of a crop, hung under a block or on a wall. */
    public static final RegistryObject<Block> HOP_BUNDLE = bundle("hop_bundle", SoundType.GRASS);
    public static final RegistryObject<Block> LAVENDER_BUNDLE = bundle("lavender_bundle", SoundType.GRASS);
    public static final RegistryObject<Block> GARLIC_BRAID = bundle("garlic_braid", SoundType.CROP);
    public static final RegistryObject<Block> CHILI_STRING = bundle("chili_string", SoundType.CROP);

    private static RegistryObject<Block> bundle(String name, SoundType sound) {
        return BLOCKS.register(name, () -> new io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT).noCollission().strength(0.2F).sound(sound).ignitedByLava()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    }

    /** Drinks set down on a surface (sneak and right-click with a drink); no item of its own. */
    public static final RegistryObject<Block> PLACED_DRINKS = BLOCKS.register("placed_drinks",
            () -> new io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .strength(0.2F).sound(SoundType.GLASS).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
    public static final RegistryObject<Block> VANILLA = BLOCKS.register("vanilla",
            () -> new io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .noCollission().randomTicks().strength(0.2F).sound(SoundType.VINE).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY),
                    io.github.spencerharris192.seedtocellar.farming.Climate.HOT));

    /** Black Forest Cake (GDD section 15.2): chocolate, cream, cherries and kirsch; six slices. */
    public static final RegistryObject<Block> BLACK_FOREST_CAKE = BLOCKS.register("black_forest_cake",
            () -> new io.github.spencerharris192.seedtocellar.food.LayerCakeBlock(BlockBehaviour.Properties.copy(Blocks.CAKE),
                    () -> ModItems.BLACK_FOREST_CAKE_SLICE.get()));

    // --- Distillery ---
    public static final RegistryObject<Block> POT_STILL = BLOCKS.register("pot_still",
            () -> new io.github.spencerharris192.seedtocellar.distillery.PotStillBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE).strength(3.0F).requiresCorrectToolForDrops().sound(SoundType.COPPER).noOcclusion()));

    private static RegistryObject<Block> bale(String id, Supplier<? extends ItemLike> contents, String tag, String name,
                                              MapColor color) {
        RegistryObject<Block> block = BLOCKS.register(id, () -> new StorageBlocks.Bale(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK).mapColor(color)));
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    private static RegistryObject<Block> sack(String id, Supplier<? extends ItemLike> contents, String tag, String name) {
        RegistryObject<Block> block = BLOCKS.register(id, () -> new StorageBlocks.Sack(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN).strength(0.8F).sound(SoundType.WOOL).ignitedByLava()));
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    private static RegistryObject<Block> crate(String id, Supplier<? extends ItemLike> contents, String tag, String name) {
        RegistryObject<Block> block = BLOCKS.register(id, () -> new StorageBlocks.Crate(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).ignitedByLava()));
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    /** Like a hay bale: soft, rustles like grass, burns (see ThatchBlock). */
    private static BlockBehaviour.Properties thatch() {
        return BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK);
    }

    /** A trellis, or a trellis with a vine on it (vines add random ticks). */
    public static BlockBehaviour.Properties trellis() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.WOOD)
                .noOcclusion().ignitedByLava();
    }

    private ModBlocks() {}
}
