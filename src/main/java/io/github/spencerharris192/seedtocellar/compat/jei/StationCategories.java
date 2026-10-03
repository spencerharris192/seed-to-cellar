package io.github.spencerharris192.seedtocellar.compat.jei;

import io.github.spencerharris192.seedtocellar.brewing.MaltType;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.CrushingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.DryingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MixingRecipe;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** One JEI page per station. Each shows its slots, an arrow, and plain-language notes. */
public final class StationCategories {
    private static final String J = "jei.seedtocellar.";

    /**
     * A plant and what it gives (the Growing page): what you plant, what you harvest, how to plant it (a lang key) and the
     * climate it likes.
     */
    public record Growing(ItemStack plant, List<ItemStack> harvest, String hintKey,
                          io.github.spencerharris192.seedtocellar.farming.Climate climate) {}

    public static class GrowingCategory extends SimpleCategory<Growing> {
        public GrowingCategory(IGuiHelper gui) {
            // Room for the longest hint (4 lines: the grapes, agave, elderberry) and the climate line under the slots
            super(gui, JeiTypes.GROWING, J + "growing", io.github.spencerharris192.seedtocellar.farming.Crops.BARLEY.seeds(), 78);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, Growing r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addItemStack(r.plant());
            for (int i = 0; i < r.harvest().size(); i++) {
                // 24 apart, like the other pages' outputs: an output slot's frame is wider than the item it holds
                b.addSlot(RecipeIngredientRole.OUTPUT, 61 + 24 * i, 1).setOutputSlotBackground().addItemStack(r.harvest().get(i));
            }
        }

        @Override
        public void draw(Growing r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int y = 26;   // below the output slots' frames
            for (var line : font.split(Component.translatable(r.hintKey()), WIDTH - 2)) {
                g.drawString(font, line, 1, y, 0x555555, false);
                y += 10;
            }
            lines(g, 1, y, List.of(Component.translatable("tooltip.seedtocellar.climate", Component.translatable(r.climate().translationKey()))));
        }
    }

    public static class Malting extends SimpleCategory<MaltingRecipe> {
        public Malting(IGuiHelper gui) {
            super(gui, JeiTypes.MALTING, "block.seedtocellar.malting_tub", ModItems.MALTING_TUB.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, MaltingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(r.ingredient());
            b.addSlot(RecipeIngredientRole.INPUT, 21, 1).setStandardSlotBackground().addFluidStack(Fluids.WATER, 1000)
                    .setFluidRenderer(1000, false, 16, 16);
            b.addSlot(RecipeIngredientRole.OUTPUT, 71, 1).setOutputSlotBackground().addItemStack(r.result());
        }

        @Override
        public void draw(MaltingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 44, 1);
            lines(g, 1, 24, List.of(Component.translatable(J + "malting.times", seconds(r.steepTime()), seconds(r.sproutTime()))));
        }
    }

    public static class Kilning extends SimpleCategory<KilningRecipe> {
        public Kilning(IGuiHelper gui) {
            super(gui, JeiTypes.KILNING, "block.seedtocellar.kiln", ModItems.KILN.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, KilningRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(r.ingredient());
            b.addSlot(RecipeIngredientRole.OUTPUT, 61, 1).setOutputSlotBackground().addItemStack(r.result());
        }

        @Override
        public void draw(KilningRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            flame.draw(g, 88, 3);
            lines(g, 1, 24, List.of(Component.translatable(J + "kiln.setting", r.roast().displayName(), seconds(r.time()))));
        }
    }

    public static class Milling extends SimpleCategory<MillingRecipe> {
        public Milling(IGuiHelper gui) {
            super(gui, JeiTypes.MILLING, "block.seedtocellar.millstone", ModItems.MILLSTONE.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, MillingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(r.ingredient());
            b.addSlot(RecipeIngredientRole.OUTPUT, 61, 1).setOutputSlotBackground().addItemStack(r.result());
            if (!r.byproduct().isEmpty()) b.addSlot(RecipeIngredientRole.OUTPUT, 85, 1).setStandardSlotBackground().addItemStack(r.byproduct());
        }

        @Override
        public void draw(MillingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            lines(g, 1, 24, List.of(Component.translatable(J + "mill.cranks", r.cranks())));
        }
    }

