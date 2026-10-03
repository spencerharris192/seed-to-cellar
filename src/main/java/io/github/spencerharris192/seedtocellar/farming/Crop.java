package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One crop, described once (GDD section 6.2): what it's called, how it grows, its climate,
 * where it grows wild, and whether grass drops its seeds. Registering it creates the growing
 * block, the seeds, the harvested item and the wild plant; data generation, world generation,
 * tags, compost values and config read everything else from here. IDs are permanent.
 */
public final class Crop {
    /** What the harvest is, for tags other mods look for (GDD section 22.1). */
    public enum Kind { GRAIN, VEGETABLE, SPICE, BERRY, FRUIT, HERB }

    /**
     * How it grows (GDD section 6.1): ROW on farmland like wheat; TALL on farmland, two blocks
     * high (corn); PADDY in water one block deep (rice); BUSH, picked again and again like sweet
     * berries; HERB, cut back and regrowing (mint); VINE, climbing a trellis and picked again and
     * again (grapes, planted from cuttings); SUCCULENT, slow on desert soils with no water, cut down for its heart
     * when it flowers (agave, planted from pups). Bushes and herbs are "perennial": they're found in the wild as
     * themselves, already ripe, rather than as a separate wild plant; succulents grow wild as themselves, in flower.
     * Vines have a wild plant.
     */
    public enum Style { ROW, TALL, PADDY, BUSH, HERB, VINE, SUCCULENT }

    /**
     * Where it grows wild: patch size (placement attempts), one patch per `rarity` chunks on
     * average, seeds from breaking one, the chance of the crop itself, and a plain-language
     * place name for tooltips and JEI ("taiga and snowy plains").
     */
    public record Wild(int tries, int rarity, int minSeeds, int maxSeeds, float bonusChance, String where, boolean onSand) {}

    /** Seeds sometimes dropped by grass (5% for barley); coldOnly = only in cold biomes. */
    public record GrassDrop(float chance, boolean coldOnly) {}

    public final String name;
    /** Registry paths: the growing block, the seeds, and the harvest (usually `name`; "blueberries"). */
    public final String blockId, seedId, produceId;
    /** Names: the crop ("Blueberry"), its growing block ("Blueberry Bush"), seeds, and harvest ("Blueberries"). */
    public final String displayName, blockName, seedName, produceName;
    /** Eaten raw (berries, tomatoes, cucumbers), or null. */
    public final FoodProperties food;
    /** Thorny (slows and scratches, like sweet berries) and needs water beside it: bush options. */
    public final boolean thorny, waterEdge;
    /** Vines: what shears cut off a grown vine (grape leaves), or null. */
    private final Supplier<? extends Item> prunings;
    /** Vines: how many a ripe vine gives per picking. */
    public final int minYield, maxYield;
    /** Flowers you can pick instead of waiting for fruit (elderflowers): id, name, tooltip lines. */
    public final String flowersId, flowersName, flowersDesc, flowersNext;
    public final Climate climate;
    public final Kind kind;
    public final Style style;
    /** Planted with its own harvest (rice), like potatoes: no separate seed item. */
    public final boolean selfPlanting;
    /** Texture prefix: block/<texture>_stage<n>. */
    public final String texture;
    /** How many different-looking growth stages the 8 ages are shown as. */
    public final int stages;
    /**
     * First stage that reaches above its block, with a second texture <texture>_stage<n>_top:
     * the upper half of a TALL crop, or the part of a PADDY crop above the water. -1 = never.
     */
    public final int topFrom;
    public final Wild wild;
    public final GrassDrop grassDrop;
    /** What the harvest is for, and what to do next (the item's "what is this / Next:" lines). */
    public final String produceDesc, produceNext;

    private final RegistryObject<Block> block;
    private final RegistryObject<Item> seeds;
    private final RegistryObject<Item> produce;
    private final RegistryObject<Item> flowers;
    private final RegistryObject<Block> wildBlock;
    private final RegistryObject<Item> wildItem;

