package io.github.spencerharris192.seedtocellar.food;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * The Harvest Feast (GDD section 15.2): a platter for the table with six servings. Right-click
 * with a bowl to serve yourself; the platter looks picked-over as it goes, and when the last
 * serving is gone only a bone is left, which a right-click clears away. Needs something solid under it.
 */
public class FeastBlock extends Block {
    public static final int SERVINGS_MAX = 6;
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, SERVINGS_MAX);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape FULL = box(1, 0, 1, 15, 6, 15);
    private static final VoxelShape PLATTER = box(1, 0, 1, 15, 2, 15);

    private final Supplier<? extends Item> serving;

    public FeastBlock(Properties properties, Supplier<? extends Item> serving) {
        super(properties);
        this.serving = serving;
        registerDefaultState(stateDefinition.any().setValue(SERVINGS, SERVINGS_MAX).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? PLATTER : FULL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int servings = state.getValue(SERVINGS);
        if (servings == 0) {   // leftovers: clear the platter away
            if (!level.isClientSide()) {
                level.destroyBlock(pos, false, player);
                popResource(level, pos, new ItemStack(Items.BONE));
            }
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.BOWL)) {
            if (level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.seedtocellar.feast.needs_bowl").withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (!player.getAbilities().instabuild) held.shrink(1);
            ItemStack plate = new ItemStack(serving.get());
            io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity.give(player, plate);
            level.setBlock(pos, state.setValue(SERVINGS, servings - 1), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 0.8F, 0.8F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(SERVINGS) * 2;
    }
}
