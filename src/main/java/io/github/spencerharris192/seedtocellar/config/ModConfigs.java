package io.github.spencerharris192.seedtocellar.config;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * All configuration (GDD section 21). Files:
 * <ul>
 *   <li>config/seedtocellar-common.toml: world generation and loot (applies to all worlds)</li>
 *   <li>config/seedtocellar-server.toml: gameplay balance; a world can have its own copy in saves/&lt;world&gt;/syncedconfig/</li>
 *   <li>config/seedtocellar-client.toml: the view sway</li>
 * </ul>
 */
public final class ModConfigs {
    public static final Common COMMON;
    public static final Server SERVER;
    public static final Client CLIENT;
    private static final ModConfigSpec COMMON_SPEC;
    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec CLIENT_SPEC;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        COMMON = new Common(common);
        COMMON_SPEC = common.build();
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        SERVER = new Server(server);
        SERVER_SPEC = server.build();
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        CLIENT = new Client(client);
        CLIENT_SPEC = client.build();
    }

    /**
     * The gameplay settings are synced: the server's file (config/seedtocellar-server.toml, or a world's own copy in
     * saves/&lt;world&gt;/syncedconfig/) goes to every player. World generation and loot settings are local to each game.
     */
    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.LOCAL, COMMON_SPEC, SeedToCellar.MOD_ID + "-common.toml");
        container.registerConfig(ModConfig.Type.SYNCED, SERVER_SPEC, SeedToCellar.MOD_ID + "-server.toml");
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, SeedToCellar.MOD_ID + "-client.toml");
    }

    /** Looked up by name from data files (see ConfigAddFeaturesModifier). Unknown names count as enabled. */
    public static boolean worldgenEnabled(String toggle) {
        ModConfigSpec.BooleanValue value = COMMON.worldgenToggles.get(toggle);
        return value == null || value.get();
    }

    /**
     * Scales a cask's real resting time by the aging speed multiplier. Falls back to 1x when the
     * server config isn't loaded yet (item tooltips can be drawn before joining a world).
     */
    public static long agedTicks(long rawTicks) {
        return Math.round(rawTicks * (SERVER_SPEC.isLoaded() ? SERVER.agingSpeedMultiplier.get() : 1.0));
    }

    /** Serene Seasons may shift fermentation temperature (true before a world's settings load). */
    public static boolean seasonsChangeTemperature() {
        return !SERVER_SPEC.isLoaded() || SERVER.seasonsChangeTemperature.get();
    }

    /** Scales a station's base time by the processing multiplier (never below 1 tick). */
    public static int processTicks(int baseTicks) {
        return Math.max(1, (int) Math.round(baseTicks * SERVER.processTimeMultiplier.get()));
    }

    public static final class Common {
        public final ModConfigSpec.BooleanValue grassSeedDrops;
        /** Each grass-dropped seed's chance, by its item's path (barley_seeds...). */
        public final Map<String, ModConfigSpec.DoubleValue> grassSeedChances;
        public final ModConfigSpec.BooleanValue rightClickHarvest;
        public final ModConfigSpec.DoubleValue cropGrowthMultiplier;
        public final ModConfigSpec.DoubleValue climateSlowdown;
        public final ModConfigSpec.BooleanValue climateWithSereneSeasons;
        public final ModConfigSpec.DoubleValue fertileGrowthBonus;
        public final ModConfigSpec.DoubleValue fertileExtraHarvestChance;
        public final ModConfigSpec.DoubleValue fertileUseChance;
        public final Map<String, ModConfigSpec.BooleanValue> worldgenToggles;
        public final ModConfigSpec.BooleanValue chestLoot;
        public final ModConfigSpec.BooleanValue theOneProbe;

        Common(ModConfigSpec.Builder b) {
            b.push("farming");
            grassSeedDrops = b.comment("Breaking grass sometimes drops barley and oat seeds (and rye seeds in cold biomes), like vanilla wheat seeds.")
                    .define("grassSeedDrops", true);
            b.comment("The chance that breaking a grass block drops each of these seeds (vanilla's wheat seeds: 0.125).").push("grassSeedChances");
            Map<String, ModConfigSpec.DoubleValue> chances = new LinkedHashMap<>();
            for (Crop crop : Crops.all()) {
                if (crop.grassDrop == null) continue;
                chances.put(crop.seedId, b.comment(crop.seedName + (crop.grassDrop.coldOnly() ? ", in cold biomes only." : "."))
                        .defineInRange(crop.name, Double.parseDouble(Float.toString(crop.grassDrop.chance())), 0.0, 1.0));   // 0.05, not 0.0500000007
            }
            grassSeedChances = Map.copyOf(chances);
            b.pop();
            rightClickHarvest = b.comment("Right-click a ripe crop to harvest and replant it. Turn off if another mod already does this.")
                    .define("rightClickHarvest", true);
            cropGrowthMultiplier = b.comment("How fast Seed to Cellar crops grow. 2.0 = twice as fast, 0.5 = half as fast.")
                    .defineInRange("cropGrowthMultiplier", 1.0, 0.0, 10.0);
            climateSlowdown = b.comment("How much growth a crop loses outside its preferred climate (0.5 = grows at half speed, 0 = climate off).",
                            "A greenhouse (glass within 8 blocks above) counts as Warm.")
                    .defineInRange("climateSlowdown", 0.5, 0.0, 1.0);
            climateWithSereneSeasons = b.comment("Keep climate preference on when Serene Seasons is installed (off by default: seasons take over).")
                    .define("climateWithSereneSeasons", false);
            fertileGrowthBonus = b.comment("Fertile Farmland: extra growth for the crop on it (0.5 = about 50% faster, 0 = none).")
                    .defineInRange("fertileGrowthBonus", 0.5, 0.0, 1.0);
            fertileExtraHarvestChance = b.comment("Fertile Farmland: chance a ripe harvest gives one extra crop.")
                    .defineInRange("fertileExtraHarvestChance", 0.5, 0.0, 1.0);
            fertileUseChance = b.comment("Fertile Farmland: chance each ripe harvest uses up one of its 3 fertility (0.33 = about 9 harvests per compost).")
                    .defineInRange("fertileUseChance", 0.33, 0.0, 1.0);
            b.pop();

            b.push("worldgen");
            b.comment("Wild plants generate in the world. Turning one off only affects newly generated chunks.");
            Map<String, ModConfigSpec.BooleanValue> toggles = new LinkedHashMap<>();
            for (Crop crop : Crops.all()) {
                if (crop.growsWild()) {
                    toggles.put("wild_" + crop.name, b.comment("Wild " + crop.displayName.toLowerCase(Locale.ROOT) + " in " + crop.wild.where() + ".")
                            .define(crop.worldgenToggle(), true));
                }
            }
            toggles.put("wild_hops", b.comment("Wild hops in forests.").define("wildHops", true));
            toggles.put("wild_vanilla", b.comment("Wild vanilla vines on jungle tree trunks.").define("wildVanilla", true));
            for (FruitTree tree : FruitTrees.all()) {
                toggles.put(tree.name + "_tree", b.comment("Wild " + tree.displayName.toLowerCase(Locale.ROOT) + " trees in " + tree.where + ".")
                        .define(tree.worldgenToggle(), true));
            }
            toggles.put("wild_orchards", b.comment("Wild orchards: small groves of mixed fruit trees in forests, plains, savannas and jungles.")
                    .define("wildOrchards", true));
            toggles.put("vineyards", b.comment("Vineyards in plains and savanna villages (with a Fruit Press, where Vintners work).")
                    .define("vineyards", true));
            toggles.put("brewhouses", b.comment("Brewhouses in plains, taiga and snowy villages (with a Brew Kettle, where Brewers work).")
                    .define("brewhouses", true));
            worldgenToggles = Map.copyOf(toggles);
            chestLoot = b.comment("Our finds in vanilla chests: seeds in village houses, agave in desert pyramids, vanilla and coffee in",
                            "jungle temples, aged rum in shipwrecks and buried treasure, old wine and spirits in woodland mansions.")
                    .define("chestLoot", true);
            b.pop();

            b.comment("Other mods. Each integration only switches on when that mod is installed; recipes for other mods' machines",
                    "can be removed with a data pack, KubeJS or CraftTweaker. Jade has its own settings for our read-outs.").push("compat");
            theOneProbe = b.comment("The One Probe shows the Hydrometer's read-out on our stations, casks and crops.")
                    .define("theOneProbe", true);
            b.pop();
        }
    }

    public static final class Server {
        public final ModConfigSpec.DoubleValue processTimeMultiplier;
        public final ModConfigSpec.DoubleValue fermentationTimeMultiplier;
        public final ModConfigSpec.DoubleValue agingSpeedMultiplier;
        public final ModConfigSpec.BooleanValue drinkEffects;
        public final ModConfigSpec.BooleanValue seasonsChangeTemperature;
        public final ModConfigSpec.BooleanValue intoxication;
        public final ModConfigSpec.DoubleValue intoxicationIntensity;
        public final ModConfigSpec.IntValue secondsPerUnit;
        public final ModConfigSpec.BooleanValue hangovers;
        public final ModConfigSpec.BooleanValue hiccups;

        Server(ModConfigSpec.Builder b) {
            b.push("brewing");
            processTimeMultiplier = b.comment(
                            "Multiplies how long stations take (malting, the kiln, the kettle, the still, drying, composting). 0.1 = ten times faster, 2.0 = twice as slow.")
                    .defineInRange("processTimeMultiplier", 1.0, 0.001, 100.0);
            fermentationTimeMultiplier = b.comment(
                            "Multiplies fermentation and jar times (a beer takes 1 in-game day at 1.0). 0.1 = ten times faster.")
                    .defineInRange("fermentationTimeMultiplier", 1.0, 0.001, 100.0);
            agingSpeedMultiplier = b.comment(
                            "How fast casks and kegs age and condition drinks. At 1.0, one in-game day = one year on the label. 2.0 = twice as fast.")
                    .defineInRange("agingSpeedMultiplier", 1.0, 0.01, 1000.0);
            drinkEffects = b.comment("Drinks give their small positive effects (Refreshed, Warmth, Courage...).")
                    .define("drinkEffects", true);
            seasonsChangeTemperature = b.comment("With Serene Seasons: winter makes a Fermenting Vat or Preserving Jar a step cooler, summer a step warmer",
                            "(not in the biomes Serene Seasons gives wet and dry seasons instead: deserts, savannas, jungles...).")
                    .define("seasonsChangeTemperature", true);
            b.pop();

            b.push("intoxication");
            intoxication = b.comment("Alcoholic drinks make you tipsy. Off = drinks only give their positive effects.")
                    .define("enabled", true);
            intoxicationIntensity = b.comment("Multiplies how much each drink adds. 0.5 = half as strong.")
                    .defineInRange("intensity", 1.0, 0.0, 10.0);
            secondsPerUnit = b.comment("Seconds for one unit of alcohol to wear off (a beer is 1 unit, wine 2, spirits 3).")
                    .defineInRange("secondsPerUnit", 40, 1, 3600);
            hangovers = b.comment("Getting drunk (7+ units) gives a hangover afterwards: slower mining for 2-5 minutes.")
                    .define("hangovers", true);
            hiccups = b.comment("Smashed (11+ units), you hiccup now and then: a little sound and a little hop.")
                    .define("hiccups", true);
            b.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.DoubleValue swayIntensity;

        Client(ModConfigSpec.Builder b) {
            swayIntensity = b.comment("How much the view sways when tipsy (also scaled by vanilla's Distortion Effects slider). 0 = none.")
                    .defineInRange("swayIntensity", 1.0, 0.0, 1.0);
        }
    }

    private ModConfigs() {}
}
