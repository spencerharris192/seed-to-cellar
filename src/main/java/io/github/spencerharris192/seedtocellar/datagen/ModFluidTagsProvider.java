package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.Vessel;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, SeedToCellar.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Every beer: "any ale" in cooking (beef & ale stew).
        for (Drinks.Drink beer : Drinks.beers()) tag(ModTags.Fluids.ALES).add(beer.fluid().get());
        // Left open in a Preserving Jar, these sour into vinegar: any beer, wine, cider, perry or mead.
        tag(ModTags.Fluids.SOURS_TO_VINEGAR).addTag(ModTags.Fluids.ALES);
        for (Drinks.Drink drink : Drinks.all()) {
            if (drink.profile().graded() && (drink.vessel() == Vessel.WINE_BOTTLE || drink == Drinks.CIDER || drink == Drinks.PERRY)) {
                tag(ModTags.Fluids.SOURS_TO_VINEGAR).add(drink.fluid().get());
            }
        }
        tag(ModTags.Fluids.LEMON_JUICE).add(ModFluids.LEMON_JUICE.get());
        // Distillery: every spirit can run again; grain spirits turn to vodka on a third run; the filter makes vodka of these.
        for (Drinks.Drink spirit : Drinks.spirits()) tag(ModTags.Fluids.SPIRITS).add(spirit.fluid().get());
        tag(ModTags.Fluids.GRAIN_SPIRITS).add(ModFluids.MALT_WHISKEY.get(), ModFluids.BOURBON.get());
        tag(ModTags.Fluids.VODKA_SOURCES).addTag(ModTags.Fluids.GRAIN_SPIRITS)
                .add(ModFluids.PLAIN_ALE.get(), ModFluids.CORN_WASH.get(), ModFluids.POTATO_WASH.get(), ModFluids.VODKA.get());
        tag(ModTags.Fluids.FERTILIZERS).add(ModFluids.STILLAGE.get());
        tag(ModTags.Fluids.LIQUEUR_BASES).add(ModFluids.VODKA.get(), ModFluids.BRANDY.get(), ModFluids.RUM.get());
        tag(ModTags.Fluids.GRAPE_WINES).add(ModFluids.RED_WINE.get(), ModFluids.WHITE_WINE.get(), ModFluids.ROSE.get());
        // What the Brew Kettle's tank holds: water and worts for brewing; milk, ales and wines for cooking; honey water
        // (mixed in it) for mead; lemon juice and lemonade; sorghum juice (boiled down to syrup).
        tag(ModTags.Fluids.KETTLE_LIQUIDS).addTag(FluidTags.WATER).addTag(Tags.Fluids.MILK).addTag(ModTags.Fluids.ALES)
                .addTag(ModTags.Fluids.LEMON_JUICE)
                .add(ModFluids.SWEET_WORT.get(), ModFluids.HOPPED_WORT.get(), ModFluids.HONEY_WATER.get(), ModFluids.RED_WINE.get(),
                        ModFluids.WHITE_WINE.get(), ModFluids.LEMONADE.get(), ModFluids.SORGHUM_JUICE.get(), ModFluids.CANE_JUICE.get(),
                        ModFluids.AGAVE_JUICE.get());
    }
}
