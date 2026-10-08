package io.github.spencerharris192.seedtocellar.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.level.material.Fluid;

/**
 * Recipes for other mods' stations (GDD sections 15.3 and 22), so their players can use the tools they
 * already have: Farmer's Delight, Create, Mekanism, Immersive Engineering and Botany Pots. Each is wrapped in a
 * forge:conditional recipe that only loads when that mod is installed; we write their JSON directly (formats read from
 * each mod's own recipes), so we don't need those mods to build.
 */
public final class CompatRecipes {
    public static final String FARMERS_DELIGHT = "farmersdelight";
    public static final String CREATE = "create";
    public static final String MEKANISM = "mekanism";
    public static final String IMMERSIVE = "immersiveengineering";
    public static final String BOTANY_POTS = "botanypots";

    public static final String PATCHOULI = "patchouli";

    /** The Brewer's Almanac: a book and barley seeds make Patchouli's guide book, opened to ours. */
    public static void almanac(Consumer<FinishedRecipe> out, String book) {
        conditional(out, PATCHOULI, new Foreign(SeedToCellar.id("patchouli/brewers_almanac"), "minecraft:crafting_shapeless", json -> {
            json.addProperty("category", "misc");
            JsonArray ingredients = new JsonArray();
            ingredients.add(Ingredient.of(net.minecraft.world.item.Items.BOOK).toJson());
            JsonObject seeds = new JsonObject();
            seeds.addProperty("tag", "forge:seeds/barley");
            ingredients.add(seeds);
            json.add("ingredients", ingredients);
            JsonObject result = new JsonObject();
            result.addProperty("item", "patchouli:guide_book");
            result.addProperty("nbt", "{\"patchouli:book\":\"" + book + "\"}");
            json.add("result", result);
        }));
    }

    // --- Create, Mekanism and Immersive Engineering: our stations' work on their machines (GDD section 22.2) ----------
    // StationRecipes calls these beside each of our own recipes, so the two never drift apart.

    /**
     * Grinding those mods already do their own way, so a twin of ours would compete with theirs for the same input: Create
     * mills wheat into its own wheat flour (in forge:flour/wheat, so our recipes take it), and Mekanism crushes wheat into
     * Bio Fuel.
     */
    private static final Map<String, java.util.Set<String>> OWN_MILLING = Map.of(CREATE, java.util.Set.of("wheat_flour"),
            MEKANISM, java.util.Set.of("wheat_flour"));

    /** What our Millstone grinds: Create's Millstone and Crushing Wheels, Mekanism's Crusher, IE's Crusher. */
    public static void milling(Consumer<FinishedRecipe> out, String name, Ingredient input, ItemLike result, @Nullable ItemLike byproduct) {
        if (!OWN_MILLING.get(CREATE).contains(name)) conditional(out, CREATE, new Foreign(SeedToCellar.id("create/milling/" + name), "create:milling", json -> {
            JsonArray ingredients = new JsonArray();
            ingredients.add(input.toJson());
            json.add("ingredients", ingredients);
            json.addProperty("processingTime", 150);
            JsonArray results = new JsonArray();
            results.add(item(result, 1));
            if (byproduct != null) results.add(item(byproduct, 1));
            json.add("results", results);
        }));
        if (!OWN_MILLING.get(MEKANISM).contains(name)) conditional(out, MEKANISM, new Foreign(SeedToCellar.id("mekanism/crushing/" + name), "mekanism:crushing", json -> {
            JsonObject in = new JsonObject();
            in.add("ingredient", input.toJson());
            json.add("input", in);
            json.add("output", item(result, 1));
        }));
        conditional(out, IMMERSIVE, new Foreign(SeedToCellar.id("immersiveengineering/crusher/" + name), "immersiveengineering:crusher", json -> {
            json.addProperty("energy", 1600);
            json.add("input", input.toJson());
            json.add("result", item(result, 1));
            JsonArray secondaries = new JsonArray();
            if (byproduct != null) {
                JsonObject secondary = new JsonObject();
                secondary.addProperty("chance", 1.0F);
                secondary.add("output", item(byproduct, 1));
                secondaries.add(secondary);
            }
            json.add("secondaries", secondaries);
        }));
    }

    /**
     * What our Fruit Press (`count` of the input) or Crushing Tub (one at a time) gets out of fruit: Create's Mechanical Press
     * on a Basin (compacting) and IE's Squeezer, with the press's pomace beside it.
     */
    public static void pressing(Consumer<FinishedRecipe> out, String name, Ingredient input, int count, Fluid fluid, int amount,
                                @Nullable ItemLike byproduct) {
        conditional(out, CREATE, new Foreign(SeedToCellar.id("create/compacting/" + name), "create:compacting", json -> {
            JsonArray ingredients = new JsonArray();
            for (int i = 0; i < count; i++) ingredients.add(input.toJson());
            json.add("ingredients", ingredients);
            JsonArray results = new JsonArray();
            results.add(fluid(fluid, amount, null));
            if (byproduct != null) results.add(item(byproduct, 1));
            json.add("results", results);
        }));
        conditional(out, IMMERSIVE, new Foreign(SeedToCellar.id("immersiveengineering/squeezer/" + name), "immersiveengineering:squeezer", json -> {
            json.addProperty("energy", 6400);
            if (count > 1) {
                JsonObject in = new JsonObject();
                in.add("base_ingredient", input.toJson());
                in.addProperty("count", count);
                json.add("input", in);
            } else {
                json.add("input", input.toJson());
            }
            json.add("fluid", fluid(fluid, amount, null));
            if (byproduct != null) json.add("result", item(byproduct, 1));
        }));
    }

