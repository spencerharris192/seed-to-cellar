package io.github.spencerharris192.seedtocellar.compat.jade;

import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlock;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlock;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlock;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.AbstractCaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MillstoneBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerItem;
import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisVineBlock;
import io.github.spencerharris192.seedtocellar.food.FeastBlock;
import io.github.spencerharris192.seedtocellar.food.PieBlock;
import io.github.spencerharris192.seedtocellar.farming.ClimateCrop;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlock;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlock;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitSaplingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

/**
 * Jade integration (GDD section 22): looking at a station shows the same plain-language lines
 * as the Hydrometer (progress, temperature, quality, age). Only loaded when Jade is installed.
 */
@WailaPlugin
public class SeedToCellarJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        for (Class<? extends BlockEntity> type : List.of(
                MaltingTubBlockEntity.class, KilnBlockEntity.class, MillstoneBlockEntity.class, BrewKettleBlockEntity.class,
                FermentingVatBlockEntity.class, PreservingJarBlockEntity.class, CaskBlockEntity.class, CompostBinBlockEntity.class,
                DryingRackBlockEntity.class, CrushingTubBlockEntity.class, FruitPressBlockEntity.class, WineRackBlockEntity.class,
                io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity.class)) {
            registration.registerBlockDataProvider(StationData.INSTANCE, type);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        for (Class<? extends Block> type : List.of(
                MaltingTubBlock.class, KilnBlock.class, MillstoneBlock.class, BrewKettleBlock.class,
                FermentingVatBlock.class, PreservingJarBlock.class, AbstractCaskBlock.class, CompostBinBlock.class,
                DryingRackBlock.class, CrushingTubBlock.class, FruitPressBlock.class, WineRackBlock.class,
                io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.class)) {
            registration.registerBlockComponent(StationInfo.INSTANCE, type);
        }
        // Looking at the Pot Still's head reads its pot (the half with the block entity).
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (accessor instanceof BlockAccessor block && block.getBlock() instanceof io.github.spencerharris192.seedtocellar.distillery.PotStillBlock
                    && io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.isUpper(block.getBlockState())) {
                BlockPos lower = block.getPosition().below();
                return registration.blockAccessor().from(block).blockState(block.getLevel().getBlockState(lower))
                        .blockEntity(block.getLevel().getBlockEntity(lower)).hit(block.getHitResult().withPosition(lower)).build();
            }
            return accessor;
        });
        registration.registerBlockComponent(VineGrowth.INSTANCE, TrellisVineBlock.class);
        registration.registerBlockComponent(TableFood.INSTANCE, PieBlock.class);
        registration.registerBlockComponent(TableFood.INSTANCE, FeastBlock.class);
        registration.registerBlockComponent(TableFood.INSTANCE, io.github.spencerharris192.seedtocellar.food.LayerCakeBlock.class);
        for (Class<? extends Block> type : List.of(CropBlock.class, BushCropBlock.class, FertileFarmlandBlock.class,
                FruitLeavesBlock.class, FruitSaplingBlock.class)) {
            registration.registerBlockComponent(FarmInfo.INSTANCE, type);
        }
    }

    /** Server: gathers the station's read-out lines (the Hydrometer's). */
    public enum StationData implements StreamServerDataProvider<BlockAccessor, List<Component>> {
        INSTANCE;

        @Override
        public List<Component> streamData(BlockAccessor accessor) {
            return accessor.getBlockEntity() instanceof HydrometerReadable readable ? readable.hydrometerLines() : List.of();
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, List<Component>> streamCodec() {
            return ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list());
        }

        @Override
        public Identifier getUid() {
            return JadeIds.STATION;
        }
    }

    /** Client: shows the lines {@link StationData} sent (Jade keeps the two apart since 1.21.6). */
    public enum StationInfo implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            StationData.INSTANCE.decodeFromData(accessor).ifPresent(lines -> lines.forEach(tooltip::add));
        }

        @Override
        public Identifier getUid() {
            return JadeIds.STATION;
        }
    }

    /**
     * Crops: our climate check (✔/✘ where it grows) and the soil's fertility, for vanilla crops too;
     * growth for our bushes and herbs, which Jade doesn't know. Fertile Farmland shows its fertility.
     * Everything here is in the block states and biome the client already has.
     */
    public enum FarmInfo implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            BlockPos pos = accessor.getPosition();
            if (state.getBlock() instanceof BushCropBlock bush) {
                int age = state.getValue(BushCropBlock.AGE);
                tooltip.add(Component.translatable(age == BushCropBlock.MAX_AGE ? "hydrometer.seedtocellar.bush_ripe" : "hydrometer.seedtocellar.growth",
                        age * 100 / BushCropBlock.MAX_AGE));
                tooltip.add(HydrometerItem.climateLine(bush.climate(), accessor.getLevel(), pos));
            } else if (state.getBlock() instanceof FruitLeavesBlock) {
                tooltip.add(Component.translatable("hydrometer.seedtocellar.fruit_" + state.getValue(FruitLeavesBlock.AGE)));
                if (FruitLeavesBlock.growing(state)) tooltip.add(HydrometerItem.climateLine(((ClimateCrop) state.getBlock()).climate(), accessor.getLevel(), pos));
            } else if (state.getBlock() instanceof ClimateCrop crop) {
                tooltip.add(HydrometerItem.climateLine(crop.climate(), accessor.getLevel(), pos));
            }
            BlockState soil = state.getBlock() instanceof FertileFarmlandBlock ? state : accessor.getLevel().getBlockState(pos.below());
            if (soil.getBlock() instanceof FertileFarmlandBlock) {
                tooltip.add(Component.translatable("hydrometer.seedtocellar.fertility", soil.getValue(FertileFarmlandBlock.FERTILITY)));
            }
        }

        @Override
        public Identifier getUid() {
            return JadeIds.FARM;
        }
    }

    /** Pies and feasts on the table: how many slices or servings are left (read from the block state). */
    public enum TableFood implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            if (state.getBlock() instanceof PieBlock) {
                tooltip.add(Component.translatable("hydrometer.seedtocellar.slices_left", PieBlock.SLICES - state.getValue(PieBlock.BITES)));
            } else if (state.getBlock() instanceof io.github.spencerharris192.seedtocellar.food.LayerCakeBlock) {
                tooltip.add(Component.translatable("hydrometer.seedtocellar.slices_left",
                        io.github.spencerharris192.seedtocellar.food.LayerCakeBlock.SLICES
                                - state.getValue(io.github.spencerharris192.seedtocellar.food.LayerCakeBlock.BITES)));
            } else if (state.getBlock() instanceof FeastBlock) {
                int servings = state.getValue(FeastBlock.SERVINGS);
                tooltip.add(servings == 0 ? Component.translatable("hydrometer.seedtocellar.leftovers")
                        : Component.translatable("hydrometer.seedtocellar.servings_left", servings));
            }
        }

        @Override
        public Identifier getUid() {
            return JadeIds.TABLE_FOOD;
        }
    }

    /** Trellis vines (hops, grapes) aren't vanilla crops, so Jade needs telling how grown they are, and their climate. */
    public enum VineGrowth implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            int age = accessor.getBlockState().getValue(TrellisVineBlock.AGE);
            tooltip.add(Component.translatable(age == TrellisVineBlock.MAX_AGE ? "hydrometer.seedtocellar.vine_ripe" : "hydrometer.seedtocellar.growth",
                    age * 100 / TrellisVineBlock.MAX_AGE));
            if (accessor.getBlock() instanceof TrellisVineBlock vine) {
                tooltip.add(HydrometerItem.climateLine(vine.climate(), accessor.getLevel(), accessor.getPosition()));
            }
        }

        @Override
        public Identifier getUid() {
            return JadeIds.VINES;
        }
    }
}
