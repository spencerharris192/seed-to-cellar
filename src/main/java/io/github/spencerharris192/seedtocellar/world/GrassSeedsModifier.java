package io.github.spencerharris192.seedtocellar.world;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Adds a seed to grass drops when its conditions pass (set in the data file: which loot
 * table, not sheared). Adds to vanilla loot without replacing it, and respects the
 * grassSeedDrops config option. The chance is our seed's grassSeedChances setting, or the
 * data file's `chance` for any other item. `cold_biomes_only` limits it to cold biomes
 * (rye), judged by the biome's temperature where the grass was broken.
 */
public class GrassSeedsModifier extends LootModifier {
    /** Biome temperature at or below which a biome counts as cold (taiga 0.25, snowy plains 0). */
    public static final float COLD_BIOME = 0.3F;

    public static final Supplier<Codec<GrassSeedsModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst ->
            codecStart(inst).and(inst.group(
                            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                            Codec.BOOL.optionalFieldOf("cold_biomes_only", false).forGetter(m -> m.coldOnly),
                            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(m -> m.chance)))
                    .apply(inst, GrassSeedsModifier::new)));

    private final Item item;
    private final boolean coldOnly;
    private final float chance;

    public GrassSeedsModifier(LootItemCondition[] conditions, Item item, boolean coldOnly, float chance) {
        super(conditions);
        this.item = item;
        this.coldOnly = coldOnly;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfigs.COMMON.grassSeedDrops.get()) return generatedLoot;
        var key = ForgeRegistries.ITEMS.getKey(item);
        var setting = key == null || !key.getNamespace().equals(io.github.spencerharris192.seedtocellar.SeedToCellar.MOD_ID) ? null
                : ModConfigs.COMMON.grassSeedChances.get(key.getPath());
        if (context.getRandom().nextFloat() >= (setting != null ? setting.get().floatValue() : chance)) return generatedLoot;
        if (coldOnly) {
            Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
            if (origin == null) return generatedLoot;
            float temperature = context.getLevel().getBiome(BlockPos.containing(origin)).value().getBaseTemperature();
            if (temperature > COLD_BIOME) return generatedLoot;
        }
        generatedLoot.add(new ItemStack(item));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
