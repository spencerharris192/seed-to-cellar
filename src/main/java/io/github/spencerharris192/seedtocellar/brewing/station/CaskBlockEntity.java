package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerReadable;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cask (ages) and Keg (conditions only) logic (GDD section 13).
 * <ul>
 *   <li>Stores the game time the contents were "born"; age = now - that, scaled by the aging
 *       speed config. No ticking, and it keeps aging in unloaded chunks.</li>
 *   <li>Adding more of the same drink averages the age by volume (solera blending).</li>
 *   <li>Resting 1 day conditions a drink (+1 star). Ageable drinks earn the aging star at their
 *       threshold in years (1 in-game day = 1 year) in one of their ideal woods, or twice that in any
 *       other; crimson and warped casks count two years a day but never give it. Stars are applied
 *       when poured out, and the drink remembers the wood that gave it its years.</li>
 *   <li>Pouring out needs a tap. Breaking keeps contents; aging pauses until placed again.</li>
 * </ul>
 */
public class CaskBlockEntity extends SyncedBlockEntity implements HydrometerReadable {
    public static final int DAY = 24000;

    private final FluidTank tank;
    private final boolean ages;
    @Nullable
    private final CaskWood wood;
    private long filledTime;
    /** Age carried in from an item (broken cask), applied once the block has a level. */
    private long pendingAge = -1;

    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(Handler::new);

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CASK.get(), pos, state);
        AbstractCaskBlock block = (AbstractCaskBlock) state.getBlock();
        this.ages = block.ages();
        this.wood = block.wood();
        // fermented drinks only: juices and kettle drinks have nothing to gain from a cask
        this.tank = new FluidTank(block.capacity(), stack -> Drinks.byFluid(stack.getFluid()).map(d -> d.profile().graded()).orElse(false)) {
            @Override
            protected void onContentsChanged() {
                sync();
            }
        };
    }

    public FluidTank tank() {
        return tank;
    }

    public boolean ages() {
        return ages;
    }

    @Nullable
    public CaskWood wood() {
        return wood;
    }

    /** Charred inside (a cask's blockstate; kegs never are). */
    public boolean charred() {
        return CaskBlock.isCharred(getBlockState());
    }

    private boolean tapped() {
        return getBlockState().getBlock() instanceof AbstractCaskBlock cask && cask.isTapped(getBlockState());
    }

    // --- age ---------------------------------------------------------------------------------

    private long rawAgeTicks() {
        if (level == null || tank.isEmpty()) return 0;
        if (pendingAge >= 0) {
            filledTime = level.getGameTime() - pendingAge;
            pendingAge = -1;
        }
        return Math.max(0, level.getGameTime() - filledTime);
    }

    public long ageTicks() {
        return agedTicks(rawAgeTicks(), wood);
    }

    /** Resting time as it counts for aging: scaled by the aging speed config, and doubled in nether wood. */
    public static long agedTicks(long rawTicks, @Nullable CaskWood wood) {
        return ModConfigs.agedTicks(rawTicks) * (wood == null ? 1 : wood.speed());
    }

    public int years() {
        return (int) (ageTicks() / DAY);
    }

    public boolean conditioned() {
        return ageTicks() >= DAY;
    }

    /** The contents as they'd pour right now: conditioning and aging stars applied. */
    public FluidStack serving(FluidStack raw) {
        return serving(raw, ageTicks(), wood, charred());
    }

    public static FluidStack serving(FluidStack raw, long ageTicks, @Nullable CaskWood wood) {
        return serving(raw, ageTicks, wood, false);
    }

    /**
     * How contents that have rested {@code ageTicks} (from {@link #agedTicks}) would pour from a cask of
     * {@code wood}, or a keg if null. Shared with the cask item's tooltip so both always agree.
     */
    public static FluidStack serving(FluidStack raw, long ageTicks, @Nullable CaskWood wood, boolean charred) {
        if (raw.isEmpty()) return raw;
        FluidStack out = raw.copy();
        Optional<Drinks.Drink> drink = Drinks.byFluid(out.getFluid());
        boolean rests = drink.map(d -> d.profile().craft().byResting()).orElse(true);   // spirits earn theirs in the still
        BrewQuality quality = BrewQuality.of(out).withCraft(BrewQuality.of(out).craft() || rests && ageTicks >= DAY);
        if (wood != null && drink.isPresent() && drink.get().profile().ageable()) {
            int years = (int) (ageTicks / DAY);
            int starAt = drink.get().profile().starYears(wood, charred);
            if (starAt >= 0 && years >= starAt) quality = quality.withAged(true);
            CompoundTag tag = out.getOrCreateTag();
            if (years > tag.getInt(DrinkItem.AGE)) { // the label names the cask that gave it the most years
                tag.putInt(DrinkItem.AGE, years);
                tag.putString(DrinkItem.WOOD, wood.id());
                if (charred) tag.putBoolean(DrinkItem.CHARRED, true);
                else tag.remove(DrinkItem.CHARRED);
            }
        }
        return quality.applyTo(out);
    }

    // --- filling and pouring -----------------------------------------------------------------

    private int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        int accepted = tank.fill(resource, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0 || action.simulate()) return accepted;
        long now = level == null ? 0 : level.getGameTime();
        if (tank.isEmpty()) {
            filledTime = now;
        } else {
            long age = rawAgeTicks();
            filledTime = now - age * tank.getFluidAmount() / (tank.getFluidAmount() + accepted); // blend ages by volume
        }
        return tank.fill(resource, IFluidHandler.FluidAction.EXECUTE);
    }

    private FluidStack drain(int amount, IFluidHandler.FluidAction action) {
        if (!tapped()) return FluidStack.EMPTY;
        FluidStack preview = serving(tank.getFluid());
        FluidStack drained = tank.drain(amount, action);
        if (drained.isEmpty()) return drained;
        preview.setAmount(drained.getAmount());
        return preview;
    }

    public IFluidHandler handler() {
        return fluidCap.orElseThrow(IllegalStateException::new);
    }

    // --- read-outs ---------------------------------------------------------------------------

    @Override
    public List<Component> hydrometerLines() {
        List<Component> lines = new ArrayList<>();
        if (tank.isEmpty()) {
            lines.add(Component.translatable("hydrometer.seedtocellar.empty"));
            return lines;
        }
        FluidStack serving = serving(tank.getFluid());
        lines.add(serving.getDisplayName().copy().append(" " + tank.getFluidAmount() + " mB"));
        lines.add(DrinkItem.stars(BrewQuality.of(serving).stars()));
        if (Drinks.byFluid(serving.getFluid()).map(d -> d.profile().craft().byResting()).orElse(true)) {
            lines.add(Component.translatable(conditioned() ? "hydrometer.seedtocellar.conditioned" : "hydrometer.seedtocellar.conditioning",
                    Math.min(100, ageTicks() * 100 / DAY)).withStyle(ChatFormatting.GRAY));
        }
        Drinks.byFluid(serving.getFluid()).filter(d -> wood != null && d.profile().ageable()).ifPresent(d -> {
            int starAt = d.profile().starYears(wood, charred());
            lines.add((starAt < 0 ? Component.translatable("hydrometer.seedtocellar.aged_no_star", years())
                    : Component.translatable("hydrometer.seedtocellar.aged", years(), starAt)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(wood.nether() ? "hydrometer.seedtocellar.wood_nether"
                    : d.profile().idealIn(wood, charred()) ? "hydrometer.seedtocellar.wood_ideal" : "hydrometer.seedtocellar.wood_slow",
                    DrinkItem.woodName(wood, charred())).withStyle(ChatFormatting.GRAY));
        });
        if (!tapped()) lines.add(Component.translatable("hydrometer.seedtocellar.no_tap").withStyle(ChatFormatting.YELLOW));
        return lines;
    }

    // --- save / load -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        // Only a cask with contents has a birth time. (Placing a cask item merges its data into a
        // fresh cask's; a stray FilledTime of 0 would make the drink as old as the world.)
        if (!tank.isEmpty()) tag.putLong("FilledTime", filledTime);
        tag.putLong("AgeTicks", rawAgeTicks());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("FilledTime")) {
            filledTime = tag.getLong("FilledTime");
            pendingAge = -1;
        } else {
            pendingAge = tag.getLong("AgeTicks"); // placed from an item: age resumes from here
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }

    /** Fill from any side; pour only through the tap. Poured drinks carry their stars and age. */
    private class Handler implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int t) { return serving(tank.getFluid()); }
        @Override public int getTankCapacity(int t) { return tank.getCapacity(); }
        @Override public boolean isFluidValid(int t, FluidStack stack) { return tank.isFluidValid(stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return CaskBlockEntity.this.fill(resource, action); }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid() == tank.getFluid().getFluid() ? CaskBlockEntity.this.drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return CaskBlockEntity.this.drain(maxDrain, action);
        }
    }
}
