package io.github.spencerharris192.seedtocellar.compat.jei;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.CrushingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.DryingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MixingRecipe;
import mezz.jei.api.recipe.RecipeType;

public final class JeiTypes {
    public static final RecipeType<MaltingRecipe> MALTING = RecipeType.create(SeedToCellar.MOD_ID, "malting", MaltingRecipe.class);
    public static final RecipeType<KilningRecipe> KILNING = RecipeType.create(SeedToCellar.MOD_ID, "kilning", KilningRecipe.class);
    public static final RecipeType<MillingRecipe> MILLING = RecipeType.create(SeedToCellar.MOD_ID, "milling", MillingRecipe.class);
    public static final RecipeType<StationCategories.KettleStep> KETTLE = RecipeType.create(SeedToCellar.MOD_ID, "kettle", StationCategories.KettleStep.class);
    public static final RecipeType<FermentingRecipe> FERMENTING = RecipeType.create(SeedToCellar.MOD_ID, "fermenting", FermentingRecipe.class);
    public static final RecipeType<JarRecipe> JAR = RecipeType.create(SeedToCellar.MOD_ID, "jar", JarRecipe.class);
    public static final RecipeType<DryingRecipe> DRYING = RecipeType.create(SeedToCellar.MOD_ID, "drying", DryingRecipe.class);
    public static final RecipeType<CookingRecipe> COOKING = RecipeType.create(SeedToCellar.MOD_ID, "cooking", CookingRecipe.class);
    public static final RecipeType<MixingRecipe> MIXING = RecipeType.create(SeedToCellar.MOD_ID, "mixing", MixingRecipe.class);
    public static final RecipeType<CrushingRecipe> CRUSHING = RecipeType.create(SeedToCellar.MOD_ID, "crushing", CrushingRecipe.class);
    public static final RecipeType<PressingRecipe> PRESSING = RecipeType.create(SeedToCellar.MOD_ID, "pressing", PressingRecipe.class);
    public static final RecipeType<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe> DISTILLING =
            RecipeType.create(SeedToCellar.MOD_ID, "distilling", io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe.class);

    public static final RecipeType<StationCategories.Growing> GROWING = RecipeType.create(SeedToCellar.MOD_ID, "growing", StationCategories.Growing.class);

    private JeiTypes() {}
}
