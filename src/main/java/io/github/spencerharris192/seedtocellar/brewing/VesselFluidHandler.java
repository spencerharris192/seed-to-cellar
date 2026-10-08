package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Lets a drink's vessel act as a 250 mB container through NeoForge's standard item-fluid system, so taps,
 * vats, presses and other mods' machines can fill an empty mug, wine bottle or glass bottle with the drink that belongs
 * in it, or empty a full one, keeping quality and age. A glass bottle also takes a cooking liquid that has a bottled
 * item (olive oil: {@link BottledLiquidItem}). Registered on the vessels, drinks and bottled liquids in
 * {@link VesselEvents}.
 */
public class VesselFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    public VesselFluidHandler(ItemAccess access) {
        super(access, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource held, int index) {
        if (held.getItem() instanceof DrinkItem drink) return FluidResource.of(drink.asFluid(held.toStack()));
        if (held.getItem() instanceof BottledLiquidItem bottled) return FluidResource.of(bottled.fluid());
        return FluidResource.EMPTY;
    }

    @Override
    protected int getAmountFrom(ItemResource held, int index) {
        return getResourceFrom(held, index).isEmpty() ? 0 : DrinkItem.SERVING;
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return DrinkItem.SERVING;
    }

    /** A drink served in this vessel (wine won't go in a mug), or a bottled liquid for a glass bottle. */
    @Override
    public boolean isValid(int index, FluidResource resource) {
        Vessel vessel = vessel(itemAccess.getResource());
        if (vessel == null) return false;
        if (BottledLiquidItem.byFluid(resource.getFluid()).isPresent()) return vessel == Vessel.GLASS_BOTTLE;
        return Drinks.byFluid(resource.getFluid()).map(d -> d.vessel() == vessel).orElse(false);
    }

    /** Full (one serving) or empty: a vessel never holds part of a serving. */
    @Override
    protected ItemResource update(ItemResource held, int index, FluidResource resource, int amount) {
        Vessel vessel = vessel(held);
        if (vessel == null) return ItemResource.EMPTY;
        if (amount == 0) return ItemResource.of(vessel.empty());
        if (amount != DrinkItem.SERVING) return ItemResource.EMPTY;
        BottledLiquidItem bottled = BottledLiquidItem.byFluid(resource.getFluid()).orElse(null);
        if (bottled != null) return ItemResource.of(bottled);
        ItemStack filled = DrinkItem.fromFluid(resource.toStack(DrinkItem.SERVING));
        return filled.isEmpty() ? ItemResource.EMPTY : ItemResource.of(filled);
    }

    /** The vessel an item is or is served in. */
    private static Vessel vessel(ItemResource held) {
        if (held.getItem() instanceof DrinkItem drink) return drink.vessel();
        if (held.getItem() instanceof BottledLiquidItem) return Vessel.GLASS_BOTTLE;
        return Vessel.of(held.toStack()).orElse(null);
    }
}
