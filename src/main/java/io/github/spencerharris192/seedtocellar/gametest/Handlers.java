package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.brewing.station.StationTank;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * What hoppers, pipes and other mods see of a station (its item slots and tanks), with simple fill/drain/insert/extract
 * calls for tests. Each call is its own transaction: done for real (committed) unless it's a simulation.
 */
final class Handlers {
    /** A block's tanks, as seen from {@code side} (null: from inside). */
    static Fluids fluids(BlockEntity be, @Nullable Direction side) {
        ResourceHandler<FluidResource> handler = be.getLevel().getCapability(Capabilities.Fluid.BLOCK, be.getBlockPos(), side);
        if (handler == null) throw new IllegalStateException("no fluid handler on " + be.getType() + " from " + side);
        return new Fluids(handler);
    }

    /** A block's item slots, as seen from {@code side} (null: from inside). */
    static Items items(BlockEntity be, @Nullable Direction side) {
        ResourceHandler<ItemResource> handler = be.getLevel().getCapability(Capabilities.Item.BLOCK, be.getBlockPos(), side);
        if (handler == null) throw new IllegalStateException("no item handler on " + be.getType() + " from " + side);
        return new Items(handler);
    }

    /** A copy of {@code stack} held as in a hand, if it can hold liquid (a bottle, mug or bucket). */
    static Optional<Held> fluidsOf(ItemStack stack) {
        Held held = new Held(stack.copy());
        return held.handler() != null ? Optional.of(held) : Optional.empty();
    }

    static Held fluids(ItemStack stack) {
        return fluidsOf(stack).orElseThrow(() -> new IllegalStateException(stack + " holds no liquid"));
    }

    static Fluids fluids(ResourceHandler<FluidResource> handler) {
        return new Fluids(handler);
    }

    record Fluids(ResourceHandler<FluidResource> handler) {
        /** How much of {@code stack} went in. */
        int fill(FluidStack stack, StationTank.Action action) {
            if (stack.isEmpty()) return 0;
            try (Transaction tx = Transaction.openRoot()) {
                int in = handler.insert(FluidResource.of(stack), stack.getAmount(), tx);
                if (action.execute()) tx.commit();
                return in;
            }
        }

        /** Up to {@code amount} of whatever liquid comes out first. */
        FluidStack drain(int amount, StationTank.Action action) {
            for (int i = 0; i < handler.size(); i++) {
                FluidResource resource = handler.getResource(i);
                if (resource.isEmpty()) continue;
                FluidStack out = drain(resource.toStack(amount), action);
                if (!out.isEmpty()) return out;
            }
            return FluidStack.EMPTY;
        }

        /** Up to that much of that liquid. */
        FluidStack drain(FluidStack stack, StationTank.Action action) {
            try (Transaction tx = Transaction.openRoot()) {
                FluidResource resource = FluidResource.of(stack);
                int out = handler.extract(resource, stack.getAmount(), tx);
                if (action.execute()) tx.commit();
                return out == 0 ? FluidStack.EMPTY : resource.toStack(out);
            }
        }

        int getTanks() {
            return handler.size();
        }

        FluidStack getFluidInTank(int tank) {
            return handler.getResource(tank).toStack(handler.getAmountAsInt(tank));
        }
    }

    /**
     * One item held as in a hand: filling or emptying it can turn it into another item (a glass bottle into a bottle of
     * juice), which {@link #getContainer()} then shows.
     */
    static final class Held {
        private final SimpleContainer slot = new SimpleContainer(1);
        private final ResourceHandler<ItemResource> slotHandler = VanillaContainerWrapper.of(slot);

        Held(ItemStack stack) {
            slot.setItem(0, stack);
        }

        @Nullable ResourceHandler<FluidResource> handler() {
            return getContainer().isEmpty() ? null : ItemAccess.forHandlerIndex(slotHandler, 0).getCapability(Capabilities.Fluid.ITEM);
        }

        int fill(FluidStack stack, StationTank.Action action) {
            ResourceHandler<FluidResource> handler = handler();
            return handler == null ? 0 : new Fluids(handler).fill(stack, action);
        }

        FluidStack drain(int amount, StationTank.Action action) {
            ResourceHandler<FluidResource> handler = handler();
            return handler == null ? FluidStack.EMPTY : new Fluids(handler).drain(amount, action);
        }

        ItemStack getContainer() {
            return slot.getItem(0);
        }
    }

    record Items(ResourceHandler<ItemResource> handler) {
        int getSlots() {
            return handler.size();
        }

        ItemStack getStackInSlot(int slot) {
            return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
        }

        /** What didn't fit. */
        ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            try (Transaction tx = Transaction.openRoot()) {
                int in = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
                if (!simulate) tx.commit();
                return stack.copyWithCount(stack.getCount() - in);
            }
        }

        /** What came out. */
        ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) return ItemStack.EMPTY;
            try (Transaction tx = Transaction.openRoot()) {
                int out = handler.extract(slot, resource, amount, tx);
                if (!simulate) tx.commit();
                return resource.toStack(out);
            }
        }
    }

    private Handlers() {}
}
