package io.github.spencerharris192.seedtocellar.decor;

import io.github.spencerharris192.seedtocellar.brewing.CaskItem;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;

/**
 * A Tavern Sign (GDD section 17.3): a painted board hanging from an iron bracket that sticks out from a wall, its
 * picture on both sides for passers-by. Right-click it with a mug of anything to paint a foaming mug (an alehouse), a
 * wine bottle (a wine bar), a spirit bottle (a distillery) or a cask (a cellar).
 */
public class TavernSignBlock extends WallDecorBlock {
    public enum Emblem implements StringRepresentable {
        ALE, WINE, SPIRITS, CASK;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final EnumProperty<Emblem> EMBLEM = EnumProperty.create("emblem", Emblem.class);

    public TavernSignBlock(Properties properties) {
        super(properties, box(6.5, 1, 0, 9.5, 16, 16));
        registerDefaultState(defaultBlockState().setValue(EMBLEM, Emblem.ALE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EMBLEM);
    }

    /** The picture an item calls for, or null. */
    public static Emblem emblemFor(ItemStack stack) {
        if (stack.getItem() instanceof CaskItem) return Emblem.CASK;
        if (!(stack.getItem() instanceof DrinkItem drink)) return null;
        return switch (drink.vessel()) {
            case MUG -> Emblem.ALE;
            case WINE_BOTTLE -> Emblem.WINE;
            case SPIRIT_BOTTLE -> Emblem.SPIRITS;
            case GLASS_BOTTLE -> null;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Emblem emblem = emblemFor(player.getItemInHand(hand));
        if (emblem == null) return InteractionResult.PASS;
        // Already painted so: nothing to do (rather than drinking the mug or placing the cask beside the sign).
        if (emblem == state.getValue(EMBLEM)) return InteractionResult.sidedSuccess(level.isClientSide);
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(EMBLEM, emblem), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1F, 1F);   // a fresh coat of paint
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
