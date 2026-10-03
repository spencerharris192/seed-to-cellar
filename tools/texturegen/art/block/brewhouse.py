"""Brewhouse blocks: Brew Kettle (copper), Fermenting Vat (oak staves), Preserving Jar (glass).

Kettle: riveted copper sheet with a rolled band at the top; darker copper inside.
Vat: taller cousin of the malting tub, three iron hoops, a plank lid with a handle and a small
glass airlock.
Jar: tinted see-through glass with a rim; closed, a linen cover tied on with cord.
"""
from texturegen.palettes import CLOTH, COPPER, GLASS, GLASS_TINTED, IRON, WOOD_OAK

# 0-5 copper, a-f wood, i-n iron, w-z glass, S/E/B/G tinted glass (shadow, edge, body, glint),
# o-u cloth (letters must not overlap: texturegen.lint checks)
LEGEND = {
    **{str(i): COPPER[i] for i in range(6)},
    **dict(zip("abcdef", WOOD_OAK)),
    **dict(zip("ijklmn", IRON)),
    **dict(zip("wxyz", GLASS)),
    **dict(zip("SEBG", GLASS_TINTED)),
    **dict(zip("opqrtu", CLOTH)),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def kettle_side(x, y):
    if y == 4:               # rolled band; its row is also the rim's top face in the model,
        return "5" if x % 6 == 2 else "4"   # so bright copper with small glints, not all-highlight
    if y == 5:
        return "2"
    if y == 6:
        return "1"
    if y in (8, 13) and x % 4 == 1:
        return "5"           # rivet heads
    if y in (9, 14) and x % 4 == 1:
        return "1"           # rivet shadows
    if y == 15:
        return "1"
    if x % 8 == 7:
        return "2"           # sheet seams
    return "4" if x % 8 == 0 else "3"


def kettle_inner(x, y):
    return "1" if x % 8 == 7 else "2"


def kettle_bottom(x, y):
    if x in (0, 15) or y in (0, 15):
        return "1"
    if (x in (2, 13) and y % 4 == 2) or (y in (2, 13) and x % 4 == 2):
        return "4"  # rivet ring
    return "2"


STAVE = "edcb"
STAVE_INNER = "cbba"


def vat_side(x, y):
    # hoops match the malting tub's iron so the woodwork reads as one family
    if y in (3, 9, 14):
        return "m" if x % 5 == 2 else "l"
    if y in (4, 10, 15):
        return "k" if x % 5 == 2 else "j"
    return STAVE[x % 4]


def vat_inner(x, y):
    return STAVE_INNER[x % 4]


def vat_lid(x, y):
    if x in (0, 15) or y in (0, 15):
        return "b"
    if y in (5, 10):
        return "j" if x % 5 else "l"   # iron straps across the planks
    return "d" if y % 5 == 1 else ("b" if y % 5 == 4 else "c")


def airlock(x, y):
    # The vat's airlock is a 1-pixel glass tube: each face samples one column of this
    # left-to-right shading (lit face brightest, far side darkest).
    return "zyxw"[x % 4]


# Preserving Jar glass, a small atlas (the model samples each region 1:1):
#   body side 8x9 at (0,0) · shoulder side 6x1 at (0,10) · rim side 7x1 at (0,12)
#   base/top 8x8 at (8,0) · rim top 7x7 at (8,8), a ring around the open mouth
# Translucent (GLASS_TINTED): faint tint all over, glint on the lit left, darker shadow edge
# on the right and a thicker base, so it reads as a glass body you can see the contents in.
JAR_GLASS = """
EEEEEEESEEEEEEEE
EGGBBBBSEBBBBBBE
EGGBBBBSEBBBBBBE
EGBBBBBSEBBBBBBE
EGBBBBBSEBBBBBBE
EGBBBBBSEBBBBBBE
EGBBBBBSEBBBBBBE
EBBBBBBSEEEEEEEE
SSSSSSSSEGGGEEE.
........G.....E.
EGEEES..G.....E.
........E.....E.
EGGEEES.E.....S.
........E.....S.
........EESSSSS.
................
"""

# Linen cover tied over the jar's mouth (Preserving Jar, closed):
#   top 8x8 at (0,0): stretched cloth, lit along the top-left edges, shaded bottom-right,
#                     with a small dip in the middle where it sags into the jar's mouth
#   skirt side 8x3 at (0,8): cloth over the rim, the tied cord, then the pleated frill
JAR_LID = """
uuuuuuut........
uttttttr........
uttttttr........
utttrttr........
uttrrttr........
uttttttr........
uttttttr........
trrrrrrq........
ttttttrr........
pppppppo........
trtrtrrq........
................
................
................
................
................
"""


TEXTURES = {
    "brew_kettle_side": rows(kettle_side),
    "brew_kettle_inner": rows(kettle_inner),
    "brew_kettle_bottom": rows(kettle_bottom),
    "fermenting_vat_side": rows(vat_side),
    "fermenting_vat_inner": rows(vat_inner),
    "fermenting_vat_lid": rows(vat_lid),
    "fermenting_vat_airlock": rows(airlock),
    "preserving_jar_glass": JAR_GLASS,
    "preserving_jar_lid": JAR_LID,
}

COMPARE = ["block/cauldron_side", "block/barrel_side", "block/glass", "block/copper_block"]
