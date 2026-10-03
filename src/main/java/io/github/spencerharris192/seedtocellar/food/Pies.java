package io.github.spencerharris192.seedtocellar.food;

import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Fruit pies (GDD section 15.2, "Fruit Pie (family)"), each described once: the placeable pie
 * block, its item and its slice. Recipes, models, loot, names and textures are generated from
 * this list.
 */
public final class Pies {
    /** One pie: its id prefix and display name, blocks and items, and the fruit it's made with. */
    public record Pie(String name, String displayName, RegistryObject<Block> block, RegistryObject<Item> item,
                      RegistryObject<Item> slice, Supplier<Ingredient> fruit) {
        public String id() {
            return name + "_pie";
        }
    }

    private static final List<Pie> ALL = new ArrayList<>();

    public static final Pie APPLE = add("apple", "Apple", () -> Ingredient.of(Items.APPLE));
    public static final Pie BLUEBERRY = add("blueberry", "Blueberry", () -> Ingredient.of(ModTags.Items.crop(Crops.BLUEBERRY.name)));
    public static final Pie BLACKBERRY = add("blackberry", "Blackberry", () -> Ingredient.of(ModTags.Items.crop(Crops.BLACKBERRY.name)));
    public static final Pie SWEET_BERRY = add("sweet_berry", "Sweet Berry", () -> Ingredient.of(Items.SWEET_BERRIES));
    public static final Pie CHERRY = add("cherry", "Cherry", () -> Ingredient.of(ModTags.Items.fruit("cherry")));
    public static final Pie PLUM = add("plum", "Plum", () -> Ingredient.of(ModTags.Items.fruit("plum")));
    public static final Pie PEACH = add("peach", "Peach", () -> Ingredient.of(ModTags.Items.fruit("peach")));
    public static final Pie PEAR = add("pear", "Pear", () -> Ingredient.of(ModTags.Items.fruit("pear")));

    public static List<Pie> all() {
        return Collections.unmodifiableList(ALL);
    }

    private static Pie add(String name, String displayName, Supplier<Ingredient> fruit) {
        RegistryObject<Item> slice = ModItems.ITEMS.register(name + "_pie_slice",
                () -> new Item(new Item.Properties().food(ModFoods.PIE_SLICE)));
        RegistryObject<Block> block = ModBlocks.BLOCKS.register(name + "_pie",
                () -> new PieBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), slice));
        RegistryObject<Item> item = ModItems.ITEMS.register(name + "_pie", () -> new BlockItem(block.get(), new Item.Properties()));
        Pie pie = new Pie(name, displayName, block, item, slice, fruit);
        ALL.add(pie);
        return pie;
    }

    /** Loads the class, registering every pie (called from the mod constructor). */
    public static void init() {}

    private Pies() {}
}