    /** Kettle cooking: up to four ingredients, a liquid from the tank, a bowl or bottle, one serving. */
    public static class Cooking extends SimpleCategory<CookingRecipe> {
        public Cooking(IGuiHelper gui) {
            super(gui, JeiTypes.COOKING, "jei.seedtocellar.cooking", ModItems.BREW_KETTLE.get(), 52);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, CookingRecipe r, IFocusGroup focuses) {
            List<Ingredient> ingredients = r.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                b.addSlot(RecipeIngredientRole.INPUT, 1 + (i % 2) * 18, 1 + (i / 2) * 18).setStandardSlotBackground()
                        .addIngredients(ingredients.get(i));
            }
            if (r.liquid() != null) {
                b.addSlot(RecipeIngredientRole.INPUT, 43, 10).setStandardSlotBackground()
                        .addIngredients(ForgeTypes.FLUID_STACK, r.liquid().examples())
                        .setFluidRenderer(r.liquid().amount(), false, 16, 16);
            }
            if (r.needsContainer()) {
                b.addSlot(RecipeIngredientRole.INPUT, 97, 1).setStandardSlotBackground().addIngredients(r.container());
            }
            b.addSlot(RecipeIngredientRole.OUTPUT, 97, 19).setOutputSlotBackground().addItemStack(r.result());
        }

        @Override
        public void draw(CookingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 67, 19);
            lines(g, 1, 42, List.of(Component.translatable(J + "cooking.time", seconds(r.time()))));
        }
    }

    public static class Drying extends SimpleCategory<DryingRecipe> {
        public Drying(IGuiHelper gui) {
            super(gui, JeiTypes.DRYING, "block.seedtocellar.drying_rack", ModItems.DRYING_RACK.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, DryingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(r.ingredient());
            b.addSlot(RecipeIngredientRole.OUTPUT, 61, 1).setOutputSlotBackground().addItemStack(r.result());
        }

        @Override
        public void draw(DryingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            lines(g, 1, 24, List.of(Component.translatable(J + "drying.time", seconds(r.time())),
                    Component.translatable(J + "drying.weather")));
        }
    }

    public static class Mixing extends SimpleCategory<MixingRecipe> {
        public Mixing(IGuiHelper gui) {
            super(gui, JeiTypes.MIXING, "block.seedtocellar.brew_kettle", ModItems.BREW_KETTLE.get(), 34);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, MixingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addFluidStack(r.liquid().examples().isEmpty()
                    ? Fluids.WATER : r.liquid().examples().get(0).getFluid(), 1000).setFluidRenderer(1000, false, 16, 16);
            b.addSlot(RecipeIngredientRole.INPUT, 21, 1).setStandardSlotBackground()
                    .addItemStacks(java.util.Arrays.stream(r.ingredient().getItems()).map(s -> s.copyWithCount(r.perBucket())).toList());
            b.addSlot(RecipeIngredientRole.OUTPUT, 71, 1).setOutputSlotBackground().addFluidStack(r.result(), 1000)
                    .setFluidRenderer(1000, false, 16, 16);
        }

        @Override
        public void draw(MixingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 44, 1);
            lines(g, 1, 24, List.of(Component.translatable(r.boilsDown() ? J + "mixing.boil_down" : J + "mixing.per_bucket", r.perBucket())));
        }
    }

    public static class Crushing extends SimpleCategory<CrushingRecipe> {
        public Crushing(IGuiHelper gui) {
            super(gui, JeiTypes.CRUSHING, "block.seedtocellar.crushing_tub", ModItems.CRUSHING_TUB.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, CrushingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(r.ingredient());
            b.addSlot(RecipeIngredientRole.OUTPUT, 61, 1).setOutputSlotBackground()
                    .addFluidStack(r.result().getFluid(), r.result().getAmount()).setFluidRenderer(1000, false, 16, 16);
        }

        @Override
        public void draw(CrushingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            lines(g, 1, 24, List.of(Component.translatable(J + "crushing.stomps", r.stomps()), Component.translatable(J + "crushing.jump")));
        }
    }

    public static class Pressing extends SimpleCategory<PressingRecipe> {
        public Pressing(IGuiHelper gui) {
            super(gui, JeiTypes.PRESSING, "block.seedtocellar.fruit_press", ModItems.FRUIT_PRESS.get(), 34);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, PressingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground()
                    .addItemStacks(java.util.Arrays.stream(r.ingredient().getItems()).map(s -> s.copyWithCount(r.count())).toList());
            b.addSlot(RecipeIngredientRole.OUTPUT, 61, 1).setOutputSlotBackground()
                    .addFluidStack(r.result().getFluid(), r.result().getAmount()).setFluidRenderer(1000, false, 16, 16);
            if (!r.byproduct().isEmpty()) b.addSlot(RecipeIngredientRole.OUTPUT, 85, 1).setOutputSlotBackground().addItemStack(r.byproduct());
        }

        @Override
        public void draw(PressingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 26, 1);
            lines(g, 1, 24, List.of(Component.translatable(J + "pressing.cranks", r.count(), r.cranks())));
        }
    }

    /** The two kettle steps are fixed rules (not recipe files), shown as two entries. */
    public record KettleStep(boolean boil) {}

