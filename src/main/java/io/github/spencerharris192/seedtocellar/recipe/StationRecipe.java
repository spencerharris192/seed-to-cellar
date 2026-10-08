package io.github.spencerharris192.seedtocellar.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Vanilla's recipe plumbing for our station recipes. Stations match recipes with their own methods (a roast setting, a
 * tank, a yeast...), never through the recipe book, so these are all "special": no book, no notification, no assembly.
 * Recipes that take one item match it through {@link #matches(SingleRecipeInput, Level)}.
 */
public interface StationRecipe extends Recipe<SingleRecipeInput> {
    @Override
    default boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override
    default ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
