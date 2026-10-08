package io.github.spencerharris192.seedtocellar.brewing.station;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import java.util.function.Predicate;

/**
 * A station's tank. Pipes and other mods reach it through NeoForge's fluid handler (inside transactions); the station's
 * own code fills and drains it directly here. Liquids only mix when they're the same and carry the same data (a wort's
 * malt bill, a drink's quality), so batches made differently never blend. Subclasses override {@link #onContentsChanged()}.
 */
public class StationTank extends FluidStacksResourceHandler {
    /** Whether a fill or drain really happens, or only reports what would. */
    public enum Action {
        EXECUTE, SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }

        public boolean simulate() {
            return this == SIMULATE;
        }
    }

    private final Predicate<FluidStack> validator;

    public StationTank(int capacity) {
        this(capacity, stack -> true);
    }

    public StationTank(int capacity, Predicate<FluidStack> validator) {
        super(1, capacity);
        this.validator = validator;
    }

    /** The liquid inside (live: change it through {@link #setFluid}). */
    public FluidStack getFluid() {
        return stacks.get(0);
    }

    public void setFluid(FluidStack stack) {
        set(0, FluidResource.of(stack), stack.getAmount());
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluidAmount());
    }

    public boolean isEmpty() {
        return getFluid().isEmpty();
    }

    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    /** Called after the contents change (from our code at once, from a transaction when it commits). */
    protected void onContentsChanged() {
    }

    /** Like {@link #onContentsChanged()}, knowing what was in the tank before. */
    protected void onContentsChanged(FluidStack previous) {
        onContentsChanged();
    }

    @Override
    protected final void onContentsChanged(int index, FluidStack previousContents) {
        onContentsChanged(previousContents);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return validator.test(resource.toStack(1));
    }

    /** Pours in as much as fits; returns how much went in. */
    public int fill(FluidStack resource, Action action) {
        if (resource.isEmpty() || !isFluidValid(resource)) return 0;
        FluidStack current = getFluid();
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) return 0;
        int filled = Math.min(getSpace(), resource.getAmount());
        if (filled > 0 && action.execute()) setFluid(resource.copyWithAmount(current.getAmount() + filled));
        return filled;
    }

    /** Takes out up to {@code maxDrain}; returns what came out. */
    public FluidStack drain(int maxDrain, Action action) {
        FluidStack current = getFluid();
        int drained = Math.min(maxDrain, current.getAmount());
        if (drained <= 0) return FluidStack.EMPTY;
        FluidStack out = current.copyWithAmount(drained);
        if (action.execute()) setFluid(current.copyWithAmount(current.getAmount() - drained));
        return out;
    }

    /** Takes out up to that much of that liquid (nothing if it's a different one). */
    public FluidStack drain(FluidStack resource, Action action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(getFluid(), resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }
}
