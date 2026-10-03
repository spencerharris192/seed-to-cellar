"""Red grapes: a hanging bunch on its woody stalk, with a lobed leaf at the top.

The bunch is a dark cone with round berries packed over it (each a small lit diamond), so the gaps
between berries read as shadow; back rows are drawn first so the front ones overlap them.
white_grapes.py recolors it.
"""
from texturegen.compose import blank, to_grid
from texturegen.palettes import FOLIAGE_CROP, RED_GRAPE, ROOT

# 0-5 grapes (darkest -> bloom); w W stalk; d f g h leaf (dark -> light)
LEGEND = {
    **dict(zip("012345", RED_GRAPE)),
    "w": ROOT[1], "W": ROOT[2],
    **dict(zip("dfgh", FOLIAGE_CROP[1:5])),
}

LEAF = """
..h.h..
.hghghg
hgggggf
.fggfd.
..fd...
"""

# Berry centers, back rows first; the bunch narrows toward its tip.
BERRIES = [(5, 5), (7, 5), (9, 5), (11, 5),
           (4, 7), (6, 7), (8, 7), (10, 7), (12, 7),
           (5, 9), (7, 9), (9, 9), (11, 9),
           (6, 11), (8, 11), (10, 11),
           (7, 13), (9, 13),
           (8, 15)]


def build() -> str:
    c = blank()
    # the dark body of the bunch, showing between the berries
    for x, y in BERRIES:
        for dx, dy in ((0, -1), (-1, 0), (0, 0), (1, 0), (0, 1)):
            if 0 <= y + dy < 16:
                c[y + dy][x + dx] = "0"
    for x, y in BERRIES:
        c[y][x] = "3"
        c[y - 1][x] = "4"
        c[y][x - 1] = "4" if (x + y) % 4 else "5"   # a few berries catch the light: bloom
        c[y][x + 1] = "2"
        if y + 1 < 16:
            c[y + 1][x] = "1"
    for x, y in [(10, 3), (11, 2), (12, 1), (12, 0)]:  # the stalk
        c[y][x] = "w"
    c[3][9] = "W"
    for dy, row in enumerate(LEAF.strip("\n").splitlines()):
        for dx, ch in enumerate(row):
            if ch != ".":
                c[dy][2 + dx] = ch
    return to_grid(c)


GRID = build()

COMPARE = ["item/sweet_berries", "item/glow_berries", "item/chorus_fruit"]
