package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModItems;

/** Hops on a trellis (GDD growth style D): planted from a rhizome, 1-3 cones a picking, temperate. */
public class HopsBlock extends TrellisVineBlock {
    public HopsBlock(Properties properties) {
        super(properties, new Harvest(ModItems.HOP_RHIZOME, ModItems.HOP_CONES, 1, 3, null), Climate.TEMPERATE);
    }
}
