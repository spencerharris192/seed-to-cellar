package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
public class FermentingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
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

    public FermentingRecipe(ResourceLocation id, Fluid input, Fluid result, YeastType yeast, boolean allowWild, List<Temperature> temperatures,
                            WortData.Strength minStrength, WortData.Strength maxStrength, Map<MaltType, Float> maltMin,
                            Map<MaltType, Float> maltMax, int time, int priority) {
        this.id = id;
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

    // Vanilla recipe plumbing: fermenting doesn't use item containers.
    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.FERMENTING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.FERMENTING.get(); }

    public static class Serializer implements RecipeSerializer<FermentingRecipe> {
        @Override
        public FermentingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new FermentingRecipe(id,
                    fluid(GsonHelper.getAsString(json, "input")),
                    fluid(GsonHelper.getAsString(json, "result")),
                    YeastType.byKey(GsonHelper.getAsString(json, "yeast", "ale")),
                    GsonHelper.getAsBoolean(json, "allow_wild", true),
                    temperatures(json),
                    strength(GsonHelper.getAsString(json, "min_strength", "light")),
                    strength(GsonHelper.getAsString(json, "max_strength", "strong")),
                    malts(GsonHelper.getAsJsonObject(json, "malt_min", new JsonObject())),
                    malts(GsonHelper.getAsJsonObject(json, "malt_max", new JsonObject())),
                    GsonHelper.getAsInt(json, "time", 24000),
                    GsonHelper.getAsInt(json, "priority", 0));
        }

        private static List<Temperature> temperatures(JsonObject json) {
            if (json.has("temperature") && json.get("temperature").isJsonArray()) {
                List<Temperature> list = new ArrayList<>();
                for (var element : json.getAsJsonArray("temperature")) list.add(Temperature.byName(element.getAsString()));
                if (!list.isEmpty()) return list;
            }
            return List.of(Temperature.byName(GsonHelper.getAsString(json, "temperature", "mild")));
        }

        private static Fluid fluid(String id) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(id));
            if (fluid == null) throw new IllegalArgumentException("Unknown fluid " + id);
            return fluid;
        }

        private static WortData.Strength strength(String name) {
            return WortData.Strength.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        }

        private static Map<MaltType, Float> malts(JsonObject obj) {
            Map<MaltType, Float> map = new EnumMap<>(MaltType.class);
            for (String key : obj.keySet()) {
                MaltType type = MaltType.byKey(key);
                if (type != null) map.put(type, obj.get(key).getAsFloat());
            }
            return map;
        }

        @Override
        public FermentingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Fluid input = buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            Fluid result = buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            YeastType yeast = buf.readEnum(YeastType.class);
            boolean wild = buf.readBoolean();
            List<Temperature> temps = buf.readList(b -> b.readEnum(Temperature.class));
            WortData.Strength min = buf.readEnum(WortData.Strength.class);
            WortData.Strength max = buf.readEnum(WortData.Strength.class);
            Map<MaltType, Float> maltMin = readMalts(buf);
            Map<MaltType, Float> maltMax = readMalts(buf);
            return new FermentingRecipe(id, input, result, yeast, wild, temps, min, max, maltMin, maltMax, buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FermentingRecipe r) {
            buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, r.input);
            buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, r.result);
            buf.writeEnum(r.yeast);
            buf.writeBoolean(r.allowWild);
            buf.writeCollection(r.temperatures, FriendlyByteBuf::writeEnum);
            buf.writeEnum(r.minStrength);
            buf.writeEnum(r.maxStrength);
            writeMalts(buf, r.maltMin);
            writeMalts(buf, r.maltMax);
            buf.writeVarInt(r.time);
            buf.writeVarInt(r.priority);
        }

        private static Map<MaltType, Float> readMalts(FriendlyByteBuf buf) {
            Map<MaltType, Float> map = new EnumMap<>(MaltType.class);
            int n = buf.readVarInt();
            for (int i = 0; i < n; i++) map.put(buf.readEnum(MaltType.class), buf.readFloat());
            return map;
        }

        private static void writeMalts(FriendlyByteBuf buf, Map<MaltType, Float> map) {
            buf.writeVarInt(map.size());
            map.forEach((type, value) -> {
                buf.writeEnum(type);
                buf.writeFloat(value);
            });
        }
    }
}
