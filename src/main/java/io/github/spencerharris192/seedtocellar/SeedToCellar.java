package io.github.spencerharris192.seedtocellar;

import com.mojang.logging.LogUtils;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.farming.CompostItem;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.registry.ModBlockEntities;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import io.github.spencerharris192.seedtocellar.registry.ModCreativeTabs;
import io.github.spencerharris192.seedtocellar.registry.ModEntities;
import io.github.spencerharris192.seedtocellar.registry.ModFeatures;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModMenus;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import io.github.spencerharris192.seedtocellar.registry.ModSerializers;
import io.github.spencerharris192.seedtocellar.registry.ModStructures;
import io.github.spencerharris192.seedtocellar.registry.ModTriggers;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

/** Entry point for Seed to Cellar. Registration only; features live in their own packages. */
@Mod(SeedToCellar.MOD_ID)
public final class SeedToCellar {
    /** Permanent. Never change after release: every saved world and quest book depends on it. */
    public static final String MOD_ID = "seedtocellar";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SeedToCellar(IEventBus modBus, ModContainer container) {
        ModConfigs.register(container);
        NeoForgeMod.enableMilkFluid();         // milk as a liquid: porridge in the Brew Kettle
        Crops.init();                       // adds every row crop's blocks and items
        FruitTrees.init();                  // adds every fruit tree's sapling, leaves and fruit
        ModComponents.COMPONENTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.TYPES.register(modBus);   // also adds the bucket items
        ModFluids.FLUIDS.register(modBus);
        Drinks.init();                      // adds the drink items
        Pies.init();                        // adds the pie blocks, items and slices
        ModEffects.EFFECTS.register(modBus);
        ModBlockEntities.TYPES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModVillagers.POI_TYPES.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModStructures.POOL_ELEMENT_TYPES.register(modBus);
        ModFeatures.FEATURE_TYPES.register(modBus);
        ModTriggers.TRIGGERS.register(modBus);   // advancement triggers (the Crushing Tub's stomp)
        ModSerializers.LOOT_MODIFIERS.register(modBus);
        ModSerializers.BIOME_MODIFIERS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::compostables);
        // The One Probe's integration (compat/top) is parked until The One Probe reaches this Minecraft version.
    }

    /**
     * The vanilla composter takes our leftovers (values match similar vanilla items). Crops, fruit and saplings get theirs
     * where they're registered (Crop, FruitTree).
     */
    private void compostables(ModifyDefaultComponentsEvent event) {
        compost(event, ContextIntProviders.COMPOSTABLE_LOW, ModItems.HOP_RHIZOME, ModItems.STRAW, ModItems.GRAPE_LEAVES,
                ModItems.VANILLA_POD, ModItems.AGAVE_FIBER);
        compost(event, ContextIntProviders.COMPOSTABLE_LOW_MEDIUM, ModItems.HOP_CONES, ModItems.SPENT_GRAIN, ModItems.RICE_BRAN,
                ModItems.GRAPE_POMACE, ModItems.FRUIT_POMACE, ModItems.OLIVE_POMACE, ModItems.BAGASSE);
        compost(event, ContextIntProviders.COMPOSTABLE_MEDIUM, ModItems.WILD_HOPS);
    }

    @SafeVarargs
    private static void compost(ModifyDefaultComponentsEvent event, ResourceKey<ContextIntProvider> chance, Supplier<? extends ItemLike>... items) {
        for (Supplier<? extends ItemLike> item : items) {
            event.modify(item.get(), (components, context, it) -> components.set(DataComponents.COMPOSTABLE, new Compostable(chance)));
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // A dispenser facing farmland (or a crop on it) spreads compost, like bone meal.
            DispenserBlock.registerBehavior(ModItems.COMPOST.get(), new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    BlockPos target = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                    setSuccess(CompostItem.spread(source.level(), target));
                    if (isSuccess()) stack.shrink(1);
                    return stack;
                }
            });
        });
    }

    /** {@code seedtocellar:<path>} */
    public static Identifier id(String path) {
        return rl(MOD_ID, path);
    }

    /** The one place we build Identifiers from parts. */
    public static Identifier rl(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /** Parses {@code namespace:path}, defaulting to {@code minecraft:}. */
    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }
}
