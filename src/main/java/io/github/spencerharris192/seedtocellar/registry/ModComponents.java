package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.CaskContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components: data that rides on items and liquids. IDs are permanent once released. */
public final class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, SeedToCellar.MOD_ID);

    /** How a liquid was made (see {@link io.github.spencerharris192.seedtocellar.brewing.BrewData}), on the liquid and on
     * whatever holds it. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> BREW = COMPONENTS.registerComponentType("brew",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));
    /**
     * The quality checks a drink passed (BrewQuality), each its own marker, there or not: kept apart from {@link #BREW} so
     * a villager trade or an advancement can ask for "at least these checks" (they match only the components they list).
     * BrewData reads and writes them as part of the brew data, so nothing else needs to know.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> QUALITY_YEAST = check("quality_yeast");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> QUALITY_TEMPERATURE = check("quality_temperature");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> QUALITY_CRAFT = check("quality_craft");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> QUALITY_AGED = check("quality_aged");
    /** Apple Crown Whiskey's secret sixth star. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> QUALITY_CROWNED = check("quality_crowned");
    /** A cask or keg item broken while full: what's inside and how long it had rested. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CaskContents>> CASK_CONTENTS = COMPONENTS.registerComponentType(
            "cask_contents", builder -> builder.persistent(CaskContents.CODEC).networkSynchronized(CaskContents.STREAM_CODEC));

    private static DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> check(String name) {
        return COMPONENTS.registerComponentType(name, builder -> builder.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));
    }

    private ModComponents() {}
}
