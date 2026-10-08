package io.github.spencerharris192.seedtocellar.datagen.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writes blockstates and block models: the same building blocks as Forge's old generator (models().withExistingParent,
 * getVariantBuilder(...).forAllStates, ConfiguredModel...), writing the game's JSON.
 */
public abstract class BlockStateProvider implements DataProvider {
    private final PackOutput output;
    private final BlockModels models = new BlockModels();
    private final Map<Block, VariantBlockStateBuilder> states = new LinkedHashMap<>();

    protected BlockStateProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void registerStatesAndModels();

    public BlockModels models() {
        return models;
    }

    public Identifier modLoc(String path) {
        return SeedToCellar.id(path);
    }

    public Identifier mcLoc(String path) {
        return SeedToCellar.rl("minecraft", path);
    }

    protected static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public VariantBlockStateBuilder getVariantBuilder(Block block) {
        return states.computeIfAbsent(block, VariantBlockStateBuilder::new);
    }

    public void simpleBlock(Block block, ModelFile model) {
        getVariantBuilder(block).forAllStatesExcept(state -> new ConfiguredModel[]{new ConfiguredModel(model)},
                block.getStateDefinition().getProperties().toArray(Property[]::new));
    }

    public void simpleBlock(Block block) {
        simpleBlock(block, models.cubeAll(name(block), modLoc("block/" + name(block))));
    }

    /** Faces the way it was placed (FACING; its model faces north). */
    public void horizontalBlock(Block block, ModelFile model) {
        horizontalBlock(block, state -> model);
    }

