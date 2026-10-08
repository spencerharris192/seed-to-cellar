package io.github.spencerharris192.seedtocellar.datagen.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes item models (models/item) and every item's definition (items/<id>.json), the file the game reads first: which
 * model to draw, and its tints and switches (a weathered kettle, a charred cask, a crowned whiskey). An item with no
 * definition of its own simply shows item/<id>.
 */
public abstract class ItemModelProvider implements DataProvider {
    private final PackOutput output;
    private final Map<Identifier, BlockModelBuilder> built = new LinkedHashMap<>();
    private final Map<Item, JsonObject> definitions = new IdentityHashMap<>();

    protected ItemModelProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void registerModels();

    public Identifier modLoc(String path) {
        return SeedToCellar.id(path);
    }

    public Identifier mcLoc(String path) {
        return SeedToCellar.rl("minecraft", path);
    }

    /** item/<name> (a name with a slash is taken as the full path). */
    public BlockModelBuilder getBuilder(String name) {
        Identifier id = name.contains(":") ? Identifier.parse(name) : SeedToCellar.id(name.contains("/") ? name : "item/" + name);
        return built.computeIfAbsent(id, BlockModelBuilder::new);
    }

    public BlockModelBuilder withExistingParent(String name, Identifier parent) {
        return getBuilder(name).parent(new ModelFile(parent));
    }

    /** A flat icon from textures/item/<id>.png. */
    public BlockModelBuilder basicItem(Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        return withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name));
    }

    /** What the item shows, in place of plain item/<id>. */
    public void definition(Item item, JsonObject model) {
        definitions.put(item, model);
    }

    // --- item model definitions (the "model" object of items/<id>.json)

    public static JsonObject model(Identifier model, JsonObject... tints) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:model");
        json.addProperty("model", model.toString());
        if (tints.length > 0) {
            JsonArray array = new JsonArray();
            for (JsonObject tint : tints) array.add(tint);
            json.add("tints", array);
        }
        return json;
    }

    /** A fixed color (RGB). */
    public static JsonObject constantTint(int rgb) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:constant");
        json.addProperty("value", rgb);
        return json;
    }

    /** No tint (white). */
    public static JsonObject noTint() {
        return constantTint(-1);
    }

    /** The color of the liquid the item holds (NeoForge's fluid contents tint: buckets, bottles, drinks). */
    public static JsonObject fluidTint() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:fluid_contents_tint");
        return json;
    }

    /** One model when the item has `component`, another when not. */
    public static JsonObject hasComponent(Identifier component, JsonObject onTrue, JsonObject onFalse) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:condition");
        json.addProperty("property", "minecraft:has_component");
        json.addProperty("component", component.toString());
        json.add("on_true", onTrue);
        json.add("on_false", onFalse);
        return json;
    }

    /** A model by the value of a property of the blockstate the item carries (as it will be placed). */
    public static JsonObject byBlockState(String property, Map<String, JsonObject> cases, JsonObject fallback) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:select");
        json.addProperty("property", "minecraft:block_state");
        json.addProperty("block_state_property", property);
        JsonArray array = new JsonArray();
        cases.forEach((when, model) -> {
            JsonObject c = new JsonObject();
            c.addProperty("when", when);
            c.add("model", model);
            array.add(c);
        });
        json.add("cases", array);
        json.add("fallback", fallback);
        return json;
    }

    /** A bucket drawn with its liquid's texture and color (NeoForge's fluid container model). */
    public static JsonObject bucket(Fluid fluid) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:fluid_container");
        JsonObject textures = new JsonObject();
        textures.addProperty("base", "minecraft:item/bucket");
        textures.addProperty("fluid", "neoforge:item/mask/bucket_fluid");
        json.add("textures", textures);
        json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        return json;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        registerModels();
        PackOutput.PathProvider modelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        PackOutput.PathProvider itemPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        built.forEach((id, model) -> futures.add(DataProvider.saveStable(cache, model.toJson(), modelPaths.json(id))));
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(SeedToCellar.MOD_ID)) continue;
            JsonObject json = new JsonObject();
            json.add("model", definitions.getOrDefault(item, model(id.withPrefix("item/"))));
            futures.add(DataProvider.saveStable(cache, json, itemPaths.json(id)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Item models: " + SeedToCellar.MOD_ID;
    }
}
