package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Lets a drink's vessel act as a 250 mB container through Forge's standard item-fluid system, so taps,
 * vats, presses and machines like Create's Spout and Item Drain can fill an empty mug, wine bottle or
 * glass bottle with the drink that belongs in it, or empty a full one, keeping quality and age. A glass bottle
 * also takes a cooking liquid that has a bottled item (olive oil: {@link BottledLiquidItem}).
 */
public class VesselFluidHandler implements IFluidHandlerItem, ICapabilityProvider {
    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
    private ItemStack container;

    public VesselFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (container.getItem() instanceof DrinkItem drink) return drink.asFluid(container);
        if (container.getItem() instanceof BottledLiquidItem bottled) return new FluidStack(bottled.fluid(), DrinkItem.SERVING);
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return DrinkItem.SERVING;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return Drinks.byFluid(stack.getFluid()).isPresent() || BottledLiquidItem.byFluid(stack.getFluid()).isPresent();
    }

    /** Fills an empty vessel with a drink served in that vessel (wine won't go in a mug). */
    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < DrinkItem.SERVING) return 0;
        Vessel vessel = Vessel.of(container).orElse(null);
        if (vessel == null) return 0;
        BottledLiquidItem bottled = BottledLiquidItem.byFluid(resource.getFluid()).orElse(null);
        if (bottled != null) {
            if (vessel != Vessel.GLASS_BOTTLE) return 0;
            if (action.execute()) container = new ItemStack(bottled);
            return DrinkItem.SERVING;
        }
        if (Drinks.byFluid(resource.getFluid()).map(d -> d.vessel() != vessel).orElse(true)) return 0;
        ItemStack filled = DrinkItem.fromFluid(resource);
        if (filled.isEmpty()) return 0;
        if (action.execute()) container = filled;
        return DrinkItem.SERVING;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack inside = getFluidInTank(0);
        return inside.isFluidEqual(resource) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < DrinkItem.SERVING) return FluidStack.EMPTY;
        if (container.getItem() instanceof BottledLiquidItem bottled) {
            if (action.execute()) container = new ItemStack(Vessel.GLASS_BOTTLE.empty());
            return new FluidStack(bottled.fluid(), DrinkItem.SERVING);
        }
        if (!(container.getItem() instanceof DrinkItem drink)) return FluidStack.EMPTY;
        FluidStack inside = drink.asFluid(container);
        if (action.execute()) container = new ItemStack(drink.vessel().empty());
        return inside;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, holder);
    }
}
