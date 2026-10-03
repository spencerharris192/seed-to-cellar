package io.github.spencerharris192.seedtocellar.farming;

import io.github.spencerharris192.seedtocellar.registry.ModItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every row crop in the mod (GDD section 6.2). Hops live apart: they climb trellises.
 * Order here is the creative tab order. Registry IDs are permanent once released.
 */
public final class Crops {
    private static final List<Crop> ALL = new ArrayList<>();

    // --- Grains ---
    public static final Crop BARLEY = add(Crop.row("barley", "Barley")
            .climate(Climate.TEMPERATE).kind(Crop.Kind.GRAIN)
            .wild(24, 6, "plains and meadows").grassDrop(0.05F, false)
            .uses("A malting grain", "Steep in a Malting Tub with water"));
    public static final Crop RYE = add(Crop.row("rye", "Rye")
            .climate(Climate.COLD).kind(Crop.Kind.GRAIN)
            .wild(24, 8, "taiga and snowy plains").grassDrop(0.05F, true)
            .uses("A hardy grain for cold lands", "Grind in a Millstone for rye flour"));
    public static final Crop OATS = add(Crop.row("oats", "Oats").ids("oat_crop", "oat_seeds", "Oat Seeds")
            .climate(Climate.TEMPERATE).kind(Crop.Kind.GRAIN)
            .wild(20, 8, "plains and meadows").grassDrop(0.05F, false)
            .uses("A soft, pale grain", "Grind in a Millstone for rolled oats"));
    public static final Crop SORGHUM = add(Crop.row("sorghum", "Sorghum")
            .climate(Climate.HOT).kind(Crop.Kind.GRAIN)
            .wild(16, 8, "savannas and badlands")
            .uses("A grain that thrives in heat", "Press it for juice, then boil the juice into syrup"));
    public static final Crop CORN = add(Crop.row("corn", "Corn").tall()
            .climate(Climate.WARM).kind(Crop.Kind.GRAIN)
            .wild(12, 10, "plains and savannas")
            .uses("Sweet ears from a tall plant", "Roast it, or grind it into cornmeal"));
    public static final Crop RICE = add(Crop.row("rice", "Rice").paddy(4, 1).selfPlanting()
            .climate(Climate.WARM).kind(Crop.Kind.GRAIN)
            .wild(16, 6, "rivers and swamps")
            .uses("A grain grown in shallow water", "Plant in water 1 block deep, or compost it"));

