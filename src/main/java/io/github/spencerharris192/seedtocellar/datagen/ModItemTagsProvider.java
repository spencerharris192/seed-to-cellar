package io.github.spencerharris192.seedtocellar.datagen;

import net.minecraft.world.item.Items;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.Climate;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.Vessel;
import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, SeedToCellar.MOD_ID, files);
    }

    /**
     * Tough As Nails (GDD section 22.2), by its own tags: our drinks quench thirst (juices most, then beer, wine, and spirits
     * barely), and every warming drink warms you. Without Tough As Nails nobody reads these.
     */
    private void toughAsNails() {
        for (Drinks.Drink drink : Drinks.all()) {
            net.minecraft.world.item.Item item = drink.item().get();
            boolean alcoholic = drink.profile().alcoholic();
            int thirst, hydration;
            switch (drink.vessel()) {
                case GLASS_BOTTLE -> { thirst = drink == Drinks.COFFEE ? 4 : 6; hydration = drink == Drinks.COFFEE ? 20 : 40; }
                case MUG -> { thirst = alcoholic ? 4 : 6; hydration = alcoholic ? 30 : 40; }
                case WINE_BOTTLE -> { thirst = 3; hydration = 20; }
                default -> { thirst = 1; hydration = 10; }   // spirits and liqueurs
            }
            tag(net.minecraft.tags.ItemTags.create(SeedToCellar.rl("toughasnails", "thirst/" + thirst + "_thirst_drinks"))).add(item);
            tag(net.minecraft.tags.ItemTags.create(SeedToCellar.rl("toughasnails", "hydration/" + hydration + "_hydration_drinks"))).add(item);
            if (drink.profile().effect().get() == io.github.spencerharris192.seedtocellar.effect.ModEffects.WARMTH.get()) {
                tag(net.minecraft.tags.ItemTags.create(SeedToCellar.rl("toughasnails", "heating_consumed_items"))).add(item);
            }
        }
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Specific tags hold the item; parent tags hold the specific tags (Farmer's Delight style).
        for (Crop crop : Crops.all()) {
            tag(ModTags.Items.crop(crop.name)).add(crop.produce());
            tag(ModTags.Items.CROPS).addTag(ModTags.Items.crop(crop.name));
            tag(ModTags.Items.flat(crop.name)).add(crop.produce());
            tag(ModTags.Items.seeds(crop.name)).add(crop.seeds());
            tag(ModTags.Items.SEEDS).addTag(ModTags.Items.seeds(crop.name));
            switch (crop.kind) {
                case GRAIN -> {
                    tag(ModTags.Items.grain(crop.name)).add(crop.produce());
                    tag(ModTags.Items.GRAIN).addTag(ModTags.Items.grain(crop.name));
                }
                case VEGETABLE -> {
                    tag(ModTags.Items.vegetable(crop.name)).add(crop.produce());
                    tag(ModTags.Items.VEGETABLES).addTag(ModTags.Items.vegetable(crop.name));
                }
                case BERRY -> {
                    tag(ModTags.Items.fruit(crop.name)).add(crop.produce());
                    tag(ModTags.Items.FRUITS).addTag(ModTags.Items.fruit(crop.name));
                    tag(ModTags.Items.BERRIES).add(crop.produce());
                }
                case FRUIT -> {
                    tag(ModTags.Items.fruit(crop.name)).add(crop.produce());
                    tag(ModTags.Items.FRUITS).addTag(ModTags.Items.fruit(crop.name));
                }
                default -> { }
            }
            // Farmer villagers plant and harvest our field crops too (not rice: they only plant on farmland).
            if (crop.style != Crop.Style.PADDY && crop.style != Crop.Style.SUCCULENT && crop.isFieldCrop()) {
                tag(ItemTags.VILLAGER_PLANTABLE_SEEDS).add(crop.seeds());
            }
        }
        // Fruit trees: forge:fruits/<tree> (apple included, for other mods), flat forge:<fruit>, saplings and leaves.
        for (FruitTree tree : FruitTrees.all()) {
            tag(ModTags.Items.fruit(tree.name)).add(tree.fruit());
            tag(ModTags.Items.FRUITS).addTag(ModTags.Items.fruit(tree.name));
            if (tree.ownFruit()) tag(ModTags.Items.flat(tree.fruitId)).add(tree.fruit());
            tag(ItemTags.SAPLINGS).add(tree.saplingItem());
            tag(ItemTags.LEAVES).add(tree.leavesItem());
        }
        // Grapes of either color, for recipes other mods (and ours) write as "any grapes".
        tag(ModTags.Items.GRAPES).addTags(ModTags.Items.fruit(Crops.RED_GRAPE.name), ModTags.Items.fruit(Crops.WHITE_GRAPE.name));
        tag(ModTags.Items.FRUITS).addTag(ModTags.Items.GRAPES);
        tag(ModTags.Items.CROPS_HOPS).add(ModItems.HOP_CONES.get());
        tag(ModTags.Items.HOPS_FLAT).add(ModItems.HOP_CONES.get());
        tag(ModTags.Items.CROPS).addTag(ModTags.Items.CROPS_HOPS);
        tag(ModTags.Items.SEEDS_HOPS).add(ModItems.HOP_RHIZOME.get());
        tag(ModTags.Items.SEEDS).addTag(ModTags.Items.SEEDS_HOPS);

        // Flour joins Create's and others' flour tags.
        tag(ModTags.Items.FLOUR_WHEAT).add(ModItems.WHEAT_FLOUR.get());
        tag(ModTags.Items.FLOUR).addTag(ModTags.Items.FLOUR_WHEAT);
        tag(ModTags.Items.FLOUR_RYE).add(ModItems.RYE_FLOUR.get());
        tag(ModTags.Items.CORNMEAL).add(ModItems.CORNMEAL.get());
        tag(ModTags.Items.FLOUR).addTags(ModTags.Items.FLOUR_RYE, ModTags.Items.CORNMEAL);
        tag(ModTags.Items.DOUGH_WHEAT).add(ModItems.DOUGH.get());
        tag(ModTags.Items.DOUGH_RYE).add(ModItems.RYE_DOUGH.get());
        tag(ModTags.Items.DOUGH).addTags(ModTags.Items.DOUGH_WHEAT, ModTags.Items.DOUGH_RYE);
        tag(ModTags.Items.BREAD).add(ModItems.RYE_BREAD.get(), ModItems.SOURDOUGH_BREAD.get(), ModItems.CORNBREAD.get(),
                ModItems.SPENT_GRAIN_BREAD.get(), ModItems.BEER_BREAD.get(), net.minecraft.world.item.Items.BREAD);
        for (Drinks.Drink beer : Drinks.beers()) tag(ModTags.Items.ALES).add(beer.item().get());
        for (RegistryObject<Item> jam : ModItems.JAMS) tag(ModTags.Items.JAMS).add(jam.get());
        tag(ModTags.Items.DRIED_FRUITS).add(ModItems.DRIED_BERRIES.get(), ModItems.RAISINS.get(), ModItems.GOLDEN_RAISINS.get(),
                ModItems.PRUNES.get(), ModItems.DRIED_CHERRIES.get(), ModItems.DRIED_APPLES.get(), ModItems.DRIED_PEACHES.get(),
                ModItems.DRIED_PEARS.get());
        tag(ModTags.Items.SWEETENERS).add(Items.SUGAR, ModItems.SORGHUM_SYRUP.get(), ModItems.MOLASSES.get(), ModItems.AGAVE_SYRUP.get());
        tag(ModTags.Items.DRYABLE_BERRIES).add(Crops.BLUEBERRY.produce(), Crops.BLACKBERRY.produce(), Crops.CRANBERRY.produce(),
                Crops.ELDERBERRY.produce(), net.minecraft.world.item.Items.SWEET_BERRIES);

        // Mash ingredients: grist counts fully, whole malt half (see MaltType). Other mods' malts can join.
        tag(MaltType.PALE.gristTag).add(ModItems.PALE_GRIST.get());
        tag(MaltType.AMBER.gristTag).add(ModItems.AMBER_GRIST.get());
        tag(MaltType.BLACK.gristTag).add(ModItems.BLACK_GRIST.get());
        tag(MaltType.PALE.maltTag).add(ModItems.PALE_MALT.get());
        tag(MaltType.AMBER.maltTag).add(ModItems.AMBER_MALT.get());
        tag(MaltType.BLACK.maltTag).add(ModItems.BLACK_MALT.get());
        tag(MaltType.WHEAT.gristTag).add(ModItems.WHEAT_GRIST.get());
        tag(MaltType.WHEAT.maltTag).add(ModItems.WHEAT_MALT.get());
        // Adjuncts: mash with malt into a corn or potato wash. Whole corn stays out (it pops into popcorn in the kettle).
        tag(MaltType.CORN.gristTag).addTag(ModTags.Items.CORNMEAL);
        tag(MaltType.POTATO.gristTag).addTag(net.minecraftforge.common.Tags.Items.CROPS_POTATO);
        tag(MaltType.CORN.maltTag);
        tag(MaltType.POTATO.maltTag);
        // Vanilla: the pod is its crop (and plants itself).
        tag(ModTags.Items.crop("vanilla")).add(ModItems.VANILLA_POD.get());
        tag(ModTags.Items.CROPS).addTag(ModTags.Items.crop("vanilla"));
        tag(ModTags.Items.flat("vanilla")).add(ModItems.VANILLA_POD.get());
        tag(ModTags.Items.seeds("vanilla")).add(ModItems.VANILLA_POD.get());
        // Distillery: spirits, the still's filter and the gin basket's botanicals.
        for (Drinks.Drink spirit : Drinks.spirits()) tag(ModTags.Items.SPIRITS).add(spirit.item().get());
        toughAsNails();
        tag(ModTags.Items.CHILIES).addTag(ModTags.Items.crop("chili")).add(ModItems.DRIED_CHILI.get());
        tag(ModTags.Items.COOKED_RICE).add(ModItems.COOKED_RICE.get()).addOptional(SeedToCellar.rl("farmersdelight", "cooked_rice"));
        for (Drinks.Drink liqueur : Drinks.liqueurs()) tag(ModTags.Items.LIQUEURS).add(liqueur.item().get());
        tag(ModTags.Items.FILTER_CHARCOAL).add(net.minecraft.world.item.Items.CHARCOAL);
        tag(ModTags.Items.BOTANICALS).addTags(ModTags.Items.crop("juniper"), ModTags.Items.crop("coriander"), ModTags.Items.crop("anise"),
                ModTags.Items.crop("lavender"), ModTags.Items.crop("mint"), ModTags.Items.crop("wormwood"), ModTags.Items.crop("ginger"),
                ModTags.Items.crop("cucumber")).add(ModItems.LEMON_PEEL.get(), ModItems.ORANGE_PEEL.get());
        tag(ModTags.Items.BOIL_HOPS).add(ModItems.DRIED_HOPS.get());

        for (Drinks.Drink drink : Drinks.all()) tag(ModTags.Items.DRINKS).add(drink.item().get());
        for (Drinks.Drink drink : Drinks.all()) {
            if (drink.vessel() == Vessel.WINE_BOTTLE && drink.profile().graded()) tag(ModTags.Items.WINES).add(drink.item().get());
        }
        tag(ModTags.Items.WINE_RACK_BOTTLES).addTag(ModTags.Items.WINES);
        tag(ModTags.Items.SHELF_DRINKS).addTag(ModTags.Items.DRINKS);
        tag(ModTags.Items.MUG_RACK_ITEMS).add(ModItems.MUG.get());

        // Serene Seasons (optional) shows the growing seasons on anything in these tags.
        for (Crop crop : Crops.all()) {
            for (String season : crop.climate.seasons()) tag(seasonCrops(season)).add(crop.seeds());
        }
        for (String season : Climate.TEMPERATE.seasons()) tag(seasonCrops(season)).add(ModItems.HOP_RHIZOME.get());

        // Sickles; straw (Farmer's Delight's joins); thatch stairs and slab.
        for (RegistryObject<Item> sickle : ModItems.SICKLES) tag(ModTags.Items.SICKLES).add(sickle.get());
        tag(ModTags.Items.TOOLS).addTag(ModTags.Items.SICKLES);
        tag(ModTags.Items.STRAW).add(ModItems.STRAW.get()).addOptional(SeedToCellar.rl("farmersdelight", "straw"));
        tag(ItemTags.STAIRS).add(ModItems.THATCH_STAIRS.get());
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            tag(ModTags.Items.storage(storage.tag())).add(storage.block().get().asItem());
            tag(ModTags.Items.STORAGE_BLOCKS).addTag(ModTags.Items.storage(storage.tag()));
        }
        tag(ItemTags.SLABS).add(ModItems.THATCH_SLAB.get());

        // Jerky from any common raw meat (Farmer's Delight style tags join if present).
        tag(ModTags.Items.JERKY_MEATS).add(net.minecraft.world.item.Items.BEEF, net.minecraft.world.item.Items.PORKCHOP,
                        net.minecraft.world.item.Items.MUTTON, net.minecraft.world.item.Items.CHICKEN, net.minecraft.world.item.Items.RABBIT)
                .addOptionalTag(SeedToCellar.rl("forge", "raw_beef")).addOptionalTag(SeedToCellar.rl("forge", "raw_pork"))
                .addOptionalTag(SeedToCellar.rl("forge", "raw_mutton")).addOptionalTag(SeedToCellar.rl("forge", "raw_chicken"));

        // The Compost Bin also takes these (on top of anything the vanilla composter takes).
        tag(ModTags.Items.COMPOSTABLES).add(ModItems.SPENT_GRAIN.get(), net.minecraft.world.item.Items.ROTTEN_FLESH,
                ModItems.GRAPE_POMACE.get(), ModItems.FRUIT_POMACE.get(), ModItems.OLIVE_POMACE.get(), ModItems.BAGASSE.get());
    }

    /** sereneseasons:&lt;season&gt;_crops (item version) */
    private static TagKey<Item> seasonCrops(String season) {
        return TagKey.create(Registries.ITEM, SeedToCellar.rl("sereneseasons", season + "_crops"));
    }
}
