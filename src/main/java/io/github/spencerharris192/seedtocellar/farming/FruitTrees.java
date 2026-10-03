package io.github.spencerharris192.seedtocellar.farming;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every fruit tree in the mod (GDD section 6.2). Order here is the creative tab order.
 * Registry IDs are permanent once released.
 */
public final class FruitTrees {
    private static final List<FruitTree> ALL = new ArrayList<>();
    private static final String MC = "minecraft:block/";

    public static final FruitTree APPLE = add(new FruitTree("apple", "Apple", "apple", "Apple", null, () -> Items.APPLE,
            Climate.TEMPERATE, () -> Blocks.OAK_LOG, MC + "oak_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.TEMPERATE,
            "forests and plains", null, null));
    public static final FruitTree CHERRY = add(new FruitTree("cherry", "Cherry", "cherries", "Cherries", food(2, 0.1F), null,
            Climate.TEMPERATE, () -> Blocks.CHERRY_LOG, MC + "oak_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.TEMPERATE,
            "cherry groves", "Sweet, dark red cherries", "Press, cook jam, bake a pie, or dry them"));
    public static final FruitTree PLUM = add(new FruitTree("plum", "Plum", "plum", "Plum", food(3, 0.3F), null,
            Climate.TEMPERATE, () -> Blocks.OAK_LOG, MC + "dark_oak_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.TEMPERATE,
            "forests and meadows", "A juicy purple plum", "Press it, cook jam, bake a pie, or dry it into prunes"));
    public static final FruitTree PEACH = add(new FruitTree("peach", "Peach", "peach", "Peach", food(4, 0.3F), null,
            Climate.HOT, () -> Blocks.ACACIA_LOG, MC + "acacia_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.WARM,
            "savannas", "A soft, blushing peach", "Press it, cook jam, bake a pie, or dry it"));
    public static final FruitTree PEAR = add(new FruitTree("pear", "Pear", "pear", "Pear", food(4, 0.3F), null,
            Climate.TEMPERATE, () -> Blocks.BIRCH_LOG, MC + "birch_leaves", FruitTree.Tint.BIRCH, FruitTree.Orchard.TEMPERATE,
            "birch forests", "A sweet, golden-green pear", "Press it for perry, bake a pie, or dry it"));
    public static final FruitTree LEMON = add(new FruitTree("lemon", "Lemon", "lemon", "Lemon", food(1, 0.1F), null,
            Climate.WARM, () -> Blocks.JUNGLE_LOG, MC + "jungle_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.WARM,
            "jungles", "A sharp, bright lemon", "Press it for lemonade, or flavor a cordial"));
    public static final FruitTree ORANGE = add(new FruitTree("orange", "Orange", "orange", "Orange", food(4, 0.3F), null,
            Climate.WARM, () -> Blocks.JUNGLE_LOG, MC + "jungle_leaves", FruitTree.Tint.FOLIAGE, FruitTree.Orchard.WARM,
            "jungles and savannas", "A sweet, juicy orange", "Eat it, press it for juice, or cook marmalade"));
    public static final FruitTree OLIVE = add(new FruitTree("olive", "Olive", "olives", "Olives", food(1, 0.1F), null,
            Climate.HOT, () -> Blocks.ACACIA_LOG, "seedtocellar:block/olive_leaves", FruitTree.Tint.NONE, FruitTree.Orchard.WARM,
            "savanna plateaus", "Small, bitter olives", "Press them for oil, or cure them in a jar of water"));

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
    }

    private static FruitTree add(FruitTree tree) {
        ALL.add(tree);
        return tree;
    }

    public static List<FruitTree> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** Called from the mod constructor so the trees register before the registry events. */
    public static void init() {
    }

    private FruitTrees() {}
}
