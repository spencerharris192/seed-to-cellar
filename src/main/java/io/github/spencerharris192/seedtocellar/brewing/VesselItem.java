package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * An empty mug or wine bottle. Right-click a tapped cask, keg, vat or press (or let a machine fill it) to
 * pour a drink. (Vanilla's glass bottle gets the same ability from {@link VesselEvents}.)
 */
public class VesselItem extends Item {
    public VesselItem(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new VesselFluidHandler(stack);
    }
}
