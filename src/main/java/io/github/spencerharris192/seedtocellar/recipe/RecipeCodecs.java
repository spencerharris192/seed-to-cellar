package io.github.spencerharris192.seedtocellar.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import java.util.Optional;
import java.util.function.Function;

/** Shared pieces of our recipe JSON (and their network forms), so every recipe type reads the same way. */
public final class RecipeCodecs {
    /** A fluid by ID ("seedtocellar:apple_juice"); the empty fluid isn't one. */
    public static final Codec<Fluid> FLUID = BuiltInRegistries.FLUID.byNameCodec()
            .validate(f -> f == Fluids.EMPTY ? DataResult.error(() -> "A recipe needs a real fluid, not empty") : DataResult.success(f));
    public static final StreamCodec<RegistryFriendlyByteBuf, Fluid> FLUID_STREAM = ByteBufCodecs.registry(Registries.FLUID);

    /** A liquid a station makes: {"fluid":"seedtocellar:apple_juice","amount":500}. */
    public static final Codec<FluidStackTemplate> FLUID_RESULT = RecordCodecBuilder.create(i -> i.group(
            FLUID.fieldOf("fluid").forGetter(t -> t.fluid().value()),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(FluidStackTemplate::amount)
    ).apply(i, FluidStackTemplate::new));

    /** A name in recipe JSON for one of our enums (roast settings, yeasts, temperatures...). */
    public static <E extends Enum<E>> Codec<E> named(Function<String, E> byName, Function<E, String> name) {
        return Codec.STRING.comapFlatMap(s -> {
            E value;
            try {
                value = byName.apply(s);
            } catch (IllegalArgumentException e) {
                value = null;
            }
            return value != null && name.apply(value).equals(s) ? DataResult.success(value) : DataResult.error(() -> "Unknown value " + s);
        }, name);
    }

    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> ordinal(Class<E> type) {
        E[] values = type.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map(i -> values[i], Enum::ordinal);
    }

    /** An optional item result: empty when the template is absent. */
    public static ItemStack create(Optional<ItemStackTemplate> template) {
        return template.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    private RecipeCodecs() {}
}
