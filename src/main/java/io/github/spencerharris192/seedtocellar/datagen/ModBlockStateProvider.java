package io.github.spencerharris192.seedtocellar.datagen;

import io.github.spencerharris192.seedtocellar.brewing.BottleLook;
import io.github.spencerharris192.seedtocellar.brewing.CaskWood;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.Vessel;
import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.CompostBinBlock;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.winery.CrushingTubBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.food.FeastBlock;
import io.github.spencerharris192.seedtocellar.food.PieBlock;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.station.AbstractCaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.FermentingVatBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.KilnBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.MaltingTubBlock;
import io.github.spencerharris192.seedtocellar.brewing.station.PreservingJarBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisVineBlock;
import io.github.spencerharris192.seedtocellar.farming.TallCropBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisBlock;
import io.github.spencerharris192.seedtocellar.decor.CopperWeathering;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import io.github.spencerharris192.seedtocellar.datagen.model.BlockModelBuilder;
import io.github.spencerharris192.seedtocellar.datagen.model.ModelBuilder;
import io.github.spencerharris192.seedtocellar.datagen.model.BlockStateProvider;
import io.github.spencerharris192.seedtocellar.distillery.PotStillBlock;
import io.github.spencerharris192.seedtocellar.food.LayerCakeBlock;
import io.github.spencerharris192.seedtocellar.datagen.model.ConfiguredModel;
import io.github.spencerharris192.seedtocellar.datagen.model.ModelFile;

