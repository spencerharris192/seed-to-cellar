package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

/**
 * Fertile Farmland (GDD section 6.5): farmland enriched with Compost, holding 1-3 fertility.
 * <ul>
 *   <li>Any crop on it, vanilla ones included, grows about 50% faster: each random tick of the
 *       soil has an even chance of also growing the crop above.</li>
 *   <li>A ripe harvest has a 50% chance of one extra crop, and a 1-in-3 chance of using up a
 *       point of fertility (see {@link FertileHarvestModifier}); at none left it's ordinary
 *       farmland again. One Compost lasts about 9 harvests.</li>
 *   <li>Otherwise it's farmland: it needs water, dries out, and turns to dirt if trampled.</li>
 * </ul>
 */
public class FertileFarmlandBlock extends FarmBlock {
    public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 1, 3);
    public static final int MAX_FERTILITY = 3;

    public FertileFarmlandBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FERTILITY, MAX_FERTILITY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FERTILITY);
    }

    /** Crops (vanilla's too) accept it like farmland: Forge asks the soil, and vanilla crops only know FARMLAND. */
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        PlantType type = plantable.getPlantType(level, pos.relative(facing));
        return type == PlantType.CROP || type == PlantType.PLAINS || super.canSustainPlant(state, level, pos, facing, plantable);
    }

    /** Wet like watered farmland, so crops count it when working out their growth speed. */
    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);   // moisture, drying out
        if (!level.getBlockState(pos).is(this)) return;
        BlockPos above = pos.above();
        BlockState crop = level.getBlockState(above);
        if (crop.isRandomlyTicking() && random.nextDouble() < ModConfigs.COMMON.fertileGrowthBonus.get()) crop.randomTick(level, above, random);
    }

    /** Uses up a point of fertility; with none left it's ordinary farmland again, still as wet. */
    public static void useFertility(Level level, BlockPos pos, BlockState state) {
        int fertility = state.getValue(FERTILITY);
        level.setBlock(pos, fertility > 1 ? state.setValue(FERTILITY, fertility - 1)
                : Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, state.getValue(MOISTURE)), Block.UPDATE_ALL);
    }

    /**
     * Stillage on farmland: {@code points} of fertility, up to full (plain farmland becomes Fertile Farmland). False if
     * there's nothing to do. Effects are shown by the caller's sound; the sparkles show here.
     */
    public static boolean addFertility(Level level, BlockPos pos, int points) {
        BlockState state = level.getBlockState(pos);
        BlockState result;
        if (state.is(Blocks.FARMLAND)) {
            result = io.github.spencerharris192.seedtocellar.registry.ModBlocks.FERTILE_FARMLAND.get().defaultBlockState()
                    .setValue(FERTILITY, Math.min(MAX_FERTILITY, points)).setValue(MOISTURE, state.getValue(MOISTURE));
        } else if (state.getBlock() instanceof FertileFarmlandBlock && state.getValue(FERTILITY) < MAX_FERTILITY) {
            result = state.setValue(FERTILITY, Math.min(MAX_FERTILITY, state.getValue(FERTILITY) + points));
        } else {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, result, Block.UPDATE_ALL);
            level.levelEvent(1505, pos, 0);
        }
        return true;
    }

    /**
     * Compost on farmland: makes it Fertile Farmland (full fertility), or tops fertile farmland
     * back up. False if there's nothing to do.
     */
    public static boolean fertilize(Level level, BlockPos pos, Block fertile) {
        BlockState state = level.getBlockState(pos);
        BlockState result;
        if (state.is(Blocks.FARMLAND)) {
            result = fertile.defaultBlockState().setValue(MOISTURE, state.getValue(MOISTURE));
        } else if (state.getBlock() instanceof FertileFarmlandBlock && state.getValue(FERTILITY) < MAX_FERTILITY) {
            result = state.setValue(FERTILITY, MAX_FERTILITY);
        } else {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, result, Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(1505, pos, 0);   // the green growth sparkles, as bone meal shows
        }
        return true;
    }
}
