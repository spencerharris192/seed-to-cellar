package io.github.spencerharris192.seedtocellar.compat.top;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.HydrometerItem;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

/**
 * The One Probe (GDD section 22.2): looking at a station, cask or crop of ours shows what the Hydrometer would read
 * (progress, temperature, quality, age; growth, climate and fertility). Only loaded when The One Probe is installed.
 */
public final class TopCompat implements Function<ITheOneProbe, Void> {
    public static void register() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TopCompat::new);
    }

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(new Readout());
        return null;
    }

    private static final class Readout implements IProbeInfoProvider {
        @Override
        public ResourceLocation getID() {
            return SeedToCellar.id("readout");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state, IProbeHitData data) {
            if (!io.github.spencerharris192.seedtocellar.config.ModConfigs.COMMON.theOneProbe.get()) return;
            // Ours only: The One Probe already describes vanilla's crops (the Hydrometer reads those too).
            ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (id == null || !id.getNamespace().equals(SeedToCellar.MOD_ID)) return;
            for (Component line : HydrometerItem.readout(level, data.getPos(), state)) info.text(line);
        }
    }
}
