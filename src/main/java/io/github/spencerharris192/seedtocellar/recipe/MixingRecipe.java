package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Brew Kettle mixing: stir `per_bucket` of an ingredient into every bucket of a liquid and heat it, and the
 * whole kettle becomes another liquid (water + honey -> honey water, for mead). Containers the ingredient
 * leaves (a honey bottle's glass bottle) go back into the kettle's bottle slot.
 * JSON: {"type":"seedtocellar:mixing","liquid":{"fluid":"minecraft:water","amount":250},
 * "ingredient":{"item":"minecraft:honey_bottle"},"per_bucket":2,"result":"seedtocellar:honey_water","time":400}
 * With no "ingredient" the liquid is simply boiled down into the result (cane juice -> molasses), with nothing else in the slots.
 */
public class MixingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final CookingRecipe.Liquid liquid;
    private final Ingredient ingredient;
    private final int perBucket;
    private final Fluid result;
    private final int time;

    public MixingRecipe(ResourceLocation id, CookingRecipe.Liquid liquid, Ingredient ingredient, int perBucket, Fluid result, int time) {
        this.id = id;
        this.liquid = liquid;
        this.ingredient = ingredient;
        this.perBucket = perBucket;
        this.result = result;
        this.time = time;
    }

    /** What the kettle must hold (and at least how much of it). */
    public CookingRecipe.Liquid liquid() {
        return liquid;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int perBucket() {
        return perBucket;
    }

    /** Boiled down alone: nothing to stir in. */
    public boolean boilsDown() {
        return perBucket == 0;
    }

    /** How many of the ingredient `volume` mB takes (a started bucket counts as a whole one). */
    public int needed(int volume) {
        return (volume + 999) / 1000 * perBucket;
    }

    public Fluid result() {
        return result;
    }

    public int time() {
        return time;
    }

    public boolean matchesLiquid(FluidStack stack) {
        return liquid.test(stack);
    }

    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.MIXING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.MIXING.get(); }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {
        @Override
        public MixingRecipe fromJson(ResourceLocation id, JsonObject json) {
            ResourceLocation resultId = SeedToCellar.parse(GsonHelper.getAsString(json, "result"));
            Fluid result = ForgeRegistries.FLUIDS.getValue(resultId);
            if (result == null) throw new IllegalArgumentException("Unknown fluid " + resultId);
            boolean stirred = json.has("ingredient");
            return new MixingRecipe(id, CookingRecipe.Liquid.fromJson(GsonHelper.getAsJsonObject(json, "liquid")),
                    stirred ? Ingredient.fromJson(json.get("ingredient")) : Ingredient.EMPTY,
                    stirred ? Math.max(1, GsonHelper.getAsInt(json, "per_bucket", 1)) : 0, result,
                    Math.max(1, GsonHelper.getAsInt(json, "time", 400)));
        }

        @Override
        public MixingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MixingRecipe(id, CookingRecipe.Liquid.fromNetwork(buf), Ingredient.fromNetwork(buf), buf.readVarInt(),
                    buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MixingRecipe r) {
            r.liquid.toNetwork(buf);
            r.ingredient.toNetwork(buf);
            buf.writeVarInt(r.perBucket);
            buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, r.result);
            buf.writeVarInt(r.time);
        }
    }
}
