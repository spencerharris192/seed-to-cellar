"""Sake, coffee, lager and wheat beer ingredients (Phase 6.6), drawn from the families they belong to:

- lager_yeast: the ale-yeast vial (brewhouse_items.py) with a pale, icy-grey slurry: yeast grown in the cold.
- green_wheat_malt / wheat_malt / wheat_grist: the malt family (malts.py) in pale wheat straw; the green malt sprouts.
- polished_rice: the kernel heap in milled white.
- rice_bran: the grist heap in tan bran flakes.
- steamed_rice: a soft white mound of sticky rice, glistening on top.
- koji_rice: the same mound furred with pale yellow-green koji.
- roasted_coffee: a handful of fat, glossy dark-brown beans, each with its crease.
- ground_coffee: the milled-grain sack (wheat_flour.py) heaped with dark coffee grounds.
"""
from texturegen.compose import sibling
from texturegen.palettes import CLOTH, FLOUR, FOLIAGE_CROP, GLASS, MALT_AMBER, MALT_BLACK, OAT, PEAR, RICE, WOOD_OAK

_malts = sibling(__file__, "malts")
_brewhouse = sibling(__file__, "brewhouse_items")
_flour = sibling(__file__, "wheat_flour")

# s-x wheat (oat straw); A-F white rice (flour); G-L bran (rice); g h j k vial glass; a-d cork; m n o p icy slurry;
# q r shoots; t u rootlets; 0-5 coffee (malt black, amber glint); M-R sack cloth; S-V koji
LEGEND = {
    **dict(zip("sTUVWX", OAT)),
    **dict(zip("ABCDEF", FLOUR)),
    **dict(zip("GHIJKL", RICE)),
    **dict(zip("ghjk", GLASS)),
    **dict(zip("abcd", WOOD_OAK[2:6])),
    "m": GLASS[1], "n": GLASS[2], "o": FLOUR[4], "p": FLOUR[5],
    "q": FOLIAGE_CROP[3], "r": FOLIAGE_CROP[4],
    "t": FLOUR[3], "u": FLOUR[4],
    **{str(i): MALT_BLACK[i] for i in range(5)}, "5": MALT_AMBER[3],
    **dict(zip("MNOPQR", CLOTH)),
    "S": PEAR[3], "Y": PEAR[4], "Z": PEAR[5],
}

WHEAT = "sTUVWX"


def lager_yeast() -> str:
    # the vial's cork letters (a-f in its file) onto ours; the slurry (v w x y) onto the icy greys
    table = {"c": "b", "d": "c", "e": "d", "b": "a", "v": "m", "w": "n", "x": "o", "y": "p"}
    return "".join(table.get(ch, ch) for ch in _brewhouse.ALE_YEAST)


def green_wheat_malt() -> str:
    rows = [list(r) for r in _malts.recolor(_malts.malt("012345"), "TUVWXX").split("\n")]
    for x, y, ch in [(4, 12, "u"), (3, 13, "t"), (8, 12, "u"), (13, 12, "u"), (14, 13, "t"), (1, 10, "u"), (12, 8, "u")]:
        rows[y][x] = ch
    for x, y, ch in [(6, 2, "q"), (7, 1, "r"), (9, 2, "q"), (10, 1, "r"), (3, 6, "q"), (2, 5, "r")]:
        rows[y][x] = ch
    return "\n".join("".join(r) for r in rows)


STEAMED_RICE = """
................
................
................
................
................
................
.....DEEEED.....
....DEFEFEEC....
...DEFEEEFEEC...
..CDEEFEEEEEDC..
..CDDEEEDEEDCC..
..BCDDEDDDDCCB..
...BCCDDDCCCB...
....BBCCCCBB....
................
................
"""

# The same mound with koji's fuzz: pale yellow-green speckles over the rice.
KOJI_RICE = (STEAMED_RICE.replace("DEFEFEEC", "DSZEYEZC").replace("DEFEEEFEEC", "DEYSEEYEZC")
             .replace("CDEEFEEEEEDC", "CDSEZEYESEDC").replace("CDDEEEDEEDCC", "CDYEESDEZDCC"))

ROASTED_COFFEE = """
................
................
................
................
......2443......
.....234432.....
.....340043.....
..2443243422....
.234432344321...
.3400433324432..
.234432234433...
..2332..3443....
.......234432...
.......340043...
.......234432...
........2332....
"""


def ground_coffee() -> str:
    # the milled-grain sack with its heap's digits 0-5 as coffee grounds (cloth letters A-F there are M-R here)
    cloth = dict(zip("ABCDEF", "MNOPQR"))
    grounds = "001234"
    out = []
    for ch in _flour.GRID:
        out.append(grounds[int(ch)] if ch.isdigit() else cloth.get(ch, ch))
    return "".join(out)


TEXTURES = {
    "lager_yeast": lager_yeast(),
    "green_wheat_malt": green_wheat_malt(),
    "wheat_malt": _malts.recolor(_malts.malt("012345"), WHEAT),
    "wheat_grist": _malts.recolor(_malts.GRIST, WHEAT),
    "polished_rice": _malts.recolor(_malts.malt("012345"), "BCDEFF"),
    "rice_bran": _malts.recolor(_malts.GRIST, "GHIJKL"),
    "steamed_rice": STEAMED_RICE,
    "koji_rice": KOJI_RICE,
    "roasted_coffee": ROASTED_COFFEE,
    "ground_coffee": ground_coffee(),
}

COMPARE = ["item/wheat_seeds", "item/sugar", "item/cocoa_beans"]
