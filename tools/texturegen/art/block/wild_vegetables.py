"""Wild vegetables and spices (cross models), built from each crop's own leaves so players
connect them, plus the flower each one shows in the wild:

- wild sugar beet (sea beet): a rosette with the cream root showing
- wild onion: tube leaves and a round purple allium flower on a tall stalk
- wild garlic: strap leaves and a cluster of white star flowers
- wild cabbage: a loose rosette and a spike of yellow flowers
- wild ginger: reedy stems and a red flower cone at the base
- wild coriander and wild anise: the flowering plant
"""
from texturegen.compose import blank, plants, recolor, sibling, stamp, stamp_bottom, to_grid
from texturegen.palettes import CORN, FLOUR, FOLIAGE_CABBAGE, FOLIAGE_CROP, FOLIAGE_DUSTY, FOLIAGE_FRESH, GARLIC, \
    OAT, ONION, ROOT, SORGHUM, SUGAR_BEET

# One legend for all seven. Leaves: q-y FOLIAGE_CROP, abcdsz FOLIAGE_DUSTY, g-l FOLIAGE_FRESH,
# m-p FOLIAGE_CABBAGE (dark -> light, 4 shades). Others: 0-5 SUGAR_BEET, A-F GARLIC,
# G-L ONION, M-R ROOT, S-V FLOUR (white, 4 lightest), W-Z CORN (yellow), 6-9 SORGHUM (red),
# f, u, v OAT (cream).
LEGEND = {
    **dict(zip("qwerty", FOLIAGE_CROP)),
    **dict(zip("abcdsz", FOLIAGE_DUSTY)),
    **dict(zip("ghijkl", FOLIAGE_FRESH)),
    **dict(zip("mnop", FOLIAGE_CABBAGE[2:])),
    **{str(i): SUGAR_BEET[i] for i in range(6)},
    **dict(zip("ABCDEF", GARLIC)),
    **dict(zip("GHIJKL", ONION)),
    **dict(zip("MNOPQR", ROOT)),
    **dict(zip("STUV", FLOUR[2:])),
    **dict(zip("WXYZ", CORN[2:])),
    **dict(zip("6789", SORGHUM[1:5])),
    **dict(zip("fuv", OAT[2:5])),
}

_beet = sibling(__file__, "sugar_beet_crop")
_onion = sibling(__file__, "onion_crop")
_garlic = sibling(__file__, "garlic_crop")
_cabbage = sibling(__file__, "cabbage_crop")
_ginger = sibling(__file__, "ginger_crop")
_coriander = sibling(__file__, "coriander_crop")
_anise = sibling(__file__, "anise_crop")

# Each crop file's leaf letters, moved onto this file's ramps.
DUSTY = {"q": "a", "w": "b", "e": "c", "r": "d", "t": "s", "y": "z"}
FRESH = {"q": "g", "w": "h", "e": "i", "r": "j", "t": "k", "y": "l"}
CABBAGE = {"e": "m", "r": "n", "t": "o", "y": "p"}

ALLIUM = """
.BCB.
CDEDC
BEFEB
.CDC.
..b..
"""

STAR_FLOWERS = """
T.U.T
.VUV.
U.V.U
.c.c.
..c..
"""

YELLOW_SPIKE = """
.Z.
YZY
.Y.
ZXZ
.c.
.c.
"""

RED_CONE = """
.8.
898
787
.6.
"""


def wild_sugar_beet():
    return to_grid(plants(_beet.RIPE, (8,)))


def wild_onion():
    c = plants(recolor(_onion.MID, DUSTY), (4, 12))
    stamp(c, ALLIUM, 6, 1)
    for y in range(6, 15):
        c[y][8] = "d"
    return to_grid(c)


def wild_garlic():
    c = plants(recolor(recolor(_garlic.MID, _garlic.DARKER), DUSTY), (4, 12))
    stamp(c, STAR_FLOWERS, 6, 3)
    for y in range(8, 15):
        c[y][8] = "c"
    return to_grid(c)


def wild_cabbage():
    c = plants(recolor(_cabbage.YOUNG, CABBAGE), (4, 12))
    stamp(c, YELLOW_SPIKE, 7, 2)
    for y in range(8, 14):
        c[y][8] = "c"
    return to_grid(c)


def wild_ginger():
    c = plants(recolor(_ginger.MID, FRESH), (5, 11))
    stamp_bottom(c, RED_CONE, 7, 15)
    return to_grid(c)


def wild_coriander():
    sprite = recolor(_coriander.RIPE, {**FRESH, "5": "V", "p": "C"})
    return to_grid(plants(sprite, (4, 12)))


def wild_anise():
    sprite = recolor(_anise.RIPE, {**FRESH, "3": "f", "4": "u", "5": "v"})
    return to_grid(plants(sprite, (8,)))


TEXTURES = {
    "wild_sugar_beet": wild_sugar_beet(),
    "wild_onion": wild_onion(),
    "wild_garlic": wild_garlic(),
    "wild_cabbage": wild_cabbage(),
    "wild_ginger": wild_ginger(),
    "wild_coriander": wild_coriander(),
    "wild_anise": wild_anise(),
}

COMPARE = ["block/grass", "block/allium", "block/dandelion"]
