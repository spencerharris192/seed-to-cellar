package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An aging cask lying on its side (GDD section 13). Holds 16 buckets. Right-click the front
 * with a Tap to fit one; then pour into mugs or buckets. The wood matters: see {@link CaskWood}.
 */
public class CaskBlock extends AbstractCaskBlock {
    public static final int CAPACITY = 16000;
    public static final BooleanProperty TAP = BooleanProperty.create("tap");
    /** Charred inside with flint and steel (GDD section 13): whiskeys age best in it; bourbon needs it. */
    public static final BooleanProperty CHARRED = BooleanProperty.create("charred");
    private static final VoxelShape SHAPE_NS = box(1, 0, 0, 15, 14, 16);
    private static final VoxelShape SHAPE_EW = box(0, 0, 1, 16, 14, 15);

    private final CaskWood wood;

    public CaskBlock(CaskWood wood, Properties properties) {
        super(properties);
        this.wood = wood;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TAP, false).setValue(CHARRED, false));
    }

    @Override
    public CaskWood wood() {
        return wood;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TAP, CHARRED);
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }

    @Override
    public boolean ages() {
        return true;
    }

    @Override
    public boolean isTapped(BlockState state) {
        return state.getValue(TAP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return facing(state).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.TAP.get()) && !state.getValue(TAP)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(TAP, true), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1F, 1.2F);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)) {   // (never lights a fire beside the cask)
            if (!level.isClientSide) {
                if (state.getValue(CHARRED)) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.seedtocellar.cask.already_charred"), true);
                } else {
                    charWith(state, level, pos, player, hand);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /** Flint and steel on an empty cask chars it inside, once. Nether wood won't take a char. */
    private void charWith(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        String msg = "message.seedtocellar.cask.";
        if (wood.nether()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(msg + "wont_char"), true);
            return;
        }
        if (level.getBlockEntity(pos) instanceof CaskBlockEntity cask && !cask.tank().isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(msg + "char_empty"), true);
            return;
        }
        level.setBlock(pos, state.setValue(CHARRED, true), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1F, 1F);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 0.8F);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    12, 0.3, 0.3, 0.3, 0.01);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    8, 0.3, 0.2, 0.3, 0.02);
        }
        player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(msg + "charred"), true);
    }

    /** Pick block on a charred cask gives a charred cask. */
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (state.getValue(CHARRED)) {
            net.minecraft.nbt.CompoundTag properties = new net.minecraft.nbt.CompoundTag();
            properties.putString(CHARRED.getName(), "true");
            stack.addTagElement(net.minecraft.world.item.BlockItem.BLOCK_STATE_TAG, properties);
        }
        return stack;
    }

    public static boolean isCharred(BlockState state) {
        return state.hasProperty(CHARRED) && state.getValue(CHARRED);
    }
}
