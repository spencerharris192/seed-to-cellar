"""Red grape cutting: a length of woody cane with two green buds, a paper nursery tag tied to its top
with a dot of the grape's color (white_grape_cutting.py recolors the dot), so the two read apart.
"""
from texturegen.compose import blank, to_grid
from texturegen.palettes import FLOUR, FOLIAGE_CROP, RED_GRAPE, ROOT

# w W x cane (shadow, lit, cut end); g h buds; p P tag paper; t string; 1 2 grape dot
LEGEND = {
    "w": ROOT[1], "W": ROOT[3], "x": ROOT[4],
    "g": FOLIAGE_CROP[3], "h": FOLIAGE_CROP[4],
    "p": FLOUR[2], "P": FLOUR[4],
    "t": ROOT[2],
    "1": RED_GRAPE[2], "2": RED_GRAPE[4],
}

TAG = """
.pppp
pPPPp
pP21p
pPP1p
.pppp
"""


def build() -> str:
    c = blank()
    for i in range(10):                     # the cane, lying on the diagonal: lit on top, shadow under
        x, y = 2 + i, 14 - i
        c[y][x] = "W"
        c[y][x + 1] = "w"
    c[14][2] = "x"                          # the fresh cut at its foot
    for x, y in [(5, 10), (8, 7)]:          # buds breaking
        c[y][x] = "g"
        c[y - 1][x] = "h"
    for x, y in [(11, 4), (11, 3)]:         # string up to the tag
        c[y][x] = "t"
    for dy, row in enumerate(TAG.strip("\n").splitlines()):
        for dx, ch in enumerate(row):
            if ch != ".":
                c[dy][10 + dx] = ch
    return to_grid(c)


GRID = build()

COMPARE = ["item/stick", "item/sugar_cane", "item/bamboo"]
