package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.recipe.Recipes;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModEntities;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.List;

/**
 * Client-only setup. Never loaded on a dedicated server. Item looks that depend on the stack (a crowned whiskey, a weathered
 * kettle, a charred cask, a drink's color) are in the item model files datagen writes.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.KILN.get(), KilnScreen::new);
        event.register(ModMenus.BREW_KETTLE.get(), BrewKettleScreen::new);
        event.register(ModMenus.FERMENTING_VAT.get(), FermentingVatScreen::new);
        event.register(ModMenus.POT_STILL.get(), PotStillScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.KILN.get(), KilnRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PLACED_DRINKS.get(), PlacedDrinksRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);   // a seat is invisible
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MILLSTONE.get(), MillstoneRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BREW_KETTLE.get(), VesselRenderers.Kettle::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FERMENTING_VAT.get(), VesselRenderers.Vat::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRESERVING_JAR.get(), VesselRenderers.Jar::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUSHING_TUB.get(), WineryRenderers.Tub::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRUIT_PRESS.get(), WineryRenderers.Press::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WINE_RACK.get(), WineryRenderers.Rack::new);
        event.registerBlockEntityRenderer(ModBlockEntities.POT_STILL.get(), PotStillRenderer::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterStandalone event) {
        ClientModels.register(event, MillstoneRenderer.RUNNER);
        WineryRenderers.registerModels(event);
    }

    @SubscribeEvent
    public static void registerFluids(RegisterFluidModelsEvent event) {
        BrewFluidClient.register(event);
    }

    /** Fruit tree leaves: the base layer (tint 0) takes the biome's foliage color, birch's green, or none. */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        for (FruitTree tree : FruitTrees.all()) {
            switch (tree.tint) {
                case FOLIAGE -> event.register(List.of(BlockTintSources.foliage()), tree.leaves());
                case BIRCH -> event.register(List.of(BlockTintSources.constant(FruitTree.BIRCH_LEAF_COLOR)), tree.leaves());
                case NONE -> { }
            }
        }
    }

    /** The stations look up their recipes on the client too (the faded dish in the kettle, what the vat will make). */
    @SubscribeEvent
    public static void recipesReceived(RecipesReceivedEvent event) {
        Recipes.received(event.getRecipeMap());
    }

    private ClientSetup() {}
}
