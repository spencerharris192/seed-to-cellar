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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/**
 * Crushing Tub: each fruit, stomped `stomps` times (jump on it), gives its liquid.
 * JSON: {"type":"seedtocellar:crushing","ingredient":...,"result":{"fluid":"...","amount":125},"stomps":2}
 */
public class CrushingRecipe implements StationRecipe {
    public static final MapCodec<CrushingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            RecipeCodecs.FLUID_RESULT.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("stomps", 2).forGetter(r -> r.stomps)
    ).apply(i, CrushingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            FluidStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.stomps,
            CrushingRecipe::new);

    private final Ingredient ingredient;
    private final FluidStackTemplate result;
    private final int stomps;

    public CrushingRecipe(Ingredient ingredient, FluidStackTemplate result, int stomps) {
        this.ingredient = ingredient;
        this.result = result;
        this.stomps = stomps;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    /** The liquid from one fruit. */
    public FluidStack result() {
        return result.create();
    }

    public int stomps() {
        return stomps;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public RecipeSerializer<CrushingRecipe> getSerializer() {
        return ModRecipes.CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CrushingRecipe> getType() {
        return ModRecipes.CRUSHING.get();
    }
}
