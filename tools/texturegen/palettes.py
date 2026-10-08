"""Shared named palettes for every Seed to Cellar texture.

Rules (GDD section 20):
- Each ramp runs darkest -> lightest, 4-8 shades.
- Hue-shifted: shadows move cooler and more saturated, highlights warmer and lighter.
  For warm materials the shadow path runs gold -> amber -> red-brown -> purple-brown.
- Art files may ONLY use colors from this file (core.render enforces it), so the
  whole mod stays coherent. Add a new ramp here rather than an ad-hoc color.
"""

# --- Grains -----------------------------------------------------------------

# Ripe barley: warmer and lighter than vanilla wheat's khaki, so the two read apart.
BARLEY = [
    "#3d2530",  # 0 deep plum-brown (darkest accents, never pure black)
    "#6b3f2c",  # 1 red-brown shadow
    "#9a6432",  # 2 amber
    "#c48f40",  # 3 gold
    "#e0b75e",  # 4 light gold
    "#f1d88c",  # 5 pale straw highlight
    "#fbefc4",  # 6 hot highlight (sparingly)
]

# Straw and thatch: dry, pale stalks; browner shadows than barley so a thatch roof isn't a hay bale.
STRAW = [
    "#43301a",  # 0
    "#6f5230",  # 1
    "#9a7843",  # 2
    "#bf9d58",  # 3
    "#dcc07b",  # 4
    "#efdca6",  # 5
]

# Ripe rye: cooler, greyer gold than barley (rye's heads are dusty grey-brown), shadows
# toward grey-plum.
RYE = [
    "#3a3136",  # 0
    "#5e5049",  # 1
    "#86735f",  # 2
    "#aa9677",  # 3
    "#c9b791",  # 4
    "#e3d6b4",  # 5
]

# Ripe oats: the palest grain, almost cream, so hanging oat bells glow against the leaves.
OAT = [
    "#4a3a2c",  # 0
    "#75603f",  # 1
    "#a08a58",  # 2
    "#c4b07a",  # 3
    "#ddd0a0",  # 4
    "#f0e8c8",  # 5
]

# Corn kernels: deep golden yellow, shadows toward burnt orange, highlights toward butter.
CORN = [
    "#3f2410",  # 0
    "#7a4414",  # 1
    "#b8701a",  # 2
    "#e0a124",  # 3
    "#f4c842",  # 4
    "#fbe487",  # 5
]

# Ripe rice panicles: golden straw with a green-olive shadow (paddy rice), lighter than barley.
RICE = [
    "#3a3522",  # 0
    "#6a5c2c",  # 1
    "#9a8638",  # 2
    "#c6ae4c",  # 3
    "#e2cd72",  # 4
    "#f3e6ab",  # 5
]

# Sorghum heads: rust to red-brown, shadows toward wine-purple, highlights toward copper.
SORGHUM = [
    "#2f1519",  # 0
    "#54211c",  # 1
    "#7c3220",  # 2
    "#a3492a",  # 3
    "#c56a3a",  # 4
    "#de9459",  # 5
]


# --- Vegetables -----------------------------------------------------------------

# Sugar beet root: cream, shadows toward mauve-grey.
SUGAR_BEET = [
    "#4d3a3a",  # 0
    "#7c6260",  # 1
    "#a8908a",  # 2
    "#cbb7ae",  # 3
    "#e5d8cf",  # 4
    "#f6eee8",  # 5
]

# Beetroot flesh (borscht, pickled beets): deep crimson-magenta, apart from red fruit's orange-reds.
BEET = [
    "#2a0a18",  # 0
    "#4f1230",  # 1
    "#7a1c45",  # 2
    "#a42a5a",  # 3
    "#c84f7a",  # 4
    "#e184a3",  # 5
]

# Onion skin: papery golden brown, shadows toward red-brown.
ONION = [
    "#3b1d12",  # 0
    "#6b3419",  # 1
    "#9d5422",  # 2
    "#c77a33",  # 3
    "#e0a558",  # 4
    "#f1cf93",  # 5
]

# Garlic: white bulbs with purple streaks; also allium (wild onion) flowers.
GARLIC = [
    "#4b3550",  # 0
    "#785f80",  # 1
    "#a490a8",  # 2
    "#cbbfcd",  # 3
    "#e8e1e8",  # 4
    "#faf7f9",  # 5
]

