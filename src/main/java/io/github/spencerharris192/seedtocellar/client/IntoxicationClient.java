package io.github.spencerharris192.seedtocellar.client;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The view sways when Tipsy or worse. Strength follows the stage, vanilla's Distortion Effects
 * accessibility slider, and our client swayIntensity option (either at 0 = no sway at all).
 */
@Mod.EventBusSubscriber(modid = SeedToCellar.MOD_ID, value = Dist.CLIENT)
public final class IntoxicationClient {
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        MobEffectInstance tipsy = player.getEffect(ModEffects.TIPSY.get());
        if (tipsy == null || tipsy.getAmplifier() < 1) return; // Merry: no sway
        double scale = mc.options.screenEffectScale().get() * ModConfigs.CLIENT.swayIntensity.get();
        if (scale <= 0) return;
        float strength = (float) (tipsy.getAmplifier() * scale);
        float t = (float) (player.tickCount + event.getPartialTick());
        event.setRoll(event.getRoll() + Mth.sin(t * 0.07F) * strength * 1.5F);
        event.setYaw(event.getYaw() + Mth.sin(t * 0.045F) * strength * 0.8F);
        event.setPitch(event.getPitch() + Mth.sin(t * 0.06F + 1.3F) * strength * 0.4F);
    }

    private IntoxicationClient() {}
}
