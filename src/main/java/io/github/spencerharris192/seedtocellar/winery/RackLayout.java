package io.github.spencerharris192.seedtocellar.winery;

import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * The bottle racks (GDD section 16): how many places each has, in how many columns and rows (top row first, left to
 * right as you face it), and what it holds. They share their behaviour: click a place to put a bottle in or take it
 * out, hoppers fill and empty them, comparators read how full they are.
 */
public enum RackLayout {
    /** Six cubbies in two rows: bottles lie neck out, the foil in the wine's color. Compact cellar storage. */
    WINE_RACK(3, 2, ModTags.Items.WINE_RACK_BOTTLES),
    /** A half-deep wall shelf, two boards of three: drinks stand upright, just as they look in the hand. */
    BOTTLE_SHELF(3, 2, ModTags.Items.SHELF_DRINKS),
    /** An open display of three shelves: a bottle lies along each, its whole side and label showing. */
    WINE_DISPLAY(1, 3, ModTags.Items.WINE_RACK_BOTTLES),
    /** A wall board with four pegs: clean, empty mugs hang from them by their handles. */
    MUG_RACK(4, 1, ModTags.Items.MUG_RACK_ITEMS);

    private final int columns;
    private final int rows;
    private final TagKey<Item> accepts;

    RackLayout(int columns, int rows, TagKey<Item> accepts) {
        this.columns = columns;
        this.rows = rows;
        this.accepts = accepts;
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public int slots() {
        return columns * rows;
    }

    public TagKey<Item> accepts() {
        return accepts;
    }
}
