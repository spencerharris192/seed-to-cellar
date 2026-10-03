package io.github.spencerharris192.seedtocellar.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Data other mods read from us in their own formats (GDD section 22.2), that no vanilla provider writes. Cold Sweat: a
 * player under Warmth gives off body heat (its entity_temp data). Without Cold Sweat nobody reads these files.
 */
public class ModCompatDataProvider implements DataProvider {
    private final PackOutput output;

    public ModCompatDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public String getName() {
        return "Seed to Cellar data for other mods";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path data = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(SeedToCellar.MOD_ID);
        return DataProvider.saveStable(cache, coldSweatWarmth(), data.resolve("cold_sweat/entity/entity_temp/warmth.json"));
    }

    /** Cold Sweat: Warmth (from whiskey, brandy, stout, mulled wine...) warms the player who drank it, like a fire would. */
    private static JsonObject coldSweatWarmth() {
        JsonObject warmed = new JsonObject();
        JsonArray players = new JsonArray();
        players.add("minecraft:player");
        warmed.add("entities", players);
        JsonObject effects = new JsonObject();
        effects.add(ForgeRegistries.MOB_EFFECTS.getKey(ModEffects.WARMTH.get()).toString(), new JsonObject());
        warmed.add("effects", effects);

        JsonObject json = new JsonObject();
        JsonArray mods = new JsonArray();
        mods.add("cold_sweat");
        json.add("required_mods", mods);
        json.add("entity", warmed);
        json.add("affected_entity", warmed.deepCopy());
        json.addProperty("temperature", 20);
        json.addProperty("units", "f");
        json.addProperty("range", 1);
        json.addProperty("affects_self", true);
        return json;
    }
}
