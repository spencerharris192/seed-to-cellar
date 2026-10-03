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
 * Millstone: grinds one item per crank (by hand or a redstone pulse), sometimes leaving a byproduct (rice's bran).
 * JSON: {"type":"seedtocellar:milling","ingredient":{...},"result":{...},"byproduct":{...},"cranks":1}
 */
public class MillingRecipe extends ProcessingRecipe {
    private final ItemStack byproduct;

    public MillingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, ItemStack byproduct, int cranks) {
        super(id, ingredient, result, cranks);
        this.byproduct = byproduct;
    }

    /** What's left besides the result (empty for most). */
    public ItemStack byproduct() {
        return byproduct;
    }

    public int cranks() {
        return time;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MILLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MILLING.get();
    }

    public static class Serializer implements RecipeSerializer<MillingRecipe> {
        @Override
        public MillingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new MillingRecipe(id, Ingredient.fromJson(json.get("ingredient")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    json.has("byproduct") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "byproduct")) : ItemStack.EMPTY,
                    Math.max(1, GsonHelper.getAsInt(json, "cranks", 1)));
        }

        @Override
        public MillingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MillingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MillingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeItem(recipe.byproduct);
            buf.writeVarInt(recipe.time);
        }
    }
}
