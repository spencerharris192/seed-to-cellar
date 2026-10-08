package io.github.spencerharris192.seedtocellar.brewing;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A cooking liquid in vanilla's glass bottle (olive oil, GDD section 17): one 250 mB serving that fills and
 * empties like a drink (right-click the press with an empty glass bottle) but isn't drunk. Used in recipes,
 * which give the bottle back.
 */
public class BottledLiquidItem extends Item {
    private static final List<BottledLiquidItem> ALL = new ArrayList<>();

    private final Supplier<Fluid> fluid;

    public BottledLiquidItem(Supplier<Fluid> fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
        ALL.add(this);
    }

    public Fluid fluid() {
        return fluid.get();
    }

    public static Optional<BottledLiquidItem> byFluid(Fluid fluid) {
        return ALL.stream().filter(item -> item.fluid() == fluid).findFirst();
    }
}
