package io.github.spencerharris192.seedtocellar.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Copper that weathers (GDD section 17.3): the Brew Kettle and Pot Still turn green over time through vanilla copper's
 * four stages, at a lone copper block's pace (about 25 minutes a stage). Purely a look: they work the same at every
 * stage. Honeycomb waxes one so it keeps its look; an axe takes the wax off, or else scrapes back a stage. Broken, it
 * keeps its stage and wax on the item.
 */
public final class CopperWeathering {
    public enum Stage implements StringRepresentable {
        UNAFFECTED, EXPOSED, WEATHERED, OXIDIZED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public Stage next() {
            return this == OXIDIZED ? this : values()[ordinal() + 1];
        }

        public Stage previous() {
            return this == UNAFFECTED ? this : values()[ordinal() - 1];
        }
    }

    public static final EnumProperty<Stage> STAGE = EnumProperty.create("weathering", Stage.class);
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
    /** Vanilla's odds for a copper block with no other copper near it: a chance per random tick, less from fresh copper. */
    private static final float TICK_CHANCE = 0.05688889F, FRESH_CHANCE = 0.75F;

    private CopperWeathering() {
    }

    /** Still turning: not waxed, not fully green yet. */
    public static boolean weathers(BlockState state) {
        return !state.getValue(WAXED) && state.getValue(STAGE) != Stage.OXIDIZED;
    }

    public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!weathers(state) || random.nextFloat() >= TICK_CHANCE) return;
        Stage stage = state.getValue(STAGE);
        if (random.nextFloat() < (stage == Stage.UNAFFECTED ? FRESH_CHANCE : 1F)) {
            level.setBlockAndUpdate(pos, state.setValue(STAGE, stage.next()));
        }
    }

    /** Honeycomb on an unwaxed one waxes it (vanilla's sparkle and sound); null if the held item isn't honeycomb or it's waxed. */
    @Nullable
    public static InteractionResult wax(BlockState state, Level level, BlockPos pos, Player player, ItemStack held) {
        if (!held.is(Items.HONEYCOMB) || state.getValue(WAXED)) return null;
        if (!level.isClientSide()) {
            BlockState waxed = state.setValue(WAXED, true);
            level.setBlock(pos, waxed, Block.UPDATE_ALL_IMMEDIATE);
            if (!player.getAbilities().instabuild) held.shrink(1);
            level.levelEvent(player, 3003, pos, 0);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxed));
        }
        return InteractionResult.SUCCESS;
    }

    /** What an axe makes of it: the wax off, or else a stage scraped back; null if there's nothing to scrape. */
    @Nullable
    public static BlockState axed(BlockState state) {
        if (state.getValue(WAXED)) return state.setValue(WAXED, false);
        if (state.getValue(STAGE) != Stage.UNAFFECTED) return state.setValue(STAGE, state.getValue(STAGE).previous());
        return null;
    }

    /**
     * An axe on it scrapes the wax off, or else a stage of patina, as on vanilla copper: the sound, the sparks and a point
     * of wear on the axe. Null if the held item isn't an axe or there's nothing to scrape.
     */
    @Nullable
    public static InteractionResult axe(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held) {
        if (!held.is(ItemTags.AXES)) return null;
        BlockState result = axed(state);
        if (result == null) return null;
        if (!level.isClientSide()) {
            boolean waxOff = state.getValue(WAXED);
            level.setBlock(pos, result, Block.UPDATE_ALL_IMMEDIATE);
            if (waxOff) level.playSound(null, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1F, 1F);
            else level.playSound(null, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1F, 1F);
            level.levelEvent(player, waxOff ? LevelEvent.PARTICLES_WAX_OFF : LevelEvent.PARTICLES_SCRAPE, pos, 0);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, result));
            held.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    /** The other half of a two-block copper block, in step with this one. */
    public static BlockState matching(BlockState state, BlockState other) {
        return state.setValue(STAGE, other.getValue(STAGE)).setValue(WAXED, other.getValue(WAXED));
    }

    // --- on the item ----------------------------------------------------------------------------

    public static Stage stage(ItemStack stack) {
        Stage stage = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(STAGE);
        return stage != null ? stage : Stage.UNAFFECTED;
    }

    public static boolean waxed(ItemStack stack) {
        return Boolean.TRUE.equals(stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(WAXED));
    }

    /** "Waxed Weathered Brew Kettle", in vanilla's order. */
    public static Component name(ItemStack stack, Component name) {
        Stage stage = stage(stack);
        if (stage != Stage.UNAFFECTED) name = Component.translatable("block.seedtocellar.weathering." + stage.getSerializedName(), name);
        return waxed(stack) ? Component.translatable("block.seedtocellar.waxed", name) : name;
    }
}