    private Crop(Builder b) {
        name = b.name;
        blockId = b.blockId;
        seedId = b.seedId;
        produceId = b.produceId;
        displayName = b.displayName;
        blockName = b.blockName;
        seedName = b.seedName;
        produceName = b.produceName;
        food = b.food;
        thorny = b.thorny;
        waterEdge = b.waterEdge;
        flowersId = b.flowersId;
        flowersName = b.flowersName;
        flowersDesc = b.flowersDesc;
        flowersNext = b.flowersNext;
        climate = b.climate;
        kind = b.kind;
        style = b.style;
        selfPlanting = b.selfPlanting;
        texture = b.texture;
        stages = b.stages;
        topFrom = b.topFrom;
        wild = b.wild;
        grassDrop = b.grassDrop;
        produceDesc = b.produceDesc;
        produceNext = b.produceNext;
        prunings = b.prunings;
        minYield = b.minYield;
        maxYield = b.maxYield;

        // Registration order is the creative tab order: seeds, harvest, wild plant.
        block = ModBlocks.BLOCKS.register(blockId, () -> switch (style) {
            case ROW -> new ModCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT), this::seeds, climate);
            case TALL -> new TallCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT), this::seeds, climate);
            case PADDY -> new PaddyCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT), this::seeds, climate);
            case BUSH, HERB -> new BushCropBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), this::seeds, this::produce,
                    new BushCropBlock.Traits(thorny, waterEdge, flowersId == null ? null : this::flowers), climate);
            case VINE -> new TrellisVineBlock(ModBlocks.trellis().randomTicks(),
                    new TrellisVineBlock.Harvest(this::seeds, this::produce, minYield, maxYield, prunings), climate);
            case SUCCULENT -> new SucculentCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).sound(SoundType.GRASS), this::seeds, climate);
        });
        if (style == Style.VINE) {
            seeds = ModItems.ITEMS.register(seedId, () -> new TrellisPlantItem(block, new Item.Properties()));
            produce = ModItems.ITEMS.register(produceId, () -> new Item(produceProperties()));
        } else if (selfPlanting) {
            produce = ModItems.ITEMS.register(produceId, () -> new ItemNameBlockItem(block.get(), produceProperties()));
            seeds = produce;
        } else {
            seeds = ModItems.ITEMS.register(seedId, () -> new ItemNameBlockItem(block.get(), new Item.Properties()));
            produce = ModItems.ITEMS.register(produceId, () -> new Item(produceProperties()));
        }
        flowers = flowersId == null ? null : ModItems.ITEMS.register(flowersId, () -> new Item(new Item.Properties()));
        if (wild != null && !wildAsItself()) {
            wildBlock = ModBlocks.BLOCKS.register("wild_" + name, () -> style == Style.PADDY
                    ? new WildPaddyPlantBlock(wildPlantProperties()) : new WildPlantBlock(wildPlantProperties(), wild.onSand()));
            wildItem = ModItems.ITEMS.register("wild_" + name, () -> new BlockItem(wildBlock.get(), new Item.Properties()));
        } else {
            wildBlock = null;
            wildItem = null;
        }
    }

    private Item.Properties produceProperties() {
        return food == null ? new Item.Properties() : new Item.Properties().food(food);
    }

    public static BlockBehaviour.Properties wildPlantProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak()
                .sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY);
    }

    public Block block() {
        return block.get();
    }

    public Item seeds() {
        return seeds.get();
    }

    public Item produce() {
        return produce.get();
    }

    /** Picked or cut again and again (bushes, herbs). */
    public boolean isPerennial() {
        return style == Style.BUSH || style == Style.HERB;
    }

    /** Grows wild as itself rather than as a separate wild plant: bushes and herbs (ripe), succulents (in flower). */
    public boolean wildAsItself() {
        return isPerennial() || style == Style.SUCCULENT;
    }

    /** Has a separate wild plant block (wild_<name>). Perennials and succulents grow wild as themselves instead. */
    public boolean hasWild() {
        return wild != null && !wildAsItself();
    }

    /** Grows wild in the world, as a wild plant or (perennials) as itself. */
    public boolean growsWild() {
        return wild != null;
    }

    /** What world generation places: the wild plant, a ripe perennial, or a succulent in flower. */
    public BlockState wildState() {
        if (isPerennial()) return ((BushCropBlock) block()).ripe();
        if (style == Style.SUCCULENT) return ((SucculentCropBlock) block()).getStateForAge(SucculentCropBlock.MAX_AGE);
        return wildBlock().defaultBlockState();
    }

    public boolean hasFlowers() {
        return flowers != null;
    }

    public Item flowers() {
        return flowers.get();
    }

    public Block wildBlock() {
        return wildBlock.get();
    }

    public Item wildItem() {
        return wildItem.get();
    }

    /** Everything this crop adds to the creative tab, in order. */
    public List<Item> items() {
        List<Item> items = new ArrayList<>();
        if (!selfPlanting) items.add(seeds());
        items.add(produce());
        if (hasFlowers()) items.add(flowers());
        if (hasWild()) items.add(wildItem());
        return items;
    }

    /** Whether growth stage `stage` has a second texture above its block (see {@link #topFrom}). */
    public boolean hasTop(int stage) {
        return topFrom >= 0 && stage >= topFrom;
    }

    /** How to plant it, for tooltips and JEI. */
    public String plantingHint() {
        return switch (style) {
            case ROW -> "Plant on farmland";
            case TALL -> "Plant on farmland; it grows 2 blocks tall";
            case PADDY -> "Plant in water 1 block deep";
            case BUSH -> waterEdge ? "Plant beside water" : "Plant on grass or dirt";
            case HERB -> "Plant on grass, dirt or farmland";
            case VINE -> "Plant on the bottom Trellis of a stack standing on soil";
            case SUCCULENT -> "Plant on sand, red sand, coarse dirt or terracotta";
        };
    }

    /** Which biomes it grows wild in: seedtocellar:has_wild/<name> (pack makers can edit it). */
    public TagKey<Biome> wildBiomes() {
        return TagKey.create(Registries.BIOME, SeedToCellar.id("has_wild/" + name));
    }

    /** Which of the (few) textures to show for a growth age (0-7; perennials 0-3 and vines 0-5, one look each). */
    public int stageFor(int age) {
        return isPerennial() || style == Style.VINE ? age : age * stages / 8;
    }

    /** A field crop in the vanilla sense: grows on farmland (or in water) through ages, harvested whole. */
    public boolean isFieldCrop() {
        return !isPerennial() && style != Style.VINE;
    }

    /** The config toggle name for its wild plant, e.g. wildOats, wildSugarBeet. */
    public String worldgenToggle() {
        return "wild" + camel(name);
    }

    /** "sugar_beet" as "SugarBeet". */
    public static String camel(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        return out.toString();
    }

    static Builder row(String name, String displayName) {
        return new Builder(name, displayName);
    }

    static final class Builder {
        private final String name, displayName;
        private String seedName, blockId, seedId, texture, produceId, produceName, blockName;
        private boolean thorny, waterEdge;
        private FoodProperties food;
        private String flowersId, flowersName, flowersDesc, flowersNext;
        private int stages = 8;
        private int topFrom = -1;
        private Climate climate = Climate.TEMPERATE;
        private Kind kind = Kind.GRAIN;
        private Style style = Style.ROW;
        private boolean selfPlanting;
        private Wild wild;
        private GrassDrop grassDrop;
        private String produceDesc, produceNext;
        private Supplier<? extends Item> prunings;
        private int minYield = 1, maxYield = 3;

        private Builder(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
            this.seedName = displayName + " Seeds";
            this.blockId = name + "_crop";
            this.seedId = name + "_seeds";
            this.texture = name;
            this.produceId = name;
            this.produceName = displayName;
            this.blockName = displayName;
        }

        /** IDs that don't follow <name>_crop / <name>_seeds (e.g. oat_crop, oat_seeds for "oats"). */
        Builder ids(String blockId, String seedId, String seedName) {
            this.blockId = blockId;
            this.seedId = seedId;
            this.seedName = seedName;
            this.texture = blockId.substring(0, blockId.length() - "_crop".length());
            return this;
        }

        /** Two blocks tall (corn): the top half appears from age 4, shown by <texture>_stage<4-7>_top. */
        Builder tall() {
            this.style = Style.TALL;
            this.topFrom = TallCropBlock.UPPER_FROM;
            return this;
        }

        /** Grows in water one block deep (rice), shown as `stages` looks, reaching above the water from `topFrom`. */
        Builder paddy(int stages, int topFrom) {
            this.style = Style.PADDY;
            this.stages = stages;
            this.topFrom = topFrom;
            return this;
        }

        /** Planted with its own harvest, like potatoes (no separate seed item). Call after produce(). */
        Builder selfPlanting() {
            this.selfPlanting = true;
            this.seedId = produceId;
            this.seedName = produceName;
            return this;
        }

        /** A bush picked again and again (4 looks: planted, young, flowering, ripe), block <name>_bush. */
        Builder bush() {
            this.style = Style.BUSH;
            this.stages = 4;
            this.blockId = name + "_bush";
            this.blockName = displayName + " Bush";
            return this;
        }

        /** An herb cut back when harvested and regrowing (4 looks), planted with its own sprigs. */
        Builder herb() {
            this.style = Style.HERB;
            this.stages = 4;
            this.kind = Kind.HERB;
            this.selfPlanting = true;
            this.seedId = produceId;
            this.seedName = produceName;
            return this;
        }

        /**
         * Climbs a trellis (6 looks: 3 growing, leafy, flowering, ripe): block <name>_vine, planted from a
         * <name>_cutting; a ripe vine gives `min`-`max` of its harvest, and shears cut `prunings` off it.
         */
        Builder vine(int min, int max, Supplier<? extends Item> prunings) {
            this.style = Style.VINE;
            this.stages = 6;
            this.blockId = name + "_vine";
            this.blockName = displayName + " Vine";
            this.seedId = name + "_cutting";
            this.seedName = displayName + " Cutting";
            this.minYield = min;
            this.maxYield = max;
            this.prunings = prunings;
            return this;
        }

        /**
         * A desert succulent (5 looks: 4 growing, then in flower): block <name>, planted from a <name>_pup; cut down in
         * flower for its harvest and 1-2 pups.
         */
        Builder succulent() {
            this.style = Style.SUCCULENT;
            this.stages = 5;
            this.blockId = name;
            this.seedId = name + "_pup";
            this.seedName = displayName + " Pup";
            return this;
        }

        /** The harvest has its own ID and name ("blueberries", "Blueberries"). */
        Builder produce(String id, String name) {
            this.produceId = id;
            this.produceName = name;
            return this;
        }

        /** Can be eaten raw: hunger points and saturation (sweet berries are 2 and 0.1). */
        Builder food(int nutrition, float saturation) {
            this.food = new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
            return this;
        }

        Builder thorny() {
            this.thorny = true;
            return this;
        }

        /** Must have water right beside it (cranberry). */
        Builder waterEdge() {
            this.waterEdge = true;
            return this;
        }

        /** Flowering bushes can be picked for these instead (costs that cycle's fruit). */
        Builder flowers(String id, String name, String desc, String next) {
            this.flowersId = id;
            this.flowersName = name;
            this.flowersDesc = desc;
            this.flowersNext = next;
            return this;
        }

        Builder climate(Climate climate) {
            this.climate = climate;
            return this;
        }

        Builder kind(Kind kind) {
            this.kind = kind;
            return this;
        }

        Builder wild(int tries, int rarity, String where) {
            this.wild = new Wild(tries, rarity, 1, 2, 0.25F, where, false);
            return this;
        }

        /** Its wild plant also grows on sand (beach plants: sea beet, wild cabbage). Call after wild(). */
        Builder wildOnSand() {
            this.wild = new Wild(wild.tries(), wild.rarity(), wild.minSeeds(), wild.maxSeeds(), wild.bonusChance(), wild.where(), true);
            return this;
        }

        /** Shown as this many growth looks over its 8 ages (vegetables use 4, like vanilla's). */
        Builder stages(int stages) {
            this.stages = stages;
            return this;
        }

        Builder grassDrop(float chance, boolean coldOnly) {
            this.grassDrop = new GrassDrop(chance, coldOnly);
            return this;
        }

        Builder uses(String desc, String next) {
            this.produceDesc = desc;
            this.produceNext = next;
            return this;
        }

        Crop register() {
            return new Crop(this);
        }
    }
}
