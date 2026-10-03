package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Sickle (GDD section 6.6), six tiers: right-click a crop to harvest every ripe plant in the 3x3
 * around it and replant them. Unripe plants are left alone; Fortune gives more; grain harvested
 * with a sickle also gives Straw (see {@link StrawModifier}). One point of durability per swing.
 * Being a digging tool, it also cuts plants and leaves quickly and takes Fortune, Efficiency,
 * Unbreaking and Mending.
 */
public class SickleItem extends DiggerItem {
    public static final int RADIUS = 1;

    public SickleItem(Tier tier, Properties properties) {
        super(1.5F, -2.2F, tier, ModTags.Blocks.MINEABLE_WITH_SICKLE, properties);
    }

    /** Runs before the crop's own right-click, so the sickle's area harvest wins over picking one plant. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;
        BlockPos center = context.getClickedPos();
        if (!Harvesting.isPlant(level.getBlockState(center))) center = center.above();   // aimed at the soil
        List<BlockPos> ripe = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, 0, -RADIUS), center.offset(RADIUS, 0, RADIUS))) {
            if (Harvesting.isRipe(level.getBlockState(pos))) ripe.add(pos.immutable());
        }
        if (ripe.isEmpty()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            for (BlockPos pos : ripe) Harvesting.harvest(server, pos, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.2F);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
