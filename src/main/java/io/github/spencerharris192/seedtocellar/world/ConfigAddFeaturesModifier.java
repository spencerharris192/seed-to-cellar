package io.github.spencerharris192.seedtocellar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

/**
 * Like Forge's add_features biome modifier, but skipped when its config toggle is off.
 * Lets players and pack makers disable any wild plant without writing a datapack.
 */
public record ConfigAddFeaturesModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                        GenerationStep.Decoration step, String configToggle) implements BiomeModifier {
    public static final Codec<ConfigAddFeaturesModifier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigAddFeaturesModifier::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConfigAddFeaturesModifier::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigAddFeaturesModifier::step),
            Codec.STRING.fieldOf("config_toggle").forGetter(ConfigAddFeaturesModifier::configToggle)
    ).apply(inst, ConfigAddFeaturesModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome) && ModConfigs.worldgenEnabled(configToggle)) {
            features.forEach(feature -> builder.getGenerationSettings().addFeature(step, feature));
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
