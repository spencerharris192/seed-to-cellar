package io.github.spencerharris192.seedtocellar;

import io.github.spencerharris192.seedtocellar.registry.ModStructures;
import io.github.spencerharris192.seedtocellar.registry.ModTriggers;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import io.github.spencerharris192.seedtocellar.farming.CompostItem;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import com.mojang.logging.LogUtils;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModCreativeTabs;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.github.spencerharris192.seedtocellar.registry.ModSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

/** Entry point for Seed to Cellar. Registration only; features live in their own packages. */
@Mod(SeedToCellar.MOD_ID)
public final class SeedToCellar {
    /** Permanent. Never change after release: every saved world and quest book depends on it. */
    public static final String MOD_ID = "seedtocellar";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Newer Forge 47.x builds deprecate some APIs in favour of replacements that older 47.x
    // builds lack. We keep the old forms so the mod runs on every Forge 47 build.
    @SuppressWarnings("removal")
    public SeedToCellar() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModConfigs.register(ModLoadingContext.get());
        ForgeMod.enableMilkFluid();         // milk as a liquid: porridge in the Brew Kettle
        Crops.init();                       // adds every row crop's blocks and items
        FruitTrees.init();                  // adds every fruit tree's sapling, leaves and fruit
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.TYPES.register(modBus);   // also adds the bucket items
        ModFluids.FLUIDS.register(modBus);
        Drinks.init();                      // adds the drink items
        Pies.init();                        // adds the pie blocks, items and slices
        ModEffects.EFFECTS.register(modBus);
        ModBlockEntities.TYPES.register(modBus);
        io.github.spencerharris192.seedtocellar.registry.ModEntities.ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModVillagers.POI_TYPES.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModStructures.POOL_ELEMENT_TYPES.register(modBus);
        io.github.spencerharris192.seedtocellar.registry.ModFeatures.FEATURES.register(modBus);
        ModTriggers.init();                 // advancement triggers (the Crushing Tub's stomp)
        ModSerializers.LOOT_MODIFIERS.register(modBus);
        ModSerializers.BIOME_MODIFIERS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::sendIntegrations);
    }

    /** The One Probe asks for its providers by message (only sent when it's installed). */
    private void sendIntegrations(net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent event) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("theoneprobe")) io.github.spencerharris192.seedtocellar.compat.top.TopCompat.register();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // The vanilla composter accepts our plants (values match similar vanilla items).
            for (Crop crop : Crops.all()) {
                if (!crop.selfPlanting) ComposterBlock.COMPOSTABLES.put(crop.seeds(), 0.3F);
                ComposterBlock.COMPOSTABLES.put(crop.produce(), crop.isPerennial() || crop.kind == Crop.Kind.FRUIT ? 0.3F : 0.65F);   // fruit like sweet berries
                if (crop.hasFlowers()) ComposterBlock.COMPOSTABLES.put(crop.flowers(), 0.3F);
                if (crop.hasWild()) ComposterBlock.COMPOSTABLES.put(crop.wildItem(), 0.65F);
            }
            ComposterBlock.COMPOSTABLES.put(ModItems.HOP_RHIZOME.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.HOP_CONES.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(ModItems.WILD_HOPS.get(), 0.65F);
            ComposterBlock.COMPOSTABLES.put(ModItems.SPENT_GRAIN.get(), 0.5F);
            ComposterBlock.COMPOSTABLES.put(ModItems.STRAW.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.GRAPE_LEAVES.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.VANILLA_POD.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.AGAVE_FIBER.get(), 0.3F);
            ComposterBlock.COMPOSTABLES.put(ModItems.RICE_BRAN.get(), 0.5F);
            for (var pomace : List.of(ModItems.GRAPE_POMACE, ModItems.FRUIT_POMACE, ModItems.OLIVE_POMACE, ModItems.BAGASSE)) {
                ComposterBlock.COMPOSTABLES.put(pomace.get(), 0.5F);
            }
            for (FruitTree tree : FruitTrees.all()) {
                ComposterBlock.COMPOSTABLES.put(tree.saplingItem(), 0.3F);
                ComposterBlock.COMPOSTABLES.put(tree.leavesItem(), 0.3F);
                if (tree.ownFruit()) ComposterBlock.COMPOSTABLES.put(tree.fruit(), 0.65F);
            }

            // A dispenser facing farmland (or a crop on it) spreads compost, like bone meal.
            DispenserBlock.registerBehavior(ModItems.COMPOST.get(), new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    BlockPos target = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                    setSuccess(CompostItem.spread(source.getLevel(), target));
                    if (isSuccess()) stack.shrink(1);
                    return stack;
                }
            });
        });
    }

    /** {@code seedtocellar:<path>} */
    public static ResourceLocation id(String path) {
        return rl(MOD_ID, path);
    }

    /** The one place we build ResourceLocations from parts (see note above). */
    @SuppressWarnings("removal")
    public static ResourceLocation rl(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    /** Parses {@code namespace:path}, defaulting to {@code minecraft:}. */
    @SuppressWarnings("removal")
    public static ResourceLocation parse(String id) {
        return new ResourceLocation(id);
    }
}
