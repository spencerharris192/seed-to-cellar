package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.world.effect.MobEffect;

import java.util.List;
import java.util.function.Supplier;

/**
 * How a drink behaves (GDD sections 10, 13, 14).
 *
 * @param effect        its small positive effect
 * @param seconds       effect length at 3 stars (scaled x0.5 at 1 star up to x1.5 at 5 stars)
 * @param units         alcohol units added to the tipsiness meter (0 for juices)
 * @param nutrition     hunger restored
 * @param saturation    saturation modifier
 * @param agingYears    years in an ideal-wood cask for the aging star, or 0 if aging doesn't help it
 * @param graded        earns quality stars (fermented drinks); juices and kettle drinks like mulled wine don't
 * @param woods         the cask woods it ages best in (empty if it doesn't age)
 * @param craft         what earns its craft star: resting a day (beers, wines) or how it was distilled (spirits)
 * @param charring      what a charred cask means to it (GDD section 13): nothing, ideal whatever the wood (whiskeys), or
 *                      the only way its woods are ideal (bourbon: charred oak)
 */
public record DrinkProfile(Supplier<MobEffect> effect, int seconds, float units, int nutrition, float saturation, int agingYears,
                           boolean graded, List<CaskWood> woods, CraftStep craft, Charring charring) {
    public enum Charring { NONE, IDEAL, REQUIRED }

    public boolean ageable() {
        return agingYears > 0;
    }

    public boolean idealIn(CaskWood wood) {
        return idealIn(wood, false);
    }

    /** An ideal cask for it: one of its woods (charred, if it needs that), or any charred cask for a whiskey. */
    public boolean idealIn(CaskWood wood, boolean charred) {
        return switch (charring) {
            case NONE -> woods.contains(wood);
            case IDEAL -> charred && !wood.nether() || woods.contains(wood);
            case REQUIRED -> charred && woods.contains(wood);
        };
    }

    public int starYears(CaskWood wood) {
        return starYears(wood, false);
    }

    /** Years in this cask before the aging star: on time in an ideal one, twice as long otherwise; -1 if never. */
    public int starYears(CaskWood wood, boolean charred) {
        if (!ageable() || !wood.givesStar()) return -1;
        return idealIn(wood, charred) ? agingYears : agingYears * 2;
    }

    public boolean alcoholic() {
        return units > 0;
    }

    /** Effect duration in ticks for a given star count (ungraded drinks always count as 3 stars). */
    public int effectTicks(int stars) {
        int s = graded ? stars : 3;
        return Math.round(seconds * 20 * (0.25F * s + 0.25F));
    }
}
