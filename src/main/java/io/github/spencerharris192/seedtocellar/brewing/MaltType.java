package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Kinds of malt in a mash. Items join a malt by tag, so other mods' malts can take part:
 * {@code seedtocellar:grist/<type>} counts fully; whole {@code seedtocellar:malt/<type>} counts half
 * (unmilled malt gives up less sugar, which is why the millstone is worth it).
 * <p>
 * Corn and potatoes are <i>adjuncts</i> (GDD section 9.4): cornmeal ({@code grist/corn}) and potatoes ({@code grist/potato})
 * mash alongside malt, whose enzymes turn their starch into sugar, so a mash needs at least some real malt. Half corn or
 * more makes a corn wash (bourbon); half potato, a potato wash (vodka).
 */
public enum MaltType {
    PALE("pale", false),
    AMBER("amber", false),
    BLACK("black", false),
    WHEAT("wheat", false),
    CORN("corn", true),
    POTATO("potato", true);

    public final String key;
    public final boolean adjunct;
    public final TagKey<Item> gristTag;
    public final TagKey<Item> maltTag;

    MaltType(String key, boolean adjunct) {
        this.key = key;
        this.adjunct = adjunct;
        this.gristTag = ItemTags.create(SeedToCellar.id("grist/" + key));
        this.maltTag = ItemTags.create(SeedToCellar.id("malt/" + key));
    }

    /** How much mash weight one of this item adds (1 for grist, 0.5 for whole malt), or 0 if not this malt. */
    public float weightOf(ItemStack stack) {
        if (stack.is(gristTag)) return 1F;
        if (stack.is(maltTag)) return 0.5F;
        return 0F;
    }

    public static MaltType of(ItemStack stack) {
        for (MaltType type : values()) if (type.weightOf(stack) > 0) return type;
        return null;
    }

    public static MaltType byKey(String key) {
        for (MaltType type : values()) if (type.key.equals(key)) return type;
        return null;
    }
}
