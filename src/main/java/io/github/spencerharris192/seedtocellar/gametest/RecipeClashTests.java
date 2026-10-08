package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * None of our crafting or cooking recipes shares its input with another recipe that makes something else: the game would
 * pick one of the two (another mod's crate, a vanilla recipe new in this version...). Each recipe of ours is filled in as
 * a player would, with each item its tags accept. Other mods' recipes are checked when they're loaded (the Cooking Pot
 * scan comes back with the Farmer's Delight integration).
 */
public final class RecipeClashTests {
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void ourRecipesDontShareAnInputWithAnother(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = level.recipeAccess().recipeMap();
        Set<String> clashes = new TreeSet<>();
        int[] grids = {0};
        for (RecipeHolder<CraftingRecipe> ours : Recipes.holders(level, RecipeType.CRAFTING).toList()) {
            if (!ours(ours)) continue;
            for (CraftingInput grid : grids(ours.value())) {
                if (!ours.value().matches(grid, level)) continue;
                grids[0]++;
                ItemStack made = ours.value().assemble(grid);
                recipes.getRecipesFor(RecipeType.CRAFTING, grid, level).forEach(other -> {
                    if (other != ours && !sameStack(made, other.value().assemble(grid))) clashes.add(ours.id().identifier() + " / " + other.id().identifier());
                });
            }
        }
        for (RecipeType<? extends AbstractCookingRecipe> type : List.of(RecipeType.SMELTING, RecipeType.SMOKING, RecipeType.BLASTING,
                RecipeType.CAMPFIRE_COOKING)) {
            List<? extends RecipeHolder<? extends AbstractCookingRecipe>> all = Recipes.holders(level, type).toList();
            for (var ours : all) {
                if (!ours(ours)) continue;
                for (Holder<Item> item : ours.value().input().items().toList()) {
                    SingleRecipeInput oven = new SingleRecipeInput(new ItemStack(item));
                    ItemStack made = ours.value().assemble(oven);
                    for (var other : all) {
                        if (other != ours && other.value().matches(oven, level) && !sameStack(made, other.value().assemble(oven))) {
                            clashes.add(ours.id().identifier() + " / " + other.id().identifier());
                        }
                    }
                }
            }
        }
        helper.assertTrue(grids[0] > 100, "our crafting recipes were filled in and checked (" + grids[0] + " grids)");
        clashes.forEach(clash -> SeedToCellar.LOGGER.warn("[recipe clash] {}", clash));
        helper.assertTrue(clashes.isEmpty(), clashes.size() + " recipes share their input with another (see the log): "
                + clashes.stream().limit(3).toList());
        helper.succeed();
    }

    private static boolean ours(RecipeHolder<?> recipe) {
        return recipe.id().identifier().getNamespace().equals(SeedToCellar.MOD_ID);
    }

    /** A crafting grid filled with a recipe's ingredients, once for each item they accept (up to 8). */
    private static List<CraftingInput> grids(CraftingRecipe recipe) {
        List<Optional<Ingredient>> slots;
        int width, height;
        if (recipe instanceof ShapedRecipe shaped) {
            slots = shaped.getIngredients();
            width = shaped.getWidth();
            height = shaped.getHeight();
        } else {
            slots = recipe.placementInfo().ingredients().stream().map(Optional::of).toList();
            width = 3;
            height = 3;
        }
        List<List<Holder<Item>>> choices = slots.stream().map(slot -> slot.map(i -> i.items().toList()).orElse(List.of())).toList();
        int alternatives = choices.stream().mapToInt(List::size).max().orElse(0);
        List<CraftingInput> grids = new ArrayList<>();
        for (int k = 0; k < Math.min(alternatives, 8); k++) {
            List<ItemStack> items = new ArrayList<>();
            for (List<Holder<Item>> choice : choices) items.add(choice.isEmpty() ? ItemStack.EMPTY : new ItemStack(choice.get(k % choice.size())));
            while (items.size() < width * height) items.add(ItemStack.EMPTY);
            grids.add(CraftingInput.of(width, height, items));
        }
        return grids;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b) && a.getCount() == b.getCount();
    }

    private RecipeClashTests() {}
}
