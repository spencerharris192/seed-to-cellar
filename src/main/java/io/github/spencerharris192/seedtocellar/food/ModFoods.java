package io.github.spencerharris192.seedtocellar.food;

import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

/**
 * Food values (GDD section 15), in four tiers: Snack (2-3 hunger), Simple (4-6), Meal (7-10,
 * usually in a bowl) and Feast (placed, several servings). Saturation is the vanilla modifier:
 * 0.6 is bread-like, 0.8 hearty, 0.3-0.4 light. Fine-tuned in the final balance pass.
 */
public final class ModFoods {
    // Breads (Simple)
    public static final FoodProperties RYE_BREAD = food(5, 0.7F);
    public static final FoodProperties SOURDOUGH_BREAD = food(6, 0.7F);
    public static final FoodProperties CORNBREAD = food(5, 0.6F);
    public static final FoodProperties SPENT_GRAIN_BREAD = food(5, 0.7F);
    public static final FoodProperties BEER_BREAD = food(6, 0.6F);

    // Snacks
    public static final FoodProperties TORTILLA = new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).fast().build();
    public static final FoodProperties ROASTED_CORN = food(4, 0.5F);
    public static final FoodProperties POPCORN = new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).fast().build();
    public static final FoodProperties RICE_BALL = new FoodProperties.Builder().nutrition(3).saturationMod(0.5F).fast().build();
    public static final FoodProperties JAM = food(3, 0.3F);
    public static final FoodProperties CRANBERRY_SAUCE = food(3, 0.4F);
    public static final FoodProperties PICKLES = food(3, 0.4F);
    public static final FoodProperties SAUERKRAUT = food(3, 0.4F);
    public static final FoodProperties DRIED_BERRIES = new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).fast().build();
    public static final FoodProperties RAISINS = new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).fast().build();
    public static final FoodProperties GRANOLA = new FoodProperties.Builder().nutrition(3).saturationMod(0.5F).fast().build();

    // Kettle dishes: Simple
    public static final FoodProperties TOMATO_SOUP = food(6, 0.6F);
    public static final FoodProperties COOKED_RICE = food(5, 0.6F);
    public static final FoodProperties JAM_TOAST = food(6, 0.6F);
    public static final FoodProperties KIMCHI = food(5, 0.6F);
    /** A quarter of a pie (Simple): a whole pie is 16 hunger. */
    public static final FoodProperties PIE_SLICE = food(4, 0.4F);
    /** Plain steamed rice, eaten from the hand. */
    public static final FoodProperties STEAMED_RICE = food(3, 0.3F);
    /** A slice of rich layer cake: a little more than pie. */
    public static final FoodProperties CAKE_SLICE = food(5, 0.5F);

    // Kettle dishes: Meals
    public static final FoodProperties PORRIDGE = food(7, 0.7F);
    public static final FoodProperties BORSCHT = food(8, 0.7F);
    public static final FoodProperties MUTTON_AND_BARLEY_STEW = food(9, 0.8F);
    public static final FoodProperties BEEF_AND_ALE_STEW = food(10, 0.8F);
    /** One of the Harvest Feast's six servings (Feast tier): the most filling bowl in the mod. */
    public static final FoodProperties HARVEST_FEAST_SERVING = food(10, 0.9F);
    /** Warms you through: 3 minutes of Warmth (no freezing in powder snow). */
    public static final FoodProperties CHILI_CON_CARNE = new FoodProperties.Builder().nutrition(9).saturationMod(0.8F)
            .effect(() -> new MobEffectInstance(ModEffects.WARMTH.get(), 3600), 1.0F).build();

    // Orchard kitchen (Phase 5): olive oil, wine and grape-leaf dishes
    public static final FoodProperties CURED_OLIVES = new FoodProperties.Builder().nutrition(3).saturationMod(0.4F).fast().build();
    public static final FoodProperties STUFFED_GRAPE_LEAVES = new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).fast().build();
    public static final FoodProperties BRUSCHETTA = food(5, 0.6F);
    public static final FoodProperties SALAD = food(5, 0.6F);
    public static final FoodProperties GARLIC_BREAD = food(6, 0.6F);
    public static final FoodProperties RISOTTO = food(8, 0.7F);
    public static final FoodProperties COQ_AU_VIN = food(10, 0.8F);

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
    }

    private ModFoods() {}
}
