package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * What earns a drink its craft star (GDD section 11). Beers and wines rest a day in a keg or cask; spirits are
 * distilled twice; vodka takes a third run or a charcoal filter; gin wants juniper and three more botanicals.
 */
public enum CraftStep {
    CONDITIONED("conditioned"),
    DOUBLE_DISTILLED("double_distilled"),
    NEUTRAL("neutral"),
    BOTANICALS("botanicals"),
    /** Liqueurs and bitters: steeped from a spirit, whose stars they keep (its craft star included). */
    INFUSED("infused");

    /** Times through the still (on spirits). */
    public static final String RUNS = "Runs";
    /** Run through a charcoal filter at least once (on spirits). */
    public static final String FILTERED = "Filtered";
    /** Different botanicals in the gin basket (on gin). */
    public static final String BOTANICAL_COUNT = "Botanicals";
    /** Gin's craft star: juniper and at least three others. */
    public static final int GIN_BOTANICALS = 4;

    private final String key;

    CraftStep(String key) {
        this.key = key;
    }

    /** Resting in a keg or cask earns the star (only for conditioned drinks). */
    public boolean byResting() {
        return this == CONDITIONED;
    }

    /** Whether a spirit carrying this data has earned the star. */
    public boolean earned(@Nullable CompoundTag data) {
        int runs = data == null ? 0 : data.getIntOr(RUNS, 0);
        return switch (this) {
            case CONDITIONED -> false;   // decided by resting, not by the data
            case DOUBLE_DISTILLED -> runs >= 2;
            case NEUTRAL -> runs >= 3 || data != null && data.getBooleanOr(FILTERED, false);
            case BOTANICALS -> data != null && data.getIntOr(BOTANICAL_COUNT, 0) >= GIN_BOTANICALS;
            case INFUSED -> false;   // carried over from the spirit
        };
    }

    /** The checklist line's translation key. */
    public String checkKey() {
        return "tooltip.seedtocellar.check." + key;
    }
}
