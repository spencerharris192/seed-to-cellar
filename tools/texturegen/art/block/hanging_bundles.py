"""The hanging bundles' parts (models hop_bundle, lavender_bundle, garlic_braid, chili_string and their _wall twins). Each
face takes the whole texture squeezed onto a small part, so these are bold, simple patterns that still read at 2 pixels:

- bundle_twine: twisted straw twine, the twist slanting across it. bundle_nail: a dark iron nail.
- bundle_hop: a hop cone: pale green bracts overlapping like scales, rows offset, lit along their tops.
  bundle_hop_bine: the bines' dry, dark green-brown cut ends. bundle_hop_leaf: a dried, faded leaf with a vein.
- bundle_lavender: lavender florets, dotted purple with lighter tips. bundle_lavender_stem: dusty grey-green stems.
- bundle_garlic: a garlic bulb: papery white, the cloves' seams running down it, a purple blush at the bottom.
  bundle_braid: the braided stalks, a chevron of straw.
- bundle_chili: a glossy red chili, a highlight down its left side. bundle_chili_cap: its green cap.
"""
from texturegen.palettes import FOLIAGE_CROP, FOLIAGE_DUSTY, FRUIT_RED, GARLIC, HOP_CONE, HOP_DRIED, IRON, LAVENDER, STRAW

# a-f straw; g-l dried hop; m-r lavender; s-x garlic; A-F red; G-L dusty green; M-R crop green; 0-5 iron; S-X hop cone
LEGEND = {
    **dict(zip("abcdef", STRAW)),
    **dict(zip("ghijkl", HOP_DRIED)),
    **dict(zip("mnopqr", LAVENDER)),
    **dict(zip("stuvwx", GARLIC)),
    **dict(zip("ABCDEF", FRUIT_RED)),
    **dict(zip("GHIJKL", FOLIAGE_DUSTY)),
    **dict(zip("MNOPQR", FOLIAGE_CROP)),
    **dict(zip("012345", IRON)),
    **dict(zip("STUVWX", HOP_CONE)),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def twine(x, y):
    band = (x + y) % 6
    return "e" if band == 0 else "c" if band in (1, 2) else "d" if band == 3 else "b"


def nail(x, y):
    if y < 3:
        return "3"
    return "1" if x > 11 else "2"


def hop(x, y):
    """A hop cone: rows of pale green bracts overlapping like scales, the rows offset by half a bract. Each is lit along
    its rounded top and shaded where the next row tucks over it, darkest only in the notch between two tips, so the
    cone reads as scales rather than bricks even squeezed onto a small part."""
    row = y // 4
    within = (x + (2 if row % 2 else 0)) % 4
    t = y % 4
    if t == 0:
        return "X" if within in (1, 2) else "W"          # lit along each bract's top
    if t == 3:
        return "S" if within == 0 else "T"               # tucked under the next row, darkest between tips
    if within == 0 and t == 2:
        return "U"                                       # the edge where two bracts overlap
    return "V" if row < 2 else "U"


def bine(x, y):
    return "h" if x % 5 == 0 else "i" if (x + y) % 7 else "j"


def hop_leaf(x, y):
    if x in (7, 8):
        return "k"                                       # the vein
    return "i" if (x + y) % 5 else "h"                   # a dried leaf, green-brown


def lavender(x, y):
    """Florets in little whorls: dots of light purple on deep purple."""
    if (x + (y // 2) % 2 * 2) % 4 == 0 and y % 2 == 0:
        return "r"
    if (x + y) % 3 == 0:
        return "p"
    return "o" if y < 8 else "n"


def lavender_stem(x, y):
    return "J" if x % 4 == 1 else "I" if x % 4 == 3 else "K"


def garlic(x, y):
    """Papery white, the cloves' seams running down, a purple blush low on it."""
    if x in (0, 15) or y == 15:
        return "u"
    if x in (5, 10):
        return "v"                                       # seams between the cloves
    if y >= 12:
        return "t" if (x + y) % 3 == 0 else "u"          # the purple blush
    if y < 3:
        return "x"
    return "w" if (x * 3 + y) % 9 else "x"


def braid(x, y):
    """Three strands crossing: a chevron down the braid."""
    k = (abs(x - 7.5) // 1 + y) % 6
    return "e" if k == 0 else "d" if k < 3 else "c" if k < 5 else "b"


def chili(x, y):
    if x in (2, 3) and y < 13:
        return "F" if y < 6 else "E"                     # the glossy highlight
    if x > 12:
        return "B"
    return "D" if y < 10 else "C"


def chili_cap(x, y):
    return "Q" if y < 6 else "O" if (x + y) % 4 else "N"


TEXTURES = {
    "bundle_twine": rows(twine),
    "bundle_nail": rows(nail),
    "bundle_hop": rows(hop),
    "bundle_hop_bine": rows(bine),
    "bundle_hop_leaf": rows(hop_leaf),
    "bundle_lavender": rows(lavender),
    "bundle_lavender_stem": rows(lavender_stem),
    "bundle_garlic": rows(garlic),
    "bundle_braid": rows(braid),
    "bundle_chili": rows(chili),
    "bundle_chili_cap": rows(chili_cap),
}

COMPARE = ["block/hay_block_side", "item/string"]
