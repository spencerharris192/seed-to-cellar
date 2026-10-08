package io.github.spencerharris192.seedtocellar.brewing.station;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.CaskContents;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
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

    private final StationTank tank;
    private final boolean ages;
    @Nullable
    private final CaskWood wood;
    private long filledTime;
    /** Age carried in from an item (broken cask), applied once the block has a level. */
    private long pendingAge = -1;

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CASK.get(), pos, state);
        AbstractCaskBlock block = (AbstractCaskBlock) state.getBlock();
        this.ages = block.ages();
        this.wood = block.wood();
        // fermented drinks only: juices and kettle drinks have nothing to gain from a cask
        this.tank = new StationTank(block.capacity(), stack -> Drinks.byFluid(stack.getFluid()).map(d -> d.profile().graded()).orElse(false)) {
            @Override
            protected void onContentsChanged(FluidStack previous) {
                blendAge(previous, getFluid());
                sync();
            }
        };
    }

    public StationTank tank() {
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
            CompoundTag tag = BrewData.get(out);
            if (years > tag.getIntOr(DrinkItem.AGE, 0)) { // the label names the cask that gave it the most years
                tag.putInt(DrinkItem.AGE, years);
                tag.putString(DrinkItem.WOOD, wood.id());
                if (charred) tag.putBoolean(DrinkItem.CHARRED, true);
                else tag.remove(DrinkItem.CHARRED);
                BrewData.set(out, tag);
            }
        }
        return quality.applyTo(out);
    }

    // --- filling and pouring -----------------------------------------------------------------

    /** New contents start their age now; more of the same blends its age with what's there, by volume (solera). */
    private void blendAge(FluidStack before, FluidStack after) {
        if (after.getAmount() <= before.getAmount()) return;   // poured out: the rest keeps its age
        long now = level == null ? 0 : level.getGameTime();
        if (before.isEmpty()) {
            filledTime = now;
            pendingAge = -1;
        } else {
            long age = level == null ? 0 : Math.max(0, now - filledTime);
            filledTime = now - age * before.getAmount() / after.getAmount();
        }
    }

    /** Pours out through the tap: the serving (stars applied), or nothing without a tap. */
    public FluidStack drain(int amount, StationTank.Action action) {
        if (!tapped()) return FluidStack.EMPTY;
        FluidStack preview = serving(tank.getFluid());
        FluidStack drained = tank.drain(amount, action);
        if (drained.isEmpty()) return drained;
        preview.setAmount(drained.getAmount());
        return preview;
    }

    /** Fill from any side; pour only through the tap. Poured drinks carry their stars and age. */
    private final ResourceHandler<FluidResource> handler = new ResourceHandler<>() {
        @Override public int size() { return 1; }
        @Override public FluidResource getResource(int index) { return FluidResource.of(serving(tank.getFluid())); }
        @Override public long getAmountAsLong(int index) { return tank.getFluidAmount(); }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return tank.getCapacity(); }
        @Override public boolean isValid(int index, FluidResource resource) { return tank.isValid(0, resource); }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return tank.insert(0, resource, amount, transaction);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (!tapped() || tank.isEmpty() || !resource.equals(getResource(0))) return 0;
            return tank.extract(0, FluidResource.of(tank.getFluid()), amount, transaction);
        }
    };

    public ResourceHandler<FluidResource> handler() {
        return handler;
    }

    public ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        return handler;
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
        lines.add(serving.getHoverName().copy().append(" " + tank.getFluidAmount() + " mB"));
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("Tank"));
        if (!tank.isEmpty()) {
            rawAgeTicks();   // settles an age carried in from an item into a fill time
            output.putLong("FilledTime", filledTime);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("Tank"));
        filledTime = input.getLongOr("FilledTime", 0L);
        // a template's cask (the Brewhouse cellar's) gives an age instead, as no fill time can be known before it's placed
        pendingAge = input.getLongOr("AgeTicks", -1L);
    }

    /** Broken full, the cask item keeps the drink and how long it had rested (CaskItem shows it; loot copies it). */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!tank.isEmpty()) {
            components.set(ModComponents.CASK_CONTENTS.get(), new CaskContents(SimpleFluidContent.copyOf(tank.getFluid()), rawAgeTicks()));
        }
    }

    /** Placed from a full cask item: the drink goes back in, and its age resumes from where it was. */
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        CaskContents contents = components.get(ModComponents.CASK_CONTENTS.get());
        if (contents != null && !contents.fluid().isEmpty()) {
            tank.setFluid(contents.fluid().copy());
            pendingAge = contents.ageTicks();
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("Tank");
        output.discard("FilledTime");
    }
}
