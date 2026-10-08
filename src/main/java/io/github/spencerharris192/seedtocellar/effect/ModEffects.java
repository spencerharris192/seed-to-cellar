package io.github.spencerharris192.seedtocellar.effect;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Map;
import java.util.WeakHashMap;

/** Drink effects (GDD section 14). Small and flavorful; tipsiness and hangover are toggleable in config. */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, SeedToCellar.MOD_ID);

    /** Hunger drains 25% slower. */
    public static final DeferredHolder<MobEffect, MobEffect> REFRESHED = EFFECTS.register("refreshed", Refreshed::new);
    /** No freezing in powder snow. */
    public static final DeferredHolder<MobEffect, MobEffect> WARMTH = EFFECTS.register("warmth", Warmth::new);
    /** +20% knockback resistance. */
    public static final DeferredHolder<MobEffect, MobEffect> COURAGE = EFFECTS.register("courage", () ->
            new Plain(MobEffectCategory.BENEFICIAL, 0xC8702A).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                    SeedToCellar.id("effect.courage"), 0.2, AttributeModifier.Operation.ADD_VALUE));
    /** Intoxication stage (level I Merry .. IV Smashed). Driven by Intoxication; milk clears it. */
    public static final DeferredHolder<MobEffect, MobEffect> TIPSY = EFFECTS.register("tipsy", () -> new Plain(MobEffectCategory.NEUTRAL, 0xE0A33A));
    /** After a heavy night: mining is 30% slower. Water, milk or bitters cure it. */
    public static final DeferredHolder<MobEffect, MobEffect> HANGOVER = EFFECTS.register("hangover", () -> new Plain(MobEffectCategory.HARMFUL, 0x8A8F6A));

    static class Plain extends MobEffect {
        Plain(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    static class Warmth extends MobEffect {
        Warmth() {
            super(MobEffectCategory.BENEFICIAL, 0xE85A2A);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (entity.getTicksFrozen() > 0) entity.setTicksFrozen(0);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
    }

    static class Refreshed extends MobEffect {
        /** Each refreshed player's exhaustion as of the tick it was last seen. */
        private record Seen(float exhaustion, long tick) {}

        private final Map<Player, Seen> seen = new WeakHashMap<>();

        Refreshed() {
            super(MobEffectCategory.BENEFICIAL, 0x9BD36A);
        }

        /** Gives back a quarter of the exhaustion spent since the tick before (never what was spent before the effect). */
        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player)) return true;
            long tick = level.getGameTime();
            float now = player.getFoodData().exhaustionLevel;   // public through our access transformer
            Seen last = seen.get(player);
            if (last != null && last.tick() == tick - 1 && now > last.exhaustion()) {
                player.getFoodData().addExhaustion(-(now - last.exhaustion()) * 0.25F);
                now = player.getFoodData().exhaustionLevel;
            }
            seen.put(player, new Seen(now, tick));
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
    }

    private ModEffects() {}
}
