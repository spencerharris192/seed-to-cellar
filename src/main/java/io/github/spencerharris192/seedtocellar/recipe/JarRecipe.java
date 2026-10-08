package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.station.StationItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Preserving Jar: some items plus (optionally) some liquid, left with the lid closed for a while, become new items, or
 * (with "result_fluid") steep the whole liquid into another: vodka with lemon peel and sugar into limoncello, keeping the
 * spirit's stars. Each ingredient entry is one item (list it twice for two). The liquid is a "fluid" or a "fluid_tag".
 * <pre>{"type":"seedtocellar:jar","ingredients":["#c:flours/wheat","#c:flours/wheat"],
 *  "fluid":"minecraft:water","fluid_amount":250,"result":{"id":"seedtocellar:sourdough_starter"},"time":24000}
 * {"type":"seedtocellar:jar","ingredients":[...],"fluid_tag":"seedtocellar:liqueur_bases","fluid_amount":250,
 *  "result_fluid":"seedtocellar:limoncello","time":24000}</pre>
 * "temperature": the temperatures it works at (any, if left out). "crown": true (Apple Crown Whiskey's golden apple) adds
 * the secret sixth star when the spirit going in is perfect (5 stars); JEI never shows such a recipe.
 */
public class JarRecipe implements StationRecipe {
    private static final Codec<Temperature> TEMPERATURE = RecipeCodecs.named(Temperature::byName, Temperature::getSerializedName);

    public static final MapCodec<JarRecipe> MAP_CODEC = RecordCodecBuilder.<JarRecipe>mapCodec(i -> i.group(
            Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(r -> r.ingredients),
            RecipeCodecs.FLUID.optionalFieldOf("fluid").forGetter(r -> r.liquid == null ? Optional.empty() : Optional.ofNullable(r.liquid.fluid())),
            TagKey.codec(Registries.FLUID).optionalFieldOf("fluid_tag").forGetter(r -> r.liquid == null ? Optional.empty() : Optional.ofNullable(r.liquid.tag())),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("fluid_amount", 0).forGetter(JarRecipe::fluidAmount),
            ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(r -> r.result),
            RecipeCodecs.FLUID.optionalFieldOf("result_fluid").forGetter(r -> Optional.ofNullable(r.resultFluid)),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 24000).forGetter(r -> r.time),
            TEMPERATURE.listOf().optionalFieldOf("temperature", List.of()).forGetter(r -> r.temperatures),
            Codec.BOOL.optionalFieldOf("crown", false).forGetter(r -> r.crowns)
    ).apply(i, (ingredients, fluid, fluidTag, amount, result, resultFluid, time, temperatures, crown) -> new JarRecipe(ingredients,
            fluid.isPresent() || fluidTag.isPresent() ? new CookingRecipe.Liquid(fluid.orElse(null), fluidTag.orElse(null), amount) : null,
            result, resultFluid.orElse(null), time, temperatures, crown)))
            .validate(r -> r.liquid != null && r.liquid.fluid() != null && r.liquid.tag() != null
                    ? DataResult.error(() -> "A jar recipe takes a \"fluid\" or a \"fluid_tag\", not both") : DataResult.success(r));
    public static final StreamCodec<RegistryFriendlyByteBuf, JarRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
            ByteBufCodecs.optional(CookingRecipe.Liquid.STREAM_CODEC), r -> Optional.ofNullable(r.liquid),
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> r.result,
            ByteBufCodecs.optional(RecipeCodecs.FLUID_STREAM), r -> Optional.ofNullable(r.resultFluid),
            ByteBufCodecs.VAR_INT, r -> r.time,
            RecipeCodecs.ordinal(Temperature.class).apply(ByteBufCodecs.list()), r -> r.temperatures,
            ByteBufCodecs.BOOL, r -> r.crowns,
            (ingredients, liquid, result, resultFluid, time, temperatures, crown) ->
                    new JarRecipe(ingredients, liquid.orElse(null), result, resultFluid.orElse(null), time, temperatures, crown));

    private final List<Ingredient> ingredients;
    private final CookingRecipe.@Nullable Liquid liquid;
    private final Optional<ItemStackTemplate> result;
    private final @Nullable Fluid resultFluid;
    private final int time;
    /** Where it has to stand (koji: Warm; lager yeast: Cold); empty = anywhere. */
    private final List<Temperature> temperatures;
    /** Crowns a perfect spirit (the secret sixth star). */
    private final boolean crowns;

    public JarRecipe(List<Ingredient> ingredients, CookingRecipe.@Nullable Liquid liquid, Optional<ItemStackTemplate> result,
                     @Nullable Fluid resultFluid, int time, List<Temperature> temperatures, boolean crowns) {
        this.ingredients = List.copyOf(ingredients);
        this.liquid = liquid;
        this.result = result;
        this.resultFluid = resultFluid;
        this.time = time;
        this.temperatures = List.copyOf(temperatures);
        this.crowns = crowns;
    }

    /** True if the jar holds exactly these ingredients (in any slots) and enough of the liquid. */
    public boolean matches(StationItems items, FluidStack tank) {
        if (liquid != null && !liquid.test(tank)) return false;
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            for (int n = 0; n < stack.getCount(); n++) contents.add(stack);
        }
        if (contents.size() != ingredients.size()) return false;
        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (ItemStack stack : contents) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).test(stack)) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public List<Ingredient> ingredients() { return ingredients; }
    /** The liquid it needs, or null. */
    public CookingRecipe.@Nullable Liquid liquid() { return liquid; }
    /** One fluid it takes (for display), or null. */
    public @Nullable Fluid fluid() {
        if (liquid == null) return null;
        return liquid.fluid() != null ? liquid.fluid() : liquid.examples().stream().findFirst().map(FluidStack::getFluid).orElse(null);
    }
    public int fluidAmount() { return liquid == null ? 0 : liquid.amount(); }
    /** The item made (empty when the recipe steeps the liquid instead). */
    public ItemStack result() { return RecipeCodecs.create(result); }
    /** What the whole liquid becomes, or null if the recipe makes an item. */
    public @Nullable Fluid resultFluid() { return resultFluid; }
    /** Ticks with the lid closed (before the fermentation multiplier). */
    public int time() { return time; }
    /** Crowns a perfect spirit with the secret sixth star (hidden from JEI). */
    public boolean crowns() { return crowns; }
    /** The temperatures it works at (empty: any). */
    public List<Temperature> temperatures() { return temperatures; }

    public boolean suits(Temperature at) {
        return temperatures.isEmpty() || temperatures.contains(at);
    }

    @Override public RecipeSerializer<JarRecipe> getSerializer() { return ModRecipes.JAR_SERIALIZER.get(); }
    @Override public RecipeType<JarRecipe> getType() { return ModRecipes.JAR.get(); }
}
