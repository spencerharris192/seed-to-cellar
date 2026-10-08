package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Pot Still (GDD section 9.4): what one run through the still turns the pot into. Each run halves the volume (the rest is
 * stillage) and counts one more run on the spirit. Recipes are checked from highest priority down; the first match wins.
 * <pre>{"type":"seedtocellar:distilling","input":{"fluid":"seedtocellar:plain_ale"},"result":"seedtocellar:malt_whiskey"}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:vodka_sources"},"filter":true,"result":"seedtocellar:vodka","priority":10}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:grain_spirits"},"min_runs":2,"result":"seedtocellar:vodka","priority":5}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:spirits"},"priority":-10}</pre>
 * No "result" means the same spirit, distilled once more. "filter": needs (and uses) one charcoal in the still's filter.
 * "min_runs": only for a pot that has already been through the still this often. "basket": botanicals in the Gin Basket,
 * {"required":"#seedtocellar:juniper","min":3}: the required one plus others, this many different in all.
 */
public class DistillingRecipe implements StationRecipe {
    /** Gin basket botanicals: one that must be there, and how many different ones in all. */
    public record Basket(Ingredient required, int min) {
        public static final Codec<Basket> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC.fieldOf("required").forGetter(Basket::required),
                Codec.intRange(1, 64).optionalFieldOf("min", 1).forGetter(Basket::min)
        ).apply(i, Basket::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Basket> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Basket::required,
                ByteBufCodecs.VAR_INT, Basket::min,
                Basket::new);

        public boolean test(List<ItemStack> stacks) {
            return stacks.stream().anyMatch(required) && distinct(stacks) >= min;
        }

        public static int distinct(List<ItemStack> stacks) {
            Set<Item> kinds = new HashSet<>();
            for (ItemStack stack : stacks) if (!stack.isEmpty()) kinds.add(stack.getItem());
            return kinds.size();
        }
    }

    public static final MapCodec<DistillingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CookingRecipe.Liquid.codec(1).codec().fieldOf("input").forGetter(r -> r.input),
            RecipeCodecs.FLUID.optionalFieldOf("result").forGetter(r -> Optional.ofNullable(r.result)),
            Codec.intRange(0, 64).optionalFieldOf("min_runs", 0).forGetter(r -> r.minRuns),
            Codec.BOOL.optionalFieldOf("filter", false).forGetter(r -> r.filter),
            Basket.CODEC.optionalFieldOf("basket").forGetter(r -> Optional.ofNullable(r.basket)),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(r -> r.priority)
    ).apply(i, (input, result, minRuns, filter, basket, priority) ->
            new DistillingRecipe(input, result.orElse(null), minRuns, filter, basket.orElse(null), priority)));
    public static final StreamCodec<RegistryFriendlyByteBuf, DistillingRecipe> STREAM_CODEC = StreamCodec.composite(
            CookingRecipe.Liquid.STREAM_CODEC, r -> r.input,
            ByteBufCodecs.optional(RecipeCodecs.FLUID_STREAM), r -> Optional.ofNullable(r.result),
            ByteBufCodecs.VAR_INT, r -> r.minRuns,
            ByteBufCodecs.BOOL, r -> r.filter,
            ByteBufCodecs.optional(Basket.STREAM_CODEC), r -> Optional.ofNullable(r.basket),
            ByteBufCodecs.VAR_INT, r -> r.priority,
            (input, result, minRuns, filter, basket, priority) ->
                    new DistillingRecipe(input, result.orElse(null), minRuns, filter, basket.orElse(null), priority));

    private final CookingRecipe.Liquid input;
    private final @Nullable Fluid result;
    private final int minRuns;
    private final boolean filter;
    private final @Nullable Basket basket;
    private final int priority;

    public DistillingRecipe(CookingRecipe.Liquid input, @Nullable Fluid result, int minRuns, boolean filter, @Nullable Basket basket,
                            int priority) {
        this.input = input;
        this.result = result;
        this.minRuns = minRuns;
        this.filter = filter;
        this.basket = basket;
        this.priority = priority;
    }

    /** True if a pot of {@code pot} can run with this recipe, given what's in the filter and the basket. */
    public boolean matches(FluidStack pot, boolean filterPresent, List<ItemStack> basketStacks) {
        if (!input.test(pot)) return false;
        if (runs(pot) < minRuns) return false;
        if (filter && !filterPresent) return false;
        return basket == null || basket.test(basketStacks);
    }

    /** Times this liquid has been through the still already (0 for a wash or a wine). */
    public static int runs(FluidStack stack) {
        return BrewData.get(stack).getIntOr(CraftStep.RUNS, 0);
    }

    /** What a pot of {@code pot} comes out as. */
    public Fluid resultFor(FluidStack pot) {
        return result != null ? result : pot.getFluid();
    }

    public CookingRecipe.Liquid input() { return input; }
    /** The spirit made, or null for "the same spirit again". */
    public @Nullable Fluid result() { return result; }
    public int minRuns() { return minRuns; }
    public boolean filter() { return filter; }
    public @Nullable Basket basket() { return basket; }
    public int priority() { return priority; }

    @Override public RecipeSerializer<DistillingRecipe> getSerializer() { return ModRecipes.DISTILLING_SERIALIZER.get(); }
    @Override public RecipeType<DistillingRecipe> getType() { return ModRecipes.DISTILLING.get(); }
}
