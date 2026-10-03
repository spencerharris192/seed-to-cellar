package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BottleLook;
import io.github.spencerharris192.seedtocellar.brewing.CaskItem;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only setup. Never loaded on a dedicated server. */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.KILN.get(), KilnScreen::new);
            MenuScreens.register(ModMenus.BREW_KETTLE.get(), BrewKettleScreen::new);
            MenuScreens.register(ModMenus.FERMENTING_VAT.get(), FermentingVatScreen::new);
            MenuScreens.register(ModMenus.POT_STILL.get(), PotStillScreen::new);
            // The crowned Apple Crown Whiskey has its own icon.
            net.minecraft.client.renderer.item.ItemProperties.register(Drinks.APPLE_CROWN_WHISKEY.item().get(), SeedToCellar.id("crowned"),
                    (stack, level, entity, seed) -> DrinkItem.quality(stack).crowned() ? 1 : 0);
            // A weathered kettle or still shows its patina.
            for (var copper : java.util.List.of(io.github.spencerharris192.seedtocellar.registry.ModItems.BREW_KETTLE,
                    io.github.spencerharris192.seedtocellar.registry.ModItems.POT_STILL)) {
                net.minecraft.client.renderer.item.ItemProperties.register(copper.get(), SeedToCellar.id("weathering"),
                        (stack, level, entity, seed) -> io.github.spencerharris192.seedtocellar.decor.CopperWeathering.stage(stack).ordinal());
            }
            // A charred cask item shows its char.
            for (CaskWood wood : CaskWood.values()) {
                net.minecraft.client.renderer.item.ItemProperties.register(ModBlocks.CASKS.get(wood).get().asItem(), SeedToCellar.id("charred"),
                        (stack, level, entity, seed) -> CaskItem.charred(stack) ? 1 : 0);
            }
        });
        // Guard for the automated client check: a lost tint draws every liquid black.
        for (ModFluids.Entry fluid : ModFluids.all()) {
            int tint = IClientFluidTypeExtensions.of(fluid.get()).getTintColor();
            if (tint != fluid.tint) {
                SeedToCellar.LOGGER.error("Fluid {} has client tint {} instead of {}", fluid.name,
                        Integer.toHexString(tint), Integer.toHexString(fluid.tint));
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.KILN.get(), KilnRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PLACED_DRINKS.get(), PlacedDrinksRenderer::new);
        event.registerEntityRenderer(io.github.spencerharris192.seedtocellar.registry.ModEntities.SEAT.get(),
                net.minecraft.client.renderer.entity.NoopRenderer::new);   // a seat is invisible
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
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(MillstoneRenderer.RUNNER);
        event.register(WineryRenderers.PRESS_SCREW);
        event.register(WineryRenderers.PRESS_HANDLE);
        event.register(WineryRenderers.RACK_BOTTLE);
        event.register(WineryRenderers.DRINK_BOTTLE);
        event.register(WineryRenderers.DRINK_MUG);
        event.register(WineryRenderers.DRINK_FLASK);
        event.register(WineryRenderers.MUG_EMPTY);
        for (Drinks.Drink drink : Drinks.all()) {
            BottleLook look = BottleLook.of(drink);
            if (look != null) event.register(WineryRenderers.spiritModel(drink));
            if (look != null && WineryRenderers.canBeCrowned(look)) event.register(WineryRenderers.crownedModel(drink));
        }
    }

    /** Fruit tree leaves: the base layer (tint 0) takes the biome's foliage color, birch's green, or none. */
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (FruitTree tree : FruitTrees.all()) {
            event.register((state, level, pos, tintIndex) -> {
                if (tintIndex != 0) return -1;
                return switch (tree.tint) {
                    case FOLIAGE -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
                    case BIRCH -> FoliageColor.getBirchColor();
                    case NONE -> -1;
                };
            }, tree.leaves());
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Fruit tree leaves in the inventory: the default foliage green (birch's for pear).
        for (FruitTree tree : FruitTrees.all()) {
            int color = switch (tree.tint) {
                case FOLIAGE -> FoliageColor.getDefaultColor();
                case BIRCH -> FoliageColor.getBirchColor();
                case NONE -> -1;
            };
            event.register((stack, layer) -> layer == 0 ? color : -1, tree.leavesItem());
        }
        // Buckets show their liquid's color.
        event.register(new DynamicFluidContainerModel.Colors(),
                ModFluids.all().stream().map(e -> e.bucket.get()).toArray(Item[]::new));
        // Drinks: layer 1 (the liquid in the mug) takes the drink's color.
        // Kettle drinks (mulled wine) have no foam: their head takes the drink's color too.
        // Spirit bottles: the label's paper (layer 2) and print (layer 3) in the spirit's own colors.
        for (Drinks.Drink d : Drinks.all()) {
            BottleLook look = BottleLook.of(d);
            event.register((stack, layer) -> {
                if (!(stack.getItem() instanceof DrinkItem drink)) return -1;
                if (look != null && layer >= 2) return layer == 2 ? 0xFF000000 | look.paper() : layer == 3 ? 0xFF000000 | look.accent() : -1;
                return layer == 1 || layer == 2 && !drink.profile().graded()
                        ? 0xFF000000 | Drinks.tint(drink.fluid(), stack.getTag(), IClientFluidTypeExtensions.of(drink.fluid()).getTintColor()) : -1;
            }, d.item().get());
        }
    }

    private ClientSetup() {}
}
