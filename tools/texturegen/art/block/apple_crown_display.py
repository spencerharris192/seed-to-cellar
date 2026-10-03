"""Apple Crown Whiskey's bottle on the shelves (model block/display/apple_crown_whiskey and its _crowned twin): each face
takes the whole texture, stretched to fit, so these are built to read at any size.

- display_gold: polished gold for the crown, its orb and cross, and the label's bands: lit along the top and left, a glint
  slanting across it, a few sparkles, shaded toward the bottom and right.
- display_ruby / display_emerald: faceted gems for the crown's band: a bright highlight in the upper left, deep at the lower
  right, a dark rim.
- display_velvet: the deep red velvet cap inside the crown, its nap catching the light here and there.
- display_leaf: the apple leaf at the neck: green, a pale midrib along its length, veins slanting off it, darker edges.
  display_gold_leaf: the same leaf in gold, for the crowned bottle.
- display_crown_emblem: the little crown on the label (cut out): three points with pearls, a band set with rubies. Drawn
  tall, since the label squashes it to 2 by 1.2 pixels. display_crown_emblem_red: the same crown in red, for the crowned
  bottle's gold label.
"""
from texturegen.palettes import FOLIAGE_FRESH, FRUIT_RED, GOLD

# A-F gold; r-w red; L-P leaf green
LEGEND = {
    **dict(zip("ABCDEF", GOLD)),
    **dict(zip("rstuvw", FRUIT_RED)),
    **dict(zip("LMNOP", FOLIAGE_FRESH[1:6])),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def gold(x, y):
    if x == 15 or y == 15:
        return "B"                                       # shaded edge
    if x == 0 or y == 0:
        return "E"                                       # lit edge
    if x + y in (6, 7):
        return "F"                                       # the glint slanting across
    if (x * 7 + y * 5) % 29 == 0:
        return "F"                                       # sparkles
    return "D" if y < 8 else "C"


def gem(dark, deep, body, light, bright):
    def face(x, y):
        if x in (0, 15) or y in (0, 15):
            return dark                                  # the setting's shadow round it
        if x + y < 7:
            return bright if x + y < 4 else light        # the highlight, upper left
        if x + y > 22:
            return deep
        return body
    return face


def velvet(x, y):
    if y < 2:
        return "u"                                       # light along its top
    return "u" if (x * 3 + y * 5) % 7 == 0 else ("s" if (x + y * 2) % 9 == 0 else "t")


def leaf(dark, mid, main, light, rib):
    def face(x, y):
        if y in (7, 8):
            return rib                                   # the midrib, along its length
        if y in (0, 15):
            return dark                                  # its edges
        if (x + abs(y * 2 - 15) // 2) % 5 == 0:
            return mid                                   # veins slanting off the rib
        return light if y < 7 else main                  # lit on its upper half
    return face


EMBLEM = """
................
.F.....FF.....F.
.E.....EE.....E.
.D.....DD.....D.
.DD...DDDD...DD.
.DD...DDDD...DD.
.DDD..DDDD..DDD.
.DDDD.DDDD.DDDD.
.DDDDDDDDDDDDDD.
.CCCCCCCCCCCCCC.
.EEEEEEEEEEEEEE.
.DDvDDDvvDDDvDD.
.DDvDDDvvDDDvDD.
.CCCCCCCCCCCCCC.
.BBBBBBBBBBBBBB.
................
"""

# The same crown in red, its jewels gold.
EMBLEM_RED = EMBLEM.translate(str.maketrans({"F": "w", "E": "v", "D": "u", "C": "t", "B": "s", "v": "E"}))

TEXTURES = {
    "display_gold": rows(gold),
    "display_ruby": rows(gem("r", "s", "u", "v", "w")),
    "display_emerald": rows(gem("L", "M", "N", "O", "P")),
    "display_velvet": rows(velvet),
    "display_leaf": rows(leaf("L", "M", "N", "O", "P")),
    "display_gold_leaf": rows(leaf("B", "C", "D", "E", "F")),
    "display_crown_emblem": EMBLEM,
    "display_crown_emblem_red": EMBLEM_RED,
}

COMPARE = ["block/gold_block", "item/golden_apple"]
