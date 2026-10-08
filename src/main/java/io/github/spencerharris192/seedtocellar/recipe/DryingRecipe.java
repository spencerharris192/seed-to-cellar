package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Drying Rack: air-dries one item hanging on the rack. {@code time} is ticks at the normal rate;
 * sun on the rack doubles the rate and rain on it pauses it.
 * JSON: {"type":"seedtocellar:drying","ingredient":...,"result":{"id":"..."},"time":2400}
 */
public class DryingRecipe extends ProcessingRecipe {
    public static final MapCodec<DryingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 2400).forGetter(r -> r.time)
    ).apply(i, DryingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.time,
            DryingRecipe::new);

    public DryingRecipe(Ingredient ingredient, ItemStackTemplate result, int time) {
        super(ingredient, result, time);
    }

    @Override
    public RecipeSerializer<DryingRecipe> getSerializer() {
        return ModRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<DryingRecipe> getType() {
        return ModRecipes.DRYING.get();
    }
}
