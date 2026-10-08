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
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Data-driven recipe types: pack makers can add or change them with datapacks, KubeJS or CraftTweaker. */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, SeedToCellar.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, SeedToCellar.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MaltingRecipe>> MALTING = type("malting");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MaltingRecipe>> MALTING_SERIALIZER = SERIALIZERS.register("malting", () -> new RecipeSerializer<>(MaltingRecipe.MAP_CODEC, MaltingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<KilningRecipe>> KILNING = type("kilning");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KilningRecipe>> KILNING_SERIALIZER = SERIALIZERS.register("kilning", () -> new RecipeSerializer<>(KilningRecipe.MAP_CODEC, KilningRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<MillingRecipe>> MILLING = type("milling");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MillingRecipe>> MILLING_SERIALIZER = SERIALIZERS.register("milling", () -> new RecipeSerializer<>(MillingRecipe.MAP_CODEC, MillingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<FermentingRecipe>> FERMENTING = type("fermenting");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FermentingRecipe>> FERMENTING_SERIALIZER = SERIALIZERS.register("fermenting", () -> new RecipeSerializer<>(FermentingRecipe.MAP_CODEC, FermentingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<JarRecipe>> JAR = type("jar");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JarRecipe>> JAR_SERIALIZER = SERIALIZERS.register("jar", () -> new RecipeSerializer<>(JarRecipe.MAP_CODEC, JarRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING = type("drying");
    public static final DeferredHolder<RecipeType<?>, RecipeType<CookingRecipe>> COOKING = type("cooking");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CookingRecipe>> COOKING_SERIALIZER = SERIALIZERS.register("cooking", () -> new RecipeSerializer<>(CookingRecipe.MAP_CODEC, CookingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = SERIALIZERS.register("drying", () -> new RecipeSerializer<>(DryingRecipe.MAP_CODEC, DryingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<MixingRecipe>> MIXING = type("mixing");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER = SERIALIZERS.register("mixing", () -> new RecipeSerializer<>(MixingRecipe.MAP_CODEC, MixingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrushingRecipe>> CRUSHING = type("crushing");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrushingRecipe>> CRUSHING_SERIALIZER = SERIALIZERS.register("crushing", () -> new RecipeSerializer<>(CrushingRecipe.MAP_CODEC, CrushingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeType<?>, RecipeType<PressingRecipe>> PRESSING = type("pressing");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressingRecipe>> PRESSING_SERIALIZER = SERIALIZERS.register("pressing", () -> new RecipeSerializer<>(PressingRecipe.MAP_CODEC, PressingRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe>> DISTILLING = type("distilling");
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe>> DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", () -> new RecipeSerializer<>(io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe.MAP_CODEC, io.github.spencerharris192.seedtocellar.recipe.DistillingRecipe.STREAM_CODEC));

    /** Every recipe type of ours (clients get them all, for JEI and the stations' read-outs). */
    public static java.util.List<RecipeType<?>> all() {
        return TYPES.getEntries().stream().<RecipeType<?>>map(DeferredHolder::get).toList();
    }

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
        return TYPES.register(name, () -> RecipeType.simple(SeedToCellar.id(name)));
    }

    private ModRecipes() {}
}