# --- Fruit ------------------------------------------------------------------------

# Blueberries (and frosted juniper berries): dusty blue, highlights toward powder blue.
BLUEBERRY = [
    "#1f2440",  # 0
    "#2e3a6b",  # 1
    "#44579a",  # 2
    "#6078bf",  # 3
    "#8aa0d8",  # 4
    "#b8c8ec",  # 5
]

# Blackberries and elderberries: glossy purple-black, never pure black.
BLACKBERRY = [
    "#150d1a",  # 0
    "#261630",  # 1
    "#3b2148",  # 2
    "#553066",  # 3
    "#784a8a",  # 4
    "#a57bb2",  # 5
]

# Red fruit (tomato, chili, coffee cherries, cranberries): shadows toward wine, highlights toward coral.
FRUIT_RED = [
    "#3a0f12",  # 0
    "#6b1a1a",  # 1
    "#9c2a22",  # 2
    "#c7432c",  # 3
    "#e06a45",  # 4
    "#f29a73",  # 5
]

# Red grapes: deep wine red (redder than plums, darker and cooler than cherries), shadows toward purple-black,
# a dusty rose bloom on the highlights.
RED_GRAPE = [
    "#1c0a18",  # 0
    "#38102a",  # 1
    "#58183c",  # 2
    "#78244c",  # 3
    "#963a5e",  # 4
    "#b46c84",  # 5 bloom
]

# White (green) grapes: yellow-green, shadows toward olive, warm golden highlights.
WHITE_GRAPE = [
    "#343c16",  # 0
    "#56631f",  # 1
    "#7c8b2b",  # 2
    "#a3b43e",  # 3
    "#c8d466",  # 4
    "#e9ec9f",  # 5
]

# --- Orchard fruit ---------------------------------------------------------------

# Plums: purple with a dusty blue bloom, shadows toward wine-black.
PLUM = [
    "#1f0f24",  # 0
    "#3b1840",  # 1
    "#5c2560",  # 2
    "#7e3a7c",  # 3
    "#9e5a98",  # 4
    "#b98fc2",  # 5 bloom
]

# Peaches: warm apricot skin with a red blush, shadows toward rose-brown.
PEACH = [
    "#5a2218",  # 0
    "#9a3f24",  # 1
    "#d0643a",  # 2
    "#ef9254",  # 3
    "#f8bb7c",  # 4
    "#fde0b0",  # 5
]

# Pears: golden green, shadows toward olive-brown, warm yellow highlights.
PEAR = [
    "#3e3a14",  # 0
    "#6a6420",  # 1
    "#9a9430",  # 2
    "#c4c046",  # 3
    "#e0dc70",  # 4
    "#f4f0a8",  # 5
]

# Lemons: clear yellow, shadows toward ochre.
LEMON = [
    "#5a4410",  # 0
    "#997414",  # 1
    "#d4ac1c",  # 2
    "#f0d23a",  # 3
    "#fbe870",  # 4
    "#fff6b4",  # 5
]

# Oranges: deep orange, shadows toward burnt red-brown.
ORANGE = [
    "#5a240c",  # 0
    "#9a4410",  # 1
    "#d46a16",  # 2
    "#f08e24",  # 3
    "#fab04c",  # 4
    "#fdd28a",  # 5
]

# Olives: green ripening to purple-black, dull and waxy.
OLIVE = [
    "#1c1a14",  # 0
    "#35301e",  # 1
    "#56552a",  # 2
    "#7a7c38",  # 3
    "#9fa052",  # 4
    "#c2c07e",  # 5
]

# Agave: a waxy blue-grey green, shadows toward deep teal, highlights toward a warm pale sage.
AGAVE = [
    "#16272e",  # 0
    "#24414a",  # 1
    "#365f62",  # 2
    "#4f807a",  # 3
    "#76a395",  # 4
    "#adc8b0",  # 5
]

# Dark green wine-bottle glass: deep bottle green, a pale glint.
BOTTLE_GLASS = [
    "#0c1a13",  # 0
    "#15301f",  # 1
    "#1f462d",  # 2
    "#2c6040",  # 3
    "#46805a",  # 4
    "#9cc8ac",  # 5 glint
]

