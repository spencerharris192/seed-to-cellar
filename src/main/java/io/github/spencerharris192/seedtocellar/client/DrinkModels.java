package io.github.spencerharris192.seedtocellar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.spencerharris192.seedtocellar.SeedToCellar;
import io.github.spencerharris192.seedtocellar.brewing.BottleLook;
import io.github.spencerharris192.seedtocellar.brewing.BrewData;
import io.github.spencerharris192.seedtocellar.brewing.DrinkItem;
import io.github.spencerharris192.seedtocellar.brewing.Drinks;
import io.github.spencerharris192.seedtocellar.registry.ModFluids;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jspecify.annotations.Nullable;

/**
 * The 3D drinks on the Bottle Shelf, the Wine Display and tables: bottles, mugs and flasks in each drink's color, every
 * spirit in its own bottle with its own label (datagen builds one from each drink's BottleLook).
 */
public final class DrinkModels {
    /** A wine bottle lying in a rack hole, neck out; its foil (tint 0) takes the wine's color. */
    public static final Identifier RACK_BOTTLE = SeedToCellar.id("block/wine_rack_bottle");
    /** The 3D drinks on the Bottle Shelf and Wine Display: upright, on y = 0 around x = z = 8; tint 0 is the drink's color. */
    public static final Identifier DRINK_BOTTLE = SeedToCellar.id("block/drink_wine_bottle");
    public static final Identifier DRINK_MUG = SeedToCellar.id("block/drink_mug");
    public static final Identifier DRINK_FLASK = SeedToCellar.id("block/drink_flask");
    /** An empty mug, for the Mug Rack's pegs. */
    public static final Identifier MUG_EMPTY = SeedToCellar.id("block/drink_mug_empty");
    /** How high a wine bottle's middle rests above a Wine Display shelf: its body is 5 pixels thick. */
    private static final float WINE_BOTTLE_RADIUS = 2.65F;

    /**
     * A drink as the shelves show it, worked out from the item: its model (null for another mod's drink, drawn as its item),
     * each tinted part's color, whether it shimmers (the crowned Apple Crown), and how it lies on the Wine Display.
     */
    public record Shown(@Nullable Identifier model, int[] tints, boolean foil, float restingRadius, @Nullable BottleLook look) {
        public void submit(PoseStack pose, SubmitNodeCollector collector, int light) {
            if (model != null) ClientModels.submit(model, pose, collector, tints, light, foil);
        }

        /** The Wine Display's usual scale, smaller for a bottle too stout to lie under the shelf above (the decanter, the Apple Crown). */
        public float displayScale(float usual, float room) {
            return look == null ? usual : Math.min(usual, room / (restingRadius + look.shape().reach));
        }
    }

    public static void register(ModelEvent.RegisterStandalone event) {
        for (Identifier id : new Identifier[]{RACK_BOTTLE, DRINK_BOTTLE, DRINK_MUG, DRINK_FLASK, MUG_EMPTY}) ClientModels.register(event, id);
        for (Drinks.Drink drink : Drinks.all()) {
            BottleLook look = BottleLook.of(drink);
            if (look != null) ClientModels.register(event, spiritModel(drink));
            if (look != null && canBeCrowned(look)) ClientModels.register(event, crownedModel(drink));
        }
    }

    /** A spirit's own bottle on the shelves. */
    public static Identifier spiritModel(Drinks.Drink drink) {
        return SeedToCellar.id("block/display/" + drink.name());
    }

    /** The crowned (six-star) Apple Crown Whiskey's bottle. */
    public static Identifier crownedModel(Drinks.Drink drink) {
        return SeedToCellar.id("block/display/" + drink.name() + "_crowned");
    }

    /** Only the Apple Crown's bottle has a crowned look. */
    public static boolean canBeCrowned(BottleLook look) {
        return look.shape() == BottleLook.Shape.APPLE;
    }

    /** How {@code stack} shows on the shelves (as its own model if {@code model} is given, like the empty mug). */
    public static Shown shown(ItemStack stack, @Nullable Identifier model) {
        int drink = color(stack);
        BottleLook look = look(stack);
        boolean crowned = look != null && canBeCrowned(look) && DrinkItem.quality(stack).crowned();
        Identifier id = model != null ? model : drinkModel(stack, look, crowned);
        // tint 0 is the drink's (the color its item shows); a spirit's bottle adds its label's paper (1) and accent (2) and
        // its glass (3), the spirit seen through tinted glass taking its tint. A crowned bottle swaps its label's colors.
        int[] tints = look == null || model != null ? new int[]{drink}
                : crowned ? new int[]{multiply(drink, look.glass()), opaque(look.accent()), opaque(look.paper()), opaque(look.glass())}
                : new int[]{multiply(drink, look.glass()), opaque(look.paper()), opaque(look.accent()), opaque(look.glass())};
        float radius = look == null ? WINE_BOTTLE_RADIUS : look.shape().radius + 0.15F;
        return new Shown(id, tints, crowned, radius, look);
    }

    /** The drink's color, as its item shows it (white for anything not a drink). */
    public static int color(ItemStack stack) {
        if (!(stack.getItem() instanceof DrinkItem drink)) return -1;
        int base = drink.fluid().getFluidType() instanceof ModFluids.BrewFluidType type ? type.tint : -1;
        return ARGB.opaque(Drinks.tint(drink.fluid(), BrewData.orNull(stack), base));
    }

    /** Our 3D model for a drink, by what it's served in (each spirit its own); null for anything else. */
    private static @Nullable Identifier drinkModel(ItemStack stack, @Nullable BottleLook look, boolean crowned) {
        if (!(stack.getItem() instanceof DrinkItem drink)) return null;
        return switch (drink.vessel()) {
            case WINE_BOTTLE -> DRINK_BOTTLE;
            case MUG -> DRINK_MUG;
            case GLASS_BOTTLE -> DRINK_FLASK;
            case SPIRIT_BOTTLE -> look == null ? null : Drinks.byFluid(drink.fluid())
                    .map(d -> crowned ? crownedModel(d) : spiritModel(d)).orElse(null);
        };
    }

    private static @Nullable BottleLook look(ItemStack stack) {
        return stack.getItem() instanceof DrinkItem drink ? Drinks.byFluid(drink.fluid()).map(BottleLook::of).orElse(null) : null;
    }

    private static int opaque(int rgb) {
        return ARGB.opaque(rgb);
    }

    private static int multiply(int a, int b) {
        int out = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            out |= ((a >> shift & 255) * (b >> shift & 255) / 255) << shift;
        }
        return ARGB.opaque(out);
    }

    private DrinkModels() {}
}
