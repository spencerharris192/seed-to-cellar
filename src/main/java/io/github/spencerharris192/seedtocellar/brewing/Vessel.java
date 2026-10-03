package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * What a drink is served in (GDD section 16): the glass Mug (beers, cider, perry), the dark Wine Bottle
 * (wines, mead), the squat clear Spirit Bottle (spirits) or vanilla's Glass Bottle (juices). Each holds one
 * 250 mB serving and comes back empty when the drink is finished.
 */
public enum Vessel {
    MUG(() -> ModItems.MUG.get()),
    WINE_BOTTLE(() -> ModItems.WINE_BOTTLE.get()),
    SPIRIT_BOTTLE(() -> ModItems.SPIRIT_BOTTLE.get()),
    GLASS_BOTTLE(() -> Items.GLASS_BOTTLE);

    private final Supplier<Item> empty;

    Vessel(Supplier<Item> empty) {
        this.empty = empty;
    }

    /** The empty vessel. */
    public Item empty() {
        return empty.get();
    }

    /** Which vessel an empty stack is, if any. */
    public static Optional<Vessel> of(ItemStack stack) {
        for (Vessel vessel : values()) if (stack.is(vessel.empty())) return Optional.of(vessel);
        return Optional.empty();
    }
}