    public void horizontalBlock(Block block, Function<BlockState, ModelFile> model) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model.apply(state))
                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360).build());
    }

    public void axisBlock(RotatedPillarBlock block, Identifier side, Identifier end) {
        String name = name(block);
        ModelFile vertical = models.withExistingParent(name, mcLoc("block/cube_column")).texture("side", side).texture("end", end);
        ModelFile horizontal = models.withExistingParent(name + "_horizontal", mcLoc("block/cube_column_horizontal"))
                .texture("side", side).texture("end", end);
        getVariantBuilder(block).forAllStates(state -> switch (state.getValue(RotatedPillarBlock.AXIS)) {
            case Y -> ConfiguredModel.builder().modelFile(vertical).build();
            case Z -> ConfiguredModel.builder().modelFile(horizontal).rotationX(90).build();
            case X -> ConfiguredModel.builder().modelFile(horizontal).rotationX(90).rotationY(90).build();
        });
    }

    public void stairsBlock(StairBlock block, Identifier texture) {
        String name = name(block);
        ModelFile stairs = models.withExistingParent(name, mcLoc("block/stairs")).texture("side", texture).texture("bottom", texture).texture("top", texture);
        ModelFile inner = models.withExistingParent(name + "_inner", mcLoc("block/inner_stairs")).texture("side", texture).texture("bottom", texture)
                .texture("top", texture);
        ModelFile outer = models.withExistingParent(name + "_outer", mcLoc("block/outer_stairs")).texture("side", texture).texture("bottom", texture)
                .texture("top", texture);
        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction facing = state.getValue(StairBlock.FACING);
            Half half = state.getValue(StairBlock.HALF);
            StairsShape shape = state.getValue(StairBlock.SHAPE);
            int y = (int) facing.getClockWise().toYRot();   // the stairs model faces east
            if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) y += 270;
            if (shape != StairsShape.STRAIGHT && half == Half.TOP) y += 90;
            y %= 360;
            return ConfiguredModel.builder()
                    .modelFile(shape == StairsShape.STRAIGHT ? stairs : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? inner : outer)
                    .rotationX(half == Half.BOTTOM ? 0 : 180).rotationY(y).uvLock(y != 0 || half == Half.TOP).build();
        }, StairBlock.WATERLOGGED);
    }

    public void slabBlock(SlabBlock block, Identifier doubleSlab, Identifier texture) {
        String name = name(block);
        ModelFile bottom = models.withExistingParent(name, mcLoc("block/slab")).texture("side", texture).texture("bottom", texture).texture("top", texture);
        ModelFile top = models.withExistingParent(name + "_top", mcLoc("block/slab_top")).texture("side", texture).texture("bottom", texture)
                .texture("top", texture);
        ModelFile full = new ModelFile(doubleSlab);
        getVariantBuilder(block).forAllStatesExcept(state -> new ConfiguredModel[]{new ConfiguredModel(switch (state.getValue(SlabBlock.TYPE)) {
            case BOTTOM -> bottom;
            case TOP -> top;
            case DOUBLE -> full;
        })}, SlabBlock.WATERLOGGED);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        registerStatesAndModels();
        PackOutput.PathProvider modelPaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        PackOutput.PathProvider statePaths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        List<CompletableFuture<?>> futures = new ArrayList<>();
        models.all().forEach((id, model) -> futures.add(DataProvider.saveStable(cache, model.toJson(), modelPaths.json(id))));
        states.forEach((block, state) -> futures.add(DataProvider.saveStable(cache, state.toJson(),
                statePaths.json(BuiltInRegistries.BLOCK.getKey(block)))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Block states and models: " + SeedToCellar.MOD_ID;
    }

    /** The models we write (block models, and any other path given in full). */
    public final class BlockModels {
        private final Map<Identifier, BlockModelBuilder> built = new LinkedHashMap<>();

        Map<Identifier, BlockModelBuilder> all() {
            return built;
        }

        /** A name without a folder goes in block/; one with a slash is taken as the full path. */
        private Identifier path(String name) {
            if (name.contains(":")) return Identifier.parse(name);
            return SeedToCellar.id(name.contains("/") ? name : "block/" + name);
        }

        public BlockModelBuilder getBuilder(String name) {
            return built.computeIfAbsent(path(name), BlockModelBuilder::new);
        }

        public BlockModelBuilder withExistingParent(String name, Identifier parent) {
            return getBuilder(name).parent(new ModelFile(parent));
        }

        public ModelFile getExistingFile(Identifier id) {
            return new ModelFile(id);
        }

        public BlockModelBuilder cubeAll(String name, Identifier texture) {
            return withExistingParent(name, mcLoc("block/cube_all")).texture("all", texture);
        }

        public BlockModelBuilder cubeBottomTop(String name, Identifier side, Identifier bottom, Identifier top) {
            return withExistingParent(name, mcLoc("block/cube_bottom_top")).texture("side", side).texture("bottom", bottom).texture("top", top);
        }

        public BlockModelBuilder orientable(String name, Identifier side, Identifier front, Identifier top) {
            return withExistingParent(name, mcLoc("block/orientable")).texture("side", side).texture("front", front).texture("top", top);
        }

        public BlockModelBuilder orientableWithBottom(String name, Identifier side, Identifier front, Identifier bottom, Identifier top) {
            return withExistingParent(name, mcLoc("block/orientable_with_bottom")).texture("side", side).texture("front", front)
                    .texture("bottom", bottom).texture("top", top);
        }

        public BlockModelBuilder cross(String name, Identifier cross) {
            return withExistingParent(name, mcLoc("block/cross")).texture("cross", cross);
        }

        public BlockModelBuilder crop(String name, Identifier crop) {
            return withExistingParent(name, mcLoc("block/crop")).texture("crop", crop);
        }

        public BlockModelBuilder leaves(String name, Identifier texture) {
            return withExistingParent(name, mcLoc("block/leaves")).texture("all", texture);
        }
    }

    /** A block's variants: one entry (one or more models) per combination of the properties that matter. */
    public static final class VariantBlockStateBuilder {
        private final Block block;
        private final Map<String, ConfiguredModel[]> variants = new LinkedHashMap<>();

        VariantBlockStateBuilder(Block block) {
            this.block = block;
        }

        public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> models) {
            return forAllStatesExcept(models);
        }

        /** Every state, the `ignored` properties left out of the key (so they don't change the model). */
        public VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> models, Property<?>... ignored) {
            Set<Property<?>> skip = Set.of(ignored);
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                String key = state.getProperties().stream().filter(p -> !skip.contains(p))
                        .map(p -> p.getName() + "=" + valueName(state, p)).collect(Collectors.joining(","));
                if (!variants.containsKey(key)) variants.put(key, models.apply(state));
            }
            return this;
        }

        private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
            return property.getName(state.getValue(property));
        }

        JsonElement toJson() {
            JsonObject json = new JsonObject();
            JsonObject variantJson = new JsonObject();
            variants.forEach((key, models) -> {
                if (models.length == 1) {
                    variantJson.add(key, models[0].toJson());
                } else {
                    JsonArray array = new JsonArray();
                    Arrays.stream(models).map(ConfiguredModel::toJson).forEach(array::add);
                    variantJson.add(key, array);
                }
            });
            json.add("variants", variantJson);
            return json;
        }
    }
}
