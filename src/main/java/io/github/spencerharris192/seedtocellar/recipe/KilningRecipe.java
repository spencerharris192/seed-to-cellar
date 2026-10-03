package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Kiln: roasts or dries a batch at one roast setting. The same input can have a recipe per
 * setting (green malt: light -> pale, medium -> amber, dark -> black).
 * JSON: {"type":"seedtocellar:kilning","ingredient":{...},"roast":"medium","result":{...},"time":1200}
 */
public class KilningRecipe extends ProcessingRecipe {
    private final RoastLevel roast;

    public KilningRecipe(ResourceLocation id, Ingredient ingredient, RoastLevel roast, ItemStack result, int time) {
        super(id, ingredient, result, time);
        this.roast = roast;
    }

    public RoastLevel roast() {
        return roast;
    }

    /** The kiln calls this; the inherited matches(container, level) ignores the setting. */
    public boolean matches(Container container, RoastLevel setting) {
        return setting == roast && ingredient.test(container.getItem(0));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.KILNING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.KILNING.get();
    }

    public static class Serializer implements RecipeSerializer<KilningRecipe> {
        @Override
        public KilningRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new KilningRecipe(id, Ingredient.fromJson(json.get("ingredient")),
                    RoastLevel.byName(GsonHelper.getAsString(json, "roast", "light")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")),
                    GsonHelper.getAsInt(json, "time", 600));
        }

        @Override
        public KilningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new KilningRecipe(id, Ingredient.fromNetwork(buf), buf.readEnum(RoastLevel.class), buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, KilningRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeEnum(recipe.roast);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.time);
        }
    }
}
