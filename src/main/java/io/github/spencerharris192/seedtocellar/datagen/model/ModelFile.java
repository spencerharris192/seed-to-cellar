package io.github.spencerharris192.seedtocellar.datagen.model;

import net.minecraft.resources.Identifier;

/** A model by its id ({@code seedtocellar:block/kiln}): one we build, or one that already exists. */
public class ModelFile {
    private final Identifier location;

    public ModelFile(Identifier location) {
        this.location = location;
    }

    public Identifier getLocation() {
        return location;
    }
}
