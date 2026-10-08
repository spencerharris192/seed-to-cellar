package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.concurrent.CompletableFuture;

/**
 * Point-of-interest tags. A villager with no job only looks for job sites in {@code minecraft:acquirable_job_site},
 * so every profession's workstation must be added here or nobody will ever take it up.
 */
public class ModPoiTypeTagsProvider extends TagsProvider<PoiType> {
    public ModPoiTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, lookup, SeedToCellar.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(ModVillagers.VINTNER_POI.key(), ModVillagers.BREWER_POI.key());
    }
}
