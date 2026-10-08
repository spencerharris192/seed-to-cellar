package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.CrushingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.DryingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MixingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Our station recipes (malting, kilning, milling...) for data generation: each is built as the recipe itself, so the file
 * written is exactly what the game reads back. (The Create, Mekanism and IE twins of milling, pressing, crushing and
 * mixing come back with the integrations: see docs/PORT_26.3.md.)
 */
public final class StationRecipes {
    private static final List<Temperature> MILD_OR_WARM = List.of(Temperature.MILD, Temperature.WARM);

    private static void save(RecipeOutput out, String path, Recipe<?> recipe) {
        out.accept(ResourceKey.create(Registries.RECIPE, SeedToCellar.id(path)), recipe, null);
    }

    private static ItemStackTemplate item(ItemLike item, int count) {
        return new ItemStackTemplate(item.asItem(), count);
    }

    /** A fluid by id ("seedtocellar:rum") or "#tag", as a liquid of `amount`. */
    private static CookingRecipe.Liquid liquid(String fluidOrTag, int amount) {
        if (fluidOrTag.startsWith("#")) {
            return new CookingRecipe.Liquid(null, TagKey.create(Registries.FLUID, Identifier.parse(fluidOrTag.substring(1))), amount);
        }
        return new CookingRecipe.Liquid(BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidOrTag)), null, amount);
    }

    public static void malting(RecipeOutput out, String name, Ingredient input, ItemLike result, int steepTicks, int sproutTicks) {
        save(out, "malting/" + name, new MaltingRecipe(input, item(result, 1), steepTicks, sproutTicks));
    }

    public static void kilning(RecipeOutput out, String name, Ingredient input, RoastLevel roast, ItemLike result, int ticks) {
        save(out, "kilning/" + name, new KilningRecipe(input, roast, item(result, 1), ticks));
    }

    public static void drying(RecipeOutput out, String name, Ingredient input, ItemLike result, int ticks) {
        save(out, "drying/" + name, new DryingRecipe(input, item(result, 1), ticks));
    }

    /** A wine, cider, perry or mead: any juice (or honey water) fermented with wine yeast (or wild). */
    public static void wine(RecipeOutput out, String name, Fluid input, Fluid result, Temperature temperature, int ticks) {
        save(out, "fermenting/" + name, new FermentingRecipe(input, result, YeastType.WINE, true, List.of(temperature),
                WortData.Strength.LIGHT, WortData.Strength.STRONG, Map.of(), Map.of(), ticks, 0));
    }

    /** Brew Kettle mixing: `perBucket` of the ingredient into every bucket of `liquid` (a fluid tag) makes `result`. */
    public static void mixing(RecipeOutput out, String name, TagKey<Fluid> liquid, Ingredient ingredient, int perBucket, Fluid result, int ticks) {
        save(out, "mixing/" + name, new MixingRecipe(new CookingRecipe.Liquid(null, liquid, 250), Optional.of(ingredient), perBucket, result, ticks));
    }

    /** Crushing Tub: one fruit, stomped `stomps` times, gives `amount` mB of `fluid`. */
    public static void crushing(RecipeOutput out, String name, Ingredient input, Fluid fluid, int amount, int stomps) {
        save(out, "crushing/" + name, new CrushingRecipe(input, new FluidStackTemplate(fluid, amount), stomps));
    }

    /** Fruit Press: `count` of the input, `cranks` turns, give `amount` mB of `fluid` and leave `byproduct` (or null). */
    public static void pressing(RecipeOutput out, String name, Ingredient input, int count, Fluid fluid, int amount,
                                @Nullable ItemLike byproduct, int cranks) {
        save(out, "pressing/" + name, new PressingRecipe(input, count, new FluidStackTemplate(fluid, amount),
                Optional.ofNullable(byproduct).map(b -> item(b, 1)), cranks));
    }

    public static void milling(RecipeOutput out, String name, Ingredient input, ItemLike result, int cranks) {
        milling(out, name, input, result, null, cranks);
    }

    /** A milling with a byproduct (rice: polished rice and bran). */
    public static void milling(RecipeOutput out, String name, Ingredient input, ItemLike result, @Nullable ItemLike byproduct, int cranks) {
        save(out, "milling/" + name, new MillingRecipe(input, item(result, 1), Optional.ofNullable(byproduct).map(b -> item(b, 1)), cranks));
    }

    /** A beer style (or other ferment). Malt shares are fractions, e.g. Map.of(MaltType.AMBER, 0.25F). */
    public static void fermenting(RecipeOutput out, String name, Fluid input, Fluid result, Temperature temperature,
                                  WortData.Strength min, WortData.Strength max, Map<MaltType, Float> maltMin,
                                  Map<MaltType, Float> maltMax, int ticks, int priority) {
        fermentingWith(out, name, input, result, YeastType.ALE, true, temperature, min, max, maltMin, maltMax, ticks, priority);
    }

    /**
     * A distiller's wash (GDD sections 9.4, 12): sweet wort with at least half `adjunct` (corn, potato), fermented with Ale
     * Yeast at Mild or Warm. Any strength; it outranks Plain Ale and Table Beer.
     */
    public static void wash(RecipeOutput out, String name, Fluid input, Fluid result, MaltType adjunct, int ticks) {
        save(out, "fermenting/" + name, new FermentingRecipe(input, result, YeastType.ALE, true, MILD_OR_WARM,
                WortData.Strength.LIGHT, WortData.Strength.STRONG, Map.of(adjunct, 0.5F), Map.of(), ticks, 30));
    }

    /** Brew Kettle: `liquid` boiled down alone into `result` (cane juice into molasses). */
    public static void boilDown(RecipeOutput out, String name, Fluid liquid, Fluid result, int ticks) {
        save(out, "mixing/" + name, new MixingRecipe(new CookingRecipe.Liquid(liquid, null, 250), Optional.empty(), 0, result, ticks));
    }

    /** A fruit wash for the still (pomace mash): fermented with wine yeast, at Mild or Warm like every distiller's wash. */
    public static void fruitWash(RecipeOutput out, String name, Fluid input, Fluid result, int ticks) {
        distillersWash(out, name, input, result, YeastType.WINE, ticks);
    }

    /** A distiller's wash from a liquid (molasses, agave juice): fermented with `yeast`, at Mild or Warm. */
    public static void distillersWash(RecipeOutput out, String name, Fluid input, Fluid result, YeastType yeast, int ticks) {
        save(out, "fermenting/" + name, new FermentingRecipe(input, result, yeast, true, MILD_OR_WARM,
                WortData.Strength.LIGHT, WortData.Strength.STRONG, Map.of(), Map.of(), ticks, 0));
    }

    /**
     * A Pot Still run (GDD section 9.4): a pot of `input` (a fluid id, or "#tag") becomes `result` (null: the same spirit, once
     * more). `minRuns`: only after that many runs; `filter`: needs a charcoal filter.
     */
    public static void distilling(RecipeOutput out, String name, String input, @Nullable Fluid result, int minRuns, boolean filter, int priority) {
        save(out, "distilling/" + name, new DistillingRecipe(liquid(input, 1), result, minRuns, filter, null, priority));
    }

    /** A Pot Still run through the Gin Basket: `required` in it, and `min` different botanicals in all. */
    public static void distillingWithBasket(RecipeOutput out, String name, String input, Fluid result, Ingredient required, int min, int priority) {
        save(out, "distilling/" + name, new DistillingRecipe(liquid(input, 1), result, 0, false, new DistillingRecipe.Basket(required, min), priority));
    }

    /**
     * A jar steeping (liqueurs): these items in a jar of `base` (a fluid id or "#tag", at least 250 mB), lid closed for
     * `ticks`, steep the whole jar into `result`, keeping the spirit's stars.
     */
    public static void steep(RecipeOutput out, String name, List<Ingredient> ingredients, String base, Fluid result, int ticks) {
        steep(out, name, ingredients, base, result, ticks, false);
    }

    /** A steeping that crowns a perfect spirit with the secret sixth star (hidden from JEI). */
    public static void steep(RecipeOutput out, String name, List<Ingredient> ingredients, String base, Fluid result, int ticks, boolean crown) {
        save(out, "jar/" + name, new JarRecipe(ingredients, liquid(base, 250), Optional.empty(), result, ticks, List.of(), crown));
    }

    /** A jar recipe that only works at certain temperatures (koji: Warm; lager yeast: Cold). */
    public static void jarAt(RecipeOutput out, String name, List<Ingredient> ingredients, @Nullable Fluid fluid, int amount,
                             ItemLike result, int count, int ticks, Temperature... temperatures) {
        save(out, "jar/" + name, new JarRecipe(ingredients, fluid == null ? null : new CookingRecipe.Liquid(fluid, null, amount),
                Optional.of(item(result, count)), null, ticks, List.of(temperatures), false));
    }

    /** A beer style fermented with a particular yeast family (lager: lager yeast only, `allowWild` false). */
    public static void fermentingWith(RecipeOutput out, String name, Fluid input, Fluid result, YeastType yeast, boolean allowWild,
                                      Temperature temperature, WortData.Strength min, WortData.Strength max, Map<MaltType, Float> maltMin,
                                      Map<MaltType, Float> maltMax, int ticks, int priority) {
        save(out, "fermenting/" + name, new FermentingRecipe(input, result, yeast, allowWild, List.of(temperature), min, max,
                maltMin, maltMax, ticks, priority));
    }

    /** A jar recipe: each listed ingredient is one item (list twice for two). */
    public static void jar(RecipeOutput out, String name, List<Ingredient> ingredients, Fluid fluid, int amount, ItemLike result, int count, int ticks) {
        jarAt(out, name, ingredients, fluid, amount, result, count, ticks);
    }

    /** A Brew Kettle dish: 1-4 ingredients, an optional liquid and container, `count` servings of the result. */
    public static void cooking(RecipeOutput out, String name, List<Ingredient> ingredients, CookingRecipe.@Nullable Liquid liquid,
                               @Nullable ItemLike container, ItemLike result, int count, int ticks) {
        save(out, "cooking/" + name, new CookingRecipe(ingredients, liquid, Optional.ofNullable(container).map(Ingredient::of),
                item(result, count), ticks));
    }

    private StationRecipes() {}
}
