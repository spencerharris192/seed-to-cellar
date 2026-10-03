package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.recipe.CookingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.CrushingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.DryingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.FermentingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.JarRecipe;
import io.github.spencerharris192.seedtocellar.recipe.KilningRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MaltingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MillingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.MixingRecipe;
import io.github.spencerharris192.seedtocellar.recipe.PressingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Data-driven recipe types: pack makers can add or change them with datapacks, KubeJS or CraftTweaker. */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, SeedToCellar.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SeedToCellar.MOD_ID);

    public static final RegistryObject<RecipeType<MaltingRecipe>> MALTING = type("malting");
    public static final RegistryObject<RecipeSerializer<MaltingRecipe>> MALTING_SERIALIZER = SERIALIZERS.register("malting", MaltingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<KilningRecipe>> KILNING = type("kilning");
    public static final RegistryObject<RecipeSerializer<KilningRecipe>> KILNING_SERIALIZER = SERIALIZERS.register("kilning", KilningRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<MillingRecipe>> MILLING = type("milling");
    public static final RegistryObject<RecipeSerializer<MillingRecipe>> MILLING_SERIALIZER = SERIALIZERS.register("milling", MillingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<FermentingRecipe>> FERMENTING = type("fermenting");
    public static final RegistryObject<RecipeSerializer<FermentingRecipe>> FERMENTING_SERIALIZER = SERIALIZERS.register("fermenting", FermentingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<JarRecipe>> JAR = type("jar");
    public static final RegistryObject<RecipeSerializer<JarRecipe>> JAR_SERIALIZER = SERIALIZERS.register("jar", JarRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<DryingRecipe>> DRYING = type("drying");
    public static final RegistryObject<RecipeType<CookingRecipe>> COOKING = type("cooking");
    public static final RegistryObject<RecipeSerializer<CookingRecipe>> COOKING_SERIALIZER = SERIALIZERS.register("cooking", CookingRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = SERIALIZERS.register("drying", DryingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<MixingRecipe>> MIXING = type("mixing");
    public static final RegistryObject<RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER = SERIALIZERS.register("mixing", MixingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<CrushingRecipe>> CRUSHING = type("crushing");
    public static final RegistryObject<RecipeSerializer<CrushingRecipe>> CRUSHING_SERIALIZER = SERIALIZERS.register("crushing", CrushingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<PressingRecipe>> PRESSING = type("pressing");
    public static final RegistryObject<RecipeSerializer<PressingRecipe>> PRESSING_SERIALIZER = SERIALIZERS.register("pressing", PressingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe>> DISTILLING = type("distilling");
    public static final RegistryObject<RecipeSerializer<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe>> DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe.Serializer::new);

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> type(String name) {
        return TYPES.register(name, () -> RecipeType.simple(SeedToCellar.id(name)));
    }

    private ModRecipes() {}
}
