package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/**
 * Wires up every data provider. Run with the Gradle task {@code runData} (client data: assets and data together).
 * World registry entries (worldgen, trades) come first, then the reloadable ones (loot, recipes, advancements, burn times),
 * so the tag providers after them can see our new entries.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        Set<String> ours = Set.of(SeedToCellar.MOD_ID);
        event.createWorldRegistryObjects(ModWorldGen.BUILDER, ours);
        event.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(Registries.LOOT_TABLE, ModLootTableProvider.create())
                .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(ModAdvancements::new)))
                .add(Registries.CONTEXT_INT_PROVIDER, ModContextProviders::bootstrap)
                .add(RecipeProvider.asBootstrap(ModRecipeProvider::new)), ours);

        // Client assets
        event.createProvider(ModLanguageProvider::new);
        event.createProvider(ModBlockStateProvider::new);
        event.createProvider(ModItemModelProvider::new);

        // Server data
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModBiomeTagsProvider::new);
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModPoiTypeTagsProvider::new);
        event.createProvider(ModTrades.Tags::new);
        event.createProvider(ModLootModifierProvider::new);

        // Must stay last: providers run in order, and the audit checks what they wrote.
        event.createProvider(output -> new AssetAuditProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
    }

    private DataGenerators() {}
}
