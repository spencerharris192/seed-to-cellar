package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour of casks and kegs: bucket/mug interaction, an empty-hand read-out of the
 * contents, and facing (the tap points at the player who placed it).
 */
public abstract class AbstractCaskBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    protected AbstractCaskBlock(Properties properties) {
        super(properties);
    }

    public abstract int capacity();

    public abstract boolean ages();

    /** The cask's wood, or null for a keg (which doesn't age). */
    @Nullable
    public CaskWood wood() {
        return null;
    }

    public abstract boolean isTapped(BlockState state);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CaskBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CaskBlockEntity cask)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (io.github.spencerharris192.seedtocellar.brewing.station.SyncedBlockEntity.holdsLiquidContainer(player, hand)) {
            if (!level.isClientSide()) {
                FluidStack before = cask.serving(cask.tank().getFluid());
                int amount = cask.tank().getFluidAmount();
                boolean moved = net.neoforged.neoforge.transfer.fluid.FluidUtil.interactWithFluidHandler(player, hand, pos, cask.handler(), null);
                if (moved && cask.tank().getFluidAmount() < amount && player instanceof net.minecraft.server.level.ServerPlayer server
                        && io.github.spencerharris192.seedtocellar.brewing.Drinks.spirits().stream().anyMatch(d -> d.fluid().get() == before.getFluid())
                        && io.github.spencerharris192.seedtocellar.brewing.AgedStyle.years(io.github.spencerharris192.seedtocellar.brewing.BrewData.orNull(before))
                        >= io.github.spencerharris192.seedtocellar.registry.ModTriggers.ANGELS_SHARE_YEARS) {
                    io.github.spencerharris192.seedtocellar.registry.ModTriggers.ANGELS_SHARE.get().trigger(server);
                }
                if (!moved && !isTapped(state) && !cask.tank().isEmpty()) {
                    player.sendOverlayMessage(Component.translatable("message.seedtocellar.cask.no_tap"));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            if (!level.isClientSide()) {
                FluidStack serving = cask.serving(cask.tank().getFluid());
                Component summary = serving.isEmpty() ? Component.translatable("hydrometer.seedtocellar.empty")
                        : serving.getHoverName().copy().append(" ").append(DrinkItem.stars(BrewQuality.of(serving).stars()));
                player.sendOverlayMessage(summary);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected static Direction facing(BlockState state) {
        return state.getValue(FACING);
    }
}
