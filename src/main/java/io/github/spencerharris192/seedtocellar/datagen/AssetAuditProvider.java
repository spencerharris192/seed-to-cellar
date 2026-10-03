package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.brewing.BottledLiquidItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Registry audit (GDD section 21). Runs last in {@code runData} and FAILS the run if any of
 * our items or blocks is missing a translation, model, texture, blockstate, loot table, or a
 * way to be obtained. A report is written to build/reports/asset-audit.txt.
 */
public class AssetAuditProvider implements DataProvider {
    /**
     * Items with no recipe or loot source yet, and why. Printed as warnings on every run.
     * Must be EMPTY before any public release. Entries here are temporary work-in-progress only.
     */
    private static final Map<String, String> KNOWN_UNOBTAINABLE = Map.of();

    /** Items that station or block code (not a recipe file) produces. Permanent, legitimate sources. */
    private static final Map<String, String> MADE_BY_STATIONS = Map.of(
            "spent_grain", "Brew Kettle: left over from every mash",
            "compost", "Compost Bin: 16 leftovers become 4 compost",
            "mother_of_vinegar", "Preserving Jar: beer left open sours into vinegar and a mother forms",
            "harvest_feast_serving", "Harvest Feast: right-click the placed feast with a bowl",
            "grape_leaves", "Grape vines: shears on a grown vine cut them off",
            "wine_yeast", "Fermenting Vat: the lees of any fruit or honey ferment");

    private static final String NS = SeedToCellar.MOD_ID;

    private final Path generated;
    private final Path existing;
    private final Path report;
    private final ExistingFileHelper files;

    public AssetAuditProvider(PackOutput output, ExistingFileHelper files) {
        this.generated = output.getOutputFolder();                       // <project>/src/generated/resources
        Path project = generated.getParent().getParent().getParent();
        this.existing = project.resolve("src/main/resources");
        this.report = project.resolve("build/reports/asset-audit.txt");
        this.files = files;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(this::audit, Util.backgroundExecutor());
    }

    @Override
    public String getName() {
        return "Seed to Cellar asset audit";
    }

    private void audit() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        JsonObject lang = readLang();
        Set<String> obtainable = collectObtainable();
        int blockCount = 0;
        int itemCount = 0;

        for (Map.Entry<net.minecraft.resources.ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (!id.getNamespace().equals(NS)) continue;
            blockCount++;
            Block block = entry.getValue();
            String where = "block " + id;
            checkLang(lang, block.getDescriptionId(), where, errors);
            String blockstate = "assets/" + NS + "/blockstates/" + id.getPath() + ".json";
            Optional<JsonObject> state = readJson(blockstate);
            if (state.isEmpty()) {
                errors.add(where + ": missing blockstate " + blockstate);
            } else {
                for (String model : blockstateModels(state.get())) checkModel(model, where, errors);
            }
            if (!block.getLootTable().equals(BuiltInLootTables.EMPTY)) {
                String loot = "data/" + NS + "/loot_tables/blocks/" + id.getPath() + ".json";
                if (find(loot).isEmpty()) errors.add(where + ": missing loot table " + loot);
            }
        }

