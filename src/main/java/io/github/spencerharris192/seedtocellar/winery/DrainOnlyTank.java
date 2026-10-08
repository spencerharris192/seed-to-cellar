package io.github.spencerharris192.seedtocellar.winery;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A station's output tank as the outside world sees it: buckets, bottles and pipes can take the liquid
 * out, but nothing can be poured in (the tub and press only make liquid).
 */
public class DrainOnlyTank extends DelegatingResourceHandler<FluidResource> {
    public DrainOnlyTank(ResourceHandler<FluidResource> tank) {
        super(tank);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return false;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
