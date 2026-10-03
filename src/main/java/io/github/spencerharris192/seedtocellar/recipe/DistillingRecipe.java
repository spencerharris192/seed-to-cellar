package io.github.spencerharris192.seedtocellar.recipe;

import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pot Still (GDD section 9.4): what one run through the still turns the pot into. Each run halves the volume (the rest is
 * stillage) and counts one more run on the spirit. Recipes are checked from highest priority down; the first match wins.
 * <pre>{"type":"seedtocellar:distilling","input":{"fluid":"seedtocellar:plain_ale"},"result":"seedtocellar:malt_whiskey"}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:vodka_sources"},"filter":true,"result":"seedtocellar:vodka","priority":10}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:grain_spirits"},"min_runs":2,"result":"seedtocellar:vodka","priority":5}
 * {"type":"seedtocellar:distilling","input":{"tag":"seedtocellar:spirits"},"priority":-10}</pre>
 * No "result" means the same spirit, distilled once more. "filter": needs (and uses) one charcoal in the still's filter.
 * "min_runs": only for a pot that has already been through the still this often. "basket": botanicals in the Gin Basket,
 * {"required":{"tag":"seedtocellar:juniper"},"min":3}: the required one plus others, this many different in all.
 */
public class DistillingRecipe implements Recipe<Container> {
    /** Gin basket botanicals: one that must be there, and how many different ones in all. */
    public record Basket(Ingredient required, int min) {
        public boolean test(List<ItemStack> stacks) {
            return stacks.stream().anyMatch(required) && distinct(stacks) >= min;
        }

        public static int distinct(List<ItemStack> stacks) {
            Set<Item> kinds = new HashSet<>();
            for (ItemStack stack : stacks) if (!stack.isEmpty()) kinds.add(stack.getItem());
            return kinds.size();
        }
    }

    private final ResourceLocation id;
    private final CookingRecipe.Liquid input;
    @Nullable private final Fluid result;
    private final int minRuns;
    private final boolean filter;
    @Nullable private final Basket basket;
    private final int priority;

    public DistillingRecipe(ResourceLocation id, CookingRecipe.Liquid input, @Nullable Fluid result, int minRuns, boolean filter,
                            @Nullable Basket basket, int priority) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.minRuns = minRuns;
        this.filter = filter;
        this.basket = basket;
        this.priority = priority;
    }

    /** True if a pot of {@code pot} can run with this recipe, given what's in the filter and the basket. */
    public boolean matches(FluidStack pot, boolean filterPresent, List<ItemStack> basketStacks) {
        if (!input.test(pot)) return false;
        if (runs(pot) < minRuns) return false;
        if (filter && !filterPresent) return false;
        return basket == null || basket.test(basketStacks);
    }

    /** Times this liquid has been through the still already (0 for a wash or a wine). */
    public static int runs(FluidStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(CraftStep.RUNS);
    }

    /** What a pot of {@code pot} comes out as. */
    public Fluid resultFor(FluidStack pot) {
        return result != null ? result : pot.getFluid();
    }

    public CookingRecipe.Liquid input() { return input; }
    /** The spirit made, or null for "the same spirit again". */
    @Nullable public Fluid result() { return result; }
    public int minRuns() { return minRuns; }
    public boolean filter() { return filter; }
    @Nullable public Basket basket() { return basket; }
    public int priority() { return priority; }

    // Vanilla recipe plumbing: distilling doesn't use item containers.
    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.DISTILLING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.DISTILLING.get(); }

    public static class Serializer implements RecipeSerializer<DistillingRecipe> {
        @Override
        public DistillingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Fluid result = null;
            if (json.has("result")) {
                ResourceLocation key = ResourceLocation.tryParse(GsonHelper.getAsString(json, "result"));
                result = ForgeRegistries.FLUIDS.getValue(key);
                if (result == null) throw new IllegalArgumentException("Unknown fluid " + key + " in distilling recipe " + id);
            }
            Basket basket = null;
            if (json.has("basket")) {
                JsonObject b = GsonHelper.getAsJsonObject(json, "basket");
                basket = new Basket(Ingredient.fromJson(b.get("required")), GsonHelper.getAsInt(b, "min", 1));
            }
            JsonObject in = GsonHelper.getAsJsonObject(json, "input");
            if (!in.has("amount")) in.addProperty("amount", 1);
            return new DistillingRecipe(id, CookingRecipe.Liquid.fromJson(in), result, GsonHelper.getAsInt(json, "min_runs", 0),
                    GsonHelper.getAsBoolean(json, "filter", false), basket, GsonHelper.getAsInt(json, "priority", 0));
        }

        @Override
        public DistillingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CookingRecipe.Liquid input = CookingRecipe.Liquid.fromNetwork(buf);
            Fluid result = buf.readBoolean() ? buf.readRegistryIdUnsafe(ForgeRegistries.FLUIDS) : null;
            int minRuns = buf.readVarInt();
            boolean filter = buf.readBoolean();
            Basket basket = buf.readBoolean() ? new Basket(Ingredient.fromNetwork(buf), buf.readVarInt()) : null;
            return new DistillingRecipe(id, input, result, minRuns, filter, basket, buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DistillingRecipe r) {
            r.input.toNetwork(buf);
            buf.writeBoolean(r.result != null);
            if (r.result != null) buf.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, r.result);
            buf.writeVarInt(r.minRuns);
            buf.writeBoolean(r.filter);
            buf.writeBoolean(r.basket != null);
            if (r.basket != null) {
                r.basket.required().toNetwork(buf);
                buf.writeVarInt(r.basket.min());
            }
            buf.writeVarInt(r.priority);
        }
    }
}
