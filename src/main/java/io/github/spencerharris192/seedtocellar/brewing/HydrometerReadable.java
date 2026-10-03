package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.network.chat.Component;

import java.util.List;

/** A block entity the Hydrometer (and Jade) can read: a few plain-language status lines. */
public interface HydrometerReadable {
    List<Component> hydrometerLines();
}
