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
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Crushing Tub: each fruit, stomped `stomps` times (jump on it), gives its liquid.
 * JSON: {"type":"seedtocellar:crushing","ingredient":{...},"result":{"fluid":"...","amount":125},"stomps":2}
 */
public class CrushingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final FluidStack result;
    private final int stomps;

    public CrushingRecipe(ResourceLocation id, Ingredient ingredient, FluidStack result, int stomps) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.stomps = stomps;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    /** The liquid from one fruit. */
    public FluidStack result() {
        return result.copy();
    }

    public int stomps() {
        return stomps;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
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
        return ModRecipes.CRUSHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUSHING.get();
    }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {
        @Override
        public CrushingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new CrushingRecipe(id, Ingredient.fromJson(json.get("ingredient")), FluidResult.fromJson(GsonHelper.getAsJsonObject(json, "result")),
                    Math.max(1, GsonHelper.getAsInt(json, "stomps", 2)));
        }

        @Override
        public CrushingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new CrushingRecipe(id, Ingredient.fromNetwork(buf), FluidResult.fromNetwork(buf), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CrushingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            FluidResult.toNetwork(buf, recipe.result);
            buf.writeVarInt(recipe.stomps);
        }
    }
}
