package io.github.spencerharris192.seedtocellar.recipe;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Our recipes by type. The server has them all; players' games get ours sent too (vanilla only sends recipe-book recipes),
 * so JEI and the stations' read-outs work in multiplayer.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class Recipes {
    private static volatile RecipeMap clientRecipes = RecipeMap.EMPTY;

    public static <I extends RecipeInput, T extends Recipe<I>> Stream<T> stream(@Nullable Level level, RecipeType<T> type) {
        return holders(level, type).map(RecipeHolder::value);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> holders(@Nullable Level level, RecipeType<T> type) {
        RecipeMap map = level instanceof ServerLevel server ? server.recipeAccess().recipeMap() : clientRecipes;
        return map.byType(type).stream();
    }

    /** The first recipe of this type that takes the input. */
    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> find(@Nullable Level level, RecipeType<T> type, I input) {
        if (level == null) return Optional.empty();
        return holders(level, type).filter(h -> h.value().matches(input, level)).findFirst();
    }

    /** A recipe by its ID (stations remember the one they're working through), if it's still there and of this kind. */
    public static <T extends Recipe<?>> Optional<T> byId(@Nullable Level level, @Nullable Identifier id, Class<T> kind) {
        if (id == null) return Optional.empty();
        RecipeMap map = level instanceof ServerLevel server ? server.recipeAccess().recipeMap() : clientRecipes;
        RecipeHolder<?> holder = map.byKey(ResourceKey.create(Registries.RECIPE, id));
        return holder != null && kind.isInstance(holder.value()) ? Optional.of(kind.cast(holder.value())) : Optional.empty();
    }

    @SubscribeEvent
    public static void send(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.all());
    }

    /** The recipes a player's game received (client code calls this when they arrive). */
    public static void received(RecipeMap recipes) {
        clientRecipes = recipes;
    }

    private Recipes() {}
}
