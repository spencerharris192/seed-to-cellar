package io.github.spencerharris192.seedtocellar.farming;

import java.util.Optional;
import net.minecraft.core.Holder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.HashMap;
import java.util.Map;

/**
 * The harvest side of Fertile Farmland, for any crop (ours or vanilla's, broken by hand, by a
 * right-click harvest, by a villager or by water): when a ripe crop's loot is made and the soil
 * under it is fertile, 50% of the time add one more of the crop itself, and 1 time in 3 use up
 * a point of the soil's fertility. Soil tagged {@code seedtocellar:always_fertile} (Farmer's Delight's
 * Rich Soil Farmland) gives the extra harvests and never wears out.
 */
public class FertileHarvestModifier extends LootModifier {
    public static final MapCodec<FertileHarvestModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, FertileHarvestModifier::new));

    public FertileHarvestModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        BlockState state = context.getOptional(LootContextParams.BLOCK_STATE);
        Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
        if (state == null || origin == null || !(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return loot;
        BlockPos soilPos = BlockPos.containing(origin).below();
        BlockState soil = context.getLevel().getBlockState(soilPos);
        boolean always = soil.is(ModTags.Blocks.ALWAYS_FERTILE);
        if (!always && !(soil.getBlock() instanceof FertileFarmlandBlock)) return loot;

        if (context.getRandom().nextDouble() < ModConfigs.COMMON.fertileExtraHarvestChance.get()) {
            Item harvest = harvestOf(loot, state.getCloneItemStack(context.getLevel(), soilPos.above(), false).getItem());
            if (harvest != null) loot.add(new ItemStack(harvest));
        }
        if (!always && context.getRandom().nextDouble() < ModConfigs.COMMON.fertileUseChance.get()) {
            FertileFarmlandBlock.useFertility(context.getLevel(), soilPos, soil);
        }
        return loot;
    }

    /** The crop itself among the drops: the most plentiful item that isn't the seed (or the seed, for potatoes). */
    private static Item harvestOf(ObjectArrayList<ItemStack> loot, Item seed) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack stack : loot) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        return counts.entrySet().stream().filter(e -> e.getKey() != seed).max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(counts.containsKey(seed) ? seed : null);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
