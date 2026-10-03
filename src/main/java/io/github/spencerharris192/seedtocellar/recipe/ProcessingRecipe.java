package io.github.spencerharris192.seedtocellar.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/**
 * Base for our simple station recipes: one ingredient in, one result out, per item,
 * plus a duration. Stations process a whole stack as a batch (16 barley -> 16 malt).
 * Slot 0 of the container is the input.
 */
public abstract class ProcessingRecipe implements Recipe<Container> {
    protected final ResourceLocation id;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final int time;

    protected ProcessingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int time) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.time = time;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result;
    }

    /** Ticks for one batch (before config multipliers), or cranks for the millstone. */
    public int time() {
        return time;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return ingredient.test(container.getItem(0));
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
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isSpecial() {
        return true; // keeps them out of the vanilla recipe book
    }
}
