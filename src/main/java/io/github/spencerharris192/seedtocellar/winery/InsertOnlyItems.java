package io.github.spencerharris192.seedtocellar.winery;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** A station's input as machines see it: hoppers can put things in, but not pull them back out. */
public class InsertOnlyItems extends DelegatingResourceHandler<ItemResource> {
    public InsertOnlyItems(ResourceHandler<ItemResource> inner) {
        super(inner);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
