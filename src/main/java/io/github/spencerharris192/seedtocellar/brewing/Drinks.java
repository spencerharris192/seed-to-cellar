package io.github.spencerharris192.seedtocellar.brewing;

import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import io.github.spencerharris192.seedtocellar.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Every drinkable liquid (GDD section 10): its fluid, the item you drink it from, what it's served in,
 * and how it behaves. Item IDs match their fluid IDs (seedtocellar:pale_ale is both).
 */
public final class Drinks {
    /** {@code style}: how its name and color follow its age (spirits like whiskey), or null. */
    public record Drink(ModFluids.Entry fluid, RegistryObject<Item> item, DrinkProfile profile, Vessel vessel, @Nullable AgedStyle style) {
        public String name() {
            return fluid.name;
        }
    }

    private static final List<Drink> ALL = new ArrayList<>();

    // Ideal cask woods (GDD section 13): oak suits everything that ages; each other wood has its own character.
    private static final List<CaskWood> OLD_ALE_WOODS = List.of(CaskWood.OAK, CaskWood.SPRUCE, CaskWood.DARK_OAK);      // resinous, rich
    private static final List<CaskWood> RED_WINE_WOODS = List.of(CaskWood.OAK, CaskWood.DARK_OAK, CaskWood.MANGROVE);   // rich, earthy
    private static final List<CaskWood> WHITE_WINE_WOODS = List.of(CaskWood.OAK, CaskWood.BIRCH, CaskWood.ACACIA);      // light, honeyed
    private static final List<CaskWood> MEAD_WOODS = List.of(CaskWood.OAK, CaskWood.ACACIA, CaskWood.JUNGLE);           // honeyed, spicy
    private static final List<CaskWood> PERRY_WOODS = List.of(CaskWood.OAK, CaskWood.BIRCH);                            // light
    private static final List<CaskWood> FRUITY_WOODS = List.of(CaskWood.OAK, CaskWood.CHERRY);                          // cider, fruit wines

    // Effect lengths (the final balance pass): at three stars a beer gives 1.5-3.5 minutes, a wine or liqueur about 3-4, a
    // spirit 4-5 (half that at one star, half as long again at five). Refreshed, Warmth and Courage outlast the drink's alcohol
    // (40 s a unit) even at one star, so a drink now and then keeps them going without ever getting tipsy. Vanilla effects stay
    // short: Regeneration 8 s, Speed 40 s, Haste 1.5-2 minutes, Night Vision 3.

    // Beers, in a mug.                                   effect                 sec units hunger sat  aging years
    public static final Drink PALE_ALE = add(ModFluids.PALE_ALE, Vessel.MUG, graded(ModEffects.REFRESHED, 180, 1, 2, 0.2F, 0));
    public static final Drink AMBER_ALE = add(ModFluids.AMBER_ALE, Vessel.MUG, graded(ModEffects.REFRESHED, 180, 1, 2, 0.2F, 0));
    public static final Drink STOUT = add(ModFluids.STOUT, Vessel.MUG, graded(ModEffects.WARMTH, 240, 1, 3, 0.3F, 0));
    public static final Drink OLD_ALE = add(ModFluids.OLD_ALE, Vessel.MUG, graded(ModEffects.COURAGE, 240, 1.5F, 3, 0.3F, 3, OLD_ALE_WOODS));
    public static final Drink PLAIN_ALE = add(ModFluids.PLAIN_ALE, Vessel.MUG, graded(ModEffects.REFRESHED, 120, 1, 2, 0.2F, 0));
    public static final Drink TABLE_BEER = add(ModFluids.TABLE_BEER, Vessel.MUG, graded(ModEffects.REFRESHED, 90, 0.5F, 1, 0.1F, 0));
    /** Lager: pale, crisp, fermented cold with lager yeast. */
    public static final Drink LAGER = add(ModFluids.LAGER, Vessel.MUG, graded(ModEffects.REFRESHED, 210, 1, 2, 0.2F, 0));
    /** Wheat beer: hazy, half or more wheat malt. */
    public static final Drink WHEAT_BEER = add(ModFluids.WHEAT_BEER, Vessel.MUG, graded(ModEffects.REFRESHED, 180, 1, 2, 0.2F, 0));

