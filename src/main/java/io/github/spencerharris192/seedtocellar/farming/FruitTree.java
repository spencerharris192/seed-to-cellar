package io.github.spencerharris192.seedtocellar.farming;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One fruit tree (GDD section 6.2, growth style H), described once: its fruit, the vanilla log its
 * trunk is made of, the look of its leaves, its climate and where it grows wild. Registering it adds
 * the sapling, the fruiting leaves and (unless it's a vanilla fruit, like the apple) the fruit;
 * data generation, world generation, tags and JEI read everything else from here. IDs are permanent.
 */
public final class FruitTree {
    /** How the leaves are colored: by the biome like oak leaves, birch's fixed green, or not at all (own texture). */
    public enum Tint { FOLIAGE, BIRCH, NONE }
    /** Birch leaves' fixed green (vanilla's), for leaves tinted like birch's in the world and in the hand. */
    public static final int BIRCH_LEAF_COLOR = 0xFF80A755;

    /** Which wild orchard it joins: temperate (forests, plains) or warm (savannas, jungles). */
    public enum Orchard { TEMPERATE, WARM }

    public final String name, displayName;
    /** The fruit's ID and name ("cherries", "Cherries"); for a vanilla fruit, just its name. */
    public final String fruitId, fruitName;
    public final Climate climate;
    public final Supplier<Block> log;
    /** The leaves' base texture ("minecraft:block/oak_leaves"), under the fruit layer. */
    public final String leafTexture;
    public final Tint tint;
    public final Orchard orchard;
    /** Where it grows wild, in words ("birch forests"). */
    public final String where;
    /** Fruit tooltip lines (null for vanilla fruit, which keep vanilla's). */
    public final String fruitDesc, fruitNext;

    private final DeferredBlock<Block> sapling, leaves;
    private final DeferredItem<Item> saplingItem, leavesItem;
    private final Supplier<? extends Item> fruit;

    FruitTree(String name, String displayName, String fruitId, String fruitName, @Nullable FoodProperties food,
              @Nullable Supplier<? extends Item> vanillaFruit, Climate climate, Supplier<Block> log, String leafTexture,
              Tint tint, Orchard orchard, String where, String fruitDesc, String fruitNext) {
        this.name = name;
        this.displayName = displayName;
        this.fruitId = fruitId;
        this.fruitName = fruitName;
        this.climate = climate;
        this.log = log;
        this.leafTexture = leafTexture;
        this.tint = tint;
        this.orchard = orchard;
        this.where = where;
        this.fruitDesc = fruitDesc;
        this.fruitNext = fruitNext;

        // Creative tab order: sapling, fruit, leaves. All compost like vanilla's saplings, leaves and apples.
        sapling = ModBlocks.BLOCKS.registerBlock(name + "_sapling", p -> new FruitSaplingBlock(name, climate, p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));
        saplingItem = ModItems.ITEMS.registerItem(name + "_sapling", p -> new BlockItem(sapling.get(), p),
                () -> new Item.Properties().compostable(ContextIntProviders.COMPOSTABLE_LOW).useBlockDescriptionPrefix());
        if (vanillaFruit != null) {
            fruit = vanillaFruit;
        } else {
            fruit = ModItems.ITEMS.registerItem(fruitId, Item::new, () -> new Item.Properties().food(food).compostable(ContextIntProviders.COMPOSTABLE_MEDIUM));
        }
        leaves = ModBlocks.BLOCKS.registerBlock(name + "_leaves", p -> new FruitLeavesBlock(p,
                fruit, climate), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
        leavesItem = ModItems.ITEMS.registerItem(name + "_leaves", p -> new BlockItem(leaves.get(), p),
                () -> new Item.Properties().compostable(ContextIntProviders.COMPOSTABLE_LOW).useBlockDescriptionPrefix());
    }

    public Block sapling() {
        return sapling.get();
    }

    public Item saplingItem() {
        return saplingItem.get();
    }

    public Block leaves() {
        return leaves.get();
    }

    public Item leavesItem() {
        return leavesItem.get();
    }

    public Item fruit() {
        return fruit.get();
    }

    /** Whether the fruit is one of ours (not a vanilla item like the apple). */
    public boolean ownFruit() {
        return fruit instanceof DeferredHolder<?, ?>;
    }

    /** The configured feature its saplings grow into, and wild trees are placed from. */
    public ResourceKey<Feature> treeFeature() {
        return FruitSaplingBlock.treeKey(name);
    }

    /** Biomes it grows wild in, as single trees: seedtocellar:has_wild/&lt;name&gt;_tree (pack makers can edit it). */
    public TagKey<Biome> wildBiomes() {
        return TagKey.create(Registries.BIOME, SeedToCellar.id("has_wild/" + name + "_tree"));
    }

    /** The config toggle for its wild trees, e.g. wildCherryTrees. */
    public String worldgenToggle() {
        return "wild" + Crop.camel(name) + "Trees";
    }

    /** Everything it adds to the creative tab, in order. */
    public List<Item> items() {
        List<Item> items = new ArrayList<>();
        items.add(saplingItem());
        if (ownFruit()) items.add(fruit());
        items.add(leavesItem());
        return items;
    }
}
