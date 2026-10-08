package io.github.spencerharris192.seedtocellar.farming;

import java.util.Optional;
import net.minecraft.core.Holder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;


/**
 * Straw (GDD section 9.7): a ripe grain crop (tag {@code seedtocellar:straw_crops}: wheat, barley,
 * rye, oats, sorghum, rice) harvested with a sickle gives one Straw as well. The item and the
 * "held a sickle" match_tool condition are in the modifier's JSON, so pack makers can change them.
 */
public class StrawModifier extends LootModifier {
    public static final MapCodec<StrawModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .apply(inst, StrawModifier::new));

    private final Item item;

    public StrawModifier(Optional<Holder<LootItemCondition>> condition, int priority, Item item) {
        super(condition, priority);
        this.item = item;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        BlockState state = context.getOptional(LootContextParams.BLOCK_STATE);
        if (state != null && state.is(ModTags.Blocks.STRAW_CROPS) && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            loot.add(new ItemStack(item));
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
