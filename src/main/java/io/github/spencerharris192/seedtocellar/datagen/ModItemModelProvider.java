package io.github.spencerharris192.seedtocellar.datagen;

import net.neoforged.neoforge.registries.DeferredItem;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.decor.CopperWeathering;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import io.github.spencerharris192.seedtocellar.brewing.BottleLook;
import io.github.spencerharris192.seedtocellar.datagen.model.ItemModelProvider;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.FoliageColor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Item models, and what each item shows (its definition): drinks tinted with their liquid, spirits' labels in their colors,
 * weathered copper and charred casks by the blockstate the item carries, the crowned whiskey by its crown. The millstone
 * item model is hand-written (base + runner together).
 */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels() {
        // Crops: flat seeds and harvest items; the wild plant shown flat with its block texture.
        for (Crop crop : Crops.all()) {
            if (!crop.selfPlanting) basicItem(crop.seeds());
            basicItem(crop.produce());
            if (crop.hasFlowers()) basicItem(crop.flowers());
            if (crop.hasWild()) flatBlockItem(crop.wildItem(), "wild_" + crop.name);
        }
        // Fruit trees: the sapling flat, the fruit, and the leaves as a (tinted) block.
        for (FruitTree tree : FruitTrees.all()) {
            flatBlockItem(tree.saplingItem(), tree.name + "_sapling");
            if (tree.ownFruit()) basicItem(tree.fruit());
            withExistingParent(tree.name + "_leaves", modLoc("block/" + tree.name + "_leaves"));
            // in the hand, the leaves' base layer takes the default foliage green (birch's for pear), as the block does outside
            switch (tree.tint) {
                case FOLIAGE -> definition(tree.leavesItem(), model(modLoc("item/" + tree.name + "_leaves"), constantTint(FoliageColor.FOLIAGE_DEFAULT)));
                case BIRCH -> definition(tree.leavesItem(), model(modLoc("item/" + tree.name + "_leaves"), constantTint(FruitTree.BIRCH_LEAF_COLOR)));
                case NONE -> { }
            }
        }

        // Flat items: textures/item/<id>.png, made by tools/texturegen.
        for (DeferredItem<Item> item : List.of(ModItems.HOP_RHIZOME, ModItems.HOP_CONES,
                ModItems.DRIED_HOPS, ModItems.GREEN_BARLEY_MALT, ModItems.PALE_MALT, ModItems.AMBER_MALT, ModItems.BLACK_MALT,
                ModItems.PALE_GRIST, ModItems.AMBER_GRIST, ModItems.BLACK_GRIST, ModItems.WHEAT_FLOUR,
                ModItems.SPENT_GRAIN, ModItems.SOURDOUGH_STARTER, ModItems.ALE_YEAST,
                ModItems.MUG, ModItems.TAP, ModItems.HYDROMETER,
                ModItems.RYE_FLOUR, ModItems.CORNMEAL, ModItems.ROLLED_OATS, ModItems.DOUGH, ModItems.RYE_DOUGH,
                ModItems.SOURDOUGH_DOUGH, ModItems.SPENT_GRAIN_DOUGH, ModItems.BEER_BREAD_DOUGH, ModItems.CORNBREAD_BATTER,
                ModItems.MASA, ModItems.RYE_BREAD, ModItems.SOURDOUGH_BREAD, ModItems.CORNBREAD, ModItems.SPENT_GRAIN_BREAD,
                ModItems.BEER_BREAD, ModItems.TORTILLA, ModItems.ROASTED_CORN,
                ModItems.BEEF_AND_ALE_STEW, ModItems.MUTTON_AND_BARLEY_STEW, ModItems.CHILI_CON_CARNE, ModItems.BORSCHT,
                ModItems.TOMATO_SOUP, ModItems.PORRIDGE, ModItems.COOKED_RICE, ModItems.RICE_BALL, ModItems.POPCORN,
                ModItems.CRANBERRY_SAUCE, ModItems.BLUEBERRY_JAM, ModItems.BLACKBERRY_JAM, ModItems.ELDERBERRY_JAM,
                ModItems.SWEET_BERRY_JAM, ModItems.JAM_TOAST, ModItems.MOTHER_OF_VINEGAR, ModItems.PICKLES, ModItems.SAUERKRAUT,
                ModItems.KIMCHI, ModItems.DRIED_BERRIES, ModItems.GRANOLA,
                ModItems.GRAPE_LEAVES, ModItems.RAISINS, ModItems.GOLDEN_RAISINS,
                ModItems.CHERRY_JAM, ModItems.PLUM_JAM, ModItems.PEACH_JAM, ModItems.MARMALADE, ModItems.PRUNES,
                ModItems.DRIED_CHERRIES, ModItems.DRIED_APPLES, ModItems.DRIED_PEACHES, ModItems.DRIED_PEARS, ModItems.OLIVE_OIL,
                ModItems.SORGHUM_SYRUP, ModItems.CURED_OLIVES, ModItems.STUFFED_GRAPE_LEAVES, ModItems.BRUSCHETTA,
                ModItems.GARLIC_BREAD, ModItems.SALAD, ModItems.RISOTTO, ModItems.COQ_AU_VIN)) {
            basicItem(item.get());
        }

        // Drinks: the vessel, and the drink showing in it (layer 1, tinted with the drink's liquid).
        // Mugs add a foam head (a kettle drink's in its own color); wine bottles show the wine at the neck and on the label's
        // band; spirit bottles add their label's paper and print (layers 2 and 3, in each spirit's BottleLook colors).
        for (Drinks.Drink drink : Drinks.all()) {
            var model = withExistingParent(drink.name(), mcLoc("item/generated"));
            if (drink == Drinks.APPLE_CROWN_WHISKEY) {   // its own bottle, all one layer; crowned, its own icon (and a shimmer)
                withExistingParent(drink.name() + "_crowned", mcLoc("item/generated"))
                        .texture("layer0", modLoc("item/" + drink.name() + "_crowned"));
                model.texture("layer0", modLoc("item/" + drink.name()));
                definition(drink.item().get(), hasComponent(SeedToCellar.id("quality_crowned"),
                        model(modLoc("item/" + drink.name() + "_crowned")), model(modLoc("item/" + drink.name()))));
                continue;
            }
            BottleLook look = BottleLook.of(drink);
            JsonObject[] tints = look != null ? new JsonObject[]{noTint(), fluidTint(), constantTint(look.paper()), constantTint(look.accent())}
                    : drink.profile().graded() ? new JsonObject[]{noTint(), fluidTint()}
                    : new JsonObject[]{noTint(), fluidTint(), fluidTint()};
            definition(drink.item().get(), model(modLoc("item/" + drink.name()), tints));
            switch (drink.vessel()) {
                case MUG -> model.texture("layer0", modLoc("item/mug")).texture("layer1", modLoc("item/mug_liquid"))
                        .texture("layer2", modLoc("item/mug_foam"));
                case WINE_BOTTLE -> model.texture("layer0", modLoc("item/wine_bottle_full")).texture("layer1", modLoc("item/wine_bottle_wine"));
                case GLASS_BOTTLE -> model.texture("layer0", modLoc("item/juice_bottle")).texture("layer1", modLoc("item/juice_bottle_juice"));
                case SPIRIT_BOTTLE -> model.texture("layer0", modLoc("item/spirit_bottle_full")).texture("layer1", modLoc("item/spirit_bottle_spirit"))
                        .texture("layer2", modLoc("item/spirit_bottle_label")).texture("layer3", modLoc("item/spirit_bottle_print"));
            }
        }
        basicItem(ModItems.WINE_BOTTLE.get());
        basicItem(ModItems.SPIRIT_BOTTLE.get());
        // The Pot Still's icon, weathered as the item's blockstate says (its "weathering" property).
        basicItem(ModItems.POT_STILL.get());
        Map<String, JsonObject> stills = new LinkedHashMap<>();
        for (CopperWeathering.Stage stage : CopperWeathering.Stage.values()) {
            if (stage == CopperWeathering.Stage.UNAFFECTED) continue;
            String id = "pot_still_" + stage.getSerializedName();
            withExistingParent(id, mcLoc("item/generated")).texture("layer0", modLoc("item/" + id));
            stills.put(stage.getSerializedName(), model(modLoc("item/" + id)));
        }
        definition(ModItems.POT_STILL.get(), byBlockState(CopperWeathering.STAGE.getName(), stills, model(modLoc("item/pot_still"))));
        for (DeferredItem<Item> item : List.of(ModItems.MOLASSES, ModItems.AGAVE_SYRUP, ModItems.ROASTED_AGAVE, ModItems.AGAVE_FIBER,
                ModItems.GIN_BASKET, ModItems.LEMON_PEEL, ModItems.ORANGE_PEEL, ModItems.VANILLA_POD, ModItems.CURED_VANILLA,
                ModItems.BLACK_FOREST_CAKE, ModItems.BLACK_FOREST_CAKE_SLICE, ModItems.LAGER_YEAST, ModItems.GREEN_WHEAT_MALT,
                ModItems.WHEAT_MALT, ModItems.WHEAT_GRIST, ModItems.POLISHED_RICE, ModItems.RICE_BRAN, ModItems.STEAMED_RICE,
                ModItems.KOJI_RICE, ModItems.ROASTED_COFFEE, ModItems.GROUND_COFFEE)) {
            basicItem(item.get());
        }
        basicItem(ModItems.WINE_YEAST.get());

        // Block items shown flat using their block texture.
        flatBlockItem(ModItems.TRELLIS.get(), "trellis");
        flatBlockItem(ModItems.WILD_HOPS.get(), "wild_hops");

        // Block items shown as their 3D block.
        withExistingParent("malting_tub", modLoc("block/malting_tub"));
        withExistingParent("compost_bin", modLoc("block/compost_bin_3"));
        basicItem(ModItems.COMPOST.get());
        for (DeferredItem<Item> sickle : ModItems.SICKLES) {
            withExistingParent(sickle.getId().getPath(), mcLoc("item/handheld")).texture("layer0", modLoc("item/" + sickle.getId().getPath()));
        }
        basicItem(ModItems.STRAW.get());
        withExistingParent("drying_rack", modLoc("block/drying_rack"));
        basicItem(ModItems.DRIED_CHILI.get());
        basicItem(ModItems.JERKY.get());
        withExistingParent("thatch", modLoc("block/thatch"));
        withExistingParent("thatch_stairs", modLoc("block/thatch_stairs"));
        withExistingParent("thatch_slab", modLoc("block/thatch_slab"));
        basicItem(ModItems.HARVEST_FEAST.get());
        basicItem(ModItems.HARVEST_FEAST_SERVING.get());
        for (Pies.Pie pie : Pies.all()) {   // flat icons: a whole pie and a slice
            basicItem(pie.item().get());
            basicItem(pie.slice().get());
        }
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            String id = storage.block().getId().getPath();
            withExistingParent(id, modLoc("block/" + id));
        }
        withExistingParent("kiln", modLoc("block/kiln"));
        withExistingParent("brew_kettle", modLoc("block/brew_kettle"));
        Map<String, JsonObject> kettles = new LinkedHashMap<>();
        for (CopperWeathering.Stage stage : CopperWeathering.Stage.values()) {
            if (stage == CopperWeathering.Stage.UNAFFECTED) continue;
            kettles.put(stage.getSerializedName(), model(modLoc("block/brew_kettle_" + stage.getSerializedName())));
        }
        definition(ModItems.BREW_KETTLE.get(), byBlockState(CopperWeathering.STAGE.getName(), kettles, model(modLoc("item/brew_kettle"))));
        withExistingParent("fermenting_vat", modLoc("block/fermenting_vat_closed"));
        withExistingParent("preserving_jar", modLoc("block/preserving_jar_closed"));
        for (CaskWood wood : CaskWood.values()) {
            withExistingParent(wood.id() + "_cask", modLoc("block/" + wood.id() + "_cask_tapped"));
            if (!wood.nether()) {   // a charred cask item shows its char (the "charred" property, from its blockstate)
                definition(ModBlocks.CASKS.get(wood).get().asItem(), byBlockState("charred",
                        Map.of("true", model(modLoc("block/" + wood.id() + "_cask_charred_tapped"))), model(modLoc("item/" + wood.id() + "_cask"))));
            }
        }
        withExistingParent("keg", modLoc("block/keg"));
        for (String bundle : List.of("hop_bundle", "lavender_bundle", "garlic_braid", "chili_string")) withExistingParent(bundle, modLoc("block/" + bundle));
        withExistingParent("mug_rack", modLoc("block/mug_rack"));
        withExistingParent("hop_garland", modLoc("block/hop_garland"));
        withExistingParent("tavern_sign", modLoc("block/tavern_sign_ale"));
        for (CaskWood wood : CaskWood.values()) {
            withExistingParent(wood.id() + "_bar_counter", modLoc("block/" + wood.id() + "_bar_counter"));
            withExistingParent(wood.id() + "_bar_stool", modLoc("block/" + wood.id() + "_bar_stool"));
        }
        withExistingParent("wine_rack", modLoc("block/wine_rack"));
        withExistingParent("bottle_shelf", modLoc("block/bottle_shelf"));
        withExistingParent("wine_display", modLoc("block/wine_display"));
        withExistingParent("crushing_tub", modLoc("block/crushing_tub"));
        withExistingParent("fruit_press", modLoc("block/fruit_press"));
        for (DeferredItem<Item> item : List.of(ModItems.GRAPE_POMACE, ModItems.FRUIT_POMACE, ModItems.OLIVE_POMACE, ModItems.BAGASSE)) {
            basicItem(item.get());
        }

        // Buckets: NeoForge draws the vanilla bucket with the fluid's texture and color.
        for (ModFluids.Entry fluid : ModFluids.all()) definition(fluid.bucket.get(), bucket(fluid.get()));
    }

    private void flatBlockItem(Item item, String blockTexture) {
        withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + blockTexture));
    }
}
