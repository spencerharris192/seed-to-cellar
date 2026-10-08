package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.Optional;

/**
 * Fruit Press: `count` of the ingredient, pressed with `cranks` turns of the screw, give a liquid and
 * (optionally) a byproduct left in the press (pomace, bagasse).
 * JSON: {"type":"seedtocellar:pressing","ingredient":...,"count":4,"result":{"fluid":"...","amount":500},
 * "byproduct":{"id":"..."},"cranks":4}
 */
public class PressingRecipe implements StationRecipe {
    public static final MapCodec<PressingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            Codec.intRange(1, 99).optionalFieldOf("count", 4).forGetter(r -> r.count),
            RecipeCodecs.FLUID_RESULT.fieldOf("result").forGetter(r -> r.result),
            ItemStackTemplate.CODEC.optionalFieldOf("byproduct").forGetter(r -> r.byproduct),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cranks", 4).forGetter(r -> r.cranks)
    ).apply(i, PressingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ByteBufCodecs.VAR_INT, r -> r.count,
            FluidStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> r.byproduct,
            ByteBufCodecs.VAR_INT, r -> r.cranks,
            PressingRecipe::new);

    private final Ingredient ingredient;
    private final int count;
    private final FluidStackTemplate result;
    private final Optional<ItemStackTemplate> byproduct;
    private final int cranks;

    public PressingRecipe(Ingredient ingredient, int count, FluidStackTemplate result, Optional<ItemStackTemplate> byproduct, int cranks) {
        this.ingredient = ingredient;
        this.count = count;
        this.result = result;
        this.byproduct = byproduct;
        this.cranks = cranks;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    /** How many of the ingredient one pressing takes. */
    public int count() {
        return count;
    }

    public FluidStack result() {
        return result.create();
    }

    /** Left behind in the press (may be empty). */
    public ItemStack byproduct() {
        return RecipeCodecs.create(byproduct);
    }

    public int cranks() {
        return cranks;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return byproduct();
    }

    @Override
    public RecipeSerializer<PressingRecipe> getSerializer() {
        return ModRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PressingRecipe> getType() {
        return ModRecipes.PRESSING.get();
    }
}
