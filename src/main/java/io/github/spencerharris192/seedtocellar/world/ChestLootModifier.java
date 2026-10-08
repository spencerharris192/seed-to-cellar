package io.github.spencerharris192.seedtocellar.world;

import java.util.Optional;
import net.minecraft.core.Holder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.CraftStep;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

/**
 * Our finds in vanilla's chests (GDD section 18.4), added to their loot without replacing it: with `chance`, `rolls`
 * picks from the weighted `entries` (seeds in village houses, agave pups in desert pyramids, rum in shipwrecks...). A
 * `brewed` entry is a drink as it was left: a random quality, and up to `max_years` in an oak cask (named and starred just
 * as a cask would pour it: a shipwreck's rum may be Dark Rum). The chestLoot config option turns it all off.
 */
public class ChestLootModifier extends LootModifier {
    /** One find: `weight` against the others, `min`-`max` of it; `brewed` drinks get a random quality and age. */
    public record Entry(Item item, int weight, int min, int max, boolean brewed, int maxYears) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Entry::item),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight),
                Codec.INT.optionalFieldOf("min", 1).forGetter(Entry::min),
                Codec.INT.optionalFieldOf("max", 1).forGetter(Entry::max),
                Codec.BOOL.optionalFieldOf("brewed", false).forGetter(Entry::brewed),
                Codec.INT.optionalFieldOf("max_years", 0).forGetter(Entry::maxYears)).apply(inst, Entry::new));
    }

    public static final MapCodec<ChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(inst.group(
                            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                            Codec.INT.optionalFieldOf("rolls", 1).forGetter(m -> m.rolls),
                            Entry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries)))
                    .apply(inst, ChestLootModifier::new));

    private final float chance;
    private final int rolls;
    private final List<Entry> entries;

    public ChestLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, float chance, int rolls, List<Entry> entries) {
        super(condition, priority);
        this.chance = chance;
        this.rolls = rolls;
        this.entries = List.copyOf(entries);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfigs.COMMON.chestLoot.get() || entries.isEmpty()) return generatedLoot;
        RandomSource random = context.getRandom();
        if (random.nextFloat() >= chance) return generatedLoot;
        int total = entries.stream().mapToInt(e -> Math.max(0, e.weight())).sum();
        if (total <= 0) return generatedLoot;   // (a data pack's entries all weighing nothing)
        for (int roll = 0; roll < rolls; roll++) {
            int pick = random.nextInt(total);
            for (Entry entry : entries) {
                pick -= Math.max(0, entry.weight());
                if (pick >= 0) continue;
                ItemStack stack = new ItemStack(entry.item(), entry.min() + random.nextInt(Math.max(1, entry.max() - entry.min() + 1)));
                if (entry.brewed()) brew(stack, entry.maxYears(), random);
                generatedLoot.add(stack);
                break;
            }
        }
        return generatedLoot;
    }

    /**
     * A found drink: its maker's luck with yeast, temperature and the craft step (a spirit's is its runs through the still,
     * so its label says how many), then up to `maxYears` in oak.
     */
    public static void brew(ItemStack stack, int maxYears, RandomSource random) {
        if (!(stack.getItem() instanceof DrinkItem drink)) return;
        boolean craft = random.nextFloat() < 0.5F;
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        var spirit = Drinks.byFluid(drink.fluid()).filter(d -> Drinks.spirits().contains(d));
        if (spirit.isPresent()) {
            CraftStep step = spirit.get().profile().craft();
            data.putInt(CraftStep.RUNS, craft ? (step == CraftStep.NEUTRAL ? 3 : 2) : 1);
            craft = step.earned(data);
        }
        FluidStack base = new FluidStack(drink.fluid(), 250);
        io.github.spencerharris192.seedtocellar.brewing.BrewData.set(base, data);
        FluidStack raw = new BrewQuality(random.nextFloat() < 0.75F, random.nextFloat() < 0.6F, craft, false).applyTo(base);
        int years = maxYears <= 0 ? 0 : random.nextInt(maxYears + 1);
        FluidStack poured = CaskBlockEntity.serving(raw, (long) years * CaskBlockEntity.DAY, CaskWood.OAK, false);
        io.github.spencerharris192.seedtocellar.brewing.BrewData.copy(poured, stack);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