    public static class Kettle extends SimpleCategory<KettleStep> {
        public Kettle(IGuiHelper gui) {
            super(gui, JeiTypes.KETTLE, "block.seedtocellar.brew_kettle", ModItems.BREW_KETTLE.get(), 54);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, KettleStep step, IFocusGroup focuses) {
            if (!step.boil()) {
                List<ItemStack> grist = new ArrayList<>();
                for (MaltType type : MaltType.values()) grist.addAll(List.of(Ingredient.of(type.gristTag).getItems()));
                b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addItemStacks(grist);
                b.addSlot(RecipeIngredientRole.INPUT, 21, 1).setStandardSlotBackground().addFluidStack(Fluids.WATER, 1000)
                        .setFluidRenderer(1000, false, 16, 16);
                b.addSlot(RecipeIngredientRole.OUTPUT, 71, 1).setOutputSlotBackground()
                        .addFluidStack(ModFluids.SWEET_WORT.get(), 1000)
                        .setFluidRenderer(1000, false, 16, 16);
                b.addSlot(RecipeIngredientRole.OUTPUT, 95, 1).setOutputSlotBackground().addItemStack(new ItemStack(ModItems.SPENT_GRAIN.get()));
            } else {
                b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground()
                        .addFluidStack(ModFluids.SWEET_WORT.get(), 1000)
                        .setFluidRenderer(1000, false, 16, 16);
                b.addSlot(RecipeIngredientRole.INPUT, 21, 1).setStandardSlotBackground().addItemStack(new ItemStack(ModItems.DRIED_HOPS.get()));
                b.addSlot(RecipeIngredientRole.OUTPUT, 71, 1).setOutputSlotBackground()
                        .addFluidStack(ModFluids.HOPPED_WORT.get(), 1000)
                        .setFluidRenderer(1000, false, 16, 16);
            }
        }

