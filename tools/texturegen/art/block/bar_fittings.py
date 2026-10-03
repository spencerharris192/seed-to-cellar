"""The tavern set's fittings:

- bar_brass: the Bar Counter's polished brass foot rail and brackets: warm gold, a bright line along its top, darker below.
- bar_cushion: the Bar Stool's deep red leather cushion: a buttoned center, stitching round its edge, a soft sheen.
"""
from texturegen.palettes import FRUIT_RED, GOLD

# A-F brass; r-w leather
LEGEND = {
    **dict(zip("ABCDEF", GOLD)),
    **dict(zip("rstuvw", FRUIT_RED)),
}


def rows(fn):
    return "\n".join("".join(fn(x, y) for x in range(16)) for y in range(16))


def brass(x, y):
    if y < 3:
        return "F" if (x + y) % 5 else "E"           # the bright line along its top
    if y > 12:
        return "B"
    return "D" if (x * 3 + y) % 7 else "E"


def cushion(x, y):
    if x in (1, 14) or y in (1, 14):
        return "w" if (x + y) % 2 == 0 else "u"      # the stitching round its edge
    if x in (0, 15) or y in (0, 15):
        return "s"
    if (x, y) in ((7, 7), (8, 8), (7, 8), (8, 7)):
        return "r"                                   # the button
    if (abs(x - 7.5) + abs(y - 7.5)) < 3:
        return "t"                                   # the leather pulled in round it
    return "v" if x + y < 12 else "u"                # a soft sheen, upper left


TEXTURES = {
    "bar_brass": rows(brass),
    "bar_cushion": rows(cushion),
}

COMPARE = ["block/gold_block"]
