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
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
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
        if (!level.isClientSide) {
            BlockState waxed = state.setValue(WAXED, true);
            level.setBlock(pos, waxed, Block.UPDATE_ALL_IMMEDIATE);
            if (!player.getAbilities().instabuild) held.shrink(1);
            level.levelEvent(player, 3003, pos, 0);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxed));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** What an axe makes of it: the wax off, or else a stage scraped back (Forge's axe does the sound, sparks and wear). */
    @Nullable
    public static BlockState axed(BlockState state, ToolAction action) {
        if (action == ToolActions.AXE_SCRAPE && !state.getValue(WAXED) && state.getValue(STAGE) != Stage.UNAFFECTED) {
            return state.setValue(STAGE, state.getValue(STAGE).previous());
        }
        if (action == ToolActions.AXE_WAX_OFF && state.getValue(WAXED)) return state.setValue(WAXED, false);
        return null;
    }

    /** The held item is an axe with something to do here: the block lets the axe have the click. */
    public static boolean axeWorks(BlockState state, ItemStack held) {
        return held.canPerformAction(ToolActions.AXE_SCRAPE) && axed(state, ToolActions.AXE_SCRAPE) != null
                || held.canPerformAction(ToolActions.AXE_WAX_OFF) && axed(state, ToolActions.AXE_WAX_OFF) != null;
    }

    /** The other half of a two-block copper block, in step with this one. */
    public static BlockState matching(BlockState state, BlockState other) {
        return state.setValue(STAGE, other.getValue(STAGE)).setValue(WAXED, other.getValue(WAXED));
    }

    // --- on the item ----------------------------------------------------------------------------

    public static Stage stage(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        if (tag == null) return Stage.UNAFFECTED;
        for (Stage stage : Stage.values()) if (stage.getSerializedName().equals(tag.getString(STAGE.getName()))) return stage;
        return Stage.UNAFFECTED;
    }

    public static boolean waxed(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return tag != null && "true".equals(tag.getString(WAXED.getName()));
    }

    /** "Waxed Weathered Brew Kettle", in vanilla's order. */
    public static Component name(ItemStack stack, Component name) {
        Stage stage = stage(stack);
        if (stage != Stage.UNAFFECTED) name = Component.translatable("block.seedtocellar.weathering." + stage.getSerializedName(), name);
        return waxed(stack) ? Component.translatable("block.seedtocellar.waxed", name) : name;
    }
}
