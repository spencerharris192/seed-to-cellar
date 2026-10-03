package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Where wild plants grow. Using shared biome tags means modded biomes (BOP, Terralith, ...) join in. */
public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, SeedToCellar.MOD_ID, files);
    }

    private final Set<Crop> done = new HashSet<>();

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        wild(Crops.BARLEY).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW).addTag(Tags.Biomes.IS_PLAINS);
        wild(Crops.RYE).addTag(BiomeTags.IS_TAIGA).add(Biomes.SNOWY_PLAINS);
        wild(Crops.OATS).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW).addTag(Tags.Biomes.IS_PLAINS);
        // (badlands only where there's grass or dirt: wild plants can't grow on sand or terracotta)
        wild(Crops.SORGHUM).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_BADLANDS);
        wild(Crops.CORN).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS).addTag(Tags.Biomes.IS_PLAINS).addTag(BiomeTags.IS_SAVANNA);
        // (only where the water is one block deep: river edges, swamp shallows)
        wild(Crops.RICE).addTag(BiomeTags.IS_RIVER).addTag(Tags.Biomes.IS_SWAMP);
        wild(Crops.SUGAR_BEET).addTag(BiomeTags.IS_BEACH).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
        wild(Crops.ONION).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW);
        wild(Crops.GARLIC).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS).addTag(BiomeTags.IS_HILL);
        wild(Crops.CABBAGE).addTag(BiomeTags.IS_BEACH).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
        wild(Crops.GINGER).addTag(BiomeTags.IS_JUNGLE);
        wild(Crops.CORIANDER).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS).addTag(BiomeTags.IS_SAVANNA);
        wild(Crops.ANISE).addTag(BiomeTags.IS_SAVANNA).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
        // Bushes and herbs generate as themselves, ripe (like sweet berry bushes).
        wild(Crops.BLUEBERRY).addTag(BiomeTags.IS_TAIGA).add(Biomes.GROVE);
        wild(Crops.BLACKBERRY).addTag(BiomeTags.IS_FOREST);
        wild(Crops.ELDERBERRY).add(Biomes.MEADOW).addTag(BiomeTags.IS_FOREST);
        wild(Crops.JUNIPER).addTag(BiomeTags.IS_TAIGA).add(Biomes.SNOWY_SLOPES).addTag(BiomeTags.IS_HILL);
        wild(Crops.CRANBERRY).addTag(Tags.Biomes.IS_SWAMP);   // only right beside water
        wild(Crops.TOMATO).addTag(BiomeTags.IS_SAVANNA).add(Biomes.SPARSE_JUNGLE);
        wild(Crops.CHILI).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_JUNGLE);
        wild(Crops.CUCUMBER).addTag(BiomeTags.IS_JUNGLE);
        wild(Crops.COFFEE).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_HILL);
        // Wild grapes sprawl at forest edges and in open country.
        wild(Crops.RED_GRAPE).addTag(BiomeTags.IS_FOREST).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS).addTag(BiomeTags.IS_SAVANNA);
        wild(Crops.WHITE_GRAPE).addTag(BiomeTags.IS_FOREST).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW);
        wild(Crops.MINT).addTag(BiomeTags.IS_RIVER).addTag(Tags.Biomes.IS_SWAMP);
        wild(Crops.LAVENDER).add(Biomes.MEADOW, Biomes.FLOWER_FOREST, Biomes.CHERRY_GROVE);
        wild(Crops.WORMWOOD).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_HILL);
        tag(ModTags.Biomes.HAS_WILD_HOPS).addTag(BiomeTags.IS_FOREST);
        // Agave grows wild as itself, in flower, on desert sand and badlands terracotta.
        wild(Crops.AGAVE).addTag(Tags.Biomes.IS_DESERT).addTag(BiomeTags.IS_BADLANDS);
        tag(ModTags.Biomes.HAS_WILD_VANILLA).addTag(BiomeTags.IS_JUNGLE);

        // Fruit trees: rare single trees where each grows wild, and mixed orchards.
        tree(FruitTrees.APPLE).addTag(BiomeTags.IS_FOREST).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS);
        tree(FruitTrees.CHERRY).add(Biomes.CHERRY_GROVE);
        tree(FruitTrees.PLUM).addTag(BiomeTags.IS_FOREST).add(Biomes.MEADOW);
        tree(FruitTrees.PEACH).addTag(BiomeTags.IS_SAVANNA);
        tree(FruitTrees.PEAR).add(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST);
        tree(FruitTrees.LEMON).addTag(BiomeTags.IS_JUNGLE);
        tree(FruitTrees.ORANGE).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_SAVANNA);
        tree(FruitTrees.OLIVE).add(Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA);
        tag(ModTags.Biomes.HAS_ORCHARD_TEMPERATE).addTag(BiomeTags.IS_FOREST).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW);
        tag(ModTags.Biomes.HAS_ORCHARD_WARM).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_JUNGLE);
        for (FruitTree tree : FruitTrees.all()) {
            if (!trees.contains(tree)) throw new IllegalStateException("No biomes listed for wild " + tree.name + " trees in ModBiomeTagsProvider");
        }

        for (Crop crop : Crops.all()) {
            if (crop.growsWild() && !done.contains(crop)) {
                throw new IllegalStateException("No biomes listed for wild " + crop.name + " in ModBiomeTagsProvider");
            }
        }
    }

    private final Set<FruitTree> trees = new HashSet<>();

    private TagAppender<Biome> tree(FruitTree tree) {
        trees.add(tree);
        return tag(tree.wildBiomes());
    }

    private TagAppender<Biome> wild(Crop crop) {
        done.add(crop);
        return tag(crop.wildBiomes());
    }
}
