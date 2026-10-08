package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Drinks' vessels hold liquid for pipes and machines ({@link VesselFluidHandler}): our mugs and bottles, every drink and
 * bottled liquid, and vanilla's Glass Bottle, which serves juices (GDD section 16). The handler only ever accepts drinks
 * served in that vessel, so water, potions and other mods' liquids are untouched. Our buckets keep their liquid's data.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class VesselEvents {
    @SubscribeEvent
    public static void capabilities(RegisterCapabilitiesEvent event) {
        List<ItemLike> vessels = new ArrayList<>(List.of(ModItems.MUG.get(), ModItems.WINE_BOTTLE.get(), ModItems.SPIRIT_BOTTLE.get(), Items.GLASS_BOTTLE));
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof DrinkItem || item instanceof BottledLiquidItem) vessels.add(item);
        }
        event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new VesselFluidHandler(access), vessels.toArray(ItemLike[]::new));
        event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new VesselBucketItem.Handler(access),
                ModFluids.all().stream().map(fluid -> fluid.bucket.get()).toArray(ItemLike[]::new));
    }

    private VesselEvents() {}
}
