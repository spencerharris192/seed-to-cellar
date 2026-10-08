package io.github.spencerharris192.seedtocellar.datagen.model;

import net.minecraft.resources.Identifier;

/** A block (or item) model being written. */
public class BlockModelBuilder extends ModelBuilder<BlockModelBuilder> {
    public BlockModelBuilder(Identifier location) {
        super(location);
    }
}
