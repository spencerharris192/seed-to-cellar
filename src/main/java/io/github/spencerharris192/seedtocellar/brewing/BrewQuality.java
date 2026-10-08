package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The quality checks a drink earned (GDD section 11). Stars = 1 + checks passed, so 5 at most, except for one secret:
 * a perfect (5-star) malt whiskey steeped with a golden apple becomes a crowned Apple Crown Whiskey, the only 6-star drink.
 * Rides along on the fluid (and later the bottle/mug) so quality carries down the chain.
 */
public record BrewQuality(boolean yeast, boolean temperature, boolean craft, boolean aged, boolean crowned) {
    public static final String TAG = "Brew";
    public static final BrewQuality NONE = new BrewQuality(false, false, false, false);
    /** Every check passed: the most stars a drink can have (but for the crown). */
    public static final int PERFECT = 5;

    public BrewQuality(boolean yeast, boolean temperature, boolean craft, boolean aged) {
        this(yeast, temperature, craft, aged, false);
    }

    public int stars() {
        return 1 + (yeast ? 1 : 0) + (temperature ? 1 : 0) + (craft ? 1 : 0) + (aged ? 1 : 0) + (crowned ? 1 : 0);
    }

    public BrewQuality withCraft(boolean value) {
        return new BrewQuality(yeast, temperature, value, aged, crowned);
    }

    public BrewQuality withAged(boolean value) {
        return new BrewQuality(yeast, temperature, craft, value, crowned);
    }

    public BrewQuality withCrowned(boolean value) {
        return new BrewQuality(yeast, temperature, craft, aged, value);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Yeast", yeast);
        tag.putBoolean("Temperature", temperature);
        tag.putBoolean("Craft", craft);
        tag.putBoolean("Aged", aged);
        if (crowned) tag.putBoolean("Crowned", true);   // only when earned, so every other drink's data stays as it was
        return tag;
    }

    public static BrewQuality load(CompoundTag tag) {
        return new BrewQuality(tag.getBooleanOr("Yeast", false), tag.getBooleanOr("Temperature", false), tag.getBooleanOr("Craft", false),
                tag.getBooleanOr("Aged", false), tag.getBooleanOr("Crowned", false));
    }

    /** The quality on a liquid or anything holding one (none if it has none). */
    public static BrewQuality of(DataComponentGetter holder) {
        CompoundTag tag = BrewData.get(holder);
        return tag.contains(TAG) ? load(tag.getCompoundOrEmpty(TAG)) : NONE;
    }

    public static boolean has(DataComponentGetter holder) {
        return BrewData.has(holder, TAG);
    }

    public FluidStack applyTo(FluidStack stack) {
        BrewData.update(stack, tag -> tag.put(TAG, save()));
        return stack;
    }
}
