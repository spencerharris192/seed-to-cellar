package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * What a wort was mashed from: each malt's share of the grist (rounded to 5% so batches made
 * the same way are identical and can be combined) and its strength. Stored on the fluid stack.
 */
public record WortData(Map<MaltType, Float> shares, Strength strength) {
    public static final String TAG = "Wort";

    public enum Strength {
        LIGHT, NORMAL, STRONG;

        /** Weight per bucket: under 1.5 light, under 3.5 normal, otherwise strong (2 grist/bucket = normal, 4 = strong). */
        public static Strength of(float weightPerBucket) {
            if (weightPerBucket < 1.5F) return LIGHT;
            if (weightPerBucket < 3.5F) return NORMAL;
            return STRONG;
        }

        public Component displayName() {
            return Component.translatable("strength.seedtocellar." + name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    /** Builds wort data from total mash weight per malt and the volume of water it went into. */
    public static WortData fromMash(Map<MaltType, Float> weights, int millibuckets) {
        float total = 0;
        for (float w : weights.values()) total += w;
        Map<MaltType, Float> shares = new EnumMap<>(MaltType.class);
        if (total > 0) {
            for (Map.Entry<MaltType, Float> e : weights.entrySet()) {
                float share = Math.round(e.getValue() / total * 20F) / 20F;
                if (share > 0) shares.put(e.getKey(), share);
            }
        }
        return new WortData(shares, Strength.of(total / Math.max(0.001F, millibuckets / 1000F)));
    }

    public float share(MaltType type) {
        return shares.getOrDefault(type, 0F);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag malts = new CompoundTag();
        shares.forEach((type, share) -> malts.putFloat(type.key, share));
        tag.put("Malts", malts);
        tag.putString("Strength", strength.name());
        return tag;
    }

    public static WortData load(CompoundTag tag) {
        Map<MaltType, Float> shares = new EnumMap<>(MaltType.class);
        CompoundTag malts = tag.getCompoundOrEmpty("Malts");
        for (String key : malts.keySet()) {
            MaltType type = MaltType.byKey(key);
            if (type != null) shares.put(type, malts.getFloatOr(key, 0F));
        }
        Strength strength;
        try {
            strength = Strength.valueOf(tag.getStringOr("Strength", ""));
        } catch (IllegalArgumentException e) {
            strength = Strength.NORMAL;
        }
        return new WortData(shares, strength);
    }

    public static WortData of(FluidStack stack) {
        CompoundTag tag = BrewData.get(stack);
        return tag.contains(TAG) ? load(tag.getCompoundOrEmpty(TAG)) : new WortData(Map.of(MaltType.PALE, 1F), Strength.NORMAL);
    }

    public FluidStack applyTo(FluidStack stack) {
        BrewData.update(stack, tag -> tag.put(TAG, save()));
        return stack;
    }
}
