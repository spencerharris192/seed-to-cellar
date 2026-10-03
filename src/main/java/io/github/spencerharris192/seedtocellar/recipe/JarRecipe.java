package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Preserving Jar: some items plus (optionally) some liquid, left with the lid closed for a while, become new items, or
 * (with "result_fluid") steep the whole liquid into another: vodka with lemon peel and sugar into limoncello, keeping the
 * spirit's stars. Each ingredient entry is one item (list it twice for two). The liquid is a "fluid" or a "fluid_tag".
 * <pre>{"type":"seedtocellar:jar","ingredients":[{"tag":"forge:flour/wheat"},{"tag":"forge:flour/wheat"}],
 *  "fluid":"minecraft:water","fluid_amount":250,"result":{"item":"seedtocellar:sourdough_starter"},"time":24000}
 * {"type":"seedtocellar:jar","ingredients":[...],"fluid_tag":"seedtocellar:liqueur_bases","fluid_amount":250,
 *  "result_fluid":"seedtocellar:limoncello","time":24000}</pre>
 * "crown": true (Apple Crown Whiskey's golden apple) adds the secret sixth star when the spirit going in is perfect
 * (5 stars); JEI never shows such a recipe.
 */
public class JarRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    @Nullable private final CookingRecipe.Liquid liquid;
    private final ItemStack result;
    @Nullable private final Fluid resultFluid;
    private final int time;
    /** Where it has to stand (koji: Warm; lager yeast: Cold); empty = anywhere. */
    private final List<io.github.spencerharris192.seedtocellar.brewing.Temperature> temperatures;
    /** Crowns a perfect spirit (the secret sixth star). */
    private final boolean crowns;

    public JarRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, @Nullable CookingRecipe.Liquid liquid, ItemStack result,
                     @Nullable Fluid resultFluid, int time) {
        this(id, ingredients, liquid, result, resultFluid, time, List.of());
    }

    public JarRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, @Nullable CookingRecipe.Liquid liquid, ItemStack result,
                     @Nullable Fluid resultFluid, int time, List<io.github.spencerharris192.seedtocellar.brewing.Temperature> temperatures) {
        this(id, ingredients, liquid, result, resultFluid, time, temperatures, false);
    }

    public JarRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, @Nullable CookingRecipe.Liquid liquid, ItemStack result,
                     @Nullable Fluid resultFluid, int time, List<io.github.spencerharris192.seedtocellar.brewing.Temperature> temperatures,
                     boolean crowns) {
        this.crowns = crowns;
        this.temperatures = List.copyOf(temperatures);
        this.id = id;
        this.ingredients = ingredients;
        this.liquid = liquid;
        this.result = result;
        this.resultFluid = resultFluid;
        this.time = time;
    }

    /** True if the jar holds exactly these ingredients (in any slots) and enough of the liquid. */
    public boolean matches(IItemHandler items, FluidStack tank) {
        if (liquid != null && !liquid.test(tank)) return false;
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            for (int n = 0; n < stack.getCount(); n++) contents.add(stack);
        }
        if (contents.size() != ingredients.size()) return false;
        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (ItemStack stack : contents) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).test(stack)) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    /** The liquid it needs, or null. */
    @Nullable public CookingRecipe.Liquid liquid() { return liquid; }
    /** One fluid it takes (for display), or null. */
    @Nullable public Fluid fluid() {
        if (liquid == null) return null;
        return liquid.fluid() != null ? liquid.fluid() : liquid.examples().stream().findFirst().map(FluidStack::getFluid).orElse(null);
    }
    public int fluidAmount() { return liquid == null ? 0 : liquid.amount(); }
    /** The item made (empty when the recipe steeps the liquid instead). */
    public ItemStack result() { return result; }
    /** What the whole liquid becomes, or null if the recipe makes an item. */
    @Nullable public Fluid resultFluid() { return resultFluid; }
    /** Ticks with the lid closed (before the fermentation multiplier). */
    public int time() { return time; }
    /** Crowns a perfect spirit with the secret sixth star (hidden from JEI). */
    public boolean crowns() { return crowns; }
    /** The temperatures it works at (empty: any). */
    public List<io.github.spencerharris192.seedtocellar.brewing.Temperature> temperatures() { return temperatures; }

    public boolean suits(io.github.spencerharris192.seedtocellar.brewing.Temperature at) {
        return temperatures.isEmpty() || temperatures.contains(at);
    }

    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.JAR_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.JAR.get(); }

    public static class Serializer implements RecipeSerializer<JarRecipe> {
        @Override
        public JarRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            for (JsonElement element : array) ingredients.add(Ingredient.fromJson(element));
            int amount = GsonHelper.getAsInt(json, "fluid_amount", 0);
            CookingRecipe.Liquid liquid = null;
            if (json.has("fluid")) {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(GsonHelper.getAsString(json, "fluid")));
                if (fluid == null) throw new IllegalArgumentException("Unknown fluid in jar recipe " + id);
                liquid = new CookingRecipe.Liquid(fluid, null, amount);
            } else if (json.has("fluid_tag")) {
                liquid = new CookingRecipe.Liquid(null, TagKey.create(Registries.FLUID, ResourceLocation.tryParse(GsonHelper.getAsString(json, "fluid_tag"))), amount);
            }
            Fluid resultFluid = null;
            if (json.has("result_fluid")) {
                resultFluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(GsonHelper.getAsString(json, "result_fluid")));
                if (resultFluid == null) throw new IllegalArgumentException("Unknown result fluid in jar recipe " + id);
            }
            ItemStack result = json.has("result") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")) : ItemStack.EMPTY;
            List<io.github.spencerharris192.seedtocellar.brewing.Temperature> temps = new ArrayList<>();
            if (json.has("temperature")) {
                for (JsonElement t : GsonHelper.getAsJsonArray(json, "temperature")) {
                    temps.add(io.github.spencerharris192.seedtocellar.brewing.Temperature.byName(t.getAsString()));
                }
            }
            return new JarRecipe(id, ingredients, liquid, result, resultFluid, GsonHelper.getAsInt(json, "time", 24000), temps,
                    GsonHelper.getAsBoolean(json, "crown", false));
        }

        @Override
        public JarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < n; i++) ingredients.add(Ingredient.fromNetwork(buf));
            CookingRecipe.Liquid liquid = buf.readBoolean() ? CookingRecipe.Liquid.fromNetwork(buf) : null;
            ItemStack result = buf.readItem();
            Fluid resultFluid = buf.readBoolean() ? buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS) : null;
            int time = buf.readVarInt();
            return new JarRecipe(id, ingredients, liquid, result, resultFluid, time,
                    buf.readList(b -> b.readEnum(io.github.spencerharris192.seedtocellar.brewing.Temperature.class)), buf.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, JarRecipe r) {
            buf.writeVarInt(r.ingredients.size());
            r.ingredients.forEach(i -> i.toNetwork(buf));
            buf.writeBoolean(r.liquid != null);
            if (r.liquid != null) r.liquid.toNetwork(buf);
            buf.writeItem(r.result);
            buf.writeBoolean(r.resultFluid != null);
            if (r.resultFluid != null) buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, r.resultFluid);
            buf.writeVarInt(r.time);
            buf.writeCollection(r.temperatures, FriendlyByteBuf::writeEnum);
            buf.writeBoolean(r.crowns);
        }
    }
}
