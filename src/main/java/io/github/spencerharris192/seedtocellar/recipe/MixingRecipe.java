package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

/**
 * Brew Kettle mixing: stir `per_bucket` of an ingredient into every bucket of a liquid and heat it, and the
 * whole kettle becomes another liquid (water + honey -> honey water, for mead). Containers the ingredient
 * leaves (a honey bottle's glass bottle) go back into the kettle's bottle slot.
 * JSON: {"type":"seedtocellar:mixing","liquid":{"fluid":"minecraft:water","amount":250},
 * "ingredient":"minecraft:honey_bottle","per_bucket":2,"result":"seedtocellar:honey_water","time":400}
 * With no "ingredient" the liquid is simply boiled down into the result (cane juice -> molasses), with nothing else in the slots.
 */
public class MixingRecipe implements StationRecipe {
    public static final MapCodec<MixingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CookingRecipe.Liquid.codec(250).codec().fieldOf("liquid").forGetter(r -> r.liquid),
            Ingredient.CODEC.optionalFieldOf("ingredient").forGetter(r -> r.ingredient),
            Codec.intRange(1, 64).optionalFieldOf("per_bucket", 1).forGetter(r -> Math.max(1, r.perBucket)),
            RecipeCodecs.FLUID.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 400).forGetter(r -> r.time)
    ).apply(i, MixingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC = StreamCodec.composite(
            CookingRecipe.Liquid.STREAM_CODEC, r -> r.liquid,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ByteBufCodecs.VAR_INT, r -> Math.max(1, r.perBucket),
            RecipeCodecs.FLUID_STREAM, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.time,
            MixingRecipe::new);

    private final CookingRecipe.Liquid liquid;
    private final Optional<Ingredient> ingredient;
    private final int perBucket;
    private final Fluid result;
    private final int time;

    public MixingRecipe(CookingRecipe.Liquid liquid, Optional<Ingredient> ingredient, int perBucket, Fluid result, int time) {
        this.liquid = liquid;
        this.ingredient = ingredient;
        this.perBucket = ingredient.isPresent() ? perBucket : 0;
        this.result = result;
        this.time = time;
    }

    /** What the kettle must hold (and at least how much of it). */
    public CookingRecipe.Liquid liquid() {
        return liquid;
    }

    /** What's stirred in (none when the liquid is just boiled down). */
    public Optional<Ingredient> ingredient() {
        return ingredient;
    }

    public int perBucket() {
        return perBucket;
    }

    /** Boiled down alone: nothing to stir in. */
    public boolean boilsDown() {
        return perBucket == 0;
    }

    /** How many of the ingredient `volume` mB takes (a started bucket counts as a whole one). */
    public int needed(int volume) {
        return (volume + 999) / 1000 * perBucket;
    }

    public Fluid result() {
        return result;
    }

    public int time() {
        return time;
    }

    public boolean matchesLiquid(FluidStack stack) {
        return liquid.test(stack);
    }

    @Override
    public RecipeSerializer<MixingRecipe> getSerializer() {
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<MixingRecipe> getType() {
        return ModRecipes.MIXING.get();
    }
}
