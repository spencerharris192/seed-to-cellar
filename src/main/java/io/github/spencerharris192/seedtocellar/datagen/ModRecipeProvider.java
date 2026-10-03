package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.farming.Crops;
import net.minecraft.core.registries.BuiltInRegistries;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.brewing.YeastType;
import io.github.spencerharris192.seedtocellar.brewing.RoastLevel;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.brewing.WortData;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.food.PieBlock;
import io.github.spencerharris192.seedtocellar.food.LayerCakeBlock;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Crafting and processing recipes. Ingredients use tags wherever another mod could supply them. */
public class ModRecipeProvider extends RecipeProvider {
    private static final int SECOND = 20;

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    private static void sickle(Consumer<FinishedRecipe> writer, net.minecraft.world.item.Item sickle, Ingredient blade,
                               String unlockName, net.minecraft.advancements.CriterionTriggerInstance unlock) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, sickle)
                .pattern(" MM").pattern("  M").pattern(" S ")
                .define('M', blade)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy(unlockName, unlock)
                .save(writer);
    }

    /**
     * Crushing Tub and Fruit Press (GDD section 7): stomping gives red grapes' must (for red wine) and, at a lower
     * yield, soft fruit's juice; pressing gives every fruit's juice (8 fruit a bucket), olive oil and sorghum
     * juice, and leaves pomace or bagasse behind.
     */
    private static void winery(Consumer<FinishedRecipe> writer) {
        StationRecipes.crushing(writer, "red_grape_must", Ingredient.of(ModTags.Items.fruit("red_grape")), ModFluids.RED_GRAPE_MUST.get(), 125, 2);
        StationRecipes.crushing(writer, "white_grape_juice", Ingredient.of(ModTags.Items.fruit("white_grape")), ModFluids.WHITE_GRAPE_JUICE.get(), 100, 2);
        Map<String, ModFluids.Entry> soft = new java.util.LinkedHashMap<>();
        soft.put("blueberry", ModFluids.BLUEBERRY_JUICE);
        soft.put("blackberry", ModFluids.BLACKBERRY_JUICE);
        soft.put("elderberry", ModFluids.ELDERBERRY_JUICE);
        soft.put("cranberry", ModFluids.CRANBERRY_JUICE);
        soft.put("cherry", ModFluids.CHERRY_JUICE);
        soft.put("plum", ModFluids.PLUM_JUICE);
        soft.put("peach", ModFluids.PEACH_JUICE);
        for (Map.Entry<String, ModFluids.Entry> e : soft.entrySet()) {
            boolean berry = e.getKey().endsWith("berry");
            StationRecipes.crushing(writer, e.getValue().name, Ingredient.of(ModTags.Items.fruit(e.getKey())), e.getValue().get(), 75, berry ? 2 : 3);
        }
        StationRecipes.crushing(writer, "sweet_berry_juice", Ingredient.of(Items.SWEET_BERRIES), ModFluids.SWEET_BERRY_JUICE.get(), 75, 2);
        StationRecipes.crushing(writer, "glow_berry_juice", Ingredient.of(Items.GLOW_BERRIES), ModFluids.GLOW_BERRY_JUICE.get(), 75, 2);
        StationRecipes.crushing(writer, "melon_juice", Ingredient.of(Items.MELON_SLICE), ModFluids.MELON_JUICE.get(), 75, 2);

        StationRecipes.pressing(writer, "red_grape_juice", Ingredient.of(ModTags.Items.fruit("red_grape")), 4, ModFluids.RED_GRAPE_JUICE.get(), 500, ModItems.GRAPE_POMACE.get(), 4);
        StationRecipes.pressing(writer, "white_grape_juice", Ingredient.of(ModTags.Items.fruit("white_grape")), 4, ModFluids.WHITE_GRAPE_JUICE.get(), 500, ModItems.GRAPE_POMACE.get(), 4);
        Map<String, ModFluids.Entry> juicy = new java.util.LinkedHashMap<>(soft);
        juicy.put("apple", ModFluids.APPLE_JUICE);
        juicy.put("pear", ModFluids.PEAR_JUICE);
        juicy.put("orange", ModFluids.ORANGE_JUICE);
        juicy.put("lemon", ModFluids.LEMON_JUICE);
        for (Map.Entry<String, ModFluids.Entry> e : juicy.entrySet()) {
            StationRecipes.pressing(writer, e.getValue().name, Ingredient.of(ModTags.Items.fruit(e.getKey())), 4, e.getValue().get(), 500, ModItems.FRUIT_POMACE.get(), 4);
        }
        StationRecipes.pressing(writer, "sweet_berry_juice", Ingredient.of(Items.SWEET_BERRIES), 4, ModFluids.SWEET_BERRY_JUICE.get(), 500, ModItems.FRUIT_POMACE.get(), 4);
        StationRecipes.pressing(writer, "glow_berry_juice", Ingredient.of(Items.GLOW_BERRIES), 4, ModFluids.GLOW_BERRY_JUICE.get(), 500, ModItems.FRUIT_POMACE.get(), 4);
        StationRecipes.pressing(writer, "melon_juice", Ingredient.of(Items.MELON_SLICE), 4, ModFluids.MELON_JUICE.get(), 500, ModItems.FRUIT_POMACE.get(), 4);
        StationRecipes.pressing(writer, "olive_oil", Ingredient.of(ModTags.Items.fruit("olive")), 4, ModFluids.OLIVE_OIL.get(), 250, ModItems.OLIVE_POMACE.get(), 6);
        StationRecipes.pressing(writer, "sorghum_juice", Ingredient.of(ModTags.Items.crop("sorghum")), 4, ModFluids.SORGHUM_JUICE.get(), 500, ModItems.BAGASSE.get(), 4);

        // Wines (GDD section 10.2): juice or must + wine yeast in the vat. Red wine and mead like it Mild; the rest Cool.
        int wineTime = 36000, ciderTime = 24000;
        StationRecipes.wine(writer, "red_wine", ModFluids.RED_GRAPE_MUST.get(), ModFluids.RED_WINE.get(), Temperature.MILD, wineTime);
        StationRecipes.wine(writer, "white_wine", ModFluids.WHITE_GRAPE_JUICE.get(), ModFluids.WHITE_WINE.get(), Temperature.COOL, wineTime);
        StationRecipes.wine(writer, "rose", ModFluids.RED_GRAPE_JUICE.get(), ModFluids.ROSE.get(), Temperature.COOL, wineTime);
        StationRecipes.wine(writer, "cider", ModFluids.APPLE_JUICE.get(), ModFluids.CIDER.get(), Temperature.COOL, ciderTime);
        StationRecipes.wine(writer, "perry", ModFluids.PEAR_JUICE.get(), ModFluids.PERRY.get(), Temperature.COOL, ciderTime);
        StationRecipes.wine(writer, "mead", ModFluids.HONEY_WATER.get(), ModFluids.MEAD.get(), Temperature.MILD, wineTime);
        Map<ModFluids.Entry, ModFluids.Entry> fruitWines = new java.util.LinkedHashMap<>();
        fruitWines.put(ModFluids.CHERRY_JUICE, ModFluids.CHERRY_WINE);
        fruitWines.put(ModFluids.PLUM_JUICE, ModFluids.PLUM_WINE);
        fruitWines.put(ModFluids.PEACH_JUICE, ModFluids.PEACH_WINE);
        fruitWines.put(ModFluids.BLUEBERRY_JUICE, ModFluids.BLUEBERRY_WINE);
        fruitWines.put(ModFluids.BLACKBERRY_JUICE, ModFluids.BLACKBERRY_WINE);
        fruitWines.put(ModFluids.ELDERBERRY_JUICE, ModFluids.ELDERBERRY_WINE);
        fruitWines.put(ModFluids.CRANBERRY_JUICE, ModFluids.CRANBERRY_WINE);
        fruitWines.put(ModFluids.SWEET_BERRY_JUICE, ModFluids.SWEET_BERRY_WINE);
        fruitWines.put(ModFluids.GLOW_BERRY_JUICE, ModFluids.GLOW_BERRY_WINE);
        fruitWines.put(ModFluids.MELON_JUICE, ModFluids.MELON_WINE);
        fruitWines.forEach((juice, wine) -> StationRecipes.wine(writer, wine.name, juice.get(), wine.get(), Temperature.COOL, wineTime));
        // Honey water for mead: 2 honey bottles stirred into each bucket of water in the kettle.
        StationRecipes.mixing(writer, "honey_water", FluidTags.WATER, Ingredient.of(Items.HONEY_BOTTLE), 2, ModFluids.HONEY_WATER.get(), 400);
        StationRecipes.mixing(writer, "lemonade", ModTags.Fluids.LEMON_JUICE, Ingredient.of(ModTags.Items.SWEETENERS), 2,
                ModFluids.LEMONADE.get(), 400);
        // Dark glass wine bottles.
        ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModItems.WINE_BOTTLE.get(), 3)
                .pattern(" G ").pattern("G G").pattern("GDG")
                .define('G', Tags.Items.GLASS_COLORLESS).define('D', Tags.Items.DYES_GREEN)
                .unlockedBy("has_juice", has(ModTags.Items.FRUITS)).save(writer);

        // Pressed sugar-cane and sorghum fiber makes paper, like the cane itself.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("BBB").define('B', ModItems.BAGASSE.get())
                .unlockedBy("has_bagasse", has(ModItems.BAGASSE.get())).save(writer, SeedToCellar.id("paper_from_bagasse"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.CRUSHING_TUB.get())
                .pattern("P P").pattern("PSP").pattern("III")
                .define('P', ItemTags.PLANKS).define('S', ItemTags.WOODEN_SLABS).define('I', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_grapes", has(ModTags.Items.GRAPES)).save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.FRUIT_PRESS.get())
                .pattern("LIL").pattern("LFL").pattern("SSS")
                .define('L', ItemTags.LOGS).define('I', Tags.Items.INGOTS_IRON).define('F', Items.OAK_FENCE)
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_fruit", has(ModTags.Items.FRUITS)).save(writer);
    }

    /** The Pot Still, its bottles, the washes and the spirits they distil into (GDD section 9.4). */
    private void distillery(Consumer<FinishedRecipe> writer) {
        // The still: a Brew Kettle with a copper head and swan neck.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POT_STILL.get())
                .pattern(" CC").pattern(" C ").pattern("CKC")
                .define('C', Tags.Items.INGOTS_COPPER).define('K', ModItems.BREW_KETTLE.get())
                .unlockedBy("has_kettle", has(ModItems.BREW_KETTLE.get())).save(writer);
        // Squat clear spirit bottles, stoppered with a wooden button.
        ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ModItems.SPIRIT_BOTTLE.get(), 4)
                .pattern(" B ").pattern("G G").pattern("GGG")
                .define('G', Tags.Items.GLASS_COLORLESS).define('B', ItemTags.WOODEN_BUTTONS)
                .unlockedBy("has_still", has(ModItems.POT_STILL.get())).save(writer);

        // Washes: corn or potato mashed with malt, fermented like ale (Plain Ale is the malt wash).
        Fluid sweet = ModFluids.SWEET_WORT.get();
        StationRecipes.wash(writer, "corn_wash", sweet, ModFluids.CORN_WASH.get(), MaltType.CORN, 24000);
        StationRecipes.wash(writer, "potato_wash", sweet, ModFluids.POTATO_WASH.get(), MaltType.POTATO, 24000);

        // Each wash's spirit; a charcoal filter (or a third run of grain spirit) makes vodka; any spirit can run again.
        String vodkaSources = "#" + ModTags.Fluids.VODKA_SOURCES.location();
        StationRecipes.distilling(writer, "malt_whiskey", "seedtocellar:plain_ale", ModFluids.MALT_WHISKEY.get(), 0, false, 0);
        StationRecipes.distilling(writer, "bourbon", "seedtocellar:corn_wash", ModFluids.BOURBON.get(), 0, false, 0);
        StationRecipes.distilling(writer, "vodka", "seedtocellar:potato_wash", ModFluids.VODKA.get(), 0, false, 0);
        StationRecipes.distilling(writer, "vodka_filtered", vodkaSources, ModFluids.VODKA.get(), 0, true, 10);
        StationRecipes.distilling(writer, "vodka_third_run", "#" + ModTags.Fluids.GRAIN_SPIRITS.location(), ModFluids.VODKA.get(), 2, false, 5);
        StationRecipes.distilling(writer, "run_again", "#" + ModTags.Fluids.SPIRITS.location(), null, 0, false, -10);
        // Liqueurs and bitters (GDD section 10.4): a spirit steeped in the jar with fruit or herbs, keeping its stars.
        String bases = "#" + ModTags.Fluids.LIQUEUR_BASES.location();
        Ingredient sugar = Ingredient.of(ModTags.Items.SWEETENERS);
        Ingredient lemonPeel = Ingredient.of(ModItems.LEMON_PEEL.get());
        Ingredient orangePeel = Ingredient.of(ModItems.ORANGE_PEEL.get());
        StationRecipes.steep(writer, "limoncello", List.of(lemonPeel, lemonPeel, sugar), bases, ModFluids.LIMONCELLO.get(), 24000);
        StationRecipes.steep(writer, "orange_liqueur", List.of(orangePeel, orangePeel, sugar), bases, ModFluids.ORANGE_LIQUEUR.get(), 24000);
        Ingredient plum = Ingredient.of(ModTags.Items.fruit("plum"));
        StationRecipes.steep(writer, "umeshu", List.of(plum, plum, sugar), bases, ModFluids.UMESHU.get(), 24000);
        Ingredient cherry = Ingredient.of(ModTags.Items.fruit("cherry"));
        StationRecipes.steep(writer, "cherry_liqueur", List.of(cherry, cherry, sugar), bases, ModFluids.CHERRY_LIQUEUR.get(), 24000);
        Ingredient blackberry = Ingredient.of(ModTags.Items.crop("blackberry"));
        StationRecipes.steep(writer, "creme_de_mure", List.of(blackberry, blackberry, sugar), bases, ModFluids.CREME_DE_MURE.get(), 24000);
        Ingredient elderflower = Ingredient.of(Crops.ELDERBERRY.flowers());
        StationRecipes.steep(writer, "elderflower_liqueur", List.of(elderflower, elderflower, sugar), bases, ModFluids.ELDERFLOWER_LIQUEUR.get(), 24000);
        StationRecipes.steep(writer, "herbal_liqueur", List.of(Ingredient.of(ModTags.Items.crop("mint")), Ingredient.of(ModTags.Items.crop("lavender")),
                Ingredient.of(ModTags.Items.crop("anise")), Ingredient.of(Items.HONEY_BOTTLE)), "seedtocellar:brandy", ModFluids.HERBAL_LIQUEUR.get(), 48000);
        StationRecipes.steep(writer, "spiced_rum", List.of(Ingredient.of(ModItems.CURED_VANILLA.get()), orangePeel,
                Ingredient.of(ModTags.Items.crop("ginger"))), "seedtocellar:rum", ModFluids.SPICED_RUM.get(), 48000);
        StationRecipes.steep(writer, "aromatic_bitters", List.of(Ingredient.of(ModTags.Items.crop("wormwood")), Ingredient.of(ModTags.Items.crop("anise")),
                orangePeel), "seedtocellar:vodka", ModFluids.AROMATIC_BITTERS.get(), 48000);
        // Apple Crown Whiskey: malt whiskey steeped with apples and honey. (And, unlisted, with a golden apple: crowned, if perfect.)
        Ingredient honey = Ingredient.of(Items.HONEY_BOTTLE);
        Ingredient apple = Ingredient.of(ModTags.Items.fruit("apple"));
        StationRecipes.steep(writer, "apple_crown_whiskey", List.of(apple, apple, honey), "seedtocellar:malt_whiskey",
                ModFluids.APPLE_CROWN_WHISKEY.get(), 24000);
        StationRecipes.steep(writer, "apple_crown_whiskey_golden", List.of(Ingredient.of(Items.GOLDEN_APPLE), honey), "seedtocellar:malt_whiskey",
                ModFluids.APPLE_CROWN_WHISKEY.get(), 24000, true);
        // Gin: vodka run through the Gin Basket with juniper and at least two other botanicals (three more for the star).
        StationRecipes.distillingWithBasket(writer, "gin", "seedtocellar:vodka", ModFluids.GIN.get(),
                Ingredient.of(ModTags.Items.crop("juniper")), 3, 20);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GIN_BASKET.get())
                .pattern(" C ").pattern("CBC").pattern(" C ")
                .define('C', Tags.Items.INGOTS_COPPER).define('B', Items.IRON_BARS)
                .unlockedBy("has_still", has(ModItems.POT_STILL.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.LEMON_PEEL.get(), 2).requires(ModTags.Items.fruit("lemon"))
                .unlockedBy("has_lemon", has(ModTags.Items.fruit("lemon"))).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.ORANGE_PEEL.get(), 2).requires(ModTags.Items.fruit("orange"))
                .unlockedBy("has_orange", has(ModTags.Items.fruit("orange"))).save(writer);

        // Brandy from grape wine; a fruit brandy from each fruit wine with a famous eau-de-vie (GDD section 10.3).
        StationRecipes.distilling(writer, "brandy", "#" + ModTags.Fluids.GRAPE_WINES.location(), ModFluids.BRANDY.get(), 0, false, 0);
        StationRecipes.distilling(writer, "apple_brandy", "seedtocellar:cider", ModFluids.APPLE_BRANDY.get(), 0, false, 0);
        StationRecipes.distilling(writer, "pear_brandy", "seedtocellar:perry", ModFluids.PEAR_BRANDY.get(), 0, false, 0);
        StationRecipes.distilling(writer, "kirsch", "seedtocellar:cherry_wine", ModFluids.KIRSCH.get(), 0, false, 0);
        StationRecipes.distilling(writer, "slivovitz", "seedtocellar:plum_wine", ModFluids.SLIVOVITZ.get(), 0, false, 0);
        StationRecipes.distilling(writer, "peach_brandy", "seedtocellar:peach_wine", ModFluids.PEACH_BRANDY.get(), 0, false, 0);
        // Grappa: grape pomace steeped in hot water (4 a bucket), fermented with wine yeast, distilled.
        StationRecipes.mixing(writer, "pomace_mash", FluidTags.WATER, Ingredient.of(ModItems.GRAPE_POMACE.get()), 4,
                ModFluids.POMACE_MASH.get(), 400);
        StationRecipes.fruitWash(writer, "pomace_wash", ModFluids.POMACE_MASH.get(), ModFluids.POMACE_WASH.get(), 24000);
        StationRecipes.distilling(writer, "grappa", "seedtocellar:pomace_wash", ModFluids.GRAPPA.get(), 0, false, 0);

        // Sake (GDD section 9.3): rice polished in the Millstone (leaving bran), steamed in the kettle, grown into koji in a
        // warm jar (faster with some koji already), stirred into water as a mash, fermented cool with wine yeast.
        StationRecipes.milling(writer, "polished_rice", Ingredient.of(ModTags.Items.crop("rice")), ModItems.POLISHED_RICE.get(),
                ModItems.RICE_BRAN.get(), 1);
        Ingredient steamed = Ingredient.of(ModItems.STEAMED_RICE.get());
        StationRecipes.jarAt(writer, "koji_rice", List.of(steamed, steamed), null, 0, ModItems.KOJI_RICE.get(), 2, 48000, Temperature.WARM);
        StationRecipes.jarAt(writer, "koji_rice_from_koji", List.of(steamed, steamed, Ingredient.of(ModItems.KOJI_RICE.get())), null, 0,
                ModItems.KOJI_RICE.get(), 3, 24000, Temperature.WARM);
        StationRecipes.mixing(writer, "sake_mash", FluidTags.WATER, Ingredient.of(ModItems.KOJI_RICE.get()), 2, ModFluids.SAKE_MASH.get(), 400);
        StationRecipes.wine(writer, "sake", ModFluids.SAKE_MASH.get(), ModFluids.SAKE.get(), Temperature.COOL, 36000);
        // Coffee: green beans roasted in the kiln, ground, brewed in the kettle; coffee liqueur steeps in the jar.
        StationRecipes.kilning(writer, "roasted_coffee", Ingredient.of(ModTags.Items.crop("coffee")), RoastLevel.MEDIUM,
                ModItems.ROASTED_COFFEE.get(), 800);
        StationRecipes.milling(writer, "ground_coffee", Ingredient.of(ModItems.ROASTED_COFFEE.get()), ModItems.GROUND_COFFEE.get(), 1);
        Ingredient ground = Ingredient.of(ModItems.GROUND_COFFEE.get());
        StationRecipes.steep(writer, "coffee_liqueur", List.of(ground, ground, Ingredient.of(ModTags.Items.SWEETENERS)),
                "#" + ModTags.Fluids.LIQUEUR_BASES.location(), ModFluids.COFFEE_LIQUEUR.get(), 24000);
        // Ginger beer: ginger, sugar and ale yeast in a jar of water.
        StationRecipes.steep(writer, "ginger_beer", List.of(Ingredient.of(ModTags.Items.crop("ginger")), Ingredient.of(ModTags.Items.SWEETENERS),
                Ingredient.of(ModItems.ALE_YEAST.get())), "minecraft:water", ModFluids.GINGER_BEER.get(), 24000);
        // Lager yeast: ale yeast cultured in a cold jar, on sweet wort or sugared water.
        StationRecipes.jarAt(writer, "lager_yeast", List.of(Ingredient.of(ModItems.ALE_YEAST.get())), ModFluids.SWEET_WORT.get(), 250,
                ModItems.LAGER_YEAST.get(), 2, 24000, Temperature.COLD);
        StationRecipes.jarAt(writer, "lager_yeast_from_sugar", List.of(Ingredient.of(ModItems.ALE_YEAST.get()), Ingredient.of(ModTags.Items.SWEETENERS)),
                net.minecraft.world.level.material.Fluids.WATER, 250, ModItems.LAGER_YEAST.get(), 2, 24000, Temperature.COLD);

        // Rum: sugar cane pressed, the juice boiled down to molasses, fermented and distilled.
        StationRecipes.pressing(writer, "cane_juice", Ingredient.of(Items.SUGAR_CANE), 4, ModFluids.CANE_JUICE.get(), 500, ModItems.BAGASSE.get(), 4);
        StationRecipes.boilDown(writer, "molasses", ModFluids.CANE_JUICE.get(), ModFluids.MOLASSES.get(), 600);
        StationRecipes.distillersWash(writer, "rum_wash", ModFluids.MOLASSES.get(), ModFluids.RUM_WASH.get(), YeastType.ALE, 24000);
        StationRecipes.distilling(writer, "rum", "seedtocellar:rum_wash", ModFluids.RUM.get(), 0, false, 0);
        // Tequila: the agave's heart roasted, pressed, the juice fermented and distilled.
        StationRecipes.kilning(writer, "roasted_agave", Ingredient.of(ModTags.Items.crop("agave")), RoastLevel.MEDIUM, ModItems.ROASTED_AGAVE.get(), 1200);
        StationRecipes.pressing(writer, "agave_juice", Ingredient.of(ModItems.ROASTED_AGAVE.get()), 4, ModFluids.AGAVE_JUICE.get(), 500,
                ModItems.AGAVE_FIBER.get(), 6);
        StationRecipes.distillersWash(writer, "agave_wash", ModFluids.AGAVE_JUICE.get(), ModFluids.AGAVE_WASH.get(), YeastType.ALE, 24000);
        StationRecipes.distilling(writer, "tequila", "seedtocellar:agave_wash", ModFluids.TEQUILA.get(), 0, false, 0);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STRING, 2)
                .requires(ModItems.AGAVE_FIBER.get()).requires(ModItems.AGAVE_FIBER.get()).requires(ModItems.AGAVE_FIBER.get())
                .unlockedBy("has_fiber", has(ModItems.AGAVE_FIBER.get())).save(writer, SeedToCellar.id("string_from_agave_fiber"));
    }

    /**
     * Other mods' machines (GDD section 22.2; each recipe loads only with its mod): Create mashes and hops our worts; IE's
     * Garden Cloche and Botany Pots grow every crop (and Botany Pots our fruit trees, hops and vanilla too). The Millstone,
     * press, tub and kettle recipes get their Create, Mekanism and IE twins in StationRecipes.
     */
    /** The tavern set, in each cask wood: counters from slabs and planks, stools from a slab on sticks under leather. */
    private static void tavern(Consumer<FinishedRecipe> writer) {
        for (CaskWood wood : CaskWood.values()) {
            var planks = BuiltInRegistries.ITEM.get(SeedToCellar.rl("minecraft", wood.id() + "_planks"));
            var slab = BuiltInRegistries.ITEM.get(SeedToCellar.rl("minecraft", wood.id() + "_slab"));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.BAR_COUNTERS.get(wood).get(), 4)
                    .pattern("SSS").pattern("P P").pattern("PPP").define('S', slab).define('P', planks)
                    .unlockedBy("has_planks", has(planks)).save(writer);
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.BAR_STOOLS.get(wood).get(), 2)
                    .pattern(" L ").pattern(" S ").pattern("/ /").define('L', Tags.Items.LEATHER).define('S', slab)
                    .define('/', Tags.Items.RODS_WOODEN).unlockedBy("has_slab", has(slab)).save(writer);
        }
    }

    /** The tavern's wall pieces: a peg board, a hop garland, a sign on its bracket. */
    private static void tavernWall(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.MUG_RACK.get()).pattern("PPP").pattern("/ /")
                .define('P', ItemTags.PLANKS).define('/', Tags.Items.RODS_WOODEN).unlockedBy("has_mug", has(ModItems.MUG.get())).save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.HOP_GARLAND.get(), 3).pattern("S S").pattern("HHH")
                .define('S', Tags.Items.STRING).define('H', ModTags.Items.crop("hops")).unlockedBy("has_hops", has(ModItems.HOP_CONES.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.TAVERN_SIGN.get()).pattern("NNN").pattern("PPP").pattern("PPP")
                .define('N', Tags.Items.NUGGETS_IRON).define('P', ItemTags.PLANKS).unlockedBy("has_mug", has(ModItems.MUG.get())).save(writer);
    }

    /** Hanging bundles: nine of the crop tie into one, and untie back into nine. */
    private static void bundles(Consumer<FinishedRecipe> writer) {
        bundleRecipe(writer, ModItems.HOP_BUNDLE.get(), ModItems.HOP_CONES.get(), Ingredient.of(ModTags.Items.crop("hops")));
        bundleRecipe(writer, ModItems.LAVENDER_BUNDLE.get(), Crops.LAVENDER.produce(), Ingredient.of(ModTags.Items.crop("lavender")));
        bundleRecipe(writer, ModItems.GARLIC_BRAID.get(), Crops.GARLIC.produce(), Ingredient.of(ModTags.Items.crop("garlic")));
        bundleRecipe(writer, ModItems.CHILI_STRING.get(), Crops.CHILI.produce(), Ingredient.of(ModTags.Items.crop("chili")));
    }

    private static void bundleRecipe(Consumer<FinishedRecipe> writer, ItemLike bundle, ItemLike crop, Ingredient crops) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, bundle).pattern("###").pattern("###").pattern("###").define('#', crops)
                .unlockedBy("has_crop", has(crop)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, crop, 9).requires(bundle).unlockedBy("has_bundle", has(bundle))
                .save(writer, SeedToCellar.id(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(crop.asItem()).getPath() + "_from_"
                        + net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(bundle.asItem()).getPath()));
    }

    private static void compat(Consumer<FinishedRecipe> writer) {
        java.util.Map<String, java.util.Map<ItemLike, Integer>> bills = new java.util.LinkedHashMap<>();
        bills.put("pale", bill(ModItems.PALE_GRIST.get(), 2));
        bills.put("amber", bill(ModItems.PALE_GRIST.get(), 1, ModItems.AMBER_GRIST.get(), 1));
        bills.put("dark", bill(ModItems.PALE_GRIST.get(), 1, ModItems.BLACK_GRIST.get(), 1));
        bills.put("strong_amber", bill(ModItems.PALE_GRIST.get(), 2, ModItems.AMBER_GRIST.get(), 2));
        bills.put("wheat", bill(ModItems.PALE_GRIST.get(), 1, ModItems.WHEAT_GRIST.get(), 1));
        CompatRecipes.almanac(writer, SeedToCellar.MOD_ID + ":" + ModBookProvider.BOOK);
        CompatRecipes.worts(writer, bills, ModItems.SPENT_GRAIN.get(), ModItems.DRIED_HOPS.get(), Fluids.WATER,
                ModFluids.SWEET_WORT.get(), ModFluids.HOPPED_WORT.get());

        for (io.github.spencerharris192.seedtocellar.farming.Crop crop : Crops.all()) {
            boolean ownSeed = crop.seeds() != crop.produce();
            java.util.List<com.google.gson.JsonObject> grown = new java.util.ArrayList<>();
            grown.add(CompatRecipes.itemResult(crop.produce(), 2));
            if (ownSeed) grown.add(CompatRecipes.itemResult(crop.seeds(), 1));
            var style = crop.style;
            boolean desert = style == io.github.spencerharris192.seedtocellar.farming.Crop.Style.SUCCULENT;
            boolean paddy = style == io.github.spencerharris192.seedtocellar.farming.Crop.Style.PADDY;
            CompatRecipes.cloche(writer, crop.name, crop.seeds(), crop.block(), desert ? "forge:sand" : "minecraft:dirt", desert ? 1600 : 800, grown);
            java.util.List<com.google.gson.JsonObject> drops = new java.util.ArrayList<>();
            drops.add(CompatRecipes.potDrop(crop.produce(), 1F, 1, 2));
            drops.add(ownSeed ? CompatRecipes.potDrop(crop.seeds(), 0.05F, 1, 1) : CompatRecipes.potDrop(crop.produce(), 0.15F, 1, 2));
            CompatRecipes.botanyPot(writer, crop.name, crop.seeds(), desert ? List.of("sand") : paddy ? List.of("water") : List.of("dirt", "farmland"),
                    crop.block(), true, desert ? 2400 : style == io.github.spencerharris192.seedtocellar.farming.Crop.Style.ROW ? 1200 : 1600, drops);
        }
        // Hops on their trellis, and vanilla on jungle wood like cocoa
        CompatRecipes.cloche(writer, "hops", ModItems.HOP_RHIZOME.get(), ModBlocks.HOPS.get(), "minecraft:dirt", 800,
                List.of(CompatRecipes.itemResult(ModItems.HOP_CONES.get(), 2)));
        CompatRecipes.botanyPot(writer, "hops", ModItems.HOP_RHIZOME.get(), List.of("dirt", "farmland"), ModBlocks.HOPS.get(), true, 1600,
                List.of(CompatRecipes.potDrop(ModItems.HOP_CONES.get(), 1F, 1, 2), CompatRecipes.potDrop(ModItems.HOP_RHIZOME.get(), 0.05F, 1, 1)));
        CompatRecipes.botanyPot(writer, "vanilla", ModItems.VANILLA_POD.get(), List.of("jungle_wood"), ModBlocks.VANILLA.get(), true, 1600,
                List.of(CompatRecipes.potDrop(ModItems.VANILLA_POD.get(), 1F, 1, 2)));
        // The fruit trees, as a sapling in a pot that fruits
        for (io.github.spencerharris192.seedtocellar.farming.FruitTree tree : io.github.spencerharris192.seedtocellar.farming.FruitTrees.all()) {
            CompatRecipes.botanyPot(writer, tree.name + "_tree", tree.saplingItem(), List.of("dirt"), tree.sapling(), false, 2400,
                    List.of(CompatRecipes.potDrop(tree.fruit(), 1F, 1, 2), CompatRecipes.potDrop(tree.saplingItem(), 0.05F, 1, 1),
                            CompatRecipes.potDrop(Items.STICK, 0.1F, 1, 2)));
        }
    }

    private static java.util.Map<ItemLike, Integer> bill(ItemLike a, int n) {
        java.util.Map<ItemLike, Integer> bill = new java.util.LinkedHashMap<>();
        bill.put(a, n);
        return bill;
    }

    private static java.util.Map<ItemLike, Integer> bill(ItemLike a, int n, ItemLike b, int m) {
        java.util.Map<ItemLike, Integer> bill = bill(a, n);
        bill.put(b, m);
        return bill;
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        crafting(writer);
        compat(writer);
        bundles(writer);
        tavern(writer);
        tavernWall(writer);

        // Malting Tub: barley steeps 2 min, then sprouts 3 min (before the config multiplier).
        StationRecipes.malting(writer, "barley", Ingredient.of(ModTags.Items.CROPS_BARLEY), ModItems.GREEN_BARLEY_MALT.get(), 120 * SECOND, 180 * SECOND);
        StationRecipes.malting(writer, "wheat", Ingredient.of(Tags.Items.CROPS_WHEAT), ModItems.GREEN_WHEAT_MALT.get(), 120 * SECOND, 180 * SECOND);
        StationRecipes.kilning(writer, "wheat_malt", Ingredient.of(ModItems.GREEN_WHEAT_MALT.get()), RoastLevel.LIGHT, ModItems.WHEAT_MALT.get(), 30 * SECOND);
        StationRecipes.milling(writer, "wheat_grist", Ingredient.of(ModItems.WHEAT_MALT.get()), ModItems.WHEAT_GRIST.get(), 1);

        // Kiln: longer, hotter roasts make darker malt; hops dry on the lightest setting.
        Ingredient greenMalt = Ingredient.of(ModItems.GREEN_BARLEY_MALT.get());
        StationRecipes.kilning(writer, "pale_malt", greenMalt, RoastLevel.LIGHT, ModItems.PALE_MALT.get(), 30 * SECOND);
        StationRecipes.kilning(writer, "amber_malt", greenMalt, RoastLevel.MEDIUM, ModItems.AMBER_MALT.get(), 60 * SECOND);
        StationRecipes.kilning(writer, "black_malt", greenMalt, RoastLevel.DARK, ModItems.BLACK_MALT.get(), 120 * SECOND);
        StationRecipes.kilning(writer, "dried_hops", Ingredient.of(ModTags.Items.CROPS_HOPS), RoastLevel.LIGHT, ModItems.DRIED_HOPS.get(), 20 * SECOND);

        // Drying Rack: much slower than the kiln but free; times are at the normal rate (sun halves them, rain pauses).
        // Hops half a day, chili three quarters, jerky a whole day (one in-game day = 20 minutes).
        StationRecipes.drying(writer, "dried_hops", Ingredient.of(ModTags.Items.CROPS_HOPS), ModItems.DRIED_HOPS.get(), 600 * SECOND);
        StationRecipes.drying(writer, "dried_chili", Ingredient.of(ModTags.Items.crop("chili")), ModItems.DRIED_CHILI.get(), 900 * SECOND);
        StationRecipes.drying(writer, "cured_vanilla", Ingredient.of(ModItems.VANILLA_POD.get()), ModItems.CURED_VANILLA.get(), 900 * SECOND);
        StationRecipes.drying(writer, "jerky", Ingredient.of(ModTags.Items.JERKY_MEATS), ModItems.JERKY.get(), 1200 * SECOND);
        StationRecipes.drying(writer, "dried_berries", Ingredient.of(ModTags.Items.DRYABLE_BERRIES), ModItems.DRIED_BERRIES.get(), 900 * SECOND);
        winery(writer);
        distillery(writer);
        StationRecipes.drying(writer, "raisins", Ingredient.of(ModTags.Items.fruit("red_grape")), ModItems.RAISINS.get(), 900 * SECOND);
        StationRecipes.drying(writer, "golden_raisins", Ingredient.of(ModTags.Items.fruit("white_grape")), ModItems.GOLDEN_RAISINS.get(), 900 * SECOND);
        StationRecipes.drying(writer, "prunes", Ingredient.of(ModTags.Items.fruit("plum")), ModItems.PRUNES.get(), 900 * SECOND);
        StationRecipes.drying(writer, "dried_cherries", Ingredient.of(ModTags.Items.fruit("cherry")), ModItems.DRIED_CHERRIES.get(), 900 * SECOND);
        StationRecipes.drying(writer, "dried_apples", Ingredient.of(ModTags.Items.fruit("apple")), ModItems.DRIED_APPLES.get(), 900 * SECOND);
        StationRecipes.drying(writer, "dried_peaches", Ingredient.of(ModTags.Items.fruit("peach")), ModItems.DRIED_PEACHES.get(), 900 * SECOND);
        StationRecipes.drying(writer, "dried_pears", Ingredient.of(ModTags.Items.fruit("pear")), ModItems.DRIED_PEARS.get(), 900 * SECOND);

        // Millstone: one crank per item.
        StationRecipes.milling(writer, "pale_grist", Ingredient.of(ModItems.PALE_MALT.get()), ModItems.PALE_GRIST.get(), 1);
        StationRecipes.milling(writer, "amber_grist", Ingredient.of(ModItems.AMBER_MALT.get()), ModItems.AMBER_GRIST.get(), 1);
        StationRecipes.milling(writer, "black_grist", Ingredient.of(ModItems.BLACK_MALT.get()), ModItems.BLACK_GRIST.get(), 1);
        StationRecipes.milling(writer, "wheat_flour", Ingredient.of(Tags.Items.CROPS_WHEAT), ModItems.WHEAT_FLOUR.get(), 1);
        StationRecipes.milling(writer, "rye_flour", Ingredient.of(ModTags.Items.crop("rye")), ModItems.RYE_FLOUR.get(), 1);
        StationRecipes.milling(writer, "cornmeal", Ingredient.of(ModTags.Items.crop("corn")), ModItems.CORNMEAL.get(), 1);
        StationRecipes.milling(writer, "rolled_oats", Ingredient.of(ModTags.Items.crop("oats")), ModItems.ROLLED_OATS.get(), 1);

        baking(writer);
        cooking(writer);

        beerStyles(writer);

        // Preserving Jar cultures (half an in-game day each, before the fermentation multiplier).
        Ingredient flour = Ingredient.of(ModTags.Items.FLOUR_WHEAT);
        StationRecipes.jar(writer, "sourdough_starter", List.of(flour, flour), Fluids.WATER, 250, ModItems.SOURDOUGH_STARTER.get(), 1, 12000);
        StationRecipes.jar(writer, "ale_yeast", List.of(Ingredient.of(ModItems.SOURDOUGH_STARTER.get())),
                ModFluids.SWEET_WORT.get(), 250, ModItems.ALE_YEAST.get(), 2, 12000);

        // Preserves: a day with the lid on (GDD section 9.5). Vinegar itself comes from beer left open (the jar's own rule).
        Ingredient cucumber = Ingredient.of(ModTags.Items.crop("cucumber"));
        Ingredient cabbage = Ingredient.of(ModTags.Items.crop("cabbage"));
        Ingredient garlic = Ingredient.of(ModTags.Items.crop("garlic"));
        StationRecipes.jar(writer, "pickles", List.of(cucumber, cucumber, garlic, Ingredient.of(ModTags.Items.crop("coriander"))),
                ModFluids.VINEGAR.get(), 250, ModItems.PICKLES.get(), 4, 24000);
        StationRecipes.jar(writer, "sauerkraut", List.of(cabbage, cabbage), Fluids.WATER, 250, ModItems.SAUERKRAUT.get(), 3, 24000);
        Ingredient olives = Ingredient.of(ModTags.Items.fruit("olive"));
        StationRecipes.jar(writer, "cured_olives", List.of(olives, olives, olives), Fluids.WATER, 250, ModItems.CURED_OLIVES.get(), 3, 24000);
        StationRecipes.jar(writer, "kimchi", List.of(cabbage, Ingredient.of(ModTags.Items.CHILIES), garlic,
                Ingredient.of(ModTags.Items.crop("ginger"))), Fluids.WATER, 250, ModItems.KIMCHI.get(), 3, 24000);
    }

    /**
     * Beer styles (GDD section 10.1). Checked from highest priority down; Table Beer catches
     * everything else so any wort always makes a drink. Ales ferment best at Mild.
     */
    private void beerStyles(Consumer<FinishedRecipe> writer) {
        Fluid hopped = ModFluids.HOPPED_WORT.get();
        Fluid sweet = ModFluids.SWEET_WORT.get();
        int day = 24000;
        Temperature mild = Temperature.MILD;
        var light = WortData.Strength.LIGHT;
        var normal = WortData.Strength.NORMAL;
        var strong = WortData.Strength.STRONG;

        // Lager (lager yeast only, cold, pale malt) and wheat beer (half or more wheat malt).
        StationRecipes.fermentingWith(writer, "lager", hopped, ModFluids.LAGER.get(), YeastType.LAGER, false, Temperature.COLD, normal, strong,
                Map.of(), Map.of(MaltType.AMBER, 0.2F, MaltType.BLACK, 0F), (int) (day * 1.5), 60);
        StationRecipes.fermenting(writer, "wheat_beer", hopped, ModFluids.WHEAT_BEER.get(), mild, normal, strong,
                Map.of(MaltType.WHEAT, 0.5F), Map.of(MaltType.BLACK, 0F), day, 45);
        StationRecipes.fermenting(writer, "stout", hopped, ModFluids.STOUT.get(), mild, normal, strong,
                Map.of(MaltType.BLACK, 0.25F), Map.of(), day, 50);
        StationRecipes.fermenting(writer, "old_ale", hopped, ModFluids.OLD_ALE.get(), mild, strong, strong,
                Map.of(MaltType.AMBER, 0.25F), Map.of(MaltType.BLACK, 0.2F), day, 40);
        StationRecipes.fermenting(writer, "amber_ale", hopped, ModFluids.AMBER_ALE.get(), mild, normal, strong,
                Map.of(MaltType.AMBER, 0.25F), Map.of(MaltType.BLACK, 0.2F), day, 30);
        StationRecipes.fermenting(writer, "pale_ale", hopped, ModFluids.PALE_ALE.get(), mild, normal, strong,
                Map.of(), Map.of(MaltType.AMBER, 0.2F, MaltType.BLACK, 0F), day, 20);
        StationRecipes.fermenting(writer, "plain_ale", sweet, ModFluids.PLAIN_ALE.get(), mild, normal, strong,
                Map.of(), Map.of(), day, 20);
        StationRecipes.fermenting(writer, "table_beer_from_hopped_wort", hopped, ModFluids.TABLE_BEER.get(), mild, light, strong,
                Map.of(), Map.of(), day, 0);
        StationRecipes.fermenting(writer, "table_beer_from_sweet_wort", sweet, ModFluids.TABLE_BEER.get(), mild, light, strong,
                Map.of(), Map.of(), day, 0);
    }

    /** Doughs by hand, then the furnace or smoker bakes them (GDD section 9.6). */
    private void baking(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.DOUGH.get(), 3)
                .requires(ModTags.Items.FLOUR_WHEAT).requires(ModTags.Items.FLOUR_WHEAT).requires(ModTags.Items.FLOUR_WHEAT)
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_flour", has(ModTags.Items.FLOUR_WHEAT)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.RYE_DOUGH.get(), 3)
                .requires(ModTags.Items.FLOUR_RYE).requires(ModTags.Items.FLOUR_RYE).requires(ModTags.Items.FLOUR_RYE)
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_rye_flour", has(ModTags.Items.FLOUR_RYE)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SOURDOUGH_DOUGH.get())
                .requires(ModTags.Items.DOUGH_WHEAT).requires(ModItems.SOURDOUGH_STARTER.get())
                .unlockedBy("has_starter", has(ModItems.SOURDOUGH_STARTER.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SPENT_GRAIN_DOUGH.get())
                .requires(ModTags.Items.DOUGH_WHEAT).requires(ModItems.SPENT_GRAIN.get())
                .unlockedBy("has_spent_grain", has(ModItems.SPENT_GRAIN.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BEER_BREAD_DOUGH.get(), 2)
                .requires(ModTags.Items.FLOUR_WHEAT).requires(ModTags.Items.FLOUR_WHEAT).requires(ModTags.Items.ALES)
                .unlockedBy("has_ale", has(ModTags.Items.ALES)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.CORNBREAD_BATTER.get(), 2)
                .requires(ModTags.Items.CORNMEAL).requires(ModTags.Items.CORNMEAL).requires(Tags.Items.EGGS).requires(Items.MILK_BUCKET)
                .unlockedBy("has_cornmeal", has(ModTags.Items.CORNMEAL)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.MASA.get(), 2)
                .requires(ModTags.Items.CORNMEAL).requires(ModTags.Items.CORNMEAL).requires(Items.WATER_BUCKET)
                .unlockedBy("has_cornmeal", has(ModTags.Items.CORNMEAL)).save(writer);

        bake(writer, "bread_from_dough", ModTags.Items.DOUGH_WHEAT, Items.BREAD, false);
        bake(writer, "rye_bread", ModTags.Items.DOUGH_RYE, ModItems.RYE_BREAD.get(), false);
        bake(writer, "sourdough_bread", ModItems.SOURDOUGH_DOUGH.get(), ModItems.SOURDOUGH_BREAD.get(), false);
        bake(writer, "spent_grain_bread", ModItems.SPENT_GRAIN_DOUGH.get(), ModItems.SPENT_GRAIN_BREAD.get(), false);
        bake(writer, "beer_bread", ModItems.BEER_BREAD_DOUGH.get(), ModItems.BEER_BREAD.get(), false);
        bake(writer, "cornbread", ModItems.CORNBREAD_BATTER.get(), ModItems.CORNBREAD.get(), false);
        bake(writer, "tortilla", ModItems.MASA.get(), ModItems.TORTILLA.get(), true);
        bake(writer, "roasted_corn", ModTags.Items.crop("corn"), ModItems.ROASTED_CORN.get(), true);
    }

    /** One Brew Kettle dish: written as our kettle recipe, and again for Farmer's Delight's Cooking Pot. */
    private record Dish(String name, List<Ingredient> ingredients, @Nullable CookingRecipe.Liquid liquid, @Nullable ItemLike container,
                        ItemLike result, int count, int ticks) {}

    /**
     * Brew Kettle dishes (GDD section 15): one serving per batch over heat. Crop ingredients use tags,
     * so another mod's onions or tomatoes work too. Stews take 15 s, soups 10-12 s (before the config).
     * With Farmer's Delight installed, each also cooks in its Cooking Pot (GDD section 15.3): the pot has
     * no tank, so milk and ale go in as items (any milk; a mug of any ale) and water is simply left out.
     */
    private void cooking(Consumer<FinishedRecipe> writer) {
        CookingRecipe.Liquid water = new CookingRecipe.Liquid(null, FluidTags.WATER, 250);
        CookingRecipe.Liquid milk = new CookingRecipe.Liquid(null, Tags.Fluids.MILK, 250);
        CookingRecipe.Liquid ale = new CookingRecipe.Liquid(null, ModTags.Fluids.ALES, 250);
        CookingRecipe.Liquid redWine = new CookingRecipe.Liquid(ModFluids.RED_WINE.get(), null, 250);
        CookingRecipe.Liquid whiteWine = new CookingRecipe.Liquid(ModFluids.WHITE_WINE.get(), null, 250);
        CookingRecipe.Liquid sorghumJuice = new CookingRecipe.Liquid(ModFluids.SORGHUM_JUICE.get(), null, 250);
        Ingredient beef = Ingredient.of(Items.BEEF);
        Ingredient onion = Ingredient.of(ModTags.Items.crop("onion"));
        Ingredient carrot = Ingredient.of(Tags.Items.CROPS_CARROT);
        Ingredient potato = Ingredient.of(Tags.Items.CROPS_POTATO);
        Ingredient tomato = Ingredient.of(ModTags.Items.crop("tomato"));
        Ingredient sugar = Ingredient.of(ModTags.Items.SWEETENERS);   // sugar or sorghum syrup
        Ingredient mushroom = Ingredient.of(Tags.Items.MUSHROOMS);
        Ingredient rice = Ingredient.of(ModTags.Items.crop("rice"));

        List<Dish> dishes = List.of(
                new Dish("beef_and_ale_stew", List.of(beef, onion, carrot, potato), ale, Items.BOWL, ModItems.BEEF_AND_ALE_STEW.get(), 1, 300),
                new Dish("mutton_and_barley_stew", List.of(Ingredient.of(Items.MUTTON), Ingredient.of(ModTags.Items.CROPS_BARLEY), carrot, onion),
                        water, Items.BOWL, ModItems.MUTTON_AND_BARLEY_STEW.get(), 1, 300),
                new Dish("chili_con_carne", List.of(beef, tomato, Ingredient.of(ModTags.Items.CHILIES), onion), water, Items.BOWL,
                        ModItems.CHILI_CON_CARNE.get(), 1, 300),
                new Dish("borscht", List.of(Ingredient.of(Tags.Items.CROPS_BEETROOT), Ingredient.of(ModTags.Items.crop("cabbage")), onion),
                        water, Items.BOWL, ModItems.BORSCHT.get(), 1, 240),
                new Dish("tomato_soup", List.of(tomato, onion), water, Items.BOWL, ModItems.TOMATO_SOUP.get(), 1, 200),
                new Dish("porridge", List.of(Ingredient.of(ModItems.ROLLED_OATS.get()), Ingredient.of(Items.HONEY_BOTTLE)), milk, Items.BOWL,
                        ModItems.PORRIDGE.get(), 1, 200),
                new Dish("cooked_rice", List.of(Ingredient.of(ModTags.Items.crop("rice"))), water, Items.BOWL, ModItems.COOKED_RICE.get(), 1, 200),
                new Dish("popcorn", List.of(Ingredient.of(ModTags.Items.crop("corn"))), null, null, ModItems.POPCORN.get(), 2, 160),
                new Dish("sugar_from_sugar_beet", List.of(Ingredient.of(ModTags.Items.crop("sugar_beet"))), water, null, Items.SUGAR, 2, 200),
                new Dish("cranberry_sauce", List.of(Ingredient.of(ModTags.Items.crop("cranberry")), sugar), null, Items.BOWL,
                        ModItems.CRANBERRY_SAUCE.get(), 1, 200),
                new Dish("blueberry_jam", List.of(Ingredient.of(ModTags.Items.crop("blueberry")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.BLUEBERRY_JAM.get(), 1, 200),
                new Dish("blackberry_jam", List.of(Ingredient.of(ModTags.Items.crop("blackberry")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.BLACKBERRY_JAM.get(), 1, 200),
                new Dish("elderberry_jam", List.of(Ingredient.of(ModTags.Items.crop("elderberry")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.ELDERBERRY_JAM.get(), 1, 200),
                new Dish("sweet_berry_jam", List.of(Ingredient.of(Items.SWEET_BERRIES), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.SWEET_BERRY_JAM.get(), 1, 200),
                new Dish("cherry_jam", List.of(Ingredient.of(ModTags.Items.fruit("cherry")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.CHERRY_JAM.get(), 1, 200),
                new Dish("plum_jam", List.of(Ingredient.of(ModTags.Items.fruit("plum")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.PLUM_JAM.get(), 1, 200),
                new Dish("peach_jam", List.of(Ingredient.of(ModTags.Items.fruit("peach")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.PEACH_JAM.get(), 1, 200),
                new Dish("marmalade", List.of(Ingredient.of(ModTags.Items.fruit("orange")), sugar), null, Items.GLASS_BOTTLE,
                        ModItems.MARMALADE.get(), 1, 200),
                // Wine in the pot: risotto with white wine, coq au vin with red.
                new Dish("risotto", List.of(rice, onion, mushroom), whiteWine, Items.BOWL, ModItems.RISOTTO.get(), 1, 300),
                new Dish("coq_au_vin", List.of(Ingredient.of(Items.CHICKEN), onion, mushroom), redWine, Items.BOWL,
                        ModItems.COQ_AU_VIN.get(), 1, 300),
                new Dish("stuffed_grape_leaves", List.of(Ingredient.of(ModItems.GRAPE_LEAVES.get()), rice, onion,
                        Ingredient.of(ModTags.Items.crop("mint"))), water, null, ModItems.STUFFED_GRAPE_LEAVES.get(), 3, 240),
                // Elderflower cordial: the flowers steeped with sugar and a lemon, bottled.
                new Dish("elderflower_cordial", List.of(Ingredient.of(Crops.ELDERBERRY.flowers()), sugar,
                        Ingredient.of(ModTags.Items.fruit("lemon"))), water, Items.GLASS_BOTTLE, Drinks.ELDERFLOWER_CORDIAL.item().get(), 1, 200),
                // Sorghum syrup: the juice alone, boiled down into a bottle.
                new Dish("sorghum_syrup", List.of(), sorghumJuice, Items.GLASS_BOTTLE, ModItems.SORGHUM_SYRUP.get(), 1, 300),
                new Dish("steamed_rice", List.of(Ingredient.of(ModItems.POLISHED_RICE.get())), water, null, ModItems.STEAMED_RICE.get(), 2, 160),
                new Dish("coffee", List.of(Ingredient.of(ModItems.GROUND_COFFEE.get())), water, Items.GLASS_BOTTLE, Drinks.COFFEE.item().get(), 1, 160),
                new Dish("agave_syrup", List.of(), new CookingRecipe.Liquid(ModFluids.AGAVE_JUICE.get(), null, 250), Items.GLASS_BOTTLE,
                        ModItems.AGAVE_SYRUP.get(), 1, 300),
                // Mulled wine: red wine heated with an orange and sugar, served hot in a mug.
                new Dish("mulled_wine", List.of(Ingredient.of(ModTags.Items.fruit("orange")), sugar), redWine, ModItems.MUG.get(),
                        Drinks.MULLED_WINE.item().get(), 1, 200));

        Ingredient anyMilk = Ingredient.of(ItemTags.create(SeedToCellar.rl("forge", "milk")));
        for (Dish dish : dishes) {
            StationRecipes.cooking(writer, dish.name(), dish.ingredients(), dish.liquid(), dish.container(), dish.result(), dish.count(), dish.ticks());
            List<Ingredient> potIngredients = new java.util.ArrayList<>(dish.ingredients());
            if (dish.liquid() == milk) potIngredients.add(anyMilk);
            if (dish.liquid() == ale) potIngredients.add(Ingredient.of(ModTags.Items.ALES));
            if (dish.liquid() == redWine) potIngredients.add(Ingredient.of(Drinks.RED_WINE.item().get()));
            if (dish.liquid() == whiteWine) potIngredients.add(Ingredient.of(Drinks.WHITE_WINE.item().get()));
            if (dish.liquid() == sorghumJuice || dish.name().equals("agave_syrup")) continue;   // the pot has no tank to boil juice in
            if (dish.name().equals("cooked_rice")) continue;   // Farmer's Delight's pot already cooks any rice into its own
            CompatRecipes.cookingPot(writer, dish.name(), potIngredients, dish.container(), dish.result(), dish.count(), dish.ticks());
        }

        // Black Forest Cake (GDD section 15.2): cherries and kirsch, cocoa, milk, an egg and flour; six slices.
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.BLACK_FOREST_CAKE.get())
                .pattern("CKC").pattern("BMB").pattern("FEF")
                .define('C', ModTags.Items.fruit("cherry")).define('K', Drinks.KIRSCH.item().get()).define('B', Items.COCOA_BEANS)
                .define('M', Items.MILK_BUCKET).define('F', ModTags.Items.FLOUR_WHEAT).define('E', Tags.Items.EGGS)
                .unlockedBy("has_kirsch", has(Drinks.KIRSCH.item().get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BLACK_FOREST_CAKE_SLICE.get(), LayerCakeBlock.SLICES)
                .requires(ModItems.BLACK_FOREST_CAKE.get())
                .unlockedBy("has_cake", has(ModItems.BLACK_FOREST_CAKE.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BLACK_FOREST_CAKE.get())
                .requires(ModItems.BLACK_FOREST_CAKE_SLICE.get(), LayerCakeBlock.SLICES)
                .unlockedBy("has_slice", has(ModItems.BLACK_FOREST_CAKE_SLICE.get())).save(writer, SeedToCellar.id("black_forest_cake_from_slices"));
        CompatRecipes.cuttingBoard(writer, "black_forest_cake", ModItems.BLACK_FOREST_CAKE.get(), ModItems.BLACK_FOREST_CAKE_SLICE.get(),
                LayerCakeBlock.SLICES);

        // Pies: fruit on top, sugar and an egg, a flour crust; whole pies and slices convert both ways.
        for (Pies.Pie pie : Pies.all()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, pie.item().get())
                    .pattern("FFF").pattern("SES").pattern("WWW")
                    .define('F', pie.fruit().get())
                    .define('S', ModTags.Items.SWEETENERS)
                    .define('E', Tags.Items.EGGS)
                    .define('W', ModTags.Items.FLOUR_WHEAT)
                    .unlockedBy("has_flour", has(ModTags.Items.FLOUR_WHEAT)).save(writer);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, pie.slice().get(), PieBlock.SLICES)
                    .requires(pie.item().get())
                    .unlockedBy("has_pie", has(pie.item().get())).save(writer);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, pie.item().get())
                    .requires(pie.slice().get(), PieBlock.SLICES)
                    .unlockedBy("has_slice", has(pie.slice().get())).save(writer, SeedToCellar.id(pie.id() + "_from_slices"));
            CompatRecipes.cuttingBoard(writer, pie.id(), pie.item().get(), pie.slice().get(), PieBlock.SLICES);
        }

        // The Harvest Feast (GDD section 15.2): roast chicken with potatoes, carrots, bread and cranberry sauce.
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.HARVEST_FEAST.get())
                .pattern(" S ").pattern("PCP").pattern("KBK")
                .define('S', ModItems.CRANBERRY_SAUCE.get())
                .define('P', Items.BAKED_POTATO)
                .define('C', Items.COOKED_CHICKEN)
                .define('K', Tags.Items.CROPS_CARROT)
                .define('B', ModTags.Items.BREAD)
                .unlockedBy("has_cranberry_sauce", has(ModItems.CRANBERRY_SAUCE.get())).save(writer);

        // By hand: the bowl or bottle comes back (DishItem).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.RICE_BALL.get(), 2)
                .requires(ModTags.Items.COOKED_RICE).requires(Items.DRIED_KELP)
                .unlockedBy("has_cooked_rice", has(ModTags.Items.COOKED_RICE)).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.GRANOLA.get(), 3)
                .requires(ModItems.ROLLED_OATS.get()).requires(ModItems.ROLLED_OATS.get()).requires(Items.HONEY_BOTTLE)
                .requires(ModTags.Items.DRIED_FRUITS)
                .unlockedBy("has_dried_fruit", has(ModTags.Items.DRIED_FRUITS)).save(writer);
        // Olive oil dishes (the bottle comes back).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SALAD.get())
                .requires(Items.BOWL).requires(ModTags.Items.crop("cabbage")).requires(ModTags.Items.crop("tomato"))
                .requires(ModTags.Items.crop("cucumber")).requires(ModItems.OLIVE_OIL.get())
                .unlockedBy("has_olive_oil", has(ModItems.OLIVE_OIL.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.BRUSCHETTA.get(), 2)
                .requires(ModTags.Items.BREAD).requires(ModTags.Items.crop("tomato")).requires(ModTags.Items.crop("garlic"))
                .requires(ModItems.OLIVE_OIL.get())
                .unlockedBy("has_olive_oil", has(ModItems.OLIVE_OIL.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.GARLIC_BREAD.get(), 2)
                .requires(ModTags.Items.BREAD).requires(ModTags.Items.crop("garlic")).requires(ModItems.OLIVE_OIL.get())
                .unlockedBy("has_olive_oil", has(ModItems.OLIVE_OIL.get())).save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.JAM_TOAST.get(), 2)
                .requires(ModTags.Items.BREAD).requires(ModTags.Items.JAMS)
                .unlockedBy("has_jam", has(ModTags.Items.JAMS)).save(writer);
    }

    private static void bake(Consumer<FinishedRecipe> writer, String name, TagKey<Item> input, ItemLike result, boolean campfire) {
        bake(writer, name, Ingredient.of(input), has(input), result, campfire);
    }

    private static void bake(Consumer<FinishedRecipe> writer, String name, ItemLike input, ItemLike result, boolean campfire) {
        bake(writer, name, Ingredient.of(input), has(input), result, campfire);
    }

    /** Furnace and smoker (and campfire, for flatbreads and roasting) versions of one bake. */
    private static void bake(Consumer<FinishedRecipe> writer, String name, Ingredient ingredient, CriterionTriggerInstance unlock,
                             ItemLike result, boolean campfire) {
        SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.FOOD, result, 0.35F, 200)
                .unlockedBy("has_input", unlock).save(writer, SeedToCellar.id(name));
        SimpleCookingRecipeBuilder.smoking(ingredient, RecipeCategory.FOOD, result, 0.35F, 100)
                .unlockedBy("has_input", unlock).save(writer, SeedToCellar.id(name + "_from_smoking"));
        if (campfire) {
            SimpleCookingRecipeBuilder.campfireCooking(ingredient, RecipeCategory.FOOD, result, 0.35F, 600)
                    .unlockedBy("has_input", unlock).save(writer, SeedToCellar.id(name + "_from_campfire"));
        }
    }

    private void crafting(Consumer<FinishedRecipe> writer) {
        // Five sticks in an X (the lattice) make three trellises.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.TRELLIS.get(), 3)
                .pattern("S S").pattern(" S ").pattern("S S")
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
                .save(writer);

        // Sickles: a curved blade of the tier's material on a stick; netherite by smithing, like vanilla tools.
        sickle(writer, ModItems.WOODEN_SICKLE.get(), Ingredient.of(net.minecraft.tags.ItemTags.PLANKS), "has_planks",
                has(net.minecraft.tags.ItemTags.PLANKS));
        sickle(writer, ModItems.STONE_SICKLE.get(), Ingredient.of(net.minecraft.tags.ItemTags.STONE_TOOL_MATERIALS), "has_cobblestone",
                has(net.minecraft.tags.ItemTags.STONE_TOOL_MATERIALS));
        sickle(writer, ModItems.IRON_SICKLE.get(), Ingredient.of(Tags.Items.INGOTS_IRON), "has_iron", has(Tags.Items.INGOTS_IRON));
        sickle(writer, ModItems.GOLDEN_SICKLE.get(), Ingredient.of(Tags.Items.INGOTS_GOLD), "has_gold", has(Tags.Items.INGOTS_GOLD));
        sickle(writer, ModItems.DIAMOND_SICKLE.get(), Ingredient.of(Tags.Items.GEMS_DIAMOND), "has_diamond", has(Tags.Items.GEMS_DIAMOND));
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(net.minecraft.world.item.Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ModItems.DIAMOND_SICKLE.get()), Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.TOOLS, ModItems.NETHERITE_SICKLE.get())
                .unlocks("has_netherite_ingot", has(Tags.Items.INGOTS_NETHERITE))
                .save(writer, SeedToCellar.id("netherite_sickle_smithing"));

        // Thatch from our straw (Farmer's Delight weaves its own straw into canvas the same way), then roofing stairs and
        // slabs the vanilla way.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.THATCH.get())
                .pattern("SS").pattern("SS")
                .define('S', ModItems.STRAW.get())
                .unlockedBy("has_straw", has(ModItems.STRAW.get()))
                .save(writer);
        stairBuilder(ModItems.THATCH_STAIRS.get(), Ingredient.of(ModItems.THATCH.get()))
                .unlockedBy("has_thatch", has(ModItems.THATCH.get())).save(writer);
        slab(writer, RecipeCategory.BUILDING_BLOCKS, ModItems.THATCH_SLAB.get(), ModItems.THATCH.get());

        // Drying Rack: a stick frame strung with twine.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DRYING_RACK.get())
                .pattern("SSS").pattern("T T").pattern("S S")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('T', Tags.Items.STRING)
                .unlockedBy("has_string", has(Tags.Items.STRING))
                .save(writer);

        // Bales, sacks and crates, packed from our own items (other mods pack theirs into their own blocks): nine to a bale;
        // eight round a string make a sack, round a wooden slab a crate. They unpack to what went in.
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            String id = storage.block().getId().getPath();
            Ingredient contents = Ingredient.of(storage.contents().get());
            ShapedRecipeBuilder pack = ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, storage.block().get())
                    .define('#', contents)
                    .unlockedBy("has_" + storage.tag(), has(storage.contents().get()));
            if (storage.count() == 9) {
                pack.pattern("###").pattern("###").pattern("###");
            } else {
                boolean crate = storage.block().get() instanceof StorageBlocks.Crate;
                pack.pattern("###").pattern("#C#").pattern("###")
                        .define('C', crate ? Ingredient.of(ItemTags.WOODEN_SLABS) : Ingredient.of(Tags.Items.STRING));
            }
            pack.save(writer);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, storage.contents().get(), storage.count())
                    .requires(storage.block().get())
                    .unlockedBy("has_" + id, has(storage.block().get()))
                    .save(writer, SeedToCellar.id(storage.tag() + "_from_" + id));
        }

        // Compost Bin: a slatted plank box.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMPOST_BIN.get())
                .pattern("S S").pattern("P P").pattern("PPP")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .unlockedBy("has_planks", has(net.minecraft.tags.ItemTags.PLANKS))
                .save(writer);

        // Malting Tub: a plank tub bound with iron hoops.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MALTING_TUB.get())
                .pattern("P P").pattern("N N").pattern("PPP")
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_barley", has(ModTags.Items.CROPS_BARLEY))
                .save(writer);

        // Kiln: a brick oven around a furnace.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.KILN.get())
                .pattern("BBB").pattern("BIB").pattern("BFB")
                .define('B', Items.BRICK)
                .define('I', Items.IRON_BARS)
                .define('F', Items.FURNACE)
                .unlockedBy("has_green_malt", has(ModItems.GREEN_BARLEY_MALT.get()))
                .save(writer);

        // Brew Kettle: a copper cauldron.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BREW_KETTLE.get())
                .pattern("C C").pattern("C C").pattern("CCC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_grist", has(ModItems.PALE_GRIST.get()))
                .save(writer);

        // Fermenting Vat: a tall iron-bound plank vat.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FERMENTING_VAT.get())
                .pattern("PIP").pattern("P P").pattern("PPP")
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_kettle", has(ModItems.BREW_KETTLE.get()))
                .save(writer);

        // Preserving Jar: glass with a wooden lid (makes two).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRESERVING_JAR.get(), 2)
                .pattern(" P ").pattern("G G").pattern("GGG")
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .define('G', Tags.Items.GLASS_COLORLESS)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_COLORLESS))
                .save(writer);

        // Casks: one wood's planks bound with iron (the wood matters for aging).
        for (CaskWood wood : CaskWood.values()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CASKS.get(wood).get())
                    .pattern("PIP").pattern("P P").pattern("PIP")
                    .define('P', BuiltInRegistries.ITEM.get(SeedToCellar.rl("minecraft", wood.id() + "_planks")))
                    .define('I', Tags.Items.INGOTS_IRON)
                    .unlockedBy("has_vat", has(ModItems.FERMENTING_VAT.get()))
                    .save(writer);
        }

        // Wine Rack: a planked frame with stick dividers.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.WINE_RACK.get())
                .pattern("PSP").pattern("S S").pattern("PSP")
                .define('P', ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_wine_bottle", has(ModItems.WINE_BOTTLE.get()))
                .save(writer);

        // Bottle Shelf: wooden slabs for its boards, sticks for its sides (makes two).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.BOTTLE_SHELF.get(), 2)
                .pattern("SSS").pattern("T T").pattern("SSS")
                .define('S', ItemTags.WOODEN_SLABS)
                .define('T', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_mug", has(ModItems.MUG.get()))
                .save(writer);

        // Wine Display: a planked case with slab shelves.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.WINE_DISPLAY.get())
                .pattern("PSP").pattern("PSP").pattern("PSP")
                .define('P', ItemTags.PLANKS)
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_wine_bottle", has(ModItems.WINE_BOTTLE.get()))
                .save(writer);

        // Tap: a wooden spigot with an iron handle (makes two).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TAP.get(), 2)
                .pattern(" N").pattern("PS")
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_vat", has(ModItems.FERMENTING_VAT.get()))
                .save(writer);

        // Keg: a small iron-banded barrel with a tap built in.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.KEG.get())
                .pattern("PNP").pattern("P P").pattern("PTP")
                .define('P', net.minecraft.tags.ItemTags.PLANKS)
                .define('N', Tags.Items.NUGGETS_IRON)
                .define('T', ModItems.TAP.get())
                .unlockedBy("has_tap", has(ModItems.TAP.get()))
                .save(writer);

        // Beer mug: glass, so you can see what's in it (makes two).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUG.get(), 2)
                .pattern("G G").pattern("GGG")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_vat", has(ModItems.FERMENTING_VAT.get()))
                .save(writer);

        // Hydrometer: a weighted glass float.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HYDROMETER.get())
                .pattern("G").pattern("G").pattern("N")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_kettle", has(ModItems.BREW_KETTLE.get()))
                .save(writer);

        // Millstone: two stones and a stick for the handle.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MILLSTONE.get())
                .pattern(" S ").pattern("TTT").pattern("TTT")
                .define('S', Tags.Items.RODS_WOODEN)
                .define('T', Items.SMOOTH_STONE_SLAB)
                .unlockedBy("has_malt", has(ModItems.PALE_MALT.get()))
                .save(writer);
    }
}
