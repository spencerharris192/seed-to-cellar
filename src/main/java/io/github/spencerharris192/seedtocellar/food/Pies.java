package io.github.spencerharris192.seedtocellar.food;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Fruit pies (GDD section 15.2, "Fruit Pie (family)"), each described once: the placeable pie
 * block, its item and its slice. Recipes, models, loot, names and textures are generated from
 * this list.
 */
public final class Pies {
    /** One pie: its id prefix and display name, blocks and items, and the fruit it's made with (from the item lookup datagen has). */
    public record Pie(String name, String displayName, DeferredBlock<Block> block, DeferredItem<Item> item,
                      DeferredItem<Item> slice, Function<HolderGetter<Item>, Ingredient> fruit) {
        public String id() {
            return name + "_pie";
        }
    }

    private static final List<Pie> ALL = new ArrayList<>();

    public static final Pie APPLE = add("apple", "Apple", items -> Ingredient.of(Items.APPLE));
    public static final Pie BLUEBERRY = add("blueberry", "Blueberry", items -> Ingredient.of(items.getOrThrow(ModTags.Items.crop(Crops.BLUEBERRY.name))));
    public static final Pie BLACKBERRY = add("blackberry", "Blackberry", items -> Ingredient.of(items.getOrThrow(ModTags.Items.crop(Crops.BLACKBERRY.name))));
    public static final Pie SWEET_BERRY = add("sweet_berry", "Sweet Berry", items -> Ingredient.of(Items.SWEET_BERRIES));
    public static final Pie CHERRY = add("cherry", "Cherry", items -> Ingredient.of(items.getOrThrow(ModTags.Items.fruit("cherry"))));
    public static final Pie PLUM = add("plum", "Plum", items -> Ingredient.of(items.getOrThrow(ModTags.Items.fruit("plum"))));
    public static final Pie PEACH = add("peach", "Peach", items -> Ingredient.of(items.getOrThrow(ModTags.Items.fruit("peach"))));
    public static final Pie PEAR = add("pear", "Pear", items -> Ingredient.of(items.getOrThrow(ModTags.Items.fruit("pear"))));

    public static List<Pie> all() {
        return Collections.unmodifiableList(ALL);
    }

    private static Pie add(String name, String displayName, Function<HolderGetter<Item>, Ingredient> fruit) {
        DeferredItem<Item> slice = ModItems.ITEMS.registerItem(name + "_pie_slice", Item::new, () -> ModFoods.properties(ModFoods.PIE_SLICE));
        DeferredBlock<Block> block = ModBlocks.BLOCKS.registerBlock(name + "_pie", p -> new PieBlock(p, slice), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE));
        DeferredItem<Item> item = ModItems.ITEMS.registerItem(name + "_pie", p -> new BlockItem(block.get(), p), () -> new Item.Properties().useBlockDescriptionPrefix());
        Pie pie = new Pie(name, displayName, block, item, slice, fruit);
        ALL.add(pie);
        return pie;
    }

    /** Loads the class, registering every pie (called from the mod constructor). */
    public static void init() {}

    private Pies() {}
}
