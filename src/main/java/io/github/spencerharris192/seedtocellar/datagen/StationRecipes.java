package io.github.spencerharris192.seedtocellar.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.recipe.FluidResult;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import org.jetbrains.annotations.Nullable;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Writes our station recipe JSON (malting, kilning, milling) during data generation. */
public final class StationRecipes {
    public static void malting(Consumer<FinishedRecipe> out, String name, Ingredient input, ItemLike result, int steepTicks, int sproutTicks) {
        out.accept(new Json(SeedToCellar.id("malting/" + name), ModRecipes.MALTING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.add("result", item(result));
            json.addProperty("steep_time", steepTicks);
            json.addProperty("sprout_time", sproutTicks);
        }));
    }

    public static void kilning(Consumer<FinishedRecipe> out, String name, Ingredient input, RoastLevel roast, ItemLike result, int ticks) {
        out.accept(new Json(SeedToCellar.id("kilning/" + name), ModRecipes.KILNING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.addProperty("roast", roast.getSerializedName());
            json.add("result", item(result));
            json.addProperty("time", ticks);
        }));
    }

    public static void drying(Consumer<FinishedRecipe> out, String name, Ingredient input, ItemLike result, int ticks) {
        out.accept(new Json(SeedToCellar.id("drying/" + name), ModRecipes.DRYING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.add("result", item(result));
            json.addProperty("time", ticks);
        }));
    }

    /** A wine, cider, perry or mead: any juice (or honey water) fermented with wine yeast (or wild). */
    public static void wine(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, Temperature temperature, int ticks) {
        out.accept(new Json(SeedToCellar.id("fermenting/" + name), ModRecipes.FERMENTING_SERIALIZER.get(), json -> {
            json.addProperty("input", ForgeRegistries.FLUIDS.getKey(input).toString());
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("yeast", YeastType.WINE.key);
            json.addProperty("allow_wild", true);
            json.addProperty("temperature", temperature.getSerializedName());
            json.addProperty("time", ticks);
            json.addProperty("priority", 0);
        }));
    }

    /** Brew Kettle mixing: `perBucket` of the ingredient into every bucket of `liquid` (a fluid tag) makes `result`. */
    public static void mixing(Consumer<FinishedRecipe> out, String name, net.minecraft.tags.TagKey<Fluid> liquid, Ingredient ingredient,
                              int perBucket, Fluid result, int ticks) {
        CompatRecipes.mixing(out, name, CompatRecipes.fluidTag(liquid), ingredient, perBucket, result);
        out.accept(new Json(SeedToCellar.id("mixing/" + name), ModRecipes.MIXING_SERIALIZER.get(), json -> {
            JsonObject in = new JsonObject();
            in.addProperty("tag", liquid.location().toString());
            in.addProperty("amount", 250);
            json.add("liquid", in);
            json.add("ingredient", ingredient.toJson());
            json.addProperty("per_bucket", perBucket);
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("time", ticks);
        }));
    }

    /** Crushing Tub: one fruit, stomped `stomps` times, gives `amount` mB of `fluid`. */
    public static void crushing(Consumer<FinishedRecipe> out, String name, Ingredient input, Fluid fluid, int amount, int stomps) {
        CompatRecipes.pressing(out, "tub_" + name, input, 1, fluid, amount, null);
        out.accept(new Json(SeedToCellar.id("crushing/" + name), ModRecipes.CRUSHING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.add("result", FluidResult.toJson(fluid, amount));
            json.addProperty("stomps", stomps);
        }));
    }

    /** Fruit Press: `count` of the input, `cranks` turns, give `amount` mB of `fluid` and leave `byproduct` (or null). */
    public static void pressing(Consumer<FinishedRecipe> out, String name, Ingredient input, int count, Fluid fluid, int amount,
                                @Nullable ItemLike byproduct, int cranks) {
        CompatRecipes.pressing(out, name, input, count, fluid, amount, byproduct);
        out.accept(new Json(SeedToCellar.id("pressing/" + name), ModRecipes.PRESSING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.addProperty("count", count);
            json.add("result", FluidResult.toJson(fluid, amount));
            if (byproduct != null) json.add("byproduct", item(byproduct));
            json.addProperty("cranks", cranks);
        }));
    }

    public static void milling(Consumer<FinishedRecipe> out, String name, Ingredient input, ItemLike result, int cranks) {
        milling(out, name, input, result, null, cranks);
    }

    /** A milling with a byproduct (rice: polished rice and bran). */
    public static void milling(Consumer<FinishedRecipe> out, String name, Ingredient input, ItemLike result, @Nullable ItemLike byproduct, int cranks) {
        CompatRecipes.milling(out, name, input, result, byproduct);
        out.accept(new Json(SeedToCellar.id("milling/" + name), ModRecipes.MILLING_SERIALIZER.get(), json -> {
            json.add("ingredient", input.toJson());
            json.add("result", item(result));
            if (byproduct != null) json.add("byproduct", item(byproduct));
            json.addProperty("cranks", cranks);
        }));
    }

    /** A beer style (or other ferment). Malt shares are fractions, e.g. Map.of(MaltType.AMBER, 0.25F). */
    public static void fermenting(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, Temperature temperature,
                                  WortData.Strength min, WortData.Strength max, Map<MaltType, Float> maltMin,
                                  Map<MaltType, Float> maltMax, int ticks, int priority) {
        out.accept(new Json(SeedToCellar.id("fermenting/" + name), ModRecipes.FERMENTING_SERIALIZER.get(), json -> {
            json.addProperty("input", ForgeRegistries.FLUIDS.getKey(input).toString());
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("yeast", YeastType.ALE.key);
            json.addProperty("allow_wild", true);
            json.addProperty("temperature", temperature.getSerializedName());
            json.addProperty("min_strength", min.name().toLowerCase(java.util.Locale.ROOT));
            json.addProperty("max_strength", max.name().toLowerCase(java.util.Locale.ROOT));
            json.add("malt_min", malts(maltMin));
            json.add("malt_max", malts(maltMax));
            json.addProperty("time", ticks);
            json.addProperty("priority", priority);
        }));
    }

    /**
     * A distiller's wash (GDD sections 9.4, 12): sweet wort with at least half `adjunct` (corn, potato), fermented with Ale
     * Yeast at Mild or Warm. Any strength; it outranks Plain Ale and Table Beer.
     */
    public static void wash(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, MaltType adjunct, int ticks) {
        out.accept(new Json(SeedToCellar.id("fermenting/" + name), ModRecipes.FERMENTING_SERIALIZER.get(), json -> {
            json.addProperty("input", ForgeRegistries.FLUIDS.getKey(input).toString());
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("yeast", YeastType.ALE.key);
            json.addProperty("allow_wild", true);
            JsonArray temps = new JsonArray();
            temps.add(Temperature.MILD.getSerializedName());
            temps.add(Temperature.WARM.getSerializedName());
            json.add("temperature", temps);
            json.add("malt_min", malts(Map.of(adjunct, 0.5F)));
            json.addProperty("time", ticks);
            json.addProperty("priority", 30);
        }));
    }

    /** Brew Kettle: `liquid` boiled down alone into `result` (cane juice into molasses). */
    public static void boilDown(Consumer<FinishedRecipe> out, String name, Fluid liquid, Fluid result, int ticks) {
        CompatRecipes.mixing(out, name, CompatRecipes.fluidIn(liquid, null), null, 0, result);
        out.accept(new Json(SeedToCellar.id("mixing/" + name), ModRecipes.MIXING_SERIALIZER.get(), json -> {
            JsonObject in = new JsonObject();
            in.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(liquid).toString());
            in.addProperty("amount", 250);
            json.add("liquid", in);
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("time", ticks);
        }));
    }

    /** A fruit wash for the still (pomace mash): fermented with wine yeast, at Mild or Warm like every distiller's wash. */
    public static void fruitWash(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, int ticks) {
        distillersWash(out, name, input, result, YeastType.WINE, ticks);
    }

    /** A distiller's wash from a liquid (molasses, agave juice): fermented with `yeast`, at Mild or Warm. */
    public static void distillersWash(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, YeastType yeast, int ticks) {
        out.accept(new Json(SeedToCellar.id("fermenting/" + name), ModRecipes.FERMENTING_SERIALIZER.get(), json -> {
            json.addProperty("input", ForgeRegistries.FLUIDS.getKey(input).toString());
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("yeast", yeast.key);
            json.addProperty("allow_wild", true);
            JsonArray temps = new JsonArray();
            temps.add(Temperature.MILD.getSerializedName());
            temps.add(Temperature.WARM.getSerializedName());
            json.add("temperature", temps);
            json.addProperty("time", ticks);
            json.addProperty("priority", 0);
        }));
    }

    /**
     * A Pot Still run (GDD section 9.4): a pot of `input` (a fluid id, or "#tag") becomes `result` (null: the same spirit, once
     * more). `minRuns`: only after that many runs; `filter`: needs a charcoal filter.
     */
    public static void distilling(Consumer<FinishedRecipe> out, String name, String input, @Nullable Fluid result, int minRuns,
                                  boolean filter, int priority) {
        out.accept(new Json(SeedToCellar.id("distilling/" + name), ModRecipes.DISTILLING_SERIALIZER.get(), json -> {
            JsonObject in = new JsonObject();
            if (input.startsWith("#")) in.addProperty("tag", input.substring(1));
            else in.addProperty("fluid", input);
            json.add("input", in);
            if (result != null) json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            if (minRuns > 0) json.addProperty("min_runs", minRuns);
            if (filter) json.addProperty("filter", true);
            if (priority != 0) json.addProperty("priority", priority);
        }));
    }

    /** A Pot Still run through the Gin Basket: `required` in it, and `min` different botanicals in all. */
    public static void distillingWithBasket(Consumer<FinishedRecipe> out, String name, String input, Fluid result, Ingredient required,
                                            int min, int priority) {
        out.accept(new Json(SeedToCellar.id("distilling/" + name), ModRecipes.DISTILLING_SERIALIZER.get(), json -> {
            JsonObject in = new JsonObject();
            in.addProperty("fluid", input);
            json.add("input", in);
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            JsonObject basket = new JsonObject();
            basket.add("required", required.toJson());
            basket.addProperty("min", min);
            json.add("basket", basket);
            json.addProperty("priority", priority);
        }));
    }

    /**
     * A jar steeping (liqueurs): these items in a jar of `base` (a fluid id or "#tag", at least 250 mB), lid closed for
     * `ticks`, steep the whole jar into `result`, keeping the spirit's stars.
     */
    public static void steep(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, String base, Fluid result, int ticks) {
        steep(out, name, ingredients, base, result, ticks, false);
    }

    /** A steeping that crowns a perfect spirit with the secret sixth star (hidden from JEI). */
    public static void steep(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, String base, Fluid result, int ticks,
                             boolean crown) {
        out.accept(new Json(SeedToCellar.id("jar/" + name), ModRecipes.JAR_SERIALIZER.get(), json -> {
            if (crown) json.addProperty("crown", true);
            JsonArray array = new JsonArray();
            ingredients.forEach(i -> array.add(i.toJson()));
            json.add("ingredients", array);
            if (base.startsWith("#")) json.addProperty("fluid_tag", base.substring(1));
            else json.addProperty("fluid", base);
            json.addProperty("fluid_amount", 250);
            json.addProperty("result_fluid", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("time", ticks);
        }));
    }

    /** A jar recipe that only works at certain temperatures (koji: Warm; lager yeast: Cold). */
    public static void jarAt(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, @Nullable Fluid fluid, int amount,
                             ItemLike result, int count, int ticks, Temperature... temperatures) {
        out.accept(new Json(SeedToCellar.id("jar/" + name), ModRecipes.JAR_SERIALIZER.get(), json -> {
            JsonArray array = new JsonArray();
            ingredients.forEach(i -> array.add(i.toJson()));
            json.add("ingredients", array);
            if (fluid != null) {
                json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
                json.addProperty("fluid_amount", amount);
            }
            JsonObject res = item(result);
            if (count > 1) res.addProperty("count", count);
            json.add("result", res);
            json.addProperty("time", ticks);
            JsonArray temps = new JsonArray();
            for (Temperature t : temperatures) temps.add(t.getSerializedName());
            json.add("temperature", temps);
        }));
    }

    /** A beer style fermented with a particular yeast family (lager: lager yeast only, `allowWild` false). */
    public static void fermentingWith(Consumer<FinishedRecipe> out, String name, Fluid input, Fluid result, YeastType yeast, boolean allowWild,
                                      Temperature temperature, WortData.Strength min, WortData.Strength max, Map<MaltType, Float> maltMin,
                                      Map<MaltType, Float> maltMax, int ticks, int priority) {
        out.accept(new Json(SeedToCellar.id("fermenting/" + name), ModRecipes.FERMENTING_SERIALIZER.get(), json -> {
            json.addProperty("input", ForgeRegistries.FLUIDS.getKey(input).toString());
            json.addProperty("result", ForgeRegistries.FLUIDS.getKey(result).toString());
            json.addProperty("yeast", yeast.key);
            json.addProperty("allow_wild", allowWild);
            json.addProperty("temperature", temperature.getSerializedName());
            json.addProperty("min_strength", min.name().toLowerCase(java.util.Locale.ROOT));
            json.addProperty("max_strength", max.name().toLowerCase(java.util.Locale.ROOT));
            json.add("malt_min", malts(maltMin));
            json.add("malt_max", malts(maltMax));
            json.addProperty("time", ticks);
            json.addProperty("priority", priority);
        }));
    }

    /** A jar recipe: each listed ingredient is one item (list twice for two). */
    public static void jar(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, Fluid fluid, int amount,
                           ItemLike result, int count, int ticks) {
        out.accept(new Json(SeedToCellar.id("jar/" + name), ModRecipes.JAR_SERIALIZER.get(), json -> {
            JsonArray array = new JsonArray();
            ingredients.forEach(i -> array.add(i.toJson()));
            json.add("ingredients", array);
            if (fluid != null) {
                json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluid).toString());
                json.addProperty("fluid_amount", amount);
            }
            JsonObject res = item(result);
            if (count > 1) res.addProperty("count", count);
            json.add("result", res);
            json.addProperty("time", ticks);
        }));
    }

    /** A Brew Kettle dish: 1-4 ingredients, an optional liquid and container, `count` servings of the result. */
    public static void cooking(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, @Nullable CookingRecipe.Liquid liquid,
                               @Nullable ItemLike container, ItemLike result, int count, int ticks) {
        out.accept(new Json(SeedToCellar.id("cooking/" + name), ModRecipes.COOKING_SERIALIZER.get(), json -> {
            JsonArray array = new JsonArray();
            for (Ingredient ingredient : ingredients) array.add(ingredient.toJson());
            json.add("ingredients", array);
            if (liquid != null) json.add("fluid", CookingRecipe.liquidJson(liquid));
            if (container != null) json.add("container", Ingredient.of(container).toJson());
            JsonObject res = item(result);
            if (count > 1) res.addProperty("count", count);
            json.add("result", res);
            json.addProperty("time", ticks);
        }));
    }

    private static JsonObject malts(Map<MaltType, Float> map) {
        JsonObject obj = new JsonObject();
        map.forEach((type, share) -> obj.addProperty(type.key, share));
        return obj;
    }

    private static JsonObject item(ItemLike item) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", ForgeRegistries.ITEMS.getKey(item.asItem()).toString());
        return obj;
    }

    private record Json(ResourceLocation id, RecipeSerializer<?> serializer, Consumer<JsonObject> writer) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            writer.accept(json);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private StationRecipes() {}
}
