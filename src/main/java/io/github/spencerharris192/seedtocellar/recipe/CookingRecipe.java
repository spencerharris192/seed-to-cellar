package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Brew Kettle cooking (GDD sections 7 and 15): soups, stews, porridge, jams and the like. One
 * serving per batch, over heat: up to 4 ingredients (in any of the kettle's ingredient slots, any
 * order; nothing else may be in them), an optional liquid from the tank, and an optional
 * container (a bowl or bottle) from the container slot.
 * <pre>
 * {"type":"seedtocellar:cooking",
 *  "ingredients":[{...}, ...],
 *  "fluid":{"fluid":"minecraft:water" | "tag":"forge:milk", "amount":250},   (optional)
 *  "container":{"item":"minecraft:bowl"},                                  (optional)
 *  "result":{"item":"...", "count":1},
 *  "time":200}
 * </pre>
 * The kettle's container is its four ingredient slots.
 */
public class CookingRecipe implements Recipe<Container> {
    /** A liquid the recipe needs from the kettle's tank: one fluid, or any fluid in a tag. */
    public record Liquid(@Nullable Fluid fluid, @Nullable TagKey<Fluid> tag, int amount) {
        public boolean test(FluidStack stack) {
            if (stack.isEmpty() || stack.getAmount() < amount) return false;
            return fluid != null ? stack.getFluid().isSame(fluid) : stack.getFluid().is(tag);
        }

        /** Every fluid this accepts (for JEI). */
        public List<FluidStack> examples() {
            List<FluidStack> list = new ArrayList<>();
            if (fluid != null) {
                list.add(new FluidStack(fluid, amount));
            } else {
                for (Fluid f : ForgeRegistries.FLUIDS) {
                    if (f.is(tag) && f.isSource(f.defaultFluidState())) list.add(new FluidStack(f, amount));
                }
            }
            return list;
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (fluid != null) json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
            else json.addProperty("tag", tag.location().toString());
            json.addProperty("amount", amount);
            return json;
        }

        static Liquid fromJson(JsonObject json) {
            int amount = GsonHelper.getAsInt(json, "amount", 250);
            if (json.has("tag")) {
                return new Liquid(null, TagKey.create(Registries.FLUID, SeedToCellar.parse(GsonHelper.getAsString(json, "tag"))), amount);
            }
            ResourceLocation id = SeedToCellar.parse(GsonHelper.getAsString(json, "fluid"));
            Fluid f = ForgeRegistries.FLUIDS.getValue(id);
            if (f == null) throw new JsonParseException("Unknown fluid " + id);
            return new Liquid(f, null, amount);
        }

        void toNetwork(FriendlyByteBuf buf) {
            buf.writeBoolean(fluid != null);
            buf.writeResourceLocation(fluid != null ? ForgeRegistries.FLUIDS.getKey(fluid) : tag.location());
            buf.writeVarInt(amount);
        }

        static Liquid fromNetwork(FriendlyByteBuf buf) {
            boolean single = buf.readBoolean();
            ResourceLocation id = buf.readResourceLocation();
            int amount = buf.readVarInt();
            return single ? new Liquid(ForgeRegistries.FLUIDS.getValue(id), null, amount)
                    : new Liquid(null, TagKey.create(Registries.FLUID, id), amount);
        }
    }

    public static final int MAX_INGREDIENTS = 4;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    @Nullable private final Liquid liquid;
    private final Ingredient container;   // Ingredient.EMPTY = none
    private final ItemStack result;
    private final int time;

    public CookingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, @Nullable Liquid liquid, Ingredient container,
                         ItemStack result, int time) {
        this.id = id;
        this.ingredients = ingredients;
        this.liquid = liquid;
        this.container = container;
        this.result = result;
        this.time = time;
    }

    @Nullable
    public Liquid liquid() {
        return liquid;
    }

    public Ingredient container() {
        return container;
    }

    public boolean needsContainer() {
        return !container.isEmpty();
    }

    public ItemStack result() {
        return result;
    }

    /** Ticks per serving before the processing-time multiplier. */
    public int time() {
        return time;
    }

    /** True if the non-empty stacks are exactly this recipe's ingredients, one each, in any order. */
    public boolean matchesItems(List<ItemStack> stacks) {
        List<ItemStack> present = stacks.stream().filter(s -> !s.isEmpty()).toList();
        return present.size() == ingredients.size() && RecipeMatcher.findMatches(present, ingredients) != null;
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) stacks.add(container.getItem(i));
        return matchesItems(stacks);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isSpecial() {
        return true; // keeps them out of the vanilla recipe book
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COOKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COOKING.get();
    }

    public static class Serializer implements RecipeSerializer<CookingRecipe> {
        @Override
        public CookingRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            for (JsonElement element : array) ingredients.add(Ingredient.fromJson(element));
            if (ingredients.size() > MAX_INGREDIENTS) {
                throw new JsonParseException("A cooking recipe takes at most " + MAX_INGREDIENTS + " ingredients: " + id);
            }
            Liquid liquid = json.has("fluid") ? Liquid.fromJson(GsonHelper.getAsJsonObject(json, "fluid")) : null;
            if (ingredients.isEmpty() && liquid == null) {
                throw new JsonParseException("A cooking recipe needs ingredients, a liquid, or both: " + id);
            }
            Ingredient container = json.has("container") ? Ingredient.fromJson(json.get("container")) : Ingredient.EMPTY;
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new CookingRecipe(id, ingredients, liquid, container, result, Math.max(1, GsonHelper.getAsInt(json, "time", 200)));
        }

        @Override
        public CookingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < count; i++) ingredients.add(Ingredient.fromNetwork(buf));
            Liquid liquid = buf.readBoolean() ? Liquid.fromNetwork(buf) : null;
            Ingredient container = buf.readBoolean() ? Ingredient.fromNetwork(buf) : Ingredient.EMPTY;
            return new CookingRecipe(id, ingredients, liquid, container, buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CookingRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) ingredient.toNetwork(buf);
            buf.writeBoolean(recipe.liquid != null);
            if (recipe.liquid != null) recipe.liquid.toNetwork(buf);
            buf.writeBoolean(recipe.needsContainer());
            if (recipe.needsContainer()) recipe.container.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.time);
        }
    }

    /** For datagen: the JSON form of a liquid requirement. */
    public static JsonObject liquidJson(Liquid liquid) {
        return liquid.toJson();
    }
}
