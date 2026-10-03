package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Wires up every data provider. Run with the Gradle task {@code runData}. */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();

        // Worldgen entries (features, placements, biome modifiers) extend the registry lookup used by tags.
        DatapackBuiltinEntriesProvider worldgen = generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), ModWorldGen.BUILDER, Set.of(SeedToCellar.MOD_ID)));
        CompletableFuture<HolderLookup.Provider> lookup = worldgen.getRegistryProvider();

        // Client assets
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, files));

        // Server data
        ModBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModFluidTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModPoiTypeTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        generator.addProvider(event.includeServer(), ModLootTableProvider.create(output));
        generator.addProvider(event.includeServer(), new ModLootModifierProvider(output));
        generator.addProvider(event.includeClient() || event.includeServer(), new ModBookProvider(output));
        generator.addProvider(event.includeServer(), new ModCompatDataProvider(output));
        generator.addProvider(event.includeServer(), new ForgeAdvancementProvider(output, lookup, files, List.of(new ModAdvancements())));

        // Must stay last: providers run in order, and the audit checks what they wrote.
        generator.addProvider(event.includeClient() && event.includeServer(), new AssetAuditProvider(output, files));
    }

    private DataGenerators() {}
}
