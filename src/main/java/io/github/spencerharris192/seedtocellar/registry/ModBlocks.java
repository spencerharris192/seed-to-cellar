package io.github.spencerharris192.seedtocellar.registry;

import net.neoforged.neoforge.registries.DeferredBlock;
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
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** All blocks. IDs are permanent once released. */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SeedToCellar.MOD_ID);
    /** Every bale and sack, in registration order (datagen makes their recipes, tags, models and names). */
    public static final List<StorageBlocks.Storage> STORAGE = new ArrayList<>();

    // --- Farming --- (row crops such as barley register themselves: see farming.Crops)
    public static final DeferredBlock<Block> WILD_HOPS = BLOCKS.registerBlock("wild_hops", WildPlantBlock::new, () -> Crop.wildPlantProperties());
    public static final DeferredBlock<Block> TRELLIS = BLOCKS.registerBlock("trellis", TrellisBlock::new, () -> trellis());
    public static final DeferredBlock<Block> HOPS = BLOCKS.registerBlock("hops", HopsBlock::new, () -> trellis().randomTicks());
    public static final DeferredBlock<Block> FERTILE_FARMLAND = BLOCKS.registerBlock("fertile_farmland", FertileFarmlandBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND).mapColor(MapColor.TERRACOTTA_BROWN));
    public static final DeferredBlock<Block> COMPOST_BIN = BLOCKS.registerBlock("compost_bin", CompostBinBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> DRYING_RACK = BLOCKS.registerBlock("drying_rack", DryingRackBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> THATCH = BLOCKS.registerBlock("thatch", ThatchBlock::new, () -> thatch());
    public static final DeferredBlock<Block> THATCH_STAIRS = BLOCKS.registerBlock("thatch_stairs", p -> new ThatchBlock.Stairs(THATCH.get().defaultBlockState(), p), () -> thatch());
    public static final DeferredBlock<Block> THATCH_SLAB = BLOCKS.registerBlock("thatch_slab", ThatchBlock.Slab::new, () -> thatch());

    // --- Storage: nine to a bale, eight to a sack or crate ---
    public static final DeferredBlock<Block> BARLEY_BALE = bale("barley_bale", () -> Crops.BARLEY.produce(), "barley", "Barley Bale", MapColor.COLOR_YELLOW);
    public static final DeferredBlock<Block> RYE_BALE = bale("rye_bale", () -> Crops.RYE.produce(), "rye", "Rye Bale", MapColor.SAND);
    public static final DeferredBlock<Block> OAT_BALE = bale("oat_bale", () -> Crops.OATS.produce(), "oats", "Oat Bale", MapColor.SAND);
    public static final DeferredBlock<Block> RICE_BALE = bale("rice_bale", () -> Crops.RICE.produce(), "rice", "Rice Bale", MapColor.COLOR_YELLOW);
    public static final DeferredBlock<Block> WHEAT_FLOUR_SACK = sack("wheat_flour_sack", () -> ModItems.WHEAT_FLOUR.get(), "wheat_flour", "Wheat Flour Sack");
    public static final DeferredBlock<Block> PALE_MALT_SACK = sack("pale_malt_sack", () -> ModItems.PALE_MALT.get(), "pale_malt", "Pale Malt Sack");
    public static final DeferredBlock<Block> COFFEE_SACK = sack("coffee_sack", () -> Crops.COFFEE.produce(), "coffee_beans", "Coffee Sack");
    public static final DeferredBlock<Block> SUGAR_SACK = sack("sugar_sack", () -> Items.SUGAR, "sugar", "Sugar Sack");
    public static final DeferredBlock<Block> APPLE_CRATE = crate("apple_crate", () -> Items.APPLE, "apple", "Apple Crate");
    public static final DeferredBlock<Block> RED_GRAPE_CRATE = crate("red_grape_crate", () -> Crops.RED_GRAPE.produce(), "red_grape", "Red Grape Crate");
    public static final DeferredBlock<Block> WHITE_GRAPE_CRATE = crate("white_grape_crate", () -> Crops.WHITE_GRAPE.produce(), "white_grape", "White Grape Crate");
    public static final DeferredBlock<Block> CHERRY_CRATE = crate("cherry_crate", () -> FruitTrees.CHERRY.fruit(), "cherry", "Cherry Crate");
    public static final DeferredBlock<Block> PLUM_CRATE = crate("plum_crate", () -> FruitTrees.PLUM.fruit(), "plum", "Plum Crate");
    public static final DeferredBlock<Block> PEACH_CRATE = crate("peach_crate", () -> FruitTrees.PEACH.fruit(), "peach", "Peach Crate");
    public static final DeferredBlock<Block> PEAR_CRATE = crate("pear_crate", () -> FruitTrees.PEAR.fruit(), "pear", "Pear Crate");
    public static final DeferredBlock<Block> LEMON_CRATE = crate("lemon_crate", () -> FruitTrees.LEMON.fruit(), "lemon", "Lemon Crate");
    public static final DeferredBlock<Block> ORANGE_CRATE = crate("orange_crate", () -> FruitTrees.ORANGE.fruit(), "orange", "Orange Crate");

    // --- Kitchen ---
    public static final DeferredBlock<Block> HARVEST_FEAST = BLOCKS.registerBlock("harvest_feast", p -> new FeastBlock(p, () -> ModItems.HARVEST_FEAST_SERVING.get()), () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD)
                    .noOcclusion());

    // --- Brewing stations ---
    public static final DeferredBlock<Block> MALTING_TUB = BLOCKS.registerBlock("malting_tub", MaltingTubBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> KILN = BLOCKS.registerBlock("kiln", KilnBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.5F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(KilnBlock.LIT) ? 13 : 0));
    public static final DeferredBlock<Block> MILLSTONE = BLOCKS.registerBlock("millstone", MillstoneBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE).noOcclusion());
    public static final DeferredBlock<Block> BREW_KETTLE = BLOCKS.registerBlock("brew_kettle", BrewKettleBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.COPPER).noOcclusion());
    public static final DeferredBlock<Block> FERMENTING_VAT = BLOCKS.registerBlock("fermenting_vat", FermentingVatBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> PRESERVING_JAR = BLOCKS.registerBlock("preserving_jar", PreservingJarBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3F)
                    .sound(SoundType.GLASS).noOcclusion());

    // --- Winery ---
    public static final DeferredBlock<Block> CRUSHING_TUB = BLOCKS.registerBlock("crushing_tub", CrushingTubBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> FRUIT_PRESS = BLOCKS.registerBlock("fruit_press", FruitPressBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());

    // --- Cellar ---
    /** A cask in each of the twelve vanilla woods (GDD section 13): <wood>_cask. */
    public static final Map<CaskWood, DeferredBlock<Block>> CASKS = new EnumMap<>(CaskWood.class);

    static {
        for (CaskWood wood : CaskWood.values()) {
            CASKS.put(wood, BLOCKS.registerBlock(wood.id() + "_cask", p -> new CaskBlock(wood, p),
                    () -> woodProperties(wood).strength(2.5F).noOcclusion()));
        }
    }

    public static final DeferredBlock<Block> OAK_CASK = CASKS.get(CaskWood.OAK);
    public static final DeferredBlock<Block> KEG = BLOCKS.registerBlock("keg", KegBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> WINE_RACK = BLOCKS.registerBlock("wine_rack", p -> new WineRackBlock(RackLayout.WINE_RACK, p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).ignitedByLava());
    public static final DeferredBlock<Block> BOTTLE_SHELF = BLOCKS.registerBlock("bottle_shelf", p -> new WineRackBlock(RackLayout.BOTTLE_SHELF, p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    public static final DeferredBlock<Block> MUG_RACK = BLOCKS.registerBlock("mug_rack", p -> new WineRackBlock(RackLayout.MUG_RACK, p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)
                    .sound(SoundType.WOOD).noOcclusion().noCollision().ignitedByLava());
    /** A garland of fresh hop bines draped along a wall; neighbors join into one. */
    public static final DeferredBlock<Block> HOP_GARLAND = BLOCKS.registerBlock("hop_garland", p -> new io.github.spencerharris192.seedtocellar.decor.WallDecorBlock(p, Block.box(0, 7, 13, 16, 15, 16)), () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .noCollision().strength(0.2F).sound(SoundType.GRASS).ignitedByLava()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
    /** A painted pub sign on an iron bracket. */
    public static final DeferredBlock<Block> TAVERN_SIGN = BLOCKS.registerBlock("tavern_sign", io.github.spencerharris192.seedtocellar.decor.TavernSignBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .noCollision().strength(1F).sound(SoundType.HANGING_SIGN).ignitedByLava());
    public static final DeferredBlock<Block> WINE_DISPLAY = BLOCKS.registerBlock("wine_display", p -> new WineRackBlock(RackLayout.WINE_DISPLAY, p), () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava());

    /** Vanilla: an orchid vine climbing a jungle log (GDD growth style J). */
    /** The tavern set (GDD section 17.3): a Bar Counter and a Bar Stool in every cask wood. */
    public static final Map<CaskWood, DeferredBlock<Block>> BAR_COUNTERS = new EnumMap<>(CaskWood.class);
    public static final Map<CaskWood, DeferredBlock<Block>> BAR_STOOLS = new EnumMap<>(CaskWood.class);

    static {
        for (CaskWood wood : CaskWood.values()) {
            BAR_COUNTERS.put(wood, BLOCKS.registerBlock(wood.id() + "_bar_counter", io.github.spencerharris192.seedtocellar.decor.BarCounterBlock::new, () -> woodProperties(wood).strength(2F, 3F).noOcclusion()));
            BAR_STOOLS.put(wood, BLOCKS.registerBlock(wood.id() + "_bar_stool", io.github.spencerharris192.seedtocellar.decor.BarStoolBlock::new, () -> woodProperties(wood).strength(1.5F).noOcclusion()));
        }
    }

    private static BlockBehaviour.Properties woodProperties(CaskWood wood) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(wood.mapColor()).sound(wood.sound());
        return wood.nether() ? properties : properties.ignitedByLava();
    }

    /** Hanging bundles (GDD section 17.3): decor made from nine of a crop, hung under a block or on a wall. */
    public static final DeferredBlock<Block> HOP_BUNDLE = bundle("hop_bundle", SoundType.GRASS);
    public static final DeferredBlock<Block> LAVENDER_BUNDLE = bundle("lavender_bundle", SoundType.GRASS);
    public static final DeferredBlock<Block> GARLIC_BRAID = bundle("garlic_braid", SoundType.CROP);
    public static final DeferredBlock<Block> CHILI_STRING = bundle("chili_string", SoundType.CROP);

    private static DeferredBlock<Block> bundle(String name, SoundType sound) {
        return BLOCKS.registerBlock(name, io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock::new, () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT).noCollision().strength(0.2F).sound(sound).ignitedByLava()
                .pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
    }

    /** Drinks set down on a surface (sneak and right-click with a drink); no item of its own. */
    public static final DeferredBlock<Block> PLACED_DRINKS = BLOCKS.registerBlock("placed_drinks", io.github.spencerharris192.seedtocellar.decor.PlacedDrinksBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .strength(0.2F).sound(SoundType.GLASS).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
    public static final DeferredBlock<Block> VANILLA = BLOCKS.registerBlock("vanilla", p -> new io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock(p,
                    io.github.spencerharris192.seedtocellar.farming.Climate.HOT), () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                    .noCollision().randomTicks().strength(0.2F).sound(SoundType.VINE).pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));

    /** Black Forest Cake (GDD section 15.2): chocolate, cream, cherries and kirsch; six slices. */
    public static final DeferredBlock<Block> BLACK_FOREST_CAKE = BLOCKS.registerBlock("black_forest_cake", p -> new io.github.spencerharris192.seedtocellar.food.LayerCakeBlock(p,
                    () -> ModItems.BLACK_FOREST_CAKE_SLICE.get()), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE));

    // --- Distillery ---
    public static final DeferredBlock<Block> POT_STILL = BLOCKS.registerBlock("pot_still", io.github.spencerharris192.seedtocellar.distillery.PotStillBlock::new, () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE).strength(3.0F).requiresCorrectToolForDrops().sound(SoundType.COPPER).noOcclusion());

    private static DeferredBlock<Block> bale(String id, Supplier<? extends ItemLike> contents, String tag, String name,
                                              MapColor color) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(id, StorageBlocks.Bale::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK).mapColor(color));
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    private static DeferredBlock<Block> sack(String id, Supplier<? extends ItemLike> contents, String tag, String name) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(id, StorageBlocks.Sack::new, () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN).strength(0.8F).sound(SoundType.WOOL).ignitedByLava());
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    private static DeferredBlock<Block> crate(String id, Supplier<? extends ItemLike> contents, String tag, String name) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(id, StorageBlocks.Crate::new, () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).ignitedByLava());
        STORAGE.add(new StorageBlocks.Storage(block, contents, tag, name));
        return block;
    }

    /** Like a hay bale: soft, rustles like grass, burns (see ThatchBlock). */
    private static BlockBehaviour.Properties thatch() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK);
    }

    /** A trellis, or a trellis with a vine on it (vines add random ticks). */
    public static BlockBehaviour.Properties trellis() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.WOOD)
                .noOcclusion().ignitedByLava();
    }

    private ModBlocks() {}
}
