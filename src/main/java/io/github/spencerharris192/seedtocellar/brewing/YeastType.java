package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Yeast families (GDD section 8). WILD means no yeast was added: slower, and no yeast star.
 * Grain ferments (beers) want ALE yeast; fruit and honey ferments (wines, cider, perry, mead) want WINE yeast.
 * Every ferment leaves lees of its family's yeast, so a wild batch of juice is how you first get wine yeast.
 */
public enum YeastType {
    WILD("wild"),
    ALE("ale"),
    WINE("wine"),
    /** Lager yeast: ale yeast cultured in the cold. It brews lager, and ales too (but they earn the yeast star only with ale yeast). */
    LAGER("lager");

    public final String key;

    YeastType(String key) {
        this.key = key;
    }

    public static YeastType of(ItemStack stack) {
        if (stack.is(ModItems.ALE_YEAST.get())) return ALE;
        if (stack.is(ModItems.WINE_YEAST.get())) return WINE;
        if (stack.is(ModItems.LAGER_YEAST.get())) return LAGER;
        return WILD;
    }

    /** The item this family's lees become. */
    public Item leesItem() {
        return switch (this) {
            case WINE -> ModItems.WINE_YEAST.get();
            case LAGER -> ModItems.LAGER_YEAST.get();
            default -> ModItems.ALE_YEAST.get();
        };
    }

    /** Whether this yeast can ferment a recipe that wants `wanted` (lager yeast ferments ales as well). */
    public boolean works(YeastType wanted) {
        return this == wanted || this == LAGER && wanted == ALE;
    }

    public static YeastType byKey(String key) {
        for (YeastType t : values()) if (t.key.equals(key)) return t;
        return WILD;
    }
}
