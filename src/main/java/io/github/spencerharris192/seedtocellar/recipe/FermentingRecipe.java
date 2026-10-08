package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fermenting Vat: decides what a batch becomes (GDD section 9.1). Recipes are checked from
 * highest priority down; the first match wins, so a low-priority catch-all (Table Beer)
 * means any wort always makes something.
 *
 * <pre>{"type":"seedtocellar:fermenting","input":"seedtocellar:hopped_wort","result":"seedtocellar:amber_ale",
 *  "yeast":"ale","allow_wild":true,"temperature":"mild","min_strength":"normal","max_strength":"normal",
 *  "malt_min":{"amber":0.25},"malt_max":{"black":0.0},"time":24000,"priority":40}</pre>
 * "temperature" may also be a list of the temperatures that all count as right (distiller's washes: ["mild","warm"]).
 */
public class FermentingRecipe implements StationRecipe {
    private static final Codec<YeastType> YEAST = RecipeCodecs.named(YeastType::byKey, y -> y.key);
    private static final Codec<Temperature> TEMPERATURE = RecipeCodecs.named(Temperature::byName, Temperature::getSerializedName);
    private static final Codec<WortData.Strength> STRENGTH = RecipeCodecs.named(
            s -> WortData.Strength.valueOf(s.toUpperCase(Locale.ROOT)), s -> s.name().toLowerCase(Locale.ROOT));
    private static final Codec<Map<MaltType, Float>> MALTS = Codec.unboundedMap(RecipeCodecs.named(MaltType::byKey, m -> m.key), Codec.FLOAT)
            .xmap(m -> m.isEmpty() ? Map.of() : new EnumMap<>(m), m -> m);
    private static final StreamCodec<ByteBuf, Map<MaltType, Float>> MALTS_STREAM = ByteBufCodecs.map(
            n -> new EnumMap<>(MaltType.class), RecipeCodecs.ordinal(MaltType.class), ByteBufCodecs.FLOAT);

    public static final MapCodec<FermentingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeCodecs.FLUID.fieldOf("input").forGetter(r -> r.input),
            RecipeCodecs.FLUID.fieldOf("result").forGetter(r -> r.result),
            YEAST.optionalFieldOf("yeast", YeastType.ALE).forGetter(r -> r.yeast),
            Codec.BOOL.optionalFieldOf("allow_wild", true).forGetter(r -> r.allowWild),
            ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(TEMPERATURE)).optionalFieldOf("temperature", List.of(Temperature.MILD))
                    .forGetter(r -> r.temperatures),
            STRENGTH.optionalFieldOf("min_strength", WortData.Strength.LIGHT).forGetter(r -> r.minStrength),
            STRENGTH.optionalFieldOf("max_strength", WortData.Strength.STRONG).forGetter(r -> r.maxStrength),
            MALTS.optionalFieldOf("malt_min", Map.of()).forGetter(r -> r.maltMin),
            MALTS.optionalFieldOf("malt_max", Map.of()).forGetter(r -> r.maltMax),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 24000).forGetter(r -> r.time),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(r -> r.priority)
    ).apply(i, FermentingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC = StreamCodec.composite(
            RecipeCodecs.FLUID_STREAM, r -> r.input,
            RecipeCodecs.FLUID_STREAM, r -> r.result,
            RecipeCodecs.ordinal(YeastType.class), r -> r.yeast,
            ByteBufCodecs.BOOL, r -> r.allowWild,
            RecipeCodecs.ordinal(Temperature.class).apply(ByteBufCodecs.list()), r -> r.temperatures,
            RecipeCodecs.ordinal(WortData.Strength.class), r -> r.minStrength,
            RecipeCodecs.ordinal(WortData.Strength.class), r -> r.maxStrength,
            MALTS_STREAM, r -> r.maltMin,
            MALTS_STREAM, r -> r.maltMax,
            ByteBufCodecs.VAR_INT, r -> r.time,
            ByteBufCodecs.VAR_INT, r -> r.priority,
            FermentingRecipe::new);

    private final Fluid input;
    private final Fluid result;
    private final YeastType yeast;
    private final boolean allowWild;
    private final List<Temperature> temperatures;
    private final WortData.Strength minStrength;
    private final WortData.Strength maxStrength;
    private final Map<MaltType, Float> maltMin;
    private final Map<MaltType, Float> maltMax;
    private final int time;
    private final int priority;

    public FermentingRecipe(Fluid input, Fluid result, YeastType yeast, boolean allowWild, List<Temperature> temperatures,
                            WortData.Strength minStrength, WortData.Strength maxStrength, Map<MaltType, Float> maltMin,
                            Map<MaltType, Float> maltMax, int time, int priority) {
        this.input = input;
        this.result = result;
        this.yeast = yeast;
        this.allowWild = allowWild;
        this.temperatures = List.copyOf(temperatures);
        this.minStrength = minStrength;
        this.maxStrength = maxStrength;
        this.maltMin = maltMin;
        this.maltMax = maltMax;
        this.time = time;
        this.priority = priority;
    }

    public boolean matches(FluidStack fluid, YeastType used) {
        if (fluid.isEmpty() || fluid.getFluid() != input) return false;
        if (!used.works(yeast) && !(used == YeastType.WILD && allowWild)) return false;
        WortData wort = WortData.of(fluid);
        if (wort.strength().ordinal() < minStrength.ordinal() || wort.strength().ordinal() > maxStrength.ordinal()) return false;
        for (Map.Entry<MaltType, Float> e : maltMin.entrySet()) if (wort.share(e.getKey()) < e.getValue() - 0.001F) return false;
        for (Map.Entry<MaltType, Float> e : maltMax.entrySet()) if (wort.share(e.getKey()) > e.getValue() + 0.001F) return false;
        return true;
    }

    public Fluid input() { return input; }
    public Fluid result() { return result; }
    public YeastType yeast() { return yeast; }
    public boolean allowWild() { return allowWild; }
    /** The first right temperature (most recipes have just one). */
    public Temperature temperature() { return temperatures.get(0); }
    public List<Temperature> temperatures() { return temperatures; }

    /** Whether fermenting at {@code t} earns the temperature star. */
    public boolean suits(Temperature t) {
        return temperatures.contains(t);
    }

    /** "Mild", or "Mild or Warm". */
    public Component idealName() {
        MutableComponent name = Component.empty();
        for (int i = 0; i < temperatures.size(); i++) {
            if (i > 0) name.append(Component.translatable("temperature.seedtocellar.or"));
            name.append(temperatures.get(i).displayName());
        }
        return name;
    }
    public WortData.Strength minStrength() { return minStrength; }
    public WortData.Strength maxStrength() { return maxStrength; }
    public Map<MaltType, Float> maltMin() { return maltMin; }
    public Map<MaltType, Float> maltMax() { return maltMax; }
    /** Ticks with the right yeast (before the config multiplier); wild fermentation takes 1.5x. */
    public int time() { return time; }
    public int priority() { return priority; }

    @Override public RecipeSerializer<FermentingRecipe> getSerializer() { return ModRecipes.FERMENTING_SERIALIZER.get(); }
    @Override public RecipeType<FermentingRecipe> getType() { return ModRecipes.FERMENTING.get(); }
}
