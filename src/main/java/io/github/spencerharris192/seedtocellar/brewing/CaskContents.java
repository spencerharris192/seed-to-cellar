package io.github.spencerharris192.seedtocellar.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * What a cask or keg holds, carried by its item when it's broken full: the liquid and how long it had rested
 * (the {@code seedtocellar:cask_contents} component; the block's loot copies it from the cask).
 */
public record CaskContents(SimpleFluidContent fluid, long ageTicks) {
    public static final Codec<CaskContents> CODEC = RecordCodecBuilder.create(i -> i.group(
            SimpleFluidContent.CODEC.fieldOf("fluid").forGetter(CaskContents::fluid),
            Codec.LONG.optionalFieldOf("age_ticks", 0L).forGetter(CaskContents::ageTicks)
    ).apply(i, CaskContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CaskContents> STREAM_CODEC = StreamCodec.composite(
            SimpleFluidContent.STREAM_CODEC, CaskContents::fluid,
            ByteBufCodecs.VAR_LONG.<RegistryFriendlyByteBuf>cast(), CaskContents::ageTicks,
            CaskContents::new);
}
