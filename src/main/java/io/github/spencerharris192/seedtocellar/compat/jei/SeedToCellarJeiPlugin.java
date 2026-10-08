package io.github.spencerharris192.seedtocellar.compat.jei;

import net.minecraft.core.registries.BuiltInRegistries;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

import java.util.Comparator;
import java.util.List;

/**
 * JEI integration (GDD section 19): a page per station and info pages for things that happen
 * in the world. Only loaded by JEI itself, so the mod runs fine without JEI installed.
 */
@JeiPlugin
public class SeedToCellarJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return SeedToCellar.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new StationCategories.Malting(gui), new StationCategories.Kilning(gui), new StationCategories.Milling(gui),
                new StationCategories.Kettle(gui), new StationCategories.Fermenting(gui), new StationCategories.Jar(gui),
                new StationCategories.Drying(gui), new StationCategories.Cooking(gui), new StationCategories.Crushing(gui), new StationCategories.Mixing(gui),
                new StationCategories.Pressing(gui), new StationCategories.Distilling(gui), new StationCategories.GrowingCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        registration.addRecipes(JeiTypes.MALTING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.MALTING.get()).toList());
        registration.addRecipes(JeiTypes.KILNING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.KILNING.get()).toList());
        registration.addRecipes(JeiTypes.MILLING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.MILLING.get()).toList());
        registration.addRecipes(JeiTypes.DRYING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.DRYING.get()).toList());
        registration.addRecipes(JeiTypes.COOKING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.COOKING.get()).toList());
        registration.addRecipes(JeiTypes.CRUSHING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.CRUSHING.get()).toList());
        registration.addRecipes(JeiTypes.MIXING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.MIXING.get()).toList());
        registration.addRecipes(JeiTypes.PRESSING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.PRESSING.get()).toList());
        registration.addRecipes(JeiTypes.KETTLE, List.of(new StationCategories.KettleStep(false), new StationCategories.KettleStep(true)));
        registration.addRecipes(JeiTypes.FERMENTING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.FERMENTING.get())
                .sorted(Comparator.comparingInt(FermentingRecipe::priority).reversed()).toList());
        registration.addRecipes(JeiTypes.JAR, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.JAR.get()).filter(r -> !r.crowns()).toList());   // a secret stays one
        registration.addRecipes(JeiTypes.DISTILLING, io.github.spencerharris192.seedtocellar.recipe.Recipes.stream(level, ModRecipes.DISTILLING.get())
                .sorted(Comparator.comparingInt(io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe::priority).reversed()).toList());

        registration.addRecipes(JeiTypes.GROWING, growing());

        // Things that happen in the world rather than in a recipe.
        info(registration, ModItems.FERMENTING_VAT.get(), "fermenting_vat");
        info(registration, ModItems.BREW_KETTLE.get(), "brew_kettle");
        info(registration, ModItems.ALE_YEAST.get(), "ale_yeast");
        ModItems.CASKS.values().forEach(cask -> info(registration, cask.get(), cask.getId().getPath()));
        info(registration, ModItems.WINE_RACK.get(), "wine_rack");
        info(registration, ModItems.BOTTLE_SHELF.get(), "bottle_shelf");
        info(registration, ModItems.WINE_DISPLAY.get(), "wine_display");
        info(registration, ModItems.MUG_RACK.get(), "mug_rack");
        info(registration, ModItems.TAVERN_SIGN.get(), "tavern_sign");
        info(registration, ModItems.KEG.get(), "keg");
        info(registration, ModItems.HYDROMETER.get(), "hydrometer");
        for (Crop crop : Crops.all()) if (crop.growsWild()) info(registration, crop.seeds(), crop.seedId);
        info(registration, ModItems.HOP_RHIZOME.get(), "hop_rhizome");
        for (FruitTree tree : FruitTrees.all()) info(registration, tree.saplingItem(), tree.name + "_sapling");
        info(registration, ModItems.COMPOST_BIN.get(), "compost_bin");
        info(registration, ModItems.COMPOST.get(), "compost");
        registration.addItemStackInfo(ModItems.SICKLES.stream().map(sickle -> new ItemStack(sickle.get())).toList(),
                Component.translatable("jei.seedtocellar.info.sickle"));
        info(registration, ModItems.STRAW.get(), "straw");
        info(registration, ModItems.DRYING_RACK.get(), "drying_rack");
        info(registration, ModItems.CRUSHING_TUB.get(), "crushing_tub");
        info(registration, ModItems.FRUIT_PRESS.get(), "fruit_press");
        info(registration, ModItems.MOTHER_OF_VINEGAR.get(), "vinegar");
        info(registration, ModItems.BREW_KETTLE.get(), "kettle_cooking");
        info(registration, ModItems.HARVEST_FEAST.get(), "harvest_feast");
        registration.addItemStackInfo(Pies.all().stream().map(pie -> new ItemStack(pie.item().get())).toList(),
                Component.translatable("jei.seedtocellar.info.pie"));
        info(registration, ModFluids.VINEGAR, "vinegar");
        // One shared page about climates, shown for every seed.
        registration.addItemStackInfo(java.util.stream.Stream.concat(Crops.all().stream().map(crop -> new ItemStack(crop.seeds())),
                FruitTrees.all().stream().map(tree -> new ItemStack(tree.saplingItem()))).toList(),
                Component.translatable("jei.seedtocellar.info.climate"));
        info(registration, ModItems.MUG.get(), "mug");
        info(registration, ModItems.WINE_BOTTLE.get(), "wine_bottle");
        info(registration, ModItems.WINE_YEAST.get(), "wine_yeast");
        info(registration, ModFluids.HONEY_WATER, "honey_water");
        info(registration, ModItems.OLIVE_OIL.get(), "olive_oil");
        info(registration, ModItems.SORGHUM_SYRUP.get(), "sorghum_syrup");
        info(registration, ModItems.POT_STILL.get(), "pot_still");
        info(registration, ModItems.POT_STILL.get(), "copper");
        info(registration, ModItems.BREW_KETTLE.get(), "copper");
        info(registration, ModItems.SPIRIT_BOTTLE.get(), "spirit_bottle");
        info(registration, ModFluids.STILLAGE, "stillage");
        info(registration, ModFluids.CORN_WASH, "washes");
        info(registration, ModFluids.POTATO_WASH, "washes");
        for (Drinks.Drink drink : Drinks.spirits()) if (drink.style() != null) info(registration, drink.item().get(), "spirit_names");
        info(registration, Drinks.GRAPPA.item().get(), "grappa");
        info(registration, ModFluids.POMACE_MASH, "grappa");
        info(registration, ModItems.MOLASSES.get(), "rum");
        info(registration, ModItems.GIN_BASKET.get(), "gin");
        info(registration, ModItems.VANILLA_POD.get(), "vanilla");
        info(registration, ModItems.BLACK_FOREST_CAKE.get(), "black_forest_cake");
        info(registration, ModItems.LAGER_YEAST.get(), "lager_yeast");
        info(registration, ModItems.KOJI_RICE.get(), "sake");
        info(registration, Drinks.SAKE.item().get(), "sake");
        info(registration, ModItems.GROUND_COFFEE.get(), "coffee");
        info(registration, Drinks.COFFEE.item().get(), "coffee");
        info(registration, ModItems.WHEAT_MALT.get(), "wheat_beer");
        for (Drinks.Drink liqueur : Drinks.liqueurs()) info(registration, liqueur.item().get(), "liqueurs");
        info(registration, Drinks.GIN.item().get(), "gin");
        info(registration, ModFluids.CANE_JUICE, "rum");
        info(registration, ModItems.ROASTED_AGAVE.get(), "tequila");
        info(registration, ModFluids.AGAVE_JUICE, "tequila");
        info(registration, Drinks.GINGER_BEER.item().get(), "ginger_beer");
        info(registration, ModItems.HARVEST_FEAST_SERVING.get(), "harvest_feast");
        // Every liquid comes in a bucket, filled from (and poured back into) the vessels that hold it.
        registration.addItemStackInfo(ModFluids.all().stream().map(fluid -> new ItemStack(fluid.bucket.get())).toList(),
                Component.translatable("jei.seedtocellar.info.buckets"));
        // Liquids only ever poured from their bottles or mugs.
        for (Drinks.Drink drink : List.of(Drinks.COFFEE, Drinks.ELDERFLOWER_CORDIAL, Drinks.MULLED_WINE)) {
            registration.addIngredientInfo(new net.neoforged.neoforge.fluids.FluidStack(drink.fluid().get(), 1000),
                    mezz.jei.api.neoforge.NeoForgeTypes.FLUID_STACK, Component.translatable("jei.seedtocellar.info.poured"));
        }
        for (Crop crop : Crops.all()) if (crop.hasWild()) info(registration, crop.wildItem(), "wild_" + crop.name);
        info(registration, ModItems.WILD_HOPS.get(), "wild_hops");
        for (Drinks.Drink drink : Drinks.all()) {
            if (drink.profile().graded()) info(registration, drink.item().get(), "drinks");
            else if (drink == Drinks.LEMONADE || drink == Drinks.ELDERFLOWER_CORDIAL) info(registration, drink.item().get(), "soft_drinks");
            else if (drink.vessel() == io.github.spencerharris192.seedtocellar.brewing.Vessel.GLASS_BOTTLE) info(registration, drink.item().get(), "juices");
        }
    }

    private static void info(IRecipeRegistration registration, ItemLike item, String key) {
        registration.addItemStackInfo(new ItemStack(item), Component.translatable("jei.seedtocellar.info." + key));
    }

    /** An info page for a liquid, shown for its bucket and for the liquid itself. */
    private static void info(IRecipeRegistration registration, ModFluids.Entry fluid, String key) {
        info(registration, fluid.bucket.get(), key);
        registration.addIngredientInfo(new net.neoforged.neoforge.fluids.FluidStack(fluid.get(), 1000), mezz.jei.api.neoforge.NeoForgeTypes.FLUID_STACK,
                Component.translatable("jei.seedtocellar.info." + key));
    }

    /** The Growing page: every crop, fruit tree, hops and vanilla, from what you plant to what you harvest. */
    private static List<StationCategories.Growing> growing() {
        List<StationCategories.Growing> plants = new java.util.ArrayList<>();
        for (Crop crop : Crops.all()) {
            List<ItemStack> harvest = new java.util.ArrayList<>();
            harvest.add(new ItemStack(crop.produce()));
            if (crop.seeds() != crop.produce() && (crop.isFieldCrop())) harvest.add(new ItemStack(crop.seeds()));
            if (crop.hasFlowers()) harvest.add(new ItemStack(crop.flowers()));
            if (crop.style == Crop.Style.VINE) harvest.add(new ItemStack(ModItems.GRAPE_LEAVES.get()));
            plants.add(new StationCategories.Growing(new ItemStack(crop.seeds()), harvest, "jei.seedtocellar.growing." + crop.name, crop.climate));
        }
        for (FruitTree tree : FruitTrees.all()) {
            plants.add(new StationCategories.Growing(new ItemStack(tree.saplingItem()), List.of(new ItemStack(tree.fruit()),
                    new ItemStack(tree.saplingItem()), new ItemStack(tree.leavesItem())), "jei.seedtocellar.growing.tree", tree.climate));
        }
        plants.add(new StationCategories.Growing(new ItemStack(ModItems.HOP_RHIZOME.get()), List.of(new ItemStack(ModItems.HOP_CONES.get())),
                "jei.seedtocellar.growing.hops", io.github.spencerharris192.seedtocellar.farming.Climate.TEMPERATE));
        plants.add(new StationCategories.Growing(new ItemStack(ModItems.VANILLA_POD.get()), List.of(new ItemStack(ModItems.VANILLA_POD.get())),
                "jei.seedtocellar.growing.vanilla", io.github.spencerharris192.seedtocellar.farming.Climate.HOT));
        return plants;
    }

    /**
     * In the dev game only, checks JEI's coverage (GDD section 19): every item and liquid of ours shows at least one way to
     * get it (a recipe or an info page) and one use. The result goes to the log as "[jei audit]".
     */
    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        if (net.neoforged.fml.loading.FMLEnvironment.isProduction()) return;
        var focuses = runtime.getJeiHelpers().getFocusFactory();
        var manager = runtime.getRecipeManager();
        java.util.function.BiPredicate<mezz.jei.api.recipe.RecipeIngredientRole, Object> shown = (role, value) -> {
            mezz.jei.api.recipe.IFocus<?> focus = value instanceof ItemStack stack
                    ? focuses.createFocus(role, mezz.jei.api.constants.VanillaTypes.ITEM_STACK, stack)
                    : focuses.createFocus(role, mezz.jei.api.neoforge.NeoForgeTypes.FLUID_STACK, (net.neoforged.neoforge.fluids.FluidStack) value);
            return manager.createRecipeCategoryLookup().limitFocus(List.of(focus)).get()
                    .anyMatch(category -> manager.createRecipeLookup(category.getRecipeType()).limitFocus(List.of(focus)).get().findAny().isPresent());
        };
        List<String> noSource = new java.util.ArrayList<>();
        List<String> noUse = new java.util.ArrayList<>();
        int items = 0, fluids = 0;
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
            if (!entry.getKey().identifier().getNamespace().equals(SeedToCellar.MOD_ID)) continue;
            items++;
            ItemStack stack = new ItemStack(entry.getValue());
            if (!shown.test(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, stack)) noSource.add(entry.getKey().identifier().getPath());
            boolean consumed = stack.has(net.minecraft.core.component.DataComponents.CONSUMABLE) || entry.getKey().identifier().getPath().endsWith("_bucket");   // eaten, drunk or poured
            if (!consumed && !shown.test(mezz.jei.api.recipe.RecipeIngredientRole.INPUT, stack)
                    && !shown.test(mezz.jei.api.recipe.RecipeIngredientRole.CRAFTING_STATION, stack)) noUse.add(entry.getKey().identifier().getPath());
        }
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.FLUID.entrySet()) {
            var fluid = entry.getValue();
            if (!entry.getKey().identifier().getNamespace().equals(SeedToCellar.MOD_ID) || !fluid.isSource(fluid.defaultFluidState())) continue;
            fluids++;
            var stack = new net.neoforged.neoforge.fluids.FluidStack(fluid, 1000);
            if (!shown.test(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, stack)) noSource.add("liquid " + entry.getKey().identifier().getPath());
        }
        if (noSource.isEmpty()) {
            SeedToCellar.LOGGER.info("[jei audit] OK: all {} items and {} liquids show how to get them", items, fluids);
        } else {
            SeedToCellar.LOGGER.warn("[jei audit] {} with no way to get them in JEI: {}", noSource.size(), noSource);
        }
        if (!noUse.isEmpty()) SeedToCellar.LOGGER.info("[jei audit] {} items show no use in JEI: {}", noUse.size(), noUse);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.MALTING_TUB.get()), JeiTypes.MALTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.KILN.get()), JeiTypes.KILNING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MILLSTONE.get()), JeiTypes.MILLING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.DRYING_RACK.get()), JeiTypes.DRYING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BREW_KETTLE.get()), JeiTypes.COOKING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BREW_KETTLE.get()), JeiTypes.KETTLE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BREW_KETTLE.get()), JeiTypes.MIXING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.FERMENTING_VAT.get()), JeiTypes.FERMENTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.PRESERVING_JAR.get()), JeiTypes.JAR);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CRUSHING_TUB.get()), JeiTypes.CRUSHING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.FRUIT_PRESS.get()), JeiTypes.PRESSING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.POT_STILL.get()), JeiTypes.DISTILLING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SICKLES.get(0).get()), JeiTypes.GROWING);
    }
}
