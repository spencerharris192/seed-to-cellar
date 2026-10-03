"""Berry items: a handful of each, from the same berry shape so they read as one family, told
apart by color and size.

- blueberries: dusty blue with a pale bloom
- blackberries: bigger, bumpy, glossy purple-black
- cranberries: small, bright red
- juniper berries: small, frosted blue, with a sprig of needles
"""
from texturegen.compose import blank, sibling, stamp, to_grid
from texturegen.palettes import BLACKBERRY, BLUEBERRY, FOLIAGE_DUSTY, FRUIT_RED

_b = sibling(__file__, "_berry")

# 0-5 BLUEBERRY, A-F BLACKBERRY, a b c d g h FRUIT_RED, q-y FOLIAGE_DUSTY (juniper needles)
LEGEND = {
    **{str(i): BLUEBERRY[i] for i in range(6)},
    **dict(zip("ABCDEF", BLACKBERRY)),
    **dict(zip("abcdgh", FRUIT_RED)),
    **dict(zip("qwerty", FOLIAGE_DUSTY)),
}

BLACKBERRY_SHAPE = """
.EFE.
EFDED
DEDCD
CDCDC
.BCB.
"""


def recolor_digits(sprite: str, letters: str) -> str:
    """Moves a digit sprite (0-5) onto another ramp's letters."""
    return "".join(letters[int(ch)] if ch.isdigit() else ch for ch in sprite)


def blackberries():
    c = blank()
    for x, y in ((6, 2), (2, 6), (9, 7), (5, 10)):
        stamp(c, BLACKBERRY_SHAPE, x, y)
    return to_grid(c)


def juniper():
    needles = ((3, 12, "e"), (4, 11, "r"), (5, 10, "e"), (6, 9, "r"), (7, 8, "e"), (2, 11, "t"), (4, 13, "r"),
               (5, 12, "t"), (6, 11, "e"), (8, 7, "r"), (9, 6, "e"), (7, 10, "t"))
    return _b.cluster(_b.SMALL, ((9, 3), (11, 6), (7, 5), (10, 9), (12, 11)), stem=needles)


TEXTURES = {
    "blueberries": _b.cluster(),
    "blackberries": blackberries(),
    "cranberries": recolor_digits(_b.cluster(_b.SMALL, ((8, 3), (4, 5), (11, 6), (7, 8), (3, 10), (10, 11), (6, 12))), "abcdgh"),
    "juniper_berries": juniper(),
}

COMPARE = ["item/sweet_berries", "item/glow_berries"]
