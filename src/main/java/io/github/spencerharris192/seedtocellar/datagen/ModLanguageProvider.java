package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.compat.jade.JadeIds;
import java.util.List;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.brewing.Vessel;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.Map;

/** English names. Written by hand on purpose: display names deserve care. The audit flags any missing one. */
public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, SeedToCellar.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + SeedToCellar.MOD_ID, "Seed to Cellar");

        // Farming: crop names come from their definitions in Crops.
        for (Crop crop : Crops.all()) {
            add(crop.produce(), crop.produceName);
            if (!crop.selfPlanting) add(crop.seeds(), crop.seedName);
            if (crop.hasFlowers()) add(crop.flowers(), crop.flowersName);
            add(crop.block(), crop.blockName);
            if (crop.hasWild()) add(crop.wildBlock(), "Wild " + crop.displayName);
        }
        for (FruitTree tree : FruitTrees.all()) {
            add(tree.sapling(), tree.displayName + " Sapling");
            add(tree.leaves(), tree.displayName + " Leaves");
            if (tree.ownFruit()) add(tree.fruit(), tree.fruitName);
        }
        for (Climate climate : Climate.values()) {
            String name = climate.name().charAt(0) + climate.name().substring(1).toLowerCase(Locale.ROOT);
            add(climate.translationKey(), name);
        }
        add(ModItems.HOP_RHIZOME.get(), "Hop Rhizome");
        add(ModItems.HOP_CONES.get(), "Hop Cones");
        add(ModBlocks.WILD_HOPS.get(), "Wild Hops");
        add(ModBlocks.TRELLIS.get(), "Trellis");
        add(ModBlocks.COMPOST_BIN.get(), "Compost Bin");
        add(ModItems.COMPOST.get(), "Compost");
        add(ModItems.WOODEN_SICKLE.get(), "Wooden Sickle");
        add(ModItems.STONE_SICKLE.get(), "Stone Sickle");
        add(ModItems.IRON_SICKLE.get(), "Iron Sickle");
        add(ModItems.GOLDEN_SICKLE.get(), "Golden Sickle");
        add(ModItems.DIAMOND_SICKLE.get(), "Diamond Sickle");
        add(ModItems.NETHERITE_SICKLE.get(), "Netherite Sickle");
        add(ModItems.STRAW.get(), "Straw");
        add(ModBlocks.DRYING_RACK.get(), "Drying Rack");
        add(ModItems.DRIED_CHILI.get(), "Dried Chili");
        add(ModItems.JERKY.get(), "Jerky");
        add(ModBlocks.THATCH.get(), "Thatch");
        add(ModBlocks.THATCH_STAIRS.get(), "Thatch Stairs");
        add(ModBlocks.THATCH_SLAB.get(), "Thatch Slab");
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) add(storage.block().get(), storage.name());
        add(ModBlocks.FERTILE_FARMLAND.get(), "Fertile Farmland");
        add(ModBlocks.PLACED_DRINKS.get(), "Placed Drinks");
        add(ModBlocks.HOP_BUNDLE.get(), "Hop Bundle");
        add(ModBlocks.MUG_RACK.get(), "Mug Rack");
        add(ModBlocks.HOP_GARLAND.get(), "Hop Garland");
        add(ModBlocks.TAVERN_SIGN.get(), "Tavern Sign");
        add("block.seedtocellar.weathering.exposed", "Exposed %s");
        add("block.seedtocellar.weathering.weathered", "Weathered %s");
        add("block.seedtocellar.weathering.oxidized", "Oxidized %s");
        add("block.seedtocellar.waxed", "Waxed %s");
        for (CaskWood wood : CaskWood.values()) {
            String name = titleCase(wood.id());
            add(ModBlocks.BAR_COUNTERS.get(wood).get(), name + " Bar Counter");
            add(ModBlocks.BAR_STOOLS.get(wood).get(), name + " Bar Stool");
        }
        add(ModBlocks.LAVENDER_BUNDLE.get(), "Lavender Bundle");
        add(ModBlocks.GARLIC_BRAID.get(), "Garlic Braid");
        add(ModBlocks.CHILI_STRING.get(), "Chili String");
        add(ModBlocks.HOPS.get(), "Hops");

        // Malt house
        add(ModBlocks.MALTING_TUB.get(), "Malting Tub");
        add(ModBlocks.KILN.get(), "Kiln");
        add(ModBlocks.MILLSTONE.get(), "Millstone");
        add(ModItems.DRIED_HOPS.get(), "Dried Hops");
        add(ModItems.GREEN_BARLEY_MALT.get(), "Green Barley Malt");
        add(ModItems.PALE_MALT.get(), "Pale Malt");
        add(ModItems.AMBER_MALT.get(), "Amber Malt");
        add(ModItems.BLACK_MALT.get(), "Black Malt");
        add(ModItems.PALE_GRIST.get(), "Pale Grist");
        add(ModItems.AMBER_GRIST.get(), "Amber Grist");
        add(ModItems.BLACK_GRIST.get(), "Black Grist");
        add(ModItems.WHEAT_FLOUR.get(), "Wheat Flour");
        add(ModItems.RYE_FLOUR.get(), "Rye Flour");
        add(ModItems.CORNMEAL.get(), "Cornmeal");
        add(ModItems.ROLLED_OATS.get(), "Rolled Oats");
        add(ModItems.DOUGH.get(), "Dough");
        add(ModItems.RYE_DOUGH.get(), "Rye Dough");
        add(ModItems.SOURDOUGH_DOUGH.get(), "Sourdough Dough");
        add(ModItems.SPENT_GRAIN_DOUGH.get(), "Spent-Grain Dough");
        add(ModItems.BEER_BREAD_DOUGH.get(), "Beer Bread Dough");
        add(ModItems.CORNBREAD_BATTER.get(), "Cornbread Batter");
        add(ModItems.MASA.get(), "Masa");
        add(ModItems.RYE_BREAD.get(), "Rye Bread");
        add(ModItems.SOURDOUGH_BREAD.get(), "Sourdough Bread");
        add(ModItems.CORNBREAD.get(), "Cornbread");
        add(ModItems.SPENT_GRAIN_BREAD.get(), "Spent-Grain Bread");
        add(ModItems.BEER_BREAD.get(), "Beer Bread");
        add(ModItems.TORTILLA.get(), "Tortilla");
        add(ModItems.ROASTED_CORN.get(), "Roasted Corn");
        add(ModItems.BEEF_AND_ALE_STEW.get(), "Beef and Ale Stew");
        add(ModItems.MUTTON_AND_BARLEY_STEW.get(), "Mutton and Barley Stew");
        add(ModItems.CHILI_CON_CARNE.get(), "Chili con Carne");
        add(ModItems.BORSCHT.get(), "Borscht");
        add(ModItems.TOMATO_SOUP.get(), "Tomato Soup");
        add(ModItems.PORRIDGE.get(), "Porridge");
        add(ModItems.COOKED_RICE.get(), "Cooked Rice");
        add(ModItems.RICE_BALL.get(), "Rice Ball");
        add(ModItems.POPCORN.get(), "Popcorn");
        add(ModItems.CRANBERRY_SAUCE.get(), "Cranberry Sauce");
        add(ModItems.BLUEBERRY_JAM.get(), "Blueberry Jam");
        add(ModItems.BLACKBERRY_JAM.get(), "Blackberry Jam");
        add(ModItems.ELDERBERRY_JAM.get(), "Elderberry Jam");
        add(ModItems.SWEET_BERRY_JAM.get(), "Sweet Berry Jam");
        add(ModItems.CHERRY_JAM.get(), "Cherry Jam");
        add(ModItems.PLUM_JAM.get(), "Plum Jam");
        add(ModItems.PEACH_JAM.get(), "Peach Jam");
        add(ModItems.MARMALADE.get(), "Marmalade");
        add(ModItems.JAM_TOAST.get(), "Jam Toast");
        add(ModItems.MOTHER_OF_VINEGAR.get(), "Mother of Vinegar");
        add(ModItems.PICKLES.get(), "Pickles");
        add(ModItems.SAUERKRAUT.get(), "Sauerkraut");
        add(ModItems.KIMCHI.get(), "Kimchi");
        add(ModItems.DRIED_BERRIES.get(), "Dried Berries");
        add(ModItems.GRAPE_LEAVES.get(), "Grape Leaves");
        add(ModItems.PRUNES.get(), "Prunes");
        add(ModItems.DRIED_CHERRIES.get(), "Dried Cherries");
        add(ModItems.DRIED_APPLES.get(), "Dried Apples");
        add(ModItems.DRIED_PEACHES.get(), "Dried Peaches");
        add(ModItems.DRIED_PEARS.get(), "Dried Pears");
        add(ModItems.OLIVE_OIL.get(), "Olive Oil");
        add(ModItems.SORGHUM_SYRUP.get(), "Sorghum Syrup");
        add(ModItems.CURED_OLIVES.get(), "Cured Olives");
        add(ModItems.STUFFED_GRAPE_LEAVES.get(), "Stuffed Grape Leaves");
        add(ModItems.BRUSCHETTA.get(), "Bruschetta");
        add(ModItems.GARLIC_BREAD.get(), "Garlic Bread");
        add(ModItems.SALAD.get(), "Salad");
        add(ModItems.RISOTTO.get(), "Risotto");
        add(ModItems.COQ_AU_VIN.get(), "Coq au Vin");
        add(ModBlocks.CRUSHING_TUB.get(), "Crushing Tub");
        add(ModBlocks.FRUIT_PRESS.get(), "Fruit Press");
        add(ModItems.GRAPE_POMACE.get(), "Grape Pomace");
        add(ModItems.FRUIT_POMACE.get(), "Fruit Pomace");
        add(ModItems.OLIVE_POMACE.get(), "Olive Pomace");
        add(ModItems.BAGASSE.get(), "Bagasse");
        add(ModItems.RAISINS.get(), "Raisins");
        add(ModItems.GOLDEN_RAISINS.get(), "Golden Raisins");
        add(ModItems.GRANOLA.get(), "Granola");
        add(ModBlocks.HARVEST_FEAST.get(), "Harvest Feast");
        add(ModItems.HARVEST_FEAST_SERVING.get(), "Harvest Feast Serving");
        add("message." + SeedToCellar.MOD_ID + ".feast.needs_bowl", "Use a bowl to serve the feast");
        add("message." + SeedToCellar.MOD_ID + ".tub.add_fruit", "Add fruit, then jump in and stomp it");
        add("message." + SeedToCellar.MOD_ID + ".tub.jump_in", "Jump in and stomp the fruit!");
        add("message." + SeedToCellar.MOD_ID + ".tub.one_fruit", "The tub takes one kind of fruit at a time (up to 16)");
        add("message." + SeedToCellar.MOD_ID + ".tub.other_liquid", "The tub holds %s: empty it before crushing different fruit");
        add("message." + SeedToCellar.MOD_ID + ".tub.full", "The tub is full: take some out with a bucket or bottle");
        add("message." + SeedToCellar.MOD_ID + ".press.one_fruit", "The press takes one kind of fruit at a time (up to 16)");
        for (Pies.Pie pie : Pies.all()) {
            add(pie.block().get(), pie.displayName() + " Pie");
            add(pie.slice().get(), pie.displayName() + " Pie Slice");
        }

        add("roast." + SeedToCellar.MOD_ID + ".light", "Light");
        add("roast." + SeedToCellar.MOD_ID + ".medium", "Medium");
        add("roast." + SeedToCellar.MOD_ID + ".dark", "Dark");
        add("gui." + SeedToCellar.MOD_ID + ".kiln.roast", "Roast: %s");
        add("gui." + SeedToCellar.MOD_ID + ".kiln.roast.light.hint", "Pale malt; dries hops");
        add("gui." + SeedToCellar.MOD_ID + ".kiln.roast.medium.hint", "Amber malt (twice as long)");
        add("gui." + SeedToCellar.MOD_ID + ".kiln.roast.dark.hint", "Black malt (four times as long)");

        // Brewhouse
        add(ModBlocks.BREW_KETTLE.get(), "Brew Kettle");
        add(ModBlocks.FERMENTING_VAT.get(), "Fermenting Vat");
        add(ModBlocks.PRESERVING_JAR.get(), "Preserving Jar");
        add(ModItems.SPENT_GRAIN.get(), "Spent Grain");
        add(ModItems.SOURDOUGH_STARTER.get(), "Sourdough Starter");
        add(ModItems.ALE_YEAST.get(), "Ale Yeast");
        add(ModItems.WINE_YEAST.get(), "Wine Yeast");
        add(ModItems.WINE_BOTTLE.get(), "Wine Bottle");

        // Distillery
        add(ModBlocks.POT_STILL.get(), "Pot Still");
        add(ModItems.SPIRIT_BOTTLE.get(), "Spirit Bottle");

        String gui = "gui." + SeedToCellar.MOD_ID + ".";
        add(gui + "tank.empty", "Empty");
        add(gui + "tank.amount", "%s / %s mB");
        add(gui + "kettle.no_heat", "Needs heat below");
        add(gui + "kettle.mashing", "Mashing...");
        add(gui + "kettle.boiling", "Boiling...");
        add(gui + "kettle.add_water", "Add water, milk or ale");
        add(gui + "kettle.add_grist", "Add grist or ingredients");
        add(gui + "kettle.cooking", "Cooking...");
        add(gui + "kettle.mixing", "Mixing...");
        add(gui + "kettle.needs_liquid", "Add %2$s");
        add(gui + "kettle.needs_container", "Add a %s");
        add(gui + "kettle.output_full", "Empty the output");
        add(gui + "kettle.needs_more", "Add %s more %s (%s per bucket)");
        add(gui + "kettle.stir_in", "Stir in %s %s per bucket");
        add(gui + "kettle.makes", "Makes: %s");
        add(gui + "kettle.add_hops", "Add %s dried hops");
        add(gui + "kettle.ready", "Wort ready!");
        add(gui + "kettle.strength", "Strength: %s");
        add(gui + "vat.add_wort", "Add wort or juice");
        add(gui + "vat.makes", "Makes: %s");
        add(gui + "vat.fermenting", "Fermenting %s%%");
        add(gui + "vat.temperature", "Temp: %s");
        add(gui + "vat.ideal", "Ideal: %s");
        add(gui + "vat.yeast_ok", "Yeast: cultured");
        add(gui + "vat.yeast_wild", "Yeast: wild (slow)");
        add(gui + "vat.hint_close", "Close lid to start");
        add(gui + "vat.hint_open", "Lid closed");
        add(gui + "vat.close_lid", "Close the lid (starts fermenting)");
        add(gui + "vat.open_lid", "Open the lid");
        add(gui + "kettle.needs_malt", "Add malt too: corn and potatoes need it to mash");
        String still = gui + "still.";
        add(still + "tank_pot", "Pot");
        add(still + "tank_receiver", "Receiver (spirit safe)");
        add(still + "tank_stillage", "Stillage");
        add(still + "pot", "Pot: %s, %s mB");
        add(still + "pot_empty", "Pot: empty");
        add(still + "receiver", "Spirit: %s, %s mB");
        add(still + "stillage", "Stillage: %s mB");
        add(still + "makes", "Makes: %s (run %s)");
        add(still + "no_heat", "Light a fire under the still");
        add(still + "distilling", "Distilling... %s%%");
        add(still + "starting", "Starting the run");
        add(still + "add_wash", "Pour in a wash, a wine or a spirit");
        add(still + "hint_run_again", "Done! Take the spirit, or run it again for a star");
        add(still + "wont_distil", "That won't distil");
        add(still + "too_little", "Too little in the pot");
        add(still + "receiver_full", "The receiver is full: take the spirit");
        add(still + "receiver_other", "Take the spirit out first (it's a different one)");
        add(still + "stillage_full", "Drain the stillage (bucket, or a pipe below)");
        add(still + "run_again", "Run again");
        add(still + "run_again_hint", "Pour the spirit back into the pot to distil it once more");
        add(still + "basket_missing", "Fit a Gin Basket to the still for botanicals");
        add(still + "filter", "Charcoal filter");
        add(still + "filter_hint", "Grain or potato spirit run through charcoal becomes vodka");

        String msg = "message." + SeedToCellar.MOD_ID + ".";
        add(msg + "vat.busy", "Still fermenting. Wait until it's done.");
        add(msg + "vat.busy_progress", "Still fermenting (%s%% done)");
        add(msg + "vat.closed", "Open the lid first (sneak + right-click)");
        add(msg + "vat.nothing", "Nothing here can ferment yet");
        add(msg + "jar.closed", "Open the lid first (right-click with an empty hand)");
        add(msg + "jar.working", "Still working. Leave the lid on.");
        add(msg + "jar.temperature", "This needs a %s place (it's %s here)");
        add(msg + "still.busy", "The still is running. Wait for this run to finish.");
        add(msg + "still.wont_distil", "That can't go in the pot (one wash, wine or spirit at a time)");

        for (String s : new String[]{"light", "normal", "strong"}) {
            add("strength." + SeedToCellar.MOD_ID + "." + s, Character.toUpperCase(s.charAt(0)) + s.substring(1));
        }
        for (String t : new String[]{"cold", "cool", "mild", "warm"}) {
            add("temperature." + SeedToCellar.MOD_ID + "." + t, Character.toUpperCase(t.charAt(0)) + t.substring(1));
        }
        add("temperature." + SeedToCellar.MOD_ID + ".or", " or ");

        // Cellar
        for (CaskWood wood : CaskWood.values()) {
            add(ModBlocks.CASKS.get(wood).get(), titleCase(wood.id()) + " Cask");
            add("wood." + SeedToCellar.MOD_ID + "." + wood.id(), titleCase(wood.id()));
        }
        add(ModBlocks.WINE_RACK.get(), "Wine Rack");
        add(ModBlocks.BOTTLE_SHELF.get(), "Bottle Shelf");
        add(ModBlocks.WINE_DISPLAY.get(), "Wine Display");
        add(ModBlocks.KEG.get(), "Keg");
        add(ModItems.TAP.get(), "Tap");
        add(ModItems.MUG.get(), "Beer Mug");
        add(ModItems.HYDROMETER.get(), "Hydrometer");
        add(msg + "cask.no_tap", "Fit a Tap to pour from this cask");
        add(msg + "cask.charred", "Charred inside: whiskeys will love it");
        add(msg + "cask.char_empty", "Empty the cask before you char it");
        add(msg + "cask.wont_char", "Nether wood won't take a char");
        add(msg + "cask.already_charred", "This cask is already charred");
        add("block." + SeedToCellar.MOD_ID + ".charred_cask", "Charred %s");
        add("wood." + SeedToCellar.MOD_ID + ".charred", "Charred %s");
        add("item." + SeedToCellar.MOD_ID + ".named_bucket", "%s Bucket");
        // Spirits' names while young, or aged in the "wrong" cask (GDD section 10.3)
        String young = "drink." + SeedToCellar.MOD_ID + ".";
        add(young + "new_make", "New Make");
        add(young + "white_dog", "White Dog");
        add(young + "corn_whiskey", "Corn Whiskey");
        add(young + "eau_de_vie", "Eau-de-vie");
        add(young + "white_rum", "White Rum");
        add(young + "gold_rum", "Gold Rum");
        add(young + "dark_rum", "Dark Rum");
        add(young + "tequila_blanco", "Tequila Blanco");
        add(young + "tequila_reposado", "Tequila Reposado");
        add(young + "tequila_anejo", "Tequila Añejo");
        add(ModItems.MOLASSES.get(), "Molasses");
        add(ModItems.AGAVE_SYRUP.get(), "Agave Syrup");
        add(ModItems.ROASTED_AGAVE.get(), "Roasted Agave");
        add(ModItems.AGAVE_FIBER.get(), "Agave Fiber");
        add(ModItems.GIN_BASKET.get(), "Gin Basket");
        add(ModItems.LAGER_YEAST.get(), "Lager Yeast");
        add(ModItems.GREEN_WHEAT_MALT.get(), "Green Wheat Malt");
        add(ModItems.WHEAT_MALT.get(), "Wheat Malt");
        add(ModItems.WHEAT_GRIST.get(), "Wheat Grist");
        add(ModItems.POLISHED_RICE.get(), "Polished Rice");
        add(ModItems.RICE_BRAN.get(), "Rice Bran");
        add(ModItems.STEAMED_RICE.get(), "Steamed Rice");
        add(ModItems.KOJI_RICE.get(), "Koji Rice");
        add(ModItems.ROASTED_COFFEE.get(), "Roasted Coffee");
        add(ModItems.GROUND_COFFEE.get(), "Ground Coffee");
        add(ModBlocks.BLACK_FOREST_CAKE.get(), "Black Forest Cake");
        add(ModItems.BLACK_FOREST_CAKE_SLICE.get(), "Slice of Black Forest Cake");
        add(ModBlocks.VANILLA.get(), "Vanilla");
        add(ModItems.VANILLA_POD.get(), "Vanilla Pod");
        add(ModItems.CURED_VANILLA.get(), "Cured Vanilla");
        add(ModItems.LEMON_PEEL.get(), "Lemon Peel");
        add(ModItems.ORANGE_PEEL.get(), "Orange Peel");

        add(ModEffects.REFRESHED.get(), "Refreshed");
        add(ModEffects.WARMTH.get(), "Warmth");
        add(ModEffects.COURAGE.get(), "Courage");
        add(ModEffects.TIPSY.get(), "Tipsy");
        add(ModEffects.HANGOVER.get(), "Hangover");

        String tip = "tooltip." + SeedToCellar.MOD_ID + ".";
        add(tip + "aged_years", "Aged %s years");
        add(tip + "aged_years_in", "Aged %s years in %s");
        add(tip + "ideal_woods", "Ages best in %s (star at %s years)");
        add(tip + "list_and", " and ");
        add(tip + "or_charred", "%s, or any charred cask");
        add(tip + "hold_shift", "Hold Shift for quality details");
        add(tip + "cures_hangover", "Cures a hangover");
        add(tip + "climate", "Likes %s climates");
        add(tip + "resting_paused", "Resting paused: place it to continue");
        add(tip + "check.yeast", "Cultured yeast");
        add(tip + "check.temperature", "Right temperature");
        add(tip + "check.conditioned", "Conditioned (rested a day)");
        add(tip + "check.aged", "Aged in a cask");
        add(tip + "check.crowned", "Crowned: a perfect whiskey, steeped with a golden apple");
        add(tip + "cures", "Cures %s");
        add(tip + "units", "Alcohol: %s units");
        add(tip + "units_one", "Alcohol: 1 unit");
        add(tip + "set_down", "Sneak and right-click a surface to set it down");
        add(tip + "check.double_distilled", "Distilled twice");
        add(tip + "check.neutral", "Three runs, or a charcoal filter");
        add(tip + "check.botanicals", "Juniper and 3 more botanicals");
        add(tip + "check.infused", "Steeped from a well-distilled spirit");
        add(tip + "distilled_once", "Distilled once");
        add(tip + "distilled_times", "Distilled %s times");

        String hyd = "hydrometer." + SeedToCellar.MOD_ID + ".";
        add(hyd + "empty", "Empty");
        add(hyd + "conditioned", "Conditioned");
        add(hyd + "conditioning", "Conditioning: %s%%");
        add(hyd + "aged", "Aged %s of %s years");
        add(hyd + "aged_no_star", "Aged %s years (no aging star)");
        add(hyd + "bottles", "Bottles: %s/%s");
        add(hyd + "mugs", "Mugs: %s/%s");
        add(hyd + "wood_ideal", "%s: an ideal wood for it");
        add(hyd + "wood_slow", "%s: not its ideal wood (star takes twice as long)");
        add(hyd + "wood_nether", "%s: ages twice as fast, never earns the star");
        add(hyd + "no_tap", "No tap: right-click it with a Tap");
        add(hyd + "growth", "Growth: %s%%");
        add(hyd + "climate", "Climate: %s %s");
        add(hyd + "fertility", "Fertile soil: %s/3");
        add(hyd + "compost_filling", "Leftovers: %s/%s");
        add(hyd + "composting", "Composting: %s%%");
        add(hyd + "compost_ready", "Ready: %s Compost");
        add(hyd + "drying", "%s: %s%%");
        add(hyd + "jar.souring", "Souring into vinegar: %s min left");
        add(hyd + "slices_left", "Slices left: %s");
        add(hyd + "servings_left", "Servings left: %s");
        add(hyd + "leftovers", "Leftovers: right-click to clear");
        add(hyd + "bush_ripe", "Ripe: right-click to pick");
        add(hyd + "dried", "%s: dried");
        add(hyd + "rack_empty", "Nothing hanging");
        add(hyd + "rack_sun", "In the sun: drying twice as fast");
        add(hyd + "rack_shade", "Drying at the normal rate");
        add(hyd + "rack_rain", "Rained on: drying paused");
        add(hyd + "steeping", "Steeping: %s%%");
        add(hyd + "sprouting", "Sprouting: %s%%");
        add(hyd + "ready", "Ready: %s");
        add(hyd + "tub.add_water", "Add a bucket of water");
        add(hyd + "tub.add_grain", "Add up to 16 grain");
        add(hyd + "progress", "Progress: %s%%");
        add(hyd + "kiln.no_recipe", "Nothing to roast at this setting");
        add(hyd + "kiln.burning", "Fire burning");
        add(hyd + "kiln.cold", "No fire");
        add(hyd + "mill.input", "In: %s %s");
        add(hyd + "liquid", "%s mB of %s");
        add(hyd + "tub.fruit", "Fruit: %s %s");
        add(hyd + "tub.stomp", "Jump in to stomp it");
        add(hyd + "press.fruit", "In the cage: %s %s");
        add(hyd + "press.progress", "Screw: %s of %s turns (presses %s at a time)");
        add(hyd + "press.byproduct", "Left in the press: %s %s");
        add(hyd + "mill.output", "Out: %s %s");
        add(hyd + "temperature", "Temp: %s (ideal %s)");
        add(hyd + "jar.working", "Working: about %s min left");
        add(hyd + "jar.closed", "Lid closed");
        add(hyd + "jar.open", "Lid open");

        guideTooltips();
        jeiAndJade();

        // Liquids and their buckets
        // Liquids are named after their ID ("red_grape_must" -> "Red Grape Must").
        for (ModFluids.Entry fluid : ModFluids.all()) {
            String name = SPECIAL_NAMES.getOrDefault(fluid.name, titleCase(fluid.name));
            add("fluid." + SeedToCellar.MOD_ID + "." + fluid.name, name);
            add(fluid.bucket.get(), name + " Bucket");
        }
        // A mug of a drink is named after the drink.
        for (Drinks.Drink drink : Drinks.all()) add(drink.item().get(), SPECIAL_NAMES.getOrDefault(drink.name(), titleCase(drink.name())));
    }

    private static String titleCase(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /** "What is this" + "Next:" lines (GDD section 19). Keep each under ~50 characters so tooltips stay narrow. */
    private void guideTooltips() {
        add("tooltip." + SeedToCellar.MOD_ID + ".next", "Next: %s");
        for (Crop crop : Crops.all()) {
            String wild = crop.displayName.toLowerCase(Locale.ROOT);
            String grass = crop.grassDrop == null ? "" : crop.grassDrop.coldOnly() ? " and grass (cold biomes)" : " and grass";
            String from = !crop.growsWild() ? "From trading"
                    : crop.style == Crop.Style.SUCCULENT ? "From wild " + wild + " in flower (" + crop.wild.where() + ")"
                    : crop.style == Crop.Style.BUSH ? "From wild " + wild + " bushes"
                    : crop.style == Crop.Style.VINE ? "From wild " + wild + " vines"
                    : "From wild " + wild + grass;
            if (!crop.selfPlanting) guide(crop.seeds(), from, crop.plantingHint());
            guide(crop.produce(), crop.produceDesc, crop.produceNext);
            if (crop.hasFlowers()) guide(crop.flowers(), crop.flowersDesc, crop.flowersNext);
            if (crop.hasWild()) guide(crop.wildItem(), "Grows wild in " + crop.wild.where(), "Break it for " + crop.seedName);
        }
        for (FruitTree tree : FruitTrees.all()) {
            String lower = tree.displayName.toLowerCase(Locale.ROOT);
            guide(tree.saplingItem(), "From wild " + lower + " trees and orchards", "Plant it; it grows into a " + lower + " tree");
            if (tree.ownFruit()) guide(tree.fruit(), tree.fruitDesc, tree.fruitNext);
        }
        guide(ModItems.HOP_RHIZOME.get(), "A hop root cutting", "Plant on the bottom Trellis of a stack");
        guide(ModItems.HOP_CONES.get(), "Fresh hops", "Dry in a Kiln on Light");
        guide(ModItems.TRELLIS.get(), "Hops and grapes climb it (use on a trellis to stack; up to 3)", "Plant a rhizome or cutting on the bottom one");
        guide(ModItems.CRUSHING_TUB.get(), "Stomp fruit into juice (red grapes: must)", "Add fruit, then jump in it");
        guide(ModItems.FRUIT_PRESS.get(), "Presses fruit into juice, olives into oil", "Add fruit, then crank it (or redstone)");
        guide(ModItems.GRAPE_POMACE.get(), "Grape skins and seeds, left from pressing", "Steep 4 a bucket in a kettle of water for grappa");
        guide(ModItems.FRUIT_POMACE.get(), "Pressed fruit pulp", "Compost it");
        guide(ModItems.OLIVE_POMACE.get(), "Pressed olive pulp: oily, burns well", "Burn it as fuel, or compost it");
        guide(ModItems.BAGASSE.get(), "Pressed sorghum stalks: dry fiber", "Make paper, burn it, or compost it");
        guide(ModItems.GRAPE_LEAVES.get(), "Cut from a grown grape vine with shears", "Cook them stuffed with rice, onion and mint");
        guide(ModItems.RAISINS.get(), "Sun-dried red grapes", "Snack on them, or mix into granola");
        guide(ModItems.GOLDEN_RAISINS.get(), "Sun-dried white grapes", "Snack on them, or mix into granola");
        guide(ModItems.PRUNES.get(), "Sun-dried plums", "Snack on them, or mix into granola");
        guide(ModItems.DRIED_CHERRIES.get(), "Sun-dried cherries", "Snack on them, or mix into granola");
        guide(ModItems.DRIED_APPLES.get(), "Sun-dried apple rings", "Snack on them, or mix into granola");
        guide(ModItems.DRIED_PEACHES.get(), "Sun-dried peach slices", "Snack on them, or mix into granola");
        guide(ModItems.DRIED_PEARS.get(), "Sun-dried pear slices", "Snack on them, or mix into granola");
        guide(ModItems.OLIVE_OIL.get(), "Pressed from olives", "Make salad, bruschetta or garlic bread");
        guide(ModItems.SORGHUM_SYRUP.get(), "Sorghum juice boiled down", "Use it like sugar in cooking and baking");
        guide(ModItems.CURED_OLIVES.get(), "Olives soaked until mild", "Snack on them");
        guide(ModItems.STUFFED_GRAPE_LEAVES.get(), "Rice, onion and mint rolled in grape leaves", "Eat them");
        guide(ModItems.BRUSCHETTA.get(), "Toast with tomato, garlic and olive oil", "Eat it");
        guide(ModItems.GARLIC_BREAD.get(), "Bread with garlic and olive oil", "Eat it");
        guide(ModItems.SALAD.get(), "Cabbage, tomato and cucumber with olive oil", "Eat it (the bowl comes back)");
        guide(ModItems.RISOTTO.get(), "Creamy rice with mushrooms and white wine", "Eat it (the bowl comes back)");
        guide(ModItems.COQ_AU_VIN.get(), "Chicken braised in red wine", "Eat it (the bowl comes back)");
        guide(ModItems.COMPOST_BIN.get(), "Turns plant leftovers into compost", "Fill it with 16 leftovers");
        guide(ModItems.COMPOST.get(), "Rich, crumbly compost", "Use it on farmland to make it fertile");
        for (RegistryObject<Item> sickle : ModItems.SICKLES) {
            guide(sickle.get(), "Right-click a crop: harvests and replants all ripe crops around it", "Harvest grain with it for Straw");
        }
        guide(ModItems.STRAW.get(), "From grain harvested with a Sickle", "Craft 4 into Thatch, or compost it");
        guide(ModItems.DRYING_RACK.get(), "Air-dries hops, chili, fruit and meat; sun helps, rain pauses", "Right-click with something to hang it");
        guide(ModItems.DRIED_CHILI.get(), "Sun-dried chili pepper", "Spices chili con carne and kimchi, like a fresh one");
        guide(ModItems.JERKY.get(), "Dried meat: quick to eat", "Eat it, or pack it for a long trip");
        guide(ModItems.THATCH.get(), "Straw roofing; softens falls, burns easily", "Craft into stairs and slabs");
        guide(ModItems.WILD_HOPS.get(), "Grows wild in forests", "Break it for a Hop Rhizome");

        guide(ModItems.MALTING_TUB.get(), "Steeps, then sprouts grain", "Add a water bucket and up to 16 grain");
        guide(ModItems.KILN.get(), "Dries and roasts a batch at once", "Add fuel; pick Light, Medium or Dark");
        guide(ModItems.MILLSTONE.get(), "Grinds one item per crank", "Right-click to crank, sneak to collect");
        guide(ModItems.GREEN_BARLEY_MALT.get(), "Sprouted barley", "Roast in a Kiln: Light, Medium or Dark");
        guide(ModItems.PALE_MALT.get(), "Light malt, the base of every beer", "Grind in a Millstone");
        guide(ModItems.AMBER_MALT.get(), "Toasty malt for Amber and Old Ale", "Grind in a Millstone");
        guide(ModItems.BLACK_MALT.get(), "Roasted dark; 1 in 4 makes Stout", "Grind in a Millstone");
        guide(ModItems.PALE_GRIST.get(), "Milled pale malt", "Mash in a Brew Kettle: 2 per bucket");
        guide(ModItems.AMBER_GRIST.get(), "Milled amber malt", "Mash in a Brew Kettle with pale grist");
        guide(ModItems.BLACK_GRIST.get(), "Milled black malt", "Mash in a Brew Kettle with pale grist");
        guide(ModItems.DRIED_HOPS.get(), "Bitter and aromatic", "Boil in sweet wort: 1 per bucket");
        guide(ModItems.WHEAT_FLOUR.get(), "Ground wheat", "3 + a water bucket make dough; 2 + water in a Jar make a starter");
        guide(ModItems.RYE_FLOUR.get(), "Ground rye", "3 + a water bucket make rye dough");
        guide(ModItems.CORNMEAL.get(), "Ground corn", "With egg and milk: cornbread batter; with water: masa");
        guide(ModItems.ROLLED_OATS.get(), "Oats rolled flat in the Millstone", "Cook porridge: with honey and milk in the Brew Kettle");
        guide(ModItems.DOUGH.get(), "Plain wheat dough", "Bake it for bread, or mix in starter or spent grain");
        guide(ModItems.RYE_DOUGH.get(), "Dense, dark dough", "Bake it in a furnace or smoker");
        guide(ModItems.SOURDOUGH_DOUGH.get(), "Dough raised with a starter", "Bake it in a furnace or smoker");
        guide(ModItems.SPENT_GRAIN_DOUGH.get(), "Dough with brewing grain in it", "Bake it in a furnace or smoker");
        guide(ModItems.BEER_BREAD_DOUGH.get(), "Flour raised with ale", "Bake it in a furnace or smoker");
        guide(ModItems.CORNBREAD_BATTER.get(), "Thick corn batter", "Bake it in a furnace or smoker");
        guide(ModItems.MASA.get(), "Corn dough for flatbreads", "Cook it: furnace, smoker or campfire");
        guide(ModItems.RYE_BREAD.get(), "Dark, sour rye loaf", "Eat it");
        guide(ModItems.SOURDOUGH_BREAD.get(), "Tangy, chewy loaf", "Eat it (good the morning after)");
        guide(ModItems.CORNBREAD.get(), "Golden and crumbly", "Eat it");
        guide(ModItems.SPENT_GRAIN_BREAD.get(), "Nutty brewer's loaf", "Eat it");
        guide(ModItems.BEER_BREAD.get(), "A soft loaf with a malty crust", "Eat it");
        guide(ModItems.TORTILLA.get(), "Thin corn flatbread", "Eat it");
        guide(ModItems.ROASTED_CORN.get(), "Sweet, charred corn on the cob", "Eat it");
        guide(ModItems.BEEF_AND_ALE_STEW.get(), "Beef, roots and ale, slow-cooked", "Eat it (the bowl comes back)");
        guide(ModItems.MUTTON_AND_BARLEY_STEW.get(), "Hearty mutton stew thickened with barley", "Eat it (the bowl comes back)");
        guide(ModItems.CHILI_CON_CARNE.get(), "Spicy beef and tomato; warms you through", "Eat it before a cold trip");
        guide(ModItems.BORSCHT.get(), "Beetroot and cabbage soup", "Eat it (the bowl comes back)");
        guide(ModItems.TOMATO_SOUP.get(), "Smooth tomato and onion soup", "Eat it (the bowl comes back)");
        guide(ModItems.PORRIDGE.get(), "Oats cooked in milk with honey", "Eat it the morning after");
        guide(ModItems.COOKED_RICE.get(), "Plain steamed rice", "Eat it, or wrap it in dried kelp for rice balls");
        guide(ModItems.RICE_BALL.get(), "Pressed rice in a strip of dried kelp", "Eat it on the go");
        guide(ModItems.POPCORN.get(), "Popped in the kettle", "Snack on it");
        guide(ModItems.CRANBERRY_SAUCE.get(), "Tart and sweet", "Make a Harvest Feast, or eat it");
        for (RegistryObject<Item> jam : ModItems.JAMS) {
            if (jam != ModItems.MARMALADE) guide(jam.get(), "Fruit cooked down with sugar", "Spread it on bread for jam toast");
        }
        guide(ModItems.MARMALADE.get(), "Oranges cooked down with sugar, peel and all", "Spread it on bread for jam toast");
        guide(ModItems.JAM_TOAST.get(), "Bread with a thick layer of jam", "Eat it");
        guide(ModItems.MOTHER_OF_VINEGAR.get(), "The culture that turns beer to vinegar", "Leave it in an open jar of beer: vinegar in a day");
        guide(ModItems.PICKLES.get(), "Cucumbers pickled in vinegar", "Snack on them");
        guide(ModItems.SAUERKRAUT.get(), "Soured, salted cabbage", "Eat it");
        guide(ModItems.KIMCHI.get(), "Spicy fermented cabbage", "Eat it");
        guide(ModItems.DRIED_BERRIES.get(), "Sun-dried berries", "Snack on them, or mix into granola");
        guide(ModItems.GRANOLA.get(), "Oats, honey and dried fruit", "Snack on it");
        guide(ModItems.HARVEST_FEAST.get(), "A roast chicken dinner for six", "Place it, then right-click with a bowl");
        guide(ModItems.HARVEST_FEAST_SERVING.get(), "Chicken, roast potato and all the trimmings", "Eat it (the bowl comes back)");
        for (Pies.Pie pie : Pies.all()) {
            guide(pie.item().get(), "A whole pie: 4 slices", "Place it and right-click for a slice");
            guide(pie.slice().get(), "A quarter of a pie", "Eat it; 4 slices make a whole pie");
        }

        guide(ModItems.BREW_KETTLE.get(), "Mashes and boils; needs heat below", "Place over a lit campfire or magma");
        guide(ModItems.FERMENTING_VAT.get(), "Turns wort into beer, juice into wine", "Fill, add yeast, sneak-click to close");
        guide(ModItems.PRESERVING_JAR.get(), "Grows yeast and cultures", "Add items + liquid, click to close");
        guide(ModItems.SPENT_GRAIN.get(), "Left over from mashing", "Mix into dough for spent-grain bread, or compost it");
        guide(ModItems.SOURDOUGH_STARTER.get(), "A living culture (you keep it when mixing dough)",
                "Mix with dough for sourdough, or jar it with sweet wort for Ale Yeast");
        guide(ModItems.ALE_YEAST.get(), "Cultured brewer's yeast", "Add to a Fermenting Vat for a star");
        guide(ModItems.WINE_YEAST.get(), "Cultured wine yeast, from fruit lees", "Add to a vat of juice for a star");
        guide(ModItems.WINE_BOTTLE.get(), "Holds one bottle of wine or mead", "Fill it from a vat, cask or keg");

        for (CaskWood wood : CaskWood.values()) {
            guide(ModItems.CASKS.get(wood).get(), WOOD_CHARACTER.get(wood) + (wood.nether() ? ": ages twice as fast, no aging star"
                    : ": ideal for " + idealFor(wood)), "Fill with a fermented drink; fit a Tap to pour");
        }
        guide(ModItems.WINE_RACK.get(), "Stores and shows 6 bottles of wine", "Right-click a hole with a bottle");
        guide(ModItems.BOTTLE_SHELF.get(), "Shows 6 drinks standing on two boards", "Right-click a spot with any drink");
        guide(ModItems.WINE_DISPLAY.get(), "Shows 3 bottles of wine lying on their sides", "Right-click a shelf with a bottle");
        guide(ModItems.KEG.get(), "Serves beer; keeps it when broken", "Fill it, then pour into a Beer Mug");
        guide(ModItems.TAP.get(), "A spigot for casks", "Right-click the front of a Cask");
        guide(ModItems.MUG.get(), "Holds a beer, cider or perry", "Right-click a tapped Cask or a Keg");
        guide(ModItems.HYDROMETER.get(), "Reads brews and crops", "Right-click a station, cask or crop");
        guide(ModFluids.SWEET_WORT.bucket.get(), "Unhopped wort", "Boil with hops, or ferment for Plain Ale");
        guide(ModFluids.HOPPED_WORT.bucket.get(), "Ready to ferment", "Pour into a Fermenting Vat");
        guide(ModItems.POT_STILL.get(), "Distils washes and wines into spirits", "Place over a fire; pour in a wash");
        guide(ModItems.SPIRIT_BOTTLE.get(), "Holds one bottle of spirit", "Right-click the still (or a tapped cask)");
        guide(ModFluids.CORN_WASH.bucket.get(), "Fermented corn mash", "Distil it in a Pot Still for bourbon");
        guide(ModFluids.POTATO_WASH.bucket.get(), "Fermented potato mash", "Distil it in a Pot Still for vodka");
        guide(ModFluids.STILLAGE.bucket.get(), "What's left in the still", "Pour it on farmland to feed the soil");
        guide(Drinks.PLAIN_ALE.item().get(), "Unhopped ale; also the malt wash", "Drink it, or distil it into whiskey");
        guide(ModFluids.POMACE_MASH.bucket.get(), "Grape pomace steeped in hot water", "Ferment it with Wine Yeast");
        guide(ModFluids.CANE_JUICE.bucket.get(), "Pressed from sugar cane", "Boil it down in a Brew Kettle for molasses");
        guide(ModItems.MOLASSES.get(), "Cane juice boiled down: dark and sticky", "Ferment it for rum, or use it like sugar");
        guide(ModFluids.MOLASSES.bucket.get(), "Cane juice boiled down: dark and sticky", "Ferment it with Ale Yeast for rum");
        guide(ModFluids.RUM_WASH.bucket.get(), "Fermented molasses", "Distil it in a Pot Still for rum");
        guide(ModItems.ROASTED_AGAVE.get(), "A roasted agave heart, sweet and smoky", "Press it for agave juice");
        guide(ModFluids.AGAVE_JUICE.bucket.get(), "Pressed from roasted agave", "Ferment it for tequila, or boil it for syrup");
        guide(ModFluids.AGAVE_WASH.bucket.get(), "Fermented agave juice", "Distil it in a Pot Still for tequila");
        guide(ModItems.AGAVE_SYRUP.get(), "Agave juice boiled down", "Use it like sugar in cooking and baking");
        guide(ModItems.AGAVE_FIBER.get(), "Tough fiber left from pressing agave", "Craft 3 into 2 string, or compost it");
        guide(ModItems.GIN_BASKET.get(), "Holds botanicals in the still's vapor", "Right-click a Pot Still to fit it");
        guide(ModItems.LEMON_PEEL.get(), "Bright citrus zest", "A botanical for gin; flavors bitters");
        guide(ModItems.ORANGE_PEEL.get(), "Sweet citrus zest", "A botanical for gin; flavors spiced rum");
        guide(Drinks.GIN.item().get(), "Vodka distilled through botanicals", "Drink it: refreshing");
        guide(ModItems.BLACK_FOREST_CAKE.get(), "Chocolate, cream and kirsch cherries", "Place it, then right-click for a slice (6)");
        guide(ModItems.BLACK_FOREST_CAKE_SLICE.get(), "A slice of rich chocolate cake", "Eat it");
        guide(ModItems.LAGER_YEAST.get(), "Ale yeast grown in the cold", "Ferment pale wort cold with it for lager");
        guide(ModItems.GREEN_WHEAT_MALT.get(), "Sprouted wheat", "Dry in a Kiln on Light");
        guide(ModItems.WHEAT_MALT.get(), "Pale wheat malt", "Grind in a Millstone");
        guide(ModItems.WHEAT_GRIST.get(), "Milled wheat malt", "Half or more of a mash makes wheat beer");
        guide(ModItems.POLISHED_RICE.get(), "Rice milled white", "Steam it in a kettle of water");
        guide(ModItems.RICE_BRAN.get(), "The husk milled off rice", "Compost it");
        guide(ModItems.STEAMED_RICE.get(), "Soft steamed rice", "Eat it, or grow koji on it in a warm Jar");
        guide(ModItems.KOJI_RICE.get(), "Rice grown with koji mold", "Stir 2 a bucket into water for a sake mash");
        guide(ModItems.ROASTED_COFFEE.get(), "Roasted coffee beans", "Grind in a Millstone");
        guide(ModItems.GROUND_COFFEE.get(), "Fresh-ground coffee", "Brew in a kettle of water with glass bottles");
        guide(ModFluids.SAKE_MASH.bucket.get(), "Koji rice in water", "Ferment it cool with Wine Yeast for sake");
        guide(ModItems.VANILLA_POD.get(), "Green pods from a vine on jungle trees", "Plant on a jungle log's side, or cure on a Drying Rack");
        guide(ModItems.CURED_VANILLA.get(), "Dark, fragrant cured vanilla", "Steep it with rum for spiced rum");
        guide(Drinks.LIMONCELLO.item().get(), "Lemon peel steeped in vodka or brandy", "Sip it: refreshing");
        guide(Drinks.APPLE_CROWN_WHISKEY.item().get(), "Malt whiskey steeped with apples and honey, fit for a king", "Drink it: warming");
        guide(Drinks.HERBAL_LIQUEUR.item().get(), "Brandy steeped with herbs and honey", "Drink it to settle nausea");
        guide(Drinks.SPICED_RUM.item().get(), "Rum steeped with vanilla, orange and ginger", "Drink it: warming");
        guide(Drinks.AROMATIC_BITTERS.item().get(), "Vodka steeped with wormwood, anise and orange", "Drink it to cure a hangover or nausea");
        guide(ModFluids.POMACE_WASH.bucket.get(), "Fermented grape pomace", "Distil it in a Pot Still for grappa");
        guide(Drinks.KIRSCH.item().get(), "Cherry brandy, clear and strong", "Drink it, or bake it into Black Forest cake");
        guide(Drinks.SLIVOVITZ.item().get(), "Plum brandy", "Age it, or drink it");
        guide(ModItems.MUG_RACK.get(), "A board with pegs for four mugs", "Hang empty mugs on it");
        guide(ModItems.HOP_GARLAND.get(), "Fresh hop bines, draped along a wall", "Hang them in a row: they join up");
        guide(ModItems.TAVERN_SIGN.get(), "A painted sign on an iron bracket", "Repaint it: right-click with a mug, wine, spirit or cask");
        for (CaskWood wood : CaskWood.values()) {
            guide(ModItems.BAR_COUNTERS.get(wood).get(), "A paneled bar with a brass foot rail", "Set drinks down on its top");
            guide(ModItems.BAR_STOOLS.get(wood).get(), "A tall stool, the height for a bar", "Right-click to sit; sneak to stand up");
        }
        for (var bundle : List.of(new String[]{"hop_bundle", "Hop cones, tied in a bunch to dry"}, new String[]{"lavender_bundle",
                "Lavender, hung upside down to dry"}, new String[]{"garlic_braid", "Garlic bulbs braided by their stalks"},
                new String[]{"chili_string", "Red chilies threaded on a string"})) {
            guide(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(SeedToCellar.id(bundle[0])), bundle[1],
                    "Hang it under a block or on a wall");
        }
        generatedGuides();
    }

    /** Text for JEI recipe pages and info pages, and Jade's settings screen. */
    private void jeiAndJade() {
        String j = "jei." + SeedToCellar.MOD_ID + ".";
        add(j + "seconds", "%s s");
        add(j + "minutes", "%s min");
        add(j + "days", "%s in-game day(s)");
        add(j + "malting.times", "Steep %s, then sprout %s");
        add(j + "kiln.setting", "Roast: %s, %s");
        add(j + "drying.time", "%s in shade or at night");
        add(j + "drying.weather", "Sun: twice as fast. Rain: paused");
        add(j + "mill.cranks", "%s crank per item");
        add(j + "cooking", "Brew Kettle: Cooking");
        add(j + "cooking.time", "%s over heat, one serving");
        add(j + "kettle.mash", "Mash grist in water over heat");
        add(j + "kettle.strength", "Per bucket: 1 Light, 2 Normal, 4 Strong");
        add(j + "kettle.boil", "Boil with 1 dried hops per bucket");
        add(j + "kettle.heat", "Needs fire, lava or magma below");
        add(j + "ferment.strength", "Strength: %s to %s");
        add(j + "ferment.conditions", "Best at %s; takes %s");
        add(j + "ferment.yeast_wild_ok", "%s earns a star (wild works)");
        add(j + "ferment.yeast_needed", "Needs the right yeast");
        add(j + "malt.pale", "Pale");
        add(j + "malt.amber", "Amber");
        add(j + "malt.black", "Black");
        add(j + "malt.none", "no %s");
        add(j + "malt.corn", "Corn");
        add(j + "malt.wheat", "Wheat");
        add(j + "jar.needs", "Needs a %s place");
        add(j + "malt.potato", "Potato");
        add(j + "distilling.run", "One run: half comes over as spirit");
        add(j + "distilling.again", "The same spirit, distilled once more");
        add(j + "distilling.min_runs", "After %s runs (the next run)");
        add(j + "distilling.filter", "Needs a charcoal filter (uses one)");
        add(j + "distilling.heat", "Needs fire, lava or magma below");
        add(j + "distilling.basket", "Gin Basket: this + %s more different");
        add(j + "distilling.basket_star", "%s different botanicals earn its star");
        add(j + "jar.time", "Lid closed for %s");
        add(j + "jar.steeps", "Steeps the whole jar of spirit");

        add(j + "info.fermenting_vat", "Fill with wort (for beer) or juice (for wine, cider, perry; honey water for mead), add yeast if "
                + "you have it, then close the lid (sneak + right-click) to start. "
                + "Temperature comes from where it stands: the biome, one step cooler in a cellar (no sky), Cold next to ice or snow, "
                + "Warm next to fire (with Serene Seasons, a step cooler in winter and warmer in summer, except in tropical biomes "
                + "like deserts, savannas and jungles). The style's ideal temperature earns a star. No yeast? It still ferments with wild yeast, "
                + "just slower and without the yeast star.");
        add(j + "info.ale_yeast", "Every batch leaves lees, and lees become yeast: 1 from a wild ferment, 2 when you used yeast. "
                + "You can also grow it in a Preserving Jar from a Sourdough Starter and sweet wort.");
        add(j + "info.wine_yeast", "Ferment any juice (or honey water) wild, with no yeast, and its lees become Wine Yeast: 1 from a "
                + "wild batch, 2 when you used yeast. Wines, cider, perry and mead earn their yeast star with it.");
        add(j + "info.wine_bottle", "Wines and mead are poured into wine bottles (250 mB); ciders and perry into mugs; juices into "
                + "glass bottles. Right-click a vat, a tapped cask, a keg, a tub or a press with the empty vessel to fill it.");
        add(j + "info.juices", "Pressed or stomped fruit juice: fill a glass bottle from the Fruit Press, the Crushing Tub or a vat. "
                + "Refreshing, and no alcohol. Ferment it in a vat with Wine Yeast to make wine (apples: cider; pears: perry).");
        add(j + "info.honey_water", "Stir 2 honey bottles into every bucket of water in a heated Brew Kettle and it becomes honey water "
                + "(the bottles come back). Ferment it in a vat with Wine Yeast to make mead.");
        add(j + "info.soft_drinks", "Made in a heated Brew Kettle: lemonade from pressed lemon juice with 2 sugar (or sorghum syrup) "
                + "stirred into every bucket; elderflower cordial from elderflowers, sugar and a lemon in water, bottled. "
                + "Refreshing, and no alcohol.");
        add(j + "info.olive_oil", "Press olives in the Fruit Press, then right-click the press (or a tank of olive oil) with an empty "
                + "glass bottle. Used in salad, bruschetta and garlic bread; the bottle comes back.");
        add(j + "info.sorghum_syrup", "Press sorghum for its juice, pour it into a heated Brew Kettle with empty glass bottles in the "
                + "bottle slot and nothing else, and it boils down to syrup. Use it anywhere a recipe takes sugar.");
        add(j + "mixing.per_bucket", "%s per bucket, stirred in over heat");
        add(j + "mixing.boil_down", "Boiled down alone over heat");
        for (CaskWood wood : CaskWood.values()) {
            String name = titleCase(wood.id());
            add(j + "info." + wood.id() + "_cask", "Pour in a fermented drink and fit a Tap to serve. Drinks rest here: after 1 day "
                    + "they are conditioned (+1 star). A drink that ages earns the aging star after its years (1 in-game day = 1 year) "
                    + "in one of its ideal woods, or twice as long in any other. "
                    + (wood.nether() ? name + " casks age twice as fast (drinks condition in half a day and gain 2 years a day) "
                    + "but never give the aging star. "
                    : name + " (" + WOOD_CHARACTER.get(wood).toLowerCase(java.util.Locale.ROOT) + ") is ideal for " + idealFor(wood) + ". ")
                    + "Adding more of the same drink blends the ages. Break it and it keeps its contents; aging pauses until it's placed again."
                    + (wood.nether() ? "" : " Use flint and steel on it while it's empty to char it inside: any charred cask is ideal "
                    + "for whiskey, and bourbon is only called Bourbon (and ages on time) in charred oak."));
        }
        add(j + "info.wine_rack", "Holds 6 bottles of wine or mead, lying in its holes with their necks out. Right-click a hole with a "
                + "bottle to rack it, or a full hole with an empty hand to take the bottle back. Hoppers fill and empty it, and a comparator "
                + "reads how full it is. Racked bottles don't age: only casks age drinks.");
        add(j + "info.brew_kettle", "Mash grist, boil wort with hops, cook dishes and stir in honey or sugar, over heat: a lit campfire, "
                + "fire, lava or magma under it. A Brew Kettle is also the Brewer villager's workstation: villages with a "
                + "Brewhouse have one.");
        add(j + "info.copper", "Its copper weathers: left long enough, it turns green in patches like a copper block, then all "
                + "over. It works just the same. Wax it with honeycomb to keep the look it has; an axe takes the wax off, or else "
                + "scrapes back a stage. Broken, it keeps both.");
        add(j + "info.bottle_shelf", "A half-deep wall shelf: six drinks stand on its two boards, shown just as they look in the "
                + "hand, labels and all. It takes any drink: wine bottles, mugs, bottles of juice. Right-click a spot with a drink "
                + "to put it there, or with an empty hand to take it back. Hoppers and comparators work as on the Wine Rack.");
        add(j + "info.wine_display", "An open display case: a bottle of wine or mead lies along each of its three shelves, its "
                + "whole side and label showing. Right-click a shelf with a bottle to lay it there, or with an empty hand to take "
                + "it back. Hoppers and comparators work as on the Wine Rack.");
        add(j + "info.mug_rack", "A board for the wall with four pegs: empty mugs hang from them by their handles. Right-click a "
                + "peg with an empty mug to hang it, or with an empty hand to take it down. Hoppers and comparators work as on "
                + "the Wine Rack.");
        add(j + "info.tavern_sign", "A painted sign that hangs out from a wall on an iron bracket, its picture on both sides. "
                + "Right-click it to repaint it with what you serve: a mug of beer or cider paints an alehouse's foaming mug, "
                + "wine or mead a bunch of grapes, any spirit a pot still, a cask a cask.");
        add(j + "info.keg", "A small portable keg with its own tap. Beer conditions here after a day (+1 star) but doesn't age. "
                + "Break it to carry the beer with you.");
        add(j + "info.hydrometer", "Right-click any station, cask or crop for a read-out: progress, temperature, quality and age.");
        add(j + "info.sickle", "Right-click a crop with a sickle to harvest every ripe crop in the 3x3 around it and replant them, "
                + "all for one point of durability. Unripe crops are left alone. Works on every crop, bushes, herbs, sweet "
                + "berries, hops and grapes; Fortune gives more. Grain (wheat, barley, rye, oats, sorghum, rice) harvested with a "
                + "sickle also gives Straw. It cuts plants and leaves quickly too.");
        add(j + "info.drying_rack", "Hang up to four items on it (right-click; sneak to hang several). Hop cones become Dried "
                + "Hops, chili becomes Dried Chili, berries and grapes dry into dried berries and raisins, and raw meat becomes Jerky. Sun on the rack dries twice as fast; rain or snow "
                + "on it pauses drying; shade, night and indoors dry at the normal rate. Right-click with an empty hand to take "
                + "dried items (sneak to take anything back). Hoppers hang items and take dried ones out from below.");
        add(j + "info.kettle_cooking", "The Brew Kettle cooks as well as brews. Put a dish's ingredients in the four slots (any slots, "
                + "nothing else), its liquid in the tank (water, milk or a mug of ale) and a bowl or bottle in the slot above the output, "
                + "over heat. It cooks one serving at a time and keeps going while there's enough for another. The output slot shows a "
                + "faded picture of the dish when the ingredients are right, and the screen says what's missing. Brewing comes first.");
        add(j + "info.pie", "Place a pie on a table or any solid block, then right-click to take a slice (four in all). Crafting "
                + "turns a pie into four slices and four slices back into a pie.");
        add(j + "info.harvest_feast", "Place the feast on a table, then right-click with a bowl to serve yourself (six servings). When "
                + "it's all gone, right-click the leftovers to clear the platter.");
        add(j + "info.vinegar", "Leave beer in an open Preserving Jar (no lid, nothing else in it) and in two days it sours "
                + "into vinegar, and a Mother of Vinegar forms. Leave the mother in the next jar of beer and it takes one day, "
                + "and grows another mother. Use vinegar in the jar for pickles.");
        add(j + "info.straw", "Harvest ripe grain (wheat, barley, rye, oats, sorghum or rice) with a Sickle: one Straw each. "
                + "Four make a block of Thatch. It also composts.");
        add(j + "info.compost_bin", "Fill it with 16 plant leftovers: anything the vanilla composter takes, plus spent grain and "
                + "rotten flesh (sneak to add a whole stack). Once full it composts for a day, then turns dark: right-click it for "
                + "4 Compost. Hoppers can fill and empty it.");
        add(j + "info.compost", "Use it on farmland (or on a crop growing there) to make Fertile Farmland with 3 fertility. "
                + "Crops on it, vanilla ones too, grow about 50% faster and often give one extra harvest. Each harvest may use "
                + "up a point of fertility (about 9 harvests per compost); top it up with more. Dispensers can spread it.");
        add(j + "info.climate", "Every crop has a favorite climate (shown on its seeds): Cold, Temperate, Warm or Hot, from the "
                + "biome's temperature. Elsewhere it grows at half speed. Glass up to 8 blocks above a crop makes a greenhouse, "
                + "which counts as Warm.");
        for (Crop crop : Crops.all()) {
            if (!crop.growsWild()) continue;
            if (crop.style == Crop.Style.VINE) {
                add(j + "info." + crop.seedId, "Found by breaking wild " + crop.displayName.toLowerCase(Locale.ROOT) + " vines ("
                        + crop.wild.where() + "). " + crop.plantingHint() + ". The vine climbs up to 3 trellises and gives "
                        + crop.produceName.toLowerCase(Locale.ROOT) + " again and again. Shears cut grape leaves off a grown vine; "
                        + "it grows back.");
                continue;
            }
            if (crop.style == Crop.Style.SUCCULENT) {
                add(j + "info." + crop.seedId, "Wild " + crop.displayName.toLowerCase(Locale.ROOT) + " grows in " + crop.wild.where()
                        + ", often in flower. Cut one in flower (a tall stalk of yellow blossom) for its heart and 1-2 pups. "
                        + crop.plantingHint() + ": it needs no water, but grows slowly (best where it's hot). In flower, "
                        + "right-click it to harvest the heart and replant.");
                continue;
            }
            if (crop.isPerennial()) {
                String verb = crop.style == Crop.Style.HERB ? "Cut it" : "Pick it";
                add(j + "info." + crop.seedId, "Found growing wild in " + crop.wild.where() + ". " + crop.plantingHint() + ". "
                        + verb + " when ripe (right-click) and it grows back, again and again."
                        + (crop.hasFlowers() ? " While it's flowering you can pick the " + crop.flowersName.toLowerCase(Locale.ROOT)
                                + " instead, but then it won't fruit until next time." : "")
                        + (crop.thorny ? " Mind the thorns." : ""));
                continue;
            }
            String grass = crop.grassDrop == null ? "" : crop.grassDrop.coldOnly() ? ", and sometimes grass in cold biomes" : ", and sometimes grass";
            add(j + "info." + crop.seedId, "Found by breaking wild " + crop.displayName.toLowerCase(Locale.ROOT)
                    + " (" + crop.wild.where() + ")" + grass + ". " + crop.plantingHint() + "; right-click it when ripe to harvest and replant."
                    + switch (crop.style) {
                        case TALL -> " It needs an empty block above it to finish growing.";
                        case PADDY -> " The water must sit on dirt, mud or farmland.";
                        default -> "";
                    });
        }
        for (FruitTree tree : FruitTrees.all()) {
            String lower = tree.displayName.toLowerCase(Locale.ROOT);
            add(j + "info." + tree.name + "_sapling", "Found on wild " + lower + " trees (" + tree.where + ") and in wild orchards: "
                    + "break their leaves for a sapling. Plant it on grass or dirt and it grows into a " + lower + " tree. Its leaves "
                    + "blossom, set fruit and ripen, again and again; right-click ripe leaves to pick the "
                    + (tree.ownFruit() ? tree.fruitName : "apples").toLowerCase(Locale.ROOT) + ". Ripe fruit with nothing under "
                    + "it falls by itself after a while, so a hopper under the tree collects it.");
        }
        add(j + "info.crushing_tub", "Put up to 16 of one fruit in the tub (right-click, or a hopper), then jump in and stomp: "
                + "every landing crushes toward the next fruit, and its liquid collects in the tub. Red grapes give red must, for "
                + "red wine; other soft fruit give juice, but less of it than the Fruit Press. The tub holds 4 buckets of one "
                + "liquid: take it out with a bucket, a bottle or a pipe. Sneak with an empty hand to take the fruit back.");
        add(j + "info.fruit_press", "Put up to 16 of one fruit in the cage (right-click, or a hopper on top), then crank the press "
                + "with an empty hand, or give it redstone pulses. The screw comes down turn by turn; each full pressing squeezes "
                + "4 fruit into juice (olives into oil) in the tray and leaves pomace behind. Take the liquid out with a bucket, "
                + "a bottle or a pipe; sneak with an empty hand (or a hopper underneath) to take the pomace. A Fruit Press is also "
                + "the Vintner villager's workstation: villages with a Vineyard have one.");
        add(j + "crushing.stomps", "Stomps per fruit: %s");
        add(j + "crushing.jump", "Jump in the tub to stomp");
        add(j + "pressing.cranks", "%s at a time, %s turns of the screw");
        // The Growing page: what you plant, what you harvest
        add(j + "growing", "Growing");
        for (Crop crop : Crops.all()) {
            String then = switch (crop.style) {
                case ROW, TALL, PADDY -> "right-click it when ripe to harvest and replant";
                case BUSH -> crop.hasFlowers() ? "pick its flowers while it blooms, or the berries when ripe; it grows back"
                        : "pick it when ripe; it grows back";
                case HERB -> "cut it when grown; it grows back";
                case VINE -> "pick the bunches when ripe; shears on a grown vine cut grape leaves";
                case SUCCULENT -> "cut it in flower for its heart and pups";
            };
            add(j + "growing." + crop.name, crop.plantingHint() + "; " + then + ".");
        }
        add(j + "growing.tree", "Plant it like any sapling. Pick the ripe fruit from its leaves; shears take the leaves.");
        add(j + "growing.hops", "Plant it on the bottom Trellis of a stack standing on soil; pick the cones when ripe.");
        add(j + "growing.vanilla", "Plant a pod on the side of a jungle log; pick the green pods when they hang.");
        // Info pages for liquids and wild plants
        add(j + "info.buckets", "A bucket of one of our liquids. Fill an empty bucket from a vessel that holds it (a vat, cask, keg, "
                + "kettle, still, press or tub), and pour it into one that takes it.");
        add(j + "info.poured", "Made a serving at a time, in its bottles or mugs, never as a liquid; another mod's item drain or tank "
                + "can pour it out of them.");
        add(j + "info.ginger_beer", "Ginger, a sweetener and Ale Yeast in a Preserving Jar of water, lid on, for a day. Fill mugs from "
                + "the jar. No alcohol and no stars: a soft drink.");
        for (Crop crop : Crops.all()) {
            if (!crop.hasWild()) continue;
            add(j + "info.wild_" + crop.name, "Wild " + crop.displayName.toLowerCase(Locale.ROOT) + " grows in " + crop.wild.where()
                    + ". Break it for what you plant (" + crop.seedName + "); shears take the whole plant.");
        }
        add(j + "info.wild_hops", "Wild hops grow in forests. Break them for a Hop Rhizome; shears take the whole plant.");
        add(j + "info.hop_rhizome", "Found by breaking wild hops in forests. Plant it on the bottom Trellis of a stack standing on dirt, "
                + "grass or farmland. The vine climbs up to 3 trellises and gives cones again and again.");
        add(j + "info.mug", "Right-click a tapped cask or a keg to pour a drink (250 mB). Machines like Create's Spout can fill it too.");
        add(j + "info.pot_still", "Pour a wash (or a wine, or a spirit) into the pot and light a fire under it. A run boils the "
                + "whole pot: half comes over into the glass spirit safe as spirit, half is left as stillage. One run makes a wash "
                + "into its spirit; press Run again to pour it back in and distil it once more, for the craft star (each run halves "
                + "it again). Plain Ale (the malt wash) makes malt whiskey, a corn wash bourbon, a potato wash vodka. Charcoal in "
                + "the filter turns grain or potato spirit into vodka, and so does a third run. Take the spirit with a bucket or a "
                + "Spirit Bottle; drain the stillage with a bucket or a pipe underneath.");
        add(j + "info.washes", "Washes are made like beer: mash cornmeal or potatoes with some malt in the Brew Kettle (half or "
                + "more corn or potato), then ferment the sweet wort with Ale Yeast, at Mild or Warm. Plain Ale is the malt wash.");
        add(j + "info.stillage", "What's left in the still after a run. Pour a bucket of it on farmland: the 3x3 there gains a point "
                + "of fertility (plain farmland becomes Fertile Farmland).");
        add(j + "info.black_forest_cake", "Place the cake on a table or any solid block, then right-click to take a slice (six "
                + "in all), cut from one side like a cake. Crafting turns a cake into six slices and six slices back into a cake.");
        add(j + "info.lager_yeast", "Put Ale Yeast in a Preserving Jar with sweet wort (or water and sugar) and close the lid "
                + "somewhere Cold (in a snowy biome, or next to ice or snow): a day later it's Lager Yeast. Ferment pale hopped wort with it, "
                + "Cold, for lager; its lees give more lager yeast.");
        add(j + "info.sake", "Mill rice into polished rice (and bran), steam it in a heated kettle of water, then grow koji on it: "
                + "2 steamed rice in a Preserving Jar somewhere Warm, two days (one with a koji rice added). Stir 2 koji rice per bucket "
                + "into a heated kettle of water for sake mash, and ferment that with Wine Yeast, Cool.");
        add(j + "info.coffee", "Roast coffee beans in a Kiln on Medium, grind them in a Millstone, and brew them in a heated kettle of "
                + "water with glass bottles in its bottle slot. Coffee gives a short burst of Haste.");
        add(j + "info.wheat_beer", "Malt wheat like barley (Malting Tub, then the Kiln on Light), grind it, and mash it so that half or "
                + "more of the grist is wheat: boiled with hops and fermented with ale yeast at Mild, it's wheat beer.");
        add(j + "info.liqueurs", "Liqueurs steep in a Preserving Jar: pour in a spirit (vodka or brandy for the fruit liqueurs), "
                + "add the fruit or herbs and the sugar, and close the lid. A day later (two for herbal liqueur, spiced rum and bitters) "
                + "the whole jar has become the liqueur, with the spirit's stars. Bottles from honey or syrup come back.");
        add(j + "info.vanilla", "Vanilla climbs the trunks of jungle trees. Plant a pod on the side of a jungle log; it flowers, "
                + "then hangs with green pods: right-click to pick 1-2 and it flowers again. Cure pods on a Drying Rack.");
        add(j + "info.gin", "Fit a Gin Basket to a Pot Still (right-click it), then put botanicals in its four slots: juniper "
                + "and at least two others (coriander, anise, lavender, mint, wormwood, ginger, cucumber, lemon or orange peel). "
                + "Distil vodka and the vapor runs through them: gin. Four different botanicals earn gin its craft star. Each run "
                + "uses one of each.");
        add(j + "info.spirit_names", "Spirits change their name as they age in a cask: New Make becomes Malt Whiskey at 3 years, "
                + "White Dog becomes Bourbon after 2 years in charred oak (Corn Whiskey in anything else), Eau-de-vie becomes Brandy at "
                + "2, White Rum turns Gold at 2 and Dark at 6, Tequila Blanco becomes Reposado at 1 and Añejo at 4. Their color "
                + "deepens too, from almost clear toward amber.");
        add(j + "info.rum", "Press sugar cane for cane juice, boil it down alone in a heated Brew Kettle into molasses, ferment that "
                + "with Ale Yeast (Mild or Warm) into rum wash, then distil it. Bottle molasses with glass bottles to use like sugar.");
        add(j + "info.tequila", "Cut a wild agave in flower for its heart, roast it in a Kiln on Medium, press the roasted agave for "
                + "juice, ferment it with Ale Yeast (Mild or Warm) into agave wash, then distil it. Boil agave juice into syrup with "
                + "glass bottles in the kettle.");
        add(j + "info.grappa", "Steep grape pomace in a heated Brew Kettle of water (4 a bucket) to make pomace mash, ferment it "
                + "with Wine Yeast (Mild or Warm), then distil the pomace wash.");
        add(j + "info.spirit_bottle", "Right-click the still (or a tapped cask of spirit) with an empty Spirit Bottle to fill it "
                + "(250 mB). Spirits earn their craft star in the still: distilled twice (vodka: three runs or a charcoal filter).");
        add(j + "info.drinks", "Quality: 1 star to start, +1 for cultured yeast (ale yeast for beers, wine yeast for wines, cider, "
                + "perry and mead; spirits keep their wash's), +1 for the right fermenting temperature, +1 for the craft step "
                + "(beers and wines: a day's rest in a cask or keg; spirits: a second run through the still, vodka three or a "
                + "charcoal filter, gin four botanicals; liqueurs keep their spirit's), +1 for aging (drinks that age). "
                + "Better drinks give longer effects and gentler hangovers.");

        advancement("root", "From Seed to Cellar", "Find barley seeds or a hop rhizome in the wild");
        advancement("first_harvest", "First Harvest", "Harvest a crop you grew or found");
        advancement("fertile_ground", "Fertile Ground", "Spread Compost on farmland");
        advancement("orchardist", "Orchardist", "Plant every kind of fruit tree");
        advancement("botanist", "Botanist", "Grow every herb: mint, lavender and wormwood");
        advancement("malted", "Malted", "Steep and sprout barley in a Malting Tub");
        advancement("brew_day", "Brew Day", "Mash grist into wort in a Brew Kettle");
        advancement("first_pint", "First Pint", "Drink a beer you brewed");
        advancement("culture_club", "Culture Club", "Get Ale Yeast from lees or a Preserving Jar");
        advancement("four_stars", "Four Stars", "Pour a beer with cultured yeast, the right temperature and a day's rest");
        advancement("aged_to_perfection", "Aged to Perfection", "Age Old Ale in a cask until it earns its aging star");
        advancement("stomped", "Stomped", "Jump into a Crushing Tub full of fruit");
        advancement("vintner", "Vintner", "Make red wine, white wine and rosé");
        advancement("good_year", "A Good Year", "Pour a five-star wine");
        advancement("mother", "Mother", "Leave a drink open in a Preserving Jar until a Mother of Vinegar forms");
        advancement("first_run", "First Run", "Distil a spirit in a Pot Still");
        advancement("cold_comfort", "Cold Comfort", "Brew a lager with yeast grown in the cold");
        advancement("every_style", "Every Style", "Pour every style of beer: pale, amber, stout, old ale, lager, wheat, plain and table");
        advancement("double_distilled", "Double Distilled", "Run a spirit through the still again for its craft star");
        advancement("angels_share", "The Angel's Share", "Pour a spirit aged 10 years from a cask");
        advancement("top_shelf", "Top Shelf", "Make a five-star spirit");
        advancement("merry", "Merry!", "Have a drink or two (and stop there, if you're wise)");
        advancement("morning_after", "Morning After", "Cure a hangover: water, milk, a hearty breakfast or bitters");
        advancement("tavern_keeper", "Tavern Keeper", "Fill a Bottle Shelf with six different drinks");
        advancement("long_live_the_king", "Long Live the King", "Crown a perfect whiskey: the only six-star drink");
        add("entity.minecraft.villager." + SeedToCellar.MOD_ID + ".vintner", "Vintner");
        add("entity.minecraft.villager." + SeedToCellar.MOD_ID + ".brewer", "Brewer");

        add("hydrometer." + SeedToCellar.MOD_ID + ".vine_ripe", "Ripe: right-click to pick");
        add("hydrometer." + SeedToCellar.MOD_ID + ".vanilla_ripe", "Pods ripe: right-click to pick");
        add("hydrometer." + SeedToCellar.MOD_ID + ".fruit_0", "Leaves: no fruit yet");
        add("hydrometer." + SeedToCellar.MOD_ID + ".fruit_1", "In blossom");
        add("hydrometer." + SeedToCellar.MOD_ID + ".fruit_2", "Fruit setting (unripe)");
        add("hydrometer." + SeedToCellar.MOD_ID + ".fruit_3", "Ripe: right-click to pick");
        add("hydrometer." + SeedToCellar.MOD_ID + ".fruit_placed", "Placed leaves don't fruit");
        // Jade's settings name every panel (it won't open the title screen if one is missing).
        JadeIds.NAMES.forEach((path, name) -> add(JadeIds.configKey(path), name));
    }

    private void advancement(String key, String title, String description) {
        add("advancements." + SeedToCellar.MOD_ID + "." + key + ".title", title);
        add("advancements." + SeedToCellar.MOD_ID + "." + key + ".description", description);
    }

    /** Liquids whose names aren't their IDs title-cased. */
    private static final Map<String, String> SPECIAL_NAMES = Map.of("rose", "Rosé", "creme_de_mure", "Crème de Mûre");

    /** Each wood's character (GDD section 13), for its cask's tooltip and JEI page. */
    private static final Map<CaskWood, String> WOOD_CHARACTER = Map.of(CaskWood.OAK, "Balanced and classic",
            CaskWood.SPRUCE, "Resinous", CaskWood.BIRCH, "Light and neutral", CaskWood.JUNGLE, "Spicy", CaskWood.ACACIA, "Honeyed",
            CaskWood.DARK_OAK, "Rich", CaskWood.MANGROVE, "Earthy", CaskWood.CHERRY, "Fruity", CaskWood.CRIMSON, "Fast but rough",
            CaskWood.WARPED, "Fast but rough");

    /** "Old Ale and Red Wine": the drinks a wood is ideal for, with the fruit wines as one; "every drink that ages" for oak. */
    private static String idealFor(CaskWood wood) {
        List<Drinks.Drink> ageable = Drinks.all().stream().filter(d -> d.profile().ageable()).toList();
        List<Drinks.Drink> ideal = ageable.stream().filter(d -> d.profile().idealIn(wood)).toList();
        if (ideal.size() == ageable.size()) return "every drink that ages";
        List<String> names = new java.util.ArrayList<>();
        for (Drinks.Drink drink : ideal) {
            String name = Drinks.fruitWines().contains(drink) ? "Fruit Wines" : titleCase(drink.name());
            if (!names.contains(name)) names.add(name);
        }
        if (names.size() == 1) return names.get(0);
        return String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.get(names.size() - 1);
    }

    /** Items given their "what is this / Next:" lines so far (generatedGuides fills in the rest). */
    private final java.util.Set<net.minecraft.world.item.Item> guided = new java.util.HashSet<>();

    private void guide(net.minecraft.world.item.Item item, @org.jetbrains.annotations.Nullable String desc, @org.jetbrains.annotations.Nullable String next) {
        String base = "tooltip." + SeedToCellar.MOD_ID + "." + net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getPath();
        if (desc != null) add(base + ".desc", desc);
        if (next != null) add(base + ".next", next);
        guided.add(item);
    }

    /**
     * "What is this / Next:" for every item not written by hand (Phase 7's milestone: discover everything without JEI).
     * Finished drinks say what they're made of and where they lead (wine to brandy, cider to apple brandy...); juices which
     * wine they ferment into; buckets of drink how to serve them; storage blocks, leaves and thatch what they are. The
     * asset audit fails on any item still without a line.
     */
    private void generatedGuides() {
        Map<Drinks.Drink, String[]> drinks = new java.util.LinkedHashMap<>();
        drinks.put(Drinks.PALE_ALE, new String[]{"Pale malt and hops, fermented mild", "Drink it: refreshing"});
        drinks.put(Drinks.AMBER_ALE, new String[]{"Pale malt with a quarter amber, hopped", "Drink it: refreshing"});
        drinks.put(Drinks.STOUT, new String[]{"Pale malt with a quarter black, hopped", "Drink it: warming"});
        drinks.put(Drinks.OLD_ALE, new String[]{"Strong pale and amber wort, hopped", "Age it in a cask for its last star"});
        drinks.put(Drinks.LAGER, new String[]{"Pale and hopped, fermented cold with Lager Yeast", "Drink it: refreshing"});
        drinks.put(Drinks.WHEAT_BEER, new String[]{"Half or more wheat malt, hopped", "Drink it: refreshing"});
        drinks.put(Drinks.TABLE_BEER, new String[]{"A wort that didn't make any named style", "Drink it, then brew a named style"});
        drinks.put(Drinks.RED_WINE, new String[]{"Red grape must, fermented mild", "Drink it, age it, or distil it for brandy"});
        drinks.put(Drinks.WHITE_WINE, new String[]{"White grape juice, fermented cool", "Drink it, age it, or distil it for brandy"});
        drinks.put(Drinks.ROSE, new String[]{"Red grape juice from the press, fermented cool", "Drink it, or distil it for brandy"});
        drinks.put(Drinks.MEAD, new String[]{"Honey water, fermented with Wine Yeast", "Drink it, or age it"});
        drinks.put(Drinks.CIDER, new String[]{"Apple juice, fermented cool", "Drink it, or distil it for apple brandy"});
        drinks.put(Drinks.PERRY, new String[]{"Pear juice, fermented cool", "Drink it, or distil it for pear brandy"});
        drinks.put(Drinks.CHERRY_WINE, new String[]{"Cherry juice, fermented cool", "Drink it, or distil it for kirsch"});
        drinks.put(Drinks.PLUM_WINE, new String[]{"Plum juice, fermented cool", "Drink it, or distil it for slivovitz"});
        drinks.put(Drinks.PEACH_WINE, new String[]{"Peach juice, fermented cool", "Drink it, or distil it for peach brandy"});
        drinks.put(Drinks.SAKE, new String[]{"Koji rice mash, fermented cool", "Drink it"});
        drinks.put(Drinks.MULLED_WINE, new String[]{"Red wine warmed in the kettle with orange and sugar", "Drink it: warming"});
        drinks.put(Drinks.GINGER_BEER, new String[]{"Ginger and sugar, brewed in a jar", "Drink it: refreshing"});
        drinks.put(Drinks.LEMONADE, new String[]{"Lemon juice and sugar, from the kettle", "Drink it: refreshing"});
        drinks.put(Drinks.ELDERFLOWER_CORDIAL, new String[]{"Elderflowers, sugar and lemon, from the kettle", "Drink it: refreshing"});
        drinks.put(Drinks.COFFEE, new String[]{"Ground coffee, brewed in the kettle", "Drink it for a burst of mining speed"});
        drinks.put(Drinks.MALT_WHISKEY, new String[]{"Plain Ale, twice through the Pot Still", "Age it, or steep it with apples and honey"});
        drinks.put(Drinks.BOURBON, new String[]{"Corn wash, twice through the Pot Still", "Age it in a charred oak cask"});
        drinks.put(Drinks.VODKA, new String[]{"Potato wash, or a grain spirit filtered", "Run it through a Gin Basket, or steep liqueurs"});
        drinks.put(Drinks.BRANDY, new String[]{"Wine, twice through the Pot Still", "Age it, or steep liqueurs with it"});
        drinks.put(Drinks.APPLE_BRANDY, new String[]{"Cider, twice through the Pot Still", "Age it, or drink it"});
        drinks.put(Drinks.PEAR_BRANDY, new String[]{"Perry, twice through the Pot Still", "Age it, or drink it"});
        drinks.put(Drinks.PEACH_BRANDY, new String[]{"Peach wine, twice through the Pot Still", "Age it, or drink it"});
        drinks.put(Drinks.RUM, new String[]{"Molasses wash, twice through the Pot Still", "Age it, or steep it into spiced rum"});
        drinks.put(Drinks.TEQUILA, new String[]{"Agave wash, twice through the Pot Still", "Age it in oak or mangrove, or drink it"});
        drinks.put(Drinks.GRAPPA, new String[]{"Pomace wash, twice through the Pot Still", "Drink it"});
        drinks.put(Drinks.ORANGE_LIQUEUR, new String[]{"Orange peel steeped in vodka, brandy or rum", "Sip it: refreshing"});
        drinks.put(Drinks.UMESHU, new String[]{"Plums steeped in vodka, brandy or rum", "Sip it: refreshing"});
        drinks.put(Drinks.CHERRY_LIQUEUR, new String[]{"Cherries steeped in vodka, brandy or rum", "Sip it: refreshing"});
        drinks.put(Drinks.CREME_DE_MURE, new String[]{"Blackberries steeped in vodka, brandy or rum", "Sip it: refreshing"});
        drinks.put(Drinks.ELDERFLOWER_LIQUEUR, new String[]{"Elderflowers steeped in vodka, brandy or rum", "Sip it: refreshing"});
        drinks.put(Drinks.COFFEE_LIQUEUR, new String[]{"Ground coffee steeped in vodka, brandy or rum", "Sip it for a burst of mining speed"});
        // The juices, and the wine each one ferments into.
        Map<Drinks.Drink, Drinks.Drink> wineOf = new java.util.HashMap<>();
        wineOf.put(Drinks.APPLE_JUICE, Drinks.CIDER);
        wineOf.put(Drinks.PEAR_JUICE, Drinks.PERRY);
        wineOf.put(Drinks.RED_GRAPE_JUICE, Drinks.ROSE);
        wineOf.put(Drinks.WHITE_GRAPE_JUICE, Drinks.WHITE_WINE);
        wineOf.put(Drinks.CHERRY_JUICE, Drinks.CHERRY_WINE);
        wineOf.put(Drinks.PLUM_JUICE, Drinks.PLUM_WINE);
        wineOf.put(Drinks.PEACH_JUICE, Drinks.PEACH_WINE);
        wineOf.put(Drinks.BLUEBERRY_JUICE, Drinks.BLUEBERRY_WINE);
        wineOf.put(Drinks.BLACKBERRY_JUICE, Drinks.BLACKBERRY_WINE);
        wineOf.put(Drinks.ELDERBERRY_JUICE, Drinks.ELDERBERRY_WINE);
        wineOf.put(Drinks.CRANBERRY_JUICE, Drinks.CRANBERRY_WINE);
        wineOf.put(Drinks.SWEET_BERRY_JUICE, Drinks.SWEET_BERRY_WINE);
        wineOf.put(Drinks.GLOW_BERRY_JUICE, Drinks.GLOW_BERRY_WINE);
        wineOf.put(Drinks.MELON_JUICE, Drinks.MELON_WINE);
        Map<Drinks.Drink, Drinks.Drink> juiceOf = new java.util.HashMap<>();
        wineOf.forEach((juice, wine) -> juiceOf.put(wine, juice));

        for (Drinks.Drink drink : Drinks.all()) {
            net.minecraft.world.item.Item item = drink.item().get();
            Drinks.Drink wine = wineOf.get(drink);
            if (!guided.contains(item)) {
                String[] lines = drinks.get(drink);
                if (lines != null) guide(item, lines[0], lines[1]);
                else if (wine != null) guide(item, "Fresh-pressed juice", "Drink it, or ferment the juice for " + displayName(wine));
                else if (juiceOf.containsKey(drink)) guide(item, displayName(juiceOf.get(drink)) + ", fermented cool", "Drink it, or age it");
                else if (drink.vessel() == Vessel.GLASS_BOTTLE) guide(item, "Fresh-pressed juice", "Drink it: refreshing");
                else guide(item, null, "Drink it");
            }
            net.minecraft.world.item.Item bucket = drink.fluid().bucket.get();
            if (guided.contains(bucket)) continue;
            String vessels = switch (drink.vessel()) {
                case MUG -> "mugs";
                case WINE_BOTTLE -> "wine bottles";
                case SPIRIT_BOTTLE -> "spirit bottles";
                case GLASS_BOTTLE -> "glass bottles";
            };
            if (wine != null) guide(bucket, null, "Ferment it cool with Wine Yeast for " + displayName(wine));
            else if (drink.profile().graded()) guide(bucket, null, "Pour into a cask or vat; fill " + vessels + " from it");
            else guide(bucket, null, "Pour into a vat; fill " + vessels + " from it");
        }
        guide(ModFluids.HONEY_WATER.bucket.get(), "Honey stirred into hot water", "Ferment it with Wine Yeast for mead");
        guide(ModFluids.RED_GRAPE_MUST.bucket.get(), "Crushed red grapes, skins and all", "Ferment it with Wine Yeast for red wine");
        guide(ModFluids.LEMON_JUICE.bucket.get(), "Pressed lemons", "Stir in sugar in a heated kettle: lemonade");
        guide(ModFluids.SORGHUM_JUICE.bucket.get(), "Pressed sorghum cane", "Boil it down in a kettle for sorghum syrup");
        guide(ModFluids.OLIVE_OIL.bucket.get(), "Pressed olives", "Pour into a vat; fill glass bottles to cook with");
        guide(ModFluids.VINEGAR.bucket.get(), "Wine or cider gone sour", "Pickle with it in a Preserving Jar");
        // Storage blocks, fruit leaves and thatch say what they are.
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            net.minecraft.world.item.Item item = storage.block().get().asItem();
            String packed = storage.count() == 9 ? "Nine of them, baled for storage"
                    : storage.block().get() instanceof StorageBlocks.Crate ? "Eight of them in a wooden crate" : "Eight of them in a sack";
            if (!guided.contains(item)) guide(item, packed, "Craft it back to unpack it");
        }
        for (FruitTree tree : FruitTrees.all()) {
            String fruit = tree.displayName.toLowerCase(Locale.ROOT);
            if (!guided.contains(tree.leavesItem())) guide(tree.leavesItem(), "Leaves of " + ("aeiou".indexOf(fruit.charAt(0)) >= 0 ? "an " : "a ")
                    + fruit + " tree", "Place them as decoration");
        }
        for (var thatch : List.of(ModBlocks.THATCH_SLAB, ModBlocks.THATCH_STAIRS)) {
            if (!guided.contains(thatch.get().asItem())) guide(thatch.get().asItem(), "Straw thatch, for roofs", null);
        }
    }

    private static String displayName(Drinks.Drink drink) {
        return SPECIAL_NAMES.getOrDefault(drink.name(), titleCase(drink.name()));
    }
}
