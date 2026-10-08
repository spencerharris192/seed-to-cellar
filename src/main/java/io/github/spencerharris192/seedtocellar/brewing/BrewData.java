package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Everything a liquid carries about how it was made: its quality checks ({@link BrewQuality#TAG}), malt bill
 * ({@link WortData#TAG}), years and wood in the cask, runs through the still... One compound, kept on the liquid and on
 * whatever holds it (a bottle, a bucket, a cask's item), so nothing is lost on the way down the chain.
 * <p>
 * Stored as the {@code seedtocellar:brew} data component (the value is never changed in place), except the quality
 * checks passed: each is a marker component of its own (ModComponents.QUALITY_*), so trades and advancements can ask for
 * "at least these checks". Here they read and write as the booleans in the {@code Brew} compound; {@code Brew} itself
 * stays in the brew data (empty) to say the liquid has been judged at all.
 */
public final class BrewData {
    private static final Map<String, Supplier<DataComponentType<Unit>>> CHECKS = Map.of(
            "Yeast", ModComponents.QUALITY_YEAST, "Temperature", ModComponents.QUALITY_TEMPERATURE,
            "Craft", ModComponents.QUALITY_CRAFT, "Aged", ModComponents.QUALITY_AGED, "Crowned", ModComponents.QUALITY_CROWNED);

    /** A copy of the data, empty when there's none: change it freely, then {@link #set} it back. */
    public static CompoundTag get(DataComponentGetter holder) {
        CustomData data = holder.get(ModComponents.BREW.get());
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        CHECKS.forEach((key, check) -> {
            if (holder.get(check.get()) != null) {
                CompoundTag quality = tag.getCompoundOrEmpty(BrewQuality.TAG);
                quality.putBoolean(key, true);
                tag.put(BrewQuality.TAG, quality);
            }
        });
        return tag;
    }

    /** A copy of the data, or null when there's none. */
    public static @Nullable CompoundTag orNull(DataComponentGetter holder) {
        CompoundTag tag = get(holder);
        return tag.isEmpty() ? null : tag;
    }

    public static boolean has(DataComponentGetter holder, String key) {
        CustomData data = holder.get(ModComponents.BREW.get());
        if (data != null && data.contains(key)) return true;
        return key.equals(BrewQuality.TAG) && CHECKS.values().stream().anyMatch(check -> holder.get(check.get()) != null);
    }

    /** Replaces the data; null or empty removes it. */
    public static void set(MutableDataComponentHolder holder, @Nullable CompoundTag tag) {
        CompoundTag rest = tag == null ? new CompoundTag() : tag.copy();
        CompoundTag quality = rest.getCompoundOrEmpty(BrewQuality.TAG);
        CHECKS.forEach((key, check) -> {
            if (quality.getBooleanOr(key, false)) holder.set(check.get(), Unit.INSTANCE);
            else holder.remove(check.get());
        });
        if (rest.contains(BrewQuality.TAG)) rest.put(BrewQuality.TAG, new CompoundTag());
        if (rest.isEmpty()) holder.remove(ModComponents.BREW.get());
        else holder.set(ModComponents.BREW.get(), CustomData.of(rest));
    }

    /** The components that carry this data, for an item made from a template (a trade's drink: no stack exists yet). */
    public static DataComponentPatch patch(CompoundTag tag) {
        CompoundTag rest = tag.copy();
        CompoundTag quality = rest.getCompoundOrEmpty(BrewQuality.TAG);
        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        CHECKS.forEach((key, check) -> {
            if (quality.getBooleanOr(key, false)) patch.set(check.get(), Unit.INSTANCE);
        });
        if (rest.contains(BrewQuality.TAG)) rest.put(BrewQuality.TAG, new CompoundTag());
        if (!rest.isEmpty()) patch.set(ModComponents.BREW.get(), CustomData.of(rest));
        return patch.build();
    }

    public static void update(MutableDataComponentHolder holder, Consumer<CompoundTag> change) {
        CompoundTag tag = get(holder);
        change.accept(tag);
        set(holder, tag);
    }

    /** Gives {@code to} the same data as {@code from} (or none). */
    public static void copy(DataComponentGetter from, MutableDataComponentHolder to) {
        set(to, orNull(from));
    }

    private BrewData() {}
}
