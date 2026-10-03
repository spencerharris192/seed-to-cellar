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
 * Drying Rack: air-dries one item hanging on the rack. {@code time} is ticks at the normal rate;
 * sun on the rack doubles the rate and rain on it pauses it.
 * JSON: {"type":"seedtocellar:drying","ingredient":{...},"result":{...},"time":2400}
 */
public class DryingRecipe extends ProcessingRecipe {
    public DryingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int time) {
        super(id, ingredient, result, time);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DRYING.get();
    }

    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        @Override
        public DryingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new DryingRecipe(id, Ingredient.fromJson(json.get("ingredient")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    Math.max(1, GsonHelper.getAsInt(json, "time", 2400)));
        }

        @Override
        public DryingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new DryingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DryingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.time);
        }
    }
}
