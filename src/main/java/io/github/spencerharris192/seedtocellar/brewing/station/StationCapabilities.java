package io.github.spencerharris192.seedtocellar.brewing.station;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlockEntity;
import io.github.spencerharris192.seedtocellar.farming.DryingRackBlockEntity;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.FruitPressBlockEntity;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** What each station shows hoppers, pipes and other mods' machines: its item slots and tanks, side by side. */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class StationCapabilities {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        var item = Capabilities.Item.BLOCK;
        var fluid = Capabilities.Fluid.BLOCK;
        event.registerBlockEntity(item, ModBlockEntities.KILN.get(), KilnBlockEntity::itemHandler);
        event.registerBlockEntity(item, ModBlockEntities.MALTING_TUB.get(), MaltingTubBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.MALTING_TUB.get(), MaltingTubBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.MILLSTONE.get(), MillstoneBlockEntity::itemHandler);
        event.registerBlockEntity(item, ModBlockEntities.BREW_KETTLE.get(), BrewKettleBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.BREW_KETTLE.get(), BrewKettleBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.FERMENTING_VAT.get(), FermentingVatBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.FERMENTING_VAT.get(), FermentingVatBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.PRESERVING_JAR.get(), PreservingJarBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.PRESERVING_JAR.get(), PreservingJarBlockEntity::fluidHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.CASK.get(), CaskBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.POT_STILL.get(), PotStillBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.POT_STILL.get(), PotStillBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.CRUSHING_TUB.get(), CrushingTubBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.CRUSHING_TUB.get(), CrushingTubBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.FRUIT_PRESS.get(), FruitPressBlockEntity::itemHandler);
        event.registerBlockEntity(fluid, ModBlockEntities.FRUIT_PRESS.get(), FruitPressBlockEntity::fluidHandler);
        event.registerBlockEntity(item, ModBlockEntities.WINE_RACK.get(), WineRackBlockEntity::itemHandler);
        event.registerBlockEntity(item, ModBlockEntities.COMPOST_BIN.get(), CompostBinBlockEntity::itemHandler);
        event.registerBlockEntity(item, ModBlockEntities.DRYING_RACK.get(), DryingRackBlockEntity::itemHandler);
    }

    private StationCapabilities() {}
}
