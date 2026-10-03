package io.github.spencerharris192.seedtocellar.registry;

import io.github.spencerharris192.seedtocellar.SeedToCellar;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;

/** Advancement triggers for things that happen in the world rather than in an inventory. */
public final class ModTriggers {
    /** A player stomped fruit in a Crushing Tub (the Stomped advancement). */
    public static final PlayerTrigger STOMP = CriteriaTriggers.register(new PlayerTrigger(SeedToCellar.id("stomp")));

    /** A player poured a spirit aged 10 years or more from a cask (The Angel's Share). */
    public static final PlayerTrigger ANGELS_SHARE = CriteriaTriggers.register(new PlayerTrigger(SeedToCellar.id("angels_share")));
    public static final int ANGELS_SHARE_YEARS = 10;

    /** A player's hangover was cured: water, milk, a hearty breakfast or bitters (Morning After). */
    public static final PlayerTrigger MORNING_AFTER = CriteriaTriggers.register(new PlayerTrigger(SeedToCellar.id("morning_after")));

    /** A player filled a Bottle Shelf with six different drinks (Tavern Keeper). */
    public static final PlayerTrigger TAVERN_KEEPER = CriteriaTriggers.register(new PlayerTrigger(SeedToCellar.id("tavern_keeper")));

    /** Loads the class, registering the triggers (called from the mod constructor). */
    public static void init() {}

    private ModTriggers() {}
}
