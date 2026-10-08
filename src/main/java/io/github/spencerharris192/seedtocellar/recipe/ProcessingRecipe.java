package io.github.spencerharris192.seedtocellar.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Base for our simple station recipes: one ingredient in, one result out, per item,
 * plus a duration. Stations process a whole stack as a batch (16 barley -> 16 malt).
 */
public abstract class ProcessingRecipe implements StationRecipe {
    protected final Ingredient ingredient;
    protected final ItemStackTemplate result;
    protected final int time;

    protected ProcessingRecipe(Ingredient ingredient, ItemStackTemplate result, int time) {
        this.ingredient = ingredient;
        this.result = result;
        this.time = time;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    /** A new stack of the result (for one input item). */
    public ItemStack result() {
        return result.create();
    }

    public ItemStackTemplate resultTemplate() {
        return result;
    }

    /** Ticks for one batch (before config multipliers), or cranks for the millstone. */
    public int time() {
        return time;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }
}
