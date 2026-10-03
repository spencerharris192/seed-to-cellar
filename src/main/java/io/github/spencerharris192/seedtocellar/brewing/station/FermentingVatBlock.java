package io.github.spencerharris192.seedtocellar.brewing.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;

/**
 * Fermenting Vat (GDD section 7). Fill with wort (lid open), optionally add yeast, then close
 * the lid (sneak + right-click, the button on its screen, or a redstone signal) to ferment.
 * The airlock bubbles while it works.
 */
public class FermentingVatBlock extends BaseEntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty FERMENTING = BooleanProperty.create("fermenting");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 15, 15);

    public FermentingVatBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, true).setValue(FERMENTING, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FERMENTING, POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentingVatBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FermentingVatBlockEntity vat)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        boolean client = level.isClientSide;
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!client) vat.useFluidContainer(player, hand);
            return InteractionResult.sidedSuccess(client);
        }
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!client) vat.toggleLid(player);
            return InteractionResult.sidedSuccess(client);
        }
        if (!client && vat.isYeast(held) && vat.insertYeast(player, hand)) {
            return InteractionResult.SUCCESS;
        }
        if (!client && player instanceof ServerPlayer serverPlayer) NetworkHooks.openScreen(serverPlayer, vat, pos);
        return InteractionResult.sidedSuccess(client);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!(level.getBlockEntity(pos) instanceof FermentingVatBlockEntity vat)) return;
        vat.recheckTemperature();
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            if (powered) vat.setLid(false, null);
            else vat.setLid(true, null);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FermentingVatBlockEntity vat) vat.advance();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FermentingVatBlockEntity vat) {
            vat.dropContents();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FERMENTING)) return;
        // bubbles rising through the airlock on the lid
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.72, pos.getY() + 1.15, pos.getZ() + 0.72, 0, 0.02, 0);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                    SoundSource.BLOCKS, 0.4F, 1.4F, false);
        }
    }
}
