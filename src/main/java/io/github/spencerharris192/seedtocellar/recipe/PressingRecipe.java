package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Fruit Press: `count` of the ingredient, pressed with `cranks` turns of the screw, give a liquid and
 * (optionally) a byproduct left in the press (pomace, bagasse).
 * JSON: {"type":"seedtocellar:pressing","ingredient":{...},"count":4,"result":{"fluid":"...","amount":500},
 * "byproduct":{"item":"..."},"cranks":4}
 */
public class PressingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int count;
    private final FluidStack result;
    private final ItemStack byproduct;
    private final int cranks;

    public PressingRecipe(ResourceLocation id, Ingredient ingredient, int count, FluidStack result, ItemStack byproduct, int cranks) {
        this.id = id;
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
        return result.copy();
    }

    /** Left behind in the press (may be empty). */
    public ItemStack byproduct() {
        return byproduct.copy();
    }

    public int cranks() {
        return cranks;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return byproduct.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return byproduct;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PRESSING.get();
    }

    public static class Serializer implements RecipeSerializer<PressingRecipe> {
        @Override
        public PressingRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack byproduct = json.has("byproduct") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "byproduct")) : ItemStack.EMPTY;
            return new PressingRecipe(id, Ingredient.fromJson(json.get("ingredient")), Math.max(1, GsonHelper.getAsInt(json, "count", 4)),
                    FluidResult.fromJson(GsonHelper.getAsJsonObject(json, "result")), byproduct, Math.max(1, GsonHelper.getAsInt(json, "cranks", 4)));
        }

        @Override
        public PressingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new PressingRecipe(id, Ingredient.fromNetwork(buf), buf.readVarInt(), FluidResult.fromNetwork(buf), buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PressingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.count);
            FluidResult.toNetwork(buf, recipe.result);
            buf.writeItem(recipe.byproduct);
            buf.writeVarInt(recipe.cranks);
        }
    }
}
