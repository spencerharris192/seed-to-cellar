package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.registry.ModRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Brew Kettle cooking (GDD sections 7 and 15): soups, stews, porridge, jams and the like. One
 * serving per batch, over heat: up to 4 ingredients (in any of the kettle's ingredient slots, any
 * order; nothing else may be in them), an optional liquid from the tank, and an optional
 * container (a bowl or bottle) from the container slot.
 * <pre>
 * {"type":"seedtocellar:cooking",
 *  "ingredients":[..., ...],
 *  "fluid":{"fluid":"minecraft:water" | "tag":"c:milk", "amount":250},   (optional)
 *  "container":"minecraft:bowl",                                         (optional)
 *  "result":{"id":"...", "count":1},
 *  "time":200}
 * </pre>
 */
public class CookingRecipe implements StationRecipe {
    /** A liquid the recipe needs from a tank: one fluid, or any fluid in a tag; JSON {"fluid" or "tag", "amount"}. */
    public record Liquid(@Nullable Fluid fluid, @Nullable TagKey<Fluid> tag, int amount) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Liquid> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(RecipeCodecs.FLUID_STREAM), l -> Optional.ofNullable(l.fluid),
                ByteBufCodecs.optional(TagKey.streamCodec(Registries.FLUID)).<RegistryFriendlyByteBuf>cast(), l -> Optional.ofNullable(l.tag),
                ByteBufCodecs.VAR_INT, Liquid::amount,
                (fluid, tag, amount) -> new Liquid(fluid.orElse(null), tag.orElse(null), amount));

        /** The JSON form, with this amount when "amount" is left out. */
        public static MapCodec<Liquid> codec(int defaultAmount) {
            return RecordCodecBuilder.<Liquid>mapCodec(i -> i.group(
                    RecipeCodecs.FLUID.optionalFieldOf("fluid").forGetter(l -> Optional.ofNullable(l.fluid)),
                    TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(l -> Optional.ofNullable(l.tag)),
                    Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("amount", defaultAmount).forGetter(Liquid::amount)
            ).apply(i, (fluid, tag, amount) -> new Liquid(fluid.orElse(null), tag.orElse(null), amount))).validate(l ->
                    (l.fluid == null) == (l.tag == null) ? DataResult.error(() -> "A liquid needs a \"fluid\" or a \"tag\" (not both)") : DataResult.success(l));
        }

        public boolean test(FluidStack stack) {
            if (stack.isEmpty() || stack.getAmount() < amount) return false;
            return fluid != null ? stack.getFluid().isSame(fluid) : stack.getFluid().is(tag);
        }

        /** Every fluid this accepts (for JEI). */
        public List<FluidStack> examples() {
            List<FluidStack> list = new ArrayList<>();
            if (fluid != null) {
                list.add(new FluidStack(fluid, amount));
            } else {
                for (Fluid f : BuiltInRegistries.FLUID) {
                    if (f.is(tag) && f.isSource(f.defaultFluidState())) list.add(new FluidStack(f, amount));
                }
            }
            return list;
        }
    }

    public static final int MAX_INGREDIENTS = 4;

    public static final MapCodec<CookingRecipe> MAP_CODEC = RecordCodecBuilder.<CookingRecipe>mapCodec(i -> i.group(
            Ingredient.CODEC.listOf(0, MAX_INGREDIENTS).optionalFieldOf("ingredients", List.of()).forGetter(r -> r.ingredients),
            Liquid.codec(250).codec().optionalFieldOf("fluid").forGetter(r -> Optional.ofNullable(r.liquid)),
            Ingredient.CODEC.optionalFieldOf("container").forGetter(r -> r.container),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("time", 200).forGetter(r -> r.time)
    ).apply(i, (ingredients, liquid, container, result, time) -> new CookingRecipe(ingredients, liquid.orElse(null), container, result, time)))
            .validate(r -> r.ingredients.isEmpty() && r.liquid == null
                    ? DataResult.error(() -> "A cooking recipe needs ingredients, a liquid, or both") : DataResult.success(r));
    public static final StreamCodec<RegistryFriendlyByteBuf, CookingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INGREDIENTS)), r -> r.ingredients,
            ByteBufCodecs.optional(Liquid.STREAM_CODEC), r -> Optional.ofNullable(r.liquid),
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, r -> r.container,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.VAR_INT, r -> r.time,
            (ingredients, liquid, container, result, time) -> new CookingRecipe(ingredients, liquid.orElse(null), container, result, time));

    private final List<Ingredient> ingredients;
    private final @Nullable Liquid liquid;
    private final Optional<Ingredient> container;
    private final ItemStackTemplate result;
    private final int time;

    public CookingRecipe(List<Ingredient> ingredients, @Nullable Liquid liquid, Optional<Ingredient> container, ItemStackTemplate result,
                         int time) {
        this.ingredients = List.copyOf(ingredients);
        this.liquid = liquid;
        this.container = container;
        this.result = result;
        this.time = time;
    }

    public @Nullable Liquid liquid() {
        return liquid;
    }

    /** The container it's served in (a bowl, a bottle), if any. */
    public Optional<Ingredient> container() {
        return container;
    }

    public boolean needsContainer() {
        return container.isPresent();
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    /** A new stack of the result. */
    public ItemStack result() {
        return result.create();
    }

    /** Ticks per serving before the processing-time multiplier. */
    public int time() {
        return time;
    }

    /** True if the non-empty stacks are exactly this recipe's ingredients, one each, in any order. */
    public boolean matchesItems(List<ItemStack> stacks) {
        List<ItemStack> present = stacks.stream().filter(s -> !s.isEmpty()).toList();
        return present.size() == ingredients.size() && RecipeMatcher.findMatches(present, ingredients) != null;
    }

    @Override
    public RecipeSerializer<CookingRecipe> getSerializer() {
        return ModRecipes.COOKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CookingRecipe> getType() {
        return ModRecipes.COOKING.get();
    }
}