# Fruit blossom: white petals blushing pink, golden centers.
BLOSSOM = [
    "#b86a86",  # 0 pink shadow
    "#e0a0b8",  # 1 pink
    "#f4d4e0",  # 2 blush
    "#fcf6f4",  # 3 white
    "#f4d24a",  # 4 golden center
]

# Lavender flowers.
LAVENDER = [
    "#2f2346",  # 0
    "#4a3570",  # 1
    "#6a4e9c",  # 2
    "#8c6fc2",  # 3
    "#b096dc",  # 4
    "#d6c4f0",  # 5
]

# --- Foliage ------------------------------------------------------------------

# Cabbage leaves: pale, waxy blue-green.
FOLIAGE_CABBAGE = [
    "#1e3a30",  # 0
    "#2f5a47",  # 1
    "#4b7d61",  # 2
    "#71a283",  # 3
    "#a0c6a6",  # 4
    "#d0e6cc",  # 5
]

# Young grain crops: fresh saturated green (sits beside vanilla crops), shadows toward
# teal, highlights toward yellow.
FOLIAGE_CROP = [
    "#143624",  # 0
    "#1c5427",  # 1
    "#27732b",  # 2
    "#3c9130",  # 3
    "#63ae36",  # 4
    "#9bcb4a",  # 5
]

# Dusty blue-green (glaucous) leaves: rye now; juniper, lavender, agave and olive later.
FOLIAGE_DUSTY = [
    "#1a2f2e",  # 0
    "#28473f",  # 1
    "#3b6452",  # 2
    "#568464",  # 3
    "#7ba37a",  # 4
    "#a8c49a",  # 5
]

# Leafy vines and herbs (hops leaves): fresher, cooler green than grain.
FOLIAGE_FRESH = [
    "#14332b",  # 0
    "#1e4f38",  # 1
    "#2b6f40",  # 2
    "#43914a",  # 3
    "#6db457",  # 4
    "#a2d474",  # 5
]

# Hop cones: pale lime to cream (lupulin).
HOP_CONE = [
    "#2a4428",  # 0
    "#476b34",  # 1
    "#6f9443",  # 2
    "#9bb958",  # 3
    "#c6d982",  # 4
    "#ecf1b9",  # 5
]

# --- Wood and roots ---------------------------------------------------------

# Oak-like wood for trellises and frames, tuned to sit beside vanilla oak planks.
WOOD_OAK = [
    "#3a281e",  # 0
    "#58402a",  # 1
    "#775a38",  # 2
    "#977648",  # 3
    "#b3915c",  # 4
    "#cfae78",  # 5
]

# The other eleven cask woods, same layout as WOOD_OAK (0 deep shadow, 2-4 the body, 5 highlight), each tuned to
# sit beside its vanilla planks: shadows run cooler and toward purple-brown, highlights warmer.
WOOD_SPRUCE = [
    "#2a1b1c",  # 0
    "#3f2a20",  # 1
    "#573b24",  # 2
    "#6f4f2e",  # 3
    "#896639",  # 4
    "#a6834e",  # 5
]

WOOD_BIRCH = [
    "#5a4a3e",  # 0
    "#80704f",  # 1
    "#a08f63",  # 2
    "#bcaa77",  # 3
    "#d6c68c",  # 4
    "#ece0ab",  # 5
]

WOOD_JUNGLE = [
    "#3b2322",  # 0
    "#5c3a2c",  # 1
    "#7c533a",  # 2
    "#9c6c4a",  # 3
    "#b8875f",  # 4
    "#d4a57d",  # 5
]

WOOD_ACACIA = [
    "#4a1f22",  # 0
    "#6d3024",  # 1
    "#8e4329",  # 2
    "#aa5532",  # 3
    "#c46c3f",  # 4
    "#db8b56",  # 5
]

WOOD_DARK_OAK = [
    "#150c0e",  # 0
    "#22150f",  # 1
    "#312012",  # 2
    "#432c17",  # 3
    "#573a1e",  # 4
    "#734f2a",  # 5
]

WOOD_MANGROVE = [
    "#2c0f18",  # 0
    "#47191f",  # 1
    "#622327",  # 2
    "#793130",  # 3
    "#8f4739",  # 4
    "#aa654d",  # 5
]

