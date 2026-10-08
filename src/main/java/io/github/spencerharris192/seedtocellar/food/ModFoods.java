package io.github.spencerharris192.seedtocellar.food;

import io.github.spencerharris192.seedtocellar.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Food values (GDD section 15), in four tiers: Snack (2-3 hunger), Simple (4-6), Meal (7-10,
 * usually in a bowl) and Feast (placed, several servings). Saturation is the vanilla modifier:
 * 0.6 is bread-like, 0.8 hearty, 0.3-0.4 light. Fine-tuned in the final balance pass.
 */
public final class ModFoods {
    /** Snacks eaten twice as fast (in the order they're declared below: this has to come first). */
    private static final Set<FoodProperties> FAST = Collections.newSetFromMap(new IdentityHashMap<>());

    // Breads (Simple)
    public static final FoodProperties RYE_BREAD = food(5, 0.7F);
    public static final FoodProperties SOURDOUGH_BREAD = food(6, 0.7F);
    public static final FoodProperties CORNBREAD = food(5, 0.6F);
    public static final FoodProperties SPENT_GRAIN_BREAD = food(5, 0.7F);
    public static final FoodProperties BEER_BREAD = food(6, 0.6F);

    // Snacks
    public static final FoodProperties TORTILLA = fast(3, 0.4F);
    public static final FoodProperties ROASTED_CORN = food(4, 0.5F);
    public static final FoodProperties POPCORN = fast(2, 0.3F);
    public static final FoodProperties RICE_BALL = fast(3, 0.5F);
    public static final FoodProperties JAM = food(3, 0.3F);
    public static final FoodProperties CRANBERRY_SAUCE = food(3, 0.4F);
    public static final FoodProperties PICKLES = food(3, 0.4F);
    public static final FoodProperties SAUERKRAUT = food(3, 0.4F);
    public static final FoodProperties DRIED_BERRIES = fast(2, 0.3F);
    public static final FoodProperties RAISINS = fast(2, 0.3F);
    public static final FoodProperties GRANOLA = fast(3, 0.5F);

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
    public static final FoodProperties CHILI_CON_CARNE = food(9, 0.8F);

    // Orchard kitchen (Phase 5): olive oil, wine and grape-leaf dishes
    public static final FoodProperties CURED_OLIVES = fast(3, 0.4F);
    public static final FoodProperties STUFFED_GRAPE_LEAVES = fast(4, 0.6F);
    public static final FoodProperties BRUSCHETTA = food(5, 0.6F);
    public static final FoodProperties SALAD = food(5, 0.6F);
    public static final FoodProperties GARLIC_BREAD = food(6, 0.6F);
    public static final FoodProperties RISOTTO = food(8, 0.7F);
    public static final FoodProperties COQ_AU_VIN = food(10, 0.8F);

    /**
     * Item properties for eating this food: snacks go down twice as fast, and chili con carne warms you through. (Built
     * when the item is, after effects are registered.)
     */
    public static Item.Properties properties(FoodProperties food) {
        Item.Properties properties = new Item.Properties();
        if (FAST.contains(food)) return properties.food(food, Consumables.defaultFood().consumeSeconds(0.8F).build());
        if (food == CHILI_CON_CARNE) {
            return properties.food(food, Consumables.defaultFood()
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.WARMTH, 3600))).build());
        }
        return properties.food(food);
    }

    private static FoodProperties fast(int nutrition, float saturation) {
        FoodProperties food = food(nutrition, saturation);
        FAST.add(food);
        return food;
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private ModFoods() {}
}