    // Wines and meads, in a wine bottle; cider and perry, in a mug.
    public static final Drink RED_WINE = add(ModFluids.RED_WINE, Vessel.WINE_BOTTLE, graded(() -> MobEffects.REGENERATION, 8, 2, 2, 0.2F, 3, RED_WINE_WOODS));
    public static final Drink WHITE_WINE = add(ModFluids.WHITE_WINE, Vessel.WINE_BOTTLE, graded(ModEffects.REFRESHED, 240, 2, 2, 0.2F, 2, WHITE_WINE_WOODS));
    public static final Drink ROSE = add(ModFluids.ROSE, Vessel.WINE_BOTTLE, graded(ModEffects.REFRESHED, 210, 2, 2, 0.2F, 0));
    public static final Drink MEAD = add(ModFluids.MEAD, Vessel.WINE_BOTTLE, graded(ModEffects.COURAGE, 240, 2, 3, 0.3F, 2, MEAD_WOODS));
    public static final Drink CIDER = add(ModFluids.CIDER, Vessel.MUG, graded(ModEffects.REFRESHED, 180, 1, 2, 0.2F, 2, FRUITY_WOODS));
    public static final Drink PERRY = add(ModFluids.PERRY, Vessel.MUG, graded(ModEffects.REFRESHED, 180, 1, 2, 0.2F, 2, PERRY_WOODS));
    public static final Drink CHERRY_WINE = fruitWine(ModFluids.CHERRY_WINE);
    public static final Drink PLUM_WINE = fruitWine(ModFluids.PLUM_WINE);
    public static final Drink PEACH_WINE = fruitWine(ModFluids.PEACH_WINE);
    public static final Drink BLUEBERRY_WINE = fruitWine(ModFluids.BLUEBERRY_WINE);
    public static final Drink BLACKBERRY_WINE = fruitWine(ModFluids.BLACKBERRY_WINE);
    public static final Drink ELDERBERRY_WINE = fruitWine(ModFluids.ELDERBERRY_WINE);
    public static final Drink CRANBERRY_WINE = fruitWine(ModFluids.CRANBERRY_WINE);
    public static final Drink SWEET_BERRY_WINE = fruitWine(ModFluids.SWEET_BERRY_WINE);
    /** Glow-berry wine: a little night vision instead of Refreshed. */
    public static final Drink GLOW_BERRY_WINE = add(ModFluids.GLOW_BERRY_WINE, Vessel.WINE_BOTTLE,
            graded(() -> MobEffects.NIGHT_VISION, 180, 2, 2, 0.2F, 2, FRUITY_WOODS));
    public static final Drink MELON_WINE = fruitWine(ModFluids.MELON_WINE);
    /** Sake: rice and koji fermented cool with wine yeast; served in a wine bottle. */
    public static final Drink SAKE = add(ModFluids.SAKE, Vessel.WINE_BOTTLE, graded(() -> MobEffects.REGENERATION, 8, 2, 2, 0.2F, 0));
    /** Ginger beer, brewed in a jar: fizzy and refreshing, almost no alcohol (no stars). */
    public static final Drink GINGER_BEER = add(ModFluids.GINGER_BEER, Vessel.MUG,
            new DrinkProfile(ModEffects.REFRESHED, 180, 0, 2, 0.2F, 0, false, List.of(), CraftStep.CONDITIONED, DrinkProfile.Charring.NONE));
    /** Mulled wine, spiced in the kettle and served hot in a mug: no stars of its own. */
    public static final Drink MULLED_WINE = add(ModFluids.MULLED_WINE, Vessel.MUG,
            new DrinkProfile(ModEffects.WARMTH, 300, 2, 3, 0.3F, 0, false, List.of(), CraftStep.CONDITIONED, DrinkProfile.Charring.NONE));

    // Lemonade and elderflower cordial, made in the kettle: a longer Refreshed than juice, no alcohol, no stars.
    public static final Drink LEMONADE = softDrink(ModFluids.LEMONADE);
    public static final Drink ELDERFLOWER_CORDIAL = softDrink(ModFluids.ELDERFLOWER_CORDIAL);
    /** Coffee from the kettle: a short burst of Haste, no alcohol. */
    public static final Drink COFFEE = add(ModFluids.COFFEE, Vessel.GLASS_BOTTLE,
            new DrinkProfile(() -> MobEffects.DIG_SPEED, 120, 0, 1, 0.1F, 0, false, List.of(), CraftStep.CONDITIONED, DrinkProfile.Charring.NONE));