WOOD_CHERRY = [
    "#7b3f52",  # 0
    "#a65c6b",  # 1
    "#c67a80",  # 2
    "#dc9a97",  # 3
    "#e9b7b0",  # 4
    "#f4d5cb",  # 5
]

# Pale oak's near-white with a blush of pink, and poplar's grey taupe (26.3's woods).
WOOD_PALE_OAK = [
    "#6a5a5e",  # 0
    "#8f7f82",  # 1
    "#b3a3a4",  # 2
    "#cfc1c0",  # 3
    "#e5dbd8",  # 4
    "#f6f0ec",  # 5
]

WOOD_POPLAR = [
    "#3e3232",  # 0
    "#5a4b49",  # 1
    "#776a66",  # 2
    "#8f837b",  # 3
    "#a59a8c",  # 4
    "#bdb3a1",  # 5
]

# Nether stems: crimson's plum-magenta and warped's teal.
WOOD_CRIMSON = [
    "#200d20",  # 0
    "#38172e",  # 1
    "#4f223b",  # 2
    "#672d4b",  # 3
    "#813a5c",  # 4
    "#a05173",  # 5
]

WOOD_WARPED = [
    "#0b1c26",  # 0
    "#12343a",  # 1
    "#1a4c4d",  # 2
    "#246761",  # 3
    "#2f8276",  # 4
    "#4aa18c",  # 5
]

# Rhizomes, roots and bare soil-side stems: tan-brown with pale buds.
ROOT = [
    "#2e2127",  # 0
    "#523a2f",  # 1
    "#7b5840",  # 2
    "#a27c56",  # 3
    "#c6a378",  # 4
    "#e5cda4",  # 5
]

# Rich composted soil: darker and redder than vanilla dirt, so fertile ground stands out.
SOIL_RICH = [
    "#1d120c",  # 0
    "#2e1c12",  # 1
    "#42291a",  # 2
    "#583722",  # 3
    "#70472c",  # 4
    "#8a5b38",  # 5
]


# --- Processed grain ----------------------------------------------------------

# Amber malt / amber grist: toasted caramel-brown.
MALT_AMBER = [
    "#3a1e14",  # 0
    "#62321a",  # 1
    "#8c4d20",  # 2
    "#b36d2a",  # 3
    "#d19240",  # 4
    "#e8b565",  # 5
]

# Black (roasted) malt: near-black coffee browns, never pure black.
MALT_BLACK = [
    "#140c0d",  # 0
    "#241413",  # 1
    "#3a211a",  # 2
    "#553124",  # 3
    "#734635",  # 4
    "#94604a",  # 5
]

# Dried hops: papery olive-tan.
HOP_DRIED = [
    "#3a3320",  # 0
    "#5d532f",  # 1
    "#83753f",  # 2
    "#a89755",  # 3
    "#c6b673",  # 4
    "#e1d49a",  # 5
]

# Flour and rootlets: warm off-white, shadows toward cool grey.
FLOUR = [
    "#77798a",  # 0
    "#a09fa8",  # 1
    "#c4c0bd",  # 2
    "#ddd7cd",  # 3
    "#eee8dd",  # 4
    "#fbf8f0",  # 5
]

# Burlap sacks.
CLOTH = [
    "#3b2b22",  # 0
    "#5c4432",  # 1
    "#7e6045",  # 2
    "#9f805d",  # 3
    "#bfa07a",  # 4
    "#d9c19c",  # 5
]

# --- Materials ------------------------------------------------------------------

# Iron bands, doors and grates: neutral grey, slightly cool shadows, warm highlights.
IRON = [
    "#2c2b30",  # 0
    "#48464d",  # 1
    "#67656b",  # 2
    "#8a888c",  # 3
    "#b1afb0",  # 4
    "#dcdad7",  # 5
]

# Tool metals, tuned to sit beside vanilla's tool tiers without copying them.
GOLD = [
    "#4a2f0c",  # 0
    "#8a5a12",  # 1
    "#c48a17",  # 2
    "#e6b826",  # 3
    "#f5dc56",  # 4
    "#fff4a3",  # 5
]

DIAMOND = [
    "#0b2e2a",  # 0
    "#13574d",  # 1
    "#1f8f7c",  # 2
    "#2fbfa5",  # 3
    "#5fe3cb",  # 4
    "#bdf7ec",  # 5
]

