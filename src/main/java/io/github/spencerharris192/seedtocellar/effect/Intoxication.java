package io.github.spencerharris192.seedtocellar.effect;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.config.ModConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Tipsiness (GDD section 14). Each drink adds units to a meter kept on the player; one unit
 * wears off every {@code secondsPerUnit}. The Tipsy effect shows the stage and lets milk clear it:
 * <pre>
 *   1-3 Merry   : positive effects only
 *   4-6 Tipsy   : gentle view sway (client)
 *   7-10 Drunk  : stronger sway, occasional stumble while sprinting, hunger drains faster
 *   11+ Smashed : heavy sway, Slowness I, Mining Fatigue I
 * </pre>
 * Sobering up after 7+ units gives a Hangover (2-5 min, longer after low-star drinks).
 * Water, milk, a hearty breakfast or bitters cure it (Morning After). Smashed, you hiccup now and then.
 * Everything here is switchable in the server config.
 */
@EventBusSubscriber(modid = SeedToCellar.MOD_ID)
public final class Intoxication {
    private static final String KEY = SeedToCellar.MOD_ID + "_intoxication";

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) root.put(KEY, new CompoundTag());
        return root.getCompoundOrEmpty(KEY);
    }

    public static float units(Player player) {
        return data(player).getFloatOr("Units", 0F);
    }

    /** Stage 0-3 for a unit count, or -1 when sober. */
    public static int stage(float units) {
        if (units <= 0) return -1;
        if (units < 4) return 0;
        if (units < 7) return 1;
        if (units < 11) return 2;
        return 3;
    }

    /** Called when a player finishes a drink. */
    public static void drink(Player player, float units, int stars) {
        if (!ModConfigs.SERVER.intoxication.get() || units <= 0) return;
        float added = units * ModConfigs.SERVER.intoxicationIntensity.get().floatValue();
        CompoundTag data = data(player);
        float total = data.getFloatOr("Units", 0F) + added;
        data.putFloat("Units", total);
        data.putFloat("Peak", Math.max(data.getFloatOr("Peak", 0F), total));
        data.putFloat("Rough", data.getFloatOr("Rough", 0F) + added * Math.max(0, 5 - stars) / 4F); // low-star drinks hurt more tomorrow
        refreshEffect(player, total);
    }

    /** True while we swap the Tipsy effect ourselves, so it isn't mistaken for milk. */
    private static boolean adjusting;

    /** Shows the current stage. Vanilla won't lower an effect's level in place, so swap it when sobering. */
    private static void refreshEffect(Player player, float units) {
        int stage = stage(units);
        MobEffectInstance current = player.getEffect(ModEffects.TIPSY);
        if (current != null && (stage < 0 || current.getAmplifier() > stage)) {
            adjusting = true;
            player.removeEffect(ModEffects.TIPSY);
            adjusting = false;
        }
        if (stage < 0) return;
        int ticks = Math.round(units * ModConfigs.SERVER.secondsPerUnit.get() * 20) + 20;
        player.addEffect(new MobEffectInstance(ModEffects.TIPSY, ticks, stage, false, false, true));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) return;
        CompoundTag data = player.getPersistentData().getCompoundOrEmpty(KEY);
        float units = data.getFloatOr("Units", 0F);
        if (units <= 0) return;

        int before = stage(units);
        units = Math.max(0, units - 1F / ModConfigs.SERVER.secondsPerUnit.get());
        data.putFloat("Units", units);
        int stage = stage(units);
        if (stage != before) refreshEffect(player, units);
        if (stage >= 2) {
            player.causeFoodExhaustion(0.1F);
            if (player.isSprinting() && player.getRandom().nextInt(4) == 0) {
                double angle = player.getRandom().nextDouble() * Math.PI * 2;
                player.push(Math.cos(angle) * 0.15, 0, Math.sin(angle) * 0.15); // a small stumble
                player.needsSync = true;
            }
        }
        if (stage >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, false, false, false));
            if (ModConfigs.SERVER.hiccups.get() && player.getRandom().nextInt(HICCUP_ODDS) == 0) hiccup(player);
        }
        if (units <= 0) sobered(player, data);
    }

    /** One in this many seconds, a smashed player hiccups. */
    private static final int HICCUP_ODDS = 6;

    /** "Hic!": a little sound everyone nearby hears, and a little hop. */
    private static void hiccup(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.PLAYER_BURP,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.4F, 1.8F + player.getRandom().nextFloat() * 0.3F);
        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.18, 0));
            player.needsSync = true;
        }
    }

    private static void sobered(Player player, CompoundTag data) {
        if (ModConfigs.SERVER.hangovers.get() && data.getFloatOr("Peak", 0F) >= 7) {
            int seconds = Mth.clamp(120 + Math.round(20 * data.getFloatOr("Rough", 0F)), 120, 300);
            player.addEffect(new MobEffectInstance(ModEffects.HANGOVER, seconds * 20, 0));
        }
        data.putFloat("Peak", 0);
        data.putFloat("Rough", 0);
    }

    /** Milk (or anything that removes Tipsy) sobers you up instantly; curing a hangover earns Morning After. */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        // (NeoForge asks about removing effects the player doesn't have, too: only a hangover actually there counts.)
        if (event.getEffect().value() == ModEffects.HANGOVER.get() && event.getEffectInstance() != null
                && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            io.github.spencerharris192.seedtocellar.registry.ModTriggers.MORNING_AFTER.get().trigger(player);
        }
        if (!adjusting && event.getEffect().value() == ModEffects.TIPSY.get() && event.getEntity() instanceof Player player) {
            CompoundTag data = data(player);
            data.putFloat("Units", 0);
            data.putFloat("Peak", 0);
            data.putFloat("Rough", 0);
        }
    }

    /** Hangover: mining is 30% slower. */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(ModEffects.HANGOVER)) {
            event.setNewSpeed(event.getNewSpeed() * 0.7F);
        }
    }

    /** A bottle of water cures a hangover. */
    @SubscribeEvent
    public static void onFinishUsing(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().is(Items.POTION) && event.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            event.getEntity().removeEffect(ModEffects.HANGOVER);
        }
    }

    private Intoxication() {}
}
