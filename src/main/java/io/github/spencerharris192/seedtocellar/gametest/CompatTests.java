package io.github.spencerharris192.seedtocellar.gametest;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.brewing.Temperature;
import io.github.spencerharris192.seedtocellar.compat.SereneSeasonsCompat;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * Our recipes for other mods' machines (GDD section 22): each loads only with its mod, and with it, the mod reads it as
 * one of its own (a recipe it can't parse never loads). Run with -PwithCompat to load those mods.
 */
@GameTestHolder(SeedToCellar.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompatTests {
    private static final String EMPTY = "empty";

    /** mod id, one of our recipe ids for it, the recipe type it must load as. */
    private record Expect(String mod, String recipe, String type) {}

    private static final List<Expect> EXPECTED = List.of(
            new Expect("create", "create/milling/pale_grist", "create:milling"),
            new Expect("create", "create/compacting/apple_juice", "create:compacting"),
            new Expect("create", "create/compacting/tub_red_grape_must", "create:compacting"),
            new Expect("create", "create/mixing/honey_water", "create:mixing"),
            new Expect("create", "create/mixing/dark_wort", "create:mixing"),
            new Expect("create", "create/mixing/dark_hopped_wort", "create:mixing"),
            new Expect("mekanism", "mekanism/crushing/rye_flour", "mekanism:crushing"),
            new Expect("immersiveengineering", "immersiveengineering/crusher/polished_rice", "immersiveengineering:crusher"),
            new Expect("immersiveengineering", "immersiveengineering/squeezer/olive_oil", "immersiveengineering:squeezer"),
            new Expect("immersiveengineering", "immersiveengineering/cloche/barley", "immersiveengineering:cloche"),
            new Expect("immersiveengineering", "immersiveengineering/cloche/agave", "immersiveengineering:cloche"),
            new Expect("botanypots", "botanypots/crop/rice", "botanypots:crop"),
            new Expect("botanypots", "botanypots/crop/apple_tree", "botanypots:crop"),
            new Expect("botanypots", "botanypots/crop/hops", "botanypots:crop"),
            new Expect("patchouli", "patchouli/brewers_almanac", "minecraft:crafting"));

    @GameTest(template = EMPTY)
    public static void otherModsReadOurRecipes(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (Expect expect : EXPECTED) {
            Optional<? extends Recipe<?>> recipe = recipes.byKey(SeedToCellar.id(expect.recipe()));
            if (!ModList.get().isLoaded(expect.mod())) {
                helper.assertTrue(recipe.isEmpty(), expect.recipe() + " loads only with " + expect.mod());
                continue;
            }
            helper.assertTrue(recipe.isPresent(), expect.mod() + " couldn't read " + expect.recipe());
            String type = String.valueOf(ForgeRegistries.RECIPE_TYPES.getKey(recipe.get().getType()));
            helper.assertTrue(type.equals(expect.type()), expect.recipe() + " is a " + type + ", not a " + expect.type());
        }
        helper.succeed();
    }

    /**
     * A Create brewery makes every beer (GDD section 22.2): for each malt bill, Create's heated mixer mashes grist into sweet
     * wort, takes that wort back in to hop it, and our vat ferments the hopped wort into the right style. Create's mill grinds
     * malt into grist, and a mug takes a serving through Forge's fluid handling, as Create's Spout fills it.
     */
    @GameTest(template = EMPTY)
    public static void createBreweryMakesEveryBeer(GameTestHelper helper) {
        if (!ModList.get().isLoaded("create")) {
            helper.succeed();
            return;
        }
        var recipes = helper.getLevel().getRecipeManager();
        var fermenting = recipes.getAllRecipesFor(io.github.spencerharris192.seedtocellar.registry.ModRecipes.FERMENTING.get());
        java.util.Map<String, net.minecraft.world.level.material.Fluid> styles = java.util.Map.of(
                "pale", Drinks.PALE_ALE.fluid().get(), "amber", Drinks.AMBER_ALE.fluid().get(), "dark", Drinks.STOUT.fluid().get(),
                "strong_amber", Drinks.OLD_ALE.fluid().get(), "wheat", Drinks.WHEAT_BEER.fluid().get());
        styles.forEach((bill, beer) -> {
            Recipe<?> mash = recipes.byKey(SeedToCellar.id("create/mixing/" + bill + "_wort")).orElseThrow();
            Recipe<?> hop = recipes.byKey(SeedToCellar.id("create/mixing/" + bill + "_hopped_wort")).orElseThrow();
            var sweetWort = fluidResult(mash);
            boolean takesIt = ((List<?>) invoke(hop, "getFluidIngredients")).stream().anyMatch(i -> fluidTest(i, sweetWort));
            helper.assertTrue(takesIt, "Create's hopping recipe takes the " + bill + " sweet wort its mash made");
            var hopped = fluidResult(hop);
            var made = fermenting.stream().filter(r -> r.matches(hopped, io.github.spencerharris192.seedtocellar.brewing.YeastType.ALE))
                    .max(java.util.Comparator.comparingInt(io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe::priority));
            helper.assertTrue(made.isPresent() && made.get().result() == beer, "the " + bill + " bill ferments into " + beer);
            if (bill.equals("pale")) {
                var lager = fermenting.stream().filter(r -> r.matches(hopped, io.github.spencerharris192.seedtocellar.brewing.YeastType.LAGER))
                        .max(java.util.Comparator.comparingInt(io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe::priority));
                helper.assertTrue(lager.isPresent() && lager.get().result() == Drinks.LAGER.fluid().get(), "and with lager yeast into lager");
            }
        });
        Recipe<?> mill = recipes.byKey(SeedToCellar.id("create/milling/pale_grist")).orElseThrow();
        var milled = (List<?>) invoke(mill, "getRollableResultsAsItemStacks");
        helper.assertTrue(milled.stream().anyMatch(s -> s instanceof ItemStack stack
                && stack.is(io.github.spencerharris192.seedtocellar.registry.ModItems.PALE_GRIST.get())), "Create's mill grinds pale malt to grist");
        ItemStack mug = new ItemStack(io.github.spencerharris192.seedtocellar.registry.ModItems.MUG.get());
        var handler = mug.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);
        int filled = handler.fill(new net.minecraftforge.fluids.FluidStack(Drinks.PALE_ALE.fluid().get(), 250),
                net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(filled == 250 && handler.getContainer().is(Drinks.PALE_ALE.item().get()), "a spout fills a mug with a pint");
        helper.succeed();
    }

    private static net.minecraftforge.fluids.FluidStack fluidResult(Recipe<?> recipe) {
        return (net.minecraftforge.fluids.FluidStack) ((List<?>) invoke(recipe, "getFluidResults")).get(0);
    }

    /** Create's FluidIngredient.test(FluidStack), without compiling against Create. */
    private static boolean fluidTest(Object ingredient, net.minecraftforge.fluids.FluidStack stack) {
        try {
            return (boolean) ingredient.getClass().getMethod("test", net.minecraftforge.fluids.FluidStack.class).invoke(ingredient, stack);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /** Machine recipe types where one input makes one thing: two recipes for the same input fight over it. */
    private static final List<String> ONE_INPUT_TYPES = List.of("create:milling", "create:crushing", "create:compacting",
            "mekanism:crushing", "immersiveengineering:crusher", "immersiveengineering:squeezer", "farmersdelight:cutting");

    /**
     * Our twins of the mods' machine recipes never take an input one of their own recipes already uses (Create mills wheat
     * into its own flour; Mekanism crushes it into Bio Fuel).
     */
    @GameTest(template = EMPTY)
    public static void ourMachineRecipesDontCompeteWithTheirs(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getRecipes();
        int checked = 0;
        for (String typeId : ONE_INPUT_TYPES) {
            var type = ForgeRegistries.RECIPE_TYPES.getValue(SeedToCellar.parse(typeId));
            if (type == null) continue;   // that mod isn't installed
            List<Recipe<?>> ofType = recipes.stream().filter(r -> r.getType() == type).toList();
            java.util.Map<net.minecraft.world.item.Item, Recipe<?>> theirs = new java.util.HashMap<>();
            for (Recipe<?> recipe : ofType) {
                if (!recipe.getId().getNamespace().equals(SeedToCellar.MOD_ID)) inputs(recipe).forEach(item -> theirs.put(item, recipe));
            }
            for (Recipe<?> ours : ofType) {
                if (!ours.getId().getNamespace().equals(SeedToCellar.MOD_ID)) continue;
                java.util.Set<net.minecraft.world.item.Item> taken = inputs(ours);
                helper.assertFalse(taken.isEmpty(), "can't read the input of " + ours.getId());
                checked++;
                for (var item : taken) {
                    Recipe<?> clash = theirs.get(item);
                    helper.assertTrue(clash == null, ours.getId() + " and " + (clash == null ? "" : clash.getId()) + " both take " + item);
                }
            }
        }
        helper.assertTrue(checked > 0 || !ModList.get().isLoaded("create"), "no machine recipes of ours were checked");
        helper.succeed();
    }

    /**
     * None of our crafting, cooking or Cooking Pot recipes shares its input with another recipe that makes something else:
     * the game would pick one of the two at random (Farmer's Delight's rice bag and cooked rice, Quark's crates,
     * Supplementaries' sugar cube...). Each recipe of ours is filled in as a player would, with each item its tags accept.
     */
    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void ourRecipesDontShareAnInputWithAnother(GameTestHelper helper) {
        var level = helper.getLevel();
        var manager = level.getRecipeManager();
        var access = level.registryAccess();
        java.util.Set<String> clashes = new java.util.TreeSet<>();
        for (var ours : manager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            if (!ours.getId().getNamespace().equals(SeedToCellar.MOD_ID)) continue;
            for (var grid : grids(ours)) {
                if (!ours.matches(grid, level)) continue;
                ItemStack made = ours.assemble(grid, access);
                for (var other : manager.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, grid, level)) {
                    if (other != ours && !sameStack(made, other.assemble(grid, access))) clashes.add(ours.getId() + " / " + other.getId());
                }
            }
        }
        for (var type : List.of(net.minecraft.world.item.crafting.RecipeType.SMELTING, net.minecraft.world.item.crafting.RecipeType.SMOKING,
                net.minecraft.world.item.crafting.RecipeType.BLASTING, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING)) {
            List<? extends Recipe<net.minecraft.world.Container>> all = manager.getAllRecipesFor(type);
            for (var ours : all) {
                if (!ours.getId().getNamespace().equals(SeedToCellar.MOD_ID)) continue;
                for (ItemStack input : ours.getIngredients().get(0).getItems()) {
                    var oven = new net.minecraft.world.SimpleContainer(input.copy());
                    for (var other : all) {
                        if (other != ours && other.matches(oven, level) && !sameStack(ours.getResultItem(access), other.getResultItem(access))) {
                            clashes.add(ours.getId() + " / " + other.getId());
                        }
                    }
                }
            }
        }
        // Farmer's Delight's Cooking Pot: the same ingredients (in any order) make whichever recipe it finds first
        var pot = ForgeRegistries.RECIPE_TYPES.getValue(SeedToCellar.parse("farmersdelight:cooking"));
        if (pot != null) {
            List<Recipe<?>> all = helper.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r.getType() == pot).toList();
            for (Recipe<?> ours : all) {
                if (!ours.getId().getNamespace().equals(SeedToCellar.MOD_ID)) continue;
                for (Recipe<?> other : all) {
                    if (other != ours && sameInputs(ours.getIngredients(), other.getIngredients(), 0, new boolean[other.getIngredients().size()])
                            && !sameStack(ours.getResultItem(access), other.getResultItem(access))) {
                        clashes.add(ours.getId() + " / " + other.getId());
                    }
                }
            }
        }
        clashes.forEach(clash -> SeedToCellar.LOGGER.warn("[recipe clash] {}", clash));
        helper.assertTrue(clashes.isEmpty(), clashes.size() + " recipes share their input with another (see the log): "
                + clashes.stream().limit(3).toList());
        helper.succeed();
    }

    /** A crafting grid filled with a recipe's ingredients, once for each item they accept (up to 8). */
    private static List<net.minecraft.world.inventory.CraftingContainer> grids(net.minecraft.world.item.crafting.CraftingRecipe recipe) {
        List<net.minecraft.world.item.crafting.Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof net.minecraftforge.common.crafting.IShapedRecipe<?> shaped ? shaped.getRecipeWidth() : 3;
        boolean shapedGrid = recipe instanceof net.minecraftforge.common.crafting.IShapedRecipe<?>;
        int alternatives = ingredients.stream().mapToInt(i -> i.getItems().length).max().orElse(0);
        List<net.minecraft.world.inventory.CraftingContainer> grids = new java.util.ArrayList<>();
        for (int k = 0; k < Math.min(alternatives, 8); k++) {
            var grid = new net.minecraft.world.inventory.TransientCraftingContainer(new NoMenu(), 3, 3);
            int slot = 0;
            for (int i = 0; i < ingredients.size() && slot < 9; i++) {
                ItemStack[] items = ingredients.get(i).getItems();
                int at = shapedGrid ? (i / width) * 3 + i % width : slot;
                if (items.length > 0) grid.setItem(at, items[k % items.length].copy());
                if (!shapedGrid && items.length > 0) slot++;
            }
            grids.add(grid);
        }
        return grids;
    }

    /** Each of `ours` from `index` on can be paired with a different one of `theirs` that takes the same item. */
    private static boolean sameInputs(List<net.minecraft.world.item.crafting.Ingredient> ours, List<net.minecraft.world.item.crafting.Ingredient> theirs,
                                      int index, boolean[] used) {
        if (ours.size() != theirs.size()) return false;
        if (index == ours.size()) return true;
        for (int j = 0; j < theirs.size(); j++) {
            if (used[j]) continue;
            boolean overlap = java.util.Arrays.stream(ours.get(index).getItems()).anyMatch(theirs.get(j));
            if (!overlap) continue;
            used[j] = true;
            if (sameInputs(ours, theirs, index + 1, used)) return true;
            used[j] = false;
        }
        return false;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b) && a.getCount() == b.getCount();
    }

    /** A crafting grid needs a screen to tell about changes; this one doesn't listen. */
    private static final class NoMenu extends net.minecraft.world.inventory.AbstractContainerMenu {
        NoMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }

        @Override
        public void slotsChanged(net.minecraft.world.Container container) {
        }
    }

    /** The items a machine recipe takes: its ingredients (Create), or its `input` (Mekanism's getInput(), IE's field). */
    private static java.util.Set<net.minecraft.world.item.Item> inputs(Recipe<?> recipe) {
        java.util.Set<net.minecraft.world.item.Item> items = new java.util.HashSet<>();
        recipe.getIngredients().forEach(i -> java.util.Arrays.stream(i.getItems()).forEach(s -> items.add(s.getItem())));
        if (!items.isEmpty()) return items;
        Object input = invoke(recipe, "getInput");
        if (input == null) {
            try {
                input = recipe.getClass().getField("input").get(recipe);
            } catch (ReflectiveOperationException ignored) {
                return items;
            }
        }
        Object stacks = input instanceof net.minecraft.world.item.crafting.Ingredient ingredient ? ingredient.getItems()
                : invoke(input, "getRepresentations");               // Mekanism
        if (stacks == null) stacks = invoke(input, "getMatchingStacks");   // IE's ingredient with a count
        if (stacks instanceof ItemStack[] array) stacks = List.of(array);
        if (stacks instanceof List<?> list) list.forEach(s -> { if (s instanceof ItemStack stack) items.add(stack.getItem()); });
        return items;
    }

    @org.jetbrains.annotations.Nullable
    private static Object invoke(@org.jetbrains.annotations.Nullable Object target, String method) {
        if (target == null) return null;
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** Serene Seasons moves fermentation temperature a step; Tough As Nails counts our drinks (and warming ones warm). */
    @GameTest(template = EMPTY)
    public static void seasonsAndThirst(GameTestHelper helper) {
        helper.assertTrue(Temperature.shift(Temperature.COOL, -1) == Temperature.COLD
                && Temperature.shift(Temperature.COLD, -1) == Temperature.COLD
                && Temperature.shift(Temperature.WARM, 1) == Temperature.WARM,
                "a season moves the temperature one step, within Cold to Warm");
        if (ModList.get().isLoaded("sereneseasons")) {
            // Winter a step cooler and summer a step warmer in plains; nothing in savanna, which Serene Seasons gives wet and
            // dry seasons instead. The season is set and put back within this tick, so no other test sees it.
            var level = helper.getLevel();
            var biomes = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
            var plains = biomes.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
            var savanna = biomes.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.SAVANNA);
            var commands = level.getServer().createCommandSourceStack().withLevel(level).withSuppressedOutput();
            String before = SereneSeasonsCompat.subSeason(level);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set mid_winter");
            int plainsWinter = SereneSeasonsCompat.seasonStep(level, plains), savannaWinter = SereneSeasonsCompat.seasonStep(level, savanna);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set mid_summer");
            int plainsSummer = SereneSeasonsCompat.seasonStep(level, plains), savannaSummer = SereneSeasonsCompat.seasonStep(level, savanna);
            level.getServer().getCommands().performPrefixedCommand(commands, "season set " + before);
            helper.assertTrue(plainsWinter == -1 && plainsSummer == 1,
                    "plains: winter a step cooler, summer a step warmer (got " + plainsWinter + ", " + plainsSummer + ")");
            helper.assertTrue(savannaWinter == 0 && savannaSummer == 0, "savanna has wet and dry seasons: no change");
        }
        if (ModList.get().isLoaded("toughasnails")) {
            var drinks = ItemTags.create(SeedToCellar.rl("toughasnails", "drinks"));
            var heating = ItemTags.create(SeedToCellar.rl("toughasnails", "heating_consumed_items"));
            var ale = new ItemStack(Drinks.PALE_ALE.item().get());
            var whiskey = new ItemStack(Drinks.MALT_WHISKEY.item().get());
            helper.assertTrue(ale.is(drinks) && whiskey.is(drinks), "Tough As Nails counts our drinks");
            helper.assertTrue(whiskey.is(heating) && !ale.is(heating), "and whiskey warms you (ale doesn't)");
        }
        helper.succeed();
    }
}
