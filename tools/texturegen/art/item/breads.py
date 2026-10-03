"""Breads and bakes from the Kitchen (GDD section 15). Each has its own shape, so none is
mistaken for vanilla bread (a long pale loaf) or for another:

- rye_bread: a pointed dark loaf (batard) with three overlapping cuts along it, dusted with flour.
- sourdough_bread: a tall round boule, golden-brown, one curved "ear" score, a dusting of flour.
- spent_grain_bread: a round-ended loaf studded with pale grains.
- beer_bread: a tin loaf: straight pan-baked sides under a glossy domed top.
- cornbread: a square slab: brown baked top, yellow crumb face with little holes.
- tortilla: a thin pale disc with scorch spots.
Domed shapes come from the shared _shapes helper (lit from the top-left like vanilla foods).
"""
from texturegen.compose import sibling
from texturegen.palettes import BARLEY, CORN, FLOUR, MALT_AMBER, MALT_BLACK, OAT, ROOT, RYE

shapes = sibling(__file__, "_shapes")

# Ramps (rim, dark -> light): a-e rye crust, f-j golden crust, k-o tan crust, p-t corn, u-z tortilla;
# A-C crumb and flour, G grain, x X scorch
LEGEND = {
    **dict(zip("abcde", MALT_BLACK[1:6])),
    **dict(zip("fghij", [MALT_AMBER[0], MALT_AMBER[1], MALT_AMBER[2], MALT_AMBER[3], MALT_AMBER[4]])),
    **dict(zip("klmno", [MALT_AMBER[1], MALT_AMBER[2], MALT_AMBER[3], BARLEY[4], BARLEY[5]])),
    **dict(zip("pqrst", CORN[1:6])),
    **dict(zip("uvwyz", [OAT[2], OAT[3], OAT[4], OAT[5], FLOUR[5]])),
    "A": RYE[4],
    "B": OAT[4],
    "C": FLOUR[4],
    "G": BARLEY[5],
    "x": ROOT[1],
    "X": ROOT[3],
}


def rye_bread() -> str:
    g = shapes.shade(shapes.dome(8.0, 8.4, 7.2, 3.9, angle=-35, height=0.8), "abcde")
    # baker's cuts run along the loaf, each a little off its axis and overlapping the next: the
    # opened cut shows pale crumb over a dark lip
    for x, y in ((4, 11), (7, 8), (10, 5)):
        shapes.put(g, [(x, y), (x + 1, y), (x + 2, y - 1)], "A")
        shapes.put(g, [(x, y + 1), (x + 1, y + 1), (x + 2, y)], "a", only_on="bcde")
    shapes.put(g, [(6, 7), (9, 4), (4, 9), (11, 7)], "C", only_on="de")   # a little flour dust
    return shapes.to_str(g)


def sourdough_bread() -> str:
    g = shapes.shade(shapes.dome(8.0, 8.8, 6.4, 5.6, height=1.1), "fghij")
    shapes.put(g, [(4, 8), (5, 7), (6, 6), (7, 6), (8, 6), (9, 7)], "B")   # the ear
    shapes.put(g, [(5, 8), (7, 7), (9, 8)], "g")                         # shadow under it
    shapes.put(g, [(5, 5), (8, 4), (4, 6), (10, 5), (7, 5)], "C")       # flour dusting
    return shapes.to_str(g)


def spent_grain_bread() -> str:
    g = shapes.shade(shapes.dome(8.0, 8.6, 7.0, 4.6, angle=-20, height=0.9), "klmno")
    shapes.put(g, [(5, 6), (8, 5), (11, 6), (4, 9), (7, 8), (10, 8), (12, 9), (6, 11), (9, 11)], "G")
    return shapes.to_str(g)


BEER_BREAD = """
................
................
................
................
....ffffffff....
..fghiijjjiihgf.
.fghijjjjjjjihgf
.fghiijjjjjiihgf
.fghhiiiiiiihhgf
.fggghhhhhhhgggf
.fggghhhhhhhgggf
.fgggghhhhhggggf
.ffgggggggggggff
..ffffffffffff..
................
................
"""

CORNBREAD = """
................
................
................
................
....ffffffff....
...fgghgghhggf..
..fghhhihhhhhgf.
..fghhihhhhihgf.
..fgggggggggggf.
..fsttstsstttsf.
..fsstttstttssf.
..fsttssttstssf.
..fssssssssssrf.
..ffffffffffff..
................
................
"""


def tortilla() -> str:
    g = shapes.shade(shapes.dome(8.0, 8.6, 7.0, 5.4, height=0.3), "uvwyz")
    shapes.put(g, [(5, 6), (11, 8), (7, 10), (10, 12)], "X")   # a few scorch marks from the griddle
    shapes.put(g, [(6, 6), (11, 9)], "x")
    return shapes.to_str(g)


TEXTURES = {
    "rye_bread": rye_bread(),
    "sourdough_bread": sourdough_bread(),
    "spent_grain_bread": spent_grain_bread(),
    "beer_bread": BEER_BREAD,
    "cornbread": CORNBREAD,
    "tortilla": tortilla(),
}

COMPARE = ["item/bread", "item/cookie", "item/baked_potato"]