NETHERITE = [
    "#1c1618",  # 0
    "#2e2528",  # 1
    "#433a3e",  # 2
    "#5b5156",  # 3
    "#766b71",  # 4
    "#978b91",  # 5
]

# Bricks and mortar, matched to sit seamlessly beside vanilla bricks.
BRICK = [
    "#5a2f28",  # 0
    "#733f31",  # 1
    "#8f503f",  # 2
    "#a35a46",  # 3
    "#bf6650",  # 4
    "#d27b62",  # 5
]
MORTAR = [
    "#6f5752",  # 0
    "#8b6e67",  # 1
    "#a2867d",  # 2
    "#b5a29a",  # 3
]

# Copper vessels (kettle, still): fresh copper, shadows toward deep red-brown.
COPPER = [
    "#3e1c17",  # 0
    "#6b3222",  # 1
    "#9a4a2c",  # 2
    "#c0663a",  # 3
    "#dc8c55",  # 4
    "#f2bb86",  # 5
]

# The same copper weathering (in the shade order of COPPER, so a drawing ages pixel for pixel): dulled, browner copper
# with a pinkish cast, then the blue-green verdigris that spreads over it in patches.
COPPER_EXPOSED = [
    "#3a2420",  # 0
    "#5d3a2f",  # 1
    "#7f523f",  # 2
    "#9e6b53",  # 3
    "#b98a70",  # 4
    "#d5ae95",  # 5
]
COPPER_PATINA = [
    "#1b3833",  # 0
    "#2a5a4e",  # 1
    "#3b7b68",  # 2
    "#4e9a80",  # 3
    "#6db99c",  # 4
    "#a2d9c0",  # 5
]

# Clear glass: only edges and glints are drawn; the rest is see-through.
GLASS = [
    "#5f7f86",  # 0
    "#8fb0b5",  # 1
    "#c3dcde",  # 2
    "#eef8f8",  # 3
]

# Tinted see-through glass for translucent models (#RRGGBBAA): the same hues as GLASS, so a
# jar reads as a solid glass body with contents visible through it, not as a wire frame.
GLASS_TINTED = [
    "#5f7f86d0",  # 0 shadowed edge (right side, base)
    "#8fb0b590",  # 1 edge / rim
    "#c3dcde30",  # 2 body: a faint tint
    "#eef8f8c8",  # 3 glint, lit from the left
]

# Stoneware crocks.
CLAY = [
    "#3f302b",  # 0
    "#634c42",  # 1
    "#88695a",  # 2
    "#a88a77",  # 3
    "#c7ad98",  # 4
    "#e0cdbb",  # 5
]

# Millstone rock: warm grey granite.
STONE = [
    "#343239",  # 0
    "#4e4b51",  # 1
    "#6b6769",  # 2
    "#898583",  # 3
    "#a8a39d",  # 4
    "#c6c0b7",  # 5
]

# Water in open vessels (not biome-tinted).
WATER = [
    "#1c2c64",  # 0
    "#243f8f",  # 1
    "#305cb5",  # 2
    "#457dd2",  # 3
    "#6fa2e4",  # 4
    "#a9cdf3",  # 5
]

# Firelight.
FIRE = [
    "#4e170b",  # 0
    "#902a0c",  # 1
    "#d0540f",  # 2
    "#ef8a1f",  # 3
    "#ffc24a",  # 4
    "#fff0a8",  # 5
]

# Shared grey liquid (tinted per fluid in game): light, low contrast.
LIQUID = [
    "#bdbdbd",  # 0
    "#cbcbcb",  # 1
    "#d8d8d8",  # 2
    "#e6e6e6",  # 3
    "#f3f3f3",  # 4
    "#ffffff",  # 5
]

# Vanilla GUI chrome (panel, bevels, slots) so our screens match the game's.
GUI = [
    "#000000",  # 0 outline
    "#373737",  # 1 slot shadow edge
    "#555555",  # 2 panel shadow
    "#8b8b8b",  # 3 slot fill / empty indicator
    "#c6c6c6",  # 4 panel
    "#ffffff",  # 5 highlight
]


def all_colors() -> set[str]:
    """Every color any texture is allowed to use."""
    colors: set[str] = set()
    for name, value in globals().items():
        if name.isupper() and isinstance(value, list):
            colors.update(c.lower() for c in value)
    return colors
