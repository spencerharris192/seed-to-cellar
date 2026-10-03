package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/**
 * A row crop (GDD growth style A): 8 growth ages on farmland like wheat, random-tick growth,
 * bone meal and Forge's crop events (so other mods' growth boosters work). Ripe crops can be
 * harvested by right-clicking: you get the normal drops minus one seed, and it replants.
 */
public class ModCropBlock extends CropBlock implements ClimateCrop {
    private final Supplier<? extends Item> seeds;
    protected final Climate climate;

    public ModCropBlock(Properties properties, Supplier<? extends Item> seeds, Climate climate) {
        super(properties);
        this.seeds = seeds;
        this.climate = climate;
    }

    @Override
    public Climate climate() {
        return climate;
    }

    /** Vanilla's growth step, run as many times as the climate rules allow (see {@link ClimateRules}). */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int attempts = ClimateRules.growthAttempts(climate, level, pos, random);
        for (int i = 0; i < attempts && level.getBlockState(pos).is(this); i++) {
            super.randomTick(level.getBlockState(pos), level, pos, random);
        }
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seeds.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!isMaxAge(state) || !ModConfigs.COMMON.rightClickHarvest.get()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) harvest(server, pos, state, player, getStateForAge(0));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Right-click harvest shared by our crops: the block's own loot (so Fortune and loot
     * modifiers apply), minus one of what you'd replant, then the plant goes back to `replanted`.
     */
    public static void harvest(ServerLevel level, BlockPos pos, BlockState state, Player player, BlockState replanted) {
        ItemStack tool = player.getMainHandItem();
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, tool);
        Item replant = state.getBlock().getCloneItemStack(level, pos, state).getItem();
        for (ItemStack drop : drops) {
            if (drop.is(replant)) {
                drop.shrink(1);
                break;
            }
        }
        drops.forEach(drop -> popResource(level, pos, drop));
        level.setBlock(pos, replanted, Block.UPDATE_CLIENTS);
        level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, replanted));
    }
}
