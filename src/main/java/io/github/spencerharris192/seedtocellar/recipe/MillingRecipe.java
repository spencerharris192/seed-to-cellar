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

import java.util.Optional;

/**
 * Millstone: grinds one item per crank (by hand or a redstone pulse), sometimes leaving a byproduct (rice's bran).
 * JSON: {"type":"seedtocellar:milling","ingredient":...,"result":{"id":"..."},"byproduct":{"id":"..."},"cranks":1}
 */
public class MillingRecipe extends ProcessingRecipe {
    public static final MapCodec<MillingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            ItemStackTemplate.CODEC.optionalFieldOf("byproduct").forGetter(r -> r.byproduct),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cranks", 1).forGetter(r -> r.time)
    ).apply(i, MillingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> r.byproduct,
            ByteBufCodecs.VAR_INT, r -> r.time,
            MillingRecipe::new);

    private final Optional<ItemStackTemplate> byproduct;

    public MillingRecipe(Ingredient ingredient, ItemStackTemplate result, Optional<ItemStackTemplate> byproduct, int cranks) {
        super(ingredient, result, cranks);
        this.byproduct = byproduct;
    }

    /** What's left besides the result (empty for most). */
    public ItemStack byproduct() {
        return RecipeCodecs.create(byproduct);
    }

    public int cranks() {
        return time;
    }

    @Override
    public RecipeSerializer<MillingRecipe> getSerializer() {
        return ModRecipes.MILLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<MillingRecipe> getType() {
        return ModRecipes.MILLING.get();
    }
}
