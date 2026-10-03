package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Malting Tub: grain steeps in 1 bucket of water, the water drains, then the grain sprouts.
 * JSON: {"type":"seedtocellar:malting","ingredient":{...},"result":{...},"steep_time":2400,"sprout_time":3600}
 */
public class MaltingRecipe extends ProcessingRecipe {
    private final int sproutTime;

    public MaltingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int steepTime, int sproutTime) {
        super(id, ingredient, result, steepTime);
        this.sproutTime = sproutTime;
    }

    public int steepTime() {
        return time;
    }

    public int sproutTime() {
        return sproutTime;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MALTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MALTING.get();
    }

    public static class Serializer implements RecipeSerializer<MaltingRecipe> {
        @Override
        public MaltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new MaltingRecipe(id, Ingredient.fromJson(json.get("ingredient")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    GsonHelper.getAsInt(json, "steep_time", 2400), GsonHelper.getAsInt(json, "sprout_time", 3600));
        }

        @Override
        public MaltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MaltingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MaltingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.time);
            buf.writeVarInt(recipe.sproutTime);
        }
    }
}
