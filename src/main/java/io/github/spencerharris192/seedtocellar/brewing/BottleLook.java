package io.github.spencerharris192.seedtocellar.brewing;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * How a bottle of spirit or liqueur looks standing on a Bottle Shelf or lying in a Wine Display (GDD section 20.4): each
 * has its own bottle shape and its own label, the way real bottles are told apart, so a shelf of clear spirits still
 * reads at a glance. Datagen builds a model from each look ({@code block/display/<drink>}); the renderer and the item
 * colors take the label's colors from here. Every drink in a spirit bottle needs one (datagen fails without it).
 *
 * @param shape  the bottle's silhouette
 * @param label  where the accent color sits on the paper label
 * @param paper  the label's paper (0xRRGGBB)
 * @param accent the label's print, and the bottle's wax, foil, collar or cap where its shape has one
 * @param glass  the glass's tint; white is clear (gin's is blue, and the spirit inside looks it)
 */
public record BottleLook(Shape shape, Label label, int paper, int accent, int glass) {
    private static final int CLEAR = 0xFFFFFF;

    public enum Shape {
        /** Whiskey: a tall square bottle, flat shoulders, a collar round its short neck, a wooden-capped cork. */
        SQUARE(3),
        /** Bourbon: short and square, its neck dipped in wax that drips down onto the shoulders. */
        WAXED(3.25F),
        /** Vodka: tall, slim and round, a metal screw cap. */
        TALL(2.5F),
        /** Gin: an apothecary bottle, a label on its front and back, a wide neck and a broad cork. */
        APOTHECARY(3),
        /** Rum: squat and round, a long neck with a collar. */
        ROUND(3.25F),
        /** Brandy: a decanter, its belly wide and low, a thin neck with a collar. */
        DECANTER(3.5F),
        /** Fruit brandy: tall and slender, a long tapering neck under a foil capsule. */
        SLENDER(2.25F),
        /** Tequila: a chunky square base tapering to a short neck, a big round wooden stopper. */
        STOPPERED(3.25F),
        /** Bitters: a little bottle under an oversized label, a tall cap. */
        DASHER(2.25F),
        /**
         * Apple Crown Whiskey alone: a glass apple with a leaf at its neck and a jewelled gold crown for a stopper (its own
         * textures; the crowned six-star bottle has its own model too, and shimmers).
         */
        APPLE(3.75F, 4.75F);

        /** Half the body's width, in pixels: how high it rests when the Wine Display lays it down. */
        public final float radius;
        /** How far anything reaches out from its middle (the Apple Crown's leaf), so a laid-down bottle fits under the shelf above. */
        public final float reach;

        Shape(float radius) {
            this(radius, radius);
        }

        Shape(float radius, float reach) {
            this.radius = radius;
            this.reach = reach;
        }
    }

    /** The label's design: a band round its middle, a band along its top or bottom, an emblem front and back, or two thin rules. */
    public enum Label { BAND, HEADER, FOOT, CREST, RULES }

    private static final Map<String, BottleLook> LOOKS = new HashMap<>();

    static {
        // Spirits
        look(Drinks.MALT_WHISKEY, Shape.SQUARE, Label.CREST, 0xE8DCB8, 0x1F3A2C);
        look(Drinks.BOURBON, Shape.WAXED, Label.BAND, 0xE2C48E, 0xA51E1E);
        look(Drinks.VODKA, Shape.TALL, Label.BAND, 0xF2F4F6, 0x2F5BA8);
        look(Drinks.GRAPPA, Shape.TALL, Label.RULES, 0x34302E, 0xC8CCD0);
        look(Drinks.GIN, Shape.APOTHECARY, Label.CREST, 0xF0ECDF, 0x4B3A78, 0xA8C8EC);
        look(Drinks.BRANDY, Shape.DECANTER, Label.RULES, 0x2A2220, 0xC9A23E);
        look(Drinks.APPLE_BRANDY, Shape.SLENDER, Label.BAND, 0xEFE6CC, 0x4E8A2E);
        look(Drinks.PEAR_BRANDY, Shape.SLENDER, Label.HEADER, 0xEFE6CC, 0xA8A83A);
        look(Drinks.KIRSCH, Shape.SLENDER, Label.BAND, 0xF4F2EC, 0x9E1A2E);
        look(Drinks.SLIVOVITZ, Shape.SLENDER, Label.FOOT, 0xEFE6CC, 0x5C2A6A);
        look(Drinks.PEACH_BRANDY, Shape.SLENDER, Label.HEADER, 0xF4EEE2, 0xE0864C);
        look(Drinks.RUM, Shape.ROUND, Label.BAND, 0xE4CF9C, 0x1F6E6A);
        look(Drinks.TEQUILA, Shape.STOPPERED, Label.HEADER, 0xF2EEE4, 0xC0582A);
        // Liqueurs and bitters
        look(Drinks.LIMONCELLO, Shape.TALL, Label.FOOT, 0xF6F2DC, 0xE6C82A);
        look(Drinks.ORANGE_LIQUEUR, Shape.SQUARE, Label.BAND, 0xE88A2A, 0x5A2A14);
        look(Drinks.COFFEE_LIQUEUR, Shape.SQUARE, Label.RULES, 0xE8D8B8, 0x4A2C1C);
        look(Drinks.UMESHU, Shape.ROUND, Label.CREST, 0xF4F0E8, 0xC02828);
        look(Drinks.CREME_DE_MURE, Shape.ROUND, Label.RULES, 0x2C2430, 0x8E5AA8);
        look(Drinks.SPICED_RUM, Shape.ROUND, Label.HEADER, 0x2A2420, 0xD08A30);
        look(Drinks.CHERRY_LIQUEUR, Shape.DECANTER, Label.BAND, 0xF2EADA, 0x8A1426);
        look(Drinks.ELDERFLOWER_LIQUEUR, Shape.APOTHECARY, Label.RULES, 0xF4F2E6, 0x6E9A4E);
        look(Drinks.HERBAL_LIQUEUR, Shape.APOTHECARY, Label.BAND, 0xE8DEC0, 0x2E5A2E);
        look(Drinks.AROMATIC_BITTERS, Shape.DASHER, Label.BAND, 0xF0E8D4, 0x8A3A22);
        // Apple Crown Whiskey: deep apple-red paper, gold (a crowned bottle swaps them: gold paper, red bands)
        look(Drinks.APPLE_CROWN_WHISKEY, Shape.APPLE, Label.CREST, 0x8E1A1A, 0xE0B040);
    }

    private static void look(Drinks.Drink drink, Shape shape, Label label, int paper, int accent) {
        look(drink, shape, label, paper, accent, CLEAR);
    }

    private static void look(Drinks.Drink drink, Shape shape, Label label, int paper, int accent, int glass) {
        LOOKS.put(drink.name(), new BottleLook(shape, label, paper, accent, glass));
    }

    /** The drink's bottle, or null if it isn't served in a spirit bottle. */
    @Nullable
    public static BottleLook of(Drinks.Drink drink) {
        return LOOKS.get(drink.name());
    }

    public static Collection<BottleLook> all() {
        return LOOKS.values();
    }
}
