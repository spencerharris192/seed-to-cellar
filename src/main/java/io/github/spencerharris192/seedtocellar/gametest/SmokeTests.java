package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Basic checks that run on a dedicated GameTest server (Gradle task {@code runGameTestServer}).
 * Only active in development, when forge.enabledGameTestNamespaces includes our mod ID.
 */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SmokeTests {
    /** Uses data/seedtocellar/structures/empty.nbt, a 3x3x3 box of air. */
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void everyCropIsRegisteredAndTagged(GameTestHelper helper) {
        for (Crop crop : Crops.all()) {
            ItemStack produce = new ItemStack(crop.produce());
            helper.assertTrue(produce.is(ModTags.Items.crop(crop.name)), crop.name + " should be in forge:crops/" + crop.name);
            helper.assertTrue(produce.is(ModTags.Items.CROPS), crop.name + " should be in forge:crops");
            helper.assertTrue(new ItemStack(crop.seeds()).is(ModTags.Items.SEEDS), crop.seedId + " should be in forge:seeds");
            if (crop.kind == Crop.Kind.GRAIN) helper.assertTrue(produce.is(ModTags.Items.GRAIN), crop.name + " should be in forge:grain");
            if (crop.isFieldCrop()) helper.assertTrue(crop.block().defaultBlockState().is(BlockTags.CROPS), crop.blockId + " should be in minecraft:crops");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyBaleAndSackPacksUnpacksAndIsTagged(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            String id = storage.block().getId().getPath();
            var pack = recipes.byKey(SeedToCellar.id(id));
            var unpack = recipes.byKey(SeedToCellar.id(storage.tag() + "_from_" + id));
            helper.assertTrue(pack.isPresent() && pack.get().getResultItem(helper.getLevel().registryAccess()).is(storage.block().get().asItem())
                    && pack.get().getIngredients().stream().filter(i -> i.test(new ItemStack(storage.contents().get()))).count() == storage.count(),
                    storage.count() + " make a " + id);
            helper.assertTrue(unpack.isPresent() && unpack.get().getResultItem(helper.getLevel().registryAccess()).getCount() == storage.count(),
                    "a " + id + " gives " + storage.count() + " back");
            helper.assertTrue(new ItemStack(storage.block().get()).is(ModTags.Items.storage(storage.tag())),
                    id + " should be in forge:storage_blocks/" + storage.tag());
        }
        helper.succeed();
    }

    /** Grass drops our seeds at their grassSeedChances setting (5% for barley); another item at its data file's chance. */
    @GameTest(template = EMPTY)
    public static void grassDropsSeedsAtTheirChance(GameTestHelper helper) {
        var level = helper.getLevel();
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                        net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(net.minecraft.core.BlockPos.ZERO)))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,
                        net.minecraft.world.level.block.Blocks.GRASS.defaultBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        var context = new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(null);
        var none = new net.minecraft.world.level.storage.loot.predicates.LootItemCondition[0];
        var barley = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, Crops.BARLEY.seeds(), false, 1F);
        var always = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, net.minecraft.world.item.Items.WHEAT_SEEDS, false, 1F);
        var never = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, net.minecraft.world.item.Items.WHEAT_SEEDS, false, 0F);
        int seeds = 0, kept = 0, lost = 0;
        for (int i = 0; i < 2000; i++) {
            seeds += barley.apply(new it.unimi.dsi.fastutil.objects.ObjectArrayList<>(), context).size();
            kept += always.apply(new it.unimi.dsi.fastutil.objects.ObjectArrayList<>(), context).size();
            lost += never.apply(new it.unimi.dsi.fastutil.objects.ObjectArrayList<>(), context).size();
        }
        helper.assertTrue(seeds > 50 && seeds < 160, "barley seeds from grass about 1 in 20 (setting 0.05), got " + seeds + " in 2000");
        helper.assertTrue(kept == 2000 && lost == 0, "other items use the data file's chance");
        helper.succeed();
    }

    private SmokeTests() {}
}
