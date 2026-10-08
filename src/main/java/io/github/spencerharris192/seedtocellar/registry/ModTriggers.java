package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Advancement triggers for things that happen in the world rather than in an inventory. IDs are permanent. */
public final class ModTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, SeedToCellar.MOD_ID);

    /** A player stomped fruit in a Crushing Tub (the Stomped advancement). */
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> STOMP = TRIGGERS.register("stomp", PlayerTrigger::new);

    /** A player poured a spirit aged 10 years or more from a cask (The Angel's Share). */
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> ANGELS_SHARE = TRIGGERS.register("angels_share", PlayerTrigger::new);
    public static final int ANGELS_SHARE_YEARS = 10;

    /** A player's hangover was cured: water, milk, a hearty breakfast or bitters (Morning After). */
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> MORNING_AFTER = TRIGGERS.register("morning_after", PlayerTrigger::new);

    /** A player filled a Bottle Shelf with six different drinks (Tavern Keeper). */
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> TAVERN_KEEPER = TRIGGERS.register("tavern_keeper", PlayerTrigger::new);

    private ModTriggers() {}
}
