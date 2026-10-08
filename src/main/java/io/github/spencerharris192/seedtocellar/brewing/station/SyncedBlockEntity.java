package io.github.spencerharris192.seedtocellar.brewing.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

/** Block entity whose saved data is also sent to clients (for renderers showing contents). */
public abstract class SyncedBlockEntity extends BlockEntity {
    protected SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Save and push the new state to nearby players. */
    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Whether the player's hand holds something that holds liquid (a bucket, a bottle, a drink...). */
    public static boolean holdsLiquidContainer(Player player, InteractionHand hand) {
        return ItemAccess.forPlayerInteraction(player, hand).getCapability(Capabilities.Fluid.ITEM) != null;
    }

    /** Fills the held container from the tank, or empties it into the tank, like a cauldron. True if anything moved. */
    protected boolean pourWith(Player player, InteractionHand hand, ResourceHandler<FluidResource> tank) {
        return net.neoforged.neoforge.transfer.fluid.FluidUtil.interactWithFluidHandler(player, hand, worldPosition, tank, null);
    }

    /** Puts the stack in the player's inventory, dropping what doesn't fit. */
    public static void give(Player player, ItemStack stack) {
        if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
    }
}
