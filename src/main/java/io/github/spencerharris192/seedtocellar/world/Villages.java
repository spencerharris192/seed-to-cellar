package io.github.spencerharris192.seedtocellar.world;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.Map;

/**
 * Village buildings (GDD section 18): the Vineyard (plains and savanna villages) and Brewhouse (plains, taiga and snowy
 * villages), added to their villages' building lists when a world starts (the way most mods add village buildings, so no
 * vanilla file is replaced). The Vintner's and Brewer's trades and the wandering trader's are data (datagen/ModTrades).
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class Villages {
    /**
     * How often each building is tried among a village's houses, by village (vanilla houses weigh 1-4). Ours are big and
     * often don't fit where they're tried, so they weigh more; tuned so each turns up in roughly 40-50% of its villages
     * (VillageTests checks a good share do).
     */
    private static final Map<String, Integer> VINEYARD_WEIGHTS = Map.of("plains", 10, "savanna", 12);
    private static final Map<String, Integer> BREWHOUSE_WEIGHTS = Map.of("plains", 18, "taiga", 10, "snowy", 16);
    /** The Brewhouse's cellar template is this many blocks tall, built under the hall's floor. */
    private static final int BREWHOUSE_CELLAR_DEPTH = 4;

    @SubscribeEvent
    public static void addVillageBuildings(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        if (ModConfigs.worldgenEnabled("vineyards")) {
            VINEYARD_WEIGHTS.forEach((village, weight) ->
                    // single (not legacy) elements place the template's air, so the plot is cleared
                    addBuilding(pools, village, StructurePoolElement.single(SeedToCellar.id("village/" + village + "/vineyard").toString())
                            .apply(StructureTemplatePool.Projection.RIGID), weight));
        }
        if (ModConfigs.worldgenEnabled("brewhouses")) {
            BREWHOUSE_WEIGHTS.forEach((village, weight) ->
                    addBuilding(pools, village, CellarPoolElement.of(SeedToCellar.id("village/" + village + "/brewhouse"),
                            SeedToCellar.id("village/" + village + "/brewhouse_cellar"), BREWHOUSE_CELLAR_DEPTH), weight));
        }
    }

    private static void addBuilding(Registry<StructureTemplatePool> pools, String village, StructurePoolElement element, int weight) {
        StructureTemplatePool target = pools.getValue(SeedToCellar.rl("minecraft", "village/" + village + "/houses"));
        if (target == null) return;
        // Villages draw from the pool's expanded list (each building repeated by its weight; opened by our access transformer).
        for (int i = 0; i < weight; i++) target.templates.add(element);
    }

    private Villages() {}
}
