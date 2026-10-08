package io.github.spencerharris192.seedtocellar.compat.jade;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Our Jade panels: each one's id and the name Jade shows for it in its settings. Kept apart from the plugin (no Jade
 * classes here) so datagen can write every name: Jade refuses to open the title screen if a panel has none.
 */
public final class JadeIds {
    public static final Map<String, String> NAMES = new LinkedHashMap<>();

    public static final Identifier STATION = add("station", "Seed to Cellar: stations");
    public static final Identifier FARM = add("farm", "Seed to Cellar: climate and soil");
    public static final Identifier TABLE_FOOD = add("table_food", "Seed to Cellar: pies and feasts");
    public static final Identifier VINES = add("vines", "Seed to Cellar: vines on trellises");

    private static Identifier add(String path, String name) {
        NAMES.put(path, name);
        return SeedToCellar.id(path);
    }

    /** The settings-name key Jade looks up for a panel. */
    public static String configKey(String path) {
        return "config.jade.plugin_" + SeedToCellar.MOD_ID + "." + path;
    }

    private JadeIds() {}
}
