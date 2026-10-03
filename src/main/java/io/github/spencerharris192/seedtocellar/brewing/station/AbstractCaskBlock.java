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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Shared behaviour of casks and kegs: bucket/mug interaction, an empty-hand read-out of the
 * contents, and facing (the tap points at the player who placed it).
 */
public abstract class AbstractCaskBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CaskBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CaskBlockEntity cask)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!level.isClientSide) {
                FluidStack before = cask.serving(cask.tank().getFluid());
                int amount = cask.tank().getFluidAmount();
                boolean moved = FluidUtil.interactWithFluidHandler(player, hand, cask.handler());
                if (moved && cask.tank().getFluidAmount() < amount && player instanceof net.minecraft.server.level.ServerPlayer server
                        && io.github.spencerharris192.seedtocellar.brewing.Drinks.spirits().stream().anyMatch(d -> d.fluid().get() == before.getFluid())
                        && io.github.spencerharris192.seedtocellar.brewing.AgedStyle.years(before.getTag())
                        >= io.github.spencerharris192.seedtocellar.registry.ModTriggers.ANGELS_SHARE_YEARS) {
                    io.github.spencerharris192.seedtocellar.registry.ModTriggers.ANGELS_SHARE.trigger(server);
                }
                if (!moved && !isTapped(state) && !cask.tank().isEmpty()) {
                    player.displayClientMessage(Component.translatable("message.seedtocellar.cask.no_tap"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                FluidStack serving = cask.serving(cask.tank().getFluid());
                Component summary = serving.isEmpty() ? Component.translatable("hydrometer.seedtocellar.empty")
                        : serving.getDisplayName().copy().append(" ").append(DrinkItem.stars(BrewQuality.of(serving).stars()));
                player.displayClientMessage(summary, true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
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
