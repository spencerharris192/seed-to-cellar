package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends ValueTagsProvider<Block> {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, BuiltInRegistries.BLOCK, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Crop crop : Crops.all()) {
            if (crop.style == Crop.Style.VINE) {
                // Like hops: cut with an axe (the trellis) or a sickle; bees help them fruit.
                tag(BlockTags.MINEABLE_WITH_AXE).add(crop.block());
                tag(ModTags.Blocks.MINEABLE_WITH_SICKLE).add(crop.block());
                tag(BlockTags.BEE_GROWABLES).add(crop.block());
                tag(BlockTags.SWORD_EFFICIENT).add(crop.wildBlock());
                continue;
            }
            if (crop.isPerennial()) {
                // Like sweet berry bushes: bees help them grow, swords cut them quickly.
                tag(BlockTags.BEE_GROWABLES).add(crop.block());
                tag(BlockTags.SWORD_EFFICIENT).add(crop.block());
                continue;
            }
            // minecraft:crops also makes bees pollinate them (bee_growables includes it);
            // maintains_farmland keeps the farmland under them from turning back to dirt.
            tag(BlockTags.CROPS).add(crop.block());
            tag(BlockTags.MAINTAINS_FARMLAND).add(crop.block());
            if (crop.hasWild()) tag(BlockTags.SWORD_EFFICIENT).add(crop.wildBlock());
        }
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.VANILLA.get());   // like cocoa
        tag(ModTags.Blocks.MINEABLE_WITH_SICKLE).add(ModBlocks.VANILLA.get());
        tag(ModTags.Blocks.SUCCULENT_SOIL).addTags(BlockTags.SAND, BlockTags.TERRACOTTA).add(net.minecraft.world.level.block.Blocks.COARSE_DIRT);
        // Serene Seasons (optional): each crop is fertile in the seasons that match its climate.
        // Harmless without the mod; hops count as temperate.
        for (Crop crop : Crops.all()) {
            for (String season : crop.climate.seasons()) tag(seasonCrops(season)).add(crop.block());
        }
        for (String season : Climate.TEMPERATE.seasons()) tag(seasonCrops(season)).add(ModBlocks.HOPS.get());

        // Fruit trees: leaves are leaves (decay, hoes, sickles, bees pollinate the blossom); saplings are saplings.
        for (FruitTree tree : FruitTrees.all()) {
            tag(BlockTags.LEAVES).add(tree.leaves());
            tag(BlockTags.MINEABLE_WITH_HOE).add(tree.leaves());
            tag(BlockTags.BEE_GROWABLES).add(tree.leaves());
            tag(BlockTags.SAPLINGS).add(tree.sapling());
            for (String season : tree.climate.seasons()) tag(seasonCrops(season)).add(tree.leaves(), tree.sapling());
        }
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.FERTILE_FARMLAND.get());
        // Fertile Farmland is farmland: crops grow on it, and it keeps them growing.
        for (TagKey<Block> farmland : java.util.List.of(BlockTags.SUPPORTS_VEGETATION, BlockTags.SUPPORTS_CROPS, BlockTags.GROWS_CROPS,
                BlockTags.SUPPORT_OVERRIDE_CACTUS_FLOWER)) {
            tag(farmland).add(ModBlocks.FERTILE_FARMLAND.get());
        }
        tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.THATCH.get(), ModBlocks.THATCH_STAIRS.get(), ModBlocks.THATCH_SLAB.get());
        tag(BlockTags.STAIRS).add(ModBlocks.THATCH_STAIRS.get());
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            Block block = storage.block().get();
            tag(ModTags.Blocks.storage(storage.tag())).add(block);
            tag(ModTags.Blocks.STORAGE_BLOCKS).addTag(ModTags.Blocks.storage(storage.tag()));
            tag(block instanceof StorageBlocks.Bale ? BlockTags.MINEABLE_WITH_HOE
                    : block instanceof StorageBlocks.Crate ? BlockTags.MINEABLE_WITH_AXE : BlockTags.WOOL).add(block);
        }
        tag(BlockTags.SLABS).add(ModBlocks.THATCH_SLAB.get());

        // Sickles cut plants and leaves quickly.
        tag(ModTags.Blocks.MINEABLE_WITH_SICKLE).addTags(BlockTags.SWORD_EFFICIENT, BlockTags.CROPS, BlockTags.LEAVES,
                BlockTags.REPLACEABLE_BY_TREES).add(ModBlocks.HOPS.get(), ModBlocks.THATCH.get(), ModBlocks.THATCH_STAIRS.get(),
                ModBlocks.THATCH_SLAB.get(), Blocks.HAY_BLOCK);
        // Grain crops give straw when harvested with a sickle (corn's stalks aren't straw).
        tag(ModTags.Blocks.STRAW_CROPS).add(Blocks.WHEAT);
        for (Crop crop : Crops.all()) {
            if (crop.kind == Crop.Kind.GRAIN && crop.style != Crop.Style.TALL) tag(ModTags.Blocks.STRAW_CROPS).add(crop.block());
        }
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.COMPOST_BIN.get(), ModBlocks.DRYING_RACK.get(), ModBlocks.HARVEST_FEAST.get(),
                ModBlocks.CRUSHING_TUB.get(), ModBlocks.FRUIT_PRESS.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.TRELLIS.get(), ModBlocks.HOPS.get(), ModBlocks.MALTING_TUB.get(),
                ModBlocks.FERMENTING_VAT.get(), ModBlocks.KEG.get(), ModBlocks.WINE_RACK.get(), ModBlocks.BOTTLE_SHELF.get(),
                ModBlocks.WINE_DISPLAY.get());
        ModBlocks.CASKS.values().forEach(cask -> tag(BlockTags.MINEABLE_WITH_AXE).add(cask.get()));
        ModBlocks.BAR_COUNTERS.values().forEach(block -> tag(BlockTags.MINEABLE_WITH_AXE).add(block.get()));
        ModBlocks.BAR_STOOLS.values().forEach(block -> tag(BlockTags.MINEABLE_WITH_AXE).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.MUG_RACK.get(), ModBlocks.TAVERN_SIGN.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.KILN.get(), ModBlocks.MILLSTONE.get(), ModBlocks.BREW_KETTLE.get(),
                ModBlocks.POT_STILL.get());
        tag(BlockTags.SWORD_EFFICIENT).add(ModBlocks.WILD_HOPS.get());

        // Heat under a kettle (campfires must be lit; checked in code). Farmer's Delight's stove joins if present.
        tag(ModTags.Blocks.HEAT_SOURCES).add(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.FIRE, Blocks.SOUL_FIRE,
                        Blocks.LAVA, Blocks.MAGMA_BLOCK)
                .addOptionalTag(SeedToCellar.rl("farmersdelight", "heat_sources"));
        // Cold next to a fermenting vessel.
        tag(ModTags.Blocks.COOLING).add(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
        // Farmer's Delight's Rich Soil Farmland counts as permanently fertile (GDD section 6.5).
        tag(ModTags.Blocks.ALWAYS_FERTILE).addOptional(SeedToCellar.rl("farmersdelight", "rich_soil_farmland"));
    }

    /** sereneseasons:&lt;season&gt;_crops */
    private static TagKey<Block> seasonCrops(String season) {
        return TagKey.create(Registries.BLOCK, SeedToCellar.rl("sereneseasons", season + "_crops"));
    }
}
