package io.github.spencerharris192.seedtocellar.datagen;

import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.world.item.Items;
import java.util.function.BiConsumer;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.Identifier;
import net.minecraft.data.loot.LootTableSubProvider;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.BushCropBlock;
import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.farming.FruitLeavesBlock;
import io.github.spencerharris192.seedtocellar.farming.FruitTree;
import io.github.spencerharris192.seedtocellar.farming.FruitTrees;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.food.FeastBlock;
import io.github.spencerharris192.seedtocellar.food.PieBlock;
import io.github.spencerharris192.seedtocellar.food.Pies;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlock;
import io.github.spencerharris192.seedtocellar.farming.TrellisVineBlock;
import io.github.spencerharris192.seedtocellar.farming.TallCropBlock;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import io.github.spencerharris192.seedtocellar.registry.ModComponents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Set;

public final class ModLootTableProvider {
    public static LootTableProvider create() {
        return new LootTableProvider(Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST)));
    }

    /** What our village buildings keep in their chests and barrels (filled the first time they're opened). */
    private static final class ChestLoot implements LootTableSubProvider {
        private final LootTableSubProvider.Context context;

        ChestLoot(LootTableSubProvider.Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            generate((id, table) -> context.accept(ResourceKey.create(Registries.LOOT_TABLE, id), table));
        }

        private void generate(BiConsumer<Identifier, LootTable.Builder> out) {
            out.accept(SeedToCellar.id("chests/vineyard"), LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ContextIntProviders.between(3, 6))
                    .add(item(Crops.RED_GRAPE.produce(), 10, 2, 6))
                    .add(item(Crops.WHITE_GRAPE.produce(), 10, 2, 6))
                    .add(item(Crops.RED_GRAPE.seeds(), 6, 1, 2))
                    .add(item(Crops.WHITE_GRAPE.seeds(), 6, 1, 2))
                    .add(item(ModItems.TRELLIS.get(), 6, 2, 4))
                    .add(item(ModItems.WINE_BOTTLE.get(), 8, 1, 3))
                    .add(item(ModItems.GRAPE_LEAVES.get(), 4, 2, 4))
                    .add(item(ModItems.RAISINS.get(), 5, 2, 5))
                    .add(item(ModItems.WINE_YEAST.get(), 3, 1, 1))
                    .add(item(Drinks.RED_WINE.item().get(), 2, 1, 1))
                    .add(item(Drinks.WHITE_WINE.item().get(), 2, 1, 1))));
            out.accept(SeedToCellar.id("chests/brewhouse"), LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ContextIntProviders.between(3, 7))
                    .add(item(Crops.BARLEY.produce(), 10, 3, 8))
                    .add(item(Items.WHEAT, 8, 3, 8))
                    .add(item(Crops.BARLEY.seeds(), 6, 2, 5))
                    .add(item(ModItems.HOP_CONES.get(), 8, 2, 5))
                    .add(item(ModItems.DRIED_HOPS.get(), 6, 1, 4))
                    .add(item(ModItems.PALE_MALT.get(), 8, 2, 6))
                    .add(item(ModItems.AMBER_MALT.get(), 4, 1, 4))
                    .add(item(ModItems.BLACK_MALT.get(), 2, 1, 2))
                    .add(item(ModItems.MUG.get(), 6, 1, 3))
                    .add(item(ModItems.ALE_YEAST.get(), 3, 1, 1))
                    .add(item(ModItems.HOP_RHIZOME.get(), 3, 1, 1))
                    .add(item(Drinks.PALE_ALE.item().get(), 3, 1, 2))));
            out.accept(SeedToCellar.id("chests/brewhouse_cellar"), LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ContextIntProviders.between(2, 5))
                    .add(item(ModItems.MUG.get(), 6, 1, 3))
                    .add(item(ModItems.TAP.get(), 4, 1, 2))
                    .add(item(ModItems.WINE_BOTTLE.get(), 4, 1, 2))
                    .add(item(ModItems.DRIED_HOPS.get(), 4, 1, 3))
                    .add(item(ModItems.SPENT_GRAIN.get(), 5, 2, 6))
                    .add(item(Drinks.AMBER_ALE.item().get(), 3, 1, 1))
                    .add(item(Drinks.STOUT.item().get(), 3, 1, 1))
                    .add(item(Drinks.OLD_ALE.item().get(), 2, 1, 1))));
        }

        private static UniformContainerBase.Builder<?> item(ItemLike item, int weight, int min, int max) {
            return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
        }
    }

    /** Block drops. Every block we register must appear here (vanilla validation enforces it). */
    private static final class BlockLoot extends BlockLootSubProvider {
        BlockLoot(LootTableSubProvider.Context context) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
        }

        @Override
        protected void generate() {
            for (Crop crop : Crops.all()) {
                Block block = crop.block();
                if (crop.style == Crop.Style.VINE) {
                    add(block, trellisVine(block, crop.seeds(), crop.produce(), crop.minYield, crop.maxYield));
                    add(crop.wildBlock(), wildPlant(crop.wildBlock(), crop.seeds(), 1, 1, crop.produce(), 0.5F));
                    continue;
                }
                if (crop.style == Crop.Style.SUCCULENT) {
                    // Agave: always a pup back; cut in flower, its heart and maybe a second pup (1-2 in all).
                    LootItemCondition.Builder flowering = MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, CropBlock.MAX_AGE));
                    add(block, applyExplosionDecay(block, LootTable.lootTable()
                            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.seeds())))
                            .withPool(LootPool.lootPool().when(flowering).add(LootItem.lootTableItem(crop.produce())))
                            .withPool(LootPool.lootPool().when(flowering).when(LootItemRandomChanceCondition.randomChance(0.5F))
                                    .add(LootItem.lootTableItem(crop.seeds())))));
                    continue;
                }
                if (crop.isPerennial()) {
                    // Bushes and herbs: always what you'd plant back; a ripe one adds 1-2 of its harvest.
                    LootItemCondition.Builder ripeBush = MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(BushCropBlock.AGE, BushCropBlock.MAX_AGE));
                    LootTable.Builder table = LootTable.lootTable()
                            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(crop.seeds())))
                            .withPool(LootPool.lootPool().when(ripeBush).add(LootItem.lootTableItem(crop.produce())
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))
                                    .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
                    if (crop.hasFlowers()) {
                        // A flowering elder broken gives its flowers too.
                        table.withPool(LootPool.lootPool().when(MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(BushCropBlock.AGE, 2)))
                                .add(LootItem.lootTableItem(crop.flowers())));
                    }
                    add(block, applyExplosionDecay(block, table));
                    continue;
                }
                // Like wheat. Ripe: 1 crop + 1-4 seeds (Fortune helps). Unripe: the seed back.
                // (Rice plants itself: ripe gives 1-4 rice, unripe 1.)
                LootItemCondition.Builder ripe = MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, CropBlock.MAX_AGE));
                if (crop.style == Crop.Style.TALL) {
                    // Only the lower half drops anything (breaking either half breaks both). Ripe corn gives 2 ears.
                    LootItemCondition.Builder lower = MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(TallCropBlock.HALF, DoubleBlockHalf.LOWER));
                    add(block, applyExplosionDecay(block, LootTable.lootTable()
                            .withPool(LootPool.lootPool().when(lower)
                                    .add(LootItem.lootTableItem(crop.produce()).when(ripe)
                                            .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(2))))
                                    .add(LootItem.lootTableItem(crop.seeds()).when(ripe.invert())))
                            .withPool(LootPool.lootPool().when(lower).when(ripe)
                                    .add(LootItem.lootTableItem(crop.seeds())
                                            .apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantments.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3))))));
                } else {
                    add(block, createCropDrops(block, crop.produce(), crop.seeds(), ripe));
                }
                // Wild plants: shears pick the plant; otherwise seeds, sometimes the crop itself.
                if (crop.hasWild()) {
                    Crop.Wild wild = crop.wild;
                    add(crop.wildBlock(), wildPlant(crop.wildBlock(), crop.seeds(), wild.minSeeds(), wild.maxSeeds(), crop.produce(), wild.bonusChance()));
                }
            }
            // Fruit trees: saplings drop themselves; leaves drop like oak leaves (sapling, sticks), plus the fruit if ripe.
            for (FruitTree tree : FruitTrees.all()) {
                dropSelf(tree.sapling());
                Block leaves = tree.leaves();
                add(leaves, createLeavesDrops(leaves, tree.sapling(), NORMAL_LEAVES_SAPLING_CHANCES)
                        .withPool(LootPool.lootPool()
                                .when(MatchBlock.blockMatches(blocks, leaves, StatePropertiesPredicate.Builder.properties().hasProperty(FruitLeavesBlock.AGE, FruitLeavesBlock.RIPE)))
                                .add(applyExplosionDecay(leaves, LootItem.lootTableItem(tree.fruit())))));
            }
            add(ModBlocks.WILD_HOPS.get(), wildPlant(ModBlocks.WILD_HOPS.get(), ModItems.HOP_RHIZOME.get(), 1, 1, ModItems.HOP_CONES.get(), 0.5F));

            dropSelf(ModBlocks.TRELLIS.get());
            dropSelf(ModBlocks.COMPOST_BIN.get());
            dropSelf(ModBlocks.DRYING_RACK.get());
            for (StorageBlocks.Storage storage : ModBlocks.STORAGE) dropSelf(storage.block().get());
            // An untouched feast can be picked back up; a served one can't.
            Block feast = ModBlocks.HARVEST_FEAST.get();
            add(feast, LootTable.lootTable().withPool(applyExplosionCondition(feast, LootPool.lootPool()
                    .when(MatchBlock.blockMatches(blocks, feast, StatePropertiesPredicate.Builder.properties().hasProperty(FeastBlock.SERVINGS, FeastBlock.SERVINGS_MAX)))
                    .add(LootItem.lootTableItem(feast)))));
            // A whole pie can be picked back up; a cut one can't (take its slices instead).
            for (Pies.Pie pie : Pies.all()) {
                Block block = pie.block().get();
                add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .when(MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(PieBlock.BITES, 0)))
                        .add(LootItem.lootTableItem(block)))));
            }
            Block cake = ModBlocks.BLACK_FOREST_CAKE.get();
            add(cake, LootTable.lootTable().withPool(applyExplosionCondition(cake, LootPool.lootPool()
                    .when(MatchBlock.blockMatches(blocks, cake, StatePropertiesPredicate.Builder.properties().hasProperty(
                                    io.github.spencerharris192.seedtocellar.food.LayerCakeBlock.BITES, 0)))
                    .add(LootItem.lootTableItem(cake)))));
            dropSelf(ModBlocks.THATCH.get());
            dropSelf(ModBlocks.THATCH_STAIRS.get());
            add(ModBlocks.THATCH_SLAB.get(), createSlabItemTable(ModBlocks.THATCH_SLAB.get()));
            dropOther(ModBlocks.FERTILE_FARMLAND.get(), net.minecraft.world.level.block.Blocks.DIRT);   // like farmland
            // Stations drop themselves; their contents drop when broken (preRemoveSideEffects).
            dropSelf(ModBlocks.MALTING_TUB.get());
            dropSelf(ModBlocks.KILN.get());
            dropSelf(ModBlocks.MILLSTONE.get());
            add(ModBlocks.BREW_KETTLE.get(), LootTable.lootTable().withPool(applyExplosionCondition(ModBlocks.BREW_KETTLE.get(),
                    LootPool.lootPool().add(keepWeathering(ModBlocks.BREW_KETTLE.get(), LootItem.lootTableItem(ModBlocks.BREW_KETTLE.get()))))));
            // Vanilla: a pod back; ripe, 2-3 pods
            add(ModBlocks.VANILLA.get(), LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(ModItems.VANILLA_POD.get())
                    .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(2)).when(MatchBlock.blockMatches(blocks, ModBlocks.VANILLA.get(), StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.AGE, 3))))
                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 1), true).when(MatchBlock.blockMatches(blocks, ModBlocks.VANILLA.get(), StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(io.github.spencerharris192.seedtocellar.farming.VanillaVineBlock.AGE, 3)))))));
            // two blocks tall: only the pot (lower half) drops the still, like a door
            add(ModBlocks.POT_STILL.get(), LootTable.lootTable().withPool(applyExplosionCondition(ModBlocks.POT_STILL.get(),
                    LootPool.lootPool().add(keepWeathering(ModBlocks.POT_STILL.get(), LootItem.lootTableItem(ModBlocks.POT_STILL.get()))
                            .when(MatchBlock.blockMatches(blocks, ModBlocks.POT_STILL.get(), StatePropertiesPredicate.Builder.properties().hasProperty(
                                            io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.HALF,
                                            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER))))))
                    .withPool(LootPool.lootPool()   // a fitted Gin Basket comes off with it
                            .when(MatchBlock.blockMatches(blocks, ModBlocks.POT_STILL.get(), StatePropertiesPredicate.Builder.properties()
                                            .hasProperty(io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.HALF,
                                                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                                            .hasProperty(io.github.spencerharris192.seedtocellar.distillery.PotStillBlock.BASKET, true)))
                            .add(LootItem.lootTableItem(ModItems.GIN_BASKET.get()))));
            dropSelf(ModBlocks.FERMENTING_VAT.get());
            dropSelf(ModBlocks.PRESERVING_JAR.get());
            dropSelf(ModBlocks.CRUSHING_TUB.get());
            dropSelf(ModBlocks.FRUIT_PRESS.get());

            // Casks and kegs keep their contents and age when broken; a fitted tap drops too.
            for (DeferredBlock<Block> cask : ModBlocks.CASKS.values()) {
                add(cask.get(), keepContents(cask.get(), true)
                        .withPool(LootPool.lootPool()
                                .when(MatchBlock.blockMatches(blocks, cask.get(), StatePropertiesPredicate.Builder.properties().hasProperty(CaskBlock.TAP, true)))
                                .add(LootItem.lootTableItem(ModItems.TAP.get()))));
            }
            add(ModBlocks.KEG.get(), keepContents(ModBlocks.KEG.get(), false));
            dropSelf(ModBlocks.WINE_RACK.get()); // their bottles drop from the block itself
            add(ModBlocks.PLACED_DRINKS.get(), net.minecraft.world.level.storage.loot.LootTable.lootTable());   // the drinks drop themselves
            ModBlocks.BAR_COUNTERS.values().forEach(block -> dropSelf(block.get()));
            dropSelf(ModBlocks.MUG_RACK.get());   // its mugs drop from the block itself, like every rack's
            dropSelf(ModBlocks.HOP_GARLAND.get());
            dropSelf(ModBlocks.TAVERN_SIGN.get());
            ModBlocks.BAR_STOOLS.values().forEach(block -> dropSelf(block.get()));
            for (var bundle : java.util.List.of(ModBlocks.HOP_BUNDLE, ModBlocks.LAVENDER_BUNDLE, ModBlocks.GARLIC_BRAID, ModBlocks.CHILI_STRING)) {
                dropSelf(bundle.get());
            }
            dropSelf(ModBlocks.BOTTLE_SHELF.get());
            dropSelf(ModBlocks.WINE_DISPLAY.get());

            add(ModBlocks.HOPS.get(), trellisVine(ModBlocks.HOPS.get(), ModItems.HOP_RHIZOME.get(), ModItems.HOP_CONES.get(), 1, 3));
        }

        /** A trellis vine: always the trellis back; what was planted, from the soil segment; the harvest if ripe. */
        private LootTable.Builder trellisVine(Block vine, Item plant, Item produce, int min, int max) {
            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(vine, LootPool.lootPool().add(LootItem.lootTableItem(ModItems.TRELLIS.get()))))
                    .withPool(LootPool.lootPool()
                            .when(MatchBlock.blockMatches(blocks, vine, StatePropertiesPredicate.Builder.properties().hasProperty(TrellisVineBlock.ROOT, true)))
                            .add(LootItem.lootTableItem(plant)))
                    .withPool(LootPool.lootPool()
                            .when(MatchBlock.blockMatches(blocks, vine, StatePropertiesPredicate.Builder.properties().hasProperty(TrellisVineBlock.AGE, TrellisVineBlock.MAX_AGE)))
                            .add(applyExplosionDecay(vine, LootItem.lootTableItem(produce)
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max))))));
        }

        /**
         * A copper station keeps its weathering and wax (its blockstate rides on the item, which places it back as it was);
         * fresh, unwaxed copper drops a plain item that stacks with new ones.
         */
        private <T extends UniformContainerBase.Builder<?>> T keepWeathering(Block block, T item) {
            item.apply(net.minecraft.world.level.storage.loot.functions.CopyBlockState.copyState(block)
                    .copy(io.github.spencerharris192.seedtocellar.decor.CopperWeathering.STAGE)
                    .when(net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert(
                            MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder
                                    .properties().hasProperty(io.github.spencerharris192.seedtocellar.decor.CopperWeathering.STAGE,
                                            io.github.spencerharris192.seedtocellar.decor.CopperWeathering.Stage.UNAFFECTED)))));
            item.apply(net.minecraft.world.level.storage.loot.functions.CopyBlockState.copyState(block)
                    .copy(io.github.spencerharris192.seedtocellar.decor.CopperWeathering.WAXED)
                    .when(MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder
                            .properties().hasProperty(io.github.spencerharris192.seedtocellar.decor.CopperWeathering.WAXED, true))));
            return item;
        }

        /** The cask item keeps the contents and their age (CaskContents); a charred cask stays charred (its blockstate rides on the item). */
        private LootTable.Builder keepContents(Block block, boolean charrable) {
            var item = LootItem.lootTableItem(block)
                    .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                            .include(ModComponents.CASK_CONTENTS.get()));
            if (charrable) {
                item.apply(net.minecraft.world.level.storage.loot.functions.CopyBlockState.copyState(block).copy(CaskBlock.CHARRED)
                        .when(MatchBlock.blockMatches(blocks, block, StatePropertiesPredicate.Builder.properties().hasProperty(CaskBlock.CHARRED, true))));
            }
            return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().add(item)));
        }

        private LootTable.Builder wildPlant(Block block, Item seed, int min, int max, Item bonus, float bonusChance) {
            return LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(LootItem.lootTableItem(block).when(hasShears())
                            .otherwise(applyExplosionDecay(block, LootItem.lootTableItem(seed)
                                    .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)))))))
                    .withPool(LootPool.lootPool().when(InvertedLootItemCondition.invert(hasShears()))
                            .when(LootItemRandomChanceCondition.randomChance(bonusChance))
                            .add(LootItem.lootTableItem(bonus)));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::get).map(Block.class::cast)::iterator;
        }
    }

    private ModLootTableProvider() {}
}
