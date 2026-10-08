package io.github.spencerharris192.seedtocellar.gametest;

import java.util.Map;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import io.github.spencerharris192.seedtocellar.winery.WineRackBlockEntity;
import io.github.spencerharris192.seedtocellar.brewing.station.CaskBlockEntity;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.Rotation;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BrewQuality;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

/** The Vintner (job site, trades) and the Vineyard village building. */
public final class VillageTests {
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void theFruitPressIsTheVintnersJobSite(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var state = ModBlocks.FRUIT_PRESS.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
            helper.assertTrue(PoiTypes.forState(state).map(poi -> poi.is(ModVillagers.VINTNER_POI.getKey())).orElse(false),
                    "a press facing " + facing + " is a vintner's job site");
        }
        helper.succeed();
    }

    /** What a villager of this profession might offer at this level (every trade its trade set draws from). */
    private static List<MerchantOffer> offers(GameTestHelper helper, String profession, int level) {
        var sets = helper.getLevel().registryAccess().lookupOrThrow(Registries.TRADE_SET);
        TradeSet set = sets.getOrThrow(ModVillagers.tradeSet(profession, level)).value();
        Villager villager = helper.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER, new BlockPos(1, 1, 1));
        LootContext context = new LootContext.Builder(new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, villager.position())
                .withParameter(LootContextParams.THIS_ENTITY, villager)
                .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, net.minecraft.util.Unit.INSTANCE)
                .create(LootContextParamSets.VILLAGER_TRADE)).create(Optional.empty());
        villager.discard();
        return set.trades().stream().map(trade -> trade.value().getOffer(context)).filter(java.util.Objects::nonNull).toList();
    }

    @GameTest(template = EMPTY)
    public static void vintnersTradeAtEveryLevelAndBuyGoodWine(GameTestHelper helper) {
        for (int level = 1; level <= 5; level++) {
            helper.assertTrue(offers(helper, "vintner", level).size() >= 2, "two or more trades at level " + level);
        }
        Optional<MerchantOffer> buysRed = offers(helper, "vintner", 4).stream()
                .filter(o -> o.getCostA().is(Drinks.RED_WINE.item().get())).findFirst();
        helper.assertTrue(buysRed.isPresent(), "an expert vintner buys red wine");
        ItemStack fiveStars = wine(new BrewQuality(true, true, true, true));
        ItemStack oneStar = wine(new BrewQuality(false, false, false, false));
        helper.assertTrue(buysRed.get().satisfiedBy(fiveStars, ItemStack.EMPTY), "a better bottle than asked is fine");
        helper.assertFalse(buysRed.get().satisfiedBy(oneStar, ItemStack.EMPTY), "a one-star bottle isn't");
        helper.succeed();
    }

    private static ItemStack wine(BrewQuality quality) {
        return DrinkItem.fromFluid(quality.applyTo(new FluidStack(ModFluids.RED_WINE.get(), DrinkItem.SERVING)));
    }

    @GameTest(template = EMPTY)
    public static void vineyardsHaveAPressVinesAndAStreetJoin(GameTestHelper helper) {
        for (String village : List.of("plains", "savanna")) {
            Optional<StructureTemplate> template = helper.getLevel().getStructureTemplateManager().get(SeedToCellar.id("village/" + village + "/vineyard"));
            helper.assertTrue(template.isPresent(), "a " + village + " vineyard template");
            StructurePlaceSettings settings = new StructurePlaceSettings();
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, ModBlocks.FRUIT_PRESS.get()).size() == 1, "one Fruit Press");
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW).size() == 1, "one street join");
            var vine = (village.equals("plains") ? Crops.WHITE_GRAPE : Crops.RED_GRAPE).block();
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, vine).size() == 40, "four rows of vines");
        }
        helper.succeed();
    }

    @GameTest(template = "empty_plot", timeoutTicks = 40)
    public static void aPlacedVineyardStaysStanding(GameTestHelper helper) {
        StructureTemplate template = helper.getLevel().getStructureTemplateManager().getOrCreate(SeedToCellar.id("village/plains/vineyard"));
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        template.placeInWorld(helper.getLevel(), origin, origin, new StructurePlaceSettings(), helper.getLevel().getRandom(), 3);
        helper.runAfterDelay(20, () -> {
            int vines = 0;
            for (BlockPos pos : BlockPos.betweenClosed(0, 1, 0, 10, 3, 10)) {
                if (helper.getBlockState(pos).is(Crops.WHITE_GRAPE.block())) vines++;
            }
            helper.assertTrue(vines == 40, "every vine is still standing (" + vines + ")");
            helper.assertBlockPresent(ModBlocks.FRUIT_PRESS.get(), new BlockPos(5, 1, 9));
            helper.assertBlockPresent(Blocks.LANTERN, new BlockPos(3, 2, 8));
            helper.assertBlockPresent(ModBlocks.CRUSHING_TUB.get(), new BlockPos(8, 1, 9));
            helper.assertTrue(helper.getBlockState(new BlockPos(8, 2, 9)).isAir() && helper.getBlockState(new BlockPos(8, 3, 9)).isAir(),
                    "open sky over the tub, so you can jump in");
            helper.assertBlockPresent(Blocks.OAK_FENCE_GATE, new BlockPos(5, 1, 0));
            helper.succeed();
        });
    }

    @GameTest(template = "empty_plot", timeoutTicks = 40)
    public static void vineyardFencesJoinWhenTheBuildingIsTurned(GameTestHelper helper) {
        StructureTemplate template = helper.getLevel().getStructureTemplateManager().getOrCreate(SeedToCellar.id("village/plains/vineyard"));
        BlockPos origin = helper.absolutePos(new BlockPos(10, 0, 0));   // turned a quarter, it still covers 0-10
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90).setKnownShape(true);
        template.placeInWorld(helper.getLevel(), origin, origin, settings, helper.getLevel().getRandom(), 2);
        for (BlockPos pos : BlockPos.betweenClosed(0, 1, 0, 10, 1, 10)) {
            BlockState state = helper.getBlockState(pos);
            if (!state.is(Blocks.OAK_FENCE) || (pos.getX() % 10 != 0 && pos.getZ() % 10 != 0)) continue;
            int joins = 0;
            for (var side : List.of(FenceBlock.NORTH, FenceBlock.SOUTH, FenceBlock.EAST, FenceBlock.WEST)) if (state.getValue(side)) joins++;
            helper.assertTrue(joins >= 2, "the fence at " + pos.toShortString() + " joins its neighbours (" + joins + ")");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void villagersMayTakeUpTheFruitPressAndTheKettle(GameTestHelper helper) {
        helper.assertTrue(ModVillagers.VINTNER_POI.is(PoiTypeTags.ACQUIRABLE_JOB_SITE),
                "the Fruit Press is in minecraft:acquirable_job_site, or no villager would ever take it");
        helper.assertTrue(ModVillagers.BREWER_POI.is(PoiTypeTags.ACQUIRABLE_JOB_SITE),
                "and so is the Brew Kettle");
        helper.assertTrue(PoiTypes.forState(ModBlocks.BREW_KETTLE.get().defaultBlockState()).map(poi -> poi.is(ModVillagers.BREWER_POI.getKey()))
                .orElse(false), "a kettle is a brewer's job site");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void brewersTradeAtEveryLevelAndBuyGoodBeer(GameTestHelper helper) {
        for (int level = 1; level <= 5; level++) {
            helper.assertTrue(offers(helper, "brewer", level).size() >= 2, "two or more trades at level " + level);
        }
        Optional<MerchantOffer> buysAle = offers(helper, "brewer", 4).stream()
                .filter(o -> o.getCostA().is(Drinks.PALE_ALE.item().get())).findFirst();
        helper.assertTrue(buysAle.isPresent(), "an expert brewer buys pale ale");
        ItemStack fiveStars = DrinkItem.fromFluid(new BrewQuality(true, true, true, true).applyTo(new FluidStack(ModFluids.PALE_ALE.get(), 250)));
        helper.assertTrue(buysAle.get().satisfiedBy(fiveStars, ItemStack.EMPTY), "a better beer than asked is fine");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void brewhousesHaveAKettleOnAFireCasksAndAStreetJoin(GameTestHelper helper) {
        for (String village : List.of("plains", "taiga", "snowy")) {
            Optional<StructureTemplate> template = helper.getLevel().getStructureTemplateManager().get(SeedToCellar.id("village/" + village + "/brewhouse"));
            helper.assertTrue(template.isPresent(), "a " + village + " brewhouse template");
            StructurePlaceSettings settings = new StructurePlaceSettings();
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, ModBlocks.BREW_KETTLE.get()).size() == 1, "one Brew Kettle");
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, Blocks.CAMPFIRE).size() == 1, "on one campfire");
            helper.assertTrue(template.get().filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW).size() == 1, "one street join");
            Optional<StructureTemplate> cellar = helper.getLevel().getStructureTemplateManager().get(SeedToCellar.id("village/" + village + "/brewhouse_cellar"));
            helper.assertTrue(cellar.isPresent(), "its cellar");
            helper.assertTrue(cellar.get().filterBlocks(BlockPos.ZERO, settings, Blocks.LADDER).size() == 3, "with a ladder up");
            helper.assertTrue(cellar.get().filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW).isEmpty(), "and no street join of its own");
        }
        helper.succeed();
    }

    @GameTest(template = "empty_house", timeoutTicks = 40)
    public static void aPlacedBrewhouseWorks(GameTestHelper helper) {
        // as a village builds it: the hall at street level, its cellar under the floor
        var manager = helper.getLevel().getStructureTemplateManager();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        manager.getOrCreate(SeedToCellar.id("village/plains/brewhouse_cellar"))
                .placeInWorld(helper.getLevel(), origin, origin, new StructurePlaceSettings(), helper.getLevel().getRandom(), 2);
        manager.getOrCreate(SeedToCellar.id("village/plains/brewhouse"))
                .placeInWorld(helper.getLevel(), origin.above(4), origin.above(4), new StructurePlaceSettings(), helper.getLevel().getRandom(), 2);
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(ModBlocks.BREW_KETTLE.get(), new BlockPos(5, 5, 10));
            BlockState fire = helper.getBlockState(new BlockPos(5, 4, 10));
            helper.assertTrue(fire.is(Blocks.CAMPFIRE) && fire.getValue(CampfireBlock.LIT), "a lit campfire under the kettle");
            helper.assertBlockPresent(ModBlocks.HOPS.get(), new BlockPos(1, 7, 2));   // the hops stand three trellises tall
            helper.assertBlockPresent(Blocks.LADDER, new BlockPos(8, 1, 9));
            helper.assertBlockPresent(Blocks.OAK_TRAPDOOR, new BlockPos(8, 4, 9));
            // you can walk up to the hatch: the hall floor beside it is clear
            helper.assertTrue(helper.getBlockState(new BlockPos(7, 5, 9)).isAir() && helper.getBlockState(new BlockPos(8, 5, 10)).isAir()
                    && helper.getBlockState(new BlockPos(8, 5, 9)).isAir(), "nothing boxes in the cellar hatch");
            if (!(helper.getBlockEntity(new BlockPos(2, 1, 7), net.minecraft.world.level.block.entity.BlockEntity.class) instanceof CaskBlockEntity cask)) {
                helper.fail("a cask in the cellar");
                return;
            }
            helper.assertTrue(cask.tank().getFluid().getFluid() == ModFluids.OLD_ALE.get() && cask.tank().getFluidAmount() == 4000,
                    "four buckets of Old Ale in the cellar cask");
            helper.assertTrue(cask.years() >= 2, "already two years old");
            helper.assertTrue(helper.getBlockEntity(new BlockPos(6, 6, 5), net.minecraft.world.level.block.entity.BlockEntity.class) instanceof WineRackBlockEntity shelf && shelf.count() == 3,
                    "three ales on the shelf in the hall");
            helper.succeed();
        });
    }

    // in a batch of its own: a press placed by another test nearby could lure the villager away
    @GameTest(template = "empty_plot", timeoutTicks = 6000, batch = "villagers")
    public static void anUnemployedVillagerBecomesAVintner(GameTestHelper helper) {
        helper.setTime(1000);   // morning: at night villagers rest instead of looking for work
        // a little glass pen with the press in it, so the villager can't wander off
        for (BlockPos pos : BlockPos.betweenClosed(2, 0, 3, 7, 1, 7)) {
            if (pos.getX() == 2 || pos.getX() == 7 || pos.getZ() == 3 || pos.getZ() == 7) helper.setBlock(pos, Blocks.GLASS);
        }
        helper.setBlock(new BlockPos(6, 0, 5), ModBlocks.FRUIT_PRESS.get());
        Villager villager = helper.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER, new BlockPos(4, 0, 5));
        helper.succeedWhen(() -> helper.assertTrue(villager.getVillagerData().profession().is(ModVillagers.VINTNER.getKey()),
                "the villager took up the Fruit Press"));
    }

    // like the vintner test: on its own, in the morning, penned in with the kettle
    @GameTest(template = "empty_plot", timeoutTicks = 6000, batch = "villagers")
    public static void anUnemployedVillagerBecomesABrewer(GameTestHelper helper) {
        helper.setTime(1000);
        for (BlockPos pos : BlockPos.betweenClosed(2, 0, 3, 7, 1, 7)) {
            if (pos.getX() == 2 || pos.getX() == 7 || pos.getZ() == 3 || pos.getZ() == 7) helper.setBlock(pos, Blocks.GLASS);
        }
        helper.setBlock(new BlockPos(6, 0, 5), ModBlocks.BREW_KETTLE.get());
        Villager villager = helper.spawn(net.minecraft.world.entity.EntityTypes.VILLAGER, new BlockPos(4, 0, 5));
        helper.succeedWhen(() -> helper.assertTrue(villager.getVillagerData().profession().is(ModVillagers.BREWER.getKey()),
                "the villager took up the Brew Kettle"));
    }

    /**
     * Plans 20 villages of each kind (no blocks placed) and checks our buildings turn up in a fair share of them. They
     * once never did: a building reaching underground can't fit a village (world/CellarPoolElement), and a big building
     * at a low weight is tried too rarely to land. Tuned for about 40%; this fails below 15%.
     */
    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void villagesOftenHaveVineyardsAndBrewhouses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Map<String, List<String>> buildings = Map.of("plains", List.of("vineyard", "brewhouse"), "savanna", List.of("vineyard"),
                "taiga", List.of("brewhouse"), "snowy", List.of("brewhouse"));
        Map<String, ResourceKey<Structure>> villages = Map.of("plains", BuiltinStructures.VILLAGE_PLAINS, "savanna",
                BuiltinStructures.VILLAGE_SAVANNA, "taiga", BuiltinStructures.VILLAGE_TAIGA, "snowy", BuiltinStructures.VILLAGE_SNOWY);
        for (var entry : buildings.entrySet()) {
            var village = structures.getOrThrow(villages.get(entry.getKey()));
            var climate = level.getChunkSource().randomState().createClimateSampler(
                    net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().build());
            Map<String, Integer> found = new java.util.HashMap<>();
            for (int i = 0; i < 20; i++) {
                StructureStart start = village.value().generate(village, level.dimension(), level.registryAccess(),
                        level.getChunkSource().getGenerator(), level.getChunkSource().getGenerator().getBiomeSource(), climate,
                        level.getChunkSource().randomState(), level.getStructureTemplateManager(), 12345L + i,
                        new ChunkPos(1000 + i * 37, -2000 + i * 53), 0, level, biome -> true);
                for (String building : entry.getValue()) {
                    String id = "seedtocellar:village/" + entry.getKey() + "/" + building;
                    if (start.getPieces().stream().anyMatch(piece -> piece instanceof PoolElementStructurePiece pool
                            && pool.getElement().toString().contains(id))) {
                        found.merge(building, 1, Integer::sum);
                    }
                }
            }
            for (String building : entry.getValue()) {
                int count = found.getOrDefault(building, 0);
                helper.assertTrue(count >= 3, entry.getKey() + " villages with a " + building + ": " + count + " of 20");
            }
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void villagesCanBuildVineyardsAndBrewhouses(GameTestHelper helper) {
        var pools = helper.getLevel().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        for (String village : List.of("plains", "savanna")) {
            StructureTemplatePool pool = pools.getValue(SeedToCellar.rl("minecraft", "village/" + village + "/houses"));
            helper.assertTrue(pool != null && pool.getShuffledTemplates(helper.getLevel().getRandom()).stream()
                    .anyMatch(e -> e.toString().contains("seedtocellar:village/" + village + "/vineyard")), village + " houses include the vineyard");
        }
        for (String village : List.of("plains", "taiga", "snowy")) {
            StructureTemplatePool pool = pools.getValue(SeedToCellar.rl("minecraft", "village/" + village + "/houses"));
            helper.assertTrue(pool != null && pool.getShuffledTemplates(helper.getLevel().getRandom()).stream()
                    .anyMatch(e -> e.toString().contains("seedtocellar:village/" + village + "/brewhouse")), village + " houses include the brewhouse");
        }
        helper.succeed();
    }

    private VillageTests() {}
}