        for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (!id.getNamespace().equals(NS)) continue;
            itemCount++;
            Item item = entry.getValue();
            String where = "item " + id;
            checkLang(lang, item.getDescriptionId(), where, errors);
            checkGuide(lang, id.getPath(), where, errors);
            checkModel(NS + ":item/" + id.getPath(), where, errors);
            // Buckets, drinks and bottled liquids (olive oil) are filled from any vessel holding their liquid.
            boolean filledFromVessels = item instanceof BucketItem || item instanceof DrinkItem || item instanceof BottledLiquidItem;
            boolean madeByStation = MADE_BY_STATIONS.containsKey(id.getPath());
            if (!obtainable.contains(id.toString()) && !filledFromVessels && !madeByStation) {
                String reason = KNOWN_UNOBTAINABLE.get(id.getPath());
                if (reason != null) {
                    warnings.add(where + ": not obtainable yet (allowed for now: " + reason + ")");
                } else if (!(item instanceof BlockItem)) {
                    errors.add(where + ": no recipe or loot table produces it (orphan)");
                } else {
                    errors.add(where + ": no recipe or loot table produces it (does its block drop itself?)");
                }
            }
        }

        // Effects: a name, and an icon for the inventory and the top-right of the screen.
        for (Map.Entry<net.minecraft.resources.ResourceKey<MobEffect>, MobEffect> entry : ForgeRegistries.MOB_EFFECTS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (!id.getNamespace().equals(NS)) continue;
            String where = "effect " + id;
            checkLang(lang, entry.getValue().getDescriptionId(), where, errors);
            String icon = "assets/" + NS + "/textures/mob_effect/" + id.getPath() + ".png";
            if (find(icon).isEmpty()) errors.add(where + ": missing icon " + icon);
        }

        BuiltInRegistries.CREATIVE_MODE_TAB.entrySet().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(NS))
                .forEach(e -> {
                    if (e.getValue().getDisplayName().getContents() instanceof TranslatableContents t) {
                        checkLang(lang, t.getKey(), "creative tab " + e.getKey().location(), errors);
                    }
                });

        checkModelUvs(errors);
        writeReport(blockCount, itemCount, errors, warnings);
        warnings.forEach(w -> SeedToCellar.LOGGER.warn("[asset audit] {}", w));
        if (!errors.isEmpty()) {
            errors.forEach(e -> SeedToCellar.LOGGER.error("[asset audit] {}", e));
            throw new IllegalStateException("Asset audit failed with " + errors.size() + " problem(s). See " + report);
        }
        SeedToCellar.LOGGER.info("[asset audit] OK: {} blocks, {} items, {} warning(s)", blockCount, itemCount, warnings.size());
    }

    // --- checks ----------------------------------------------------------------------------

    /** Every item has a "what is this" or "Next:" line (Phase 7: discover everything without JEI). */
    private static void checkGuide(JsonObject lang, String path, String where, List<String> errors) {
        String base = "tooltip." + NS + "." + path;
        if (!lang.has(base + ".desc") && !lang.has(base + ".next")) {
            errors.add(where + ": no 'what is this' or 'Next:' tooltip (add a guide line in ModLanguageProvider)");
        }
    }

    private static void checkLang(JsonObject lang, String key, String where, List<String> errors) {
        if (!lang.has(key)) errors.add(where + ": missing translation for '" + key + "'");
    }

    /** Checks a model exists (ours, or vanilla) and that every texture it names exists. */
    private void checkModel(String modelRef, String where, List<String> errors) {
        ResourceLocation model = SeedToCellar.parse(modelRef);
        if (!model.getNamespace().equals(NS)) {
            if (!files.exists(model, PackType.CLIENT_RESOURCES, ".json", "models")) {
                errors.add(where + ": missing model " + model);
            }
            return;
        }
        String path = "assets/" + NS + "/models/" + model.getPath() + ".json";
        Optional<JsonObject> json = readJson(path);
        if (json.isEmpty()) {
            errors.add(where + ": missing model " + path);
            return;
        }
        if (json.get().has("textures")) {
            for (Map.Entry<String, JsonElement> texture : json.get().getAsJsonObject("textures").entrySet()) {
                String value = texture.getValue().getAsString();
                if (value.startsWith("#")) continue;
                ResourceLocation tex = SeedToCellar.parse(value);
                boolean found = tex.getNamespace().equals(NS)
                        ? find("assets/" + NS + "/textures/" + tex.getPath() + ".png").isPresent()
                        : files.exists(tex, PackType.CLIENT_RESOURCES, ".png", "textures");
                if (!found) errors.add(where + ": model " + model + " uses missing texture " + tex);
            }
        }
    }

    /**
     * Every block model part that pokes out of its block (above, below or beside the 16x16x16 space) must give its faces
     * explicit UVs: automatic ones would read outside the texture and render black, as the press handle once did.
     */
    private void checkModelUvs(List<String> errors) {
        for (Path root : List.of(generated, existing)) {
            Path models = root.resolve("assets/" + NS + "/models/block");
            if (!Files.isDirectory(models)) continue;
            try (Stream<Path> paths = Files.list(models)) {
                paths.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    JsonObject model = parse(path).getAsJsonObject();
                    if (!model.has("elements")) return;
                    for (JsonElement element : model.getAsJsonArray("elements")) {
                        JsonObject e = element.getAsJsonObject();
                        if (!e.has("faces")) continue;
                        float[] from = floats(e.getAsJsonArray("from")), to = floats(e.getAsJsonArray("to"));
                        for (Map.Entry<String, JsonElement> face : e.getAsJsonObject("faces").entrySet()) {
                            if (face.getValue().getAsJsonObject().has("uv")) continue;
                            for (float v : autoUv(face.getKey(), from, to)) {
                                if (v < 0 || v > 16) {
                                    errors.add("model " + models.relativize(path) + ": the " + face.getKey() + " face of a part outside its block "
                                            + "needs explicit UVs (automatic ones read outside the texture and render black)");
                                    break;
                                }
                            }
                        }
                    }
                });
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private static float[] floats(com.google.gson.JsonArray array) {
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    /** The UVs vanilla gives a face that names none (BlockElement's defaults). */
    private static float[] autoUv(String face, float[] f, float[] t) {
        return switch (face) {
            case "down" -> new float[]{f[0], 16 - t[2], t[0], 16 - f[2]};
            case "up" -> new float[]{f[0], f[2], t[0], t[2]};
            case "north" -> new float[]{16 - t[0], 16 - t[1], 16 - f[0], 16 - f[1]};
            case "south" -> new float[]{f[0], 16 - t[1], t[0], 16 - f[1]};
            case "west" -> new float[]{f[2], 16 - t[1], t[2], 16 - f[1]};
            default -> new float[]{16 - t[2], 16 - t[1], 16 - f[2], 16 - f[1]};
        };
    }

    private static Set<String> blockstateModels(JsonObject state) {
        Set<String> models = new HashSet<>();
        if (state.has("variants")) {
            state.getAsJsonObject("variants").entrySet().forEach(v -> addModels(v.getValue(), models));
        }
        if (state.has("multipart")) {
            state.getAsJsonArray("multipart").forEach(part -> addModels(part.getAsJsonObject().get("apply"), models));
        }
        return models;
    }

    private static void addModels(JsonElement element, Set<String> models) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> models.add(e.getAsJsonObject().get("model").getAsString()));
        } else {
            models.add(element.getAsJsonObject().get("model").getAsString());
        }
    }

    // --- sources of items ------------------------------------------------------------------

    /**
     * Every item ID that has a real source in our generated or hand-written data: a recipe
     * result, a loot table entry (except a block dropping itself, which is circular), or a
     * block placed by world generation (its block item can be picked from the world).
     */
    private Set<String> collectObtainable() {
        Set<String> ids = new HashSet<>();
        for (Path root : List.of(generated, existing)) {
            Path data = root.resolve("data");
            if (!Files.isDirectory(data)) continue;
            try (Stream<Path> paths = Files.walk(data)) {
                paths.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    String rel = data.relativize(p).toString().replace('\\', '/');
                    JsonElement json = parse(p);
                    if (rel.contains("/recipes/") && json.isJsonObject() && json.getAsJsonObject().has("result")) {
                        collectRecipeResult(json.getAsJsonObject().get("result"), ids);
                        // station recipes that leave something behind (the press's pomace)
                        if (json.getAsJsonObject().has("byproduct")) collectRecipeResult(json.getAsJsonObject().get("byproduct"), ids);
                    } else if (rel.contains("/loot_tables/")) {
                        Set<String> drops = new HashSet<>();
                        collectLootItems(json, drops);
                        // data/<ns>/loot_tables/blocks/<name>.json dropping <ns>:<name> is not a source
                        String[] parts = rel.split("/");
                        if (parts.length == 4 && parts[2].equals("blocks")) {
                            drops.remove(parts[0] + ":" + parts[3].replace(".json", ""));
                        }
                        ids.addAll(drops);
                    } else if (rel.contains("/worldgen/configured_feature/")) {
                        collectBlockNames(json, ids);
                    } else if (rel.contains("/loot_modifiers/") && json.isJsonObject() && json.getAsJsonObject().has("item")) {
                        ids.add(json.getAsJsonObject().get("item").getAsString());   // our modifiers name what they add
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return ids;
    }

    /** Block state "Name" entries in worldgen features. Our block items share their block's ID. */
    private static void collectBlockNames(JsonElement element, Set<String> ids) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("Name") && obj.get("Name").isJsonPrimitive()) ids.add(obj.get("Name").getAsString());
            obj.entrySet().forEach(e -> collectBlockNames(e.getValue(), ids));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> collectBlockNames(e, ids));
        }
    }

    private static void collectRecipeResult(JsonElement result, Set<String> ids) {
        if (result.isJsonPrimitive()) {
            ids.add(result.getAsString());
        } else if (result.isJsonObject()) {
            JsonObject obj = result.getAsJsonObject();
            if (obj.has("item")) ids.add(obj.get("item").getAsString());
            if (obj.has("id")) ids.add(obj.get("id").getAsString());
        } else if (result.isJsonArray()) {
            result.getAsJsonArray().forEach(e -> collectRecipeResult(e, ids));
        }
    }

    private static void collectLootItems(JsonElement element, Set<String> ids) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("type") && obj.get("type").getAsString().equals("minecraft:item") && obj.has("name")) {
                ids.add(obj.get("name").getAsString());
            }
            obj.entrySet().forEach(e -> collectLootItems(e.getValue(), ids));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> collectLootItems(e, ids));
        }
    }

    // --- file helpers ----------------------------------------------------------------------

    /** Looks in generated output first, then hand-written resources. */
    private Optional<Path> find(String relative) {
        for (Path root : List.of(generated, existing)) {
            Path path = root.resolve(relative);
            if (Files.isRegularFile(path)) return Optional.of(path);
        }
        return Optional.empty();
    }

    private Optional<JsonObject> readJson(String relative) {
        return find(relative).map(AssetAuditProvider::parse).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);
    }

    private JsonObject readLang() {
        JsonObject merged = new JsonObject();
        for (Path root : List.of(existing, generated)) {
            Path path = root.resolve("assets/" + NS + "/lang/en_us.json");
            if (Files.isRegularFile(path)) parse(path).getAsJsonObject().entrySet().forEach(e -> merged.add(e.getKey(), e.getValue()));
        }
        return merged;
    }

    private static JsonElement parse(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeReport(int blocks, int items, List<String> errors, List<String> warnings) {
        List<String> lines = new ArrayList<>();
        lines.add("Seed to Cellar asset audit");
        lines.add("Checked " + blocks + " blocks and " + items + " items.");
        lines.add("");
        lines.add("ERRORS (" + errors.size() + ")");
        errors.forEach(e -> lines.add("  - " + e));
        lines.add("");
        lines.add("WARNINGS (" + warnings.size() + ")");
        warnings.forEach(w -> lines.add("  - " + w));
        try {
            Files.createDirectories(report.getParent());
            Files.write(report, lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
