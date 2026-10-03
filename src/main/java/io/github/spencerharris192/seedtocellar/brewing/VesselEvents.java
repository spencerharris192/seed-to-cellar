package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Juices are served in vanilla's Glass Bottle (GDD section 16). An empty glass bottle gets our vessel
 * handler, so pressing it against a juice tank (or a machine filling it) pours a bottle of juice. It
 * only ever accepts drinks served in glass bottles, so water, potions and other mods' liquids are
 * untouched.
 */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class VesselEvents {
    private static final ResourceLocation GLASS_BOTTLE = SeedToCellar.id("drink_vessel");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().is(Items.GLASS_BOTTLE)) event.addCapability(GLASS_BOTTLE, new VesselFluidHandler(event.getObject()));
    }

    private VesselEvents() {}
}