    // Spirits, in a spirit bottle (GDD section 10.3): strong and warming, made in the Pot Still. Their craft star is how they
    // were distilled (twice; vodka three times or through charcoal), never resting.
    // Whiskeys love a charred cask (any wood); bourbon needs charred oak for its star and its name.
    private static final List<CaskWood> WHISKEY_WOODS = List.of(CaskWood.OAK, CaskWood.DARK_OAK);
    private static final List<CaskWood> BRANDY_WOODS = List.of(CaskWood.OAK, CaskWood.CHERRY, CaskWood.ACACIA);   // fruity, honeyed
    private static final List<CaskWood> FRUIT_BRANDY_WOODS = List.of(CaskWood.OAK, CaskWood.CHERRY);
    public static final Drink MALT_WHISKEY = add(ModFluids.MALT_WHISKEY, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.WARMTH, 300, 8, WHISKEY_WOODS, CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.IDEAL),
            new AgedStyle(List.of(AgedStyle.Name.from(3, null), AgedStyle.Name.from(0, "new_make")), 0xC0F0E2B8, 0xF0B86A22, 12));
    public static final Drink BOURBON = add(ModFluids.BOURBON, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.WARMTH, 300, 6, List.of(CaskWood.OAK), CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.REQUIRED),
            new AgedStyle(List.of(new AgedStyle.Name(2, null, CaskWood.OAK, true), AgedStyle.Name.from(2, "corn_whiskey"),
                    AgedStyle.Name.from(0, "white_dog")), 0xC0F4E4BC, 0xF2A04818, 8));
    public static final Drink VODKA = add(ModFluids.VODKA, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.WARMTH, 270, 0, List.of(), CraftStep.NEUTRAL, DrinkProfile.Charring.NONE));
    public static final Drink BRANDY = add(ModFluids.BRANDY, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.WARMTH, 300, 6, BRANDY_WOODS, CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.NONE),
            new AgedStyle(List.of(AgedStyle.Name.from(2, null), AgedStyle.Name.from(0, "eau_de_vie")), 0xB8F4ECD0, 0xF0A8541C, 10));
    // Fruit brandies, one per fruit wine (GDD section 10.3): clear when young, most of them golden with age.
    public static final Drink APPLE_BRANDY = fruitBrandy(ModFluids.APPLE_BRANDY, 0xF0C88A34);
    public static final Drink PEAR_BRANDY = fruitBrandy(ModFluids.PEAR_BRANDY, 0xE8D8B060);
    public static final Drink KIRSCH = fruitBrandy(ModFluids.KIRSCH, 0xE0E8D8B0);
    public static final Drink SLIVOVITZ = fruitBrandy(ModFluids.SLIVOVITZ, 0xF0C08A3C);
    public static final Drink PEACH_BRANDY = fruitBrandy(ModFluids.PEACH_BRANDY, 0xF0D49450);
    /** Rum (molasses): White, Gold at 2 years, Dark at 6; best in jungle (spicy) or oak. */
    public static final Drink RUM = add(ModFluids.RUM, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.COURAGE, 300, 6, List.of(CaskWood.JUNGLE, CaskWood.OAK), CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.NONE),
            new AgedStyle(List.of(AgedStyle.Name.from(6, "dark_rum"), AgedStyle.Name.from(2, "gold_rum"), AgedStyle.Name.from(0, "white_rum")),
                    0xB0F4F0E4, 0xF45A2410, 8));
    /** Tequila (agave): Blanco, Reposado at 1 year, Añejo at 4; best in oak or mangrove (earthy). A quick burst of speed. */
    public static final Drink TEQUILA = add(ModFluids.TEQUILA, Vessel.SPIRIT_BOTTLE,
            spirit(() -> net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 4, List.of(CaskWood.OAK, CaskWood.MANGROVE),
                    CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.NONE),
            new AgedStyle(List.of(AgedStyle.Name.from(4, "tequila_anejo"), AgedStyle.Name.from(1, "tequila_reposado"),
                    AgedStyle.Name.from(0, "tequila_blanco")), 0xA8F0F4F4, 0xE8D8963C, 6));
    /** Gin: vodka distilled again through juniper and other botanicals in the Gin Basket; refreshing, drunk young. */
    public static final Drink GIN = add(ModFluids.GIN, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.REFRESHED, 300, 0, List.of(), CraftStep.BOTANICALS, DrinkProfile.Charring.NONE));
    // Liqueurs and bitters (GDD section 10.4): a spirit steeped in the Preserving Jar with fruit or herbs and sugar; they
    // keep the spirit's stars.
    public static final Drink LIMONCELLO = liqueur(ModFluids.LIMONCELLO, ModEffects.REFRESHED, 240);
    public static final Drink ORANGE_LIQUEUR = liqueur(ModFluids.ORANGE_LIQUEUR, ModEffects.REFRESHED, 240);
    public static final Drink UMESHU = liqueur(ModFluids.UMESHU, ModEffects.REFRESHED, 240);
    public static final Drink CHERRY_LIQUEUR = liqueur(ModFluids.CHERRY_LIQUEUR, ModEffects.REFRESHED, 240);
    public static final Drink CREME_DE_MURE = liqueur(ModFluids.CREME_DE_MURE, ModEffects.REFRESHED, 240);
    public static final Drink ELDERFLOWER_LIQUEUR = liqueur(ModFluids.ELDERFLOWER_LIQUEUR, ModEffects.REFRESHED, 240);
    /** Herbal liqueur settles the stomach: it cures nausea. */
    public static final Drink HERBAL_LIQUEUR = liqueur(ModFluids.HERBAL_LIQUEUR, ModEffects.REFRESHED, 180);
    public static final Drink SPICED_RUM = add(ModFluids.SPICED_RUM, Vessel.SPIRIT_BOTTLE,
            new DrinkProfile(ModEffects.WARMTH, 300, 3, 0, 0F, 0, true, List.of(), CraftStep.INFUSED, DrinkProfile.Charring.NONE));
    /** Aromatic bitters: a single dose cures a hangover and nausea. */
    public static final Drink AROMATIC_BITTERS = add(ModFluids.AROMATIC_BITTERS, Vessel.SPIRIT_BOTTLE,
            new DrinkProfile(ModEffects.REFRESHED, 90, 1, 0, 0F, 0, true, List.of(), CraftStep.INFUSED, DrinkProfile.Charring.NONE));
    /** Coffee liqueur: a quick kick of Haste. */
    public static final Drink COFFEE_LIQUEUR = liqueur(ModFluids.COFFEE_LIQUEUR, () -> MobEffects.DIG_SPEED, 90);
    /**
     * Apple Crown Whiskey: malt whiskey steeped in the jar with apples and honey, warming like whiskey and keeping its stars.
     * Its secret: a perfect (5-star) whiskey steeped with a golden apple instead comes out crowned, with a sixth star, and
     * drinks like a golden apple too (BrewQuality, DrinkItem).
     */
    public static final Drink APPLE_CROWN_WHISKEY = add(ModFluids.APPLE_CROWN_WHISKEY, Vessel.SPIRIT_BOTTLE,
            new DrinkProfile(ModEffects.WARMTH, 300, 3, 0, 0F, 0, true, List.of(), CraftStep.INFUSED, DrinkProfile.Charring.NONE));
    /** Grappa: distilled from grape pomace, drunk young. */
    public static final Drink GRAPPA = add(ModFluids.GRAPPA, Vessel.SPIRIT_BOTTLE,
            spirit(ModEffects.WARMTH, 270, 0, List.of(), CraftStep.DOUBLE_DISTILLED, DrinkProfile.Charring.NONE));

    // Juices, in vanilla's glass bottle: a short Refreshed, no alcohol, no stars.
    public static final Drink RED_GRAPE_JUICE = juice(ModFluids.RED_GRAPE_JUICE);
    public static final Drink WHITE_GRAPE_JUICE = juice(ModFluids.WHITE_GRAPE_JUICE);
    public static final Drink APPLE_JUICE = juice(ModFluids.APPLE_JUICE);
    public static final Drink PEAR_JUICE = juice(ModFluids.PEAR_JUICE);
    public static final Drink CHERRY_JUICE = juice(ModFluids.CHERRY_JUICE);
    public static final Drink PLUM_JUICE = juice(ModFluids.PLUM_JUICE);
    public static final Drink PEACH_JUICE = juice(ModFluids.PEACH_JUICE);
    public static final Drink ORANGE_JUICE = juice(ModFluids.ORANGE_JUICE);
    public static final Drink BLUEBERRY_JUICE = juice(ModFluids.BLUEBERRY_JUICE);
    public static final Drink BLACKBERRY_JUICE = juice(ModFluids.BLACKBERRY_JUICE);
    public static final Drink ELDERBERRY_JUICE = juice(ModFluids.ELDERBERRY_JUICE);
    public static final Drink CRANBERRY_JUICE = juice(ModFluids.CRANBERRY_JUICE);
    public static final Drink SWEET_BERRY_JUICE = juice(ModFluids.SWEET_BERRY_JUICE);
    public static final Drink GLOW_BERRY_JUICE = juice(ModFluids.GLOW_BERRY_JUICE);
    public static final Drink MELON_JUICE = juice(ModFluids.MELON_JUICE);

    private static DrinkProfile graded(Supplier<MobEffect> effect, int seconds, float units, int nutrition, float saturation, int agingYears) {
        return graded(effect, seconds, units, nutrition, saturation, agingYears, List.of());
    }

    private static DrinkProfile graded(Supplier<MobEffect> effect, int seconds, float units, int nutrition, float saturation, int agingYears,
                                       List<CaskWood> woods) {
        return new DrinkProfile(effect, seconds, units, nutrition, saturation, agingYears, true, woods, CraftStep.CONDITIONED,
                DrinkProfile.Charring.NONE);
    }

    /** A spirit: 3 units, no food value, its craft star from the still. */
    private static DrinkProfile spirit(Supplier<MobEffect> effect, int seconds, int agingYears, List<CaskWood> woods, CraftStep craft,
                                       DrinkProfile.Charring charring) {
        return new DrinkProfile(effect, seconds, 3, 0, 0F, agingYears, true, woods, craft, charring);
    }

    /** A liqueur: sweet and medium-strong (2 units), a little food value. */
    private static Drink liqueur(ModFluids.Entry fluid, Supplier<MobEffect> effect, int seconds) {
        return add(fluid, Vessel.SPIRIT_BOTTLE, new DrinkProfile(effect, seconds, 2, 1, 0.1F, 0, true, List.of(), CraftStep.INFUSED,
                DrinkProfile.Charring.NONE));
    }

    private static Drink fruitBrandy(ModFluids.Entry fluid, int aged) {
        return add(fluid, Vessel.SPIRIT_BOTTLE, spirit(ModEffects.WARMTH, 300, 6, FRUIT_BRANDY_WOODS, CraftStep.DOUBLE_DISTILLED,
                DrinkProfile.Charring.NONE), new AgedStyle(List.of(), fluid.tint, aged, 10));
    }

    private static Drink fruitWine(ModFluids.Entry fluid) {
        return add(fluid, Vessel.WINE_BOTTLE, graded(ModEffects.REFRESHED, 180, 2, 2, 0.2F, 2, FRUITY_WOODS));
    }

    private static Drink softDrink(ModFluids.Entry fluid) {
        return add(fluid, Vessel.GLASS_BOTTLE, new DrinkProfile(ModEffects.REFRESHED, 240, 0, 3, 0.3F, 0, false, List.of(), CraftStep.CONDITIONED,
                DrinkProfile.Charring.NONE));
    }

    private static Drink juice(ModFluids.Entry fluid) {
        return add(fluid, Vessel.GLASS_BOTTLE, new DrinkProfile(ModEffects.REFRESHED, 90, 0, 2, 0.2F, 0, false, List.of(), CraftStep.CONDITIONED,
                DrinkProfile.Charring.NONE));
    }

    private static Drink add(ModFluids.Entry fluid, Vessel vessel, DrinkProfile profile) {
        return add(fluid, vessel, profile, null);
    }

    private static Drink add(ModFluids.Entry fluid, Vessel vessel, DrinkProfile profile, @Nullable AgedStyle style) {
        RegistryObject<Item> item = ModItems.ITEMS.register(fluid.name, () -> new DrinkItem(fluid::get, profile, vessel,
                new Item.Properties().stacksTo(16).craftRemainder(vessel.empty())
                        .food(new FoodProperties.Builder().nutrition(profile.nutrition()).saturationMod(profile.saturation()).alwaysEat().build())));
        Drink drink = new Drink(fluid, item, profile, vessel, style);
        ALL.add(drink);
        return drink;
    }

    /** The fruit wines (not grape wine or mead): one family in tooltips and JEI. */
    public static List<Drink> fruitWines() {
        return List.of(CHERRY_WINE, PLUM_WINE, PEACH_WINE, BLUEBERRY_WINE, BLACKBERRY_WINE, ELDERBERRY_WINE, CRANBERRY_WINE,
                SWEET_BERRY_WINE, GLOW_BERRY_WINE, MELON_WINE);
    }

    /** The beers: every drink an "any ale" recipe accepts. */
    public static List<Drink> beers() {
        return List.of(PALE_ALE, AMBER_ALE, STOUT, OLD_ALE, PLAIN_ALE, TABLE_BEER, LAGER, WHEAT_BEER);
    }

    /** The spirits: everything made in the Pot Still (not the liqueurs steeped from them). */
    public static List<Drink> spirits() {
        return ALL.stream().filter(d -> d.vessel() == Vessel.SPIRIT_BOTTLE && d.profile().craft() != CraftStep.INFUSED).toList();
    }

    /** Liqueurs and bitters: steeped from a spirit in the Preserving Jar. */
    public static List<Drink> liqueurs() {
        return ALL.stream().filter(d -> d.profile().craft() == CraftStep.INFUSED).toList();
    }

    /** What drinking it takes away: bitters cure a hangover and nausea, herbal liqueur nausea. */
    public static List<Supplier<MobEffect>> cures(Drink drink) {
        if (drink == AROMATIC_BITTERS) return List.of(ModEffects.HANGOVER, () -> net.minecraft.world.effect.MobEffects.CONFUSION);
        if (drink == HERBAL_LIQUEUR) return List.of(() -> net.minecraft.world.effect.MobEffects.CONFUSION);
        return List.of();
    }

    public static List<Drink> all() {
        return Collections.unmodifiableList(ALL);
    }

    /**
     * Drinks by fluid and by item, built on first use (after registration). Renderers look a drink up several times per
     * bottle on every frame, so these are maps rather than a search of the list.
     */
    private static volatile Map<Fluid, Drink> byFluid;
    private static volatile Map<Item, Drink> byItem;

    public static Optional<Drink> byFluid(Fluid fluid) {
        Map<Fluid, Drink> map = byFluid;
        if (map == null) {
            map = new IdentityHashMap<>();
            for (Drink drink : ALL) map.putIfAbsent(drink.fluid().get(), drink);
            byFluid = map;
        }
        return Optional.ofNullable(map.get(fluid));
    }

    /** What a drink with this label goes by now ("New Make", "Corn Whiskey"), or null for its own name. */
    @Nullable
    public static String nameKey(Fluid fluid, @Nullable CompoundTag data) {
        return byFluid(fluid).map(Drink::style).map(s -> s.nameKey(data)).orElse(null);
    }

    /** The drink's color at its age (spirits deepen as they age); {@code base} for everything else. */
    public static int tint(Fluid fluid, @Nullable CompoundTag data, int base) {
        return byFluid(fluid).map(Drink::style).map(s -> s.tint(data)).orElse(base);
    }

    public static Optional<Drink> byItem(Item item) {
        Map<Item, Drink> map = byItem;
        if (map == null) {
            map = new IdentityHashMap<>();
            for (Drink drink : ALL) map.putIfAbsent(drink.item().get(), drink);
            byItem = map;
        }
        return Optional.ofNullable(map.get(item));
    }

    /** Called from the mod constructor so these register before the item registry freezes. */
    public static void init() {
    }

    private Drinks() {}
}
