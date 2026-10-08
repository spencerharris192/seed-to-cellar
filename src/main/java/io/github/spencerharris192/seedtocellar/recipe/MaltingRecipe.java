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
 * Malting Tub: grain steeps in 1 bucket of water, the water drains, then the grain sprouts.
 * JSON: {"type":"seedtocellar:malting","ingredient":"minecraft:wheat","result":{"id":"..."},"steep_time":2400,"sprout_time":3600}
 */
public class MaltingRecipe extends ProcessingRecipe {
    public static final MapCodec<MaltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("steep_time", 2400).forGetter(r -> r.time),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("sprout_time", 3600).forGetter(r -> r.sproutTime)
    ).apply(i, MaltingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MaltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.time,
            ByteBufCodecs.VAR_INT, r -> r.sproutTime,
            MaltingRecipe::new);

    private final int sproutTime;

    public MaltingRecipe(Ingredient ingredient, ItemStackTemplate result, int steepTime, int sproutTime) {
        super(ingredient, result, steepTime);
        this.sproutTime = sproutTime;
    }

    public int steepTime() {
        return time;
    }

    public int sproutTime() {
        return sproutTime;
    }

    @Override
    public RecipeSerializer<MaltingRecipe> getSerializer() {
        return ModRecipes.MALTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<MaltingRecipe> getType() {
        return ModRecipes.MALTING.get();
    }
}
