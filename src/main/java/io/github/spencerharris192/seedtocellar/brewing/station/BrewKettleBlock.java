package io.github.spencerharris192.seedtocellar.brewing.station;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import io.github.spencerharris192.seedtocellar.decor.CopperWeathering;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Brew Kettle (GDD section 7): a copper kettle that sits over heat (a lit campfire, fire,
 * magma, lava, or any block in seedtocellar:heat_sources). Mashes grist into sweet wort,
 * then boils it with hops. Right-click with a bucket to fill/empty; otherwise opens the screen. Its copper weathers
 * ({@link CopperWeathering}).
 */
public class BrewKettleBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Shapes.join(box(1, 0, 1, 15, 12, 15), box(2, 2, 2, 14, 12, 14), BooleanOp.ONLY_FIRST);

    public BrewKettleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false)
                .setValue(CopperWeathering.STAGE, CopperWeathering.Stage.UNAFFECTED).setValue(CopperWeathering.WAXED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, CopperWeathering.STAGE, CopperWeathering.WAXED);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return CopperWeathering.weathers(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        CopperWeathering.randomTick(state, level, pos, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewKettleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.BREW_KETTLE.get(), BrewKettleBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BrewKettleBlockEntity kettle)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        InteractionResult waxed = CopperWeathering.wax(state, level, pos, player, held);
        if (waxed != null) return waxed;
        InteractionResult axed = CopperWeathering.axe(state, level, pos, player, hand, held);
        if (axed != null) return axed;
        if (SyncedBlockEntity.holdsLiquidContainer(player, hand)) {
            if (!level.isClientSide()) net.neoforged.neoforge.transfer.fluid.FluidUtil.interactWithFluidHandler(player, hand, pos, kettle.tank(), null);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(kettle, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
        double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        level.addParticle(ParticleTypes.CLOUD, x, pos.getY() + 0.8, z, 0, 0.03, 0);
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.BUBBLE_POP, x, pos.getY() + 0.7, z, 0, 0.02, 0);
        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    SoundSource.BLOCKS, 0.5F, 1.2F, false);
        }
    }
}
