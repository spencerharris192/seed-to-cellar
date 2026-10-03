package io.github.spencerharris192.seedtocellar.effect;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.WeakHashMap;

/** Drink effects (GDD section 14). Small and flavorful; tipsiness and hangover are toggleable in config. */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SeedToCellar.MOD_ID);

    /** Hunger drains 25% slower. */
    public static final RegistryObject<MobEffect> REFRESHED = EFFECTS.register("refreshed", Refreshed::new);
    /** No freezing in powder snow. */
    public static final RegistryObject<MobEffect> WARMTH = EFFECTS.register("warmth", Warmth::new);
    /** +20% knockback resistance. */
    public static final RegistryObject<MobEffect> COURAGE = EFFECTS.register("courage", () ->
            new Plain(MobEffectCategory.BENEFICIAL, 0xC8702A).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                    "7b0f3a52-2c1e-4f0e-9a51-2f7d3c1b8e11", 0.2, AttributeModifier.Operation.ADDITION));
    /** Intoxication stage (level I Merry .. IV Smashed). Driven by Intoxication; milk clears it. */
    public static final RegistryObject<MobEffect> TIPSY = EFFECTS.register("tipsy", () -> new Plain(MobEffectCategory.NEUTRAL, 0xE0A33A));
    /** After a heavy night: mining is 30% slower. Water, milk or bitters cure it. */
    public static final RegistryObject<MobEffect> HANGOVER = EFFECTS.register("hangover", () -> new Plain(MobEffectCategory.HARMFUL, 0x8A8F6A));

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
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity.getTicksFrozen() > 0) entity.setTicksFrozen(0);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
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
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;
            long tick = player.level().getGameTime();
            float now = player.getFoodData().getExhaustionLevel();
            Seen last = seen.get(player);
            if (last != null && last.tick() == tick - 1 && now > last.exhaustion()) {
                player.getFoodData().addExhaustion(-(now - last.exhaustion()) * 0.25F);
                now = player.getFoodData().getExhaustionLevel();
            }
            seen.put(player, new Seen(now, tick));
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    }

    private ModEffects() {}
}