        @Override
        public void draw(KettleStep step, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 44, 1);
            if (step.boil()) {
                lines(g, 1, 24, List.of(Component.translatable(J + "kettle.boil"), Component.translatable(J + "kettle.heat")));
            } else {
                lines(g, 1, 24, List.of(Component.translatable(J + "kettle.mash"), Component.translatable(J + "kettle.strength")));
            }
        }
    }

    public static class Fermenting extends SimpleCategory<FermentingRecipe> {
        public Fermenting(IGuiHelper gui) {
            super(gui, JeiTypes.FERMENTING, "block.seedtocellar.fermenting_vat", ModItems.FERMENTING_VAT.get(), 74);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, FermentingRecipe r, IFocusGroup focuses) {
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addFluidStack(r.input(), 1000)
                    .setFluidRenderer(1000, false, 16, 16);
            b.addSlot(RecipeIngredientRole.CATALYST, 21, 1).setStandardSlotBackground().addItemStack(new ItemStack(r.yeast().leesItem()));
            b.addSlot(RecipeIngredientRole.OUTPUT, 71, 1).setOutputSlotBackground().addFluidStack(r.result(), 1000)
                    .setFluidRenderer(1000, false, 16, 16);
        }

        @Override
        public void draw(FermentingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 44, 1);
            List<Component> text = new ArrayList<>();
            if (r.input() == ModFluids.SWEET_WORT.get() || r.input() == ModFluids.HOPPED_WORT.get()) {
                text.add(Component.translatable(J + "ferment.strength", r.minStrength().displayName(), r.maxStrength().displayName()));
            }
            MutableComponent malts = Component.empty();
            boolean any = false;
            for (Map.Entry<MaltType, Float> e : r.maltMin().entrySet()) {
                malts.append(any ? ", " : "").append(Component.translatable(J + "malt." + e.getKey().key))
                        .append(" ≥ " + Math.round(e.getValue() * 100) + "%");
                any = true;
            }
            for (Map.Entry<MaltType, Float> e : r.maltMax().entrySet()) {
                malts.append(any ? ", " : "");
                if (e.getValue() == 0) {
                    malts.append(Component.translatable(J + "malt.none", Component.translatable(J + "malt." + e.getKey().key)));
                } else {
                    malts.append(Component.translatable(J + "malt." + e.getKey().key)).append(" ≤ " + Math.round(e.getValue() * 100) + "%");
                }
                any = true;
            }
            if (any) text.add(malts);
            text.add(Component.translatable(J + "ferment.conditions", r.idealName(), days(r.time())));
            text.add(r.allowWild() ? Component.translatable(J + "ferment.yeast_wild_ok", r.yeast().leesItem().getDescription())
                    : Component.translatable(J + "ferment.yeast_needed"));
            lines(g, 1, 24, text);
        }
    }

    public static class Jar extends SimpleCategory<JarRecipe> {
        public Jar(IGuiHelper gui) {
            super(gui, JeiTypes.JAR, "block.seedtocellar.preserving_jar", ModItems.PRESERVING_JAR.get(), 44);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, JarRecipe r, IFocusGroup focuses) {
            int x = 1;
            for (Ingredient ingredient : r.getIngredients()) {
                b.addSlot(RecipeIngredientRole.INPUT, x, 1).setStandardSlotBackground().addIngredients(ingredient);
                x += 18;
            }
            if (r.liquid() != null) {
                b.addSlot(RecipeIngredientRole.INPUT, x, 1).setStandardSlotBackground()
                        .addIngredients(ForgeTypes.FLUID_STACK, r.liquid().examples().stream()
                                .map(f -> new net.minecraftforge.fluids.FluidStack(f.getFluid(), Math.max(250, r.fluidAmount()))).toList())
                        .setFluidRenderer(1000, false, 16, 16);
                x += 18;
            }
            var out = b.addSlot(RecipeIngredientRole.OUTPUT, x + 30, 1).setOutputSlotBackground();
            if (r.resultFluid() != null) out.addFluidStack(r.resultFluid(), 1000).setFluidRenderer(1000, false, 16, 16);
            else out.addItemStack(r.result());
        }

        @Override
        public void draw(JarRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            int inputs = r.getIngredients().size() + (r.liquid() != null ? 1 : 0);
            arrow.draw(g, 1 + inputs * 18 + 3, 1);
            List<Component> text = new ArrayList<>();
            text.add(Component.translatable(J + "jar.time", days(r.time())));
            if (!r.temperatures().isEmpty()) text.add(Component.translatable(J + "jar.needs", r.temperatures().get(0).displayName()));
            if (r.resultFluid() != null) text.add(Component.translatable(J + "jar.steeps"));
            lines(g, 1, 24, text);
        }
    }

    /** Pot Still: the pot's liquid, the filter (and the basket's botanicals) if needed, and the spirit it makes. */
    public static class Distilling extends SimpleCategory<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe> {
        public Distilling(IGuiHelper gui) {
            super(gui, JeiTypes.DISTILLING, "block.seedtocellar.pot_still", ModItems.POT_STILL.get(), 64);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder b, io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe r, IFocusGroup focuses) {
            List<net.minecraftforge.fluids.FluidStack> inputs = r.input().examples().stream()
                    .map(f -> new net.minecraftforge.fluids.FluidStack(f.getFluid(), 1000)).toList();
            b.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().addIngredients(ForgeTypes.FLUID_STACK, inputs)
                    .setFluidRenderer(1000, false, 16, 16);
            int x = 21;
            if (r.filter()) {
                b.addSlot(RecipeIngredientRole.CATALYST, x, 1).setStandardSlotBackground()
                        .addIngredients(Ingredient.of(io.github.spencerharris192.seedtocellar.registry.ModTags.Items.FILTER_CHARCOAL));
                x += 18;
            }
            if (r.basket() != null) {
                b.addSlot(RecipeIngredientRole.INPUT, x, 1).setStandardSlotBackground().addIngredients(r.basket().required());
                x += 18;
            }
            var out = b.addSlot(RecipeIngredientRole.OUTPUT, 91, 1).setOutputSlotBackground().setFluidRenderer(1000, false, 16, 16);
            if (r.result() != null) out.addFluidStack(r.result(), 500);
            else out.addIngredients(ForgeTypes.FLUID_STACK, inputs.stream().map(f -> new net.minecraftforge.fluids.FluidStack(f.getFluid(), 500)).toList());
            // the other half of the pot stays behind as stillage
            b.addSlot(RecipeIngredientRole.OUTPUT, 115, 1).setOutputSlotBackground().addFluidStack(ModFluids.STILLAGE.get(), 500)
                    .setFluidRenderer(1000, false, 16, 16);
        }

        @Override
        public void draw(io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
            arrow.draw(g, 64, 1);
            List<Component> text = new ArrayList<>();
            text.add(Component.translatable(J + (r.result() == null ? "distilling.again" : "distilling.run")));
            if (r.minRuns() > 0) text.add(Component.translatable(J + "distilling.min_runs", r.minRuns()));
            if (r.filter()) text.add(Component.translatable(J + "distilling.filter"));
            if (r.basket() != null) {
                text.add(Component.translatable(J + "distilling.basket", r.basket().min() - 1));
                text.add(Component.translatable(J + "distilling.basket_star", io.github.spencerharris192.seedtocellar.brewing.CraftStep.GIN_BOTANICALS));
            }
            text.add(Component.translatable(J + "distilling.heat"));
            lines(g, 1, 22, text);
        }
    }

    private StationCategories() {}
}
