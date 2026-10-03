package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * How a spirit's name and color follow its age (GDD sections 10.3, 20.4): New Make becomes Whiskey at 3 years, White Dog
 * becomes Bourbon after 2 years in charred oak (Corn Whiskey in anything else); the color deepens from almost clear toward
 * amber and mahogany. Read from the drink's label: "Age" (years), "Wood" and "Charred" (the cask that gave it the most years).
 *
 * @param names     checked in order; the first that fits names the drink
 * @param young     the color at 0 years (0xAARRGGBB)
 * @param old       the color once it's aged {@code fullYears} or more
 * @param fullYears when the color stops deepening
 */
public record AgedStyle(List<Name> names, int young, int old, int fullYears) {
    /**
     * A name from {@code years} on: {@code key} is a {@code drink.seedtocellar.<key>} name, or null for the drink's own name;
     * {@code wood} (null: any) and {@code charred} narrow it to drinks aged in that cask.
     */
    public record Name(int years, @Nullable String key, @Nullable CaskWood wood, boolean charred) {
        public static Name from(int years, @Nullable String key) {
            return new Name(years, key, null, false);
        }
    }

    public static int years(@Nullable CompoundTag data) {
        return data == null ? 0 : data.getInt(DrinkItem.AGE);
    }

    /** The translation key this drink goes by now, or null for its own name. */
    @Nullable
    public String nameKey(@Nullable CompoundTag data) {
        int years = years(data);
        String wood = data == null ? "" : data.getString(DrinkItem.WOOD);
        boolean charred = data != null && data.getBoolean(DrinkItem.CHARRED);
        for (Name name : names) {
            if (years < name.years()) continue;
            if (name.wood() != null && !name.wood().id().equals(wood)) continue;
            if (name.charred() && !charred) continue;
            return name.key() == null ? null : "drink." + SeedToCellar.MOD_ID + "." + name.key();
        }
        return null;
    }

    /** The color at this age: from young toward old, channel by channel. */
    public int tint(@Nullable CompoundTag data) {
        float t = fullYears <= 0 ? 1F : Math.min(1F, years(data) / (float) fullYears);
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = young >>> shift & 255, b = old >>> shift & 255;
            out |= Math.round(a + (b - a) * t) << shift;
        }
        return out;
    }
}