/** Blockstates and block models. Templates with custom shapes live in src/main/resources/.../models/block/template_*.json. */
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerStatesAndModels() {
        hangingBundles();
        tavern();
        for (Crop crop : Crops.all()) {
            crop(crop);
            if (!crop.hasWild()) continue;
            String wild = "wild_" + crop.name;
            if (crop.style == Crop.Style.PADDY) {
                // reaches above the water: a second cross one block up
                simpleBlock(crop.wildBlock(), models().withExistingParent(wild, modLoc("block/template_tall_cross"))
                        .texture("cross", modLoc("block/" + wild)).texture("top", modLoc("block/" + wild + "_top"))
                        .renderType("cutout"));
            } else {
                cross(crop.wildBlock(), wild);
            }
        }
        cross(ModBlocks.WILD_HOPS.get(), "wild_hops");
        for (FruitTree tree : FruitTrees.all()) fruitTree(tree);

        ModelFile trellis = models().withExistingParent("trellis", modLoc("block/template_trellis"))
                .texture("trellis", modLoc("block/trellis")).renderType("cutout");
        getVariantBuilder(ModBlocks.TRELLIS.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(trellis).rotationY(yRotation(state.getValue(TrellisBlock.AXIS))).build());

        trellisVine(ModBlocks.HOPS.get(), "hops");

        maltingTub();

        // Thatch, with stairs and a slab for roofs.
        Identifier thatch = modLoc("block/thatch");
        simpleBlock(ModBlocks.THATCH.get(), models().cubeAll("thatch", thatch));
        stairsBlock((StairBlock) ModBlocks.THATCH_STAIRS.get(), thatch);
        slabBlock((SlabBlock) ModBlocks.THATCH_SLAB.get(), thatch, thatch);

        // Bales stand like hay (and turn with placement); sacks show their contents on top.
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            String id = storage.block().getId().getPath();
            if (storage.block().get() instanceof RotatedPillarBlock bale) {
                axisBlock(bale, modLoc("block/" + id + "_side"), modLoc("block/" + id + "_top"));
            } else if (storage.block().get() instanceof StorageBlocks.Crate) {
                // one slatted crate for every fruit; the fruit heaped on top
                simpleBlock(storage.block().get(), models().cubeBottomTop(id, modLoc("block/crate_side"),
                        modLoc("block/crate_bottom"), modLoc("block/" + id + "_top")));
            } else {
                String contents = id.substring(0, id.length() - "_sack".length());
                simpleBlock(storage.block().get(), models().cubeBottomTop(id, modLoc("block/" + contents + "_sack_side"),
                        modLoc("block/" + contents + "_sack_bottom"), modLoc("block/" + contents + "_sack_top")));
            }
        }
        compostBin();
        dryingRack();
        crushingTub();
        fruitPress();
        pies();
        layerCake();
        harvestFeast();

        // Fertile Farmland: vanilla's farmland shape with our richer soil on top (dry and wet looks).
        ModelFile fertileDry = models().withExistingParent("fertile_farmland", mcLoc("block/template_cube_bottom_top_indented"))
                .texture("side", mcLoc("block/dirt")).texture("bottom", mcLoc("block/dirt")).texture("top", modLoc("block/fertile_farmland"));
        ModelFile fertileWet = models().withExistingParent("fertile_farmland_moist", mcLoc("block/template_cube_bottom_top_indented"))
                .texture("side", mcLoc("block/dirt")).texture("bottom", mcLoc("block/dirt")).texture("top", modLoc("block/fertile_farmland_moist"));
        getVariantBuilder(ModBlocks.FERTILE_FARMLAND.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(FarmlandBlock.MOISTURE) == 7 ? fertileWet : fertileDry).build());

        ModelFile kilnOff = models().orientableWithBottom("kiln", mcLoc("block/bricks"), modLoc("block/kiln_front"),
                mcLoc("block/bricks"), modLoc("block/kiln_top"));
        ModelFile kilnOn = models().orientableWithBottom("kiln_on", mcLoc("block/bricks"), modLoc("block/kiln_front_lit"),
                mcLoc("block/bricks"), modLoc("block/kiln_top_lit"));
        horizontalBlock(ModBlocks.KILN.get(), state -> state.getValue(KilnBlock.LIT) ? kilnOn : kilnOff);

        // Base only: the turning upper stone is drawn by MillstoneRenderer.
        simpleBlock(ModBlocks.MILLSTONE.get(), models().getExistingFile(modLoc("block/millstone")));

        // Hand-built models in src/main/resources; liquids inside are drawn by renderers.
        ModelFile[] kettles = weathered(models().getExistingFile(modLoc("block/brew_kettle")), "brew_kettle", null, java.util.Map.of(
                "particle", "brew_kettle_side", "side", "brew_kettle_side", "inner", "brew_kettle_inner", "bottom", "brew_kettle_bottom"));
        getVariantBuilder(ModBlocks.BREW_KETTLE.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(kettles[state.getValue(CopperWeathering.STAGE).ordinal()]).build(),
                io.github.spencerharris192.seedtocellar.brewing.station.BrewKettleBlock.ACTIVE, CopperWeathering.WAXED);
        openClosed(ModBlocks.FERMENTING_VAT.get(), FermentingVatBlock.OPEN, "block/fermenting_vat", "block/fermenting_vat_closed");
        openClosed(ModBlocks.PRESERVING_JAR.get(), PreservingJarBlock.OPEN, "block/preserving_jar", "block/preserving_jar_closed");

        // Casks lie on their side with the head (and tap) toward the player who placed them. One hand-built
        // shape (block/cask, block/cask_tapped) takes each wood's staves and head.
        for (CaskWood wood : CaskWood.values()) {
            String id = wood.id() + "_cask";
            ModelFile cask = models().withExistingParent(id, modLoc("block/cask"))
                    .texture("side", modLoc("block/" + id + "_side")).texture("head", modLoc("block/" + id + "_head"));
            ModelFile caskTapped = models().withExistingParent(id + "_tapped", modLoc("block/cask_tapped"))
                    .texture("side", modLoc("block/" + id + "_side")).texture("head", modLoc("block/" + id + "_head"));
            // Charred casks: the same shape in the wood's darker shades, hoops blackened (nether wood never chars).
            ModelFile charred = wood.nether() ? cask : models().withExistingParent(id + "_charred", modLoc("block/cask"))
                    .texture("side", modLoc("block/" + id + "_side_charred")).texture("head", modLoc("block/" + id + "_head_charred"));
            ModelFile charredTapped = wood.nether() ? caskTapped : models().withExistingParent(id + "_charred_tapped", modLoc("block/cask_tapped"))
                    .texture("side", modLoc("block/" + id + "_side_charred")).texture("head", modLoc("block/" + id + "_head_charred"));
            getVariantBuilder(ModBlocks.CASKS.get(wood).get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(state.getValue(CaskBlock.CHARRED) ? (state.getValue(CaskBlock.TAP) ? charredTapped : charred)
                            : (state.getValue(CaskBlock.TAP) ? caskTapped : cask))
                    .rotationY(facingRotation(state.getValue(AbstractCaskBlock.FACING))).build());
        }
        wineRack();
        potStill();
        vanilla();
        ModelFile keg = models().getExistingFile(modLoc("block/keg"));
        getVariantBuilder(ModBlocks.KEG.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(keg).rotationY(facingRotation(state.getValue(AbstractCaskBlock.FACING))).build());
    }

    /**
     * The Wine Rack: a solid block with six dark holes on its front. The bottles in them are a separate model the
     * renderer places (WineryRenderers.Rack): lying down, centered on (8, 8), the neck out of the front and the foil
     * at its tip taking the wine's color (tint 0).
     */
    private void wineRack() {
        horizontalBlock(ModBlocks.WINE_RACK.get(), models().orientable("wine_rack", modLoc("block/wine_rack_side"),
                modLoc("block/wine_rack_front"), modLoc("block/wine_rack_top")));
        BlockModelBuilder bottle = models().withExistingParent("wine_rack_bottle", mcLoc("block/block"))
                .texture("particle", modLoc("block/wine_rack_bottle")).texture("glass", modLoc("block/wine_rack_bottle"))
                .texture("foil", modLoc("block/wine_rack_foil"));
        rackBottlePart(bottle, "#glass", -1, 6, 6, -1, 10, 10, 0);                  // the shoulder, just out of the hole
        rackBottlePart(bottle, "#glass", -1, 7, 7, -2.5F, 9, 9, -1);                // the neck
        rackBottlePart(bottle, "#foil", 0, 6.75F, 6.75F, -4, 9.25F, 9.25F, -2.5F);  // the foil over the cork

        // The Bottle Shelf: half a block deep against the wall (z 8-16 facing north): a back, two sides, three boards.
        // The drinks standing on it are drawn by the renderer (WineryRenderers.Rack).
        BlockModelBuilder shelf = models().withExistingParent("bottle_shelf", mcLoc("block/block"))
                .texture("particle", modLoc("block/rack_board")).texture("board", modLoc("block/rack_board"))
                .texture("back", modLoc("block/rack_back"));
        pressBox(shelf, "#back", 0, 0, 15, 16, 16, 16);
        pressBox(shelf, "#board", 0, 0, 8, 1, 16, 15);
        pressBox(shelf, "#board", 15, 0, 8, 16, 16, 15);
        pressBox(shelf, "#board", 1, 0, 8, 15, 1, 15);
        pressBox(shelf, "#board", 1, 7, 8, 15, 8, 15);
        pressBox(shelf, "#board", 1, 15, 8, 15, 16, 15);
        horizontalBlock(ModBlocks.BOTTLE_SHELF.get(), shelf);

        // The Wine Display: an open-fronted case of three shelves, each with a lip at the front to keep its bottle on.
        BlockModelBuilder display = models().withExistingParent("wine_display", mcLoc("block/block"))
                .texture("particle", modLoc("block/rack_board")).texture("board", modLoc("block/rack_board"))
                .texture("back", modLoc("block/rack_back"));
        pressBox(display, "#back", 0, 0, 15, 16, 16, 16);
        pressBox(display, "#board", 0, 0, 0, 1, 16, 15);
        pressBox(display, "#board", 15, 0, 0, 16, 16, 15);
        for (int y : new int[]{0, 5, 10}) {
            pressBox(display, "#board", 1, y, 0, 15, y + 1, 15);
            pressBox(display, "#board", 1, y + 1, 0, 15, y + 2, 1);
        }
        pressBox(display, "#board", 1, 15, 0, 15, 16, 15);
        horizontalBlock(ModBlocks.WINE_DISPLAY.get(), display);
        displayDrinks();
        // Drinks set down on a surface: their renderer draws them; the block itself shows nothing but breaking glass.
        ModelFile placed = models().getBuilder("placed_drinks").texture("particle", mcLoc("block/glass"));
        getVariantBuilder(ModBlocks.PLACED_DRINKS.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(placed).build());
    }

    /**
     * The drinks the Bottle Shelf and Wine Display show, as little 3D models standing on y = 0 around x = z = 8 (the
     * renderer scales and places them): a wine bottle, a glass mug, a juice flask, and a bottle for each spirit. Parts with tint 0 take the
     * drink's color: the bottle's foil and label band, the drink inside a mug or flask. Every face shows its whole
     * texture; the clear glass is cut out, so the drink shows through it.
     */
    private void displayDrinks() {
        BlockModelBuilder bottle = models().withExistingParent("drink_wine_bottle", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/display_glass")).texture("glass", modLoc("block/display_glass"))
                .texture("label", modLoc("block/display_label")).texture("tint", modLoc("block/wine_rack_foil"));
        drinkPart(bottle, "#glass", -1, 5.5F, 0, 5.5F, 10.5F, 9, 10.5F);        // the body
        drinkPart(bottle, "#glass", -1, 6, 9, 6, 10, 10, 10);                  // the shoulders
        drinkPart(bottle, "#glass", -1, 6.5F, 10, 6.5F, 9.5F, 11, 9.5F);
        drinkPart(bottle, "#glass", -1, 7, 11, 7, 9, 14, 9);                   // the neck
        drinkPart(bottle, "#tint", 0, 6.8F, 12.5F, 6.8F, 9.2F, 15, 9.2F);       // the foil over the cork
        drinkPart(bottle, "#label", -1, 5.4F, 2.5F, 5.4F, 10.6F, 6.5F, 10.6F);  // the label, round the body
        drinkPart(bottle, "#tint", 0, 5.35F, 3.5F, 5.35F, 10.65F, 5.5F, 10.65F); // and its colored band

        BlockModelBuilder mug = models().withExistingParent("drink_mug", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/display_clear")).texture("glass", modLoc("block/display_clear"))
                .texture("drink", modLoc("block/liquid_still")).texture("foam", modLoc("block/display_foam"));
        drinkPart(mug, "#drink", 0, 5.5F, 0.5F, 5.5F, 10.5F, 7, 10.5F);         // the beer
        drinkPart(mug, "#foam", -1, 5.5F, 7, 5.5F, 10.5F, 8.5F, 10.5F);         // its head
        drinkPart(mug, "#glass", -1, 5, 0, 5, 11, 9, 11);                      // the glass round it
        drinkPart(mug, "#glass", -1, 11, 6, 7.5F, 12.5F, 7, 8.5F);             // the handle: top, side, bottom
        drinkPart(mug, "#glass", -1, 11.5F, 2, 7.5F, 12.5F, 6, 8.5F);
        drinkPart(mug, "#glass", -1, 11, 1, 7.5F, 12.5F, 2, 8.5F);
        // An empty mug for the Mug Rack: the same glass and handle, nothing in it.
        BlockModelBuilder empty = models().withExistingParent("drink_mug_empty", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/display_clear")).texture("glass", modLoc("block/display_clear"));
        drinkPart(empty, "#glass", -1, 5, 0, 5, 11, 9, 11);
        drinkPart(empty, "#glass", -1, 11, 6, 7.5F, 12.5F, 7, 8.5F);
        drinkPart(empty, "#glass", -1, 11.5F, 2, 7.5F, 12.5F, 6, 8.5F);
        drinkPart(empty, "#glass", -1, 11, 1, 7.5F, 12.5F, 2, 8.5F);

        BlockModelBuilder flask = models().withExistingParent("drink_flask", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/display_clear")).texture("glass", modLoc("block/display_clear"))
                .texture("drink", modLoc("block/liquid_still")).texture("cork", modLoc("block/display_cork"));
        drinkPart(flask, "#drink", 0, 5.5F, 0.5F, 5.5F, 10.5F, 5, 10.5F);       // the juice
        drinkPart(flask, "#glass", -1, 5, 0, 5, 11, 6, 11);                    // the round belly of the flask
        drinkPart(flask, "#glass", -1, 7, 6, 7, 9, 9, 9);                      // the neck
        drinkPart(flask, "#cork", -1, 7.2F, 9, 7.2F, 8.8F, 10.5F, 8.8F);        // the cork

        // Spirits and liqueurs: each its own bottle and label (brewing/BottleLook), so a shelf of clear spirits reads apart.
        for (Drinks.Drink drink : Drinks.all()) {
            if (drink.vessel() != Vessel.SPIRIT_BOTTLE) continue;
            BottleLook look = BottleLook.of(drink);
            if (look == null) throw new IllegalStateException("No BottleLook for " + drink.name() + ": add one in brewing/BottleLook");
            spiritBottle(drink.name(), look);
        }
    }

    /** The tints of a spirit bottle's parts (WineryRenderers colors each): the spirit, the label's paper, its accent, the glass. */
    private static final int SPIRIT = 0, PAPER = 1, ACCENT = 2, GLASS = 3;

    /**
     * A spirit bottle for the shelves, standing on y = 0 around x = z = 8, in its look's shape: the glass (cut out, tinted
     * with the look's glass) round the spirit, the paper label and its accent, and the closure. At most 15 pixels tall, to
     * stand under the Bottle Shelf's boards.
     */
    private void spiritBottle(String name, BottleLook look) {
        BlockModelBuilder m = models().withExistingParent("block/display/" + name, mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/display_clear")).texture("glass", modLoc("block/display_clear"))
                .texture("spirit", modLoc("block/liquid_still")).texture("cork", modLoc("block/display_cork"))
                .texture("paper", modLoc("block/display_paper")).texture("accent", modLoc("block/display_accent"));
        switch (look.shape()) {
            case SQUARE -> {
                tier(m, "#spirit", SPIRIT, 2.5F, 0, 0.5F, 7.5F);
                tier(m, "#glass", GLASS, 3, 0, 0, 8);                   // the tall square body
                tier(m, "#glass", GLASS, 2.5F, 0, 8, 9);                // flat shoulders
                tier(m, "#glass", GLASS, 1, 0, 9, 11);                  // a short neck
                tier(m, "#accent", ACCENT, 1.15F, 0, 9.5F, 10.25F);      // its collar
                tier(m, "#cork", -1, 0.8F, 0, 11, 11.6F);               // the cork
                tier(m, "#cork", -1, 1.4F, 0, 11.6F, 12.8F);            // and its wooden cap
                label(m, look, 3, 3, 0, 2, 5.75F);
            }
            case WAXED -> {
                tier(m, "#spirit", SPIRIT, 2.75F, 0, 0.5F, 5.5F);
                tier(m, "#glass", GLASS, 3.25F, 0, 0, 6);               // short and square
                tier(m, "#glass", GLASS, 2.75F, 0, 6, 7);
                tier(m, "#glass", GLASS, 1, 0, 7, 9);                   // the neck, under the wax
                tier(m, "#accent", ACCENT, 1.2F, 0, 8, 9.8F);           // dipped in wax
                tier(m, "#accent", ACCENT, 1.45F, 0, 9.8F, 10.9F);      // thick over the stopper
                for (float[] drip : new float[][]{{6.9F, 7.4F, 7}, {8.4F, 8.9F, 7.45F}}) {   // drips down the front and back
                    drinkPart(m, "#accent", ACCENT, drip[0], drip[2], 6.6F, drip[1], 8, 9.4F);
                }
                drinkPart(m, "#accent", ACCENT, 6.6F, 7.25F, 7.6F, 9.4F, 8, 8.1F);            // and the sides
                label(m, look, 3.25F, 3.25F, 0, 1.5F, 4.5F);
            }
            case TALL -> {
                tier(m, "#spirit", SPIRIT, 2, 0.5F, 0.5F, 9.5F);
                tier(m, "#glass", GLASS, 2.5F, 0.75F, 0, 10);           // tall, slim and round
                tier(m, "#glass", GLASS, 2, 0.5F, 10, 10.75F);          // round shoulders
                tier(m, "#glass", GLASS, 1.4F, 0.4F, 10.75F, 11.25F);
                tier(m, "#glass", GLASS, 0.8F, 0, 11.25F, 13);          // the neck
                tier(m, "#accent", ACCENT, 1, 0, 13, 14.5F);            // a metal screw cap
                label(m, look, 2.5F, 2.5F, 0.75F, 2.5F, 6.5F);
            }
            case APOTHECARY -> {
                tier(m, "#spirit", SPIRIT, 2.5F, 0, 0.5F, 7.5F);
                tier(m, "#glass", GLASS, 3, 0, 0, 8);                   // square and upright
                tier(m, "#glass", GLASS, 2.5F, 0, 8, 8.75F);
                tier(m, "#glass", GLASS, 1.3F, 0, 8.75F, 10.25F);       // a wide neck
                tier(m, "#cork", -1, 1.1F, 0, 10.25F, 10.75F);          // a broad cork
                tier(m, "#cork", -1, 1.7F, 0.3F, 10.75F, 11.75F);
                label(m, look, 2.25F, 3, 0, 2, 6.5F);                   // on its front and back
            }
            case ROUND -> {
                tier(m, "#spirit", SPIRIT, 2.75F, 0.8F, 0.5F, 6);
                tier(m, "#glass", GLASS, 3.25F, 1, 0, 6.5F);            // squat and round
                tier(m, "#glass", GLASS, 2.5F, 0.75F, 6.5F, 7.25F);     // round shoulders
                tier(m, "#glass", GLASS, 1.6F, 0.5F, 7.25F, 7.75F);
                tier(m, "#glass", GLASS, 0.9F, 0, 7.75F, 11);           // a long neck
                tier(m, "#accent", ACCENT, 1.05F, 0, 9, 10);            // with a collar
                tier(m, "#cork", -1, 0.75F, 0, 11, 11.5F);
                tier(m, "#cork", -1, 1.3F, 0, 11.5F, 12.6F);
                label(m, look, 3.25F, 3.25F, 1, 1.5F, 4.75F);
            }
            case DECANTER -> {
                tier(m, "#spirit", SPIRIT, 3, 1, 0.5F, 3.75F);
                tier(m, "#spirit", SPIRIT, 2.5F, 0.8F, 3.75F, 5);
                tier(m, "#spirit", SPIRIT, 1.5F, 0.4F, 5, 6);
                tier(m, "#glass", GLASS, 3.5F, 1.25F, 0, 4);            // a wide, low belly
                tier(m, "#glass", GLASS, 3, 1, 4, 5.25F);
                tier(m, "#glass", GLASS, 2, 0.6F, 5.25F, 6.25F);
                tier(m, "#glass", GLASS, 0.75F, 0, 6.25F, 9.75F);       // a thin neck
                tier(m, "#accent", ACCENT, 0.9F, 0, 7.5F, 8.25F);       // its collar
                tier(m, "#glass", GLASS, 1.1F, 0, 9.75F, 10.25F);       // the lip
                tier(m, "#cork", -1, 0.6F, 0, 10.25F, 10.75F);
                tier(m, "#cork", -1, 1.5F, 0.5F, 10.75F, 11.75F);       // a wide wooden stopper
                label(m, look, 3.5F, 3.5F, 1.25F, 0.75F, 3.25F);
            }
            case SLENDER -> {
                tier(m, "#spirit", SPIRIT, 1.75F, 0.4F, 0.5F, 7.75F);
                tier(m, "#glass", GLASS, 2.25F, 0.6F, 0, 8);            // tall and slender
                tier(m, "#glass", GLASS, 1.75F, 0.45F, 8, 9);           // tapering...
                tier(m, "#glass", GLASS, 1.25F, 0.3F, 9, 10);
                tier(m, "#glass", GLASS, 0.75F, 0, 10, 12.5F);          // ...into a long neck
                tier(m, "#accent", ACCENT, 0.9F, 0, 12.25F, 14.25F);    // under a foil capsule
                label(m, look, 2.25F, 2.25F, 0.6F, 2, 5.5F);
            }
            case STOPPERED -> {
                tier(m, "#spirit", SPIRIT, 2.75F, 0, 0.5F, 4.5F);
                tier(m, "#spirit", SPIRIT, 2, 0, 4.5F, 5.5F);
                tier(m, "#glass", GLASS, 3.25F, 0, 0, 4.75F);           // a chunky square base
                tier(m, "#glass", GLASS, 2.5F, 0, 4.75F, 5.75F);        // tapering
                tier(m, "#glass", GLASS, 1.75F, 0, 5.75F, 6.5F);
                tier(m, "#glass", GLASS, 1, 0, 6.5F, 7.5F);             // a short neck
                tier(m, "#cork", -1, 0.8F, 0, 7.5F, 8);
                tier(m, "#cork", -1, 1.6F, 0.5F, 8, 8.5F);              // a big round wooden stopper
                tier(m, "#cork", -1, 2, 0.6F, 8.5F, 10.25F);
                tier(m, "#cork", -1, 1.4F, 0.4F, 10.25F, 10.9F);
                label(m, look, 2.4F, 3.25F, 0, 1, 3.75F);               // on its front and back
            }
            case APPLE -> appleCrown(m, name);
            case DASHER -> {
                tier(m, "#spirit", SPIRIT, 1.75F, 0.4F, 0.5F, 5.75F);
                tier(m, "#glass", GLASS, 2.25F, 0.6F, 0, 6);            // a little bottle
                tier(m, "#glass", GLASS, 1.6F, 0.4F, 6, 6.75F);
                tier(m, "#glass", GLASS, 0.7F, 0, 6.75F, 8.5F);
                tier(m, "#accent", ACCENT, 0.95F, 0, 8.5F, 10.25F);     // a tall cap
                label(m, look, 2.25F, 2.25F, 0.6F, 0.4F, 5.8F);         // under an oversized label
            }
        }
    }

    /**
     * Apple Crown Whiskey: an apple of clear glass full of whiskey (round and plump, its neck rising from the dimple like a
     * stem) with a green leaf sprouting from the neck; a gold-edged red badge with a little gold crown front and back; and for a
     * stopper a gold crown: its band set with rubies and emeralds, eight points round a red velvet cap, an orb and cross on
     * top. The crowned (six-star) bottle is the same model with a golden leaf, a red edge and a red crown on a gold badge
     * (the renderer swaps the paper's color and adds the shimmer).
     */
    private void appleCrown(BlockModelBuilder m, String name) {
        m.texture("gold", modLoc("block/display_gold")).texture("ruby", modLoc("block/display_ruby"))
                .texture("emerald", modLoc("block/display_emerald")).texture("velvet", modLoc("block/display_velvet"))
                .texture("leaf", modLoc("block/display_leaf")).texture("rule", modLoc("block/display_gold"))
                .texture("emblem", modLoc("block/display_crown_emblem"));
        tier(m, "#spirit", SPIRIT, 1.25F, 0.3F, 0.5F, 0.6F);           // the whiskey, filling the apple
        tier(m, "#spirit", SPIRIT, 2.25F, 0.7F, 0.6F, 1.35F);
        tier(m, "#spirit", SPIRIT, 2.9F, 1, 1.35F, 2.35F);
        tier(m, "#spirit", SPIRIT, 3.3F, 1.1F, 2.35F, 5.35F);
        tier(m, "#spirit", SPIRIT, 3, 1.1F, 5.35F, 6.25F);
        tier(m, "#spirit", SPIRIT, 2.4F, 0.8F, 6.25F, 6.75F);
        tier(m, "#glass", GLASS, 1.75F, 0.5F, 0, 0.6F);                 // the apple of glass: a small foot...
        tier(m, "#glass", GLASS, 2.75F, 0.9F, 0.6F, 1.35F);
        tier(m, "#glass", GLASS, 3.4F, 1.2F, 1.35F, 2.35F);
        tier(m, "#glass", GLASS, 3.8F, 1.4F, 2.35F, 5.35F);            // ...round and plump...
        tier(m, "#glass", GLASS, 3.5F, 1.3F, 5.35F, 6.25F);
        tier(m, "#glass", GLASS, 2.9F, 1, 6.25F, 7);
        tier(m, "#glass", GLASS, 2, 0.6F, 7, 7.5F);                    // ...rounding in to its dimple
        tier(m, "#glass", GLASS, 0.85F, 0, 7.5F, 9.85F);               // the neck, rising like a stem
        // A badge on its front and back (the whiskey shows all round it): gold-edged red, a little gold crown on it.
        box(m, "#rule", -1, 2.1F, 3.85F, 0, 2.4F, 4.9F);                // the gold edge
        box(m, "#paper", PAPER, 1.8F, 3.9F, 0, 2.7F, 4.6F);             // the red
        var front = m.element().from(7, 3, 4.08F).to(9, 4.2F, 4.08F);
        front.face(Direction.NORTH).texture("#emblem").uvs(0, 0, 16, 16).end();
        front.end();
        var back = m.element().from(7, 3, 11.92F).to(9, 4.2F, 11.92F);
        back.face(Direction.SOUTH).texture("#emblem").uvs(0, 0, 16, 16).end();
        back.end();
        // a leaf sprouting from the neck, tilted up and out, narrowing to its tip
        appleLeaf(m, 8.85F, 10.6F, 6.25F, 8.45F);
        appleLeaf(m, 10.6F, 12.1F, 6.75F, 7.95F);
        appleLeaf(m, 12.1F, 12.9F, 7.1F, 7.6F);
        // the crown
        tier(m, "#cork", -1, 0.7F, 0, 9.85F, 10.15F);                   // a glimpse of cork
        tier(m, "#gold", -1, 1.7F, 0, 10.15F, 11.15F);                  // its band
        tier(m, "#velvet", -1, 1.4F, 0.4F, 11.15F, 11.95F);             // a velvet cap inside it
        for (int sx = -1; sx <= 1; sx += 2) {                           // four short points at the corners
            for (int sz = -1; sz <= 1; sz += 2) {
                float x = sx < 0 ? 6.3F : 9.1F, z = sz < 0 ? 6.3F : 9.1F;
                drinkPart(m, "#gold", -1, x, 11.15F, z, x + 0.6F, 12.15F, z + 0.6F);
            }
        }
        drinkPart(m, "#gold", -1, 7.7F, 11.15F, 6.3F, 8.3F, 12.55F, 6.9F);   // four tall ones between
        drinkPart(m, "#gold", -1, 7.7F, 11.15F, 9.1F, 8.3F, 12.55F, 9.7F);
        drinkPart(m, "#gold", -1, 6.3F, 11.15F, 7.7F, 6.9F, 12.55F, 8.3F);
        drinkPart(m, "#gold", -1, 9.1F, 11.15F, 7.7F, 9.7F, 12.55F, 8.3F);
        drinkPart(m, "#ruby", -1, 7.6F, 10.4F, 6.15F, 8.4F, 10.9F, 6.35F);   // rubies front and back
        drinkPart(m, "#ruby", -1, 7.6F, 10.4F, 9.65F, 8.4F, 10.9F, 9.85F);
        drinkPart(m, "#emerald", -1, 6.15F, 10.4F, 7.6F, 6.35F, 10.9F, 8.4F);   // emeralds on its sides
        drinkPart(m, "#emerald", -1, 9.65F, 10.4F, 7.6F, 9.85F, 10.9F, 8.4F);
        tier(m, "#gold", -1, 0.4F, 0, 11.95F, 12.75F);                  // the orb
        drinkPart(m, "#gold", -1, 7.85F, 12.75F, 7.85F, 8.15F, 13.35F, 8.15F);   // and its cross
        drinkPart(m, "#gold", -1, 7.6F, 12.95F, 7.85F, 8.4F, 13.15F, 8.15F);
        // The crowned bottle: a golden leaf, red bands, a red crown on its (gold-colored) label.
        models().getBuilder("block/display/" + name + "_crowned").parent(m).renderType("cutout")
                .texture("leaf", modLoc("block/display_gold_leaf")).texture("rule", modLoc("block/display_velvet"))
                .texture("emblem", modLoc("block/display_crown_emblem_red"));
    }

    /** One segment of the Apple Crown's leaf: a thin blade off the neck, tipped 22.5 degrees up about the neck's side. */
    private static void appleLeaf(BlockModelBuilder m, float x0, float x1, float z0, float z1) {
        var e = m.element().from(x0, 8.15F, z0).to(x1, 8.3F, z1)
                .rotation().angle(22.5F).axis(Direction.Axis.Z).origin(8.85F, 8.2F, 7.35F).end();
        for (Direction side : Direction.values()) e.face(side).texture("#leaf").uvs(0, 0, 16, 16).end();
        e.end();
    }

    /** One tier of a bottle, `half` pixels each way from its middle: square, or rounded (its corners cut back by `cut`). */
    private static void tier(BlockModelBuilder m, String texture, int tint, float half, float cut, float y0, float y1) {
        box(m, texture, tint, half, half, cut, y0, y1);
    }

    private static void box(BlockModelBuilder m, String texture, int tint, float halfX, float halfZ, float cut, float y0, float y1) {
        if (cut <= 0) {
            drinkPart(m, texture, tint, 8 - halfX, y0, 8 - halfZ, 8 + halfX, y1, 8 + halfZ);
            return;
        }
        // two boxes crossed, so the corners stay cut back
        drinkPart(m, texture, tint, 8 - halfX, y0, 8 - halfZ + cut, 8 + halfX, y1, 8 + halfZ - cut);
        drinkPart(m, texture, tint, 8 - halfX + cut, y0, 8 - halfZ, 8 + halfX - cut, y1, 8 + halfZ);
    }

    /**
     * The paper label just over the glass from y0 to y1 (halfX narrower than the body: a label on its front and back only),
     * and its accent a hair further out: a band round its middle, a band along its top or bottom, an emblem front and
     * back, or two thin rules.
     */
    private static void label(BlockModelBuilder m, BottleLook look, float halfX, float halfZ, float cut, float y0, float y1) {
        box(m, "#paper", PAPER, halfX + 0.05F, halfZ + 0.05F, cut, y0, y1);
        float h = y1 - y0, x = halfX + 0.1F, z = halfZ + 0.1F;
        switch (look.label()) {
            case BAND -> box(m, "#accent", ACCENT, x, z, cut, y0 + h * 0.35F, y1 - h * 0.35F);
            case HEADER -> box(m, "#accent", ACCENT, x, z, cut, y1 - h * 0.35F, y1 + 0.05F);
            case FOOT -> box(m, "#accent", ACCENT, x, z, cut, y0 - 0.05F, y0 + h * 0.35F);
            case CREST -> {
                float w = Math.min(1, halfX - cut - 0.25F), mid = (y0 + y1) / 2, e = h * 0.22F;
                drinkPart(m, "#accent", ACCENT, 8 - w, mid - e, 8 - z, 8 + w, mid + e, 8 + z);
            }
            case RULES -> {
                box(m, "#accent", ACCENT, x, z, cut, y0 + h * 0.12F, y0 + h * 0.24F);
                box(m, "#accent", ACCENT, x, z, cut, y1 - h * 0.24F, y1 - h * 0.12F);
            }
        }
    }

    /**
     * The Pot Still (GDD section 7), facing north. Lower half: a copper pot bellying out over an iron ring, with a glass
     * spirit safe on its front and a stillage tap on its side. Upper half: the neck and head, and the swan neck that runs
     * forward and down into the safe. Every part lies inside its block, so automatic UVs read the tiled copper as it is.
     */
    private void potStill() {
        BlockModelBuilder lower = models().withExistingParent("pot_still_lower", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/pot_still_copper")).texture("copper", modLoc("block/pot_still_copper"))
                .texture("top", modLoc("block/pot_still_top")).texture("band", modLoc("block/pot_still_band"))
                .texture("pipe", modLoc("block/pot_still_pipe")).texture("safe", modLoc("block/pot_still_safe"));
        stillBox(lower, "#band", "#band", 4, 0, 5, 12, 1, 13);            // the iron ring it stands on
        roundBox(lower, "#copper", "#copper", 3, 1, 4, 13, 3, 14);        // the pot, widening
        roundBox(lower, "#copper", "#copper", 2, 3, 3, 14, 11, 15);       // its belly
        roundBox(lower, "#copper", "#copper", 3, 11, 4, 13, 14, 14);      // narrowing again
        roundBox(lower, "#copper", "#top", 4, 14, 5, 12, 16, 13);         // the shoulder
        stillBox(lower, "#pipe", "#pipe", 7, 6, 1, 9, 16, 3);             // the pipe down into the safe
        stillBox(lower, "#pipe", "#pipe", 14, 1, 8, 16, 3, 10);           // the stillage tap
        stillBox(lower, "#band", "#band", 14.5F, 3, 8.5F, 15.5F, 4, 9.5F); // and its handle
        // The spirit safe: glass on three sides (the spirit shows through), copper top and bottom.
        var safe = lower.element().from(5, 0, 0).to(11, 6, 3);
        for (Direction side : Direction.values()) {
            boolean glass = side.getAxis().isHorizontal() && side != Direction.SOUTH;
            safe.face(side).texture(glass ? "#safe" : "#top").end();
        }
        safe.end();

        BlockModelBuilder upper = stillHead("pot_still_upper");
        // With a Gin Basket: a copper-banded mesh drum round the swan neck, where the vapor passes through the botanicals.
        // (A model with its own parts doesn't inherit its parent's, so the head is built again under it.)
        BlockModelBuilder basket = stillHead("pot_still_upper_basket").texture("basket", modLoc("block/pot_still_basket"));
        basket.element().from(5.5F, 7.5F, 1.5F).to(10.5F, 12.5F, 5).allFaces((side, face) -> face.texture("#basket")).end();
        basket.element().from(5, 7, 1).to(11, 8, 5.5F).allFaces((side, face) -> face.texture("#top")).end();         // its band
        basket.element().from(5, 12, 1).to(11, 13, 5.5F).allFaces((side, face) -> face.texture("#top")).end();       // and lid

        // Each at every weathering stage: the copper textures swapped for their patinated versions (the iron stays).
        java.util.Map<String, String> pot = java.util.Map.of("particle", "pot_still_copper", "copper", "pot_still_copper",
                "top", "pot_still_top", "pipe", "pot_still_pipe", "safe", "pot_still_safe");
        java.util.Map<String, String> head = java.util.Map.of("particle", "pot_still_copper", "copper", "pot_still_copper",
                "top", "pot_still_top", "pipe", "pot_still_pipe", "neck", "pot_still_neck");
        java.util.Map<String, String> headWithBasket = new java.util.HashMap<>(head);
        headWithBasket.put("basket", "pot_still_basket");
        ModelFile[] lowers = weathered(lower, "pot_still_lower", "cutout", pot);
        ModelFile[] uppers = weathered(upper, "pot_still_upper", "cutout", head);
        ModelFile[] baskets = weathered(basket, "pot_still_upper_basket", "cutout", headWithBasket);
        getVariantBuilder(ModBlocks.POT_STILL.get()).forAllStatesExcept(state -> {
            int stage = state.getValue(CopperWeathering.STAGE).ordinal();
            return ConfiguredModel.builder()
                    .modelFile(!PotStillBlock.isUpper(state) ? lowers[stage] : state.getValue(PotStillBlock.BASKET) ? baskets[stage] : uppers[stage])
                    .rotationY(facingRotation(state.getValue(PotStillBlock.FACING))).build();
        }, PotStillBlock.ACTIVE, CopperWeathering.WAXED);
    }

    /**
     * A copper model at each weathering stage (indexed by {@link CopperWeathering.Stage#ordinal()}): the fresh one, then
     * children named {@code <name>_<stage>} with each listed texture swapped for its {@code _<stage>} version.
     */
    private ModelFile[] weathered(ModelFile fresh, String name, String renderType, java.util.Map<String, String> textures) {
        ModelFile[] out = new ModelFile[CopperWeathering.Stage.values().length];
        out[0] = fresh;
        for (CopperWeathering.Stage stage : CopperWeathering.Stage.values()) {
            if (stage == CopperWeathering.Stage.UNAFFECTED) continue;
            BlockModelBuilder m = models().getBuilder(name + "_" + stage.getSerializedName()).parent(fresh);
            if (renderType != null) m.renderType(renderType);
            textures.forEach((key, texture) -> m.texture(key, modLoc("block/" + texture + "_" + stage.getSerializedName())));
            out[stage.ordinal()] = m;
        }
        return out;
    }

    /**
     * Vanilla on a jungle log (the log to the north): a vine lying flat on the bark (a plane just off it, like vanilla's
     * vines), its pale orchid flowers standing out from it once it flowers, and long green pods hanging from it when ripe.
     */
    private void vanilla() {
        ModelFile[] looks = new ModelFile[4];
        for (int age = 0; age < 4; age++) {
            BlockModelBuilder m = models().withExistingParent("vanilla_stage" + age, mcLoc("block/block")).renderType("cutout")
                    .texture("particle", modLoc("block/vanilla_vine")).texture("vine", modLoc("block/vanilla_vine"))
                    .texture("flower", modLoc("block/vanilla_flower")).texture("pod", modLoc("block/vanilla_pod"));
            int top = age == 0 ? 7 : 16;
            float x0 = age == 0 ? 5 : 2, x1 = age == 0 ? 11 : 14;
            var vine = m.element().from(x0, 0, 0.8F).to(x1, top, 0.8F);
            vine.face(Direction.SOUTH).texture("#vine").uvs(x0, 16 - top, x1, 16).end();
            vine.face(Direction.NORTH).texture("#vine").uvs(x1, 16 - top, x0, 16).end();
            vine.end();
            if (age == 2) {
                for (float[] at : new float[][]{{4, 11}, {10, 7}, {7, 3}}) {   // three orchid flowers
                    vanillaPart(m, "#flower", at[0], at[1], 0.8F, at[0] + 2, at[1] + 2, 2.3F);
                    vanillaPart(m, "#flower", at[0] + 0.5F, at[1] + 0.5F, 2.3F, at[0] + 1.5F, at[1] + 1.5F, 2.8F);   // its lip
                }
            }
            if (age == 3) {
                for (float[] at : new float[][]{{3.5F, 8}, {5.5F, 9}, {10, 4}, {12, 5}}) {   // pods hanging in pairs
                    vanillaPart(m, "#pod", at[0], at[1], 1, at[0] + 1, at[1] + 6, 2);
                }
            }
            looks[age] = m;
        }
        getVariantBuilder(ModBlocks.VANILLA.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(looks[state.getValue(io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.AGE)])
                .rotationY(facingRotation(state.getValue(io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.FACING))).build());
    }

    /** A small box of the vanilla model: every face takes the top-left of its texture (they're single-colored patches). */
    private static void vanillaPart(BlockModelBuilder m, String texture, float x0, float y0, float z0, float x1, float y1, float z1) {
        agavePart(m, texture, x0, y0, z0, x1, y1, z1);
    }

    /** The still's upper half: the neck and head, and the swan neck running forward and down into the safe. */
    private BlockModelBuilder stillHead(String name) {
        BlockModelBuilder upper = models().withExistingParent(name, mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/pot_still_copper")).texture("copper", modLoc("block/pot_still_copper"))
                .texture("top", modLoc("block/pot_still_top")).texture("pipe", modLoc("block/pot_still_pipe"))
                .texture("neck", modLoc("block/pot_still_neck"));
        roundBox(upper, "#neck", "#top", 5, 0, 6, 11, 3, 12);             // the neck rising off the shoulder
        stillBox(upper, "#neck", "#neck", 6, 3, 7, 10, 8, 11);            // the narrow neck
        roundBox(upper, "#neck", "#neck", 5, 8, 6, 11, 12, 12);           // the head
        stillBox(upper, "#neck", "#top", 6, 12, 7, 10, 13, 11);           // its cap
        stillBox(upper, "#pipe", "#pipe", 7, 9, 1, 9, 11, 6);             // the swan neck, out to the front
        stillBox(upper, "#pipe", "#pipe", 7, 0, 1, 9, 9, 3);              // and down to the safe
        return upper;
    }

    /**
     * A box with its four vertical edges cut back a pixel (two overlapping boxes, a cross seen from above), so the pot and
     * head read round rather than square. Where the two meet top and bottom they sample the same pixels, so nothing flickers.
     */
    private static void roundBox(BlockModelBuilder m, String side, String top, float x0, float y0, float z0, float x1, float y1, float z1) {
        stillBox(m, side, top, x0, y0, z0 + 1, x1, y1, z1 - 1);
        stillBox(m, side, top, x0 + 1, y0, z0, x1 - 1, y1, z1);
    }

    /**
     * The agave (GDD growth style I) as a 3D rosette: a heart of upright leaves (two crossed planes) and thick blue-green
     * leaves tilted out round it in the four directions, an outer ring lying low and, once grown, an inner ring standing
     * up; in flower, a tall stalk rises a block above it with three tiers of yellow blossom.
     */
    private void succulent(Crop crop) {
        ModelFile[] looks = new ModelFile[crop.stages];
        for (int stage = 0; stage < crop.stages; stage++) looks[stage] = rosette(crop.texture, stage, stage == crop.stages - 1);
        getVariantBuilder(crop.block()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(looks[crop.stageFor(state.getValue(CropBlock.AGE))]).build());
    }

    private ModelFile rosette(String name, int stage, boolean flowering) {
        int[] heart = {4, 6, 8, 10, 10};          // upright heart leaves: height
        int[] heartWidth = {5, 7, 9, 11, 11};
        int[] outer = {0, 5, 7, 9, 9};             // the tilted leaves' length
        BlockModelBuilder m = models().withExistingParent(name + "_stage" + stage, mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/" + name + "_leaf")).texture("leaf", modLoc("block/" + name + "_leaf"))
                .texture("heart", modLoc("block/" + name + "_heart")).texture("stalk", modLoc("block/" + name + "_stalk"))
                .texture("flower", modLoc("block/" + name + "_flower"));
        float w = heartWidth[stage] / 2F;
        for (float angle : new float[]{45, -45}) {
            var plane = m.element().from(8 - w, 0, 8).to(8 + w, heart[stage], 8);
            plane.rotation().origin(8, 0, 8).axis(Direction.Axis.Y).angle(angle).end();
            for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                plane.face(side).texture("#heart").uvs(8 - w, 16 - heart[stage], 8 + w, 16).end();
            }
            plane.end();
        }
        if (outer[stage] > 0) leafRing(m, outer[stage], stage >= 3 ? 45 : 22.5F, 1.5F);
        if (stage >= 3) leafRing(m, 7, 22.5F, 1.2F);
        if (flowering) {
            agavePart(m, "#stalk", 7.5F, 6, 7.5F, 8.5F, 29, 8.5F);                 // the stalk
            agavePart(m, "#stalk", 5, 19, 7.75F, 11, 19.75F, 8.25F);                // a tier of branches, and its blossoms
            agavePart(m, "#flower", 4, 19.5F, 7, 6, 21.5F, 9);
            agavePart(m, "#flower", 10, 19.5F, 7, 12, 21.5F, 9);
            agavePart(m, "#stalk", 7.75F, 23, 5.5F, 8.25F, 23.75F, 10.5F);           // the next, across it
            agavePart(m, "#flower", 7, 23.5F, 4.5F, 9, 25.5F, 6.5F);
            agavePart(m, "#flower", 7, 23.5F, 9.5F, 9, 25.5F, 11.5F);
            agavePart(m, "#flower", 6.5F, 27, 6.5F, 9.5F, 30, 9.5F);                 // and the crown
        }
        return m;
    }

    /**
     * Four thick leaves `length` long, one to each side, rising from the middle and tilted `tilt` degrees outward. Each
     * tapers: a broad base, then a narrow tip (the two turn about the same point, so they stay in line).
     */
    private static void leafRing(BlockModelBuilder m, float length, float tilt, float halfWidth) {
        float joint = Math.round(length * 0.55F * 2) / 2F;
        for (Direction toward : Direction.Plane.HORIZONTAL) {
            leafPart(m, toward, tilt, 0, joint, halfWidth, 7, 16, 6, 10);                 // the base: the lower leaf, full width
            leafPart(m, toward, tilt, joint, length, halfWidth / 2, 0, 9, 7, 9);          // the tip: the upper leaf and its spine
        }
    }

    /** One segment of a leaf from `y0` to `y1` along it, sampling rows v0-v1 and columns u0-u1 of the leaf texture. */
    private static void leafPart(BlockModelBuilder m, Direction toward, float tilt, float y0, float y1, float halfWidth,
                                 float v0, float v1, float u0, float u1) {
        boolean alongZ = toward.getAxis() == Direction.Axis.Z;   // leaves pointing north or south lie in the x-y plane
        float half = Math.min(0.5F, halfWidth);                  // thickness: a pixel, thinner at the tip
        float x0 = alongZ ? 8 - halfWidth : 8 - half, x1 = alongZ ? 8 + halfWidth : 8 + half;
        float z0 = alongZ ? 8 - half : 8 - halfWidth, z1 = alongZ ? 8 + half : 8 + halfWidth;
        var leaf = m.element().from(x0, y0, z0).to(x1, y1, z1);
        // Turning about x tips the top toward +z for a positive angle; about z, toward -x.
        float angle = toward == Direction.SOUTH || toward == Direction.WEST ? tilt : -tilt;
        leaf.rotation().origin(8, 0, 8).axis(alongZ ? Direction.Axis.X : Direction.Axis.Z).angle(angle).end();
        for (Direction side : Direction.values()) {
            boolean broad = side.getAxis() == (alongZ ? Direction.Axis.Z : Direction.Axis.X);
            float[] uv = side.getAxis() == Direction.Axis.Y ? new float[]{u0, v0, u1, v0 + 1}
                    : broad ? new float[]{u0, v0, u1, v1} : new float[]{10, v0, 11, v1};
            leaf.face(side).texture("#leaf").uvs(uv[0], uv[1], uv[2], uv[3]).end();
        }
        leaf.end();
    }

    /** A part of the flower spike, which reaches above the block: its own UVs on every face (the top-left of its texture). */
    private static void agavePart(BlockModelBuilder m, String texture, float x0, float y0, float z0, float x1, float y1, float z1) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) {
            float w = side.getAxis() == Direction.Axis.X ? z1 - z0 : x1 - x0;
            float h = side.getAxis() == Direction.Axis.Y ? z1 - z0 : y1 - y0;
            e.face(side).texture(texture).uvs(0, 0, Math.min(16, w), Math.min(16, h)).end();
        }
        e.end();
    }

    /** One box of the still: `top` on its top face, `side` elsewhere (automatic UVs: every part lies inside the block). */
    private static void stillBox(BlockModelBuilder m, String side, String top, float x0, float y0, float z0, float x1, float y1, float z1) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction face : Direction.values()) e.face(face).texture(face == Direction.UP ? top : side).end();
        e.end();
    }

    private static void drinkPart(BlockModelBuilder m, String texture, int tint, float x0, float y0, float z0, float x1, float y1, float z1) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) {
            var face = e.face(side).texture(texture).uvs(0, 0, 16, 16);
            if (tint >= 0) face.tintindex(tint);
            face.end();
        }
        e.end();
    }

    /**
     * One box of the racked bottle (the front of the rack is z = 0; the bottle sticks out toward -z). No back face.
     * Every face takes the top-left pixels of its texture: automatic UVs would reach outside it where z is below 0.
     */
    private static void rackBottlePart(BlockModelBuilder m, String texture, int tint, float x0, float y0, float z0,
                                       float x1, float y1, float z1) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) {
            if (side == Direction.SOUTH) continue;
            float w = side.getAxis() == Direction.Axis.X ? z1 - z0 : x1 - x0;
            float h = side.getAxis() == Direction.Axis.Y ? z1 - z0 : y1 - y0;
            var face = e.face(side).texture(texture).uvs(0, 0, w, h);
            if (tint >= 0) face.tintindex(tint);
            face.end();
        }
        e.end();
    }

    /** Models face north; turn them to face the block's direction. */
    /**
     * The tavern set in every cask wood (GDD section 17.3). The Bar Counter (front to the north): a thick top, a paneled
     * front set back under it between framing stiles and rails, a dark kick plate, and a brass foot rail on brackets; side
     * by side the panels make one long bar. The Bar Stool: a round seat on four legs braced by a foot ring, a buttoned
     * leather cushion on top.
     */
    private void tavern() {
        for (CaskWood wood : CaskWood.values()) {
            String w = wood.id();
            BlockModelBuilder counter = models().withExistingParent(w + "_bar_counter", mcLoc("block/block"))
                    .texture("particle", modLoc("block/bar_" + w + "_planks")).texture("planks", modLoc("block/bar_" + w + "_planks"))
                    .texture("trim", modLoc("block/bar_" + w + "_trim")).texture("panel", modLoc("block/bar_" + w + "_panel"))
                    .texture("brass", modLoc("block/bar_brass"));
            stillBox(counter, "#planks", "#planks", 0, 13.5F, 0, 16, 16, 16);          // the top, overhanging the front
            stillBox(counter, "#planks", "#planks", 0, 2, 1.5F, 16, 13.5F, 16);        // the body, set back under it
            stillBox(counter, "#trim", "#trim", 0, 0, 2.5F, 16, 2, 16);                 // the kick plate
            stillBox(counter, "#trim", "#trim", 0, 2, 1, 2, 13.5F, 1.5F);               // framing stiles at each end
            stillBox(counter, "#trim", "#trim", 14, 2, 1, 16, 13.5F, 1.5F);
            stillBox(counter, "#trim", "#trim", 2, 11.5F, 1, 14, 13.5F, 1.5F);          // and rails top and bottom
            stillBox(counter, "#trim", "#trim", 2, 2, 1, 14, 3.5F, 1.5F);
            drinkPart(counter, "#panel", -1, 3, 4.5F, 0.9F, 13, 10.5F, 1.5F);              // the raised panel between them
            stillBox(counter, "#brass", "#brass", 0, 4.5F, 0.2F, 16, 5.5F, 0.9F);       // the foot rail
            stillBox(counter, "#brass", "#brass", 1, 4, 0.6F, 2, 5.5F, 1.5F);           // on its brackets
            stillBox(counter, "#brass", "#brass", 14, 4, 0.6F, 15, 5.5F, 1.5F);

            // The corner sticking out toward the customers (fronts north and west): the top over both, the body set
            // back on both, a corner post where the paneled fronts meet, the foot rail running round it.
            BlockModelBuilder outer = counterModel(w + "_bar_counter_outer", w);
            stillBox(outer, "#planks", "#planks", 0, 13.5F, 0, 16, 16, 16);
            stillBox(outer, "#planks", "#planks", 1.5F, 2, 1.5F, 16, 13.5F, 16);
            stillBox(outer, "#trim", "#trim", 2.5F, 0, 2.5F, 16, 2, 16);
            stillBox(outer, "#trim", "#trim", 1, 2, 1, 3, 13.5F, 3);                  // the corner post
            stillBox(outer, "#trim", "#trim", 14, 2, 1, 16, 13.5F, 1.5F);             // north: the far stile,
            stillBox(outer, "#trim", "#trim", 3, 11.5F, 1, 14, 13.5F, 1.5F);          // rails,
            stillBox(outer, "#trim", "#trim", 3, 2, 1, 14, 3.5F, 1.5F);
            drinkPart(outer, "#panel", -1, 4, 4.5F, 0.9F, 13, 10.5F, 1.5F);              // and raised panel
            stillBox(outer, "#trim", "#trim", 1, 2, 14, 1.5F, 13.5F, 16);             // west: the same
            stillBox(outer, "#trim", "#trim", 1, 11.5F, 3, 1.5F, 13.5F, 14);
            stillBox(outer, "#trim", "#trim", 1, 2, 3, 1.5F, 3.5F, 14);
            drinkPart(outer, "#panel", -1, 0.9F, 4.5F, 4, 1.5F, 10.5F, 13);
            stillBox(outer, "#brass", "#brass", 0.2F, 4.5F, 0.2F, 16, 5.5F, 0.9F);    // the foot rail, round the corner
            stillBox(outer, "#brass", "#brass", 0.2F, 4.5F, 0.9F, 0.9F, 5.5F, 16);
            stillBox(outer, "#brass", "#brass", 14, 4, 0.6F, 15, 5.5F, 1.5F);
            stillBox(outer, "#brass", "#brass", 0.6F, 4, 14, 1.5F, 5.5F, 15);

            // The corner turning in, round the customers (the fronts of the counters north and west of it meet at its
            // north-west corner): solid but for a notch there, framed, the foot rail turning inside it.
            BlockModelBuilder inner = counterModel(w + "_bar_counter_inner", w);
            stillBox(inner, "#planks", "#planks", 0, 13.5F, 0, 16, 16, 16);
            stillBox(inner, "#planks", "#planks", 1.5F, 2, 0, 16, 13.5F, 16);
            stillBox(inner, "#planks", "#planks", 0, 2, 1.5F, 1.5F, 13.5F, 16);
            stillBox(inner, "#trim", "#trim", 2.5F, 0, 0, 16, 2, 16);
            stillBox(inner, "#trim", "#trim", 0, 0, 2.5F, 2.5F, 2, 16);
            stillBox(inner, "#trim", "#trim", 0, 2, 1, 1.5F, 13.5F, 1.5F);            // the stiles framing the notch
            stillBox(inner, "#trim", "#trim", 1, 2, 0, 1.5F, 13.5F, 1);
            stillBox(inner, "#brass", "#brass", 0, 4.5F, 0.2F, 0.9F, 5.5F, 0.9F);     // the foot rail turning
            stillBox(inner, "#brass", "#brass", 0.2F, 4.5F, 0, 0.9F, 5.5F, 0.2F);

            getVariantBuilder(ModBlocks.BAR_COUNTERS.get(wood).get()).forAllStates(state -> {
                var shape = state.getValue(io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.SHAPE);
                boolean right = shape == net.minecraft.world.level.block.state.properties.StairsShape.OUTER_RIGHT
                        || shape == net.minecraft.world.level.block.state.properties.StairsShape.INNER_RIGHT;
                ModelFile model = switch (shape) {
                    case STRAIGHT -> counter;
                    case OUTER_LEFT, OUTER_RIGHT -> outer;
                    default -> inner;
                };
                // Each corner model turns the bartender's left; a right-hand one is it turned a quarter clockwise.
                int turn = facingRotation(state.getValue(io.github.spencerharris192.seedtocellar.decor.BarCounterBlock.FACING)) + (right ? 90 : 0);
                return ConfiguredModel.builder().modelFile(model).rotationY(turn % 360).build();
            });

            BlockModelBuilder stool = models().withExistingParent(w + "_bar_stool", mcLoc("block/block")).renderType("cutout")
                    .texture("particle", modLoc("block/bar_" + w + "_planks")).texture("planks", modLoc("block/bar_" + w + "_planks"))
                    .texture("trim", modLoc("block/bar_" + w + "_trim")).texture("cushion", modLoc("block/bar_cushion"));
            for (float[] leg : new float[][]{{3.5F, 3.5F}, {11, 3.5F}, {3.5F, 11}, {11, 11}}) {
                stillBox(stool, "#trim", "#trim", leg[0], 0, leg[1], leg[0] + 1.5F, 11.5F, leg[1] + 1.5F);   // four legs
            }
            stillBox(stool, "#trim", "#trim", 4.25F, 4, 3.75F, 11.75F, 4.75F, 4.75F);   // the foot ring, round all four
            stillBox(stool, "#trim", "#trim", 4.25F, 4, 11.25F, 11.75F, 4.75F, 12.25F);
            stillBox(stool, "#trim", "#trim", 3.75F, 4, 4.25F, 4.75F, 4.75F, 11.75F);
            stillBox(stool, "#trim", "#trim", 11.25F, 4, 4.25F, 12.25F, 4.75F, 11.75F);
            roundBox(stool, "#planks", "#planks", 3, 11.5F, 3, 13, 12.75F, 13);         // the round seat
            roundBox(stool, "#cushion", "#cushion", 3.5F, 12.75F, 3.5F, 12.5F, 14, 12.5F);   // and its cushion
            simpleBlock(ModBlocks.BAR_STOOLS.get(wood).get(), stool);
        }
        tavernWall();
    }

    /**
     * The rest of the tavern set, all on walls (facing north, the wall to the south). The Mug Rack: an oak board with a
     * capping rail and four pegs (its mugs are drawn by the rack's renderer). The Hop Garland: a fresh bine sagging
     * between nails at the block's edges (so neighbors join into one garland), leaves and cones hanging from it. The
     * Tavern Sign: an iron plate and bracket arm out from the wall, a brace, two hooks, and the board, its picture on both
     * faces, one model per picture.
     */
    private void tavernWall() {
        BlockModelBuilder rack = models().withExistingParent("mug_rack", mcLoc("block/block"))
                .texture("particle", modLoc("block/bar_oak_planks")).texture("planks", modLoc("block/bar_oak_planks"))
                .texture("trim", modLoc("block/bar_oak_trim"));
        stillBox(rack, "#planks", "#planks", 0, 9, 14, 16, 14.5F, 16);               // the board
        stillBox(rack, "#trim", "#trim", 0, 14.5F, 13.5F, 16, 15.5F, 16);            // its capping rail
        stillBox(rack, "#trim", "#trim", 0, 8, 14.25F, 16, 9, 16);                   // and a bead along its foot
        for (float x : new float[]{14, 10, 6, 2}) stillBox(rack, "#trim", "#trim", x - 0.5F, 11.5F, 12, x + 0.5F, 12.5F, 14);   // pegs
        horizontalBlock(ModBlocks.MUG_RACK.get(), rack);

        BlockModelBuilder garland = models().withExistingParent("hop_garland", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/garland_leaf")).texture("bine", modLoc("block/garland_bine"))
                .texture("leaf", modLoc("block/garland_leaf")).texture("hop", modLoc("block/bundle_hop"))
                .texture("nail", modLoc("block/bundle_nail"));
        for (int i = 0; i < 8; i++) {   // the bine, sagging toward the middle
            float x = i * 2 + 1, y = sag(x);
            bundlePart(garland, "#bine", x - 1, y - 0.45F, 14.6F, x + 1, y + 0.45F, 15.4F);
        }
        for (float x : new float[]{1.5F, 5.5F, 9.5F, 13.5F}) {   // leaves hanging off it
            float y = sag(x);
            bundlePart(garland, "#leaf", x - 1.3F, y - 2.9F, 14.3F, x + 1.3F, y - 0.3F, 14.6F);
        }
        for (float x : new float[]{3.5F, 7.5F, 11.5F}) {          // and cones lower down, each on its stalk, tapering
            float y = sag(x);
            bundlePart(garland, "#bine", x - 0.15F, y - 1.5F, 14.85F, x + 0.15F, y - 0.4F, 15.15F);
            bundlePart(garland, "#hop", x - 0.6F, y - 3.1F, 14.4F, x + 0.6F, y - 1.5F, 15.6F);
            bundlePart(garland, "#hop", x - 0.35F, y - 3.7F, 14.65F, x + 0.35F, y - 3.1F, 15.35F);
        }
        bundlePart(garland, "#nail", 0, 13.2F, 15, 0.5F, 13.9F, 16);                  // half a nail at each edge
        bundlePart(garland, "#nail", 15.5F, 13.2F, 15, 16, 13.9F, 16);
        getVariantBuilder(ModBlocks.HOP_GARLAND.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(garland)
                .rotationY(facingRotation(state.getValue(io.github.spencerharris192.seedtocellar.decor.WallDecorBlock.FACING))).build());

        java.util.Map<io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem, ModelFile> signs = new java.util.EnumMap<>(
                io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.class);
        for (var emblem : io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.Emblem.values()) {
            String picture = "block/sign_" + emblem.getSerializedName();
            BlockModelBuilder sign = models().withExistingParent("tavern_sign_" + emblem.getSerializedName(), mcLoc("block/block"))
                    .texture("particle", modLoc("block/sign_edge")).texture("iron", modLoc("block/tavern_iron"))
                    .texture("edge", modLoc("block/sign_edge")).texture("picture", modLoc(picture));
            bundlePart(sign, "#iron", 6.5F, 11, 15, 9.5F, 16, 16);                        // the plate on the wall
            bundlePart(sign, "#iron", 7.5F, 14.5F, 0.5F, 8.5F, 15.5F, 15);                // the bracket arm, out from it
            bundlePart(sign, "#iron", 7.6F, 15.5F, 0.5F, 8.4F, 16, 1.5F);                 // its tip, turned up
            bundlePart(sign, "#iron", 7.6F, 13.5F, 13, 8.4F, 14.5F, 15);                  // a knee under it, by the wall
            bundlePart(sign, "#iron", 7.75F, 13, 3.25F, 8.25F, 14.5F, 3.75F);             // two hooks
            bundlePart(sign, "#iron", 7.75F, 13, 12.25F, 8.25F, 14.5F, 12.75F);
            // The board, 12 by 12: its picture on both faces, a pixel of texture to a pixel of board; its edges as they lie.
            var board = sign.element().from(7.25F, 1, 2).to(8.75F, 13, 14);
            for (Direction side : Direction.values()) {
                if (side == Direction.EAST || side == Direction.WEST) board.face(side).texture("#picture").uvs(2, 2, 14, 14).end();
                else board.face(side).texture("#edge").end();
            }
            board.end();
            signs.put(emblem, sign);
        }
        getVariantBuilder(ModBlocks.TAVERN_SIGN.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(signs.get(state.getValue(io.github.spencerharris192.seedtocellar.decor.TavernSignBlock.EMBLEM)))
                .rotationY(facingRotation(state.getValue(io.github.spencerharris192.seedtocellar.decor.WallDecorBlock.FACING))).build());
    }

    /** The garland's sag: 13.6 at the block's edges, 12 in its middle. */
    private static float sag(float x) {
        float t = (x - 8) / 8;
        return 12 + 1.6F * t * t;
    }

    private BlockModelBuilder counterModel(String name, String wood) {
        return models().withExistingParent(name, mcLoc("block/block"))
                .texture("particle", modLoc("block/bar_" + wood + "_planks")).texture("planks", modLoc("block/bar_" + wood + "_planks"))
                .texture("trim", modLoc("block/bar_" + wood + "_trim")).texture("panel", modLoc("block/bar_" + wood + "_panel"))
                .texture("brass", modLoc("block/bar_brass"));
    }

    /**
     * Hanging bundles (GDD section 17.3), each in two models: under a block, hanging from a string down its middle; on a
     * wall, from an iron nail, pushed back against the wall (facing north, the wall to the south; turned by FACING).
     * Every face shows its whole texture, so each little part reads as one thing: a cone, a bulb, a pepper.
     */
    private void hangingBundles() {
        bundle(ModBlocks.HOP_BUNDLE.get(), "hop_bundle", 11.5F, (m, z) -> {
            bundlePart(m, "#bine", 7.25F, 11.5F, z - 0.75F, 8.75F, 12.75F, z + 0.75F);    // the bines' cut ends
            bundlePart(m, "#twine", 6.75F, 10.5F, z - 1.25F, 9.25F, 11.5F, z + 1.25F);    // tied
            float[][] cones = {   // dried cones on short stalks, in a loose bunch narrowing to one at the bottom: x, top, z
                    {6.4F, 10, -1}, {9.5F, 10, -1.2F}, {6.7F, 10, 1.4F}, {9.6F, 10, 1.1F},
                    {8, 7.6F, -2}, {5.9F, 7.2F, 0.2F}, {10.1F, 7.4F, 0}, {8.2F, 7.3F, 1.9F},
                    {7, 4.8F, -0.7F}, {9.1F, 4.5F, 0.8F}, {8, 2.2F, 0}};
            for (float[] c : cones) hopCone(m, c[0], c[1], z + c[2], 10.5F);
            bundleLeaf(m, "#leaf", 8.6F, 11.5F, 10, z - 0.9F, z + 0.9F, -22.5F);         // two leaves drooping off the tie
            bundleLeaf(m, "#leaf", 4.5F, 7.4F, 10, z - 0.9F, z + 0.9F, 22.5F);
        });
        bundle(ModBlocks.LAVENDER_BUNDLE.get(), "lavender_bundle", 12.5F, (m, z) -> {
            bundlePart(m, "#stem", 7.2F, 8.5F, z - 0.8F, 8.8F, 12.5F, z + 0.8F);          // the stems, bunched
            bundlePart(m, "#twine", 6.8F, 10.5F, z - 1.2F, 9.2F, 11.5F, z + 1.2F);        // tied
            // Hung upside down: slim flower spikes pointing to the floor, splaying out, a few shorter ones among them.
            // x, z offset, the axis and angle they lean by, how much shorter.
            float[][] spikes = {{8, 0, 0, 0, 0}, {7.4F, 0, 2, -22.5F, 0}, {8.6F, 0, 2, 22.5F, 0}, {8, -0.6F, 0, -22.5F, 0},
                    {8, 0.6F, 0, 22.5F, 0}, {7.5F, 0.5F, 2, -22.5F, 0.9F}, {8.5F, -0.5F, 2, 22.5F, 0.9F},
                    {7.6F, -0.4F, 0, 0, 0.6F}, {8.4F, 0.4F, 0, 0, 1.1F}};
            for (float[] s : spikes) {
                lavenderSpike(m, s[0], z + s[1], s[2] == 2 ? Direction.Axis.Z : Direction.Axis.X, s[3], s[4]);
            }
        });
        bundle(ModBlocks.GARLIC_BRAID.get(), "garlic_braid", 15, (m, z) -> {
            bundlePart(m, "#braid", 7.25F, 1.5F, z - 0.75F, 8.75F, 15, z + 0.75F);          // the braided stalks
            float[][] bulbs = {{6.4F, 11, 0}, {9.6F, 8.6F, 0}, {8, 9.8F, -1.6F}, {6.4F, 6.2F, 0}, {8, 5, 1.6F}, {9.6F, 3.8F, 0},
                    {8, 0.6F, 0}};
            for (float[] b : bulbs) {   // a rounded bulb: a squat octagon, a narrower root end and shoulder, its neck in the braid
                float x = b[0], y = b[1], bz = z + b[2];
                bundlePart(m, "#garlic", x - 1.6F, y + 0.6F, bz - 1, x + 1.6F, y + 2.5F, bz + 1);
                bundlePart(m, "#garlic", x - 1, y + 0.6F, bz - 1.6F, x + 1, y + 2.5F, bz + 1.6F);
                bundlePart(m, "#garlic", x - 1, y, bz - 1, x + 1, y + 0.6F, bz + 1);
                bundlePart(m, "#garlic", x - 0.9F, y + 2.5F, bz - 0.9F, x + 0.9F, y + 3.1F, bz + 0.9F);
                bundlePart(m, "#braid", x - 0.4F, y + 3.1F, bz - 0.4F, x + 0.4F, y + 3.9F, bz + 0.4F);
            }
        });
        bundle(ModBlocks.CHILI_STRING.get(), "chili_string", 15.5F, (m, z) -> {
            bundlePart(m, "#twine", 7.75F, 0.5F, z - 0.25F, 8.25F, 15.5F, z + 0.25F);      // the string they're threaded on
            for (int layer = 0; layer < 4; layer++) {
                float top = 15 - layer * 3.6F;
                float d = layer % 2 == 0 ? 1.4F : 1;
                float[][] around = layer % 2 == 0 ? new float[][]{{d, 0}, {-d, 0}, {0, d}, {0, -d}}
                        : new float[][]{{d, d}, {-d, d}, {d, -d}, {-d, -d}};
                for (float[] a : around) {   // four peppers a tier, green caps up, each tier turned from the last
                    float x = 8 + a[0], pz = z + a[1];
                    bundlePart(m, "#cap", x - 0.75F, top - 0.7F, pz - 0.75F, x + 0.75F, top, pz + 0.75F);
                    bundlePart(m, "#chili", x - 0.65F, top - 2.3F, pz - 0.65F, x + 0.65F, top - 0.7F, pz + 0.65F);   // tapering
                    bundlePart(m, "#chili", x - 0.5F, top - 3.5F, pz - 0.5F, x + 0.5F, top - 2.3F, pz + 0.5F);       // to a point
                    bundlePart(m, "#chili", x - 0.3F, top - 4.2F, pz - 0.3F, x + 0.3F, top - 3.5F, pz + 0.3F);
                }
            }
        });
    }

    /**
     * One bundle's two models and its blockstate. `top`: where the bundle's string or nail meets it; `parts` draws the
     * bundle around (x 8, z) in each model.
     */
    private void bundle(Block block, String name, float top, java.util.function.BiConsumer<BlockModelBuilder, Float> parts) {
        BlockModelBuilder ceiling = bundleModel(name);
        parts.accept(ceiling, 8F);
        if (top < 16) bundlePart(ceiling, "#twine", 7.75F, top, 7.75F, 8.25F, 16, 8.25F);   // up to the block above
        BlockModelBuilder wall = bundleModel(name + "_wall");
        parts.accept(wall, 12F);
        float nail = Math.min(top, 14.5F);
        if (top < 14.5F) bundlePart(wall, "#twine", 7.75F, top, 11.75F, 8.25F, 14.5F, 12.25F);
        bundlePart(wall, "#nail", 7.5F, nail - 0.5F, 11.5F, 8.5F, nail + 0.5F, 16);      // the nail in the wall
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock.WALL) ? wall : ceiling)
                .rotationY(facingRotation(state.getValue(io.github.spencerharris192.seedtocellar.decor.HangingBundleBlock.FACING))).build());
    }

    private BlockModelBuilder bundleModel(String name) {
        String t = "block/bundle_";
        return models().withExistingParent(name, mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc(t + "twine")).texture("twine", modLoc(t + "twine")).texture("nail", modLoc(t + "nail"))
                .texture("hop", modLoc(t + "hop")).texture("bine", modLoc(t + "hop_bine")).texture("leaf", modLoc(t + "hop_leaf"))
                .texture("lavender", modLoc(t + "lavender")).texture("stem", modLoc(t + "lavender_stem"))
                .texture("garlic", modLoc(t + "garlic")).texture("braid", modLoc(t + "braid"))
                .texture("chili", modLoc(t + "chili")).texture("cap", modLoc(t + "chili_cap"));
    }

    private static void bundlePart(BlockModelBuilder m, String texture, float x0, float y0, float z0, float x1, float y1, float z1) {
        drinkPart(m, texture, -1, x0, y0, z0, x1, y1, z1);
    }

    /** A thin leaf off the tie, tipped down `angle` degrees about its inner edge. */
    private static void bundleLeaf(BlockModelBuilder m, String texture, float x0, float x1, float y, float z0, float z1, float angle) {
        var e = m.element().from(x0, y, z0).to(x1, y + 0.2F, z1)
                .rotation().angle(angle).axis(Direction.Axis.Z).origin(angle < 0 ? x0 : x1, y, (z0 + z1) / 2).end();
        for (Direction side : Direction.values()) e.face(side).texture(texture).uvs(0, 0, 16, 16).end();
        e.end();
    }

    /**
     * One dried hop cone hanging from the tie at `tie` on a thin stalk: a body tapering to a narrower tip, so a bunch reads
     * as separate cones with gaps between them.
     */
    private static void hopCone(BlockModelBuilder m, float x, float top, float z, float tie) {
        bundlePart(m, "#bine", x - 0.15F, top, z - 0.15F, x + 0.15F, tie, z + 0.15F);
        bundlePart(m, "#hop", x - 0.8F, top - 2.2F, z - 0.8F, x + 0.8F, top, z + 0.8F);
        bundlePart(m, "#hop", x - 0.45F, top - 3, z - 0.45F, x + 0.45F, top - 2.2F, z + 0.45F);
    }

    /**
     * One lavender stem hung upside down: its stem under the tie, its slim flower spike below tapering to a tip, splayed
     * `angle` about `axis`; `shorter` lifts the flower that far up the stem.
     */
    private static void lavenderSpike(BlockModelBuilder m, float x, float z, Direction.Axis axis, float angle, float shorter) {
        float[][] parts = {{5.6F + shorter, 8.8F}, {2.4F + shorter, 5.6F + shorter}, {1.4F + shorter, 2.4F + shorter}};
        String[] textures = {"#stem", "#lavender", "#lavender"};
        float[] widths = {0.25F, 0.45F, 0.3F};
        for (int i = 0; i < parts.length; i++) {
            float w = widths[i];
            var e = m.element().from(x - w, parts[i][0], z - w).to(x + w, parts[i][1], z + w);
            if (angle != 0) e.rotation().angle(angle).axis(axis).origin(x, 8.8F, z).end();
            for (Direction side : Direction.values()) e.face(side).texture(textures[i]).uvs(0, 0, 16, 16).end();
            e.end();
        }
    }

    private static int facingRotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    private void openClosed(Block block, net.minecraft.world.level.block.state.properties.BooleanProperty open, String openModel, String closedModel) {
        ModelFile opened = models().getExistingFile(modLoc(openModel));
        ModelFile closed = models().getExistingFile(modLoc(closedModel));
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(open) ? opened : closed).build());
    }

    private void maltingTub() {
        getVariantBuilder(ModBlocks.MALTING_TUB.get()).forAllStates(state -> {
            MaltingTubBlock.Contents contents = state.getValue(MaltingTubBlock.CONTENTS);
            ModelFile model;
            if (contents == MaltingTubBlock.Contents.EMPTY) {
                model = tubModel("malting_tub", "block/template_malting_tub");
            } else {
                String content = switch (contents) {
                    case WATER -> "tub_water";
                    case GRAIN -> "malting_grain";
                    case STEEPING -> "malting_steeping";
                    case SPROUTING -> "malting_sprouting";
                    default -> "malting_green_malt";
                };
                model = tubModel("malting_tub_" + contents.getSerializedName(), "block/template_malting_tub_filled")
                        .texture("content", modLoc("block/" + content));
            }
            return ConfiguredModel.builder().modelFile(model).build();
        });
    }

    private BlockModelBuilder tubModel(String name, String template) {
        return models().withExistingParent(name, modLoc(template))
                .texture("side", modLoc("block/malting_tub_side"))
                .texture("inner", modLoc("block/malting_tub_inner"))
                .texture("bottom", modLoc("block/malting_tub_bottom"));
    }

    /**
     * Pies: one model per number of slices taken. Each remaining quarter is a box; its outer sides get
     * the crust, and a side facing a quarter that's been eaten shows the filling (the "inner" texture).
     * Faces between two remaining quarters are left out. UVs are automatic, so the lattice top lines up.
     */
    /**
     * Black Forest Cake, cut from the west like vanilla's cake: a chocolate cake covered in cream (shavings round its foot),
     * the cut face showing its layers; a cream rosette with a cherry on top of every slice still there.
     */
    private void layerCake() {
        ModelFile[] byBites = new ModelFile[LayerCakeBlock.SLICES];
        for (int bites = 0; bites < LayerCakeBlock.SLICES; bites++) {
            String tex = "block/black_forest_cake_";
            BlockModelBuilder m = models().withExistingParent(bites == 0 ? "black_forest_cake" : "black_forest_cake_bite" + bites, mcLoc("block/block"))
                    .texture("particle", modLoc(tex + "side")).texture("top", modLoc(tex + "top")).texture("side", modLoc(tex + "side"))
                    .texture("inner", modLoc(tex + "inner")).texture("bottom", modLoc(tex + "bottom"))
                    .texture("cream", modLoc(tex + "cream")).texture("cherry", modLoc(tex + "cherry"));
            int x0 = LayerCakeBlock.MIN + LayerCakeBlock.SLICE_WIDTH * bites;
            var body = m.element().from(x0, 0, LayerCakeBlock.MIN).to(LayerCakeBlock.MAX, LayerCakeBlock.HEIGHT, LayerCakeBlock.MAX);
            body.face(Direction.UP).texture("#top").end();
            body.face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                body.face(side).texture(side == Direction.WEST && bites > 0 ? "#inner" : "#side").end();
            }
            body.end();
            for (int slice = bites; slice < LayerCakeBlock.SLICES; slice++) {
                float x = LayerCakeBlock.MIN + LayerCakeBlock.SLICE_WIDTH * slice + 0.25F;
                float z = slice % 2 == 0 ? 3 : 11.5F;   // rosettes round the rim, front and back in turn
                agavePart(m, "#cream", x, 8, z, x + 1.5F, 9.25F, z + 1.5F);
                agavePart(m, "#cherry", x + 0.25F, 9.25F, z + 0.25F, x + 1.25F, 10.25F, z + 1.25F);
            }
            byBites[bites] = m;
        }
        getVariantBuilder(ModBlocks.BLACK_FOREST_CAKE.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(byBites[state.getValue(LayerCakeBlock.BITES)]).build());
    }

    private void pies() {
        for (Pies.Pie pie : Pies.all()) {
            ModelFile[] byBites = new ModelFile[PieBlock.SLICES];
            for (int bites = 0; bites < PieBlock.SLICES; bites++) {
                BlockModelBuilder m = models().withExistingParent(bites == 0 ? pie.id() : pie.id() + "_bite" + bites, mcLoc("block/block"))
                        .texture("particle", modLoc("block/" + pie.id() + "_top"))
                        .texture("top", modLoc("block/" + pie.id() + "_top"))
                        .texture("side", modLoc("block/pie_side"))
                        .texture("bottom", modLoc("block/pie_bottom"))
                        .texture("inner", modLoc("block/" + pie.id() + "_inner"));
                for (int q = bites; q < PieBlock.SLICES; q++) {
                    int[] b = PieBlock.QUARTERS[q];
                    var e = m.element().from(b[0], 0, b[1]).to(b[2], PieBlock.HEIGHT, b[3]);
                    e.face(Direction.UP).texture("#top").end();
                    e.face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end();
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        int beyond = PIE_NEIGHBOR[q][side.get2DDataValue()];
                        if (beyond < 0) e.face(side).texture("#side").end();
                        else if (beyond < bites) e.face(side).texture("#inner").end();
                    }
                    e.end();
                }
                byBites[bites] = m;
            }
            getVariantBuilder(pie.block().get()).forAllStates(state ->
                    ConfiguredModel.builder().modelFile(byBites[state.getValue(PieBlock.BITES)]).build());
        }
    }

    /**
     * The Harvest Feast: a wooden platter with a roast chicken in the middle and sides around it, in three
     * looks (full, picked-over, leftovers). The food parts share one small texture sheet, each part mapped to
     * its own patch: chicken skin, potato, carrot, bread, cranberry sauce, bone. Facing the player who placed it.
     */
    private void harvestFeast() {
        ModelFile full = feastModel("harvest_feast", 3);
        ModelFile half = feastModel("harvest_feast_half", 2);
        ModelFile leftovers = feastModel("harvest_feast_leftovers", 0);
        getVariantBuilder(ModBlocks.HARVEST_FEAST.get()).forAllStates(state -> {
            int servings = state.getValue(FeastBlock.SERVINGS);
            ModelFile model = servings == 0 ? leftovers : servings <= FeastBlock.SERVINGS_MAX / 2 ? half : full;
            return ConfiguredModel.builder().modelFile(model).rotationY(facingRotation(state.getValue(FeastBlock.FACING))).build();
        });
    }

    /** `amount`: 3 full, 2 picked-over, 0 just a bone. */
    private ModelFile feastModel(String name, int amount) {
        BlockModelBuilder m = models().withExistingParent(name, mcLoc("block/block"))
                .texture("particle", modLoc("block/feast_platter"))
                .texture("platter", modLoc("block/feast_platter"))
                .texture("food", modLoc("block/harvest_feast"));
        var platter = m.element().from(1, 0, 1).to(15, 1, 15);
        for (Direction side : Direction.values()) {
            var face = platter.face(side).texture("#platter");
            if (side == Direction.DOWN) face.cullface(Direction.DOWN);
            face.end();
        }
        platter.end();
        int[] skin = {0, 0, 8, 8}, potato = {8, 0, 12, 4}, carrot = {12, 0, 16, 4}, bread = {8, 4, 12, 8},
                sauce = {12, 4, 16, 8}, bone = {0, 8, 4, 12};
        if (amount == 0) {
            food(m, 6, 1, 7, 10, 2, 8, bone);
            food(m, 3, 1, 3, 4, 2, 4, potato);
            food(m, 11, 1, 11, 13, 2, 13, sauce);
            return m;
        }
        if (amount == 3) {
            food(m, 5, 1, 5, 11, 5, 11, skin);                // the roast chicken
            food(m, 6, 1, 3, 7, 3, 5, skin);                  // drumsticks, toward the front
            food(m, 9, 1, 3, 10, 3, 5, skin);
            food(m, 2, 1, 7, 4, 3, 9, potato);
            food(m, 12, 1, 2, 15, 2, 3, carrot);
            food(m, 12, 1, 7, 15, 2, 8, carrot);
        } else {
            food(m, 5, 1, 6, 10, 4, 11, skin);                // half the chicken left
            food(m, 6, 1, 4, 7, 3, 6, skin);
        }
        food(m, 2, 1, 2, 4, 3, 4, potato);
        food(m, 2, 1, 11, 5, 3, 14, bread);
        food(m, 11, 1, 11, 14, 2, 14, sauce);
        return m;
    }

    /** One food part: a box with every face showing the same patch of the feast texture. */
    private static void food(BlockModelBuilder m, int x0, int y0, int z0, int x1, int y1, int z1, int[] uv) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) e.face(side).uvs(uv[0], uv[1], uv[2], uv[3]).texture("#food").end();
        e.end();
    }

    /** For each quarter (NW, NE, SE, SW), the quarter beyond each side (by 2D direction: S, W, N, E), or -1 for the edge. */
    private static final int[][] PIE_NEIGHBOR = {
            {3, -1, -1, 1},   // NW: south is SW, east is NE
            {2, 0, -1, -1},   // NE: south is SE, west is NW
            {-1, 3, 1, -1},   // SE: west is SW, north is NE
            {-1, -1, 0, 2},   // SW: north is NW, east is SE
    };

    /** The Drying Rack: two posts on feet with two rails between them (items hang from the rails: DryingRackRenderer). */
    private void dryingRack() {
        BlockModelBuilder m = models().withExistingParent("drying_rack", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/drying_rack")).texture("wood", modLoc("block/drying_rack"));
        box(m, 1, 0, 7, 3, 15, 9, false);      // posts
        box(m, 13, 0, 7, 15, 15, 9, false);
        box(m, 0, 0, 4, 4, 1, 12, true);       // feet
        box(m, 12, 0, 4, 16, 1, 12, true);
        box(m, 3, 13, 7.5F, 13, 14, 8.5F, true);   // rails
        box(m, 3, 7, 7.5F, 13, 8, 8.5F, true);
        horizontalBlock(ModBlocks.DRYING_RACK.get(), m);
    }

    /**
     * The Crushing Tub: a thick base (its top is the floor the fruit lies on, see CrushingTubBlock.FLOOR) and four
     * low staved walls. Faces take their UVs from their position, so the staves run on unbroken from base to rim.
     */
    private void crushingTub() {
        BlockModelBuilder m = models().withExistingParent("crushing_tub", mcLoc("block/block"))
                .texture("particle", modLoc("block/crushing_tub_side")).texture("side", modLoc("block/crushing_tub_side"))
                .texture("inner", modLoc("block/crushing_tub_inner")).texture("rim", modLoc("block/crushing_tub_rim"))
                .texture("bottom", modLoc("block/crushing_tub_bottom"));
        var base = m.element().from(0, 0, 0).to(16, CrushingTubBlock.FLOOR, 16);
        for (Direction side : Direction.Plane.HORIZONTAL) base.face(side).texture("#side").cullface(side).end();
        base.face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end();
        base.face(Direction.UP).texture("#inner").end();
        base.end();
        int f = CrushingTubBlock.FLOOR, r = CrushingTubBlock.RIM;
        tubWall(m, 0, f, 0, 16, r, 2, Direction.SOUTH);
        tubWall(m, 0, f, 14, 16, r, 16, Direction.NORTH);
        tubWall(m, 0, f, 2, 2, r, 14, Direction.EAST);
        tubWall(m, 14, f, 2, 16, r, 14, Direction.WEST);
        simpleBlock(ModBlocks.CRUSHING_TUB.get(), m);
    }

    private static void tubWall(BlockModelBuilder m, int x0, int y0, int z0, int x1, int y1, int z1, Direction inside) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            var face = e.face(side).texture(side == inside ? "#inner" : "#side");
            if (side != inside && side.getOpposite() != inside) face.cullface(side);
            face.end();
        }
        e.face(Direction.UP).texture("#rim").end();
        e.end();
    }

    /**
     * The Fruit Press, facing north (turned to face the player who placed it): a tray with a spout, a slatted cage
     * of fruit on it, and two posts carrying a crossbeam. The screw with its plate, and the handle on top, are
     * separate models the renderer moves (WineryRenderers): the screw reaches well above the beam, as on a real press,
     * so the handle clears it even with the plate right down.
     */
    private void fruitPress() {
        BlockModelBuilder m = models().withExistingParent("fruit_press", mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/fruit_press_wood")).texture("wood", modLoc("block/fruit_press_wood"))
                .texture("tray", modLoc("block/fruit_press_tray")).texture("cage", modLoc("block/fruit_press_cage"));
        pressBox(m, "#tray", 0, 0, 0, 16, 1, 16);                 // tray floor
        pressBox(m, "#tray", 0, 1, 0, 16, 3, 1);                  // tray rim
        pressBox(m, "#tray", 0, 1, 15, 16, 3, 16);
        pressBox(m, "#tray", 0, 1, 1, 1, 3, 15);
        pressBox(m, "#tray", 15, 1, 1, 16, 3, 15);
        pressBox(m, "#tray", 7, 1, -2, 9, 2, 0);                  // spout, at the front
        pressBox(m, "#cage", 2, 3, 2, 14, 10, 3);                 // slatted cage
        pressBox(m, "#cage", 2, 3, 13, 14, 10, 14);
        pressBox(m, "#cage", 2, 3, 3, 3, 10, 13);
        pressBox(m, "#cage", 13, 3, 3, 14, 10, 13);
        pressBox(m, "#wood", 0, 3, 6, 2, 13, 10);                 // posts
        pressBox(m, "#wood", 14, 3, 6, 16, 13, 10);
        box(m, 0, 13, 6, 16, 16, 10, true);                       // crossbeam, its grain running along it
        horizontalBlock(ModBlocks.FRUIT_PRESS.get(), m);

        BlockModelBuilder screw = models().withExistingParent("fruit_press_screw", mcLoc("block/block"))
                .texture("particle", modLoc("block/fruit_press_iron")).texture("wood", modLoc("block/fruit_press_wood"))
                .texture("iron", modLoc("block/fruit_press_iron"));
        pressBox(screw, "#wood", 3, 10, 3, 13, 11.5F, 13);         // pressing plate, resting on the cage
        pressBox(screw, "#iron", 7.5F, 11.5F, 7.5F, 8.5F, 22, 8.5F); // the screw
        BlockModelBuilder handle = models().withExistingParent("fruit_press_handle", mcLoc("block/block"))
                .texture("particle", modLoc("block/fruit_press_wood")).texture("wood", modLoc("block/fruit_press_wood"));
        pressBox(handle, "#wood", 2, 21, 7.5F, 14, 22, 8.5F);      // the bar
        pressBox(handle, "#wood", 2, 22, 7.5F, 3, 24, 8.5F);       // and its grips
        pressBox(handle, "#wood", 13, 22, 7.5F, 14, 24, 8.5F);
    }

    private static void pressBox(BlockModelBuilder m, String texture, float x0, float y0, float z0, float x1, float y1, float z1) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) {
            float[] uv = wrappedUv(side, x0, y0, z0, x1, y1, z1);
            e.face(side).texture(texture).uvs(uv[0], uv[1], uv[2], uv[3]).end();
        }
        e.end();
    }

    /**
     * Vanilla's automatic UVs for a box face, moved back onto the 16x16 texture when the box pokes out of the block
     * (the press's handle and screw rise above it, the spout sticks out in front): automatic UVs there would read
     * outside the texture and render black.
     */
    private static float[] wrappedUv(Direction side, float x0, float y0, float z0, float x1, float y1, float z1) {
        float[] uv = switch (side) {
            case DOWN -> new float[]{x0, 16 - z1, x1, 16 - z0};
            case UP -> new float[]{x0, z0, x1, z1};
            case NORTH -> new float[]{16 - x1, 16 - y1, 16 - x0, 16 - y0};
            case SOUTH -> new float[]{x0, 16 - y1, x1, 16 - y0};
            case WEST -> new float[]{z0, 16 - y1, z1, 16 - y0};
            default -> new float[]{16 - z1, 16 - y1, 16 - z0, 16 - y0};   // EAST
        };
        for (int axis = 0; axis < 2; axis++) {
            float lo = Math.min(uv[axis], uv[axis + 2]), hi = Math.max(uv[axis], uv[axis + 2]);
            float shift = lo < 0 ? 16 * (float) Math.ceil(-lo / 16) : hi > 16 ? -16 * (float) Math.ceil((hi - 16) / 16) : 0;
            uv[axis] += shift;
            uv[axis + 2] += shift;
        }
        return uv;
    }

    /** A plain wooden box; `lengthwise` turns the grain to run along it (rails and feet) instead of up. */
    private static void box(BlockModelBuilder m, float x0, float y0, float z0, float x1, float y1, float z1, boolean lengthwise) {
        var e = m.element().from(x0, y0, z0).to(x1, y1, z1);
        for (Direction side : Direction.values()) {
            var face = e.face(side).texture("#wood");
            if (lengthwise && side.getAxis() != Direction.Axis.X) face.rotation(ModelBuilder.FaceRotation.CLOCKWISE_90);
            face.end();
        }
        e.end();
    }

    /** The Compost Bin: open slatted box; the pile inside rises with the fill level, and goes dark when ready. */
    private void compostBin() {
        getVariantBuilder(ModBlocks.COMPOST_BIN.get()).forAllStates(state -> {
            boolean ready = state.getValue(CompostBinBlock.READY);
            int level = state.getValue(CompostBinBlock.LEVEL);
            String name = ready ? "compost_bin_ready" : level == 0 ? "compost_bin" : "compost_bin_" + level;
            int top = ready ? 13 : level == 0 ? 0 : 1 + level * 3;
            return ConfiguredModel.builder().modelFile(compostBinModel(name, top, ready ? "compost_ready" : "compost_fill")).build();
        });
    }

    private ModelFile compostBinModel(String name, int contentTop, String content) {
        BlockModelBuilder m = models().withExistingParent(name, mcLoc("block/block")).renderType("cutout")
                .texture("particle", modLoc("block/compost_bin_side")).texture("side", modLoc("block/compost_bin_side"))
                .texture("inner", modLoc("block/compost_bin_inner")).texture("bottom", modLoc("block/malting_tub_bottom"));
        m.element().from(2, 0, 2).to(14, 1, 14)
                .face(Direction.UP).uvs(2, 2, 14, 14).texture("#bottom").end()
                .face(Direction.DOWN).uvs(2, 2, 14, 14).texture("#bottom").cullface(Direction.DOWN).end().end();
        // four walls, one pixel thick, 14 high
        wall(m, 1, 1, 15, 2, Direction.SOUTH);
        wall(m, 1, 14, 15, 15, Direction.NORTH);
        wall(m, 1, 2, 2, 14, Direction.EAST);
        wall(m, 14, 2, 15, 14, Direction.WEST);
        if (contentTop > 0) {
            m.texture("content", modLoc("block/" + content));
            m.element().from(2, 1, 2).to(14, contentTop, 14)
                    .face(Direction.UP).uvs(2, 2, 14, 14).texture("#content").end().end();
        }
        return m;
    }

    /**
     * One bin wall from (x0, z0) to (x1, z1), 14 high: the face toward the middle gets the shaded inner boards,
     * the rest the slats. No explicit UVs: Minecraft maps each face by its position, so the planks line up
     * pixel for pixel on every side.
     */
    private static void wall(BlockModelBuilder m, int x0, int z0, int x1, int z1, Direction inside) {
        var e = m.element().from(x0, 0, z0).to(x1, 14, z1);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            e.face(side).texture(side == inside ? "#inner" : "#side").end();
        }
        e.face(Direction.UP).texture("#side").end();
        e.face(Direction.DOWN).texture("#side").cullface(Direction.DOWN).end();
        e.end();
    }

    private static int yRotation(Direction.Axis axis) {
        return axis == Direction.Axis.X ? 0 : 90;
    }

    /**
     * Ages 0-7 shown as the crop's stage textures (a crop with 4 stages shows each for 2 ages).
     * TALL: the top half uses <stage>_top. PADDY: stages that reach above the water use a model
     * with a second set of planes one block up (<stage>_top).
     */
    private void crop(Crop crop) {
        if (crop.style == Crop.Style.SUCCULENT) {
            succulent(crop);
            return;
        }
        if (crop.style == Crop.Style.VINE) {
            trellisVine(crop.block(), crop.texture);
            return;
        }
        if (crop.isPerennial()) {
            // Bushes and herbs: a crossed plant, one look per age (planted, young, flowering, ripe).
            getVariantBuilder(crop.block()).forAllStates(state -> {
                String name = crop.texture + "_stage" + state.getValue(BushCropBlock.AGE);
                return ConfiguredModel.builder().modelFile(models().cross(name, modLoc("block/" + name)).renderType("cutout")).build();
            });
            return;
        }
        getVariantBuilder(crop.block()).forAllStates(state -> {
            int stage = crop.stageFor(state.getValue(CropBlock.AGE));
            String name = crop.texture + "_stage" + stage;
            ModelFile model;
            if (crop.style == Crop.Style.TALL && TallCropBlock.isUpper(state)) {
                String top = crop.texture + "_stage" + Math.max(stage, crop.topFrom) + "_top";   // tops exist from topFrom on
                model = models().crop(top, modLoc("block/" + top)).renderType("cutout");
            } else if (crop.style == Crop.Style.PADDY && crop.hasTop(stage)) {
                model = models().withExistingParent(name, modLoc("block/template_crop_tall"))
                        .texture("crop", modLoc("block/" + name)).texture("top", modLoc("block/" + name + "_top"))
                        .renderType("cutout");
            } else {
                model = models().crop(name, modLoc("block/" + name)).renderType("cutout");
            }
            return ConfiguredModel.builder().modelFile(model).build();
        });
    }

    /**
     * A fruit tree: the sapling as a cross, and its leaves in four looks. Plain leaves use vanilla's leaves
     * model (tinted by the biome, see ClientSetup); blossom, unripe and ripe add an untinted layer of flowers
     * or fruit over the same cube.
     */
    private void fruitTree(FruitTree tree) {
        cross(tree.sapling(), tree.name + "_sapling");
        String[] layers = {null, "blossom", "unripe", "ripe"};
        ModelFile[] byAge = new ModelFile[layers.length];
        Identifier leafTexture = SeedToCellar.parse(tree.leafTexture);
        byAge[0] = models().withExistingParent(tree.name + "_leaves", mcLoc("block/leaves")).texture("all", leafTexture)
                .renderType("cutout_mipped");
        for (int age = 1; age < layers.length; age++) {
            BlockModelBuilder m = models().withExistingParent(tree.name + "_leaves_" + layers[age], mcLoc("block/block"))
                    .texture("particle", leafTexture).texture("all", leafTexture)
                    .texture("overlay", modLoc("block/" + tree.name + "_leaves_" + layers[age])).renderType("cutout_mipped");
            for (String texture : new String[] {"#all", "#overlay"}) {
                var e = m.element().from(0, 0, 0).to(16, 16, 16);
                for (Direction side : Direction.values()) {
                    var face = e.face(side).texture(texture).cullface(side);
                    if (texture.equals("#all")) face.tintindex(0);
                    face.end();
                }
                e.end();
            }
            byAge[age] = m;
        }
        getVariantBuilder(tree.leaves()).forAllStatesExcept(state ->
                        ConfiguredModel.builder().modelFile(byAge[state.getValue(FruitLeavesBlock.AGE)]).build(),
                LeavesBlock.DISTANCE, LeavesBlock.PERSISTENT, LeavesBlock.WATERLOGGED);
    }

    /** A trellis with a vine on it: the trellis panel plus the vine's look for each age (planes in front and behind). */
    private void trellisVine(Block block, String texture) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            int age = state.getValue(TrellisVineBlock.AGE);
            ModelFile model = models().withExistingParent(texture + "_stage" + age, modLoc("block/template_trellis_plant"))
                    .texture("trellis", modLoc("block/trellis"))
                    .texture("plant", modLoc("block/" + texture + "_stage" + age))
                    .renderType("cutout");
            return ConfiguredModel.builder().modelFile(model).rotationY(yRotation(state.getValue(TrellisBlock.AXIS))).build();
        }, TrellisVineBlock.ROOT);
    }

    private void cross(Block block, String name) {
        simpleBlock(block, models().cross(name, modLoc("block/" + name)).renderType("cutout"));
    }
}
