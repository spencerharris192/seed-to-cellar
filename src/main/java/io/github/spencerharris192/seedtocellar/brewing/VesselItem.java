package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.world.item.Item;

/**
 * An empty mug or wine bottle. Right-click a tapped cask, keg, vat or press (or let a machine fill it) to
 * pour a drink. (Every vessel holds liquid through {@link VesselEvents}.)
 */
public class VesselItem extends Item {
    public VesselItem(Properties properties) {
        super(properties);
    }
}