    /** What our Brew Kettle stirs into a liquid (`perBucket` of it a bucket; 0: boiled down alone): Create's heated mixing. */
    public static void mixing(Consumer<FinishedRecipe> out, String name, JsonObject liquid, @Nullable Ingredient ingredient, int perBucket,
                              Fluid result) {
        conditional(out, CREATE, new Foreign(SeedToCellar.id("create/mixing/" + name), "create:mixing", json -> {
            json.addProperty("heatRequirement", "heated");
            JsonArray ingredients = new JsonArray();
            if (ingredient != null) for (int i = 0; i < perBucket; i++) ingredients.add(ingredient.toJson());
            ingredients.add(liquid);
            json.add("ingredients", ingredients);
            JsonArray results = new JsonArray();
            results.add(fluid(result, 1000, null));
            // what the ingredient leaves behind comes back, as from our kettle (honey bottles: glass bottles)
            net.minecraft.world.item.ItemStack[] items = ingredient == null ? new net.minecraft.world.item.ItemStack[0] : ingredient.getItems();
            if (items.length > 0 && items[0].hasCraftingRemainingItem()) results.add(item(items[0].getCraftingRemainingItem().getItem(), perBucket));
            json.add("results", results);
        }));
    }

    /** A Create fluid ingredient: a bucket of a fluid tag. */
    public static JsonObject fluidTag(net.minecraft.tags.TagKey<Fluid> tag) {
        JsonObject json = new JsonObject();
        json.addProperty("fluidTag", tag.location().toString());
        json.addProperty("amount", 1000);
        return json;
    }

