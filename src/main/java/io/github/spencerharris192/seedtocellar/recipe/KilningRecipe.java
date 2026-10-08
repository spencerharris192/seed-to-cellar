package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Kiln: roasts or dries a batch at one roast setting. The same input can have a recipe per
 * setting (green malt: light -> pale, medium -> amber, dark -> black).
 * JSON: {"type":"seedtocellar:kilning","ingredient":...,"roast":"medium","result":{"id":"..."},"time":1200}
 */
public class KilningRecipe extends ProcessingRecipe {
    public static final Codec<RoastLevel> ROAST = RecipeCodecs.named(RoastLevel::byName, RoastLevel::getSerializedName);
    public static final MapCodec<KilningRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            ROAST.optionalFieldOf("roast", RoastLevel.LIGHT).forGetter(r -> r.roast),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 600).forGetter(r -> r.time)
    ).apply(i, KilningRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KilningRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
            RecipeCodecs.ordinal(RoastLevel.class).<RegistryFriendlyByteBuf>cast(), r -> r.roast,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.time,
            KilningRecipe::new);

    private final RoastLevel roast;

    public KilningRecipe(Ingredient ingredient, RoastLevel roast, ItemStackTemplate result, int time) {
        super(ingredient, result, time);
        this.roast = roast;
    }

    public RoastLevel roast() {
        return roast;
    }

    /** The kiln calls this: the input at the kiln's roast setting. */
    public boolean matches(ItemStack input, RoastLevel setting) {
        return setting == roast && ingredient.test(input);
    }

    @Override
    public RecipeSerializer<KilningRecipe> getSerializer() {
        return ModRecipes.KILNING_SERIALIZER.get();
    }

    @Override
    public RecipeType<KilningRecipe> getType() {
        return ModRecipes.KILNING.get();
    }
}