    // --- Vegetables (4 growth looks each, like vanilla's) ---
    public static final Crop SUGAR_BEET = add(Crop.row("sugar_beet", "Sugar Beet").stages(4)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.VEGETABLE)
            .wild(10, 10, "beaches and plains").wildOnSand()
            .uses("A sweet white root", "Boil it with water in the Brew Kettle for sugar"));
    public static final Crop ONION = add(Crop.row("onion", "Onion").stages(4).selfPlanting()
            .climate(Climate.TEMPERATE).kind(Crop.Kind.VEGETABLE)
            .wild(10, 12, "plains and meadows")
            .uses("A pungent bulb", "Cook it into soups and stews, or plant it"));
    public static final Crop GARLIC = add(Crop.row("garlic", "Garlic").stages(4).selfPlanting()
            .climate(Climate.TEMPERATE).kind(Crop.Kind.VEGETABLE)
            .wild(10, 12, "plains and hills")
            .uses("A bulb of strong cloves", "Pickle with it (pickles, kimchi), or plant it"));
    public static final Crop CABBAGE = add(Crop.row("cabbage", "Cabbage").stages(4)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.VEGETABLE)
            .wild(10, 12, "beaches and plains").wildOnSand()
            .uses("A firm head of leaves", "Cook it into borscht"));
    public static final Crop GINGER = add(Crop.row("ginger", "Ginger").stages(4).selfPlanting()
            .climate(Climate.WARM).kind(Crop.Kind.VEGETABLE)
            .wild(10, 8, "jungles")
            .uses("A hot, knobbly root", "Make kimchi, or plant it"));

    // --- Spices ---
    public static final Crop CORIANDER = add(Crop.row("coriander", "Coriander").stages(4)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.SPICE)
            .wild(10, 12, "plains and savannas")
            .uses("Fresh, bright leaves", "Season pickles in a jar"));
    public static final Crop ANISE = add(Crop.row("anise", "Anise").stages(4)
            .climate(Climate.WARM).kind(Crop.Kind.SPICE)
            .wild(10, 12, "savannas and plains")
            .uses("Sweet, licorice-scented seed heads", "A botanical for gin; steep it into bitters or herbal liqueur"));

    // --- Bushes: picked again and again (berries plant themselves) ---
    public static final Crop BLUEBERRY = add(Crop.row("blueberry", "Blueberry").bush()
            .produce("blueberries", "Blueberries").selfPlanting().food(2, 0.1F)
            .climate(Climate.COLD).kind(Crop.Kind.BERRY)
            .wild(6, 10, "taiga and groves")
            .uses("Sweet, juicy berries", "Eat them, cook jam, or plant a bush"));
    public static final Crop BLACKBERRY = add(Crop.row("blackberry", "Blackberry").bush().thorny()
            .produce("blackberries", "Blackberries").selfPlanting().food(2, 0.1F)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.BERRY)
            .wild(6, 10, "forests")
            .uses("Tart berries from a thorny bush", "Eat them, cook jam, or plant a bush"));
    public static final Crop ELDERBERRY = add(Crop.row("elderberry", "Elderberry").bush()
            .produce("elderberries", "Elderberries").selfPlanting()
            .flowers("elderflowers", "Elderflowers", "Picked from a flowering elder", "Cook cordial with sugar and a lemon")
            .climate(Climate.TEMPERATE).kind(Crop.Kind.BERRY)
            .wild(6, 12, "meadows and forests")
            .uses("Dark berries, best cooked", "Cook jam, or plant a bush"));
    public static final Crop JUNIPER = add(Crop.row("juniper", "Juniper").bush()
            .produce("juniper_berries", "Juniper Berries").selfPlanting()
            .climate(Climate.COLD).kind(Crop.Kind.BERRY)
            .wild(6, 10, "taiga, snowy slopes and hills")
            .uses("Piney, bitter berries", "Gin's must-have botanical: put it in a Gin Basket"));
    public static final Crop CRANBERRY = add(Crop.row("cranberry", "Cranberry").bush().waterEdge()
            .produce("cranberries", "Cranberries").selfPlanting().food(2, 0.1F)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.BERRY)
            .wild(20, 5, "swamps")
            .uses("Tart red bog berries", "Cook cranberry sauce, or plant a bush by water"));
    public static final Crop TOMATO = add(Crop.row("tomato", "Tomato").bush().food(3, 0.3F)
            .climate(Climate.WARM).kind(Crop.Kind.VEGETABLE)
            .wild(6, 12, "savannas and jungle edges")
            .uses("A ripe red fruit", "Eat it, or cook soup or chili con carne"));
    public static final Crop CHILI = add(Crop.row("chili", "Chili").bush()
            .produce("chili", "Chili Pepper")
            .climate(Climate.HOT).kind(Crop.Kind.VEGETABLE)
            .wild(6, 12, "savannas, badlands and jungles")
            .uses("A fiery little pepper", "Dry it, or cook chili con carne"));
    public static final Crop CUCUMBER = add(Crop.row("cucumber", "Cucumber").bush().food(3, 0.3F)
            .climate(Climate.WARM).kind(Crop.Kind.VEGETABLE)
            .wild(6, 12, "jungles")
            .uses("Cool and crisp", "Eat it, or pickle it in a jar with vinegar"));
    public static final Crop COFFEE = add(Crop.row("coffee", "Coffee").bush()
            .produce("coffee_beans", "Coffee Beans").selfPlanting()
            .climate(Climate.WARM).kind(Crop.Kind.SPICE)
            .wild(6, 12, "jungles and windswept hills")
            .uses("Green coffee beans", "Roast in a Kiln on Medium, or plant them"));

    // --- Desert succulents: slow, no water, cut down in flower ---
    public static final Crop AGAVE = add(Crop.row("agave", "Agave").succulent()
            .produce("agave_heart", "Agave Heart")
            .climate(Climate.HOT).kind(Crop.Kind.SPICE)
            .wild(5, 10, "deserts and badlands")
            .uses("The heart (piña) of a flowering agave", "Roast it in a Kiln on Medium, then press it"));

    // --- Grapes: climbing trellises, planted from cuttings, picked again and again ---
    public static final Crop RED_GRAPE = add(Crop.row("red_grape", "Red Grape").vine(2, 3, ModItems.GRAPE_LEAVES)
            .produce("red_grapes", "Red Grapes").food(2, 0.1F)
            .climate(Climate.WARM).kind(Crop.Kind.FRUIT)
            .wild(8, 10, "forest edges, plains and savannas")
            .uses("Dark, sweet grapes", "Stomp them for red wine, press them, or dry them"));
    public static final Crop WHITE_GRAPE = add(Crop.row("white_grape", "White Grape").vine(2, 3, ModItems.GRAPE_LEAVES)
            .produce("white_grapes", "White Grapes").food(2, 0.1F)
            .climate(Climate.TEMPERATE).kind(Crop.Kind.FRUIT)
            .wild(8, 10, "forest edges, plains and meadows")
            .uses("Pale green, crisp grapes", "Press them for white wine, or dry them"));

    // --- Herbs: cut back and regrowing ---
    public static final Crop MINT = add(Crop.row("mint", "Mint").herb()
            .climate(Climate.TEMPERATE)
            .wild(8, 10, "riverbanks and swamps")
            .uses("Cool, fresh leaves", "A gin botanical; steep it into herbal liqueur"));
    public static final Crop LAVENDER = add(Crop.row("lavender", "Lavender").herb()
            .climate(Climate.WARM)
            .wild(8, 10, "meadows, flower forests and cherry groves")
            .uses("Fragrant purple flowers", "A gin botanical; steep it into herbal liqueur"));
    public static final Crop WORMWOOD = add(Crop.row("wormwood", "Wormwood").herb()
            .climate(Climate.WARM)
            .wild(8, 10, "badlands and windswept hills")
            .uses("Bitter, silvery leaves", "Steep it into aromatic bitters; a gin botanical"));

    private static Crop add(Crop.Builder builder) {
        Crop crop = builder.register();
        ALL.add(crop);
        return crop;
    }

    public static List<Crop> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** Called from the mod constructor so the crops register before the registry events. */
    public static void init() {
    }

    private Crops() {}
}