    /** A Create fluid ingredient: a bucket of one fluid. */
    public static JsonObject fluidIn(Fluid fluid, @Nullable String nbt) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        json.addProperty("amount", 1000);
        if (nbt != null) json.addProperty("nbt", nbt);
        return json;
    }

    /**
     * Beer on Create (GDD section 22.2): heated mixing mashes grist into a bucket of water as sweet wort, its malt bill written
     * on it as our kettle would (2 grist a bucket is a normal wort, 4 a strong one), and boils that wort with dried hops into
     * hopped wort keeping it. One bill per style: pale (pale ale, lager), amber (amber ale), dark (stout), strong amber (old
     * ale), wheat (wheat beer). Any other bill is brewed in our kettle.
     */
    public static void worts(Consumer<FinishedRecipe> out, Map<String, Map<ItemLike, Integer>> bills, ItemLike spentGrain, ItemLike hops,
                             Fluid water, Fluid sweetWort, Fluid hoppedWort) {
        bills.forEach((name, grist) -> {
            int total = grist.values().stream().mapToInt(Integer::intValue).sum();
            StringBuilder malts = new StringBuilder();
            grist.forEach((malt, n) -> {
                if (!malts.isEmpty()) malts.append(',');
                String key = BuiltInRegistries.ITEM.getKey(malt.asItem()).getPath().replace("_grist", "");
                malts.append(key).append(':').append(Math.round(n * 20F / total) / 20F).append('f');
            });
            String strength = total >= 4 ? "STRONG" : "NORMAL";
            String nbt = "{Wort:{Malts:{" + malts + "},Strength:\"" + strength + "\"}}";
            conditional(out, CREATE, new Foreign(SeedToCellar.id("create/mixing/" + name + "_wort"), "create:mixing", json -> {
                json.addProperty("heatRequirement", "heated");
                JsonArray ingredients = new JsonArray();
                grist.forEach((malt, n) -> {
                    for (int i = 0; i < n; i++) ingredients.add(Ingredient.of(malt).toJson());
                });
                ingredients.add(fluidIn(water, null));
                json.add("ingredients", ingredients);
                JsonArray results = new JsonArray();
                results.add(fluid(sweetWort, 1000, nbt));
                results.add(item(spentGrain, (total + 1) / 2));
                json.add("results", results);
            }));
            conditional(out, CREATE, new Foreign(SeedToCellar.id("create/mixing/" + name + "_hopped_wort"), "create:mixing", json -> {
                json.addProperty("heatRequirement", "heated");
                JsonArray ingredients = new JsonArray();
                ingredients.add(Ingredient.of(hops).toJson());
                ingredients.add(fluidIn(sweetWort, nbt));
                json.add("ingredients", ingredients);
                JsonArray results = new JsonArray();
                results.add(fluid(hoppedWort, 1000, nbt));
                json.add("results", results);
            }));
        });
    }

    /** An IE Garden Cloche recipe: a seed on its soil grows `results`, drawn as a crop by its age. */
    public static void cloche(Consumer<FinishedRecipe> out, String name, ItemLike seed, net.minecraft.world.level.block.Block block,
                              String soilTag, int ticks, List<JsonObject> results) {
        conditional(out, IMMERSIVE, new Foreign(SeedToCellar.id("immersiveengineering/cloche/" + name), "immersiveengineering:cloche", json -> {
            json.add("input", Ingredient.of(seed).toJson());
            JsonObject render = new JsonObject();
            render.addProperty("type", "crop");
            render.addProperty("block", BuiltInRegistries.BLOCK.getKey(block).toString());
            json.add("render", render);
            JsonArray array = new JsonArray();
            results.forEach(array::add);
            json.add("results", array);
            JsonObject soil = new JsonObject();
            soil.addProperty("tag", soilTag);
            json.add("soil", soil);
            json.addProperty("time", ticks);
        }));
    }

    /**
     * A Botany Pots crop: the seed, the soils it takes (Botany Pots' soil categories), the block it shows (growing through its
     * ages when `aging`), and its drops (each {item, chance, min, max}).
     */
    public static void botanyPot(Consumer<FinishedRecipe> out, String name, ItemLike seed, List<String> soils,
                                 net.minecraft.world.level.block.Block display, boolean aging, int ticks, List<JsonObject> drops) {
        conditional(out, BOTANY_POTS, new Foreign(SeedToCellar.id("botanypots/crop/" + name), "botanypots:crop", json -> {
            json.add("seed", Ingredient.of(seed).toJson());
            JsonArray categories = new JsonArray();
            soils.forEach(categories::add);
            json.add("categories", categories);
            json.addProperty("growthTicks", ticks);
            JsonObject shown = new JsonObject();
            if (aging) shown.addProperty("type", "botanypots:aging");
            shown.addProperty("block", BuiltInRegistries.BLOCK.getKey(display).toString());
            json.add("display", shown);
            JsonArray array = new JsonArray();
            drops.forEach(array::add);
            json.add("drops", array);
        }));
    }

    /** A Botany Pots drop: `chance` of `min`-`max` of the item. */
    public static JsonObject potDrop(ItemLike item, float chance, int min, int max) {
        JsonObject json = new JsonObject();
        json.addProperty("chance", chance);
        json.add("output", item(item, 1));
        json.addProperty("minRolls", min);
        json.addProperty("maxRolls", max);
        return json;
    }

    /** An item result of `count`. */
    public static JsonObject itemResult(ItemLike item, int count) {
        return item(item, count);
    }

    private static JsonObject fluid(Fluid fluid, int amount, @Nullable String nbt) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        json.addProperty("amount", amount);
        if (nbt != null) json.addProperty("nbt", nbt);
        return json;
    }

    /** Farmer's Delight Cooking Pot: up to 6 ingredients, an optional container, served as `count`. */
    public static void cookingPot(Consumer<FinishedRecipe> out, String name, List<Ingredient> ingredients, @Nullable ItemLike container,
                                  ItemLike result, int count, int ticks) {
        conditional(out, FARMERS_DELIGHT, new Foreign(SeedToCellar.id("farmersdelight/cooking/" + name), "farmersdelight:cooking", json -> {
            json.addProperty("recipe_book_tab", "meals");
            JsonArray array = new JsonArray();
            for (Ingredient ingredient : ingredients) array.add(ingredient.toJson());
            json.add("ingredients", array);
            json.add("result", item(result, count));
            if (container != null) json.add("container", item(container, 1));
            json.addProperty("experience", 1.0F);
            json.addProperty("cookingtime", ticks);
        }));
    }

    /** Farmer's Delight Cutting Board: one item cut with a knife into `count` of another. */
    public static void cuttingBoard(Consumer<FinishedRecipe> out, String name, ItemLike input, ItemLike result, int count) {
        conditional(out, FARMERS_DELIGHT, new Foreign(SeedToCellar.id("farmersdelight/cutting/" + name), "farmersdelight:cutting", json -> {
            JsonArray ingredients = new JsonArray();
            ingredients.add(Ingredient.of(input).toJson());
            json.add("ingredients", ingredients);
            JsonObject tool = new JsonObject();
            tool.addProperty("tag", "forge:tools/knives");
            json.add("tool", tool);
            JsonArray results = new JsonArray();
            results.add(item(result, count));
            json.add("result", results);
        }));
    }

    private static void conditional(Consumer<FinishedRecipe> out, String modId, Foreign recipe) {
        ConditionalRecipe.builder().addCondition(new ModLoadedCondition(modId)).addRecipe(recipe).build(out, recipe.id());
    }

    private static JsonObject item(ItemLike item, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
        if (count > 1) json.addProperty("count", count);
        return json;
    }

    /** A recipe of another mod's type: its "type" and body are written as-is. */
    private record Foreign(Identifier id, String type, Consumer<JsonObject> body) implements FinishedRecipe {
        @Override
        public JsonObject serializeRecipe() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type);
            serializeRecipeData(json);
            return json;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            body.accept(json);
        }

        @Override
        public Identifier getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;   // never used: serializeRecipe writes the real type
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public Identifier getAdvancementId() {
            return null;
        }
    }

    private CompatRecipes() {}
}
