package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.ClimateRules;
import io.github.spencerharris192.seedtocellar.farming.FertileFarmlandBlock;
import io.github.spencerharris192.seedtocellar.farming.ClimateCrop;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitSaplingBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisVineBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Hydrometer (GDD section 7): right-click a station, cask or crop for a plain-language read-out
 * (progress, temperature, quality, age; for crops, growth, climate and soil fertility). Works
 * in packs without Jade or The One Probe.
 */
public class HydrometerItem extends Item {
    public HydrometerItem(Properties properties) {
        super(properties);
    }

    /**
     * Reads before the block's own right-click does anything (Forge's first-use hook), so a Brew Kettle, vat, kiln or still
     * gives its read-out rather than opening its screen. Anything it can't read is left to the block as usual.
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        List<Component> lines = readout(level, pos, state);
        if (lines.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            player.displayClientMessage(state.getBlock().getName().copy().withStyle(ChatFormatting.GOLD), false);
            for (Component line : lines) player.displayClientMessage(Component.literal("  ").append(line), false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * What the Hydrometer reads off a block: a station's or cask's progress, temperature, quality and age; a crop's growth,
     * climate and soil. Empty for anything it can't read. The One Probe shows the same lines.
     */
    public static List<Component> readout(Level level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(state.getBlock() instanceof io.github.spencerharris192.seedtocellar.distillery.PotStillBlock
                ? io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.lowerPos(pos, state) : pos);
        List<Component> lines = new ArrayList<>();
        if (be instanceof HydrometerReadable readable) {
            lines.addAll(readable.hydrometerLines());
        } else if (state.getBlock() instanceof CropBlock crop) {
            lines.add(Component.translatable("hydrometer.seedtocellar.growth", crop.getAge(state) * 100 / crop.getMaxAge()));
            if (crop instanceof ClimateCrop ours) lines.add(climateLine(ours.climate(), level, pos));
            BlockState soil = level.getBlockState(pos.below());
            if (soil.getBlock() instanceof FertileFarmlandBlock) {
                lines.add(Component.translatable("hydrometer.seedtocellar.fertility", soil.getValue(FertileFarmlandBlock.FERTILITY)));
            }
        } else if (state.getBlock() instanceof BushCropBlock bush) {
            lines.add(Component.translatable("hydrometer.seedtocellar.growth", state.getValue(BushCropBlock.AGE) * 100 / BushCropBlock.MAX_AGE));
            lines.add(climateLine(bush.climate(), level, pos));
        } else if (state.getBlock() instanceof FruitLeavesBlock leaves) {
            lines.add(Component.translatable("hydrometer.seedtocellar.fruit_" + state.getValue(FruitLeavesBlock.AGE)));
            if (!FruitLeavesBlock.growing(state)) lines.add(Component.translatable("hydrometer.seedtocellar.fruit_placed"));
            lines.add(climateLine(leaves.climate(), level, pos));
        } else if (state.getBlock() instanceof FruitSaplingBlock sapling) {
            lines.add(climateLine(sapling.climate(), level, pos));
        } else if (state.getBlock() instanceof io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock vanilla) {
            int age = state.getValue(io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.AGE);
            lines.add(Component.translatable(age == io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.MAX_AGE
                    ? "hydrometer.seedtocellar.vanilla_ripe" : "hydrometer.seedtocellar.growth", age * 100 / 3));
            lines.add(climateLine(vanilla.climate(), level, pos));
        } else if (state.getBlock() instanceof TrellisVineBlock vine) {
            lines.add(Component.translatable("hydrometer.seedtocellar.growth", state.getValue(TrellisVineBlock.AGE) * 100 / TrellisVineBlock.MAX_AGE));
            lines.add(climateLine(vine.climate(), level, pos));
        }
        return lines;
    }

    /** "Climate: Cold ✔" (green) or ✘ (yellow) where the crop is growing. Also used by the Jade plugin. */
    public static Component climateLine(Climate preferred, Level level, BlockPos pos) {
        boolean ok = ClimateRules.suits(preferred, level, pos);
        return Component.translatable("hydrometer.seedtocellar.climate", Component.translatable(preferred.translationKey()), ok ? "✔" : "✘")
                .withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
    }
}
