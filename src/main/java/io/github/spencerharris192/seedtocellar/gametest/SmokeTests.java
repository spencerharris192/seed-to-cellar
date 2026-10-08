package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.farming.Crop;
import io.github.spencerharris192.seedtocellar.farming.Crops;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.farming.StorageBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModBlocks;
import io.github.spencerharris192.seedtocellar.registry.ModTags;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Basic checks that run on a dedicated GameTest server (Gradle task {@code runGameTestServer}).
 * Only active in development, when forge.enabledGameTestNamespaces includes our mod ID.
 */
public final class SmokeTests {
    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, SeedToCellar.id(path));
    }

    /** What a crafting recipe makes from the first item each ingredient takes, laid out as it asks. */
    private static ItemStack craft(Recipe<?> recipe) {
        List<ItemStack> grid = new ArrayList<>();
        int width = 3, height = 3;
        if (recipe instanceof ShapedRecipe shaped) {
            width = shaped.getWidth();
            height = shaped.getHeight();
            for (Optional<Ingredient> slot : shaped.getIngredients()) grid.add(slot.map(SmokeTests::first).orElse(ItemStack.EMPTY));
        } else {
            for (Ingredient ingredient : recipe.placementInfo().ingredients()) grid.add(first(ingredient));
            while (grid.size() < 9) grid.add(ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(width, height, grid);
        return recipe instanceof CraftingRecipe crafting ? crafting.assemble(input) : ItemStack.EMPTY;
    }

    private static ItemStack first(Ingredient ingredient) {
        return new ItemStack(ingredient.items().findFirst().orElseThrow());
    }

    /** Uses data/seedtocellar/structure/empty.nbt, a 3x3x3 box of air. */
    private static final String EMPTY = "empty";

    @GameTest(template = EMPTY)
    public static void everyCropIsRegisteredAndTagged(GameTestHelper helper) {
        for (Crop crop : Crops.all()) {
            ItemStack produce = new ItemStack(crop.produce());
            helper.assertTrue(produce.is(ModTags.Items.crop(crop.name)), crop.name + " should be in c:crops/" + crop.name);
            helper.assertTrue(produce.is(ModTags.Items.CROPS), crop.name + " should be in c:crops");
            helper.assertTrue(new ItemStack(crop.seeds()).is(ModTags.Items.SEEDS), crop.seedId + " should be in c:seeds");
            if (crop.kind == Crop.Kind.GRAIN) helper.assertTrue(produce.is(ModTags.Items.GRAIN), crop.name + " should be in c:grains");
            if (crop.isFieldCrop()) helper.assertTrue(crop.block().defaultBlockState().is(BlockTags.CROPS), crop.blockId + " should be in minecraft:crops");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyBaleAndSackPacksUnpacksAndIsTagged(GameTestHelper helper) {
        var recipes = helper.getLevel().recipeAccess();
        for (StorageBlocks.Storage storage : ModBlocks.STORAGE) {
            String id = storage.block().getId().getPath();
            var pack = recipes.byKey(recipeKey(id)).map(RecipeHolder::value);
            var unpack = recipes.byKey(recipeKey(storage.tag() + "_from_" + id)).map(RecipeHolder::value);
            helper.assertTrue(pack.isPresent() && craft(pack.get()).is(storage.block().get().asItem())
                    && pack.get().placementInfo().ingredients().stream().filter(i -> i.test(new ItemStack(storage.contents().get()))).count() == storage.count(),
                    storage.count() + " make a " + id);
            helper.assertTrue(unpack.isPresent() && craft(unpack.get()).getCount() == storage.count(),
                    "a " + id + " gives " + storage.count() + " back");
            helper.assertTrue(new ItemStack(storage.block().get()).is(ModTags.Items.storage(storage.tag())),
                    id + " should be in c:storage_blocks/" + storage.tag());
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
                        net.minecraft.world.level.block.Blocks.SHORT_GRASS.defaultBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, ItemStack.EMPTY)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK);
        var context = new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
        java.util.Optional<net.minecraft.core.Holder<net.minecraft.world.level.storage.loot.predicates.LootItemCondition>> none = java.util.Optional.empty();
        var barley = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, 1000, Crops.BARLEY.seeds(), false, 1F);
        var always = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, 1000, net.minecraft.world.item.Items.WHEAT_SEEDS, false, 1F);
        var never = new io.github.spencerharris192.seedtocellar.world.GrassSeedsModifier(none, 1000, net.minecraft.world.item.Items.WHEAT_SEEDS, false, 0F);
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
