package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import io.github.spencerharris192.seedtocellar.world.ConfigAddFeaturesModifier;
import io.github.spencerharris192.seedtocellar.world.WildVanillaFeature;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BlockStateProviders;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Wild plant generation. Each plant = a feature (the block), a placement (how often, and the patch: so many tries around a
 * spot), and a biome modifier (which biomes, with a config toggle). Tuning lives in {@link #WILD_PLANTS}.
 * Fruit trees: each tree's feature (what its sapling grows into), rare single wild trees, and wild
 * orchards (a few mixed trees together) in temperate and warm biomes.
 */
public final class ModWorldGen {
    /** name, what to place, biome tag, patch tries, one patch per N chunks on average, grows in shallow water. */
    private record WildPlant(String name, Supplier<BlockState> state, TagKey<Biome> biomes, int tries, int rarity, boolean water) {
        ResourceKey<Feature> configured() {
            return ResourceKey.create(Registries.FEATURE, SeedToCellar.id(name));
        }

        ResourceKey<PlacedFeature> placed() {
            return ResourceKey.create(Registries.PLACED_FEATURE, SeedToCellar.id(name));
        }
    }

    /** Every crop's wild plant (tuning lives in {@link Crops}), plus wild hops. */
    private static final List<WildPlant> WILD_PLANTS = wildPlants();

    private static List<WildPlant> wildPlants() {
        List<WildPlant> plants = new ArrayList<>();
        for (Crop crop : Crops.all()) {
            if (crop.growsWild()) {
                plants.add(new WildPlant("wild_" + crop.name, crop::wildState, crop.wildBiomes(), crop.wild.tries(), crop.wild.rarity(),
                        crop.style == Crop.Style.PADDY));
            }
        }
        plants.add(new WildPlant("wild_hops", () -> ModBlocks.WILD_HOPS.get().defaultBlockState(), ModTags.Biomes.HAS_WILD_HOPS, 12, 10, false));
        return plants;
    }

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.FEATURE, ModWorldGen::configured)
            .add(Registries.PLACED_FEATURE, ModWorldGen::placed)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::biomeModifiers)
            .add(Registries.VILLAGER_TRADE, ModTrades::trades)
            .add(Registries.TRADE_SET, ModTrades::tradeSets);

    /** A patch: this many tries scattered around the spot (as vanilla's patches, 6 across and 2 up or down). */
    private static List<PlacementModifier> patch(int tries, BlockPredicate where) {
        return List.of(CountPlacement.of(tries), OffsetPlacement.ofTriangle(6, 2), BlockPredicateFilter.forPredicate(where));
    }

    /** One wild fruit tree per this many chunks on average, where it grows; one orchard per ORCHARD_RARITY. */
    private static final int TREE_RARITY = 24, ORCHARD_RARITY = 40;

    private static ResourceKey<Feature> orchardConfigured(FruitTree.Orchard orchard) {
        return ResourceKey.create(Registries.FEATURE, SeedToCellar.id(orchardName(orchard)));
    }

    private static ResourceKey<PlacedFeature> orchardPlaced(FruitTree.Orchard orchard) {
        return ResourceKey.create(Registries.PLACED_FEATURE, SeedToCellar.id(orchardName(orchard)));
    }

    private static String orchardName(FruitTree.Orchard orchard) {
        return "wild_orchard_" + orchard.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static ResourceKey<PlacedFeature> wildTreePlaced(FruitTree tree) {
        return ResourceKey.create(Registries.PLACED_FEATURE, SeedToCellar.id("wild_" + tree.name + "_tree"));
    }

    /**
     * A small orchard tree of its own vanilla log with our fruiting leaves (a few already in blossom or ripe).
     * Most are round like a small oak; citrus are a little smaller; olives grow gnarled like acacias.
     */
    private static TreeFeature tree(FruitTree tree, Holder<BlockStateProvider> soil) {
        BlockState leaves = tree.leaves().defaultBlockState();
        BlockStateProvider foliage = new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(leaves, 10)
                .add(leaves.setValue(FruitLeavesBlock.AGE, FruitLeavesBlock.BLOSSOM), 3)
                .add(leaves.setValue(FruitLeavesBlock.AGE, FruitLeavesBlock.UNRIPE), 2)
                .add(leaves.setValue(FruitLeavesBlock.AGE, FruitLeavesBlock.RIPE), 3));
        BlockStateProvider log = BlockStateProvider.of(tree.log.get());
        TreeFeature.Builder builder = switch (tree.name) {
            // olive: small and tidy, a short straight trunk under a low, rounded crown two layers deep (a forking trunk
            // grew large and lopsided; acacia's flat canopy made a wide disc)
            case "olive" -> new TreeFeature.Builder(log, new StraightTrunkPlacer(3, 1, 0), foliage,
                    new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2), new TwoLayersFeatureSize(1, 0, 1), soil);
            case "lemon", "orange" -> new TreeFeature.Builder(log, new StraightTrunkPlacer(3, 1, 0), foliage,
                    new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1), soil);
            default -> new TreeFeature.Builder(log, new StraightTrunkPlacer(4, 1, 0), foliage,
                    new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1), soil);
        };
        return builder.ignoreVines().build();
    }

    private static final ResourceKey<Feature> WILD_VANILLA_CONFIGURED = ResourceKey.create(Registries.FEATURE, SeedToCellar.id("wild_vanilla"));
    private static final ResourceKey<PlacedFeature> WILD_VANILLA_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, SeedToCellar.id("wild_vanilla"));

    private static void configured(BootstrapContext<Feature> context) {
        HolderGetter<Feature> configured = context.lookup(Registries.FEATURE);
        Holder<BlockStateProvider> soil = context.lookup(Registries.BLOCK_STATE_PROVIDER).getOrThrow(BlockStateProviders.SOIL_BENEATH_TREE);
        context.register(WILD_VANILLA_CONFIGURED, new WildVanillaFeature());
        for (FruitTree tree : FruitTrees.all()) {
            context.register(tree.treeFeature(), tree(tree, soil));
        }
        // Orchards: a random tree of the group (if a sapling could grow there), tried at several nearby spots (the placement).
        for (FruitTree.Orchard orchard : FruitTree.Orchard.values()) {
            List<Holder<PlacedFeature>> choices = FruitTrees.all().stream().filter(t -> t.orchard == orchard)
                    .map(t -> PlacementUtils.inlinePlaced(configured.getOrThrow(t.treeFeature()), PlacementUtils.filteredByBlockSurvival(t.sapling())))
                    .toList();
            List<WeightedPlacedFeature> weighted = choices.subList(0, choices.size() - 1).stream()
                    .map(c -> new WeightedPlacedFeature(c, 1.0F / choices.size())).toList();
            context.register(orchardConfigured(orchard), new RandomSelectorFeature(weighted, choices.get(choices.size() - 1)));
        }
        for (WildPlant plant : WILD_PLANTS) {
            context.register(plant.configured(), new SimpleBlockFeature(Holder.direct(BlockStateProvider.of(plant.state().get())), false));
        }
    }

    private static void placed(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> configured = context.lookup(Registries.FEATURE);
        // A few times a chunk in jungles, from the ground up the nearby trunks.
        PlacementUtils.register(context, WILD_VANILLA_PLACED, configured.getOrThrow(WILD_VANILLA_CONFIGURED),
                CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
        for (FruitTree tree : FruitTrees.all()) {
            PlacementUtils.register(context, wildTreePlaced(tree), configured.getOrThrow(tree.treeFeature()),
                    RarityFilter.onAverageOnceEvery(TREE_RARITY), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(), PlacementUtils.filteredByBlockSurvival(tree.sapling()));
        }
        for (FruitTree.Orchard orchard : FruitTree.Orchard.values()) {
            List<PlacementModifier> modifiers = new ArrayList<>(List.of(RarityFilter.onAverageOnceEvery(ORCHARD_RARITY), InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));
            modifiers.addAll(patch(10, BlockPredicate.alwaysTrue()));   // each tree checks where it grows itself
            PlacementUtils.register(context, orchardPlaced(orchard), configured.getOrThrow(orchardConfigured(orchard)), modifiers);
        }
        for (WildPlant plant : WILD_PLANTS) {
            // Water plants start from the top of the ground under the water, not the water's surface, and go where there's
            // water with open air above (one block deep).
            BlockPredicate where = plant.water()
                    ? BlockPredicate.allOf(BlockPredicate.matchesFluids(Fluids.WATER), BlockPredicate.matchesBlocks(new Vec3i(0, 1, 0), List.of(Blocks.AIR)))
                    : BlockPredicate.ONLY_IN_AIR_PREDICATE;
            List<PlacementModifier> modifiers = new ArrayList<>(List.of(RarityFilter.onAverageOnceEvery(plant.rarity()), InSquarePlacement.spread(),
                    plant.water() ? PlacementUtils.HEIGHTMAP_TOP_SOLID : PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));
            modifiers.addAll(patch(plant.tries(), where));   // the feature then checks the plant can grow there
            PlacementUtils.register(context, plant.placed(), configured.getOrThrow(plant.configured()), modifiers);
        }
    }

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (FruitTree tree : FruitTrees.all()) {
            context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SeedToCellar.id("wild_" + tree.name + "_tree")),
                    new ConfigAddFeaturesModifier(biomes.getOrThrow(tree.wildBiomes()), HolderSet.direct(placed.getOrThrow(wildTreePlaced(tree))),
                            GenerationStep.Decoration.VEGETAL_DECORATION, tree.name + "_tree"));
        }
        for (FruitTree.Orchard orchard : FruitTree.Orchard.values()) {
            TagKey<Biome> where = orchard == FruitTree.Orchard.TEMPERATE ? ModTags.Biomes.HAS_ORCHARD_TEMPERATE : ModTags.Biomes.HAS_ORCHARD_WARM;
            context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SeedToCellar.id(orchardName(orchard))),
                    new ConfigAddFeaturesModifier(biomes.getOrThrow(where), HolderSet.direct(placed.getOrThrow(orchardPlaced(orchard))),
                            GenerationStep.Decoration.VEGETAL_DECORATION, "wild_orchards"));
        }
        context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SeedToCellar.id("wild_vanilla")),
                new ConfigAddFeaturesModifier(biomes.getOrThrow(ModTags.Biomes.HAS_WILD_VANILLA), HolderSet.direct(placed.getOrThrow(WILD_VANILLA_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION, "wild_vanilla"));
        for (WildPlant plant : WILD_PLANTS) {
            context.register(ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SeedToCellar.id(plant.name())),
                    new ConfigAddFeaturesModifier(biomes.getOrThrow(plant.biomes()), HolderSet.direct(placed.getOrThrow(plant.placed())),
                            GenerationStep.Decoration.VEGETAL_DECORATION, plant.name()));
        }
    }

    private ModWorldGen() {}
}
