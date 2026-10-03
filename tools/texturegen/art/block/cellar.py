"""Cellar blocks: the oak aging cask (lying on its side) and the small upright keg.

Cask side: staves run lengthwise (horizontal here; the model turns them for the top face),
bound by two iron hoops. Cask head: the round end, planks across with an iron rim ring and a bung near the top.
Keg: vertical staves with two bands; keg top: planks with a bung.
Staves reuse the oak ramp the tub and vat use, so all cellar woodwork matches.
Charred casks (<wood>_cask_side_charred, _head_charred): the same drawing two shades darker in the wood's own ramp, the
hoops blackened, so a charred cask reads as toasted but still shows its wood.
"""
from texturegen.palettes import IRON, WOOD_OAK

LEGEND = {**dict(zip("012345", WOOD_OAK)), **dict(zip("ijklmn", IRON))}

STAVE_ROWS = "4332"


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


# Hoops across the staves: a lit outer column and a shaded inner one. The layout is mirror-
# symmetric (x -> 15 - x), because Minecraft shows opposite faces mirrored: any asymmetry makes
# the hoops on one side and the bottom miss the ones on the top by a pixel.
HOOP_OUTER = (3, 12)
HOOP_INNER = (4, 11)


# Charring: every wood shade two steps darker, the iron hoops one step.
CHAR = {"5": "3", "4": "2", "3": "1", "2": "0", "1": "0", "n": "l", "m": "k", "l": "j", "k": "i", "j": "i"}


def charred(grid: str) -> str:
    return "".join(CHAR.get(ch, ch) for ch in grid)


def cask_side(x, y):
    if x in HOOP_OUTER:
        return "m" if y % 5 == 2 else "l"   # rivets
    if x in HOOP_INNER:
        return "k"
    return STAVE_ROWS[y % 4]


def cask_head(x, y):
    # round-ish end: corners are the rim, planks run across
    cx, cy = x - 7.5, y - 7.5
    r = (cx * cx + cy * cy) ** 0.5
    if r > 7.6:
        return "k"
    if r > 6.4:
        return "l" if (x + y) % 3 else "m"   # iron rim ring
    if x in (7, 8) and y in (4, 5):
        return "5" if y == 4 else "1"         # the bung: a lit wooden plug over its shadow
    if y % 4 == 3:
        return "1"                            # seams between planks
    return "3" if y % 4 == 0 else ("2" if x % 7 == 3 else "3")


def keg_side(x, y):
    # The keg model shows rows 4-15 of this texture, so both bands must sit in that range.
    if y in (6, 12):
        return "l" if x % 5 else "m"
    if y in (7, 13):
        return "j"
    return STAVE_ROWS[x % 4]


def keg_top(x, y):
    if x in (0, 15) or y in (0, 15):
        return "j"
    if 6 <= x <= 8 and 6 <= y <= 8:
        return "0" if (x, y) == (7, 7) else "1"   # the bung
    return "1" if x % 4 == 3 else "3"


TEXTURES = {
    "oak_cask_side": rows(cask_side),
    "oak_cask_head": rows(cask_head),
    "oak_cask_side_charred": charred(rows(cask_side)),
    "oak_cask_head_charred": charred(rows(cask_head)),
    "keg_side": rows(keg_side),
    "keg_top": rows(keg_top),
}

COMPARE = ["block/barrel_side", "block/barrel_top", "block/oak_planks"]
